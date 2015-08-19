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

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockEventListener;
import com.google.security.zynamics.binnavi.debug.connection.AbstractConnection;
import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageRawArgument;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.DebugCommand;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.ConnectException;

@RunWith(JUnit4.class)
public final class CAbstractConnectionTest {
  @Test
  public void reconnect() throws DebugExceptionWrapper {
    // This test makes sure that consecutive runs of the debugger still receive messages

    final DebugCommand command = new DebugCommand(DebugCommandType.RESP_INFO, 0);
    command.addArgument(new DebugMessageRawArgument(
        "<info><registers></registers><size>32</size><options></options></info>".getBytes()));

    final MockDebugger debugger =
        new MockDebugger(new byte[][] {"NAVI".getBytes(), command.toByteArray()});

    final MockEventListener listener = new MockEventListener();

    debugger.addListener(listener);

    debugger.connect();

    debugger.connection.m_reader.next();
    debugger.connection.m_reader.next();

    debugger.close();

    assertEquals("RECEIVED_TARGET_INFORMATION;", listener.events);

    debugger.connect();

    debugger.connection.m_reader.next();
    debugger.connection.m_reader.next();

    debugger.close();

    assertEquals("RECEIVED_TARGET_INFORMATION;RECEIVED_TARGET_INFORMATION;", listener.events);
  }

  private static class FooReader implements ClientReader {
    @Override
    public int available() {
      return 0;
    }

    @Override
    public int read() {
      return 0;
    }

    @Override
    public int read(final byte[] data, final int i, final int length) {
      return 0;
    }
  }

  @SuppressWarnings("unused")
  private static class MockConnection extends AbstractConnection {
    private final ClientReader m_reader = new FooReader();

    private byte[] m_message;

    @Override
    protected int sendPacket(final DebugCommand message) throws IOException {
      m_message = message.toByteArray();

      return 0;
    }

    public byte[] getMessage() {
      return m_message;
    }

    @Override
    public void startConnection() throws ConnectException {
      super.startConnection(m_reader);
    }
  }
}
