
import java.util.Enumeration
import java.net.{ JarURLConnection, URL, URLDecoder }
import java.util.jar.JarEntry
import org.eclipse.jdt.core.dom._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

// scala> "(a*).*(b*)".r
// res0: scala.util.matching.Regex = (a*).*(b*)
// 
// scala> "aaaaaaaaafkdjfksldfbgbbbb" match { case res0(x, y) => println(x) ; case _ => println("oops") }
// aaaaaaaaa

object Main
{
  import ReflectionUtil._
  import Util._
  
  val dom = "org.eclipse.jdt.core.dom"
  val comments = new ListBuffer[String]
  var longestName = 0
  val innerClasses = List("PostfixExpression.Operator", "PrefixExpression.Operator",
			  "Assignment.Operator", "PrimitiveType.Code", "Modifier.ModifierKeyword",
			  "InfixExpression.Operator")
  
  val header = """
package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom


##COMMENTS##

trait GenExtractors
{
    // Reduce null suffering
    def Opt[T](x: T): Option[T] = if (x == null) None else Some(x)
"""

  val extractor = """
    object ##NAME## {
##IMPORTS##
        def unapply(node: dom.##NAME##) = {
##PROPERTIES##
        }
    }
  """
  
  def findClassNames(pkg: String): List[String] = {
    val cl = Thread.currentThread.getContextClassLoader
    val path = pkg.replace('.', '/')
    val urls = cl.getResources(path)
    
    def jarEntryToName(entry: JarEntry, path: String): Option[String] = {
      val fileExts = List(".java", ".class")
      val name: String = entry.getName
      if (!name.startsWith(path) || !fileExts.exists(x => name endsWith x)) return None
      
      val relPath = name.drop(path.size + 1)
      if (relPath.contains('/') || relPath.contains('$')) return None

      val snip = if (name endsWith ".class") 6 else 5				// 5 == .java
      Some(name.slice(0, name.length - snip).replace('/', '.'))
    }

    while (urls.hasMoreElements) {
      val pkgURL = urls.nextElement
      if (!(pkgURL.toString contains ".source_")) doJar(pkgURL.openConnection.asInstanceOf[JarURLConnection])
    }

    def doJar(juc: JarURLConnection) = {
      val entries: Enumeration[JarEntry] = juc.getJarFile.entries

      val jarList = enumToList(entries)			
      val names = jarList.flatMap(x => jarEntryToName(x, path))
      val classes: List[Class[_]] = names.flatMap(x => nameToClass(x, "ASTNode"))
      val csAndPs = classes.zip(classes.map(classToPropertyList)).filter(_._2 != None).map(x => (x._1, x._2.get))
      val extractors = new ListBuffer[String]
      longestName = csAndPs.map(_._1).map(trimClassWithDom).map(_.length).sort((x, y) => x > y).head

      // is it over yet?
      for ((c, proplist) <- csAndPs ; if proplist.size > 0) yield {
	extractors += makeExtractor(trimClass(c), proplist)
      }

      // yes I know this won't win any awards, it's all a one-off
      val commentText = comments.map(x => "// " + x).mkString("\n").replace('$', '.')
      
      // unleash the magic!
      print(header.replaceAll("##COMMENTS##", commentText))
      print(extractors.mkString(""))
      println("}")
    }
    
    def cleanId(p: StructuralPropertyDescriptor) = {
      val reserved = List("finally", "package", "type")
      val name = p.getId
      if (reserved contains name) "`" + name + "`" else name
    }
    
    def getter(p: StructuralPropertyDescriptor) = {
      p match {
	case pd: ChildListPropertyDescriptor => cleanId(p)
	case pd: ChildPropertyDescriptor => "get" + p.getId.capitalize
	case pd: SimplePropertyDescriptor => {
            // the special cases olympics
            if (p.getId == "booleanValue") "booleanValue"
            else if (pd.getValueType.getName.toLowerCase == "boolean") "is" + p.getId.capitalize
            else "get" + p.getId.capitalize
          }
      }
    }
    
    def makeExtractor(name: String, props: List[StructuralPropertyDescriptor]): String = {
      val lb = new ListBuffer[String]
      
      // we declare everything partly so our implicit coercions can transform java lists into scala lists
      def makeVal(p: StructuralPropertyDescriptor, c: Class[_], list: Boolean) = {
	val t = if (list) "List[" + trimClassWithDom(c) + "]" else trimClassWithDom(c)
	
	"val " + cleanId(p) + ": " + t + " = node." + getter(p)
      }
      
      lb ++= props.map { 
	case pd: SimplePropertyDescriptor => makeVal(pd, pd.getValueType, false)
	case pd: ChildPropertyDescriptor => makeVal(pd, pd.getChildType, false)
	case pd: ChildListPropertyDescriptor => makeVal(pd, pd.getElementType, true)
      }
      
      // we like pretty documentation
      def nameForComment(name: String) = List.make(longestName - name.length, " ").mkString("") + name + " => "
      
      comments ++= {
	val text = "(" + props.map(commentForProp).reduceLeft(_ + ", " + _) + ")"
	if (name.length + text.length < 80) List(nameForComment(name) + text)		// one line
	else {
	  val fields = text.split(", ").toList
	  val colon = fields.head.indexOf(':')
	  (nameForComment(name) + fields.head) :: fields.tail.map { x =>
	    val index = x.indexOf(':')
            (" " * (longestName + colon + 4 - index)) + x
          }
	}
      }
      
      lb ++= List("", "Some(" + props.map(someExpr).reduceLeft(_ + ", " + _) + ")")
      val proptext = lb.toList.map(x => "            " + x).mkString("\n").replace('$', '.')
      val importtext = innerClasses.filter(_ startsWith name).map(x => "        import " + dom + "." + x + "\n").foldLeft("")(_+_)
      
      extractor.replaceAll("##NAME##", name).
      replaceAll("##PROPERTIES##", proptext).
      replaceAll("""##IMPORTS##\s*\n""", importtext)
    }
    
    def commentForProp(prop: StructuralPropertyDescriptor): String = {
      def commentString(name: String, typeName: String, req: Boolean, list: Boolean) =
	name + ": " + ((req, list) match {
            case (true, false) => typeName
            case (false, false) => "Opt[" + typeName + "]"
            case _ => "List[" + typeName + "]"
          })
      
      return prop match {
	case pd: SimplePropertyDescriptor => 
	  commentString(pd.getId, trimClass(pd.getValueType), pd.isMandatory, false)
	case pd: ChildPropertyDescriptor =>
	  commentString(pd.getId, trimClass(pd.getChildType), pd.isMandatory, false)
	case pd: ChildListPropertyDescriptor =>
	  commentString(pd.getId, trimClass(pd.getElementType), false, true)
      }
    }
    
    // empty lists work for optional list values, but for the non-list-optionals we wrap nulls in Option
    def someExpr(p: StructuralPropertyDescriptor): String = {
      val name = cleanId(p)
      p match {
	case pd: SimplePropertyDescriptor => if (pd.isMandatory) name else "Opt(" + name + ")"
	case pd: ChildPropertyDescriptor => if (pd.isMandatory) name else "Opt(" + name + ")"
	case pd: ChildListPropertyDescriptor => name
      }
    }
    
    Nil
  }
}

