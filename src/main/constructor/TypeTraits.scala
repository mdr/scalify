package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

trait HasTypes
{
	val node: ASTNode
	val stds: List[dom.TypeDeclaration]
	def emitFactoryImports: Emission = REP(imports)

	lazy private val superTypes: List[dom.Type] = 
		removeDuplicateTypes(stds.map(_.getSuperclassType).filter(_ != null))
	lazy private val imports = 
		superTypes.filter(_.pkgName != node.pkgName).map(_.emitImportsWhenSuper)				
	
	private def removeDuplicateTypes(xs: List[dom.Type]): List[dom.Type] = xs match {
		case Nil => Nil
		case x :: rest => 
			if (rest.map(_.tb).exists(tb => tb.isEqualTo(x.tb))) removeDuplicateTypes(rest)
			else x :: removeDuplicateTypes(rest)
	}
}