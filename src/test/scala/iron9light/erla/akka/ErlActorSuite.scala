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

  test("try await") {
    val actorB = Actor.actorOf(new Actor{
      def receive = {
        case x =>
          self.reply(("got it", x))
          println("actor B reply " + x)
      }
    }).start()

    val actorA = Actor.actorOf(new ErlActor {
      def act() = {
        println("actor A started")
        val msg = "hello"
        val replied = await(actorB ? msg)
        println(replied)
        replied match {
          case ("got it", `msg`) =>
            println("works")
          case x =>
            println("wrong: " + x)
        }
      }
    }).start()

    Thread.sleep(100)
    actorB.stop()
  }
}