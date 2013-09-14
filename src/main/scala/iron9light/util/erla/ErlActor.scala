package iron9light.util.erla

import util.continuations._
import akka.actor.{UnrestrictedStash, Stash, Actor}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Failure, Success}
import scala.concurrent.duration.{FiniteDuration, Duration}
import java.util.concurrent.TimeoutException

/**
 * @author il
 */
trait Erla {
  this: Actor with UnrestrictedStash =>
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

  def tryAwait[T](future: Future[T])(implicit timeout: Duration = Duration.Inf, executor: ExecutionContext = context.dispatcher): Try[T]@suspendable = {
    future.value match {
      case Some(x) =>
        x
      case None =>
        val o = new AnyRef
        timeout match {
          case finiteTimeout: FiniteDuration =>
            val f = context.system.scheduler.scheduleOnce(
              finiteTimeout,
              self,
              (o, Failure(new TimeoutException("await timeout")))
            )
            future.onComplete {
              x =>
                if (f.cancel()) {
                  self !(o, x)
                }
            }
          case _: Duration.Infinite =>
            future.onComplete {
              x =>
                self !(o, x)
            }
        }
        react {
          case (`o`, x: Try[T]) =>
            x
        }
    }
  }

  def await[T](future: Future[T])(implicit timeout: Duration = Duration.Inf, executor: ExecutionContext = context.dispatcher): T@suspendable = {
    tryAwait(future) match {
      case Success(x) =>
        x
      case Failure(e) =>
        throw e
    }
  }

  def erlAct(act: => Any@suspendable) {
    reset[Unit, Unit] {
      context.become(Map.empty, discardOld = false)
      act
      context.unbecome()
    }
  }
}

trait ErlActor extends Stash with Erla {
  private[this] var autoStop = true

  def act(): Unit@suspendable

  import ErlActor.Spawn

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

object ErlActor {

  private object Spawn

}