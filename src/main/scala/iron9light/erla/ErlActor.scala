package iron9light.erla

import util.continuations.{cpsParam, shift, reset}
import actors.Actor

/**
 * @author il
 * @version 12/4/11 10:27 PM
 */

trait ErlActor {
  self: Actor =>
  def reactX[T](handler: PartialFunction[Any, T]): T@cpsParam[Unit, Nothing] = {
    shift[T, Unit, Nothing] {
      fun: (T => Unit) => {
        react(new PartialFunction[Any, Unit] {
          def isDefinedAt(x: Any) = handler.isDefinedAt(x)

          def apply(x: Any) {
            fun(handler(x))
          }
        })
      }
    }
  }

  def reactWithinX[T](msec: Long)(handler: PartialFunction[Any, T]): T@cpsParam[Unit, Nothing] = {
    shift[T, Unit, Nothing] {
      fun: (T => Unit) => {
        reactWithin(msec)(new PartialFunction[Any, Unit] {
          def isDefinedAt(x: Any) = handler.isDefinedAt(x)

          def apply(x: Any) {
            fun(handler(x))
          }
        })
      }
    }
  }

  def actX(): Unit@cpsParam[Unit, Nothing]

  def act() {
    reset[Unit, Nothing] {
      actX()
    }
  }
}