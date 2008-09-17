package org.improving.scalify

import ScalifySafe._
import org.eclipse.jdt.core.dom

class ScalaAST(val map: JDTMap)
{
	val pp = new PrettyPrinter(map.root)
		
	def emit = pp.emit
	def show = pp.show
}

class JDTVisitor(val cu: dom.CompilationUnit) extends dom.ASTVisitor 
{
	private val nodes = new MuNodeMapping
	private val icu = getICU(cu)
	lazy val tree = new JDTMap(cu, nodes)
	
	override def preVisit(node: ASTNode): Unit = {
		record(node)
		val n = lookup(node)
		if (node.parent != null)
			lookup(node.parent).addChild(node)
	}
	
	def record(node: ASTNode): Unit =
		if (node == null || nodes.get(node).isDefined) return
		else nodes(node) = new RecordingNode(node)
		
    def lookup(node: ASTNode): RecordingNode = 
		if (node == null) abort else nodes(node).asInstanceOf[RecordingNode]
}

class JDTForest(val trees: Map[dom.CompilationUnit, JDTMap]) {	
	def lookup(node: ASTNode): Node = trees(node.cu).lookup(node)
}

class JDTMap(val root: dom.CompilationUnit, val table: MuNodeMapping) {
	// possibly replaces each node, preserving parent/child info
	def map(f: (ASTNode) => Option[Node]): JDTMap = map((node, map) => f(node))
	
	private def map(f: (ASTNode, JDTMap) => Option[Node]): JDTMap = {
		for (x <- depthFirst) table(x) = transform(x, f)
		return this
	}
	private def transform(node: ASTNode, f: (ASTNode, JDTMap) => Option[Node]): Node =
		f(node, this) match {
			case None => table(node)
			case Some(x) => x.copyIn(table(node))		// copyIn preserves tree information
		}

	// list representing depth-first traversal
	private def depthFirst: List[ASTNode] = depthFirst(root)
	private def depthFirst(node: ASTNode): List[ASTNode] = {
		val snode = table(node)
		if (snode.children.isEmpty) List(node)
		else node :: snode.children.flatMap(depthFirst)
	}

	def lookup(node: ASTNode): Node = table(node)
}
