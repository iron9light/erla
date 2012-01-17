package iron9light.erla.akka

import util.continuations._
import akka.dispatch.Future
import akka.actor.Actor

/**
 * @author il
 * @version 12/6/11 12:00 AM
 */

trait ErlActor extends Actor {
  def react[T](handler: PartialFunction[Any, T]): T@suspendable = {
    shift[T, Unit, Unit] {
      cont: (T => Unit) => {
        context.become(new PartialFunction[Any, Unit] {
          def isDefinedAt(x: Any) = handler.isDefinedAt(x)

          def apply(x: Any) {
            cont(handler(x))
          }
        })
      }
    }
  }

  def act(): Unit@suspendable

  private object Spawn

  self ! Spawn

  def receive = {
    case `Spawn` =>
      reset[Unit, Unit] {
        act()
        context.stop(self)
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
            x
          case (`o`, Left(e: Throwable)) =>
            throw e
        }
    }
  }
}