package iron9light.erla.akka

import util.continuations._
import akka.dispatch.Future
import akka.actor.{Stash, Actor}

/**
 * @author il
 */

trait Erla {this: Actor with Stash =>
  def react[T](hander: PartialFunction[Any, T]): T@suspendable = {
    shift[T, Unit, Unit] {
      cont: (T => Unit) => {
        context.become {
          case msg if hander.isDefinedAt(msg) =>
            unstashAll()
            cont(hander(msg))
          case _ =>
            stash()
        }
      }
    }
  }

  def await[T](future: Future[T]): T@suspendable = {
    val o = new AnyRef
    future.value match {
      case Some(Right(x)) =>
        x
      case Some(Left(e)) =>
        throw e
      case None =>
        future.onComplete {
          x => self !(o, x)
        }
        react {
          case (`o`, Right(x: T)) =>
            () => x
          case (`o`, Left(e: Throwable)) =>
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