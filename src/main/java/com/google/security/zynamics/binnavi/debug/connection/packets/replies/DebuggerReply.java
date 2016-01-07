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
package com.google.security.zynamics.binnavi.debug.connection.packets.replies;

/**
 * Base class of all debugger replies.
 */
public class DebuggerReply {
  /**
   * Packet ID of the reply.
   */
  private final int packetId;

  /**
   * Error code of the reply.
   */
  private final int errorCode;

  /**
   * Creates a new debugger reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply.
   */
  protected DebuggerReply(final int packetId, final int errorCode) {
    this.packetId = packetId;
    this.errorCode = errorCode;
  }

  /**
   * Returns the error code of the reply.
   *
   * @return The error code of the reply.
   */
  public final int getErrorCode() {
    return errorCode;
  }

  /**
   * Returns the packet ID of the reply.
   *
   * @return The packet ID of the reply.
   */
  public final int getId() {
    return packetId;
  }

  /**
   * Indicates whether the reply is a success reply or an error reply.
   *
   * @return True, if the reply is a success reply. False, if it is not.
   */
  public final boolean success() {
    return errorCode == 0;
  }
}
