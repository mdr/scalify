package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

class EqualsMethod(node: dom.MethodDeclaration) extends MethodDeclaration(node)
{
	lazy val SingleVariableDeclaration(_, _, _, paramName, _, _) = parameterList.svds.head
	
	override def emitDirect: Emission = {
		(mb.imethod.map(_.srcWithoutComments) | Nil) ~
		OVERRIDE ~ DEF ~ emitString("equals") <~> PARENS(paramName <~> COLON ~ ANY) <~> COLON ~ BOOLEAN ~ 
		emitBody(parameterList.emitRenamings)
	}
}

class MethodDeclaration(override val node: dom.MethodDeclaration) extends BodyDeclaration(node) 
with MethodBound with NamedDecl with Overrider
{
    lazy val MethodDeclaration(_, _, constructor, typeParams, returnType, name, params, dims, _, body) = node
	def mb = node.resolveBinding
	override def flags = node.getModifiers
	override def allFragments: List[dom.VariableDeclarationFragment] =
		descendants.flatMap { case x: dom.VariableDeclarationFragment => List(x) ; case _ => Nil }
	
	val isPrimary = false
	lazy val dtype: dom.TypeDeclaration = findDeclaringType.get
	lazy val enclosingType: dom.TypeDeclaration = findEnclosingType.get
	lazy val isMainMethod: Boolean = mb.imethod.map(_.isMainMethod) | false
	lazy val parameterList: ParameterList = ParameterList(params)
	lazy val localVars = node.descendants.map(_.snode).flatMap { case x: LocalVariable => List(x) ; case _ => Nil }
	
	// in java a block or function can end with a variable declaration or whatever else, but not scala
	// if the function is void, we have to look for anything scala won't like
	// if it returns a value, only a return statement is a valid last statement
	lazy val dummy: List[Emission] = if (needsDummy) List(emitDefaultReturnValue) else Nil
	lazy val needsDummy: Boolean = 
		!localStmts.isEmpty &&
		(if (isVoid) localStmts.last.isExpandableStmt || localStmts.last.isDeclarationStmt
		else localStmts.last match { case _: dom.ReturnStatement => false ; case _ => true })
	
	override def ppString = Some(toString)
	override def toString: String = origName + commaSeparatedList(params.map(_.toString))
	
	def stmts: List[dom.Statement] = body match {
		case Some(Block(xs)) => xs
		case _ => Nil
	}	
	def localStmts = stmts match {
		case (x: dom.SuperConstructorInvocation) :: rest => rest
		case _ => stmts
	}
	lazy val allStmts: List[Emission] = 
		(if (localStmts.isEmpty) Nil else localStmts.last match {	
			case ReturnStatement(Some(expr)) => localStmts.init.map(_.emit) ::: List(expr.emit)
			case _ => localStmts.map(_.emit)
		}) ::: dummy

    override def emitDirect: Emission = {
		(mb.imethod.map(_.srcWithoutComments) | Nil) ~
		emitOverride ~
		emitModifierList ~
		DEF ~ METHOD(name, typeParams, parameterList.emitList) ~
		emitReturnType ~
		emitBody(parameterList.emitRenamings)
	}
	
	def emitReturnType: Emission = 
		if (returnType.isEmpty) Nil
		else NOS ~ COLON ~ arrayWrap(dims)(returnType.get.emitDirect(node))
	def emitDefaultReturnValue: Emission = returnType.map(_.emitDefaultValue).getOrElse(PARENS)
	
	def emitBody: Emission = emitBody(Nil)
	def emitBody(insertion: List[Emission]): Emission = {
		if (insertion.isEmpty && body.isEmpty) return NL // abstract
		if (insertion.isEmpty && stmts.isEmpty) return EQUALS ~ BRACES
		
		EQUALS ~ emitSynchronized ~ BRACES(insertion ::: emitBodyStmts)
	}
	def emitBodyStmts: List[Emission] = allStmts
	def emitSignature: Emission = DEF ~ METHOD(name, typeParams, parameterList.emitList) ~ emitReturnType ~ NL
	
	def isVoid = returnType.map(_.tb.isVoid) getOrElse true
}

