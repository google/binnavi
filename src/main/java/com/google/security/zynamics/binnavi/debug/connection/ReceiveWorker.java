/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.debug.connection;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.helpers.DebugProtocolHelper;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AuthenticationFailedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TerminateReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerClosedUnexpectedlyReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers.AbstractReplyParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers.ParserFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Worker thread class that processes messages from the debug client.
 */
public final class ReceiveWorker implements Runnable {
  /**
   * Flag that indicates whether the shutdown was peaceful or not.
   */
  private boolean peacefulShutdown = false;

  /**
   * Input stream that reads data from the debug client.
   */
  private final ClientReader workerInputStream;

  /**
   * Data is read from the debug client as long as this flag is false.
   */
  private boolean shutDown = false;

  /**
   * Event queue that is used to store the debug events.
   */
  private final LinkedBlockingQueue<DebuggerReply> eventQueue;

  /**
   * Provides the parsers that are used for parsing incoming messages.
   */
  private final ParserFactory parserFactory;

  /**
   * Flag that days whether the receive worker is waiting for an authentication message from the
   * debug client.
   */
  private boolean waitingForAuthentication = true;

  /**
   * Creates a new worker thread object that receives data from the debug client.
   *
   * @param inputStream The input stream that reads data from the debug client.
   * @param eventQueue The event queue where the received debug events are stored.
   */
  public ReceiveWorker(final ClientReader inputStream,
      final LinkedBlockingQueue<DebuggerReply> eventQueue) {
    workerInputStream =
        Preconditions.checkNotNull(inputStream, "IE00743: Input stream can not be null");
    this.eventQueue = Preconditions.checkNotNull(eventQueue, "IE00744: Event queue can't be null");
    parserFactory = new ParserFactory(inputStream);
  }

  /**
   * Determines whether a received message indicates a peaceful shutdown.
   *
   * @param message The message to check.
   *
   * @return True, if the event indicates a peaceful shutdown. False, otherwise.
   */
  private boolean isPeacefulShutdownEvent(final DebuggerReply message) {
    return message.success() && ((message instanceof DetachReply)
        || (message instanceof TerminateReply) || (message instanceof ProcessClosedReply));
  }

  /**
   * Starts the worker thread.
   */
  @Override
  public void run() {
    try {
      while (!shutDown) {
        int messageType = 0;
        int messageId = 0;

        try {
          // Read the type and the ID of the next message
          messageType = (int) DebugProtocolHelper.readDWord(workerInputStream);

          if (waitingForAuthentication) {
            // The first message expected from the debug client is a simple 'NAVI'.

            waitingForAuthentication = false;

            if (messageType == 0x4E415649) // == NAVI ;)
            {
              continue;
            } else {
              eventQueue.put(new AuthenticationFailedReply());
              break;
            }
          }

          messageId = (int) DebugProtocolHelper.readDWord(workerInputStream);
          if (messageType != DebugCommandType.RESP_READ_MEMORY_SUCCESS) {
            NaviLogger.info(String.format("Debug message of type %d %s arrived", messageType,
                DebugCommandType.getMessageName(messageType)));
          }

        } catch (final IOException ex) {
          if (!peacefulShutdown) {
            eventQueue.put(new DebuggerClosedUnexpectedlyReply());
          }

          break;
        }

        final AbstractReplyParser<? extends DebuggerReply> parser =
            parserFactory.getParser(messageType);

        final DebuggerReply message = parser.parse(messageType, messageId);

        eventQueue.add(message);

        if (isPeacefulShutdownEvent(message)) {
          peacefulShutdown = true;
        }
      }
    } catch (final IOException e) {
      NaviLogger.severe("Shutting down receive worker because of an IO exception");

      try {
        eventQueue.put(new DebuggerClosedUnexpectedlyReply());
      } catch (final InterruptedException e1) {
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      }
    } catch (final InterruptedException e) {
      NaviLogger.severe("Shutting down receive worker because of an interrupted exception");

      try {
        eventQueue.put(new DebuggerClosedUnexpectedlyReply());
      } catch (final InterruptedException e1) {
        CUtilityFunctions.logException(e1);
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Shuts down the receiver thread.
   */
  public void shutdown() {
    this.waitingForAuthentication = true;
    this.shutDown = true;
    this.peacefulShutdown = true;
  }
}
