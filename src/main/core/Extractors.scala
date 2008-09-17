package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

trait Extractors extends GenExtractors
{
	import JavaTypes._
	
	// transforms a JDT operator class into the string it represents
	object JavaOp {
        def unapply(op: Any) = op match {
			case x: dom.PrefixExpression.Operator => Some(x.toString)
			case x: dom.PostfixExpression.Operator => Some(x.toString)
			case x: dom.InfixExpression.Operator => Some(x.toString)
			case x: dom.Assignment.Operator => Some(x.toString)
			case _ => None
		}
	} 
	
	object JPrimitive {
		def unapply(node: ASTNode) = node match {
			case PrimitiveType(code) => primitiveTypes.find(_.code == code)
			case _ => None
		}
		def unapply(tb: TBinding) = primitiveTypes.find(_.name == tb.getName)
	}
	
	object JBoxed {
		def unapply(node: ASTNode) = node match {
			case SimpleType(name) => boxedTypes.find(_.name == name.fqname)
			case _ => None
		}		
		def unapply(tb: TBinding) = boxedTypes.find(_.name == tb.getName)
	}
	
}
object Extractors extends Extractors

trait UnsafeExtractors extends Extractors
{	
	object JArray {
		def unapply(node: TypeBound): Option[(TBinding, Int)] = unapply(node.tb)			
		def unapply(tb: TBinding): Option[(TBinding, Int)] = 
			if (tb.isArray) Some(tb.getElementType, tb.getDimensions) else None
	}
	
	object TypeBinding { 
		def unapply(node: ASTNode): Option[TBinding] = node.snode match {
			case x: TypeBound if x.tb != null => Some(x.tb)
			case _ => None
		}
	}
	object VariableBinding { 
		def unapply(node: ASTNode): Option[VBinding] = node.snode match {
			case x: VariableBound if x.vb != null => Some(x.vb)
			case _ => None
		}
	}
	object MethodBinding { 
		def unapply(node: ASTNode): Option[MBinding] = node.snode match {
			case x: MethodBound if x.mb != null => Some(x.mb)
			case _ => None
		}
	}
	object PackageBinding { 
		def unapply(node: ASTNode): Option[PBinding] = node.snode match {
			case x: PackageBound if x.pb != null => Some(x.pb)
			case _ => None
		}
	}	
	
}
object UnsafeExtractors extends UnsafeExtractors
