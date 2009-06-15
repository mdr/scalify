package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

import scala.collection.mutable.HashMap
import org.eclipse.jdt.core.dom.{ PrimitiveType => PT }
// import scalaz.OptionW._
// All type nodes represent *references* to types.  Declarations are elsewhere.


// public class Foo[A] {
//  Bar blah;
// 	public Foo[B](x: B) {
// 		this.blah = new Bar[B](x)
// 	}
// }
// 
// class Foo[A, B](x: B) { var blah: Bar[_]; this.blah = new Bar(x) }
//
// Foo[String] x = new Foo[Int](5)
//
// Foo[Int, String]
// Foo[_ <: Int, _ <: String]
// Foo[_, _]
// Foo[_ >: Null]
//
// WildCardType:

class PrimitiveType(node: dom.PrimitiveType) extends Type(node)
{
	lazy val PrimitiveType(code) = node
	lazy val JPrimitive(anyVal) = node
	
	def emitDirect: Emission = anyVal.emit
}

class BoxedType(node: dom.SimpleType) extends SimpleType(node)
{
	lazy val JBoxed(anyVal) = node
	
	override def emitDirect(context: ASTNode): Emission = emitDirect
	override def emitDirect: Emission = anyVal.emit
}

class ArrayType(node: dom.ArrayType) extends Type(node)
{
	lazy val ArrayType(componentType) = node
	lazy val elementType = node.getElementType
	lazy val dims = node.getDimensions
	
	def emitDirect: Emission = ARRAY <~> BRACKETS(componentType)
	// override def emitWithoutBounds: Emission = ARRAY <~> BRACKETS(componentType.emitWithoutBounds)
}

class ParameterizedType(node: dom.ParameterizedType) extends Type(node)
{
	lazy val ParameterizedType(jtype, typeArgs) = node
	lazy val typeArgCount = findTypeDeclaration.map(_.emitTypeParameters.size) getOrElse 0
	
	override def emitDirect(context: ASTNode): Emission = jtype.emitDirect(context) ~ TYPEARGS(typeArgs)
	override def emitDirect: Emission = jtype.emitDirect ~ TYPEARGS(typeArgs)
	
	private def emitTypeArgs: Emission = TYPEARGS(List.make(typeArgCount, UNDERSCORE).map(x => List(x)))
}

class SimpleType(node: dom.SimpleType) extends Type(node) with NameInfo
{
	lazy val SimpleType(name) = node
	lazy val typeParameters = tb.getErasure.getTypeParameters
	lazy val nameUnadorned = name.emitNameAsOrig <~> emitRawTypeArgs(ANYREF)

	override def emitDirect(context: ASTNode): Emission = {
		context match {
			case _: dom.ClassInstanceCreation => return nameUnadorned
			case _: dom.CastExpression => return nameUnadorned
			case _ =>
		}
		val standin: Emission = context match {
			case _: dom.MethodDeclaration => UNDERSCORE
			case _ => ANYREF
		}
		
		emitName(None) <~> emitRawTypeArgs(standin)
	}
		
	override def toString: String = name.toString
	def emitDirect: Emission = emitName(None) <~> emitRawTypeArgs(ANYREF)
	
	override def emitNameAsOrig = name.emitNameAsOrig
	private def emitName(context: Option[ASTNode]): Emission =
		if (context.isEmpty || !context.get.isEnclosedInSameType(node)) name.emit
		else emitNameAsOrig

	// Class => Class[_] and etc.
	private def emitRawTypeArgs(standin: Emission): Emission =
		if (!tb.isRawType || typeParameters.isEmpty || standin.isEmpty) Nil
		else NOS ~ BRACKETS(REPSEP(List.make(typeParameters.size, standin), NOS ::: COMMA))
}

class QualifiedType(node: dom.QualifiedType) extends Type(node) with NameInfo
{
	val name = node
	def emitDirect: Emission = name
	override def emitNameAsOrig = name.emitNameAsOrig
	override def toString: String = name.toString
}

class WildcardType(node: dom.WildcardType) extends Type(node)
{
	lazy val WildcardType(bound, isUpperBound) = node
	lazy val boundEmission = bound.map(b => (if (isUpperBound) BOUNDUPPER else BOUNDLOWER) ~ b) getOrElse Nil
	
	def emitDirect: Emission = UNDERSCORE ~ boundEmission
}

// trait NamedType extends NameInfo
// {
// 	self: Type =>
// 	
// 	// given a particular supercall pointed at this type, returns correct emission
// 	// the level of complication here is due to factory types having variable names for the superclass
// 	def emitTypeNameWhenSuper(sc: Option[dom.SuperConstructorInvocation]): Emission = {
// 		log.trace("emitTypeNameWhenSuper: %s", name)
// 		
// 		emitNameAsOrig
// 		// name
// 		// INVOKE(ROOTPKG, INVOKE(emitString(pkgName), name))
// 	}
// 			
// 	// def emitExprWhenSuper: Emission = emitTypeNameWhenSuper
// 	def emitExprWhenSuper(sc: Option[dom.SuperConstructorInvocation]): Emission = {
// 		if (sc.isEmpty) return emitTypeNameWhenSuper(None)
// 		lazy val SuperConstructorInvocation(expr, typeArgs, args) = sc.get
// 		
// 		emitTypeNameWhenSuper(sc) <~>
// 		emitOpt(expr, BRACKETS(_) ~ NOS) ~
// 		METHODARGS(args)
// 	}
// }

// * A type like "A.B" can be represented either of two ways:
// * <ol>
// * <li>
// * <code>QualifiedType(SimpleType(SimpleName("A")),SimpleName("B"))</code>
// * </li>
// * <li>
// * <code>SimpleType(QualifiedName(SimpleName("A"),SimpleName("B")))</code>
// * </li>
// * </ol>
// 
// Somewhere there need be a list of SimpleNames

abstract class Type(node: dom.Type) extends Node(node) with TypeBound
{
	def tb = node.resolveBinding
	
	def emitNameAsOrig = emitDirect
	def emitTypeNameWhenSuper(sc: Option[dom.SuperConstructorInvocation]): Emission = {		
		tb.getFactoryType match {
			case None => node.emitNameAsOrig
			case Some(x) => node.emitNameAsOrig <~> x.emitLabelForSuperCall(sc)
		}
	}

	def emitExprWhenSuper(sc: Option[dom.SuperConstructorInvocation]): Emission = {
		if (sc.isEmpty) return emitTypeNameWhenSuper(None)
		val SuperConstructorInvocation(expr, typeArgs, args) = sc.get

		emitTypeNameWhenSuper(sc) <~>
		emitOpt(expr, BRACKETS(_) ~ NOS) ~
		METHODARGS(args)
	}
	
	def emitImportsWhenSuper: Emission =
		tb.getFactoryType match {
			case None => Nil
			case Some(x) => x.emitImportsWhenSuper ~ NL
		}
			
	// def needsQualification = scalaPredefTypes contains leftTypeSegment
	// 
	// // unroll the confusion into a list of SimpleNames	
	// def leftTypeSegment: String = typeSegments match { case SimpleName(ident) :: _ => ident ; case _ => "" }
	
	// def pkgQualifier: String = ""
	// override def toString = node match {
	// 	case SimpleType(SimpleName(ident)) => ident
	// 	case _ => node.toString
	// }
	
	// def isVoid: Boolean = node match {
	// 	case x: dom.PrimitiveType if x.toString == "void" => true
	// 	case _ => false
	// }
		
	// emits NEW unless we used a factory on the declaring type		
	def emitNew: Emission = if (tb.isFactoryType) Nil else NEW
}

