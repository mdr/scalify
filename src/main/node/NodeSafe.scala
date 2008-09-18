package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scala.collection.immutable
import scala.collection.mutable.{ HashMap, HashSet }
import scalaz.OptionW._

trait SafeImplicits
{
	// why oh why
	implicit def seqToList[T](x: Seq[T]): List[T] = x.toList
		
	// java.util.List[?0] => List[T]    
	implicit def jListToScalaList[T](jlist: java.util.List[T] forSome { type T; }): List[T] = {
	    	val tjlist: java.util.List[T] = jlist.asInstanceOf[java.util.List[T]]
	    	val tarray: Array[T] = tjlist.toArray.map { x => x.asInstanceOf[T] }
	    	tarray.toList
	}   
	
	implicit def jArrayToScalaList[T](jarray: Array[T]): List[T] = {
		if (jarray == null) Nil
		else jarray.toList
	}
	
	// mutable<->immutable maps
	implicit def muToIm[A, B](x: HashMap[A, B]): immutable.Map[A, B] = immutable.HashMap.empty[A, B] ++ x
	implicit def muToImSet[A](x: HashSet[A]): immutable.Set[A] = immutable.Set.empty[A] ++ x
	
	// lists <=> arrays
	// implicit def listToArray[T](list: List[T]): Array[T] = list.toArray
	// implicit def arrayToList[T](array: Array[T]): List[T] = array.toList
	
	// listbuffer => list
	implicit def listBufferToList[T](lb: scala.collection.mutable.ListBuffer[T]): List[T] = lb.toList
}

// this trait is for node operations with no dependencies outside the jdt's ast
// that means no unsafe implicits, no use of Node, no Extractors, etc.
abstract trait ASTNodeSafe extends TreeSafe
{
	// abstract
	val node: ASTNode
	
	def cu: dom.CompilationUnit = node.getRoot.asInstanceOf[dom.CompilationUnit]
	def findEnclosingType: Option[dom.TypeDeclaration] = 
		ancestors flatMap { case x: dom.TypeDeclaration => List(x) ; case _ => Nil } firstOption
	def findEnclosingMethod: Option[dom.MethodDeclaration] =
		ancestors flatMap { case x: dom.MethodDeclaration => List(x) ; case _ => Nil } firstOption
	def findOuterEnclosingMethod: Option[dom.MethodDeclaration] =
		ancestors flatMap { case x: dom.MethodDeclaration => List(x) ; case _ => Nil } lastOption
	def findEnclosingVariable: Option[dom.VariableDeclaration] =
		ancestors flatMap { case x: dom.VariableDeclaration => List(x) ; case _ => Nil } firstOption
	
	def findEnclosingScope: ASTNode = ancestors flatMap { 
		case x: dom.Block => List(x)
		case x: dom.AbstractTypeDeclaration => List(x)
		case _ => Nil
	} head
	
	def fqname: String = node match { case x: dom.Name => x.getFullyQualifiedName ; case _ => "<Unknown>" }
	def pkgName: String = onull(cu.getPackage).map(_.getName.getFullyQualifiedName) | ""
	def pkgSegments: List[String] = pkgName.split('.').toList
	def subPkgs: List[String] = (1 to pkgSegments.size).toList.map(i => pkgSegments.take(i).mkString("."))
	
	// java.lang.Object
	def objectEqualsMB = {
		val objectTypeBinding = cu.getAST.resolveWellKnownType("java.lang.Object")
		val objectMBs = objectTypeBinding.getDeclaredMethods

		objectMBs.find(_.getName == "equals").get			
	}	
				
	// true if a statement is a looping construct
	def isIterationStmt = node match {
		case _:dom.ForStatement | _:dom.EnhancedForStatement | _:dom.WhileStatement | _:dom.DoStatement => true
		case _ => false
	}
	def isSwitchStmt = node match { case _:dom.SwitchStatement => true ; case _ => false }
	def isBreakableStmt = isIterationStmt || isSwitchStmt
		
	def isJumpStmt = node match {
		case _:dom.BreakStatement | _:dom.ReturnStatement | _:dom.ContinueStatement => true
		case _ => false
	}
		
	// if a statement might expand into more than one in the translation
	def isExpandableStmt = node match {
		case _:dom.ForStatement | _:dom.EnhancedForStatement | _:dom.SwitchStatement => true
		case _ => false
	}
	
	// XXX
	def isDeclarationStmt = node match {
		case _:dom.VariableDeclaration => true
		case _:dom.VariableDeclarationStatement => true
		case _ => false
	}
	
	// if this node is under a constructor
	def isInConstructor: Boolean = findEnclosingMethod.map(_.isConstructor) getOrElse false
	def isInAnonDeclaration: Boolean = ancestors.exists { case _: dom.AnonymousClassDeclaration => true ; case _ => false }

	// def getTargetNode(labelMap: LabelMapper): dom.Statement = node match {
	// 	case x: dom.BreakStatement if x.getLabel == null => node.enclosingBreakableStmt
	// 	case x: dom.ContinueStatement if x.getLabel == null => node.enclosingIterationStmt
	// 	case x: dom.BreakStatement => labelMap(x.getLabel.getIdentifier)
	// 	case x: dom.ContinueStatement => labelMap(x.getLabel.getIdentifier)
	// 	case _ => throw new Exception
	// }
		
	def enclosingBreakableStmt: dom.Statement = ancestors.find(_.isBreakableStmt).get.asInstanceOf[dom.Statement]
	def enclosingIterationStmt: dom.Statement = ancestors.find(_.isIterationStmt).get.asInstanceOf[dom.Statement]	
}

trait TreeSafe
{
	val node: ASTNode

	// The JDT's AST doesn't make children as accessible as we'd like
	def parent: ASTNode = node.getParent
	var children: List[ASTNode] = Nil
	def addChild(n: ASTNode) = children = n :: children
	
	// ancestor operations are safe because we go top-down, but descendant operations are not
	def ancestors: List[ASTNode] = if (parent == null) List(node) else node :: parent.ancestors
	def hasAncestor(cond: ASTNode => Boolean): Boolean = findAncestor(cond).isDefined
	def findAncestor(cond: ASTNode => Boolean): Option[ASTNode] = ancestors find cond
}

trait Tree extends TreeSafe
{
	def descendants: List[ASTNode] = List(node) ::: (children flatMap(_.descendants))
	def descendantExprs: List[dom.Expression] = descendants flatMap { case x: dom.Expression => List(x) ; case _ => Nil }
	def descendantStmts: List[dom.Statement] = descendants flatMap { case x: dom.Statement => List(x) ; case _ => Nil }

	// tests if node has ancestor or child matching condition
	def hasDescendant(cond: ASTNode => Boolean): Boolean = findDescendant(cond).isDefined
		
	// finds the first ancestor in the JDT AST meeting a condition
	def findDescendant(cond: ASTNode => Boolean): Option[ASTNode] = descendants find cond
	def findDescendants(cond: ASTNode => Boolean): List[ASTNode] = descendants filter cond
}

class RecordingNode(node: ASTNode) extends Node(node)
{	
	def emitDirect: Emission = throw new Exception
	override def toString: String = node.getClass.getSimpleName
}
