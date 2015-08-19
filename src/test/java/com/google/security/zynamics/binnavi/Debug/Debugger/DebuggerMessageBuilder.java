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
package com.google.security.zynamics.binnavi.Debug.Debugger;


import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.RegisterValuesParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;

public final class DebuggerMessageBuilder {
  @SuppressWarnings("unchecked")
  public static BreakpointSetReply buildBreakpointSuccess(final RelocatedAddress address) {
    return new BreakpointSetReply(0, 0, Lists.newArrayList(new Pair<RelocatedAddress, Integer>(
        address, 0)));
  }

  public static EchoBreakpointHitReply buildEchoBreakpointHit(final RelocatedAddress address)
      throws MessageParserException {
    return new EchoBreakpointHitReply(
        0,
        0,
        CommonTestObjects.THREAD_ID,
        RegisterValuesParser
            .parse(String
                .format(
                    "<Registers><Thread id=\"123\"><Register name=\"EAX\" value=\"123\" memory=\"\" /><Register name=\"EBX\" value=\"456\" memory=\"\" /><Register name=\"EIP\" value=\"%s\" memory=\"\" pc=\"true\" /></Thread></Registers>",
                    address.getAddress().toString()).getBytes()));
  }

  @SuppressWarnings("unchecked")
  public static EchoBreakpointsRemovedReply buildEchoBreakpointRemoveSucc(
      final RelocatedAddress address) {
    return new EchoBreakpointsRemovedReply(0, 0,
        Lists.newArrayList(new Pair<RelocatedAddress, Integer>(address, 0)));
  }

  public static ProcessStartReply buildProcessStartReply(final MemoryModule module) {
    final TargetProcessThread thread = new TargetProcessThread(CommonTestObjects.THREAD_ID, ThreadState.RUNNING);
    return new ProcessStartReply(0, 0, new ProcessStart(thread, module));
  }

  public static BreakpointHitReply buildRegularBreakpointHit(final RelocatedAddress address)
      throws MessageParserException {
    return new BreakpointHitReply(
        0,
        0,
        CommonTestObjects.THREAD_ID,
        RegisterValuesParser
            .parse(String
                .format(
                    "<Registers><Thread id=\"%d\"><Register name=\"EAX\" value=\"123\" memory=\"\" /><Register name=\"EBX\" value=\"456\" memory=\"\" /><Register name=\"EIP\" value=\"%s\" memory=\"\" pc=\"true\" /></Thread></Registers>",
                    CommonTestObjects.THREAD_ID, address.getAddress().toBigInteger().toString(16))
                .getBytes()));
  }

  @SuppressWarnings("unchecked")
  public static BreakpointsRemovedReply buildRegularBreakpointRemoveSucc(
      final RelocatedAddress address) {
    return new BreakpointsRemovedReply(0, 0,
        Lists.newArrayList(new Pair<RelocatedAddress, Integer>(address, 0)));
  }

  @SuppressWarnings("unchecked")
  public static BreakpointSetReply buildRegularBreakpointSetError(final RelocatedAddress address) {
    return new BreakpointSetReply(0, 0, Lists.newArrayList(new Pair<RelocatedAddress, Integer>(
        address, 1)));
  }
}
