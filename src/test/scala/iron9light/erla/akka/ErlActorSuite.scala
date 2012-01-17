package iron9light.erla.akka

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.actor.{Props, ActorSystem, Actor}

/**
 * @author il
 * @version 12/6/11 12:14 AM
 */

@RunWith(classOf[JUnitRunner])
class ErlActorSuite extends FunSuite {
  test("smoking") {
    val system = ActorSystem("TestSystem")
    val actor = system.actorOf(Props(new ErlActor {
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
    }).withDispatcher("erla"))

    actor ! 2.0
    actor ! "1"

    Thread.sleep(100)
    system.shutdown()
  }

  test("try await") {
    val system = ActorSystem("TestSystem")
    implicit val timeout = system.settings.ActorTimeout
    val actorB = system.actorOf(Props(new Actor{
      def receive = {
        case x =>
          sender ! ("got it", x)
          println("actor B reply " + x)
      }
    }))

    val actorA = system.actorOf(Props(new ErlActor {
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
    }).withDispatcher("erla"))

    Thread.sleep(100)
    system.shutdown()
  }
}