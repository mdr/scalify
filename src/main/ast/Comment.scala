package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

class BlockComment(override val node: dom.BlockComment) extends Comment(node)
class Javadoc(override val node: dom.Javadoc) extends Comment(node)

class Comment(node: dom.Comment) extends Node(node)
{
	def emitDirect: Emission = emitDefault
}
