package iron9light.util.erla

import util.continuations._
import akka.actor.{Stash, Actor}
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author il
 */
trait Erla {
  this: Actor with Stash =>
  def react[T](handler: PartialFunction[Any, T]): T@suspendable = {
    shift[T, Unit, Unit] {
      cont: (T => Unit) => {
        context.become {
          case msg if handler.isDefinedAt(msg) =>
            unstashAll()
            cont(handler(msg))
          case _ =>
            stash()
        }
      }
    }
  }

  def await[T](future: Future[T]): T@suspendable = {
    val o = new AnyRef
    future.value match {
      case Some(Success(x)) =>
        x
      case Some(Failure(e)) =>
        throw e
      case None =>
        future.onComplete {
          x => self ! (o, x)
        }(this.context.dispatcher)
        react {
          case (`o`, Success(x: T)) =>
            () => x
          case (`o`, Failure(e)) =>
            () => throw e
        }.apply()
    }
  }

  def erlAct(act: => Unit@suspendable) {
    reset[Unit, Unit] {
      context.become(Map.empty, false)
      act
      context.unbecome()
    }
  }
}

trait ErlActor extends Actor with Stash with Erla {
  private[this] var autoStop = true

  def act(): Unit@suspendable

  private object Spawn

  self ! Spawn

  def receive = {
    case `Spawn` =>
      reset[Unit, Unit] {
        unstashAll()
        act()
        if (autoStop) context.stop(self)
      }
    case _ =>
      stash()
  }

  def serve(handler: Receive) {
    autoStop = false
    context.become(handler)
  }
}