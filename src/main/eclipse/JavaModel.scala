package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom

// the java model in eclipse is all the IJavaElement business
trait JavaModel
{
	def getICU(cu: dom.CompilationUnit): ICU = cu.getJavaElement.asInstanceOf[ICU]
}
object JavaModel extends JavaModel

class RichIJavaElement(jelement: IJavaElement) { }

// The IMembers are: IType, IMethod, IField, and IInitializer
class RichIMember(member: IMember) extends RichIJavaElement(member) with Modifiable {
	val node = null
	def flags = member.getFlags
	def srcWithoutComments: Emission = {
		val commentRegexpBlock = "/\\*(?:.|[\\n\\r])*?\\*/"
		val commentRegexpLine = """(?m)[ \t]*\/\/.*?\n"""
		val str = member
			. getSource
			. replaceAll(commentRegexpLine, "\n")
			. replaceAll(commentRegexpBlock, " ")
			. replaceAll("""(?m)^\s*$""", "")		// empty lines

		if (str.matches("""\s*""")) Nil
		else COMMENT(removeCommonPrefix(str) + "\n")
	}
}

class RichIMethod(imethod: IMethod) extends RichIMember(imethod)
{
	def id = imethod.getElementName + 
				"(" + sigString + ") " + 	
				(if (imethod.isResolved) "" else " (not resolved)")
				
	def sigString = Signature.toString(imethod.getSignature)		
}

class RichIType(itype: IType) extends RichIMember(itype) {
	// lazy val fields = itype.getFields
	// lazy val methods = itype.getMethods
	def fields: List[IField] = itype.getFields
	def methods: List[IMethod] = itype.getMethods
	def id = itype.getTypeQualifiedName + (if (itype.isResolved) "" else " (not resolved)")
	
	def hasStaticMembers: Boolean = {
		val children: List[IJavaElement] = itype.getChildren
		for (c <- children) c match {
			case x: IType if x.isStatic || x.isInterface => return true
			case x: IField if x.isStatic => return true
			case x: IMethod if x.isStatic => return true
			case _ =>
		}
		false
	}
	
	def ensureOpen: IType = {
		val op = itype.getOpenable
		if (op == null || op.isOpen) return itype
		
		log.trace("ensureOpen: %s ", itype)
		op.open(null)
		itype
	}
	
	// fields.exists(_.isStatic) || methods.exists(_.isStatic)
}
