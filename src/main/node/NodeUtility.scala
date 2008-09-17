package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import org.eclipse.jdt.core.dom.{ PrimitiveType => PT }

trait NodeUtility
{
	self: Node =>

	// nesting level tells us how much we need to wrap deep internal returns
	// it counts the function layers between the node and the closest method declaration
	def getNestingLevel: Int = getNestingLevel(ancestors)
	private def getNestingLevel(xs: List[ASTNode]): Int = xs match {
		case Nil => 0
		case x :: rest => x match {
			case _: dom.MethodDeclaration => 0
			case x: dom.Statement if x.addsNestingLevel => 1 + getNestingLevel(rest)
			case _ => getNestingLevel(rest)
		}		
	}

	def isConvertibleToIgnoreNames(lhs: ASTNode): Boolean =
		isAssignableTo(lhs) || 
		isCastableTo(lhs) || 
		(lhs match {
			// Setting Varargs = Array
			case x: dom.SingleVariableDeclaration if x.isVarargs && node.isArray =>
				for (tb <- tbinding ; val el = tb.getElementType)
					yield el.isAssignableTo(lhs) || el.isCastableTo(lhs)
			// Setting String = Array[Char]
			case x: dom.SingleVariableDeclaration if x.isString && node.isCharArray => true
			case _ => false				
		})
	
	
	def isConvertibleTo(lhs: ASTNode): Boolean =
		// println("isConvertibleTo? Trying to set " + lhs.trueName + " = " + node.trueName)
		(lhs, node) match {
			case (l: Named, r: Named) => l.hasSameOriginalNameAs(r) && isConvertibleToIgnoreNames(lhs)
			case _ => isConvertibleToIgnoreNames(lhs)
		}		
		
	// returns emission => emission function which when applied to an rhs type node's emission, yields an lhs type
	def doConversion(lhs: ASTNode): EFilter =
		(for (ltb <- lhs.tbinding ; rtb <- tbinding) yield
			if (ltb.isArray && ltb.getElementType.isEqualTo(rtb))
				(x: Emission) => ARRAY <~> PARENS(x)
			else if (ltb.isString && rtb.isCharArray)
				(x: Emission) => INVOKE(x, Emit("mkString"))
			else identity) getOrElse identity
	
	def isChar:			Boolean = isSomeType(PT.CHAR)
	def isInt:			Boolean = isSomeType(PT.INT)
	def isCharArray:	Boolean = tbinding.map(_.isCharArray)
	def isString:		Boolean = tbinding.map(_.isString)
	def isArray:		Boolean = tbinding.map(_.isArray)

	def isPrimitiveType: Boolean = tbinding.map(_.isPrimitive)
	def isAnyValType: Boolean = tbinding.map(_.isAnyValType)
	def isReferenceType: Boolean = tbinding.map(_.isReferenceType)
	
	def isSomeType(code: PT.Code): Boolean = tbinding.map(_.isSomeType(code))
	def isSomeType(s: String): Boolean = tbinding.map(_.isSomeType(s))
	
	def isAssignableTo(lhs: ASTNode): Boolean = 
		for (ltb <- lhs.tbinding ; rtb <- tbinding) yield rtb.isAssignableTo(ltb)
	def isCastableTo(lhs: ASTNode): Boolean =
		for (ltb <- lhs.tbinding ; rtb <- tbinding) yield rtb.isCastableTo(ltb)		
	def isSameTypeBinding(other: ASTNode): Boolean =
		for (tb1 <- tbinding ; tb2 <- other.tbinding) yield tb1.isEqualTo(tb2)

	def isSameElementType(other: TBinding): Boolean = 
		for (tb <- tbinding) yield tb.isSameElementType(other)
	def isSameElementType(other: ASTNode): Boolean = 
		for (tb1 <- tbinding ; tb2 <- other.tbinding) yield tb1.isSameElementType(tb2)
	
	def compareSimpleNames(n1: dom.SimpleName, n2: dom.SimpleName): Boolean = n1.getIdentifier == n2.getIdentifier

	// for primitive types
	def emitBoxedValue: Emission = 
		(for (tb <- tbinding ; if tb.isPrimitive) yield
			emitJLRoot ~ getAnyValType(tb).emitBoxed(node)) getOrElse node

	// what to assign when we can't use _
	def emitDefaultValue: Emission = node match {
		case PrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.BOOLEAN) => FALSE
		case PrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.VOID) => PARENS
		case PrimitiveType(_) => ZERO
		case _ => NULL
	}
}

