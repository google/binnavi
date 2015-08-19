/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Debug.Connection;

import com.google.security.zynamics.binnavi.debug.connection.AbstractConnection;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.DebugEventListener;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.DebugCommand;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.DebuggerSynchronizer;

public final class MockDebugConnection extends AbstractConnection {
  private byte[][] m_message = new byte[0][0];

  public DebuggerSynchronizer m_synchronizer;

  public CMockReader m_reader;

  public MockDebugConnection(final IDebugger debugger) {
    m_synchronizer = new DebuggerSynchronizer(debugger);
  }

  public MockDebugConnection(final IDebugger debugger, final byte[][] data) {
    m_synchronizer = new DebuggerSynchronizer(debugger);
    m_message = data;
  }

  @Override
  protected int sendPacket(final DebugCommand message) {
    return 0;
  }

  @Override
  public void addEventListener(final DebugEventListener listener) {
    super.addEventListener(listener);

    m_synchronizer = (DebuggerSynchronizer) listener;
  }

  public DebuggerSynchronizer getSynchronizer() {
    return m_synchronizer;
  }

  @Override
  public void startConnection() {
    if (m_message == null) {
      super.startConnection(m_reader = new CMockReader(true));
    } else {
      super.startConnection(m_reader = new CMockReader(m_message));
    }
  }
}
