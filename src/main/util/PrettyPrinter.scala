package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scala.collection.mutable.ListBuffer

class PrettyPrinter(val root: dom.CompilationUnit)
{
	val wstokens = List(NOS, NL)

	// squeeze out redundant pairs
	def removeRedundantPairs(xs: Emission, l: Emit, r: Emit): Emission = {
		import scala.collection.mutable.Stack
		val stack = new Stack[Int]		// indices we've seen lparens (or whatever)
		val pairs = new ListBuffer[(Int, Int)]
		val tokens = new ListBuffer[Emit]
		val toRemove = new ListBuffer[Int]

		(xs: List[Emit]).copyToBuffer(tokens)
	
		def firstNonWS(index: Int, ascending: Boolean): Int = {
			var i = index
			while (wstokens contains tokens(i))
				i += (if (ascending) 1 else -1)

			return i
		}
	
		for (i <- 0 until tokens.size) {
			if (tokens(i) == l) stack.push(i)
			if (tokens(i) == r) {
				val p = stack.pop
				// println(p + "," + i + " => " + tokens(p) + " " + tokens(i))
				pairs += (p, i)
			}
		}
	
		pairs.sort((x, y) => x._1 < y._1).foreach { (x) => 
			val p1 = firstNonWS(x._1 + 1, true)
			val p2 = firstNonWS(x._2 - 1, false)
		
			if (pairs contains (p1, p2)) { 
				for (j <- (x._1 + 1) to p1) toRemove += j
				for (j <- p2 to (x._2 - 1)) toRemove += j
			}	
		}
	
		toRemove.sort((x, y) => x > y).map(i => tokens.remove(i))
		return tokens.toList		
	}

	// def squeezeFurther(xs: Emission): Emission = {
	// 	val list = xs:List[Emit]
	// 	
	// 	(list.indices.tail.map { (i) => 
	// 		(xs(i-1), xs(i)) match {
	// 			case (LBRACE, INDENTSTART) => LBRACE
	// 			case (INDENTSTART, LBRACE) => LBRACE
	// 			case (RBRACE, INDENTEND) => RBRACE
	// 			case (INDENTEND, RBRACE) => RBRACE
	// 			case _ => xs(i-i)
	// 		}
	// 	}) ::: List(list.last)
	// }

	// pretty print scala source
	def pp(xs: Emission): String = {
		val p1 = removeRedundantPairs(xs, LPAREN, RPAREN)
		val p2 = removeRedundantPairs(p1, LBRACE, RBRACE)
		// val p3 = squeezeFurther(p2)
		pp(p2, 0, false, Nil, Nil) mkString
	}

	// pp(xs, 0, false, Nil, Nil) mkString

	val INDENTSIZE = 4
	final def pp(xs: Emission, indent: Int, needWS: Boolean, latest: List[String], oldacc: List[String]): List[String] = {
		// println("pp(" + indent + ") => " + latest.reverse.toString)
		def WS(i: Int): String = " " * INDENTSIZE * i	//  List.make(i * 4, " ").mkString
		def isWS(x: Emit): Boolean = wstokens contains x
		val acc = latest.reverse ::: oldacc
	
		xs match {
			case Nil => return acc.reverse
			case COMMENT(s) :: rest => 
				return pp(rest, indent, needWS, List(s.replaceAll("""(?m)^""", WS(indent) + "// ")), acc)
			// case INDENTSTART :: next :: rest =>
			// 	return pp(INDENTSTART :: rest.dropWhile(isWS), indent, needWS, Nil, acc)
			// case INDENTEND :: next :: rest =>
			// 	return pp(INDENTEND :: rest.dropWhile(isWS), indent, needWS, Nil, acc)
			case _ =>
		}
	
		val ws: List[String] = if (needWS) List(WS(indent)) else Nil

		xs match {
			case RBRACE :: Nil => pp(Nil, 0, false, ws ::: List("\n", "}", "\n"), acc)
			case INDENTSTART :: rest => pp(rest, indent + 1, true, List("\n"), acc)
			case INDENTEND :: rest => pp(rest, indent - 1, true, List("\n"), acc)
			case token :: Nil => pp(Nil, 0, false, ws ::: List(token.s), acc)
			case token :: next :: rest => (token, next) match {
				case (LBRACE, RBRACE)						=> pp(rest, indent, true, List("{", "}", "\n"), acc)
				case (NL, RBRACE)							=> pp(next :: rest, indent, false, Nil, acc)
				case (INDENTEND, _)							=> pp(next :: rest, indent - 1, true, List("IEND", "\n"), acc)
				case (RBRACE, _)							=> pp(next :: rest, indent - 1, true, List("\n", WS(indent - 1), "}", "\n"), acc)
				case (INDENTSTART, _)						=> pp(next :: rest, indent + 1, true, List("ISTART", "\n"), acc)
				case (LBRACE, _)							=> pp(next :: rest, indent + 1, true, ws ::: List("{", "\n"), acc)
				case (NL, _)					=> pp(next :: rest, indent, true, List("\n"), acc)
				case (NOS, NOS)					=> pp(next :: rest, indent, false, ws, acc)
				case (_, NOS)					=> pp(rest, indent, false, ws ::: List(token.s), acc)
				case (NOS, _)					=> pp(next :: rest, indent, false, ws, acc)
				case (_, _)						=> pp(next :: rest, indent, false, ws ::: List(token.s, " "), acc)
			}
			case _ => pp(Nil, 0, false, Nil, Nil)	 // unreachable
		}
	}

