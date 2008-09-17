package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

// http://notes-on-haskell.blogspot.com/2007/02/whats-wrong-with-for-loop.html
class ForStatement(override val node: dom.ForStatement) extends Statement(node) with Loop
{
	lazy val ForStatement(inits, expr, updates, body) = node
	lazy val bodyStmts: Emission = BRACES(REP(body.stmts) ~ REPSEP(updates, NL))
	
	def emitSimple  = REP(inits, _ ~ NL) ~ WHILE ~ PARENS(emitOpt(expr, x => x, TRUE)) ~ bodyStmts
	def emitComplex = emitLoop(REP(inits, _ ~ NL), emitOpt(expr, x => x, TRUE), padBody(body), REPSEP(updates, NL), false)
}

class EnhancedForStatement(override val node: dom.EnhancedForStatement) extends Statement(node) with Loop
{
	lazy val EnhancedForStatement(param, expr, body) = node
	lazy val SingleVariableDeclaration(_, _, _, name, _, _) = param
	
	def emitSimple  = FOR ~ PARENS(name ~ ARROWLEFT ~ expr) ~ body
	def emitComplex = abort("Unimplemented")
}

class WhileStatement(override val node: dom.WhileStatement) extends Statement(node) with Loop
{
	lazy val WhileStatement(expr, body) = node
	
	def emitSimple  = WHILE ~ PARENS(expr) ~ OPTBRACES(body)
	def emitComplex = emitLoop(Nil, expr, padBody(body), Nil, false)
}

class DoStatement(override val node: dom.DoStatement) extends Statement(node) with Loop
{
	lazy val DoStatement(expr, body) = node
	
	def emitSimple  = DO ~ BRACES(body) ~ WHILE ~ PARENS(expr) ~ NL
	def emitComplex = emitLoop(Nil, expr, padBody(body), Nil, true)
}

trait Breakable extends Statement
{
	self: Statement =>

	def emitSimple: Emission
	def emitComplex: Emission
	override def emitDirect: Emission = if (isSimple) emitSimple else emitComplex
	override def addsNestingLevel = !hasNoJumps
	
	lazy val hasNoJumps = node.descendants.forall { case x if x.isJumpStmt => false ; case _ => true }
	lazy val isSimple: Boolean = hasNoJumps
	
	// lazy val hasReturn = node.descendants exists { case _: dom.ReturnStatement => true ; case _ => false }
	// lazy val hasLocalBreakOnly = node.descendants forall { 
	// 	case BreakStatement(None) => true
	// 	case x if x.isJumpStmt => false
	// 	case _ => true
	// }		
}

// loops to be represented as functions
trait Loop extends Breakable 
{
	self: Statement =>
	
	lazy val wrapper = new ReturnWrapper(node)
	protected def padBody(body: dom.Statement): Emission = {
		val needsDummy = !body.stmts.last.isJumpStmt
		body.emit ~ (if (needsDummy) RETURN ~ CONTINUE ~ NL else Nil)
	}
	
	lazy val processRetval: Emission = 
		if (isInConstructor) NL 
		else MATCH ~ BRACES(
			CASE ~ RETVAL <~> PARENS(X) ~ FUNARROW ~ RETURN ~ emitRetVal ~ NL ~
			CASE ~ UNDERSCORE ~ FUNARROW ~ NL
		)
		
	def emitLoop(inits: Emission, cond: Emission, body: Emission, updates: Emission, isDo: Boolean): Emission = {
		val outerLoopName = getLabel.map(_.emit) getOrElse createIdentifier
		val innerLoopName = createIdentifier
		val updateName: Emission = if (updates != Nil) createIdentifier else Nil
		val innerLoop = DEF ~ innerLoopName ~ wrapper.emitReturnExpr ~ EQUALS ~ BRACES(body)
		val update: Emission = if (updates != Nil) DEF ~ updateName ~ wrapper.emitReturnExpr ~ EQUALS ~ BRACES(updates) else Nil
		val whileLoop = 
			WHILE ~ PARENS(cond) ~ BRACES(
				innerLoopName ~ MATCH ~ BRACES(
					CASE ~ RETVAL <~> PARENS(X) ~ FUNARROW ~ RETURN ~ RETVAL <~> PARENS(X) ~ NL ~
					CASE ~ BREAK ~ FUNARROW ~ RETURN ~ BREAK ~ NL ~
					CASE ~ CONTINUE ~ FUNARROW ~ NL
				) ~
				updateName ~ NL
			)
		val outerLoop =
			DEF ~ outerLoopName ~ wrapper.emitReturnExpr ~ EQUALS ~ BRACES(
				inits ~ innerLoop ~ update ~ whileLoop ~ RETURN ~ emitString("Void") ~ NL
			)
			
		outerLoop ~ NL ~ outerLoopName ~ processRetval
	}
}
