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

import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart;

/**
 * Represents the reply sent by the debug client when the target process is started but the process
 * is not yet running. It contains the thread id of the initial thread as well as the process
 * module.
 */
public class ProcessStartReply extends DebuggerReply {
  /**
   * The process start instance describing the initial process.
   */
  private final ProcessStart processStart;

  /**
   * Creates a new Process Start reply.
   *
   * @param packetId Packet id of the reply.
   * @param errorCode The error code of the reply. If this error code is 0, the operation was
   *        successful.
   */
  public ProcessStartReply(final int packetId, final int errorCode,
      final ProcessStart processStart) {
    super(packetId, errorCode);
    this.processStart = processStart;
  }

  /**
   * Returns the process start object containing all necessary data to handle this reply.
   *
   * @return The corresponding process start object.
   */
  public ProcessStart getProcessStart() {
    return processStart;
  }
}
