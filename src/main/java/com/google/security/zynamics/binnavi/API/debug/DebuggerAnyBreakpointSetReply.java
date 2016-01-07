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
package com.google.security.zynamics.binnavi.API.debug;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AnyBreakpointRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AnyBreakpointSetReply;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;

public class DebuggerAnyBreakpointSetReply extends DebuggerReply {

  public DebuggerAnyBreakpointSetReply(final AnyBreakpointSetReply reply) {
    super(reply);
  }

  public List<Pair<Address, Integer>> getAddresses() {
    final List<Pair<Address, Integer>> addresses = Lists.newArrayList();
    for (final Pair<RelocatedAddress, Integer> address : ((AnyBreakpointRemovedReply) reply)
        .getAddresses()) {
      addresses.add(new Pair<Address, Integer>(new Address(address.first().getAddress()
          .toBigInteger()), address.second()));
    }
    return addresses;
  }

}
