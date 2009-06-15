package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.{ IType, IMethod }
import org.eclipse.jdt.core.dom
// import scalaz.OptionW._
// trait for all ast types that have modifiers
trait Modifiable
{
	import dom.Modifier
	val node: ASTNode
	def flags: Int

	def isAbstract		= Modifier.isAbstract(flags)
	def isFinal			= Modifier.isFinal(flags)
	def isNative		= Modifier.isNative(flags)
	def isPrivate		= Modifier.isPrivate(flags)
	def isProtected		= Modifier.isProtected(flags)
	def isPublic		= Modifier.isPublic(flags)
	def isStatic		= Modifier.isStatic(flags) || isInterface
	def isStrictfp		= Modifier.isStrictfp(flags)
	def isSynchronized	= Modifier.isSynchronized(flags)
	def isTransient		= Modifier.isTransient(flags)
	def isVolatile		= Modifier.isVolatile(flags)

	private def isInterface: Boolean = node match {
		case x: dom.TypeDeclaration if x.isInterface => true
		case _ => false
	}
	private def word(cond: Boolean, emit: Emission): Emission = if (cond) emit else Nil
	
	// TODO - dealing with access at this point is the epitome of premature optimization
	private def emitAccessModifier: Emission =  Nil
		// if (node == null) Nil
		// else if (isProtected) {
		// 	if (node.pkgName == "") PROTECTED
		// 	else PROTECTED <~> BRACKETS(Emit(node.pkgName.replaceAll(""".*\.""", "")))
		// }
		// else if (isPrivate) node.snode match {
		// 	case x: TypeBound if (x.tb.isTopLevel) => PRIVATE <~> BRACKETS(emitString(x.tb.getDeclaringClass.getName))
		// 	case _ => PRIVATE
		// }
		// else Nil
	
	// these two handled separately because their semantics are different from java
	def emitSynchronized: Emission = if (isSynchronized) SYNCHRONIZED else Nil
	def emitAbstract: Emission = if (isAbstract) ABSTRACT else Nil
	
	def emitModifierList: Emission = {
		word(isNative, NATIVE) ~
		word(isTransient, TRANSIENT) ~ 
		word(isVolatile, VOLATILE) ~
		emitAccessModifier ~
		word(isFinal, FINAL)
	}
}

trait Overrider
{
	self: MethodDeclaration =>
	final def emitOverride: Emission = if (isOverride) OVERRIDE else Nil
	
	// Scala Reference Definition 5.1.4
	// A member definition M matches definition M' if they bind to the same name and ONE of the following holds:
	// 1) neither is a method definition (e.g. val x)
	// 2) M and M' both define monomorphic (no type parameter) methods with equivalent argument types
	// 3) (not relevant to java)
	// 4) M and M' both define polymorphic methods with equal number of argument types and equal numbers of type parameters
	def isOverride: Boolean = {
		log.trace("isOverride for method %s(%s) supers: %s", node.name, 
			mb.imethod.map(_.getSignature) getOrElse "???", dtype.findAllSupertypes.map(_.id))
		
		if (mb.isStatic)									false	// by definition, as objects don't have superclasses
		else if (overridesMethodInScalaObject(origName))	true	// check this early
		else if (overridesMethodInAnon)						true	// the method may be in an anonymous class refinement
		else if (overridesMethodInInterface)				true	// confirms we're an interface before checking superints
		else if (dtype.superType.isEmpty)					false	// can't override what's not there
		else if (overridesMethodIn(dtype.superType.get))	true	// checking the immediate supertype with jdt's mechanism
		else if (dtype.findAllSupertypes.exists(overridesMethodIn))	true	// traverse the IType hierarchy 
		else false															// okay, we'll let it slide this time
	}

	// if it's an interface we check out the superinterfaces, but if it's a class those aren't really overrides
	private def overridesMethodInInterface: Boolean = {
		val TypeDeclaration(_, _, interface, _, _, superType, superIntTypes, _) = dtype

		if (!interface) false		
		else ((superType.toList) ::: superIntTypes).exists(x => overridesMethodIn(x))
	}

	private def overridesMethodInAnon: Boolean = {
		val tb = mb.getDeclaringClass
		if (!tb.isAnonymous) return false
		
		ancestors.exists {
			case ClassInstanceCreation(_, _, TypeBinding(anontb), _, _)
				if anontb.methods.exists(mb.overrides(_)) || overridesMethodIn(anontb) => true
			case _ => false
		}
	}

	// * A method m1 corresponds to another method m2 if:
	// * <ul>
	// * <li>m1 has the same element name as m2.
	// * <li>m1 has the same number of arguments as m2 and
	// *     the simple names of the argument types must be equals.
	// * <li>m1 exists.	 
	private def overridesMethodIn(jtype: dom.Type): Boolean = jtype match {
		case TypeBinding(tb) => tb.methods.exists(x => mb.overrides(x))
		case _ => false
	}
	
	private def overridesMethodIn(it: IType): Boolean = {
		// XXX - temp while I try to figure out the overriding bug
		val op = it.getOpenable
		if (op != null && !op.isOpen) {
			log.trace("!isOpen: %s", it)
			op.open(null)
		}

		val imethod = mb.imethod getOrElse (return false)		
		val methods = it.findMethods(imethod)
		
		if (methods == null || methods.size == 0) {
			val methods: List[IMethod] = it.methods
			log.trace("overridesMethodIn failed: imethod = %s, itype = %s, methods = %s", imethod.id, it.id, methods.map("\n  ---" + _.id))
		}
		
		methods != null && methods.size > 0
	}
	
	private def overridesMethodIn(tb: TBinding): Boolean = tb.findTypeDeclaration match {
		case Some(x) => x.findAllSupertypes.exists(overridesMethodIn)
		case _ => false
	}

	private def overridesMethodInScalaObject(name: String): Boolean = {
		val scalaObjectNoArgs = List("clone", "finalize", "getClass", "hashCode", "toString")
		
		if (params.isEmpty && (scalaObjectNoArgs contains name)) true
		else name == "equals" && params.size == 1 && (params.head.getType match {
			case SimpleType(SimpleName("Object")) => true
			case _ => false
		})
	}
}

trait Serializes
{
	self: TypeDeclaration =>
	
	// @SerialVersionUID(0 - 8256821097970055419L)
	// A serializable class can declare its own serialVersionUID explicitly by declaring
	// a field named "serialVersionUID" that must be static, final, and of type long
	def emitSerialVersionUID: Emission = {		
		for { 
			f @ FieldDeclaration(_, _, PrimitiveType(LONG), fragments) <- sfields 
			  if f.isFinal
			VariableDeclarationFragment(SimpleName("serialVersionUID"), 0, Some(expr)) <- fragments
		} return Emit("@SerialVersionUID") <~> PARENS(expr)

		Nil
	}
}

