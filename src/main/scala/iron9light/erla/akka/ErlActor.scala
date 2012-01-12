package iron9light.erla.akka

import akka.actor.Actor
import dispatch.ErlaDispatchers
import util.continuations._
import akka.dispatch.Future

/**
 * @author il
 * @version 12/6/11 12:00 AM
 */

trait ErlActor extends Actor {
  self.dispatcher = ErlaDispatchers.globalErlaDispatcher

  def react[T](handler: PartialFunction[Any, T]): T@suspendable = {
    shift[T, Unit, Unit] {
      cont: (T => Unit) => {
        become(new PartialFunction[Any, Unit] {
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

  def receive = {
    case `Spawn` =>
      reset[Unit, Unit] {
        act()
        self.stop()
      }
  }

  override def preStart() {
    super.preStart()
    self ! Spawn
  }

  def await[T](future: Future[T]): T@suspendable = {
    val o = new AnyRef
    future.value match {
      case Some(Right(x)) =>
        x
      case Some(Left(e)) =>
        throw e
      case None =>
        future.onResult {
          case x => self !(o, x)
        } // todo: support onException & onTimeout
        react {
          case (`o`, x: T) => x
        }
    }
  }
}