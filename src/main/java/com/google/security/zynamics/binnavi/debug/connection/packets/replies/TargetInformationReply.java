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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;

/**
 * Represents the reply sent by the debug client when it tells BinNavi about important aspects of
 * the target process.
 */
public final class TargetInformationReply extends DebuggerReply {
  /**
   * Target information sent in the reply.
   */
  private final TargetInformation targetInformation;

  /**
   * Creates a new target information reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param targetInformation Target information sent in the reply. In case of errors this argument
   *        must be null.
   */
  public TargetInformationReply(final int packetId, final int errorCode,
      final TargetInformation targetInformation) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(targetInformation,
          "IE01053: Target information argument can not be null");
    } else {
      if (targetInformation != null) {
        throw new IllegalArgumentException("IE01074: Target information must be null");
      }
    }

    this.targetInformation = targetInformation;
  }

  /**
   * Returns the target information sent by the debug client.
   *
   * @return Target information sent by the debug client or null in case of errors.
   */
  public TargetInformation getTargetInformation() {
    return targetInformation;
  }
}
