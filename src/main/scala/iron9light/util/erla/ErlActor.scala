package iron9light.util.erla

import java.util.concurrent.TimeoutException

import akka.actor.{Stash, UnrestrictedStash}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Try}

/**
 * @author il
 */
private[erla] case class JustRun(action: Runnable)

trait Erla {
  this: UnrestrictedStash =>

  protected val continuationsQueue = collection.mutable.Queue[Runnable]()

  val erlaExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable) = ???

    override def execute(runnable: Runnable) = {
      continuationsQueue.enqueue(runnable)
    }
  }

  protected val justRunExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable) = ???

    override def execute(runnable: Runnable) = {
      self ! JustRun(runnable)
    }
  }

  def react[T](handler: PartialFunction[Any, T]): Future[T] = {
    val promise = Promise[T]()
    context.become {
      case JustRun(action) =>
        action.run()
      case msg if handler.isDefinedAt(msg) =>
        unstashAll()
        promise.complete(Try(handler(msg)))
        continuationsQueue.dequeue().run()
      case _ =>
        stash()
    }
    promise.future
  }

  def tryAwait[T](future: Future[T])(implicit timeout: Duration = Duration.Inf, executor: ExecutionContext = context.dispatcher): Future[T] = {
    val o = new AnyRef
    future.value match {
      case Some(x) =>
        self !(o, x)
      case None =>
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
    }
    val promise = Promise[T]()
    context.become {
      case JustRun(action) =>
        action.run()
      case (`o`, x: Try[T]) =>
        unstashAll()
        promise.complete(x)
        continuationsQueue.dequeue().run()
      case _ =>
        stash()
    }
    promise.future
  }

  def erlAct(act: => Future[Unit]) {
    context.become(Map.empty, discardOld = false)
    act.andThen {
      case _ => context.unbecome()
    }(justRunExecutionContext)
    continuationsQueue.dequeue().run()
  }
}

trait ErlActor extends Stash with Erla {
  private[this] var autoStop = true

  def act(): Future[Unit]

  import ErlActor.Spawn

  self ! Spawn

  def receive = {
    case `Spawn` =>
      unstashAll()
      act().andThen {
        case _ =>
          if (autoStop) context.stop(self)
      }(justRunExecutionContext)
      continuationsQueue.dequeue().run()
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