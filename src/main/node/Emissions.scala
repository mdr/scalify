package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom

// Emit and Emission are the fundamental units of output
case class Emit(val s: String)

trait Emissions
{
	self: Scalify.type =>

	def createIdentifier: Emission = createIdentifier("")
	def createIdentifier(prefix: String): Emission = emitString(prefix + IdGen.get)
	val identity: EFilter = (x: Emission) => x
	def defaultFilter(x: Emission) = INDENT(x)
	def emitString = SToken.emitString _
	
	val BRACEPAIR = (LBRACE, RBRACE)
	val PARENPAIR = (LPAREN, RPAREN)
	val BRACKETPAIR = (LBRACKET, RBRACKET)
	val QUOTES = (QUOTE, QUOTE)
	
	def OPTBRACES(stmt: dom.Statement): Emission = BRACES(stmt)		// OPTBRACES(stmt, defaultFilter _)
	def OPTBRACES(stmt: dom.Statement, filter: EFilter): Emission = BRACES(stmt)
		// if (stmt.isExpandableStmt) BRACES(stmt.emit)
		// else stmt match {
		// 	case Block(xs) if xs.forall(_.isInstanceOf[EmptyStatement]) => BRACES
		// 	case Block(stmts) => BRACES(REP(stmts))
		// 		// if (stmts.head.isExpandableStmt) BRACES(stmts.map(_.emit))
		// 		// else OPTBRACES(stmts.map(_.emit), filter)
		// 	case _ => filter(stmt.emit)
		// }
		
	def OPTBRACES(xss: List[Emission]): Emission = BRACES(xss)		// OPTBRACES(xss, defaultFilter _)
	def OPTBRACES(xss: List[Emission], filter: EFilter): Emission = BRACES(xss)
	// xss.size match {
	// 	case 0 => BRACES
	// 	case 1 => BRACES(REP(xss))		// INDENT(filter(xss.head))
	// 	case _ => BRACES(REP(xss))
	// }
	
	def BRACES: Emission = EMPTYBRACES ~ NL
	def BRACES(xss: Emission*): Emission = BRACES(xss.toList)
	def BRACES(xss: List[Emission]): Emission = REP(xss).embrace(BRACEPAIR, false)
	def QUOTES(xs: Emission): Emission = xs.embrace(QUOTES, true)
	def PARENS: Emission = LPAREN <~> RPAREN
	def PARENS(xs: Emission): Emission = xs.embrace(PARENPAIR, true)
	def BRACKETS(xs: Emission): Emission = xs.embrace(BRACKETPAIR, true)
	
	def INDENT(xs: Emission): Emission = if (xs.isEmpty) Nil else BRACES(xs) 		// INDENTSTART ~ xs ~ INDENTEND

	def TUPLE[T <: List[Emission]](xss: T) =
	    if (xss.size == 0) PARENS
	    else if (xss.size == 1) xss.head
	    else LPARENDOUBLE <~> ARGS(xss) <~> RPARENDOUBLE
	    
	def COMMENTOUT(msg: Emission): Emission = Emit("/*") ~ msg ~ Emit("*/")
	
	// assignment where the result is used - lhs means result is value after assignment, rhs means value before
	def ASSIGNLHS(lhs: Emission, op: Emission, rhs: Emission, isUsed: Boolean): Emission =
		if (isUsed) SETEQLHS <~> PARENS(lhs <~> COMMA ~ lhs ~ op ~ rhs)
		else lhs ~ op ~ rhs
	def ASSIGNRHS(lhs: Emission, op: Emission, rhs: Emission, isUsed: Boolean): Emission =
		if (isUsed) SETEQ <~> PARENS(lhs ~ op ~ rhs <~> COMMA ~ lhs)
		else lhs ~ op ~ rhs

	// comma separated list
	def ARGS[T <% List[Emission]](xss: T) =						// comma-separated list
		REPSEP(xss, NOS ::: COMMA)
	def METHODARGS[T <% List[Emission]](xss: T) =				// paren-enclosed, unless empty
		if (xss.isEmpty) Nil else PARENS(ARGS(xss))
	def TYPEARGS[T <% List[Emission]](xss: T) =					// bracket-enclosed, unless empty
		if (xss.isEmpty) Nil else NOS ~ BRACKETS(REPSEP(xss, NOS ::: COMMA))
	def INVOKE(xs: Emission, ys: Emission): Emission =			// invocation or field access e.g. name.val
		// if (ys.isEmpty) xs else (xs <~> DOT <~> ys)
		if ((xs?) && (ys?)) xs <~> DOT <~> ys
		else if (xs?) xs
		else if (ys?) ys
		else Nil
	def OPT(xs: Emission) =										// insert Option[ ... ] around something
		OPTION <~> BRACKETS(xs)
		
	def METHOD[T <% List[Emission], U <% List[Emission]](name: Emission, typeArgs: T, args: U) =
		name <~> TYPEARGS(typeArgs) <~> METHODARGS(args)
		
	def REP[T <% List[Emission]](xss: T) =								// emit each one as is
		List.flatten(xss)
	def REP[T <% List[Emission]](xss: T, f: EFilter) = 					// emit each one through filter
		xss.flatMap(f)

