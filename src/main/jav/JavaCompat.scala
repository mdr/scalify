package scalify

// these are intended to smooth the bumpies - some of them are probably introducing
// gross incorrectness but they'll all be reviewed
object Compat
{
	def abort(msg: String): Nothing = throw new Exception(msg)
	def abort(): Nothing = abort("Unspecified Error")
	
    // see if we can make some scala types act like java types
    implicit def JavaListOps[T](xs: List[T]): JavaListOps[T] = new JavaListOps[T](xs)
    class JavaListOps[T](xs: List[T]) {
        def get(i: Int) = xs(i)
        def subList(from: Int, to: Int): List[T] = xs.slice(from, to)
    }

  // "covariant" arrays
  implicit def covariantArrays[T <: AnyRef, U](xs: Array[T]): Array[U] = xs.asInstanceOf[Array[U]]
  //implicit def cov2[T <: AnyVal, U](xs: Array[T]): Array[U] = xs.map(_.asInstanceOf[U])
  implicit def cov2[T <: AnyVal, U](xs: Array[T]): Array[U] = xs.map(_.asInstanceOf[U]).asInstanceOf[Array[U]]
  // implicit def covariantArrays[T <: AnyRef, U >: T](xs: Array[T]): Array[U] = xs.asInstanceOf[Array[U]]
  // implicit def cov1[T <: AnyVal, U <: AnyVal](xs: Array[T]): Array[U] xs.asInstanceOf[Array[U]]
  // implicit def cov1(xs: Array[Char]): Array[Int] = xs.map(_.toInt)
  // implicit def cov2(xs: Array[Char]): Array[Byte] = xs.map(_.toByte)

	// talk about a problem solver
	implicit def castClassObjects[T, U](x: Class[T]): Class[U] = x.asInstanceOf[Class[U]]

	// i'm sure it'll fit
	implicit def doubleToFloat(x: Double): Float = x.toFloat
	implicit def longToInt(x: Long): Int = x.toInt
	implicit def intToShort(x: Int): Short = x.toShort
	implicit def intToByte(x: Int): Byte = x.toByte
	// implicit def longToDouble(x: Long): Double = x.toDouble
	// implicit def intToDouble(x: Int): Double = x.toDouble
	// implicit def longToFloat(x: Long): Float = x.toFloat
	// implicit def intToFloat(x: Int): Float = x.toFloat

	// don't want to clutter nested return situations
	implicit def unitToContinue(x: Unit): ReturnBox[Nothing] = Void

	// classes for tunnelling returned values through functions
	abstract class ReturnBox[+T] { def value: T }
	case class Return[+T](val value: T) extends ReturnBox[T]
	case object Void extends ReturnBox[Nothing] { def value = abort() }
	case object Break extends ReturnBox[Nothing] { def value = abort() }
	case object Continue extends ReturnBox[Nothing] { def value = abort() }
	case class LabeledBreak(val label: String) extends ReturnBox[Nothing] { def value = abort() }
	case class LabeledContinue(val label: String) extends ReturnBox[Nothing] { def value = abort() }
	    
    // exceptions we use to emulate java control flow
	class ConstructorEarlyReturn extends Exception { }

	// assignments that results in the result: seteqlhs yields the pre-assignment lhs value, for simulating postfix
	def seteq[T](expr: Unit, result: T): T = result
	def seteqlhs[T](result: T, expr: Unit): T = result
	
	// primitives as references since we've lost java's autoboxing
	def asRef[T <: AnyVal](x: T): AnyRef = x.asInstanceOf[AnyRef]
			
	// there seem to be issues with generic methods which return null failing to typecheck
	implicit def anyToNull[T](x: Null): T = x.asInstanceOf[T]
	
	// not sure why we need this
	implicit def noneToAnyOption[T](x: None.type): Option[T] = None:Option[T]
	
	// java.lang.Integer <-> int
	implicit def IntegerToInt(x: java.lang.Integer): Int = x.intValue
	
	/*** LIMBO ***/
		
	// scala Char doesn't have += and -= methods
	// still TODO
	// class IncrementableChar(c: Char) {
	// 	def +=(i: Int): Char = { (c.toInt + 1).toChar }
	// 	def -=(i: Int): Char = { (c.toInt - 1).toChar }
	// }
	// implicit def toIncrementableChar(c: Char): IncrementableChar = { new IncrementableChar(c) }	

	// we want to use java collections in for comprehensions
    // implicit def comprehensibleCollections[T](x: java.util.Collection[T]): List[T] = x.toArray.toList

    // 
    // // java generics become what we expect
    // implicit def compliantLists[T, U](x: List[T]): List[U] = {
    //  x.asInstanceOf[List[U]]
    // }
    // 
    // implicit def compliantSets[T, U](x: Set[T]): Set[U] = {
    //  x.asInstanceOf[Set[U]]
    // }
}
