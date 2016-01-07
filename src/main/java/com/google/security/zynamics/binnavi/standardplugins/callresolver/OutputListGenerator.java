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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.security.zynamics.binnavi.API.debug.MemoryModule;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Function;


/**
 * Generates the text that shows the resolved function calls in the dialog.
 */
public final class OutputListGenerator {
  /**
   * Sorts a set of resolved function calls by address of the source call.
   * 
   * @param entries The list to sort.
   * 
   * @return The sorted list.
   */
  private static List<Entry<BigInteger, Set<ResolvedFunction>>> sort(
      final Set<Entry<BigInteger, Set<ResolvedFunction>>> entries) {
    final ArrayList<Entry<BigInteger, Set<ResolvedFunction>>> entryList =
        new ArrayList<Entry<BigInteger, Set<ResolvedFunction>>>(entries);

    Collections.sort(entryList, new Comparator<Entry<BigInteger, Set<ResolvedFunction>>>() {
      @Override
      public int compare(final Entry<BigInteger, Set<ResolvedFunction>> lhs,
          final Entry<BigInteger, Set<ResolvedFunction>> rhs) {
        return lhs.getKey().compareTo(rhs.getKey());
      }
    });

    return entryList;
  }

  /**
   * Generates a string that shows the resolved functions.
   * 
   * @param resolvedAddresses The function resolver result.
   * 
   * @return The string that shows the resolved functions.
   */
  public static String generate(final Map<BigInteger, Set<ResolvedFunction>> resolvedAddresses) {
    assert resolvedAddresses != null;

    final StringBuffer buffer = new StringBuffer();

    buffer.append("Resolved the following indirect calls:\n");

    for (final Entry<BigInteger, Set<ResolvedFunction>> element : sort(resolvedAddresses.entrySet())) {
      final BigInteger start = element.getKey();
      final Set<ResolvedFunction> targets = element.getValue();

      buffer.append(String.format("%08X ->\n", start.longValue()));

      for (final ResolvedFunction target : targets) {
        if (target.getFunction() != null) {
          final Function function = target.getFunction();

          final Address functionAddress = function.getAddress();
          final String functionName = function.getModule().getName() + "!" + function.getName();

          buffer.append(String.format("  %08X (%s)\n", functionAddress.toLong(), functionName));
        } else if (target.getMemoryModule() != null) {
          final MemoryModule module = target.getMemoryModule();

          final Address functionAddress = target.getAddress();
          final String functionName = module.getName() + "!???";

          buffer.append(String.format("  %08X (%s)\n", functionAddress.toLong(), functionName));
        } else {
          final Address address = target.getAddress();

          buffer.append(String
              .format("  %s (%s)\n", address.toHexString().toUpperCase(), "???!???"));
        }
      }
    }

    return buffer.toString();
  }
}