	def REPSEP[T <% List[Emission]](xss: T, sep: Emission) =			// emit each with separator
		if (xss.isEmpty) Nil else xss.reduceLeft(_ ~ sep ~ _)
	def REPSEP[T <% List[Emission]](xss: T, sep: Emission, f: EFilter) = 	// sep & filter
		if (xss.isEmpty) Nil else xss.map(f).reduceLeft(_ ~ sep ~ _)
	
	// try/catch
	def emitTry(tryPart: Emission, catchClauses: List[Emission]): Emission = emitTry(tryPart, catchClauses, Nil)
	def emitTry(tryPart: Emission, catchClauses: List[Emission], fin: Emission): Emission = {
		val catches: Emission =
			if (catchClauses.isEmpty) Nil
			else CATCH ~ BRACES(REP(catchClauses))

		TRY ~ BRACES(tryPart) ~ catches ~ (if (fin.isEmpty) Nil else FINALLY ~ BRACES(fin))
	}
		
	// invoke List.range
	def emitRange(start: ASTNode, end: ASTNode, inclusive: Boolean): Emission = emitRange(start, end, inclusive, Nil)	
	def emitRange(start: ASTNode, end: ASTNode, inclusive: Boolean, step: Emission) = {
		val endRange: Emission = if (inclusive) PARENS(end) ~ PLUS ~ ONE else end
		val stepArg: Emission = if (step.isEmpty) Nil else NOS ~ COMMA ~ step

		// TODO - qualify List.range selectively
		Emit("scala.List.range") <~> PARENS(start <~> COMMA ~ endRange ~ stepArg)
	}

	// one arg emits or Nil ; two args takes filter to apply to any result; three arg adds empty alternative
	def emitOpt[T <% Emission](x: Option[T]): Emission = emitOpt(x, x => x, Nil)
	def emitOpt[T <% Emission](x: Option[T], yes: EFilter): Emission = emitOpt(x, yes, Nil)
	def emitOpt[T <% Emission](x: Option[T], yes: EFilter, orElse: Emission): Emission =
		if (x.isDefined) yes(x.get) else orElse
	
	// 2nd arg is filter for whole output if list is non-empty; 3rd is output if empty
	def emitOptList(xs: List[Emission], f: (List[Emission]) => Emission): Emission = emitOptList(xs, f, Nil)
	def emitOptList(xs: List[Emission], f: (List[Emission]) => Emission, ifEmpty: Emission): Emission =
		if (xs.isEmpty) ifEmpty else f(xs)
		
	// emit list plus newline if non-empty
	def emitListNL(xs: List[Emission]): Emission = if (xs.isEmpty) Nil else REP(xs) ~ NL
		
	// emit if a conditional is true, otherwise Nil (or 3rd arg)
	// TODO - should be call by name
	def emitCond[T <% Emission](cond: Boolean, x: T): Emission = emitCond(cond, x, Nil)
	def emitCond[T <% Emission, U <% Emission](cond: Boolean, x: T, y: U): Emission = if (cond) x else y
			
	def arrayWrap(dim: Int): EFilter = 
		if (dim == 0) identity else (x) => ARRAY <~> BRACKETS(arrayWrap(dim - 1)(x))
			
	//
	// OBJECTS AND CLASSES
	//

	case object NOS extends Emit("")				// means "no space"
	case object BRK extends Emit("")				// tells the pretty printer where it can break long lines
	case object NOTOKEN extends Emit("")
	case object INDENTSTART extends Emit("")		// indentation not signalled by braces
	case object INDENTEND extends Emit("")
	case class ERROR(msg: String) extends Emit("/* XXX Scalify Error: " + msg + " */")
	case class COMMENT(msg: String) extends Emit(msg)
		
	class EmissionExtras(xs: Emission) {
		// ~ assembles two tokens with a space between ; <~> with no space
		def ~(ys: Emission): Emission = xs ::: ys
		def <~>(ys: Emission): Emission = 
			if (xs.isEmpty || ys.isEmpty) xs ::: ys 
			else if (xs.last == NOS || ys.head == NOS) xs ::: ys
			else xs ::: NOS ::: ys
		def ? : Boolean = !xs.isEmpty
		
		// avoid spurious enclosements
		def notWS(xs: Emission) = xs.find(x => x != NL && x != NOS).getOrElse(NOTOKEN)
		def embrace(pair: (Emit, Emit), tight: Boolean) = {
			val (l, r) = pair
			
			if (notWS(xs) == l && notWS(xs.reverse) == r && !(xs contains l)) xs
			else if (tight) l <~> xs <~> r
			else l ~ xs ~ r
		}
		
		def s: String = xs.map(_.s).mkString
		
		// def contents: String = (xs: List[Emit]).reduceLeft { case (Emit(s1), Emit(s2)) => s1.toString + s2.toString }
	}
	
	// Factories for pimped emissions and ho-hum emissions
	object EmissionExtras {
		def apply(in: Emission): EmissionExtras = {
			new EmissionExtras(in)
		}
	}
	object Emission {
		def apply(in: Emit): Emission = {
			List(in)
		}
	}
}
