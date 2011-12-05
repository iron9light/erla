package iron9light.erla.akka.dispatch

import akka.dispatch.{MessageInvocation, MessageQueue}
import annotation.tailrec
import java.util.{ArrayDeque, Deque}


/**
 * @author il
 * @version 12/5/11 6:49 PM
 */

trait ErlaMailbox extends Deque[MessageInvocation] {self: Deque[MessageInvocation] with MessageQueue =>
  val stack = new ArrayDeque[MessageInvocation]

  @inline
  final def enqueue(m: MessageInvocation) = this.addFirst(m)

  @inline
  @tailrec
  final def dequeue(): MessageInvocation = {
    val message = this.pollLast()
    if (message ne null) {
      if(message.receiver.isDefinedAt(message.message)) {
        if(!stack.isEmpty) {
          this.addAll(stack)
          stack.clear()
        }
        message
      } else {
        stack.addFirst(message)
        this.dequeue()
      }
    } else {
      message
    }
  }
}