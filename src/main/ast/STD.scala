package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom
// import scalaz.OptionW._
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword._
import scala.collection.mutable.HashMap

// Programming in Scala p129:
//
// In Scala, every auxiliary constructor must invoke another constructor of 
// the same class as its ﬁrst action. In other words, the ﬁrst statement in every 
// auxiliary constructor in every Scala class will have the form “this(. . . ).” 
// The invoked constructor is either the primary constructor (as in the Rational 
// example), or another auxiliary constructor that comes textually before the 
// calling constructor. The net effect of this rule is that every constructor invo- 
// cation in Scala will end up eventually calling the primary constructor of the 
// class. The primary constructor is thus the single point of entry of a class. 
//
// So: only the primary constructor can call the superclass constructor.
// This means that though there are gyrations that could be done to accomodate
// multiple distinct independent constructors, it's impossible to preserve
// semantics if different supers are called in a built-in class.  So for
// simplicity, anytime a class has 2 or more indpendent constructors, we switch
// to using factory methods and place each primary constructor in its own class.

abstract class TypeDeclaration(override val node: dom.TypeDeclaration) extends AbstractTypeDeclaration(node)
with HasTypes
{
    lazy val TypeDeclaration(_, _, interface, _, typeParams, superType, superIntTypes, bodyDecls) = node
	override def toString: String = 
		name.toString + (if (superType.isDefined) " (extends " + superType.get + ")" else "")
    
    // separate into static/instance fields/methods, plus constructors/non and types
    lazy val fields: List[dom.FieldDeclaration] = node.getFields.toList
    lazy val methods: List[dom.MethodDeclaration] = node.getMethods.toList
    lazy val types: List[dom.TypeDeclaration] = node.getTypes.toList
	lazy val stds = types
	lazy val inits: List[dom.Initializer] = bodyDecls.flatMap { case x: dom.Initializer => List(x) ; case _ => Nil }
	
	// what's left should be (?) EnumDeclaration, EnumConstantDeclaration, AnnotationTypeDeclaration
	// and AnnotationTypeMemberDeclaration
	lazy val otherBody: List[dom.BodyDeclaration] = bodyDecls --
		(fields ::: methods ::: types ::: inits).map(_.asInstanceOf[dom.BodyDeclaration])
	lazy val allDeclaredIdentifiers: List[NamedDecl] =
		(fields.flatMap(_.allFragments).map(_.snode) ::: methods.map(_.snode) ::: types.map(_.snode)) flatMap {
			case x: NamedDecl => List(x) ; case _ => Nil
		}

	// split everything into static and instance
    lazy val (sfields, ifields) = fields.partition(_.isStatic)
	lazy val (smethods, imethods) = methods.partition(_.isStatic)
	lazy val (stypes, itypes) = types.partition(_.isStatic)
	lazy val (sinits, iinits) = inits.partition(_.isStatic)

	// independent means they don't call this(...) in the original java
	lazy val constructors: List[Constructor] = imethods.filter(_.isConstructor).map(_.snode.asInstanceOf[Constructor])
	lazy val nonConstructors = imethods.filter(!_.isConstructor)
	lazy val independentConstructors: List[IndependentConstructor] = 
		constructors flatMap { case x: IndependentConstructor => List(x) ; case _ => Nil }
	lazy val dependentConstructors: List[DependentConstructor] = 
		PosetOps.tsort(constructors)
		. flatMap { case x: DependentConstructor => List(x) ; case _ => Nil }

	lazy val isEverCreated = Global.lookupCreation(tb.getKey)

	// abstract
	def emitInstancePart: Emission

	// umbrella methods
	override def emitDirect: Emission = emitInstancePart ~ emitStaticPart ~ emitMainProxy ~ NL
	override def allFragments = fields.flatMap(_.allFragments)
	
	// customized in the subclasses
	def emitTypeParameters: Emission = TYPEARGS(typeParams)
	def emitStaticPart: Emission =
		if (isStaticPartEmpty) Nil
		else NL ~ OBJECT ~ name ~ BRACES(emitStaticsImport(false) ~ REP(sfields) ~ REP(sinits) ~ REP(smethods) ~ REP(stypes))
	
	// this means emit the "extends SuperClass(a, b)" expression for THIS class
	// overridden in subclasses
	def emitSuperExpr: Emission = superType match {
		case None => Nil
		case Some(x) => EXTENDS ~ x.emitExprWhenSuper(None)
	}
	
	// given a particular supercall pointed at this type, returns correct emission
	// the level of complication here is due to factory types having variable names for the superclass
	def emitTypeNameWhenSuper: Emission = emitTypeNameWhenSuper(None)
	def emitTypeNameWhenSuper(sc: Option[dom.SuperConstructorInvocation]): Emission =
		INVOKE(ROOTPKG, INVOKE(emitString(pkgName), name))
				
	def findAllSupertypesWithSelf: List[IType] = itype.get :: findAllSupertypes
	def findAllSupertypes: List[IType] = {
		val xs: Option[Array[IType]] = for (it <- itype) yield hierarchy.getAllSuperclasses(it)
			
		if (xs.isEmpty || xs.get == null) Nil else xs.get.toList
	}
					
	// traits and different sorts of classes have much in common
	def emitSTDHeader(typeArgs: Emission, conArgs: Emission): Emission = 
		emitSTDHeader(typeArgs, conArgs, superType.map(x => EXTENDS ~ x.emit) getOrElse Nil)
	def emitSTDHeader(typeArgs: Emission, conArgs: Emission, superArgs: Emission): Emission = {
		emitFactoryImports ~
	    emitModifierList ~ 
		emitAbstract ~
		emitClassType ~ name ~
		typeArgs ~ conArgs ~ superArgs ~
		emitMixins(superIntTypes)
	}
	
	def emitSTDBody(instancePart: Emission): Emission =
		BRACES(
			emitStaticsImport(true) ~ 
			emitListNL(ifields) ~ 
			emitListNL(iinits) ~
			emitListNL(itypes) ~
			instancePart
		)
		
	// given a list of ASTNodes representing superclasses and/or interfaces, assembles into scala form
	def emitMixins(xs: List[ASTNode]): Emission = {
		def withList(ints: List[ASTNode]): Emission = REP(ints, NL ~ WITH ~ _)
		
		if (xs.isEmpty) Nil
		else if (superType.isEmpty) EXTENDS ~ xs.head ~ withList(xs.tail)
		else withList(xs)
	}

	def emitStaticsImport(includeSelf: Boolean): Emission = {
		def allSupertypes = if (includeSelf) findAllSupertypesWithSelf else findAllSupertypes
		// if we are an inner class inheriting from our enclosing class, we need to avoid double importing
		val typeList: List[IType] = 
			if (tb.isTopLevel) allSupertypes
			else {
				val etype = findEnclosingType.flatMap(_.itype) | abort("error")
				val okTypes = allSupertypes.takeWhile(t => t.getKey != etype.getKey)
				
				if (okTypes.isEmpty) List(itype.get) else okTypes
			}
			
		val emits = typeList
		  . takeWhile(!_.isBinary)
		  . map(it => if (it.hasStaticMembers) cu.emitImportDeclaration(it.getFullyQualifiedName('.'), true) else Nil)
		  . filter(_ != Nil)
		
		emitListNL(emits)
	}
	
	// main
	def findMainMethod: Option[dom.MethodDeclaration] = smethods.find(_.isMainMethod)
	def hasMainProxy: Boolean = findMainMethod.isDefined && !isInstancePartEmpty
	def mainProxyName: String = origName + "Main"		// XXX
	def emitMainProxy: Emission =
		if (hasMainProxy) {
			NL ~ OBJECT ~ emitString(mainProxyName) ~ BRACES(
				DEF ~ Emit("main") ~ PARENS(Emit("args") <~> COLON ~ ARRAY <~> BRACKETS(Emit("String"))) ~ 
				BRACES(INVOKE(name, Emit("main")) <~> PARENS(Emit("args")))
			)
		} else Nil
		
	
	def isInstancePartEmpty: Boolean = 
		if (isEverCreated) false
		else ifields.isEmpty && imethods.isEmpty && iinits.isEmpty && itypes.isEmpty && !isAbstract
		
	def isStaticPartEmpty: Boolean = sfields.isEmpty && smethods.isEmpty && sinits.isEmpty && stypes.isEmpty
}
