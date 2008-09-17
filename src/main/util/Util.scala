package org.improving.scalify

trait Util
{	
	def nullToOption[T >: Null](x: T): Option[T] = if (x == null) None else Some(x)
	
	// Unique id generator for function labels
	object IdGen {
		private val count = new java.util.concurrent.atomic.AtomicInteger(0)
		private val base = "scalifyId"
		
		def get: String = base + count.incrementAndGet
	}

	// given a string full of newlines, removes any prefix common to each embedded line
	def removeCommonPrefix(x: String): String = {		
		def longestCommonPrefix(x: String, y: String): String = 
			(x.toList, y.toList) match {
				case (a :: xrest, b :: yrest) if a == b =>
					a + longestCommonPrefix(xrest.mkString, yrest.mkString)
				case _ => ""
			}

		val lines = x.split("""\n""").filter(!_.matches("""\s*"""))
		val prefix = 
			if (lines.isEmpty) ""
			else lines.reduceLeft(longestCommonPrefix)

		if (prefix.length > 0) lines.map(_.substring(prefix.length)).mkString("\n") else x
	}
	
	def join(xs: List[String], sep: String): String = xs match {
		case Nil => ""
		case x :: Nil => x
		case _ => xs.reduceLeft(_ + sep + _)
	}
	
	// nice parenthesized comma separated list of strings
	def commaSeparatedList(xs: List[String]): String = 
		"(" + (xs.size match {
			case 0 => ""
			case 1 => xs.head
			case _ => xs.reduceLeft(_ + ", " + _)
		}) + ")"
	
	def allUpper(s: String): String = s.toArray.map(_.toUpperCase).mkString("")
	
	// returns filename without path
	def basename(file: String): String = file.replaceAll("""/[^/]*$""", "")	
	
	// comments out a bunch of text
	def commentize(in: String): String = in.replaceAll("""(?m)^""", "// ") + "\n"
	
	// groups a list of pairs based on equality of the first element - returns list of [ (k, [v1, v2, v3]), ... ]
	def groupByKey[T, U](xs: List[(T, U)]): List[(T, List[U])] =
		for (k <- xs.map(_._1).removeDuplicates) yield 
			(k, xs.filter(_._1 == k).map(_._2))
}

object Util extends Util
{
}

object PosetOps
{
	def arrange[T <% PartiallyOrdered[T]](xs: List[T]): List[List[T]] = {
		val (members, heads) = xs.partition { elem =>
			(xs - elem).exists(_ < elem)
		}
		arrange(members, heads.map(x => List(x)))
	}

	def arrange[T <% PartiallyOrdered[T]](xs: List[T], lists: List[List[T]]): List[List[T]] = {
		if (xs.isEmpty) return lists		
		else for (x <- xs ; i <- 0 until lists.size ; if lists(i).forall(y => !(x < y)) && lists(i).exists(_ < x))
			return arrange(xs - x, lists.take(i) ::: List(lists(i) ::: List(x)) ::: lists.drop(i+1))
		
		throw new Exception	// should be unreachable
	}
	
	// sort partially ordered list in some plausible way
	def tsort[T <% PartiallyOrdered[T]](xs: List[T]): List[T] = xs match {
		case Nil => Nil
		case x :: rest => 
			if (rest.forall(o => !(o < x))) x :: tsort(rest)
			else tsort(rest ::: List(x))
	}
}























