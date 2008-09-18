package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

// not NamedDecl because the fragments are individually
class FieldDeclaration(override val node: dom.FieldDeclaration) extends BodyDeclaration(node)
{
	lazy val FieldDeclaration(javadoc, mods, jtype, fragments) = node
	
	override def allFragments = fragments
	override def emitDirect = emitFields(_.emitValOrVar)
	def emitAsVal = emitFields(x => VAL)
		
	private def emitFields(valOrVar: (dom.VariableDeclarationFragment) => Emission): Emission = {
		val fragsToEmit = fragments.filter(!_.isDeferredVal)
		REP(fragsToEmit.map(x => valOrVar(x) ~ x ~ NL))
	}
}

class EnumConstantDeclaration(override val node: dom.EnumConstantDeclaration)
extends BodyDeclaration(node) with VariableBound with MethodBound
{
	def vb = node.resolveVariable
	def mb = node.resolveConstructorBinding
	override def flags = node.getModifiers
	override def binding = super[VariableBound].binding	
}

class EnumDeclaration(override val node: dom.EnumDeclaration)
extends AbstractTypeDeclaration(node)
{
	lazy val EnumDeclaration(_, _, _, superInterfaceTypes, enumConstants, bodyDecls) = node
	
	override def emitDirect: Emission = CLASS ~ name ~ EXTENDS ~ ENUMCLASS ~ 
		BRACES(VAL ~ ARGS(enumConstants.map(_.getName)) ~ EQUALS ~ VALUE ~ NL)
}

class AnnotationTypeDeclaration(override val node: dom.AnnotationTypeDeclaration)
extends AbstractTypeDeclaration(node)
{
	lazy val AnnotationTypeDeclaration(_, _, _, bodyDecls) = node
	lazy val emits: List[Emission] = 
		bodyDecls.map {
			case AnnotationTypeMemberDeclaration(_, _, name, jtype, expr) =>
				VAL ~ name ~ COLON ~ jtype ~ emitOpt(expr, EQUALS ~ _) ~ NL
			case x: dom.BodyDeclaration => x.emit
		}

	override def emitDirect: Emission =
		CLASS ~ name ~ EXTENDS ~ Emit("scala.Annotation") ~ BRACES(REP(emits))	
}

abstract class AbstractTypeDeclaration(override val node: dom.AbstractTypeDeclaration)
extends BodyDeclaration(node) with TypeBound with NamedDecl
{
	def tb = node.resolveBinding
	override def flags = tb.getModifiers
	val name = node.getName
	
	// misc defaults that we don't want to clutter STD
	def emitNew: Emission = NEW
	def emitClassType: Emission = CLASS
}

class BodyDeclaration(override val node: dom.BodyDeclaration) extends Node(node) with Modifiable
{
	def flags = node.getModifiers
	def allFragments: List[dom.VariableDeclarationFragment] = Nil

	def emitDirect = node match {
		case Initializer(_, _, Block(stmts)) => REP(stmts)
				
		case AnnotationTypeMemberDeclaration(_, mods, name, jtype, expr) =>
			val anns: List[dom.Annotation] = mods flatMap { case x: dom.Annotation => List(x) ; case _ => Nil }
			REP(anns) ~ name ~ jtype ~ emitOpt(expr) ~ NL	// TODO
						
		case _ => emitDefault
	}

	//
	// helper monkeys
	//
			
	// this.x = x is a no-op
	def isRedundantAssignment(x: dom.Statement) = x match {
		case ExpressionStatement(Assignment(FieldAccess(
				ThisExpression(None), SimpleName(name)), JavaOp("="), SimpleName(rhs)))
				if name == rhs => true

		case _ => false
	}
	
	// does this field declaration have the same name as a constructor parameter?
	def isRedundantVar(x: dom.FieldDeclaration, paramNames: List[String]): Boolean = {
	    val FieldDeclaration(_, _, ftype, fragments) = x
	    
	    for (VariableDeclarationFragment(SimpleName(name), _, _) <- fragments)
	        if (paramNames contains name) return true
	        
	    return false
    }

}
