package iron9light.erla.akka.dispatch

import akka.actor.ActorRef
import akka.dispatch._
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * @author il
 * @version 12/5/11 11:51 PM
 */

trait ErlaDispatcher {self: ExecutorBasedEventDrivenDispatcher =>
  override def createMailbox(actorRef: ActorRef): AnyRef = mailboxType match {
    case b: UnboundedMailbox ⇒
      new ConcurrentLinkedDeque[MessageInvocation] with ErlaMailbox with MessageQueue with ExecutableMailbox {
        @inline
        final def dispatcher = self
      }
    case b: BoundedMailbox ⇒
//      new DefaultBoundedMessageQueue(b.capacity, b.pushTimeOut) with ExecutableMailbox {
//        @inline
//        final def dispatcher = self
//      }
      throw new UnsupportedOperationException("Not implemented.")
  }

}