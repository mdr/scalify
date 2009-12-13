package org.improving.scalify.osgi

import org.osgi.framework._
import org.eclipse.osgi.framework.console._
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core._

import java.io.{ File, FileWriter }
import scala.collection.mutable.HashMap
import net.lag.logging

abstract trait Translator { def translate(in: File, out: File, cps: List[File]): String }

class TranslatorImpl extends Translator 
{	
	def translate(in: File, out: File, cps: List[File]): String = {
		println("Reading java files in " + in + " and outputting to  " + out)
			
		val eproj = new EclipseProject(in, out, cps)
		val tranny = new ProjectTranslator(eproj)
		if (!tranny.translate) return tranny.stats
		
		tranny.emitScala
		return tranny.stats
	}
}

class Activator
extends BundleActivator
with CommandProvider
{
  import org.improving.scalify.{ Scalify, ScalifyOptions }
  import ScalifyOptions._
  import Scalify._
	
	val TRANS				= classOf[Translator].getName
	val CMD					= classOf[CommandProvider].getName
	val serviceRegs 		= new HashMap[String, ServiceRegistration]
	var bc: BundleContext	= null
	lazy val runDir = new File(System.getProperty("user.dir"))
	
    def start(bc: BundleContext) = {
		this.bc = bc
		serviceRegs += (TRANS -> bc.registerService(TRANS, new TranslatorImpl, null))
		serviceRegs += (CMD -> bc.registerService(CMD, this, null))
		println("Scalify service started")
	}
	
	def stop(bc: BundleContext) = println("Scalify service terminated")
	
	
	private def translationSupport(out: File, cps: List[File]) = {
		val scalifyJar = new File(runDir, "../lib/scalify.jar")
		Runtime.getRuntime.exec(Array("jar", "xf", scalifyJar.getCanonicalPath), null, out)
		val rake = new FileWriter(out.getCanonicalPath + "/Rakefile")
		val classpath = if (cps.isEmpty) "" else "-cp " + cps.map(_.getCanonicalPath).reduceLeft(_ + ":" + _) + ":."
		rake.write("""
task :default do
  sh "fsc ##CP## `find . -name '*.scala'`"
end
""".replaceAll("##CP##", classpath))

		rake.close
	}
	
	def getHelp = {
		"---Translating Source Code---\n" + 
		"\tscalify - translates java source into scala\n" +
		"\tscademo - runs scalify demo on oro source\n" +
		"\tscabug - does debuggy things\n"
	}
	
	def getTranny = bc.getService(serviceRegs(TRANS).getReference).asInstanceOf[Translator]
		
	def _scalify(ci: CommandInterpreter): Unit = {
		def getArgs(xs: List[String]): List[String] = ci.nextArgument match {
			case null => xs.reverse
			case arg => getArgs(arg :: xs)
		}

		val argv = getArgs(Nil)
		scalifyCmd(argv) match {
			case CmdFailure(msg) => println(msg)
			case CmdOpts(in, out, cps, javaComments, verbose) =>
				if (verbose) Scalify.log.setLevel(logging.Level.TRACE)
				
				println("Translating files from " + in + " into " + out)
				if (!cps.isEmpty)
					println("Classpath additions: " + cps.mkString(":"))
				
				val result = getTranny.translate(in, out, cps)
				translationSupport(out, cps)
				println("\nResult: " + result)
		}
	}
	
	def _scabug(ci: CommandInterpreter): Unit = {
		val wspace = ResourcesPlugin.getWorkspace
		println("wspace description: " + wspace.getDescription)
		
		val root = wspace.getRoot
		println("root workspace: " + root.getName)
		
		val jModel: IJavaModel = JavaCore.create(root)
		println("IJavaModel: " + jModel.toString)
				
		val oprojs = jModel.getNonJavaResources
		println(oprojs.size + " non-java projects: " + oprojs.map(_.getClass.getName).foldLeft("")(_ + "," + _))
		
		val projs = jModel.getJavaProjects
		println(projs.size + " java projects")
		
		for (p <- projs) {
			println("Java project " + p.getElementName + " has " + p.getRawClasspath.size + " raw classpath segments:")
			for (cpe <- p.getRawClasspath) yield {
				println("  " + cpe.getPath.toOSString)
			}
			println("Resolved segments:")
			for (cpe <- p.getResolvedClasspath(false)) yield {
				println("  " + cpe.getPath.toOSString)
			}
			println("Resources in parent project:")
			for (m <- p.getProject.members) yield {
				println("  " + m.getName + " (type " + m.getType + ")")
			}
		}
	}
	
	def _scademo(ci: CommandInterpreter): Unit = {
		println("Running scalify demo ... ")
		val in = new File(runDir, "../demo/oro/src/java")
		val out = new File(runDir, "../demo/oroScala")
		
		val result = getTranny.translate(in, out, Nil)
		translationSupport(out, Nil)
		println(result)
		println("If all went well, your scala files are in: " + out.getCanonicalPath)
	}
}

