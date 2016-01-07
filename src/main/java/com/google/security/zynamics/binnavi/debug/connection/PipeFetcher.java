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
import com.google.security.zynamics.binnavi.debug.connection.interfaces.DebugEventListener;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerReply;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Worker thread that handles entries in the event queue.
 */
public final class PipeFetcher implements Runnable {
  /**
   * Event queue that is used to communicate between the worker thread and the fetcher thread.
   */
  private final LinkedBlockingQueue<DebuggerReply> eventQueue;

  /**
   * List of listeners that are notified about incoming debug messages from the debug client.
   */
  private final ListenerProvider<DebugEventListener> listeners = new ListenerProvider<>();

  /**
   * Flag that indicates whether the pipe fetcher should keep running.
   */
  private boolean m_run = true;

  /**
   * Protocol dispatcher instance which is used to perform debugger protocol actions based on the
   * received debugger replies.
   */
  private final ListenerProvider<DebugEventListener> protocolListeners =
      new ListenerProvider<>();

  /**
   * Creates a new pipe fetcher object.
   *
   * @param eventQueue The event queue from which to take the events.
   */
  public PipeFetcher(final LinkedBlockingQueue<DebuggerReply> eventQueue) {
    this.eventQueue = Preconditions.checkNotNull(eventQueue, "IE00742: Event queue can't be null");
  }

  /**
   * Notifies all listeners of an incoming debug event. Afterwards, all protocol listeners are
   * notified of the event. This way, the protocol listeners can safely trigger protocol actions
   * without interfering with the other listeners (e.g. resume the debugger).
   *
   * @param event The debug event that was received from the debug client.
   */
  private void notifyEventListeners(final DebuggerReply event) {
    for (final DebugEventListener listener : listeners) {
      try {
        listener.receivedEvent(event);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    for (final DebugEventListener listener : protocolListeners) {
      try {
        listener.receivedEvent(event);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a new event listener that is notified of incoming debug events from the debug client.
   *
   * @param listener The listener that wants to be notified of debug events.
   */
  public void addEventListener(final DebugEventListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Adds a new event listener that is notified of incoming debug events from the debug client after
   * the events have been processed by the regular event listeners. This allows the protocol
   * listeners to cause changes in the debugger protocol state.
   *
   * @param listener The listener that wants to be notified of debug events.
   */
  public void addProtocolEventListener(final DebugEventListener listener) {
    protocolListeners.addListener(listener);
  }

  /**
   * Removes an event listener from the list of listeners that are notified about incoming debug
   * events.
   *
   * @param listener The listener to remove.
   */
  public void removeEventListener(final DebugEventListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Removes an event listener from the list of protocol event listeners that are notified about
   * incoming debug events.
   *
   * @param listener The listener to remove.
   */
  public void removeProtocolEventListener(final DebugEventListener listener) {
    protocolListeners.removeListener(listener);
  }

  /**
   * Resets the pipe fetcher.
   */
  public void reset() {
    m_run = true;
  }

  /**
   * This method processes entries in the event queue.
   */
  @Override
  public void run() {
    while (m_run) {
      try {
        notifyEventListeners(eventQueue.take());
      } catch (final InterruptedException ie) {
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Stops the pipe fetcher.
   */
  public void shutdown() {
    m_run = false;
  }
}
