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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser responsible for parsing replies to Remove Breakpoint requests.
 */
public final class BreakpointsRemovedParser extends AbstractReplyParser<BreakpointsRemovedReply> {
  /**
   * Creates a new Remove Breakpoint reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public BreakpointsRemovedParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_BP_REM_SUCCESS);
  }

  @Override
  protected BreakpointsRemovedReply parseError(final int packetId) throws IOException {
    final int errorCode = parseInteger();
    return new BreakpointsRemovedReply(packetId, errorCode,
        new ArrayList<Pair<RelocatedAddress, Integer>>());
  }

  @Override
  public BreakpointsRemovedReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final int counter = parseInteger();
    final List<Pair<RelocatedAddress, Integer>> addresses = new ArrayList<>();
    for (int i = 0; i < counter; i++) {
      final RelocatedAddress address = new RelocatedAddress(parseAddress());
      final int error = parseInteger();
      addresses.add(new Pair<RelocatedAddress, Integer>(address, error));
    }
    return new BreakpointsRemovedReply(packetId, 0, addresses);
  }
}
