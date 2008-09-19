package org.improving.scalify

import org.eclipse.jdt.core.dom
import scala.collection.mutable.{ HashMap, ListBuffer }
import scala.collection.immutable
import ScalifySafe._

// A JDTForest is each CU mapped to its JDTMap
// A JDTMap is each ASTNode mapped to its Node

object Forest
{
	private var forest: JDTForest = null
	def keys: List[dom.CompilationUnit] = forest.trees.keys.toList

	def initialize(xs: List[dom.CompilationUnit]) = {
		def doASTTraversal(cu: dom.CompilationUnit): JDTMap = {
			val v = new JDTVisitor(cu)
			
			cu.accept(v)
			v.tree
		}
		val trees: Map[dom.CompilationUnit, JDTMap] = PCompute.runAll(xs, doASTTraversal _)
		forest = new JDTForest(trees)
	}

	def get(node: ASTNode): Node			= forest.lookup(node)
	def getJDTMap(cu: dom.CompilationUnit): JDTMap		= forest.trees(cu)
	
	// go through all nodes in all trees, optionally altering
	def transformByNode(f: (ASTNode) => Option[Node]) = {
		val newTrees: Map[dom.CompilationUnit, JDTMap] = PCompute.runAll(keys, x => getJDTMap(x).map(f))

		forest = new JDTForest(newTrees)
	}
	
	// go through on a map-by-map basis
	def transformByMap(f: (JDTMap) => JDTMap) = {
		val newTrees: Map[dom.CompilationUnit, JDTMap] = PCompute.runAll(keys, x => f(getJDTMap(x)))
			
		forest = new JDTForest(newTrees)
	}

	// search all trees in parallel for nodes meeting a condition
	def search[T](f: (JDTMap) => List[T]): List[T] = {
		val results = PCompute.runAll(keys, (x: dom.CompilationUnit) => f(getJDTMap(x)))
			
		List.flatten(results.values.toList)
	}
	// def search(f: (JDTMap) => List[ASTNode]): List[ASTNode] = {
	// 	val results = PCompute.runAll(keys, (x: dom.CompilationUnit) => f(getJDTMap(x)))
	// 		
	// 	List.flatten(results.values.toList)
	// }

	import scala.actors.Actor. { actor, receive, reply }
	val renamer = actor {
		while(true) {
			receive {
				case SetNodeName(node) => get(node) match {
					case x: NamedDecl => x.incrementName
					case _ => abort(node.toString)
				}
				case GetNodeName(node) => get(node) match {
					case x: Named => reply(x.currentName)
					case _ => abort(node.toString)
				}
				case x => abort(x.toString)
			}
		}
	}
}
