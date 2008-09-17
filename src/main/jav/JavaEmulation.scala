package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

class ReturnWrapper(node: ASTNode, nestingLevels: Int) {
	import org.eclipse.jdt.core.dom.{ PrimitiveType => PT }

	def this(node: ASTNode) = this(node, 1)	
	lazy val returnType: Option[dom.Type] = 
		node.findEnclosingMethod.map(_.getReturnType2).filter(_ != null)
	
	def emitReturnType: Emission = returnType match {
		case None => UNIT
		case Some(JPrimitive(PT.VOID)) => UNIT
		case Some(x) => x
	}	
	def emitReturnExpr: Emission = COLON ~ emitReturnExpr(emitReturnType, nestingLevels) 
	def emitReturnExpr(moreLevels: Int): Emission = 
		COLON ~ emitReturnExpr(emitReturnType, nestingLevels + moreLevels)
	// RETCLASS <~> BRACKETS(emitReturnType)
	
	private def emitReturnExpr(expr: Emission, levels: Int): Emission =
		if (levels == 0) expr
		else RETCLASS <~> BRACKETS(emitReturnExpr(expr, levels - 1))
}


// class BreakableStmt(override val node: dom.Statement) extends Statement(node)
// {
// 	lazy val hasReturn = node.descendants exists { case _: dom.ReturnStatement => true ; case _ => false }
// 	lazy val hasLocalBreakOnly = node.descendants forall { 
// 		case BreakStatement(None) => true
// 		case x if x.isJumpStmt => false
// 		case _ => true
// 	}
// 	
// 	override def emitDirect: Emission = node match {
// 		case EnhancedForStatement(SingleVariableDeclaration(_, _, _, name, _, _), expr, body) =>
// 			FOR ~ PARENS(name ~ ARROWLEFT ~ expr) ~ body
// 		case _ => emitLoop
// 	}
// 	
// 	// if a LoopName is attached, we emit as function ; otherwise simple loops are those without a break or continue
// 	def emitLoop: Emission = {
// 		def bodyStmts(x: dom.Statement, updates: List[dom.Expression]): Emission = BRACES(REP(x.stmts) ~ REPSEP(updates, NL))
// 	
// 		node match {
// 			case DoStatement(expr, body) => DO ~ BRACES(body) ~ WHILE ~ PARENS(expr) ~ NL
// 			case WhileStatement(expr, body) => WHILE ~ PARENS(expr) ~ OPTBRACES(body)
// 			case ForStatement(inits, expr, updates, body) =>
// 				REP(inits, _ ~ NL) ~ WHILE ~ PARENS(emitOpt(expr, x => x, TRUE)) ~ bodyStmts(body, updates)
// 
// 			case _ => ERROR("Unimplemented simple loop")
// 		}
// 	}
// }
// 

class SwitchStatement(override val node: dom.SwitchStatement) extends Statement(node)
{
	lazy val SwitchStatement(expr, statements) = node
	lazy val cases: List[CaseClass] = node match {
		case SwitchCases(xs @ _*) => xs.toList
		case _ => abort("Invalid switch arrangement")
	}
	
	override def addsNestingLevel: Boolean = !isJavaStyle

	// we drop one off the end for the check because the last case can fall through irrelevantly
	lazy val isJavaStyle = cases.dropRight(1).forall(!_.fallThrough) && cases.forall(!_.hasReturn)
	
	// if we're lucky every case except the last will end with a break or a return
	override def emitDirect: Emission = 
		if (isJavaStyle) expr ~ MATCH ~ BRACES(cases.flatMap(_.emitJavaStyle))
		// else if (hasLocalBreakOnly) emitSwitchLocalBreakOnly
		else emitSwitchAsFunction
	    
	private def methodBodies(xs: List[CaseClass]): List[Emission] = {
		val names = xs.tail.map(_.name) ::: List(Nil)
		for ((c, next) <- xs.zip(names)) yield c.emitMethodBody(next)
	}
		
	// 
	// private def emitSwitchLocalBreakOnly: Emission = {		
	// }
		
	private def emitSwitchAsFunction: Emission = {
		lazy val wrapper = new ReturnWrapper(node)
		val arg = emitString("`!arg`")
		val fname = createIdentifier
		val body = REP(cases.map(_.emitJavaStyle))
		val processRetval: Emission = 
			if (isInConstructor) NL 
			else MATCH ~ BRACES(
				CASE ~ RETVAL <~> PARENS(X) ~ FUNARROW ~ RETURN ~ emitRetVal ~ NL ~
				CASE ~ UNDERSCORE ~ FUNARROW ~ NL
			)
		
		DEF ~ fname <~> PARENS(arg <~> COLON ~ expr.tb.emitType) <~> 
		wrapper.emitReturnExpr ~
		EQUALS ~ arg ~ MATCH ~ BRACES(body) ~ NL ~
		fname <~> PARENS(expr) ~ processRetval
	}
}


trait JavaEmulation
{
	self: Node =>
	
	val DEFAULT_CASE = "Default"
	
	case class DefaultCase(s: List[dom.Statement], f: Boolean) extends CaseClass(UNDERSCORE, Nil, s, f)
	
