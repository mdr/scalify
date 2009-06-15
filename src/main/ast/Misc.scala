package org.improving.scalify

import Scalify._
import Global._
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom
// import scalaz.OptionW._
class TypeParameter(override val node: dom.TypeParameter) extends MiscNode(node) with TypeBound
{
	lazy val TypeParameter(name, bounds) = node
	def tb = node.resolveBinding
	
	// TODO: figure out what should be here
	override def emitDirect: Emission = name    // ~ BOUNDLOWER ~ Emit("Null")
}

class PackageDeclaration(override val node: dom.PackageDeclaration) extends MiscNode(node)
{
	lazy val PackageDeclaration(javadoc, annotations, name) = node
	
	override def emitDirect: Emission = PACKAGE ~ name ~ NL ~ NL
	override def toString: String = name.fqname
}

class ImportDeclaration(override val node: dom.ImportDeclaration) extends MiscNode(node)
{
	lazy val ImportDeclaration(static, name, onDemand) = node
	lazy val SimpleName(className) = name.simpleName
	lazy val importPkgName = name match {
		case QualifiedName(q, _) => q.fqname
		case _ => ""
	}
	lazy val alwaysPkgQualify: Boolean = name match {
		case QualifiedName(_, SimpleName(obj)) if typeExists(pkgName + "." + obj) => true
		case _ => false
	}
			
	override def emitDirect: Emission = {
		lazy val default = cu.emitImportDeclaration(name.fqname, onDemand)
		if (alwaysPkgQualify) Nil
		else node match {
			// scala doesn't like it when you pointlessly import classes in your own package
			case ImportDeclaration(_, QualifiedName(q, _), false) if q.fqname == pkgName => Nil
						
			// with factory types we import all the variants just in case			
			case TypeBinding(tb) if tb.isFactoryType => tb.getFactoryType.get.emitImportsWhenSuper
			case _ => default
		}
	}
}

class AnonymousClassDeclaration(override val node: dom.AnonymousClassDeclaration)
extends MiscNode(node) with TypeBound
{
	lazy val AnonymousClassDeclaration(bodyDecls) = node
	def tb = node.resolveBinding
		
	override def emitDirect: Emission = BRACES(REP(bodyDecls))		
}

class Modifier(override val node: dom.Modifier) extends MiscNode(node) with Modifiable
{
	lazy val Modifier(keyword) = node
	def flags = keyword.toFlagValue
	
	override def emitDirect: Emission = emitModifierList
}

class MemberValuePair(override val node: dom.MemberValuePair) extends MiscNode(node)
{
	lazy val MemberValuePair(name, expr) = node
	
	override def emitDirect: Emission = VAL ~ name ~ EQUALS ~ expr
}

class MiscNode(node: ASTNode) extends Node(node)
{    	
	override def emitDirect: Emission = emitDefault
}

class CompilationUnit(override val node: dom.CompilationUnit) extends MiscNode(node)
with HasTypes
{
	import org.eclipse.jdt.core.compiler.IProblem
	import Global._
	
	lazy val CompilationUnit(jpackage, imports, jtypes) = node
	lazy val comments: List[dom.Comment] = node.getCommentList
	lazy val problems: List[IProblem] = node.getProblems
	lazy val stds: List[dom.TypeDeclaration] = jtypes flatMap 
		{ case x: dom.TypeDeclaration => List(x) ; case _ => Nil }
	lazy val icu: ICU = node.getJavaElement match { case x: ICU => x ; case _ => null }
	lazy val failedImports = imports.map(_.snode) flatMap
		{ case x: ImportDeclaration if x.alwaysPkgQualify => List(x) ; case _ => Nil }
	lazy val typeHierarchy = {
		val region = JavaCore.newRegion
		region.add(icu)
		val h = javaProject.newTypeHierarchy(region, null)
		h.getAllTypes.foreach(_.ensureOpen)
		h
	}

	override def emitDirect: Emission = 
		emitOpt(jpackage) ~ 
		REP(imports) ~ emitCommonImports ~ emitFactoryImports ~ NL ~ 
		REP(jtypes)
	
	override def toString: String = icu.getElementName
	
	// if we need to prepend _root_ to something starting with this segment
	// 1) given import a.b.c.Foo in pkg x.y.z check for existence of package x.a, x.y.a, and x.y.z.a
	// 2) if there is a wildcard import p.q.* and p.q.a is a package, we get p.q.a.b.c.Foo instead of a.b.c.Foo.
	def needsRoot(name: String): Boolean = {
		if (name == "") return false
		val segment = name.split('.')(0)

		subPkgs.exists(pkgExists(_, segment)) || imports.exists(pkgExists(_, segment))
	}
	def needsPkg(name: String): Option[String] = failedImports.find(_.className == name).map(_.importPkgName)
	
	// list of imports to insert at top of every file
	// TODO - another list based on the situation
	def emitCommonImports = REP(commonImports.map(x => emitImportDeclaration(x._1, x._2)))

	def emitImportDeclaration(name: String, onDemand: Boolean): Emission = {
		val rootQualifier = if (needsRoot(name)) ROOTPKG <~> DOT ~ NOS else Nil
		val onDemandQualifier = if (onDemand) NOS ~ DOT <~> UNDERSCORE else Nil
		val additional = additionalImports(name, onDemand)

		IMPORT ~ rootQualifier ~ emitString(name) ~ onDemandQualifier ~ NL ~ additional
	}

	// when a wildcard import includes a class which is also in scala.*, the java code
	// will expect the one in the imported package (e.g. java.util.List) but scala does not
	// privilege wildcard imports over scala.*
	private def additionalImports(name: String, onDemand: Boolean): Emission = {
		(name, onDemand) match {
			case ("java.util", true) => emitImportDeclaration("java.util.List", false)
			case _ => Nil
		}
	}
}

