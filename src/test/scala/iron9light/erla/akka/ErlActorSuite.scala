package iron9light.erla.akka

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.actor.Actor

/**
 * @author il
 * @version 12/6/11 12:14 AM
 */

@RunWith(classOf[JUnitRunner])
class ErlActorSuite extends FunSuite {
  test("smoking") {
    val actor = Actor.actorOf(new ErlActor {
      def act() = {
        val number = react {
          case i: Int =>
            println("Int:" + i)
            i
          case s: String =>
            println("String:" + s)
            s.toInt
        }

        println("got int: %s" format number)

        val other = react {
          case x =>
            println("other:" + x.toString)
            x
        }

        println("got other: %s" format other)
      }
    }).start()

    actor ! 2.0
    actor ! "1"

    Thread.sleep(100)
  }
}