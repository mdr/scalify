package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

// an STD is of course a Scala Type Declaration		
class STDNoConstructor(node: dom.TypeDeclaration) extends TypeDeclaration(node)
{		
	// emits nothing if everything's static so main methods can work
	def emitInstancePart: Emission = 
		if (isInstancePartEmpty) Nil
		else emitSTDHeader(emitTypeParameters, Nil) ~ emitSTDBody(REP(imethods))
}

// interfaces are brutal because they can contain constants which are inherited
// by subinterfaces and implementing classes.  In the end this pushes us into splitting
// traits into three parts: constants in one trait, abstract methods in another,
// and types in an object which also extends the concrete trait.
class Interface(node: dom.TypeDeclaration) extends TypeDeclaration(node)
{	
	lazy val isSplit: Boolean = !fields.isEmpty || isAnySuperSplit
	lazy val isAnySuperSplit: Boolean = (superType.toList ::: superIntTypes).exists(_.tb.isSplitType) | false
	lazy val valuesName: Emission = name <~> emitString("Vals")
	lazy val valuesBody: Emission = if (fields.isEmpty) NL else BRACES(REP(fields.map(_.emitAsVal)))
	lazy val allSupers: List[dom.Type] = superType.toList ::: superIntTypes
	
	lazy val superValuesEmission: Emission = {
		val splitSups = allSupers.filter(_.tb.isSplitType)
		def emitSuperInts(xs: List[dom.Type]): Emission = xs match {
			case Nil => Nil
			case x :: rest => WITH ~ x.emitExprWhenSuper(None) <~> emitString("Vals") ~ emitSuperInts(rest)
		}
		
		splitSups match {
			case Nil => Nil
			case x :: rest => EXTENDS ~ x.emitExprWhenSuper(None) <~> emitString("Vals") ~ emitSuperInts(rest)
		}
	}
	lazy val superInstanceEmission: Emission =
		if (fields.isEmpty) Nil 
		else if (allSupers.isEmpty) EXTENDS ~ valuesName 
		else WITH ~ valuesName
			
	override def emitAbstract: Emission = ABSTRACT
	override def emitClassType: Emission = TRAIT
	override def emitDirect: Emission = {
		log.trace("Interface emitDirect: %s ", node.id)
		log.trace("Interface supers: %s", superType.toList ::: superIntTypes)
		emitValuesPart ~ emitInstancePart ~ emitStaticPart ~ NL	
	}
	
	def emitValuesPart: Emission = 
		if (isSplit) ABSTRACT ~ TRAIT ~ valuesName ~ superValuesEmission ~ valuesBody
		else Nil
		
	def emitInstancePart: Emission =
		emitSTDHeader(TYPEARGS(typeParams), Nil) ~ 
		superInstanceEmission ~
		(if (methods.isEmpty) NL else BRACES(REP(methods)))

	override def emitStaticPart: Emission =
		if (!isSplit && types.isEmpty) Nil
		else {
			OBJECT ~ name ~ 
			(if (isSplit) EXTENDS ~ valuesName else Nil) ~
			(if (types.isEmpty) NL else BRACES(REP(types)))
		}
}

// class STDWithPrimary(node: dom.TypeDeclaration, val pc: IndependentConstructor) extends TypeDeclaration(node)
class STDWithPrimary(node: dom.TypeDeclaration, con: dom.MethodDeclaration) extends TypeDeclaration(node)
{
	lazy val pc: IndependentConstructor = con.snode match { case x: IndependentConstructor => x ; case _ => abort() }
	override def emitTypeParameters: Emission = TYPEARGS(typeParams ::: pc.typeParams)	
	
	def emitInstancePart: Emission = {
		emitSTDHeader(emitTypeParameters, METHODARGS(pc.parameterList.emitPrimaryList), pc.emitSuperExpr) ~
		emitSTDBody(emitConstructorDefinitions ~ REP(nonConstructors))
	}

	def emitConstructorDefinitions: Emission = pc.emitBody ~ NL ~ REP(dependentConstructors)
}
