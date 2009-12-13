import sbt._
import scala.collection.Set
import scala.xml._
import java.io.{File, FileOutputStream}
import java.nio.channels.Channels
import Process._
import Path.makeString

class ScalifyProject(info: ProjectInfo) extends DefaultProject(info)
{
  override def compileOptions = DisableWarnings :: Deprecation :: Unchecked :: super.compileOptions.toList
  private val javaOpts = "-Xms512M -Xmx1024m -Xss4m"
  private val vmArgs = ""
  
  override def javaCompileOptions = JavaCompileOption(javaOpts) :: super.javaCompileOptions.toList
  override def testJavaCompileOptions = JavaCompileOption("-Xmx256m -Xms64m") :: Nil

  override def deliverScalaDependencies = Nil
  
  //override def parallelExecution = true
  private val scalaVersion = "2.8.0.Beta1-RC2"
  /*override def crossScalaVersions = List(scalaVersion) //"2.8.0.Beta1-RC1", "2.8.0.Beta1-RC2"*/
  def extraResources = "LICENSE.txt" +++ "NOTICE.txt"

  // There is no main class since this is a library
  //override def getMainClass (promptIfMultipleChoices : Boolean) = None
  
  //val felix = "org.apache.felix" % "org.apache.felix.framework" % "1.8.0" intransitive()
  
  /* Eclipse libraries needed */
  val eclipseLibs = List(
    "org.eclipse.ui_.*.jar",
    "org.eclipse.core.runtime_.*.jar",
    "org.eclipse.core.resources_.*.jar",
    "org.eclipse.jdt.core_.*.jar",
    "org.eclipse.jface.text_.*.jar")
  
  // get Path for $ECLIPSE_HOME environment variable 
  def eclipseHome = 
    Path.fromFile(new File(System.getenv("ECLIPSE_HOME"))) / "plugins"
  def eclipsePlugins = descendents(eclipseHome, "*.jar")
  
  // add jars to "unmanaged" path 
  override def unmanagedClasspath = 
    super.unmanagedClasspath +++ eclipsePlugins

  // paulp's output dir
  //override def outputDirectoryName = "target" / "classes"
  
  override def mainScalaSourcePath = "src" / "main"
  //override def mainResourcesPath = "src/resources"
        
  override def testScalaSourcePath = "src" / "test"
  //override def testResourcesPath = "src/resources"

  // codegen --- first phase
  lazy val codeGenAction = task {
    val gens = Map(
      "extractors.scala" -> "GenExtractors.scala",
      "tokens.scala" -> "Tokens.scala",
      "wrappers.scala" -> "GenWrappers.scala")
    val sJars = makeString(FileUtilities.scalaJars map (Path.fromFile(_)))
    val runCP = makeString(runClasspath.get)
    for ((gen,out) <- gens) {
      val genF = "bin" / gen
      val outF = mainScalaSourcePath / "codegen" / out
      if(!outF.exists || (genF.lastModified > outF.lastModified)) {
        log info "Regenerating "+outF+" ..."
        // improve this
        val p = 
          /*  <x>java -classpath {sJars}  
           scala.tools.nsc.MainGenericRunner -classpath {runCP}*/
          <x>scala -nowarn -deprecation -unchecked -classpath {runCP} -howtorun:script 
        {genF.relativePath}</x>
        p #> outF.asFile ! log
      } else {
        log info "Source file "+outF+" is up to date"
      }  
    }
    None
  } describedAs("None")

  // build phase
  override lazy val compileAction = task {
    "jar cMf lib/scalify.jar -C target/scala_2.8.0.Beta1-RC2/classes scalify" ! log
    log info "lib/scalify.jar made"
    None
  } dependsOn(super.compileAction) describedAs("Compile and make scalify.jar")

  lazy val makeJarAction = task {
    "jar cMf lib/scalify.jar -C target/scala_2.8.0.Beta1-RC2/classes scalify" ! log
    None
  } describedAs("Make scalify.jar")  
  
  // osgi --- last phase
  lazy val osgiAction = task {
    bnd+" build "+osgiBnd.relativePath ! log
    log info osgiJar+" made"
    FileUtilities.copyFlat(Set(osgiJar), eclipseHome, log)
    FileUtilities.clean(List("osgi" / "workspace"), true, log)
    var l = Path.fromFile("log")
    if(!l.exists) {
      FileUtilities.createDirectory(l, log)
    }                
    "cp /dev/null log/scalify.log" ! log
    None
  } describedAs("Build OSGI jar")

  private val osgiJar = "osgi" / "org.improving.scalify.osgi.jar"
  private val osgiBnd = "osgi" / "org.improving.scalify.osgi.bnd"
  private val osgiOpts =
    "-jar osgi/org.eclipse.osgi_3.5.0.v20090127-1630.jar -configuration configuration -console"
  private val bnd = "java -jar lib/bnd.jar -exceptions"

  lazy val cleanOsgiAction = task {
    FileUtilities.clean(List("osgi" / "workspace"), true, log)
    FileUtilities.clean(descendents("osgi"/"configuration", "org.*").get, true, log)
    None
  } describedAs("Clean OSGI stuff")
  
  lazy val buildAction = task {
    None
  } dependsOn(codeGenAction, compileAction, osgiAction) describedAs("Does complete build.")

  // 
  //val jarName = jarPathgt 
}
