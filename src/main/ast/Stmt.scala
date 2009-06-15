package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
// import scalaz.OptionW._

// Statement Classes:
// 
// Block IfStatement ForStatement EnhancedForStatement WhileStatement
// DoStatement TryStatement SwitchStatement SynchronizedStatement
// ReturnStatement ThrowStatement BreakStatement ContinueStatement
// EmptyStatement ExpressionStatement LabeledStatement AssertStatement
// VariableDeclarationStatement TypeDeclarationStatement
// ConstructorInvocation SuperConstructorInvocation

class ConstructorInvocation(override val node: dom.ConstructorInvocation)
extends Statement(node) with MethodBound
{
	lazy val ConstructorInvocation(typeArgs, args) = node
	def mb = node.resolveConstructorBinding
	override def binding = super[MethodBound].binding
	lazy val tb = mb.getDeclaringClass
	
	override def emitDirect: Emission = THIS <~> TYPEARGS(typeArgs) <~> PARENS(ARGS(args)) ~ NL
}

class SuperConstructorInvocation(override val node: dom.SuperConstructorInvocation)
extends Statement(node) with MethodBound
{
	lazy val SuperConstructorInvocation(expr, typeArgs, args) = node
	override def binding = super[MethodBound].binding
	def mb = node.resolveConstructorBinding
	
	lazy val superExpr: Emission =
		(for (etype <- node.findEnclosingType ; superType <- etype.superType) yield 
			superType.emitExprWhenSuper(Some(node))) getOrElse Nil
		
	override def emitDirect: Emission = {
		log.trace("SuperConstructorInvocation emitDirect: %s", superExpr)
		superExpr
	}
}


class ReturnStatement(override val node: dom.ReturnStatement) extends Statement(node)
{
	lazy val ReturnStatement(expr) = node
	lazy val inConstructor = findEnclosingMethod.map(_.isConstructor) getOrElse false

	override def emitDirect: Emission =
		if (inConstructor) THROW ~ NEW ~ emitString("ConstructorEarlyReturn") ~ NL
		else if (getNestingLevel == 0) RETURN ~ expr ~ NL
		else RETVAL <~> PARENS(expr)
}

// needs leading NL lest a new on the previous line think the braces mean it's a closure
class LabeledStatement(override val node: dom.LabeledStatement) extends Statement(node)
{
	lazy val LabeledStatement(label, body) = node	
	override def emitDirect: Emission = COMMENTOUT(label <~> COLON) ~ NL
}

class BreakStatement(override val node: dom.BreakStatement) extends Statement(node)
{
	lazy val BreakStatement(expr) = node
	override def emitDirect: Emission = expr match {
		case None => RETURN ~ BREAK ~ NL
		case Some(label) => RETURN ~ LBREAK <~> PARENS(QUOTES(label)) ~ NL
	}
}

class ContinueStatement(override val node: dom.ContinueStatement) extends Statement(node)
{
	lazy val ContinueStatement(expr) = node
	override def emitDirect: Emission = expr match {
		case None => RETURN ~ CONTINUE ~ NL
		case Some(label) => RETURN ~ LCONTINUE <~> PARENS(QUOTES(label)) ~ NL
	}
}

class IfStatement(override val node: dom.IfStatement) extends Statement(node)
{
	lazy val IfStatement(expr, thenStmt, elseStmt) = node
	lazy val elsePart: Emission = elseStmt match {
		case Some(x: dom.IfStatement) => ELSE ~ x
		case Some(Block(Nil)) => BRACES
		case Some(x) => ELSE ~ OPTBRACES(x)
		case _ => Nil
	}
	
	override def emitDirect: Emission = IF ~ PARENS(expr) ~ OPTBRACES(thenStmt) ~ elsePart	
}

class TryStatement(override val node: dom.TryStatement) extends Statement(node)
{
	lazy val TryStatement(body, catchClauses, fin) = node
	override def emitDirect: Emission = emitTry(body, catchClauses.map(_.emit), fin.map(_.emit) getOrElse Nil)	
}

// scala requires case variables to start with a lower case letter
class CatchClause(override val node: dom.CatchClause) extends MiscNode(node)
{
	lazy val CatchClause(exception, Block(stmts)) = node
	lazy val SingleVariableDeclaration(_, jtype, _, name, _, _) = exception
	lazy val SimpleName(ident) = name
	lazy val lcName = toLower(ident)		// TODO - incorporate this into proper renaming
		
	override def emitDirect: Emission = 
		CASE ~ emitString(lcName) ~ COLON ~ jtype ~ FUNARROW ~ INDENT(REP(stmts))
	
	private def toLower(s: String): String = s.toCharArray.toList match {
		case hd :: tl => (hd.toLowerCase :: tl) mkString
		case _ => s
	}
}

class Block(override val node: dom.Block) extends Statement(node)
{
	lazy val Block(statements) = node
	// if they bust out with a block in the middle of wherever, we need whitespace to avoid it looking like refinement
	lazy val needsWhitespace = node.getParent match {
		case x: dom.Block => true
		case _ => false
		// case x: dom.BodyDeclaration => false
		// case x if x.isLoopStmt => false
	}
	override def stmts: List[dom.Statement] = statements
	override def emitDirect: Emission = emitCond(needsWhitespace, NL) ~ OPTBRACES(stmts)
	def allFragments: List[dom.VariableDeclarationFragment] = stmts
		. flatMap { case x: dom.VariableDeclarationStatement => List(x) ; case _ => Nil }
		. flatMap { _.frags }
}

class VariableDeclarationStatement(override val node: dom.VariableDeclarationStatement) extends Statement(node)
{
	lazy val VariableDeclarationStatement(modifiers, jtype, frags) = node
	
	// if it's final but without initializer, we defer declaration to the assignment site
	private def emitFragment(x: dom.VariableDeclarationFragment) = if (x.isDeferredVal) Nil else x.emitValOrVar ~ x ~ NL
		
	override def emitDirect: Emission = REP(frags.map(emitFragment))
}

class Statement(override val node: dom.Statement) extends Node(node)
{
	def addsNestingLevel: Boolean = false		// subclassed
	def emitRetVal: Emission = if (getNestingLevel > 1) RETVAL <~> PARENS(X) else X
	def getLabel: Option[dom.SimpleName] = parent match {
		case LabeledStatement(label, _) => Some(label)
		case _ => None
	}
	def stmts: List[dom.Statement] = List(node)	// expanded by blocks
		
	override def emitDirect: Emission = node match {
		case x: dom.EmptyStatement => Nil
		case SynchronizedStatement(expr, body) => INVOKE(expr, SYNCHRONIZED) ~ BRACES(body)
		case ThrowStatement(expr) => THROW ~ expr ~ NL
		case ExpressionStatement(expr) => expr ~ NL
		case AssertStatement(expr, None) => ASSERT <~> PARENS(expr) ~ NL
		case AssertStatement(expr, Some(msg)) => ASSERT <~> METHODARGS(List(expr, msg)) ~ NL
		case TypeDeclarationStatement(statement) => statement ~ NL
		case _ => ERROR("Unreachable: " + node.getClass.getName)
	}
}