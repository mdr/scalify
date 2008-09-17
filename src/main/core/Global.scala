package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom
import scala.collection.mutable.{ HashMap, HashSet }
import scala.collection.immutable
import scalaz.OptionW._

object Global {
	private var members: Members = _
	var javaProject: IJavaProject = _
	
	def setMembers(m: Members) = members = m
	def types = members.types
	def methods = members.methods
	def vars = members.vars
	def packages = members.packages
	def creations = members.creations
	
	class Members(
		val packages: immutable.Map[String, dom.PackageDeclaration],
		val types: immutable.Map[String, dom.AbstractTypeDeclaration],
		val methods: immutable.Map[String, dom.MethodDeclaration],
		val vars: immutable.Map[String, dom.VariableDeclaration],
		val creations: immutable.Set[String]
	)

	def pkgExists(pkg: String, s: String) = packages.contains(pkg + "." + s)
	def pkgExists(s: String) = packages contains s
	def pkgExists(imp: dom.ImportDeclaration, s: String) = {
		val ImportDeclaration(static, name, onDemand) = imp
		onDemand && packages.contains(name.getFullyQualifiedName + "." + s)
	}
	def typeExists(fqname: String) = {
		log.trace("typeExists? %s", fqname)
		types.values.exists {
			case std: dom.TypeDeclaration => std.itype.map(_.getFullyQualifiedName == fqname) | false
			case _ => false
		}		
	}
	
	def lookup(key: String): Option[ASTNode] = 
		types.get(key) orElse methods.get(key) orElse vars.get(key)
	def lookupType(key: String): Option[dom.TypeDeclaration] = types.get(key) match {
		case Some(x: dom.TypeDeclaration) => Some(x)
		case _ => None
	}
	def lookupMethod(key: String): Option[dom.MethodDeclaration] = methods.get(key)
	def lookupVar(key: String): Option[dom.VariableDeclaration] = vars.get(key)
	def lookupPackage(key: String): Option[dom.PackageDeclaration] = packages.get(key)
	def lookupCreation(key: String): Boolean = creations contains key
	
	def logMembers = {
		def log = Scalify.log
		log.trace("Logging %d packages: ", packages.toList.size)
		for ((k, v) <- packages) 
			log.trace("%s => %s", k, v.getName)
		log.trace("Logging %d types: ", types.keys.toList.size)
		for ((k, v) <- types) {
			log.trace("%s => %s", k, v.getName)
			log.trace("%s is in package: %s", v.getName, v.resolveBinding.getPackage.getName)
		}
		log.trace("Logging %d methods: ", methods.keys.toList.size)
		for ((k, v) <- methods) 
			log.trace(k + " => " + v.getName)		
		log.trace("Logging %d variables: ", vars.keys.toList.size)
		for ((k, v) <- vars) 
			log.trace(k + " => " + v.getName)
	}
	
	def recordMembers(asts: Map[ICU, dom.CompilationUnit]) = {
		val packages = new HashMap[String, dom.PackageDeclaration]
		val types = new HashMap[String, dom.AbstractTypeDeclaration]
		val methods = new HashMap[String, dom.MethodDeclaration]
		val vars = new HashMap[String, dom.VariableDeclaration]
		val creations = new HashSet[String]
						
		for ((icu, cu) <- asts) {			
			cu.descendants.foreach {
				case x: dom.PackageDeclaration => packages(x.getName.getFullyQualifiedName) = x
				case x: dom.AbstractTypeDeclaration => types(x.tb.getKey) = x
				case x: dom.MethodDeclaration => methods(x.mb.getKey) = x
				case x: dom.VariableDeclaration => vars(x.vb.getKey) = x
				case x: dom.ClassInstanceCreation => creations += x.getType.resolveBinding.getKey
				case _ =>
			}
		}
		
		Global.setMembers(new Global.Members(packages, types, methods, vars, creations))
	}
}
