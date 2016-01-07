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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;

/**
 * Represents the reply sent by the debug client when registers values were requested by
 * com.google.security.zynamics.binnavi.
 */
public final class RegistersReply extends DebuggerReply {
  /**
   * The register values read from the target process.
   */
  private final RegisterValues values;

  /**
   * Creates a new register values reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param values The register values read from the target process. In case of errors this argument
   *        must be null.
   */
  public RegistersReply(final int packetId, final int errorCode, final RegisterValues values) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(values, "IE01070: Register values argument can not be null");
    } else {
      if (values != null) {
        throw new IllegalArgumentException("IE01071: Register values argument must be null");
      }
    }
    this.values = values;
  }

  /**
   * Returns the register values sent by the debug client. In case of errors this method returns
   * null.
   *
   * @return The register values sent by the debug client or null.
   */
  public RegisterValues getRegisterValues() {
    return values;
  }
}
