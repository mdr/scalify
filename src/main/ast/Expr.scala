package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

// *** TRANSFORMATIONS TO DO ***
// 
// new Double(IntLiteral) => IntLiteral NOS "d"			OR	new java.lang.Double(IntLiteral)
//
// scratch:
//         Float one = new Float(14.78f - 13.78f);
//         Float oneAgain = Float.valueOf("1.0");
//         Double doubleOne = new Double(1.0);
// 
//         int difference = one.compareTo(oneAgain);


// used by expressions which imply assignment - which is not just Assignment
trait Assigns
{
	self: Expression =>
	val op: String

	// assume normal assignment semantics
	def emitAssignment(lhs: dom.Expression, op: String, rhs: dom.Expression): Emission = 
		emitAssignment(lhs, op, rhs, false)
		
	def emitAssignment(lhs: dom.Expression, op: String, rhs: dom.Expression, isPostIncrement: Boolean): Emission = {
		if (lhs.isChar && op != "=") emitCharAssignment(lhs, op, rhs, isPostIncrement)
		else if (isResultUsed) lhs ~ Emit(op) ~ rhs
		else if (isPostIncrement) ASSIGNLHS(lhs, Emit(op), rhs, isResultUsed)
		else ASSIGNRHS(lhs, Emit(op), rhs, isResultUsed)
	}
	
	def emitCharAssignment(lhs: dom.Expression, op: String, rhs: Emission, isPostIncrement: Boolean): Emission = {
		val opNoAssign = op.substring(0, op.length - 1)
		val newRHS = node.emitCastToChar(PARENS(node.emitCastToInt(lhs) ~ Emit(opNoAssign) ~ rhs))
		
		if (isPostIncrement) ASSIGNLHS(lhs, EQUALS, newRHS, isResultUsed)
		else ASSIGNRHS(lhs, EQUALS, newRHS, isResultUsed)
	}
	
	def isResultUsed: Boolean = parent match {
		case x: dom.ExpressionStatement => true
		case x: dom.ForStatement => true
		case _ => false
	}
}

// The difficulty here is that in scala the result of an assignment is always Unit,
// whereas in java it's the value that was assigned.  So many idiomatic uses of java will fail
// utterly unless we insert some wrapping, such as...
//
//   while (x++ != 10)	// aside from the fact we have no postfix increment
//   x = y = z = 5		// not what you want
//
// Our simple wrapper will work for simple cases, but it's a big problem when the
// expression semantics require only evaluating once.  E.g.
//
//   array[side1(side2(), side3())] += array2[side2()];
//
// Scala may not have ++/-- but += and -= have the same only-evaluate-once semantics.
// 
class Assignment(override val node: dom.Assignment) extends Expression(node) with Assigns
{
	lazy val Assignment(lhs, JavaOp(op), rhs) = node
	// deferred final assignment handled specially
	lazy val emitDeferredFinal: Option[Emission] = lhs.snode match {
		case x: VariableName => 
			x.vb.findVariableDeclaration match {
				case Some(v) if v.isDeferredVal =>
					log.trace("Deferred val declaration: %s", v)
					Some(v.emitDirect(node) ~ EQUALS ~ rhs)
				case _ => None
			}
		case _ => None
	}

	override def emitDirect: Emission = emitDeferredFinal | emitAssignment(lhs, op, rhs)
}

class PostfixExpression(override val node: dom.PostfixExpression) extends Expression(node) with Assigns
{
	lazy val PostfixExpression(operand, JavaOp(op)) = node
	
	override def emitDirect: Emission = op match {
		case "++" if operand.isChar		=> emitCharAssignment(operand, "+=", ONE, true)
		case "++"						=> ASSIGNLHS(operand, PLUSEQUALS, ONE, isResultUsed)					
		case "--" if operand.isChar		=> emitCharAssignment(operand, "-=", ONE, true)
		case "--"						=> ASSIGNLHS(operand, MINUSEQUALS, ONE, isResultUsed)
		case _							=> ERROR("Unknown Postfix Operator")
	}		
}

// may or may not require Assigns, so that's resolved in the factory
class PrefixExpression(override val node: dom.PrefixExpression) extends Expression(node)
{
	lazy val PrefixExpression(JavaOp(op), operand) = node
	
