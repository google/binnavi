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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;

/**
 * Represents the reply sent by the debug client after a List Processes request.
 */
public final class ListProcessesReply extends DebuggerReply {
  /**
   * Information about the remotely running processes.
   */
  private final ProcessList processList;

  /**
   * Creates a new list processes reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param processList Information about the remotely running processes. This argument must be null
   *        in case of errors.
   */
  public ListProcessesReply(final int packetId, final int errorCode,
      final ProcessList processList) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(processList, "IE01062: Process list argument can not be null");
    } else {
      if (processList != null) {
        throw new IllegalArgumentException("IE01063: Process list argument must be null");
      }
    }
    this.processList = processList;
  }

  /**
   * Returns the information about the remotely running processes that was sent by the debug client.
   * This information is null in case of errors.
   *
   * @return Information about the remotely running processes or null.
   */
  public ProcessList getProcessList() {
    return processList;
  }
}