	// I'll never get a better chance to use this name
	case class CaseClass(name: Emission, expr: List[dom.Expression], stmts: List[dom.Statement], fallThrough: Boolean) {
		lazy val hasReturn: Boolean = 
			stmts.flatMap { _.descendants } exists { 
				case _: dom.ReturnStatement => true
				case _ => false
			}
			
		def emitExpr: Emission = if (expr.isEmpty) UNDERSCORE else REPSEP(expr, NOS ::: PIPE ::: NOS)
		
		def emitMethodBody(next: Emission): Emission = 
			DEF ~ name ~ COLON ~ UNIT ~ EQUALS ~ 
			BRACES(REP(stmts) ~ emitCond(fallThrough, next, NL))
		
		def emitMethodInvocation: Emission =
			CASE ~ emitExpr ~ FUNARROW ~ name ~ NL
			
		private def isUnlabeledBreak(x: ASTNode): Boolean = x match {
			case BreakStatement(None) => true
			case _ => false
		}
				
		def emitJavaStyle: Emission = {
			val nonBreakStmts = if (!stmts.isEmpty && isUnlabeledBreak(stmts.last)) stmts.init else stmts	
					
			CASE ~ emitExpr ~ FUNARROW ~ INDENT(REP(nonBreakStmts))				
		}		
	}
			
	object SwitchCases {
		// def caseName(expr: Option[Expression]) =
		// 	if (expr.isEmpty) DEFAULT_CASE
		// 	else {
		// 		val name = expr.get.toString.replaceAll("""[^A-Za-z0-9\-]""", "")
		// 		if (name.matches("""^[a-zA-z]""")) name
		// 		else IdGen.get
		// 	}
		
		def arrangeCases(stmts: List[dom.Statement]): List[CaseClass] = stmts match {
			case Nil => Nil
			case SwitchCase(expr) :: xs =>
				// val (someStmts, rest) = xs.span(x => !x.isInstanceOf[SwitchCase])
				val (someStmts, rest) = xs span { case _: dom.SwitchCase => false ; case _ => true }
				// this only flags a subset of fallThroughs, but it should be right about it
				val fallThrough = someStmts.isEmpty || !someStmts.last.isJumpStmt				

				(if (expr.isDefined) CaseClass(createIdentifier, List(expr.get), someStmts, fallThrough)
				else DefaultCase(someStmts, fallThrough)) :: arrangeCases(rest)
				
			// no switchcase in front of the statements, or other unknown error
			case _ => Nil
		}
		
		def combineCases(c1: CaseClass, c2: CaseClass): CaseClass =
			CaseClass(createIdentifier, c1.expr ::: c2.expr, c1.stmts ::: c2.stmts, c2.fallThrough)

		def squeezeEmpties(cases: List[CaseClass]): List[CaseClass] = cases match {
			case Nil|_::Nil => cases
			case c1 :: c2 :: rest if c1.fallThrough => squeezeEmpties(combineCases(c1, c2) :: rest)
			case _ => cases.head :: squeezeEmpties(cases.tail)
		}

		// in scala, no match throws an exception, but in java it's a no-op.
		// So we insert a no-op default case if there's not one already.
		def ensureDefaultCase(xs: List[CaseClass]) =
			if (xs.exists { case DefaultCase(_, _) => true ; case _ => false }) xs
			else xs ::: List(DefaultCase(Nil, false))
				
        def unapplySeq(node: dom.SwitchStatement) = node match {
            case SwitchStatement(expr, stmts) =>
				val cases = arrangeCases(stmts)
				if (cases.isEmpty) None else Some(squeezeEmpties(ensureDefaultCase(cases)))
				
			case _ => None
		}
	}
			
	// we'd like to determine they're using a common for idiom like (i = 0; i < foo; i++)...	
	// def emitForLoop: Emission = node match {
	// 	case ForStatement(init :: Nil, expr, updater :: Nil, body) =>
	// 		val (name, startVal) = init match {
	// 			case VariableDeclarationExpression(_, _, VariableDeclarationFragment(name, _, Some(rhs)) :: Nil) =>
	// 				(name, rhs)
	// 			case Assignment(name: SimpleName, _, rhs) => (name, rhs)
	// 			case _ => return emitLoop
	// 		}
	// 	
	// 		return expr match {
	// 			case Some(InfixExpression(lhs: SimpleName, JavaOp(op), rhs, Nil)) 
	// 			if (List("<=", "<", ">=", ">") contains op) && (name.vbinding == lhs.vbinding) => {
	// 					val range = op match {
	// 						case "<=" => emitRange(startVal, rhs, true)
	// 						case "<" => emitRange(startVal, rhs, false)
	// 						case ">=" => emitRange(startVal, rhs, true, MINUSONE)
	// 						case ">" => emitRange(startVal, rhs, false, MINUSONE)
	// 					}
	// 			
	// 					FOR ~ PARENS(name ~ ARROWLEFT ~ range) ~ body
	// 			}
	// 			case _ => emitLoop
	// 		}
	// 
	// 	case _ => emitLoop
	// }
}

// def outerLoop(): Option[T] = {
// 	inits
// 	
// 	def innerLoop() = {
// 		body
// 		break => Break
// 		continue => Continue
// 	}
// 	def update = {
// 		updates
// 		innerLoop()
// 	}
// 		
// 	while (cond) {
// 		innerLoop() match {
// 			case Return("value") => return Some(value)
// 			case Break => return None
// 			case Continue =>
// 		}
// 		update()
// 	}
// }
// 
// outerLoop match {
// 	case Some(x) => return x
// 	case None =>
// }


// trait ComplexLoop {
// 	self: Statement =>
// 	
// 	val jumpers: List[dom.Statement]
// 	val breakTo: Boolean
// 	val continueTo: Boolean
// 	
// 	def emitLoop: Emission = {
// 		COMMENT("I have " + jumpers.size + " jumpers") ~ NL
// 	}	
// }

// object c3x4_bonus {
//     def cFor (init: =>unit)
//              (p:    =>boolean)
//              (incr: =>unit)
//              (s:    =>unit)
//     : unit =
//     {
//         init; while(p) { s ; incr }
//     