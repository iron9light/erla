package iron9light.erla.scala

import org.scalatest.FunSuite
import actors.Actor
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ErlActorSuite extends FunSuite {
  test("smoking") {
    val actor = new Actor with ErlActor {
      def actX() = {
        val number = reactX {
          case i: Int =>
            println("Int:" + i)
            i
          case s: String =>
            println("String:" + s)
            s.toInt
        }

        println("got int: %s" format number)

        val other = reactX {
          case x =>
            println("other:" + x.toString)
            x
        }

        println("got other: %s" format other)
      }
    }.start()

    actor ! 2.0
    actor ! "1"

    Thread.sleep(100)
  }

  ignore("actor version") {
    val actor = new Actor {
      def fun1(number: Int) {
        println("got int: %s" format number)

        react {
          case other =>
            println("got other: %s" format other)
        }
      }

      def act() {
        react {
          case i: Int =>
            println("Int:" + i)
            fun1(i)
          case s: String =>
            println("String:" + s)
            fun1(s.toInt)
        }
      }
    }.start()

    actor ! 2.0
    actor ! "1"

    Thread.sleep(100)
  }

  test("try await") {
    import Actor._
    val actorB = actor{
      println("actor B started")
      react{
        case x =>
          reply(("got it", x))
          println("actor B reply " + x)
      }
    }

    val actorA = new Actor with ErlActor {
      def actX() = {
        println("actor A started")
        val msg = "hello"
        val replied = await(actorB !! msg)
        println(replied)
        replied match {
          case ("got it", `msg`) =>
            println("works")
          case x =>
            println("wrong: " + x)
        }
      }
    }.start()

    Thread.sleep(100)
  }

  test("try !!!") {
    import Actor._
    val actorB = actor{
      println("actor B started")
      react{
        case x =>
          reply(("got it", x))
          println("actor B reply " + x)
      }
    }

    val actorA = new Actor with ErlActor {
      def actX() = {
        println("actor A started")
        val msg = "hello"
        val replied = actorB !!! msg
        println(replied)
        replied match {
          case ("got it", `msg`) =>
            println("works")
          case x =>
            println("wrong: " + x)
        }
      }
    }.start()

    Thread.sleep(100)
  }
}
