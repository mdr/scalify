package org.improving.scalify

import scala.collection.immutable
import scala.collection.mutable.HashMap
import scala.actors._
import scala.actors.Actor._

case object AllActorsComplete

// run a bunch of computations in parallel and wait for all to complete, then return a
// map from the input values to the outputs
object PCompute
{	
	implicit def muToIm[A, B](x: HashMap[A, B]): immutable.Map[A, B] = immutable.HashMap.empty[A, B] ++ x
	
	def runAll[T, U](xs: List[T], f: (T) => U): immutable.Map[T, U] = {
		print("Accumulating results for " + xs.size + " calculations ... ")
		
		val manager = new Manager(xs, f)
		manager.start()
		manager !? AllActorsComplete		// blocks until all come back

		println("done.")
		manager.getResults.getOrElse(throw new Exception)
	}
	
	class Manager[T, U](xs: List[T], f: (T) => U) extends Actor {
		case class OneActorResult(input: T, output: U)
		
		private var remaining = xs.size
		val results = new HashMap[T, U]
		def getResults(): Option[immutable.Map[T, U]] = if (remaining != 0) None else Some(results)

		private def mkActor(x: T) = actor {
			this ! OneActorResult(x, f(x))
			exit()
		}

		def act() = {
			xs.foreach(mkActor)

			while(true) {
				receive {
					case OneActorResult(in, out) =>
						remaining -= 1
						results += in -> out
						// print(".")
					case AllActorsComplete if remaining == 0 => reply(AllActorsComplete)
						reply(AllActorsComplete)
						exit()
				}
			}
		}
	}
}