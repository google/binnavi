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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

/**
 * This class is used to display a progress dialog until the echo breakpoints of a trace are
 * removed.
 */
public final class CStopTraceListener extends CEndlessHelperThread {
  /**
   * Debugger used to remove the breakpoints.
   */
  private final IDebugger m_debugger;

  /**
   * Logger which is monitored by the trace listener.
   */
  private final TraceLogger m_logger;

  /**
   * List of addresses removed by the trace logger during its finalization step. Initially this list
   * is null.
   */
  private Set<BreakpointAddress> m_removedAddresses = null;

  /**
   * Flag that determines whether the stop trace process has been completed.
   */
  private boolean m_isDone = false;

  /**
   * Received echo breakpoint removal replies before the listener knows what echo breakpoints were
   * removed by the logger.
   */
  private final List<EchoBreakpointsRemovedReply> m_bufferedReplies =
      new ArrayList<EchoBreakpointsRemovedReply>();

  /**
   * Listener that waits for the reply that signals that the echo breakpoints were removed.
   */
  private final IDebugEventListener m_debuggerListener = new DebugEventListenerAdapter() {
    @Override
    public void debuggerClosed(final int code) {
      m_isDone = true;
    }

    @Override
    public void receivedReply(final EchoBreakpointsRemovedReply reply) {
      if (m_removedAddresses == null) {
        m_bufferedReplies.add(reply);
      } else {
        m_isDone = checkReply(reply);
      }
    }
  };

  /**
   * Creates a new trace listener object.
   *
   * @param debugger Debugger used to remove the breakpoints.
   * @param logger The trace logger whose echo breakpoints are cleared.
   */
  public CStopTraceListener(final IDebugger debugger, final TraceLogger logger) {
    Preconditions.checkNotNull(logger, "IE01565: Logger argument can not be null");

    m_debugger = debugger;
    m_logger = logger;

    debugger.addListener(m_debuggerListener);
  }

  /**
   * Checks whether a given reply is the reply the trace listener has been waiting for.
   *
   * @param reply The reply to check.
   *
   * @return True, if the reply is the one the listener has been waiting for. False, otherwise.
   */
  private boolean checkReply(final EchoBreakpointsRemovedReply reply) {
    final List<Pair<RelocatedAddress, Integer>> receivedAddresses = reply.getAddresses();

    if (receivedAddresses.size() != m_removedAddresses.size()) {
      for (final Pair<RelocatedAddress, Integer> pair : receivedAddresses) {
        final RelocatedAddress receivedAddress = pair.first();

        if (!m_removedAddresses.contains(
            DebuggerHelpers.getBreakpointAddress(m_debugger, receivedAddress))) {
          throw new IllegalStateException(
              "IE00680: the number of breakpoints removed differs from the number of received breakpoints in the debugger reply\n" + "The first breakpoint address missmatch is:" + DebuggerHelpers.getBreakpointAddress(m_debugger, receivedAddress));
        }
      }
    }

    return true;
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    m_removedAddresses = new HashSet<BreakpointAddress>(m_logger.stop());

    for (final EchoBreakpointsRemovedReply reply : m_bufferedReplies) {
      m_isDone |= checkReply(reply);
    }

    while (!m_isDone) {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException exception) {
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      }
    }

    m_debugger.removeListener(m_debuggerListener);
  }
}
