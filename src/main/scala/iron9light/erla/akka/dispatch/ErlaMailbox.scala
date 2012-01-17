package iron9light.erla.akka.dispatch

import annotation.tailrec
import akka.dispatch._
import java.util.{ArrayDeque, Deque}
import java.util.concurrent.ConcurrentLinkedDeque
import collection.immutable.Stack
import akka.actor.{Actor, ActorRef, ActorContext}
import com.typesafe.config.Config


/**
 * @author il
 */

class ErlaMailbox(config: Config) extends MailboxType {
  override def create(receiver: ActorContext) = {
    try {
      val actorHack = receiver.self.asInstanceOf[ {
        def underlying: {
          def hotswap: Stack[PartialFunction[Any, Unit]]
          def actor: {
            def receive: Actor.Receive
          }
        }
      }].underlying

      new CustomMailbox(receiver) with DequeBasedMessageQueue with UnboundedDequeMessageQueueSemantics with DefaultSystemMessageQueue {
        final val deque: Deque[Envelope] = new ConcurrentLinkedDeque

        final protected val stack = new ArrayDeque[Envelope]

        def hotswap = actorHack.hotswap
        final protected def isDefinedAt(message: Any) = {
          if (hotswap.nonEmpty) {
            hotswap.head.isDefinedAt(message)
          } else {
            actorHack.actor.receive.isDefinedAt(message)
          }
        }
      }
    } catch {
      case _ => UnboundedMailbox().create(receiver)
    }
  }
}

trait DequeBasedMessageQueue extends MessageQueue {
  def deque: Deque[Envelope]

  protected def stack: Deque[Envelope]

  def numberOfMessages = deque.size + stack.size

  def hasMessages = !deque.isEmpty
}

trait UnboundedDequeMessageQueueSemantics extends DequeBasedMessageQueue {
  final def enqueue(receiver: ActorRef, handle: Envelope): Unit = deque.addFirst(handle)

  @tailrec
  final def dequeue(): Envelope = {
    val handle = deque.pollLast()
    if (handle ne null) {
      if (isDefinedAt(handle.message)) {
        if (!stack.isEmpty) {
          deque.addAll(stack)
          stack.clear()
        }
        handle
      } else {
        stack.addFirst(handle)
        this.dequeue()
      }
    } else {
      handle
    }
  }
  
  protected def isDefinedAt(message: Any): Boolean
}