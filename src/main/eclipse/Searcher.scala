package org.improving.scalify

import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom
import org.eclipse.jdt.core.search._
import scala.collection.mutable.HashMap
import scala.collection.immutable
import ScalifySafe._
import IJavaSearchConstants._

case class RenameNodeMsg(x: ASTNode)
object Renaming {
	import Scalify._
	private val searchers = new HashMap[IType, Searcher]
	
	def findCollisions(jdtMap: JDTMap): List[ASTNode] = {
		val results = 
			for ((node, snode) <- jdtMap.table) yield {
				node match {
					case x: dom.MethodDeclaration if x.isConstructor => doConstructorRenaming(x)
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
	
	private def doFieldRenaming(node: dom.FieldDeclaration): List[ASTNode] = for { 
		frag <- node.allFragments
		if getSearcher(node).doesFieldNeedRenaming(frag)
	} yield frag
				
	private def doConstructorRenaming(node: dom.MethodDeclaration): List[ASTNode] = {
		val nodes = 
			if (!node.isPrimary) Nil 
			else for {
				svd <- node.params
				if getSearcher(node).doesConstructorParamNeedRenaming(svd)
			} yield svd
			
		nodes ::: doMethodRenaming(node)
	}
	
	private def doForLoopRenaming(fors: List[dom.ForStatement]): List[ASTNode] = {
		if (fors.size <= 1) return Nil
		
		def compareAllNames(xs: List[NamedDecl]): List[ASTNode] = xs match {
			case Nil => Nil
			case x :: Nil => Nil
			case x :: rest => 
				if (rest.exists(y => compareNames(x, y))) (x.node :: compareAllNames(rest))
				else compareAllNames(rest)
		}
		
		val allNameDecls: List[NamedDecl] = fors.flatMap
			{ _.descendants.map(_.snode).flatMap { case x: NamedDecl => List(x) ; case _ => Nil } }
		compareAllNames(allNameDecls)
	}
	
	private def doMethodRenaming(node: dom.MethodDeclaration): List[ASTNode] = {
		val fors = node.stmts.flatMap { case x: dom.ForStatement => List(x) ; case _ => Nil }
		val methodRename = if (getSearcher(node).doesMethodNeedRenaming(node)) List(node) else Nil
		val forLoopRenames = doForLoopRenaming(fors)
		// val localVarRenames = for {
		// 	frag <- node.allFragments
		// 	if !frag.vb.isField && !frag.vb.isParameter
		// 	if getSearcher(node).doesLocalVarNeedRenaming(frag, node.mb.imethod.get)
		// } yield frag
		
		methodRename ::: forLoopRenames 	// ::: localVarRenames
	}

	private def compareNames(n1: NamedDecl, n2: NamedDecl): Boolean =
		if (n1.isPrivate && n2.isPrivate) false else n1.hasSameReferenceNameAs(n2)
}

class Searcher(itype: IType)
{
	// org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true
	import scala.actors._
	
	val engine = new SearchEngine
	val default = SearchEngine.getDefaultSearchParticipant
	
	val lexicalScope = SearchEngine.createJavaSearchScope(Array(itype: IJavaElement), IJavaSearchScope.SOURCES)
	lazy val hierarchalScope = {
		val h = itype.newSupertypeHierarchy(null)
		val classes = h.getAllSuperclasses(itype).asInstanceOf[Array[IJavaElement]]
		
		SearchEngine.createJavaSearchScope(classes, IJavaSearchScope.SOURCES)
	}

	class Result(pattern: SearchPattern, scope: IJavaSearchScope) extends SearchRequestor with Actor {
		private var _endReporting = false
		private var _matches = 0
		
		start()
				
		def isReady = _endReporting
		def isMatch = isReady && _matches > 0
		def info: String = _matches.toString
		// override def beginReporting() = _beginReporting = true
		override def endReporting() = _endReporting = true
		
		def acceptSearchMatch(m: SearchMatch) = m match {
			case SearchMatch(element, resource) => _matches += 1
			case _ =>
		}
		
		def act() = {
			engine.search(pattern, Array(default), scope, this, null)
			while (true) {
				receive {
					case _ if isReady => reply("Done") ; exit()
				}
			}
		}
	}
		
	def doesFieldNeedRenaming(v: dom.VariableDeclaration) =
		doSearch(v.getName.getIdentifier, lexicalScope, METHOD) ||
		doSearch(v.getName.getIdentifier, hierarchalScope, FIELD, METHOD)
	
	def doesConstructorParamNeedRenaming(svd: dom.SingleVariableDeclaration): Boolean =
		doSearch(svd.getName.getIdentifier, lexicalScope, FIELD, METHOD)

	def doesMethodNeedRenaming(m: dom.MethodDeclaration) =
		doSearch(m.getName.getIdentifier, lexicalScope, FIELD)
	
	def doesLocalVarNeedRenaming(v: dom.VariableDeclaration, imethod: IMethod) =
		doSearch(v.getName.getIdentifier, lexicalScope, METHOD)

	private def doSearch(name: String, scope: IJavaSearchScope, whats: Int*): Boolean = {
		val pattern = whats
			. map(x => SearchPattern.createPattern(name, x, DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE))
			. reduceLeft(or(_, _))
			
		val r = new Result(pattern, scope)
		r !? "Done"
		r.isMatch
	}
	
	def or(left: SearchPattern, right: SearchPattern): SearchPattern = SearchPattern.createOrPattern(left, right)

	object SearchMatch {
		def unapply(m: SearchMatch) = {
			val element = m.getElement
			val resource = m.getResource
			
			Some(element, resource)
		}
	}
}





