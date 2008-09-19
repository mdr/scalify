package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

class MethodInvocation(override val node: dom.MethodInvocation) extends Expression(node) with MethodBound
{
	lazy val MethodInvocation(expr, typeArgs, name, args) = node
	lazy val SimpleName(method) = name
	lazy val boxedArgs = METHODARGS(doArgumentBoxing(node, args))
	lazy val eMethod = findEnclosingMethod
	def mb = node.resolveMethodBinding
	override def binding = mb
	
	// Convenience implicit
    private implicit def makeOpt(xs: Emission): Option[Emission] = Some(xs)
	
	// we glue the expression and simple name together so it's one token
	// in scala there can be a difference between an empty argument list and no argument list,
	// but it'd be hard for just-translated java code to be utilizing that distinction...
	override def emitDirect: Emission = {
		log.trace("MethodInvocation: %s", node)
		if (mb.isStatic) {	// XXX
			val pkg = mb.getStaticQualifierPkg
			ROOTPKG <~> DOT <~> INVOKE(emitString(pkg), name.emitNameAsStaticRef) <~> boxedArgs
		}
		else methodTransformation getOrElse emitInvocation
	}
	
	// separated from emitDirect so we can call it from transformation
	private def emitInvocation: Emission = {
		val qname: Emission = 
			if (name.isStaticReference) {
				val pkg = mb.getStaticQualifierPkg
				ROOTPKG <~> DOT <~> INVOKE(emitString(pkg), name.emitNameAsStaticRef)
			}
			else if (expr.isEmpty) {
				// for uses like int head = head(), we can avoid renaming by saying head = this.head
				if (declaresLocalVarNamed(name)) {
					log.trace("Qualifying %s to this.%s in %s", name.id, name.id, eMethod.map(_.id) | "?")
					if (isInAnonDeclaration || (findEnclosingType.map(!_.tb.isTopLevel) | false)) { 
						val typeQualifier = findEnclosingType.map(t => emitString(t.tb.getName)) | Nil
						log.trace("Qualifying inner collision %s with %s", method, typeQualifier)
						INVOKE(INVOKE(typeQualifier, THIS), name)
					}
					else INVOKE(THIS, name)
				} else name
			}
			else INVOKE(expr.get, name)
		
		expr match {
			case Some(JBoxed(anyVal))		=> emitJLRoot ~ INVOKE(anyVal.emit, name.emitUnqualified) <~> boxedArgs	
			case _							=> qname <~> boxedArgs
		}
	}
	
	private def declaresLocalVarNamed(v: dom.SimpleName): Boolean = {
		val localVars = eMethod.map(_.localVars) | Nil
		log.trace("Qualifying %s in %s? localVars = %s", v.getIdentifier, eMethod.map(_.id) | "?", localVars.map(_.name))
		localVars.exists(_.origName == v.getIdentifier)
	}
			
	private def doArgumentBoxing(method: dom.MethodInvocation, args: List[dom.Expression]): List[Emission] =
		(for (mb <- method.mbinding ; if mb.getParameterTypes.length == args.size) yield {
			val paramTypes: List[TBinding] = mb.getParameterTypes
			
			for ((p, a) <- paramTypes.zip(args)) yield {
				if (a.tb.isPrimitive && p.isPrimitive && !a.tb.isEqualTo(p)) p.emitCast(a.emit)
				else if (a.isPrimitiveType && p.isReferenceType) a.emitBoxedValue
				else if (a.isArray && p.isArray && !a.isSameElementType(p)) p.emitCast(a.emit)
				else a.emit
			}
		}) getOrElse args.map(_.emit)
	
	private val consoleMethods = List(
		"print", "println", "printf", "format", "readLine",
		"readBoolean", "readByte", "readShort", "readChar", "readInt", "readLong", "readFloat", "readDouble",
		"readf", "readf1", "readf2", "readf3"
	)

	private def methodTransformation: Option[Emission] = 
		(expr match {
			case Some(x: dom.SimpleName) => simpleTransforms(x)
			case Some(x: dom.QualifiedName) => qualTransforms(x)
			case _ => None
		}) orElse anyTransforms
		
	private def simpleTransforms(target: dom.SimpleName): Option[Emission] = {
		val SimpleName(targetName) = target
		if (!isAnyValTypeName(targetName)) return None
		
		// operations like java.lang.Float.floatValue are superfluous in scala
		if (isBoxedTypeName(targetName) && (method endsWith "Value")) {
			val newType = method.take(method.size - 5)
			if (isPrimitiveTypeName(newType)) Some(target.emit) else None
		}
		else if (targetName == "Boolean" && method == "valueOf" && args.size == 1) PARENS(args.head.emit)
		else args match {
            case arg :: Nil =>
				// Double.toString(d) => d.toString
                if (isValueBox(method, "to")) INVOKE(arg, name)
				// Float.parseFloat(str)
                else if (isValueBox(method, "parse")) INVOKE(arg, TO <~> Emit(method.substring(5)))
				// Float.valueOf(str) => str.toFloat
                else if (method == "valueOf") INVOKE(arg, TO <~> target)
                // else emitJLRoot ~ emitInvocation
				else None
            
            case arg1 :: arg2 :: Nil if method == "compare" => INVOKE(arg1, Emit("compare")) <~> PARENS(arg2)
            case _ => None
		}
	}
		
	private def qualTransforms(x: dom.QualifiedName): Option[Emission] = {
		// look for a declaration in scope that conflicts with the one we're thinking about unqualifying
		val ids = (eMethod.map(_.allVariableDeclarations) | Nil).map(_.name)
		if (ids.exists { case SimpleName(x) if x == method => true ; case _ => false }) None
		else x.unqualify match {
			// removing System.out. and Console. noise
			case "System.out" | "Console" if consoleMethods contains method => Some(name <~> PARENS(ARGS(args)))
			case  _ => None
		}
	}
	
	private def anyTransforms: Option[Emission] = 
		// we usually leave off parens on no-argument calls, but if the method is varargs scala gets confused
		if (mb.isVarargs) Some(emitInvocation <~> PARENS)
		// the clone method on arrays is not exposed (yet) in scala
		else if (expr.isDefined && (expr.get.tb.isArray && method == "clone" && args.size == 0)) Some(INVOKE(expr.get, emitString("toArray")))
		else None

	// looks for methods with names like toFloat, parseDouble, etc.
	private def isValueBox(method: String, prefix: String): Boolean =
		(method startsWith prefix) && isAnyValTypeName(prefix.substring(prefix.length))
}
