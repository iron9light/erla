package iron9light.erla.akka

import akka.actor.Actor
import dispatch.ErlaDispatchers
import util.continuations.{cpsParam, shift, reset}

/**
 * @author il
 * @version 12/6/11 12:00 AM
 */

trait ErlActor extends Actor {
  self.dispatcher = ErlaDispatchers.globalErlaDispatcher

  def react[T](handler: PartialFunction[Any, T]): T@cpsParam[Unit, Unit] = {
    shift[T, Unit, Unit] {
      fun: (T => Unit) => {
        become(new PartialFunction[Any, Unit] {
          def isDefinedAt(x: Any) = handler.isDefinedAt(x)

          def apply(x: Any) {
            fun(handler(x))
          }
        })
      }
    }
  }

  def act(): Unit@cpsParam[Unit, Unit]

  private object spawn

  def receive = {
    case `spawn` =>
      reset[Unit, Unit] {
        act()
        self.stop()
      }
  }

  override def preStart() {
    super.preStart()
    self ! spawn
  }
}