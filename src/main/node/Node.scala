package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.ITypeHierarchy
import org.eclipse.jdt.core.dom
// import scalaz.OptionW._
abstract trait ASTNodeAdditions extends ASTNodeSafe
{	
	// usually we use the implicits, but sometimes we need to force a conversion
	def snode: Node = Scalify.lookup(node)
	def hierarchy: ITypeHierarchy = cu.typeHierarchy

	def findDeclaringType: Option[dom.TypeDeclaration] = this match {
		case x: TypeBound => x.findTypeDeclaration
		case _ => node match {
			case x: dom.BodyDeclaration => findEnclosingType
			case _ => None
		}
	}
	
	def isEnclosedInSameType(other: ASTNode): Boolean = {
		log.trace("isEnclosedInSameType: %s %s", node.findEnclosingType.map(_.id), other.findEnclosingType.map(_.id))
		for (etype1 <- node.findEnclosingType ; etype2 <- other.findEnclosingType)
			return etype1.tb.isEqualTo(etype2.tb)
			
		false
	}
	
	def isInStatic: Boolean = {
		val bodyDecl: dom.BodyDeclaration = ancestors.flatMap
			{ case x: dom.BodyDeclaration => List(x) ; case _ => Nil } head
			
		bodyDecl.isStatic
	}

	def isInsideConstructor: Boolean = findEnclosingMethod.map(_.isConstructor) getOrElse false
}
	
//
// To "emit" is to return a list of Emits (i.e. an Emission) that when properly
// assembled will yield the output source.
//

abstract trait Emittable
{
	def emit: Emission = emitDirect
	def emitDirect: Emission									// Abstract!
	def emitDirect(context: ASTNode): Emission = emitDirect		// For specializing based on context
}

abstract trait Emitter extends Emittable
{
	self: Node =>

	def emitDefault: Emission = 								// default emission shouldn't be reached
		abort("Reached default emission:    " + node.toString)	
	def emitJLRoot: Emission = if (cu.needsRoot("java")) ROOTPKG <~> DOT ~ NOS else Nil
	
	// These are just gateways into the Emission enhancements
	def ~(ys: Emission): Emission = node.emit ~ ys
	def <~>(ys: Emission): Emission = node.emit <~> ys	
}

//
// Node: Abstract superclass of all node types
//
abstract class Node(val node: ASTNode)
	extends NodeUtility
	with JavaEmulation
	with JavaTypes
	with ASTNodeAdditions
	with Emitter
	with Tree
{
	import org.eclipse.jdt.core.{ ISourceRange, ISourceReference }
	
	// id is only for debugging - ppString is for optional pretty printing
	final def classId: String = " (" + node.getClass.getSimpleName + " / " + snode.getClass.getSimpleName + ")"
	final def id: String = toString + classId
	override def toString = "<" + node.getClass.getSimpleName + ">"
	def ppString: Option[String] = this match {
		case x: Named => Some(x.origName)
		case x: Bound => Some(x.bindingName)
		case _ => None
	}
	
	def tbinding: Option[TBinding] = node match { case TypeBinding(tb) => Some(tb) ; case _ => None }
	def vbinding: Option[VBinding] = node match { case VariableBinding(vb) => Some(vb) ; case _ => None }
	def mbinding: Option[MBinding] = node match { case MethodBinding(mb) => Some(mb) ; case _ => None }
	
	def declaringNode: Option[NamedDecl] = this match {
		case x: NamedDecl => Some(x)
		case _ => None
	}

	def copyIn(other: Node): Node = {
		children = other.children
		return this
	}	
}

