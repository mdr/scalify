package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

class STDWithFactory(node: dom.TypeDeclaration)
extends TypeDeclaration(node)
with ConstructorArrangement
{		
	override def emitNew: Emission = Nil
	def mergedTypeParameters(con: Constructor): Emission = TYPEARGS(typeParams ::: con.typeParams)
	
	override def emitTypeNameWhenSuper(sc: Option[dom.SuperConstructorInvocation]): Emission = 	
		if (sc.isEmpty) super.emitTypeNameWhenSuper(sc)
		else emitClassNameForSuperCall(sc)
		
	override def emitMixins(xs: List[ASTNode]): Emission = {
		if (xs == Nil) Nil
		else if (!interface && superType.isDefined) REP(xs.map(NL ~ WITH ~ _.emit)) ~ NL
		else if (xs.size == 1) EXTENDS ~ xs.head
		else REP(List(NL ~ EXTENDS ~ xs.head.emit) ::: xs.tail.map(NL ~ WITH ~ _.emit))
	}
	
	private def emitOneInstancePart(con: IndependentConstructor): Emission = {
		val num = getConstructorGroupLabel(con).toString
		val thisGroup = getConstructorGroup(con) - con
		val superExpr: Emission = con.emitSuperExpr
		val traitExpr: Emission =
			if (superExpr.isEmpty) EXTENDS ~ name
			else NL ~ WITH ~ name
				
		emitModifierList ~ emitAbstract ~ CLASS ~ name <~> emitString(num) ~ 
		mergedTypeParameters(con) ~ METHODARGS(con.parameterList.emitOriginalList) ~
		superExpr ~ traitExpr ~
		BRACES(emitStaticsImport ~ NL ~ con.emitBody ~ REP(thisGroup)) ~ NL
	}
			
	def emitInstancePart: Emission = {
		val icEmits = independentConstructors.map(emitOneInstancePart)
		val superEmit: Emission = superType.map(EXTENDS ~ _.emit) | Nil
			
		ABSTRACT ~ TRAIT ~ name ~ TYPEARGS(typeParams) ~ superEmit ~ emitMixins(superIntTypes) ~
		BRACES(emitStaticsImport ~ REP(ifields) ~ REP(iinits) ~ REP(itypes) ~ REP(nonConstructors)) ~ NL ~
		REP(icEmits)
	}
	
	override def emitStaticPart: Emission = {
		lazy val factoryMethods = emitAllFactoryMethods

		// no constructors for abstract classes
		if (node.isAbstract) return Nil
		
		OBJECT ~ name ~ BRACES(
			emitStaticsImport ~ 
			emitListNL(sfields) ~ 
			emitListNL(emitAllFactoryMethods) ~ 
			REP(sinits) ~ 
			emitListNL(smethods) ~ 
			emitListNL(stypes)
		)
	}
}

trait ConstructorArrangement {
	self: STDWithFactory =>
	
	lazy val groups = PosetOps.arrange(constructors)
	def getConstructorGroupLabel(con: Constructor): Int =
		(0 until groups.size).find(num => groups(num).contains(con)) | abort("Nonexistent constructor")		
	def getConstructorGroup(con: Constructor): List[Constructor] =
		groups.find(list => list.contains(con)) | Nil
		
	def emitLabelForSuperCall(sc: Option[dom.SuperConstructorInvocation]): Emission =
		emitString(getLabelForSuperCall(sc))
	def emitClassNameForSuperCall(sc: Option[dom.SuperConstructorInvocation]): Emission = 
		name ~ emitLabelForSuperCall(sc)
	def emitClassNameForMethodBinding(mb: MBinding): Emission = 
		name <~> emitString(getLabelForMBinding(mb))
	
	def emitAllFactoryMethods: List[Emission] = (0 until groups.size).map(emitFactoryMethodGroup(_))
	def emitClassName(num: Int): Emission = name <~> Emit(num.toString)
	def emitImportsWhenSuper: Emission = {
		val classes = name.currentName :: (0 until groups.size).map(name.currentName + _.toString)
		val importString = pkgName + ". " + "{ " + Scalify.join(classes, ", ") + " }"
		cu.emitImportDeclaration(importString, false)
	}

	// private
	//
	private def getLabelForSuperCall(sc: Option[dom.SuperConstructorInvocation]): String = sc match {
		case None =>		// 0-argument
			for (num <- 0 until groups.size)
				if (groups(num).exists(c => c.params.size == 0)) return num.toString
			""
		case Some(x) => getLabelForMBinding(x.mb)
		
	}
	private def getLabelForMBinding(mb: MBinding): String = {
		for (num <- 0 until groups.size)			
			if (groups(num).exists(c => c.mb.isEqualTo(mb)))
				return num.toString
		
		abort("Failed to find matching method binding for supercall: " + mb.getKey)
	}

	private def emitFactoryMethodGroup(num: Int): Emission = 
		REP(for (con <- groups(num)) yield emitFactoryMethod(con, num))	
	
	private def emitFactoryMethod(con: Constructor, num: Int): Emission = {
		val paramlist: Emission = 
			if (con.params.size == 0) PARENS 
			else METHODARGS(con.parameterList.emitOriginalList)

		DEF ~ APPLY <~> paramlist ~ EQUALS ~ NEW ~ emitClassName(num) <~> 
			METHODARGS(con.params.map(_.name)) ~ NL
	}

}