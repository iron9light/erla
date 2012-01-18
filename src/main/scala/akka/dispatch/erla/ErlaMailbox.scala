package akka.dispatch
package erla

import annotation.tailrec
import java.util.{ArrayDeque, Deque}
import java.util.concurrent.ConcurrentLinkedDeque
import com.typesafe.config.Config
import akka.actor._
import iron9light.erla.akka.ErlActor


/**
 * @author il
 */

class ErlaMailbox(config: Config) extends MailboxType {
  override def create(receiver: ActorContext) = {
    if(receiver.self.isInstanceOf[LocalActorRef]) {
      val localActorRef = receiver.self.asInstanceOf[LocalActorRef]

      new Mailbox(receiver.asInstanceOf[ActorCell]) with DequeBasedMessageQueue with UnboundedDequeMessageQueueSemantics with DefaultSystemMessageQueue {
        final val deque: Deque[Envelope] = new ConcurrentLinkedDeque

        final protected val stack = new ArrayDeque[Envelope]

        private[this] lazy val isErla = localActorRef.underlying.actor.isInstanceOf[ErlActor]
        final protected def isDefinedAt(message: Any) = {
          if(isErla) {
            val erlActor = localActorRef.underlying.actor.asInstanceOf[ErlActor]
            erlActor.isDefinedAt(message)
          } else {
            true
          }
        }
      }
    } else {
      UnboundedMailbox().create(receiver)
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
    deque.pollLast() match {
      case null =>
        null
      case handle if handle.message.isInstanceOf[AutoReceivedMessage] =>
        handle
      case handle if isDefinedAt(handle.message) =>
        if (!stack.isEmpty) {
          deque.addAll(stack)
          stack.clear()
        }
        handle
      case handle =>
        stack.addFirst(handle)
        this.dequeue()
    }
  }
  
  protected def isDefinedAt(message: Any): Boolean
}