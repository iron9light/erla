package iron9light.erla.akka

import akka.actor.Actor
import util.continuations.{cpsParam, shift, reset}
import akka.dispatch.Future

/**
 * @author il
 * @version 12/6/11 12:00 AM
 */

trait ErlActor extends Actor {
  // todo: set dispatcher
  // this.context.dispatcher = ErlaDispatcher

  def react[T](handler: PartialFunction[Any, T]): T@cpsParam[Unit, Unit] = {
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

  def act(): Unit@cpsParam[Unit, Unit]

  private object Spawn

  def receive = {
    case `Spawn` =>
      reset[Unit, Unit] {
        act()
        context.stop(self)
      }
  }

  override def preStart() {
    super.preStart()
    self ! Spawn
  }

  def await[T](future: Future[T]): T@cpsParam[Unit, Unit] = {
    val o = new AnyRef
    future.value match {
      case Some(Right(x)) =>
        shift[T, Unit, Unit] {
          cont => cont(x)
        }
      case Some(Left(e)) => throw e
      case None =>
        future.onSuccess {
          case x => self !(o, x)
        } // todo: support onException & onTimeout
        react {
          case (`o`, x: T) => x
        }
    }
  }
}