	override def emitDirect: Emission = op match {
		case "+"							=> operand
		case "-"							=> MINUS <~> operand 
		case "~"							=> COMPLEMENT <~> operand
		case "!"							=> NOT <~> operand
		case _ => this match {
			case x: Assigns => x.op match {
				case "++" if operand.isChar	=> x.emitCharAssignment(operand, "+=", ONE, false)
				case "++"					=> ASSIGNRHS(operand, PLUSEQUALS, ONE, x.isResultUsed)
				case "--" if operand.isChar => x.emitCharAssignment(operand, "-=", ONE, false)
				case "--"					=> ASSIGNRHS(operand, MINUSEQUALS, ONE, x.isResultUsed)
			}
			case _ => abort("Unknown Prefix Operator")
		}
	}	
}

class SuperMethodInvocation(override val node: dom.SuperMethodInvocation)
extends Expression(node) with MethodBound
{
	lazy val SuperMethodInvocation(qualifier, typeArgs, name, args) = node   
	lazy val superPart = INVOKE(SUPER, name) <~> TYPEARGS(typeArgs) <~> METHODARGS(args)
	def mb = node.resolveMethodBinding
	
	override def binding = super[MethodBound].binding	
    override def emitDirect: Emission = emitOpt(qualifier, INVOKE(_, superPart), superPart)	
}

class ClassInstanceCreation(override val node: dom.ClassInstanceCreation)
extends Expression(node) with MethodBound
{
	// TODO - the first Expr
	lazy val ClassInstanceCreation(_, typeArgs, jtype, args, anonClassDecl) = node
	def mb = node.resolveConstructorBinding
	override def binding = super[MethodBound].binding	
	
	// we can't always leave out the parens because of e.g. new a.b.c.d(e)
	override def emitDirect: Emission = jtype match {
		case JBoxed(anyVal) => NEW ~ emitJLRoot ~ anyVal.emit <~> PARENS(ARGS(args)) ~ emitOpt(anonClassDecl)
		case _ => jtype.emitNew ~ jtype.emitDirect(node) <~> PARENS(ARGS(args)) ~ emitOpt(anonClassDecl)
	}
}

class FieldAccess(override val node: dom.FieldAccess) extends Expression(node) with VariableBound
{
	lazy val FieldAccess(expr, name) = node
	def vb = node.resolveFieldBinding
	override def binding = super[VariableBound].binding
	override def emitDirect: Emission = {
		log.trace("FieldAccess: %s . %s", expr, name)
		INVOKE(expr, name)
	}
}

class SuperFieldAccess(override val node: dom.SuperFieldAccess) extends Expression(node) with VariableBound
{
	lazy val SuperFieldAccess(qualifier, name) = node
	def vb = node.resolveFieldBinding
	override def binding = super[VariableBound].binding
	override def emitDirect: Emission = INVOKE(SUPER ::: emitOpt(qualifier, BRACKETS(_)), name)	
}

class CastExpression(override val node: dom.CastExpression) extends Expression(node)
{
	lazy val CastExpression(jtype, expr) = node
	
	override def emitDirect: Emission = jtype match {
		case JPrimitive(av) => INVOKE(expr, TO <~> av.emit)				// (int) cast => toInt
		case _				=> expr.emitCastTo(jtype.emitDirect(node))
	}
}

class ArrayCreation(override val node: dom.ArrayCreation) extends Expression(node)
{
	lazy val ArrayCreation(jtype, dims, init) = node
		
	override def emitDirect: Emission = init match {
		case None => NEW ~ jtype <~> METHODARGS(dims)
		case Some(init) => init
	}
}

class ArrayInitializer(override val node: dom.ArrayInitializer) extends Expression(node)
{
	lazy val ArrayInitializer(exprs) = node
	lazy val v = findEnclosingVariable
	lazy val jtype = tb.getElementType
		
	// Examples such as:  return (String[])contents.toArray(new String[] {});
	// require us to special case empty literals
	override def emitDirect: Emission = exprs match {
		case Nil => NEW ~ ARRAY <~> BRACKETS(jtype.emitType) <~> PARENS(ZERO)
		case xs => ARRAY <~> PARENS(ARGS(xs))
	}
}		

class InfixExpression(override val node: dom.InfixExpression) extends Expression(node)
{
	lazy val InfixExpression(lhs, JavaOp(op), rhs, extendedOperands) = node
	lazy val default = emitInfixExpression(lhs :: rhs :: extendedOperands, op)

	override def emitDirect: Emission =
		// eq has a lower precedence than == so this must be parenthesized
		if (op == "==" && lhs.isReferenceType && rhs.isReferenceType && extendedOperands.isEmpty)
			PARENS(lhs ~ ISEQUALTOREF ~ rhs)
		// if (i_1 < 1 << 16) works in java but not here -- must be if (i_1 < (1 << 16))
		else if (op == "<<" || op == ">>>" || op == ">>") PARENS(default)
		else default
	
