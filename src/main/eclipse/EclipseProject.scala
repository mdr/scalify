package org.improving.scalify.osgi

import ScalifySafe._

// OSGI
import org.osgi.framework._
import org.eclipse.osgi.framework.console._

// JDT
import org.eclipse.jdt.core.{ ICompilationUnit => ICU, _ }
import org.eclipse.jdt.core.dom
import org.eclipse.jdt.internal.core.PackageFragment
import org.eclipse.jdt.core.compiler._

// Non-JDT Eclipse
import org.eclipse.core.resources.{ IFile, IFolder, IProject, IResource, IWorkspace, ResourcesPlugin }
import org.eclipse.core.runtime._
import org.eclipse.core.filesystem._

// Java baggage
import java.io.{ BufferedReader, File, FileWriter, InputStreamReader, PrintWriter, StringWriter }
import java.net.URI

// Scala non-baggage
import scala.io.Source
import scala.collection.immutable
import scala.collection.mutable.HashMap
import net.lag.configgy.Configgy
import net.lag.logging.Logger

class ASTCreator(val eproj: EclipseProject) extends dom.ASTRequestor
{
	private val _asts = new HashMap[ICU, dom.CompilationUnit]
	lazy val asts = immutable.HashMap.empty[ICU, dom.CompilationUnit] ++ _asts
	lazy val cus: List[dom.CompilationUnit] = asts.values.toList
	
	val proj = eproj.javaProject
	val icus = proj.getPackageFragments.flatMap(_.getCompilationUnits)
	val JAVA_LEVEL = dom.AST.JLS3
	
	val parser = dom.ASTParser.newParser(JAVA_LEVEL)
	parser.setProject(proj)
	parser.setResolveBindings(true)
	parser.setBindingsRecovery(true)
	
	// this is where we receive all the asts from the jdt
	override def acceptAST(icu: ICU, cu: dom.CompilationUnit) = _asts += (icu -> cu)
	
	def allSuccess = asts.values.toList.forall(cu => cu.getProblems.forall(problem => !problem.isError))
	def requestASTs() = parser.createASTs(icus, new Array[String](0), this, null)
	def getProblems(icu: ICU): List[String] = asts(icu).getProblems.map(_.getMessage)
	def getAllProblems: List[String] = List.flatten(asts.keys.toList.map(getProblems))
	
	// when I parallelized processing, I started getting java.io.FileNotFoundException leading back to:
	//   val writer = new FileWriter(outPath)
	// I don't know why yet, first stab is to synchronize this method
	def getOutputPath(icu: ICU): String = synchronized {
		val outPath = eproj.out + icu.getPath.removeFirstSegments(2).makeAbsolute.toOSString.replaceAll(".java$", ".scala")
		new File(basename(outPath)).mkdirs	// create any necessary directories
		
		return outPath
	}
}

class ProjectTranslator(val eproj: EclipseProject)
{
	lazy val creator = new ASTCreator(eproj)
	lazy val stats = 
		if (creator.allSuccess) "I parsed " + creator.asts.size + " compilation units. "
		else "Compilation failed because: \n" + creator.getAllProblems.reduceLeft(_ + "\n" + _)
		
	// create an AST for each unit
	def translate: Boolean = {
		creator.requestASTs
		if (!creator.allSuccess) return false
				
		// we do the minimum on the first pass so we needn't deal with the jdt's limitations
		println("Traversing JDT ASTs ... ")
		Forest.initialize(creator.cus)
		println("Analyzing and rewriting  ...")
		Forest.transformByNode(NodeFactory.evaluate(_))
			
		// now we can look up and record all the bindings
		Global.recordMembers(creator.asts)
		Global.javaProject = eproj.javaProject

		println("Resolving namespace conflicts ... ")
		val nodesToRename: List[ASTNode] = Forest.search(Renaming.findCollisions(_))
		println(nodeInfo(nodesToRename))

		for (node <- nodesToRename) 
			Forest.renamer ! RenameNodeMsg(node)
			
		// note any main proxies
		print(proxyInfo)

		true
	}
	
