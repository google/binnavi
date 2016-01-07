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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

/**
 * Class that is used to display a progress dialog until all data of a given memory range was
 * received.
 */
public final class CDumpAllWaiter extends CEndlessHelperThread {
  /**
   * Debugger that is waiting for the memory data.
   */
  private final IDebugger m_debugger;

  /**
   * Start offset of the memory section.
   */
  private final IAddress m_offset;

  /**
   * Size of the memory section.
   */
  private final int m_size;

  /**
   * Keeps track of relevant debugger events.
   */
  private final InternalDebuggerListener m_debuggerListener = new InternalDebuggerListener();

  /**
   * Keeps track of relevant process events.
   */
  private final InternalProcessListener m_processListener = new InternalProcessListener();

  /**
   * ID of the packet we are waiting a reply for.
   */
  private int m_packet;

  /**
   * Flag that indicates whether the waiter is done waiting or not.
   */
  private boolean m_finished = false;

  /**
   * Flag that indicates whether waiting was successful or not. This is not the same as
   * {@link #m_finished} because waiting can finish unsuccessfully.
   */
  private boolean m_success = false;

  /**
   * Creates a new waiter object.
   *
   * @param debugger Debugger that is waiting for the memory data.
   * @param offset Start offset of the memory section.
   * @param size Size of the memory section.
   */
  public CDumpAllWaiter(final IDebugger debugger, final IAddress offset, final int size) {
    m_debugger = Preconditions.checkNotNull(debugger, "IE01429: Debugger argument can not be null");
    m_offset = Preconditions.checkNotNull(offset, "IE01430: Offset argument can not be null");

    m_size = size;

    debugger.addListener(m_debuggerListener);
    debugger.getProcessManager().addListener(m_processListener);
  }

  /**
   * Removes all attached listeners.
   */
  private void removeListeners() {
    m_debugger.removeListener(m_debuggerListener);
    m_debugger.getProcessManager().removeListener(m_processListener);
  }

  /**
   * Closes the attached progress dialog.
   */
  private void stopWaiting() {
    m_finished = true;
    removeListeners();
    finish();
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    try {
      m_packet = m_debugger.readMemory(m_offset, m_size);
    } catch (final DebugExceptionWrapper exception) {
      removeListeners();

      throw exception;
    }

    while (!m_finished) {
      Thread.sleep(100);
    }
  }

  @Override
  public void closeRequested() {
    m_success = true;

    stopWaiting();
  }

  /**
   * Returns whether the memory dump operation was successful or not.
   *
   * @return True indicates success; false indicates failure.
   */
  public boolean success() {
    return m_success;
  }

  /**
   * Keeps track of relevant debugger events.
   */
  private class InternalDebuggerListener extends DebugEventListenerAdapter {
    @Override
    public void receivedReply(final ReadMemoryReply reply) {
      if (m_packet == reply.getId()) {
        m_success = reply.success();

        stopWaiting();
      }
    }
  }

  /**
   * Keeps track of relevant process events.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void detached() {
      m_success = true;

      stopWaiting();
    }
  }
}
