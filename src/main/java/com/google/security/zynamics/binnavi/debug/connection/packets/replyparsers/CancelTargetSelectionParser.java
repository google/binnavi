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
package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.CancelTargetSelectionReply;

/**
 * Parser responsible for parsing replies to Cancel Target Selection requests.
 */
public final class CancelTargetSelectionParser
    extends AbstractReplyParser<CancelTargetSelectionReply> {
  /**
   * Creates a new Cancel Target Selection reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public CancelTargetSelectionParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_CANCEL_TARGET_SELECTION_SUCCESS);
  }

  @Override
  protected CancelTargetSelectionReply parseError(final int packetId) {
    // TODO: There is no proper handling of errors on the side of the
    // client yet.

    throw new IllegalStateException("IE01081: Received invalid reply from the debug client");
  }

  @Override
  public CancelTargetSelectionReply parseSuccess(final int packetId, final int argumentCount) {
    return new CancelTargetSelectionReply(packetId, 0);
  }
}