object Util
{
  def enumToList[T](enum: java.util.Enumeration[T]): List[T] =
    if (enum.hasMoreElements) enum.nextElement :: enumToList(enum) else Nil
  
  def javaListToList[T](list: java.util.List[T]): List[T] =
    list.toArray.map(_.asInstanceOf[T]).toList

}

object ReflectionUtil
{
  import Util._
  
  def classToPropertyList(clazz: Class[_]): Option[List[StructuralPropertyDescriptor]] = {
    val method = try {
      //clazz.getMethod("propertyDescriptors", Array(classOf[Int]):_*)
      clazz.getMethod("propertyDescriptors", classOf[Int])
    } catch {
      case e: Exception => return None
    }
    
    //val props = method.invoke(clazz, Array(AST.JLS3.asInstanceOf[java.lang.Integer])).
    //asInstanceOf[java.util.List[StructuralPropertyDescriptor]]
    val props = method.invoke(clazz, AST.JLS3.asInstanceOf[Integer]).
    asInstanceOf[java.util.List[StructuralPropertyDescriptor]]
    Some(javaListToList(props))
  }
  
  def superclasses(clazz: Class[_]): List[Class[_]] =
    if (clazz == null) Nil else clazz :: superclasses(clazz.getSuperclass)

  // returns class objects for those that derive from req
  def nameToClass(name: String, req: String): Option[Class[_]] = {
    // eclipse appears to have invalid bytecode for these classes - java verifier chokes
    val exceptions = List("ASTConverter", "ASTParser", "PackageBinding")
    if (exceptions.exists(name contains _)) return None
    
    val clazz = Class.forName(name)
    val supers = superclasses(clazz).map(_.getName)
    return (if (supers.exists(_ endsWith ("." + req))) Some(clazz) else None)			
  }
  
  def trimClassWithDom(c: Class[_]): String = {
    val name: String = c.getName.replace('$', '.')
    
    if ((name startsWith Main.dom) && !name.contains("IDocElement")) name.drop(Main.dom.length - 3)
    else trimClass(c)
  }
  
  def trimClass(c: Class[_]) = trimClassName(c.getName.replace('$', '.'))
  def trimClassName(name: String): String = {
    val segment: String = (if (name contains '.') name.slice(name.lastIndexOf('.') + 1, name.length) else name)

    // in functional programming it's all about generality
    segment match {
      case "boolean" => "Boolean"
      case "int" => "Int"
      case "IDocElement" => "ASTNode"
      case _ => segment
    }
  }	
}

// basically it's a one-liner
Main.findClassNames(Main.dom)