	// Character ops:
	// Scala will do implicit conversions a ways, but if you try for var x: Char = 'a' ; x += 5 ; you are hurt.
	def emitInfixExpression(terms: List[dom.Expression], op: String): Emission = {
		if (isCharExprRequiringConversion && terms.exists(_.isChar) && terms.exists(!_.isChar))
			REPSEP(terms.map(x => if (x.isChar) emitCastToInt(x) else x.emit), Emit(op))
		else
			REPSEP(terms, Emit(op))
	}	
	
	// if we reached a cast before an assignment we're probably okay
	// if we're part of an infix op that results in a boolean we're okay
	private def isCharExprRequiringConversion: Boolean = {
		val booleanResultOps = List("<", ">", "<=", ">=", "==", "!=", "&&", "||")
		ancestors.foreach {
			case _:dom.CastExpression => return false
			case InfixExpression(_, JavaOp(op), _, _) if (booleanResultOps contains op) => return false
			case Assignment(lhs, _, _) if lhs.isChar => return true
			case _:dom.Assignment => return false
			case _ =>
		}
		false
	}
}

class ConditionalExpression(override val node: dom.ConditionalExpression) extends Expression(node)
{
	lazy val ConditionalExpression(expr, thenExpr, elseExpr) = node
	lazy val default = IF ~ PARENS(expr) ~ thenExpr ~ ELSE ~ elseExpr
	
	// pure assignment can be done without parens, but x += if (a) 1 else 2 needs parens
	override def emitDirect: Emission = parent match {
		case Assignment(_, JavaOp("="), _) => default
		case _:dom.ParenthesizedExpression => default
		case _ => PARENS(default)
	}
}

// if the qualifier refers to a factory class, it needs to be rewritten
class ThisExpression(override val node: dom.ThisExpression) extends Expression(node)
{
	lazy val ThisExpression(qualifier) = node
	lazy val mb = findOuterEnclosingMethod.get.mb
	lazy val qualEmit: Emission =
		qualifier match {
			case None => Nil
			case Some(TypeBinding(tb)) if tb.isFactoryType && mb.isConstructor => 
				log.trace("ThisExpression: matched emitClassNameForMethodBinding")
				tb.getFactoryType.get.emitClassNameForMethodBinding(mb)
			case Some(q) => q.emit
		}
	
	override def emitDirect: Emission = INVOKE(qualEmit, THIS)
}

class NumberLiteral(override val node: dom.NumberLiteral) extends Expression(node)
{
	lazy val NumberLiteral(token) = node
	lazy val default = SToken.emitLiteral(token)
	
	override def emitDirect: Emission = if (token startsWith "-") PARENS(default) else default
}

class VariableDeclarationExpression(override val node: dom.VariableDeclarationExpression) extends Expression(node)
{
	lazy val VariableDeclarationExpression(modifiers, jtype, frags) = node
	override def allFragments: List[dom.VariableDeclarationFragment] = frags
	
    override def emitDirect: Emission = REP(frags, VAR ~ _ ~ NL)
}

class Expression(override val node: dom.Expression) extends Node(node) with TypeBound
{
	def tb = node.resolveTypeBinding
	def allFragments: List[dom.VariableDeclarationFragment] = Nil

	def emitCastTo(to: Emission): Emission = INVOKE(node, ASINSTANCEOF) <~> BRACKETS(to)
	def emitCastToInt(expr: dom.Expression): Emission = if (expr.isInt) expr else INVOKE(expr, Emit("toInt"))
	def emitCastToChar(expr: dom.Expression): Emission = if (expr.isChar) expr else INVOKE(expr, Emit("toChar"))
	def emitCastToChar(x: Emission) = INVOKE(x, Emit("toChar"))

	def emitDirect: Emission = node match {
		case x: dom.NullLiteral => NULL
		case CharacterLiteral(escapedValue) => Emit(escapedValue)
		case BooleanLiteral(booleanValue) => if (booleanValue) TRUE else FALSE
		case StringLiteral(escapedValue) => SToken.emitLiteral(escapedValue)
		case TypeLiteral(jtype) => CLASSOF <~> BRACKETS(jtype)		// void.class correctly becomes classOf[Unit]
		case ParenthesizedExpression(expr) => PARENS(expr)
		case ArrayAccess(array, index) => array <~> PARENS(index)
		case InstanceofExpression(leftOperand, rightOperand) => INVOKE(leftOperand, ISINSTANCEOF) <~> BRACKETS(rightOperand)
		
		case _ => ERROR("Unhandled Expr " + node.getClass.getName)
	}	
}
