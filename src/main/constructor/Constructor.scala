package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
// import scalaz.OptionW._
trait ConstructorOrdering extends PartiallyOrdered[Constructor]
{
	self: Constructor =>
	
	// for sorting constructors so this() calls only reference earlier ones
	// by implementing as a partial ordering we don't have to look further than the immediate call
	def tryCompareTo [B >: Constructor <% PartiallyOrdered[B]](other: B): Option[Int] = {
		// true if c directly this-calls to method m (thus m's constructor must come earlier)
		def doesOneCallAnother(c: Constructor, mb: MBinding): Boolean = c match {
			case x: DependentConstructor => x.thisCall.mb.isEqualTo(mb)
			case _ => false
		}
		
		other match {
			case x: DependentConstructor =>
				if (this.mb.isEqualTo(x.mb)) Some(0)
				else if (doesOneCallAnother(x, this.mb)) Some(-1)
				else if (doesOneCallAnother(this, x.mb)) Some(1)
				else None
			case x: IndependentConstructor =>				
				if (doesOneCallAnother(this, x.mb)) Some(1)
				else None				
		}
	}
}

class IndependentConstructor(node: dom.MethodDeclaration)
extends Constructor(node)
{
	override val isPrimary = true
	val superCall = stmts match { case (x: dom.SuperConstructorInvocation) :: _ => Some(x) ; case _ => None }
	val superArgs = superCall.map(_.arguments: List[dom.Expression]) getOrElse Nil
	val superType = enclosingType.superType
	
	def emitSuperExpr: Emission =
		if (superCall.isDefined) EXTENDS ~ superCall.get
		else if (superType.isDefined) EXTENDS ~ superType.get
		else Nil
	
	override def emitDirect: Emission = emitBody
	override def emitBody: Emission = wrapEarlyReturn(REP(emitBodyStmts)) ~ NL
	// override def emitBody: Emission = REP(parameterList.emitRenamings) ~ wrapEarlyReturn(REP(emitBodyStmts)) ~ NL
}

class DependentConstructor(node: dom.MethodDeclaration)
extends Constructor(node)
{
	val thisCall = stmts match { case (x: dom.ConstructorInvocation) :: _ => x ; case _ => abort() }
	val thisArgs: List[Expression] = thisCall.arguments
	
	override def emitDirect: Emission = 
		emitModifierList ~ DEF ~ THIS <~> PARENS(ARGS(params)) ~ wrapEarlyReturn(emitBody)
}

abstract class Constructor(node: dom.MethodDeclaration)
extends MethodDeclaration(node)
with ConstructorOrdering
{	
	override def isOverride = false
	def hasEarlyReturn = descendants.exists { case x: dom.ReturnStatement => true ; case _ => false }
	def wrapEarlyReturn: EFilter = {
		lazy val catchClauses = List(
			emitString("case x: ConstructorEarlyReturn =>"),
			emitString("case x => throw x")
		)
		
		if (!hasEarlyReturn) identity else (body: Emission) => emitTry(body, catchClauses)
	}
}

