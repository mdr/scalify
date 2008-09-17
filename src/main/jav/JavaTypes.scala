package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import org.eclipse.jdt.core.dom.{ PrimitiveType => PT }

abstract class JAnyVal {
	val name: String
	val fqname: String = name
	val code: PT.Code
	def emit: Emission
	def emitBoxed: EFilter
	def emitCast: EFilter
}

sealed class JPrimitive(val name: String, val code: PT.Code) extends JAnyVal {		
	def this(x: String) = this(x, PT.toCode(x))
	def this(x: PT.Code) = this(x.toString, x)
	
	def emit: Emission = code match {
		case PT.VOID => UNIT
		case PT.CHAR => CHAR
		case PT.INT => INT
		case _ => emitString(toString)
	}
	// if (code == PT.VOID) UNIT else emitString(toString)
	
	def emitBoxed: EFilter = (x: Emission) => INVOKE(emitAsRef, VALUEOF <~> PARENS(x))
	def emitAsRef: Emission = if (code == PT.VOID) UNIT else JavaTypes.boxedTypes.find(_.code == code).get.emit
	def emitCast: EFilter = (x: Emission) => INVOKE(x, emitString("to") <~> emit)
			
	override def toString: String = name.capitalize
}

sealed class JBoxed(val name: String, val code: PT.Code) extends JAnyVal {
	def this(x: PT.Code) = this(JavaTypes.codeToJavaRef(x), x)
	def isEqualTo(s: String): Boolean = s == name || s == toString
	override val fqname = ROOTPKG.s + "." + JAVALANG.s + "." + name		// TODO
	val primitiveName = code.toString
	
	def emit: Emission = emitJava
	def emitCast: EFilter = (x: Emission) => INVOKE(x, ASINSTANCEOF) <~> BRACKETS(emit)
	def emitBoxed: EFilter = identity
	def emitJava: Emission = if (code == PT.VOID) UNIT else emitString(toString)
	def emitScala: Emission = code match {
		case PT.INT => INT
		case PT.CHAR => CHAR
		case _ => emitJava
	}
	override def toString = fqname
}	

trait JavaTypes
{
	import JavaTypes._
	
	def getAnyValType(tb: TBinding): JAnyVal = allTypes.find(_.name == tb.getName) getOrElse abort
	def getAnyValType(code: PT.Code): JAnyVal = allTypes.find(_.code == code) getOrElse abort
	def getAnyValType(name: String): JAnyVal = allTypes.find(_.name == name) getOrElse abort
	def isAnyValTypeName(name: String): Boolean = allTypes.exists(_.name == name)
	def isPrimitiveTypeName(name: String): Boolean = primitiveTypes.exists(_.name == name)
	def isBoxedTypeName(name: String): Boolean = boxedTypes.exists(_.isEqualTo(name))
}

object JavaTypes extends JavaTypes {
	val codes: List[PT.Code] = List(PT.INT, PT.CHAR, PT.BOOLEAN, PT.SHORT, PT.LONG, PT.FLOAT, PT.DOUBLE, PT.BYTE)
	val primitiveTypes: List[JPrimitive] = for (code <- (PT.VOID :: codes)) yield new JPrimitive(code)
	val boxedTypes: List[JBoxed] = for (code <- codes) yield new JBoxed(code)
	val allTypes: List[JAnyVal] = primitiveTypes ::: boxedTypes
	
	def codeToJavaRef(code: PT.Code): String = code match {
		case PT.INT => "Integer"
		case PT.CHAR => "Character"
		case _ => code.toString.capitalize
	}
}