	def commentize(in: String, indent: Int): String = {
		if (in == null) "<null>" else in.replaceAll("""(?m)^""", List.make(indent, "  ").mkString + "// ")
	}

	// pretty print the java AST	

	def showTree[T <: AnyRef](node: T, i: Int, children: (T) => List[T], info: (T) => Option[String]): Option[String] = {
		// Some("")
		// println("st...")
		val str: Option[String] = info(node)
		def strPrefix = List.make(i, "  ").mkString + node.getClass.getSimpleName + " => "
		def kidstrs: List[String] = 
			for (c <- children(node)) yield {
				showTree(c, i + 1, children, info).getOrElse("")
			}
		
		Some(strPrefix + str.get + kidstrs.foldLeft("")(_ + "\n" + _))
					
			// children(node).map(c => showTree(c, i + 1, children, info)).flatMap(x => x)
		// 	
		// if (str.isEmpty && kidstrs.isEmpty) None
		// else if (str.isEmpty) return Some(kidstrs.foldLeft("")(_ + "\n" + _))
		// else if (kidstrs.isEmpty) return Some(strPrefix + str.get)
		// else Some(strPrefix + str.get + kidstrs.foldLeft("")(_ + "\n" + _))
	}

	def showTreeAST(node: ASTNode): String = {
		// println("sta...")
		// def children(n: ASTNode): List[ASTNode] = Scalify.lookup(n).children.map(_.node)
		def children(n: ASTNode): List[ASTNode] = n.children		// Scalify.lookup(n).children
		def info(n: ASTNode): Option[String] = Some(n.toString.split("\n").head)
		// {
		// 	val info: List[String] = n.toString.split("""\n.*""", 2)
		// 		// if (n.ppString.isDefined) List(n.ppString.get)
		// 		// else n.toString.split("""\n.*""", 2)
		// 
		// 	val str = info(0)
		// 	// val str = info(0) + 
		// 	// 	(if (info.size == 1 || info(1).matches("""\s*""")) ""
		// 	// 	 else " ... [" + info(1).filter('\n' ==).size + " lines]")
		// 
		// 	Some(str)
		// }
		// 	
		showTree(node, 0, children, info).get + "\n"
	}

	def showTreeBindings(node: ASTNode): String = {
		// println("stb...")
		// def children(n: ASTNode): List[ASTNode] = Scalify.lookup(n).children.map(_.node)
		def children(n: ASTNode): List[ASTNode] = n.children		// Scalify.lookup(n).children
		def info(n: ASTNode): Option[String] = n.ppString
		// 
		// n.snode match {
		// 	case x: Named => Some(x.resolveName)
		// 	case x: TypeBound => Some(x.tb.getQualifiedName)
		// 	case x: Bound => Some(x.jelement.getElementName)
		// 	case _ => None
		// }
		// 
		// 	n match {
		// 		case x: Name => if (x.resolveName != "") Some(x.resolveName) else n.jelement.map(_.getElementName)
		// 		case _ => n.tbinding.map(_.getQualifiedName) orElse n.jelement.map(_.getElementName)
		// 	}
		// }
	
		showTree(node, 0, children, info).get + "\n"
	}

	def trimToOneLine(s: String) = {
		val lines = s.filter('\n' ==).size
		s.replaceAll("""\n*""", "") + (if (lines > 2) (" ...[" + lines + " lines]") else "")
	}
	
	def emit = pp(root.emit)
	def show = showTreeAST(root) 	// + "\n" + showTreeBindings(root)
	
}