	def emitScala: Unit = {
		Global.logMembers
		PCompute.runAll(creator.cus, doTranslation _)
		
		// copy in the scalify.* files
		
		// unlink the source lest we conflict on a future attempt
		eproj.ifolderIn.delete(true, null)	
	}
		
	private def doTranslation(cu: dom.CompilationUnit) = {
		val icu = getICU(cu)
		val sast = new ScalaAST(Forest.getJDTMap(cu))
		
		// ask our Creator for some icu info
		val outPath = creator.getOutputPath(icu)
		val problemText = creator.getProblems(icu).foldLeft("")(_ + "\n" + _)

	    // output a scala file
		val writer = new FileWriter(outPath)
		writer.write(commentize(icu.getSource))		// original java as comment
		writer.write(commentize(sast.show))			// pretty-printed AST as comment
		writer.write(commentize(problemText))		// jdt reported problems
		writer.write(sast.emit)						// just to be nutty, something useful
		writer.close
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
	
	private def proxyInfo: String = {
		import Scalify._
		
		val mainProxies = creator.cus
			. flatMap(_.jtypes)
			. map(_.snode)
			. flatMap { case x: TypeDeclaration if x.hasMainProxy => List(x) ; case _ => Nil }
		
		if (mainProxies.isEmpty) ""
		else "Inserting proxy classes with main methods since scala can't deal:\n" +			
				mainProxies.map(mp => "  " + mp.pkgName + "." + mp.name + " => " + mp.pkgName + "." + mp.mainProxyName + "\n").mkString
	}
}


// in and out are java source and scala dest directories
class EclipseProject(val in: File, val out: File, classpathExtras: List[File]) {
	def this(in: File, out: File) = this(in, out, Nil)
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206391
	// It appears difficult or impossible to work with eclipse without a project
	// so we tie the in directory to a temporary project
	import org.eclipse.jdt.launching.JavaRuntime
		
	// first create the project
	val workspace: IWorkspace = ResourcesPlugin.getWorkspace
	val projectName = java.util.UUID.randomUUID.toString + "_Scalify"
	val project: IProject = workspace.getRoot.getProject(projectName)
	project.create(null)
	project.open(null)
	
	// oh we must give it a java nature
	val description = project.getDescription
	val natures: List[String] = description.getNatureIds.toList ::: List(JavaCore.NATURE_ID)
	description.setNatureIds(natures.toArray)
	project.setDescription(description, null)
	
	// create project and virtual src folder that in eclipse's mind is the real thing
	val javaProject: IJavaProject = JavaCore.create(project)
	val ifolderIn: IFolder = project.getFolder("src")
	val ipathIn: IPath = new Path(in.getCanonicalPath)
	val status: IStatus = workspace.validateLinkLocation(ifolderIn, ipathIn)
	
	if (status.isOK) {
		ifolderIn.createLink(ipathIn, IResource.NONE, null)
		println("Linked Virtual Source: " + ifolderIn.toString + " to " + ipathIn.toString)
	}
	else {
		// try replacing it
		log.warning("Encountered error linking source: " + status.getMessage)
		log.warning("Trying link replacement...")
		val pm = new NullProgressMonitor {
			var ok = false
			override def done() = ok = true
		}
		ifolderIn.createLink(ipathIn, IResource.REPLACE, pm)
		while(!pm.ok) Thread.sleep(50)		// um, it's a spinlock
	}
	
	println("Compiling java sources ... ")
	val classpathEntries = 
		Array(JavaRuntime.getDefaultJREContainerEntry, JavaCore.newSourceEntry(ifolderIn.getFullPath)) ++
		classpathExtras.map(newLibraryEntry).toArray
		
	javaProject.setRawClasspath(classpathEntries, null)
	
	def packages: Set[String] = immutable.Set(
		(for (pkg <- javaProject.getPackageFragments) yield pkg.getElementName) : _*
	)
	
	def newLibraryEntry(path: File): IClasspathEntry = JavaCore.newLibraryEntry(new Path(path.getCanonicalPath), null, null)
}
