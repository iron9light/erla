package iron9light.erla.akka.dispatch

import akka.dispatch.ExecutorBasedEventDrivenDispatcher
import akka.dispatch.Dispatchers._

/**
 * @author il
 * @version 12/5/11 11:57 PM
 */

object ErlaDispatchers {
  object globalErlaDispatcher extends ExecutorBasedEventDrivenDispatcher("erla:global", THROUGHPUT, THROUGHPUT_DEADLINE_TIME_MILLIS, MAILBOX_TYPE) with ErlaDispatcher
}