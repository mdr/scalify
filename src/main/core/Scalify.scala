package org.improving.scalify

// FATAL 	the server is about to exit
// CRITICAL 	something happened that is so bad that someone should probably be paged
// ERROR 	an error occurred that may be limited in scope, but was user-visible
// WARNING 	a coder may want to be notified, but the error was probably not user-visible
// INFO 	normal informational logging
// DEBUG 	coder-level debugging information
// TRACE 	intensive debugging information

// Scala's auto-imports:
//
// * the package java.lang,
// * the package scala,
// * and the object scala.Predef.
//
// Shadowings that we know of that will break java expectations:
//   scala.Math
//
// In Java,
//   new a.b.c.d(e)		means new class a.b.c, no constructor arguments, call method d
// In Scala,
//   new a.b.c.d(e)		means object a.b.c has a type d that takes constructor argument e
//
// Java needs to go from a.b.c.d(e) to a.b.c().d(e)

import org.eclipse.jdt.core. { IType, IField, IMethod, ILocalVariable }
import org.eclipse.jdt.core.dom
import net.lag.configgy.Configgy
import net.lag.logging.Logger
// import scalaz.OptionW._
trait Control
{
	val log: Logger
	
	// Exceptional conditions
	def abort(msg: String): Nothing = {
		log.fatal(msg)
		throw new Exception(msg)
	}
	def abort(): Nothing = abort("Unspecified Error")
	
	case class ScalifyConsistencyException(msg: String) extends Exception(msg)
}

object Admin
extends Control
{
	// config and logging
	Configgy.configure("../conf/scalify.conf")	
	val log = Logger.get
	log.debug("Hello scalify")
}

trait ScalifyCommon
extends Tokens
with Control
with Constants
with JavaTypes
with JavaModel
with Util { }

object ScalifySafe
extends ScalifyCommon
with SafeImplicits
{
	val log = Admin.log
	implicit def safeNodeEnrichment(x: ASTNode) = new ASTNodeSafe { val node = x }
}

object Scalify
extends ScalifyCommon
with Emissions 
with GenImplicits 
with Implicits 
with UnsafeExtractors
with GenWrappers 
{	
	import Global._
	val log = Admin.log
	
	implicit def optionsyntax[T](x: Option[T]) = new {
	  def |(orElse: => T): T = x getOrElse orElse
  }

	// this is the path for all ASTNode => enriched node upgrades
	def lookup(node: ASTNode): Node = Forest.get(node)
	
	// declaring node for java element
	def declaration(itype: IType): Option[dom.AbstractTypeDeclaration] = onull(itype).flatMap(x => lookupType(x.getKey))
	def declaration(imethod: IMethod): Option[dom.MethodDeclaration] = onull(imethod).flatMap(x => lookupMethod(x.getKey))
	def declaration(ifield: IField): Option[dom.VariableDeclarationFragment] =
		onull(ifield).flatMap(x => lookupVar(x.getKey)) match
			{ case Some(x: dom.VariableDeclarationFragment) => Some(x) ; case _ => None }	
	def declaration(ilocalvar: ILocalVariable, vb: VBinding): Option[dom.VariableDeclaration] = lookupVar(vb.getKey)		    
}

