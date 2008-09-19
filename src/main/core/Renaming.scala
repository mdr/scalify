package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scala.collection.mutable.HashMap
import org.eclipse.jdt.core. { IType, IField, IMethod, ILocalVariable }

case class RenameNodeMsg(x: ASTNode)

object Renaming {
	private val searchers = new HashMap[IType, Searcher]
	
	// main entry point
	def performAllRenaming = {
		val nodesToRename: List[ASTNode] = Forest.search(Renaming.findCollisions(_)).removeDuplicates
		println(nodeInfo(nodesToRename))

		for (node <- nodesToRename) 
			Forest.renamer ! RenameNodeMsg(node)

		// for collisions among collisions
		doAnotherRename(nodesToRename)		
	}
	
	private def findCollisions(jdtMap: JDTMap): List[ASTNode] = {
		val results = 
			for ((node, snode) <- jdtMap.table) yield {
				node match {
					case x: dom.TypeDeclaration => doTypeRenaming(x)
					// case x: dom.MethodDeclaration if x.isConstructor && x.isPrimary=> doConstructorRenaming(x)
					case x: dom.MethodDeclaration => doMethodRenaming(x)
					case x: dom.FieldDeclaration => doFieldRenaming(x)
					// case x: dom.VariableDeclaration => doVariableRenaming(x)
					case _ => Nil
				}
			}
			
		List.flatten(results.toList).removeDuplicates
	}
	
	private def getSearcher(node: ASTNode): Searcher = synchronized {
		val itype = node.snode.findEnclosingType.get.itype.get

		searchers.get(itype) getOrElse {
			searchers(itype) = new Searcher(itype)
			searchers(itype)
		}
	}
	
	private def doTypeRenaming(node: dom.TypeDeclaration): List[ASTNode] = {
		val params = node.independentConstructors.flatMap(_.params)		
		val fieldRenames = 
			for {
				f <- node.fields.flatMap(_.allFragments)
				if node.methods.exists(compareNames(f, _)) || params.exists(compareNames(f, _))
			} yield f
		val paramRenames = for (p <- params ; if node.methods.exists(compareNames(p, _))) yield p
		
		fieldRenames ::: paramRenames
	}
	
	private def doFieldRenaming(node: dom.FieldDeclaration): List[ASTNode] = for { 
		frag <- node.allFragments
		if getSearcher(node).doesFieldNeedRenaming(frag)
	} yield frag
				
	// private def doConstructorRenaming(node: dom.MethodDeclaration): List[ASTNode] = {
	// 	val nodes = 
	// 		if (!node.isPrimary) Nil 
	// 		else for {
	// 			svd <- node.params
	// 			if getSearcher(node).doesConstructorParamNeedRenaming(svd)
	// 		} yield svd
	// 		
	// 	nodes ::: doMethodRenaming(node)
	// }
	
	// private def doForLoopRenaming(fors: List[dom.ForStatement]): List[ASTNode] = {
	// 	if (fors.size <= 1) return Nil
	// 	val allNameDecls: List[NamedDecl] = fors.flatMap
	// 		{ _.descendants.map(_.snode).flatMap { case x: NamedDecl => List(x) ; case _ => Nil } }
	// 	compareAllNames(allNameDecls)
	// }
	
	private def doMethodRenaming(node: dom.MethodDeclaration): List[ASTNode] = {
		val allBlocks = node.descendants.flatMap { case x: dom.Block => List(x) ; case _ => Nil }
		val nodes = allBlocks.flatMap(doScopeRenaming).removeDuplicates
		
		nodes
		
		// val fors = node.stmts.flatMap { case x: dom.ForStatement => List(x) ; case _ => Nil }
		// val methodRename = if (getSearcher(node).doesMethodNeedRenaming(node)) List(node) else Nil
		// val forLoopRenames = doForLoopRenaming(fors)
		// // val localVarRenames = for {
		// // 	frag <- node.allFragments
		// // 	if !frag.vb.isField && !frag.vb.isParameter
		// // 	if getSearcher(node).doesLocalVarNeedRenaming(frag, node.mb.imethod.get)
		// // } yield frag
		// 
		// /* methodRename ::: */ forLoopRenames 	// ::: localVarRenames
	}
	
	// does for loop renaming within a single scope
	private def doScopeRenaming(node: dom.Block): List[ASTNode] = {
		val fors = node.stmts.flatMap { case x: dom.ForStatement => List(x) ; case _ => Nil }
		val forVars = fors flatMap { _.inits } flatMap { _.allFragments }
		val localVars = node.allFragments
		// val params = node.getParent match {
		// 	case m: dom.MethodDeclaration if m.isConstructor && m.isPrimary => log.trace("doScopeRenaming including params: %s", m.params) ; m.params
		// 	case _ => Nil
		// }
		
		compareAllNodes(forVars ::: localVars)
	}
	
	// given a list of NamedDecls in the same scope, returns list of nodes needing renaming
	private def compareAllNodes(xs: List[ASTNode]): List[ASTNode] = {
		def compareAllNames(xs: List[NamedDecl]): List[ASTNode] = xs match {
			case Nil => Nil
			case x :: Nil => Nil
			case x :: rest => 
				if (rest.exists(y => compareNames(x, y))) (x.node :: compareAllNames(rest))
				else compareAllNames(rest)
		}	
		
		val names = xs.map(_.snode) flatMap { case x: NamedDecl => List(x) ; case _ => Nil }
		compareAllNames(names)
	}
	

	private def compareNames(n1: NamedDecl, n2: NamedDecl): Boolean =
		// if (n1.isPrivate && n2.isPrivate) false else  		XXX needs to be more specific, i.e. private method and private field in same class
		n1.hasSameReferenceNameAs(n2)
		

	private def doAnotherRename(nodes: List[ASTNode]) = {		
		def dar(xs: List[NamedDecl]): Unit = xs match {
			case Nil => return
			case x :: rest =>
				val targets = rest.filter(y => Renaming.compareNames(x, y))
				targets.foreach { y => Forest.renamer ! RenameNodeMsg(y.node) }
				dar(rest)
				// 
				// rest.find(y => Renaming.compareNames(x, y)) match {
				// 	case None => dar(rest)
				// 	case Some(y) => 
				// 		Forest.renamer ! RenameNodeMsg(x.node)
				// 		dar(rest)
				// }
		}
		
		val namedDecls = nodes.map(_.snode).flatMap { case x: NamedDecl => List(x) ; case _ => Nil }
		dar(namedDecls)
	}
		
	private def nodeInfo(xs: List[ASTNode]): String = {
		type HasName = ASTNode { def getName(): dom.SimpleName }
		val pairs = xs.map { x => (getICU(x.cu).getElementName, x.asInstanceOf[HasName].getName.getIdentifier) }
		val grouped = groupByKey(pairs)
		
		"Renaming " + xs.size + " nodes.\n" + (
			(for ((name, ids) <- grouped) yield {
				"  " + name + ": " + ids.mkString(", ")
			}).mkString("\n")
		)
	}	
}
