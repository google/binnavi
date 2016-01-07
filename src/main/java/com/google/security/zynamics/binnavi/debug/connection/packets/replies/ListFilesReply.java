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
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFileSystem;

/**
 * Represents the reply sent by the debug client after a List Files request.
 */
public final class ListFilesReply extends DebuggerReply {
  /**
   * Information about the remote file system.
   */
  private final RemoteFileSystem remoteFileSystem;

  /**
   * Creates a new list files reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param fileSystem Information about the remote file system. This argument must be null in case
   *        of errors.
   */
  public ListFilesReply(final int packetId, final int errorCode,
      final RemoteFileSystem fileSystem) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(fileSystem, "IE01060: File system argument can not be null");
    } else {
      if (fileSystem != null) {
        throw new IllegalArgumentException("IE01061: File system argument must be null");
      }
    }

    remoteFileSystem = fileSystem;
  }

  /**
   * Returns the information about the remote file system that was sent by the debug client. This
   * information is null in case of errors.
   *
   * @return Information about the remove file system or null.
   */
  public RemoteFileSystem getFileSystem() {
    return remoteFileSystem;
  }
}
