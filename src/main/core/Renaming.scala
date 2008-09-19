package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scala.collection.mutable.HashMap
import org.eclipse.jdt.core. { IType, IField, IMethod, ILocalVariable }

case class SetNodeName(x: ASTNode)
case class GetNodeName(x: ASTNode)

object Renaming {
	private val searchers = new HashMap[IType, Searcher]	
	
	// confirmed collisions
	case class RenameGroup(override val xs: List[ASTNode]) extends NameGroup(xs) {
		override val collisions: List[NamedDecl] = namedDecls
	}
	case class NameGroup(xs: List[ASTNode]) {
		val namedDecls: List[NamedDecl] = getDecls(xs)
		val collisions: List[NamedDecl] = compareAll(namedDecls)
	}
	
	// main entry point
	def performAllRenaming = {
		val groups: List[NameGroup] = Forest.search(Renaming.findCollisions(_))
		val collisions = groups.flatMap(_.collisions).removeDuplicates
		println(nodeInfo(collisions.map(_.node)))
		
		def groupRenaming(xs: List[NamedDecl]): Unit = xs match {
			case Nil => return
			case x :: rest => 
				val targets = xs.filter(y => compareNamesVolatile(x, y))
				for (t <- targets) Forest.renamer ! SetNodeName(t.node)
				groupRenaming(rest)
		}

		def compareNamesVolatile(n1: NamedDecl, n2: NamedDecl): Boolean = {
			val c1 = Forest.renamer !? GetNodeName(n1.node)
			val c2 = Forest.renamer !? GetNodeName(n2.node)
			
			c1 == c2
		}
		
		groups.foreach(g => groupRenaming(g.collisions))
		// for (node <- nodesToRename) 
		// 	Forest.renamer ! RenameNodeMsg(node)
		// 
		// // for collisions among collisions
		// doAnotherRename(nodesToRename)		
	}
	
	private def findCollisions(jdtMap: JDTMap): List[NameGroup] = {
		val results = 
			for ((node, snode) <- jdtMap.table) yield {
				node match {
					case x: dom.TypeDeclaration => doTypeRenaming(x)
					// case x: dom.MethodDeclaration if x.isConstructor && x.isPrimary=> doConstructorRenaming(x)
					case x: dom.MethodDeclaration if !x.isConstructor => doMethodRenaming(x)	// constructors handled in type
					// case x: dom.FieldDeclaration => doFieldRenaming(x)
					// case x: dom.VariableDeclaration => doVariableRenaming(x)
					case _ => Nil
				}
			}

		List.flatten(results.toList.filter(_ != Nil))
	}
	
	private def getSearcher(node: ASTNode): Searcher = synchronized {
		val itype = node.snode.findEnclosingType.get.itype.get

		searchers.get(itype) getOrElse {
			searchers(itype) = new Searcher(itype)
			searchers(itype)
		}
	}
	
	private def doTypeRenaming(node: dom.TypeDeclaration): List[NameGroup] = {
		val indCons = node.independentConstructors
		val conParams = indCons.flatMap(_.params)
		val conFors = indCons.flatMap(c => forInits(fors(c.stmts)))
		val conLocals = indCons.flatMap(_.allVariableDeclarations)
		val frags = node.fields.flatMap(_.allFragments)
		val superCollidingFields = frags.filter(f => getSearcher(node).doesFieldNeedRenaming(f))
		// val fieldRenames = 
		// 	for {
		// 		f <- node.fields.flatMap(_.allFragments)
		// 		if node.methods.exists(compareNames(f, _)) || params.exists(compareNames(f, _))
		// 	} yield f
		// val paramRenames = for (p <- params ; if node.methods.exists(compareNames(p, _))) yield p
		val varGroup = conParams ::: conFors ::: conLocals ::: (frags -- superCollidingFields)
		val methodCollidingVars = List.flatten(for (m <- node.methods) yield varGroup.filter(v => compareNames(m, v))).removeDuplicates
		
		List(RenameGroup(superCollidingFields ::: methodCollidingVars), NameGroup(varGroup -- methodCollidingVars))
		
		// List(RenameGroup(fieldRenames ::: paramRenames))
	}
		
	private def doFieldRenaming(node: dom.FieldDeclaration): List[NameGroup] =
		List(RenameGroup(
			for { 
				frag <- node.allFragments
				if getSearcher(node).doesFieldNeedRenaming(frag)
			} yield frag
		))
				
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
	
	private def doMethodRenaming(node: dom.MethodDeclaration): List[NameGroup] = {
		val allBlocks = node.descendants.flatMap { case x: dom.Block => List(x) ; case _ => Nil }
		
		allBlocks.flatMap(doScopeRenaming)
				
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
	private def doScopeRenaming(node: dom.Block): List[NameGroup] = {
		val forVars = forInits(fors(node.stmts))
		val localVars = node.allFragments
		// val params = node.getParent match {
		// 	case m: dom.MethodDeclaration if m.isConstructor && m.isPrimary => log.trace("doScopeRenaming including params: %s", m.params) ; m.params
		// 	case _ => Nil
		// }
		
		List(NameGroup(forVars ::: localVars))
	}
	
	private def fors(xs: List[ASTNode]) = xs flatMap { case x: dom.ForStatement => List(x) ; case _ => Nil }
	private def forInits(xs: List[dom.ForStatement]): List[dom.VariableDeclaration] = xs flatMap { _.inits } flatMap { _.allFragments }
	
	// // given a list of NamedDecls in the same scope, returns list of nodes needing renaming
	// private def compareAllNodes(xs: List[ASTNode]): List[ASTNode] = {
	// 	def compareAllNames(xs: List[NamedDecl]): List[ASTNode] = xs match {
	// 		case Nil => Nil
	// 		case x :: Nil => Nil
	// 		case x :: rest => 
	// 			if (rest.exists(y => compareNames(x, y))) (x.node :: compareAllNames(rest))
	// 			else compareAllNames(rest)
	// 	}	
	// 	
	// 	val names = xs.map(_.snode) flatMap { case x: NamedDecl => List(x) ; case _ => Nil }
	// 	compareAllNames(names)
	// }
	

	private def compareNames(n1: NamedDecl, n2: NamedDecl): Boolean =
		// if (n1.isPrivate && n2.isPrivate) false else  		XXX needs to be more specific, i.e. private method and private field in same class
		// n1.hasSameReferenceNameAs(n2)
		n1.hasSameReferenceNameAs(n2)

	// returns list of nodes needing renaming in the given set - which excludes the first of each colliding set
	private def compareAll(xs: List[NamedDecl]): List[NamedDecl] = xs match {
		case Nil => Nil
		case x :: rest =>			
			val (collisions, others) = rest.partition(y => Renaming.compareNames(x, y))
			if (collisions.isEmpty) compareAll(rest)
			else collisions ::: compareAll(others)
	}
		
	private def getDecls(xs: List[ASTNode]): List[NamedDecl] = xs.map(_.snode).flatMap { case x: NamedDecl => List(x) ; case _ => Nil }
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
