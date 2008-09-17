package org.improving.scalify

import scalax.data.{ Positive, Negative }
import scalax.io.CommandLineParser
import java.io.File

object ScalifyOptions extends CommandLineParser {
	val inDir = new NonOption("Input directory with java sources")
	val cpExtras = new StringOption('c', "classpathExtras", "Colon-separated list of additional classpath entries") with AllowAll
	val cwdDir = new StringOption('d', "dir", "Directory to chdir to") with AllowAll
	val outDir = new StringOption('o', "out", "Ouput directory for scala sources") with AllowAll
	val helpFlag = new Flag('h', "help", "Display help information") with AllowAll
	val commentsFlag = new Flag('j', "java-comments", "Include original java source as comment") with AllowAll
	val verboseFlag = new Flag('v', "verbose", "Verbose debugging output") with AllowAll

	val usage = "Usage: scalify <indir> [-jv] [-d basedir] [-o outdir]"
	
    override def helpHeader = """
        | Usage: scalify <indir> [options]
		| where currently documented options are:
        |
        |""".stripMargin

	def scalifyCmd(argv: List[String]): CmdResult = {
		def failSilent(x: Unit): CmdFailure = CmdFailure("")
		def fail(msg: String): CmdFailure = CmdFailure(msg + "\n" + usage)
		
		(parseOrHelp(argv.toArray) { cmd =>
			if (cmd(helpFlag)) failSilent(showHelp(System.err))
			else if (cmd.nonOptions.isEmpty) fail("Input directory is required. ")
			else {
				val inDir = cmd.nonOptions.head
				var cwd : File = if (cmd(cwdDir).isDefined) new File(cmd(cwdDir).get) else new File("")
				var in : File = new File(cwd, inDir)
				var out : File = if (cmd(outDir).isEmpty) in else new File(cwd, cmd(outDir).get)
				val cps : List[File] =
					if (cmd(cpExtras).isEmpty) Nil
					else cmd(cpExtras).get.split(":").toList.map(x => new File(cwd, x))
		
				if (!in.exists || !in.isDirectory) 
					fail("Input path must be readable (" + in.getCanonicalPath + ")")
				else {
					out.mkdirs
					if (!out.isDirectory || !out.canWrite)
						fail("Output path must be writable (" + out.getCanonicalPath + ")")
					else
						CmdOpts(in, out, cps, cmd(commentsFlag), cmd(verboseFlag))
				}				
			}
		}) match {
			case Positive(r) => r
			case Negative(s) => CmdFailure(s)
		}
	}
	
	sealed abstract class CmdResult
	
	case class CmdFailure(
		reason: String
	) extends CmdResult
	
	case class CmdOpts(
		in: File,
		out: File,
		cps: List[File],
		javaComments: Boolean,
		verbose: Boolean
	) extends CmdResult
}