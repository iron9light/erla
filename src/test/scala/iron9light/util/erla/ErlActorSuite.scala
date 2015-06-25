package iron9light.util.erla

import akka.actor.{Actor, ActorSystem, Props}
import org.scalatest.FunSuite

import scala.async.Async._

/**
 * @author il
 * @version 12/6/11 12:14 AM
 */

class ErlActorSuite extends FunSuite {
  test("smoking") {
    val system = ActorSystem("TestSystem")
    val actor = system.actorOf(Props(new ErlActor {
      def act() = async {
        val number = await(react {
          case i: Int =>
            println("Int:" + i)
            i
          case s: String =>
            println("String:" + s)
            s.toInt
        })

        println("got int: %s" format number)

        val other = await(react {
          case x =>
            println("other:" + x.toString)
            x
        })

        println("got other: %s" format other)
      }(erlaExecutionContext)
    }).withDispatcher("erla"))

    actor ! 2.0
    actor ! "1"

    Thread.sleep(100)
    system.shutdown()
  }

  test("try await") {
    val system = ActorSystem("TestSystem")
    implicit val timeout = system.settings.CreationTimeout
    val actorB = system.actorOf(Props(new Actor {
      def receive = {
        case x =>
          sender !("got it", x)
          println("actor B reply " + x)
      }
    }))

    val actorA = system.actorOf(Props(new ErlActor {
      def act() = async {
        println("actor A started")
        val msg = "hello"
        import akka.pattern.ask
        val replied = await(tryAwait(actorB ? msg))
        println(replied)
        replied match {
          case ("got it", `msg`) =>
            println("works")
          case x =>
            println("wrong: " + x)
        }
      }(erlaExecutionContext)
    }).withDispatcher("erla"))

    Thread.sleep(100)
    system.shutdown()
  }
}