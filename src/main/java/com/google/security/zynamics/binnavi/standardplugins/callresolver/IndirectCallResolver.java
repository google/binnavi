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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import java.math.BigInteger;
import java.util.List;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Module;


/**
 * Contains a helper function that helps resolve functions.
 */
public final class IndirectCallResolver {
  /**
   * Searches for an indirect call given the relocated call address.
   * 
   * @param debugger The debugger that provides the relocation information.
   * @param indirectCallAddresses The list of indirect call addresses to search through.
   * @param callAddress The relocated call address to find.
   * 
   * @return The found indirect call object.
   */
  public static IndirectCall findIndirectCall(final Debugger debugger,
      final List<IndirectCall> indirectCallAddresses, final BigInteger callAddress) {
    for (final IndirectCall indirectCall : indirectCallAddresses) {
      final Module module = indirectCall.getModule();
      final Address address = indirectCall.getAddress();

      final Address rebasedAddress = debugger.toImagebase(module, address);

      if (rebasedAddress.equals(new Address(callAddress))) {
        return indirectCall;
      }
    }

    return null;
  }

}
