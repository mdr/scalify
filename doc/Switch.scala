object Switch
{
	// def withResource[A](f : Resource => A) : A = {
	//     val r = getResource()  // Replace with the code to acquire the resource
	//     try {
	//         f(r)
	//     } finally {
	//         r.dispose()
	//     }
	// }
	
	abstract class ReturnBox[+T] { def value: T }
	case class Return[+T](val value: T) extends ReturnBox[T]
	case object Void extends ReturnBox[Nothing] { def value = throw new Exception("Oops") }
	case object Break extends ReturnBox[Nothing] { def value = throw new Exception("Oops ") }
	case object Continue extends ReturnBox[Nothing] { def value = throw new Exception("Oops") }
		
	def apply(x: A)(cases: Case[A,B]*) = {
		val default = cases.find(_.isDefault)
		
		cases match {
			case Nil => Break
			case c :: rest if x != c.value => apply(x)(rest)
			case c :: rest => c(x) match {
				case ret: Return[B] => ret
				case Break => Break
				case _ => apply(x)(rest)	// fallthrough
			}
		}
	}
	
	case class Case[A, B](val value: A, f: ReturnBox[B], next: Option[Case[A, B]]) {
		val isDefault = false
		implicit def unitToContinue(x: Unit): ReturnBox[B] = Continue
		def apply(x: A): ReturnBox[B] = f(x)
	}
	case class DefaultCase[A, B](f: ReturnBox[B], next: Option[Case[A, B]]) extends Case(x: Any => true, f, next) {
		override val isDefault = true
	}
}

class Test
{
	import Switch._
	
	def go(x: String): String = {
		Switch(
			Case { x: String => println(x) },
			Case { x: String => println(x + x) }
		)
		
		Case {  }
	}
}