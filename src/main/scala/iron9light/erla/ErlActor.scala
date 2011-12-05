package iron9light.erla

import util.continuations.{cpsParam, shift, reset}
import actors.{CanReply, Actor}
import java.lang.Object

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

  def await[T](future: Responder[T]): T@cpsParam[Unit, Nothing] = {
    val o = new Object
    future.respond(x => this ! (o, x)) // todo: It not work
    reactX {
      case (`o`, x: T) => x
    }
  }

  class CanReplyAssoc[T, R](val canReply: CanReply[T, R]) {
    def !!!(msg: T): T@cpsParam[Unit, Nothing] = {
      val o = new Object
      canReply.!!(msg, {
        case x => self ! (o, x)
      })
      reactX {
        case (`o`, x: T) => x
      }
    }
  }

  implicit def convert[T, R](canReply: CanReply[T, R]) = new CanReplyAssoc(canReply)
}