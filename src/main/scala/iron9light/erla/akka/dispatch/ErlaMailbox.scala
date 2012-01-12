package iron9light.erla.akka.dispatch

import annotation.tailrec
import akka.dispatch._
import java.util.{ArrayDeque, Deque}
import akka.actor.{ActorRef, ActorContext}
import java.util.concurrent.ConcurrentLinkedDeque


/**
 * @author il
 */

case class ErlaMailbox() extends MailboxType {
  override def create(receiver: ActorContext) = new CustomMailbox(receiver) with DequeBasedMessageQueue with UnboundedDequeMessageQueueSemantics with DefaultSystemMessageQueue {
    final val deque: Deque[Envelope] = new ConcurrentLinkedDeque

    final protected val stack = new ArrayDeque[Envelope]
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
      if (false) { // todo: if can handle it
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
}