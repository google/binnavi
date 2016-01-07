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
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.DebugCommand;
import com.google.security.zynamics.zylib.net.NetHelpers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Connection class that is used to communicate with the debug client via TCP/IP.
 */
public final class DebugConnection extends AbstractConnection {
  /**
   * Socket that is used to connect to the debug client.
   */
  private Socket debugClientSocket = null;

  /**
   * Host address of the debug client.
   */
  private final String debugClientHost;

  /**
   * Port of the debug client.
   */
  private final int debugClientPort;

  /**
   * Creates a new debug connection object.
   *
   * @param host Host of the debug client
   * @param port Port of the debug client
   */
  public DebugConnection(final String host, final int port) {
    debugClientHost = Preconditions.checkNotNull(host, "IE00739: Host can not be null");
    Preconditions.checkArgument(NetHelpers.isValidPort(port), "IE00740: Invalid port");
    debugClientPort = port;
  }

  @Override
  protected int sendPacket(final DebugCommand message) throws IOException {
    debugClientSocket.getOutputStream().write(message.toByteArray());
    debugClientSocket.getOutputStream().flush();
    return message.getPacketId();
  }

  @Override
  public void shutdown() {
    super.shutdown();

    try {
      if (debugClientSocket != null) {
        debugClientSocket.close();
      }
    } catch (final IOException e) {
      // It's probably OK to fail silently here.
      CUtilityFunctions.logException(e);
    }
  }

  @Override
  public void startConnection() throws ConnectException {
    // Attention: Do not move this into the Constructor or some
    // listeners might miss debug events.
    // TODO: The above comment is probably wrong; re-check

    NaviLogger.info("Trying to connect to the debug client via TCP/IP");

    try {
      // Open a connection to the debug client.
      debugClientSocket = new Socket(debugClientHost, debugClientPort);
      super.startConnection(new SocketReader(debugClientSocket));
    } catch (final Exception e) {
      throw new ConnectException(e.getMessage());
    }
  }
}
