import scala.io._

// # public void endVisit(NullLiteral node) { // default implementation: do nothing }
// # public boolean visit(ArrayAccess node) { return true; }
// #
// # override def endVisit(node: NullLiteral) = { }
// # override def visit(node: ArrayAccess) = { return true; }

object Hierarchy {                              
  // class, superclass, ?abstract
  def unapply(s: String): Option[(String, String, Boolean)] = {
    val words = s.split("""\s+""")
    (words.indexOf("class"), words.indexOf("extends"), words.contains("abstract")) match {
      case (-1, _, _) => None
      case (_, -1, _) => None
      case (x, y, z) => Some(words(x + 1), words(y + 1), z)
    }
  }		
}

// we bypass the would-have-been-generated-code on these because they're too special
val skips = List("TypeDeclaration", "Type", "Name", "VariableDeclaration")
//val in = Source.fromFile("doc/asthierarchy.txt")
val in = Source.fromPath("doc/asthierarchy.txt")
val lines: List[String] = in.getLines().map(_.trim).toList

print("""
package org.improving.scalify

import org.eclipse.jdt.core.dom
import org.eclipse.jdt.core.dom.ASTNode
import GenWrappers._

trait GenImplicits
{
    protected def get(n: ASTNode): Node = Scalify.lookup(n)
	
""")

val method = """    implicit def enrich##TYPE##(n: dom.##TYPE##): ##TYPE## = get(n).asInstanceOf[##TYPE##]"""
	
lines.foreach { x =>
  x match {
    case Hierarchy(node, supernode, _) if !(skips contains node) && !(skips contains supernode) =>
      println(method.replaceAll("##TYPE##", node))
    case _ =>
      println("    // ??? " + x)
  }
}

println("}")

print("""
object GenFactory
{	
	def apply(n: dom.ASTNode): Node = n match {
""")

// val create = """    def apply(n: dom.##TYPE##): ##TYPE## = new ##TYPE##(n)"""
val create = """        case x: dom.##TYPE## => new ##TYPE##(x)"""
lines.foreach { x =>
  x match {
    case Hierarchy(node, supernode, false) if !(skips contains node) && !(skips contains supernode) =>
      // case Hierarchy(node, supernode, false) =>
      println(create.replaceAll("##TYPE##", node))
    case Hierarchy(node, supernode, true) =>
      println("    // skipping abstract class " + node)
    case _ =>
      println("    // ??? " + x)
  }
}
println("    }")
println("}")

print("""
trait GenWrappers
{	
    import GenFactory._
""")

val wrap = """    class ##TYPE##(override val node: dom.##TYPE##) extends ##SUPERTYPE##(node)"""
lines.foreach { x =>
  x match {
    case Hierarchy(node, supernode, false) if !(skips contains node) && !(skips contains supernode) =>
      val supertype = if (supernode == "ASTNode") "MiscNode" else supernode
      println(wrap.replaceAll("##TYPE##", node).replaceAll("##SUPERTYPE##", supertype))
    case Hierarchy(node, supernode, true) =>
      println("    // skipping abstract class " + node)
    case _ =>
      println("    // ??? " + x)
  }
}
println("""}

object GenWrappers extends GenWrappers
""")



