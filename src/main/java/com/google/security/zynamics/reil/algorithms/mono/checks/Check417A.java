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
package com.google.security.zynamics.reil.algorithms.mono.checks;

import java.util.Set;

import com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono.StateVector;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1;

public class Check417A {
  private static void assertEquals(final String lhs, final String rhs) {
    if (!lhs.equals(rhs)) {
      throw new IllegalStateException(rhs + " != " + lhs);
    }
  }

  private static <GraphNode> GraphNode getNodes(final Set<GraphNode> nodes, final long l) {
    for (final GraphNode node : nodes) {
      if (((InstructionGraphNode) node).getInstruction().getAddress().toLong() == l) {
        return node;
      }
    }

    throw new IllegalStateException();
  }

  public static <GraphNode, LatticeElement extends ILatticeElementMono1<LatticeElement>> void checkState(
      final int counter, final Set<GraphNode> nodes,
      final StateVector<GraphNode, LatticeElement> state) {
    final Object[][] objects =
        {
            new Object[] {0x100417A00L, "100417A00: [edi -> edi]"},
            new Object[] {0x100417C00L, "100417C00: [t0 -> (esp - 4)]"},
            new Object[] {0x100417C01L, "100417C01: [esp -> (t0 & 4294967295)]"},
            new Object[] {0x100417C02L, "100417C02: [@esp -> ebp]"},
            new Object[] {0x100417D00L, "100417D00: [ebp -> esp]"},
            new Object[] {0x100417F00L, "100417F00: [t0 -> (ebp + 8)]"},
            new Object[] {0x100417F01L, "100417F01: [t1 -> (t0 & 4294967295)]"},
            new Object[] {0x100417F02L, "100417F02: [t2 -> @t1]"},
            new Object[] {0x100417F03L, "100417F03: [eax -> t2]"},
            new Object[] {0x100418200L, "100418200: [t0 -> @eax]"},
            new Object[] {0x100418201L, "100418201: [t2 -> (ecx & 4294901760)]"},
            new Object[] {0x100418202L, "100418202: [ecx -> (t0 | t2)]"},
            new Object[] {0x100418500L, "100418500: [t1 -> (ecx & 65535)]"},
            new Object[] {0x100418501L, "100418501: [t2 -> (t1 & 32768)]"},
            new Object[] {0x100418502L, "100418502: [t3 -> 0]"},
            new Object[] {0x100418503L, "100418503: [t4 -> (t1 - 32)]"},
            new Object[] {0x100418504L, "100418504: [t5 -> (t4 & 32768)]"},
            new Object[] {0x100418505L, "100418505: [SF -> (t5 >> 15)]"},
            new Object[] {0x100418506L, "100418506: [t6 -> (t2 ^ t3)]"},
            new Object[] {0x100418507L, "100418507: [t7 -> (t2 ^ t5)]"},
            new Object[] {0x100418508L, "100418508: [t8 -> (t6 & t7)]"},
            new Object[] {0x100418509L, "100418509: [OF -> (t8 >> 15)]"},
            new Object[] {0x10041850AL, "10041850A: [t9 -> (t4 & 65536)]"},
            new Object[] {0x10041850BL, "10041850B: [CF -> (t9 >> 16)]"},
            new Object[] {0x10041850CL, "10041850C: [t10 -> (t4 & 65535)]"},
            new Object[] {0x10041850DL, "10041850D: [ZF -> (t10 != 0)]"},
            new Object[] {0x100418900L, "100418900: "},
            new Object[] {0x100418B00L, "100418B00: [t1 -> (ecx & 65535)]"},
            new Object[] {0x100418B01L, "100418B01: [t2 -> (t1 & 32768)]"},
            new Object[] {0x100418B02L, "100418B02: [t3 -> 0]"},
            new Object[] {0x100418B03L, "100418B03: [t4 -> (t1 - 9)]"},
            new Object[] {0x100418B04L, "100418B04: [t5 -> (t4 & 32768)]"},
            new Object[] {0x100418B05L, "100418B05: [SF -> (t5 >> 15)]"},
            new Object[] {0x100418B06L, "100418B06: [t6 -> (t2 ^ t3)]"},
            new Object[] {0x100418B07L, "100418B07: [t7 -> (t2 ^ t5)]"},
            new Object[] {0x100418B08L, "100418B08: [t8 -> (t6 & t7)]"},
            new Object[] {0x100418B09L, "100418B09: [OF -> (t8 >> 15)]"},
            new Object[] {0x100418B0AL, "100418B0A: [t9 -> (t4 & 65536)]"},
            new Object[] {0x100418B0BL, "100418B0B: [CF -> (t9 >> 16)]"},
            new Object[] {0x100418B0CL, "100418B0C: [t10 -> (t4 & 65535)]"},
            new Object[] {0x100418B0DL, "100418B0D: [ZF -> (t10 != 0)]"},
            new Object[] {0x100418F00L, "100418F00: [t0 -> (ZF != 0)]"},
            new Object[] {0x100418F01L, "100418F01: "},
            new Object[] {0x100419100L, "100419100: [t0 -> (eax & 2147483648)]"},
            new Object[] {0x100419101L, "100419101: [t1 -> (eax + 1)]"},
            new Object[] {0x100419102L, "100419102: [t2 -> (t1 & 2147483648)]"},
            new Object[] {0x100419103L, "100419103: [SF -> (t2 >> 31)]"},
            new Object[] {0x100419104L, "100419104: [t3 -> (t0 ^ 2147483648)]"},
            new Object[] {0x100419105L, "100419105: [t4 -> (t2 & t3)]"},
            new Object[] {0x100419106L, "100419106: [OF -> (t4 >> 31)]"},
            new Object[] {0x100419107L, "100419107: [t5 -> (t1 & 4294967295)]"},
            new Object[] {0x100419108L, "100419108: [ZF -> (t5 != 0)]"},
            new Object[] {0x100419109L, "100419109: [eax -> t5]"},
            new Object[] {0x100419200L, "100419200: [t0 -> (eax & 2147483648)]"},
            new Object[] {0x100419201L, "100419201: [t1 -> (eax + 1)]"},
            new Object[] {0x100419202L, "100419202: [t2 -> (t1 & 2147483648)]"},
            new Object[] {0x100419203L, "100419203: [SF -> (t2 >> 31)]"},
            new Object[] {0x100419204L, "100419204: [t3 -> (t0 ^ 2147483648)]"},
            new Object[] {0x100419205L, "100419205: [t4 -> (t2 & t3)]"},
            new Object[] {0x100419206L, "100419206: [OF -> (t4 >> 31)]"},
            new Object[] {0x100419207L, "100419207: [t5 -> (t1 & 4294967295)]"},
            new Object[] {0x100419208L, "100419208: [ZF -> (t5 != 0)]"},
            new Object[] {0x100419209L, "100419209: [eax -> t5]"},
            new Object[] {0x100419300L, "100419300: "},
            new Object[] {0x100419500L, "100419500: [t0 -> @esp]"},
            new Object[] {0x100419501L, "100419501: [t1 -> (esp + 4)]"},
            new Object[] {0x100419502L, "100419502: [esp -> (t1 & 4294967295)]"},
            new Object[] {0x100419503L, "100419503: [ebp -> t0]"},
            new Object[] {0x100419600L, "100419600: [t0 -> @esp]"},
            new Object[] {0x100419601L, "100419601: [t1 -> (esp + 4)]"},
            new Object[] {0x100419602L, "100419602: [esp -> (t1 & 4294967295)]"},
            new Object[] {0x100417C00L, "100417C00: [t0 -> (esp - 4)]"},

            new Object[] {0x100417C00L, "100417C00: [edi -> edi][t0 -> (esp - 4)]"},
            new Object[] {0x100417C01L,
                "100417C01: [esp -> ((esp - 4) & 4294967295)][t0 -> (esp - 4)]"},
            new Object[] {0x100417C02L,
                "100417C02: [esp -> (t0 & 4294967295)][@(t0 & 4294967295) -> ebp]"},
            new Object[] {0x100417D00L, "100417D00: [ebp -> esp][@esp -> ebp]"},
            new Object[] {0x100417F00L, "100417F00: [ebp -> esp][t0 -> (esp + 8)]"},
            new Object[] {0x100417F01L,
                "100417F01: [t0 -> (ebp + 8)][t1 -> ((ebp + 8) & 4294967295)]"},
            new Object[] {0x100417F02L,
                "100417F02: [t1 -> (t0 & 4294967295)][t2 -> @(t0 & 4294967295)]"},
            new Object[] {0x100417F03L, "100417F03: [eax -> @t1][t2 -> @t1]"},
            new Object[] {0x100418200L, "100418200: [eax -> t2][t0 -> @eax]"},
            new Object[] {0x100418201L, "100418201: [t2 -> (ecx & 4294901760)]"},
            new Object[] {0x100418202L, "100418202: [ecx -> (t0 | t2)]"},
            new Object[] {0x100418500L, "100418500: [t1 -> (ecx & 65535)]"},
            new Object[] {0x100418501L, "100418501: [t2 -> (t1 & 32768)]"},
            new Object[] {0x100418502L, "100418502: [t3 -> 0]"},
            new Object[] {0x100418503L, "100418503: [t4 -> (t1 - 32)]"},
            new Object[] {0x100418504L, "100418504: [t5 -> (t4 & 32768)]"},
            new Object[] {0x100418505L, "100418505: [SF -> (t5 >> 15)]"},
            new Object[] {0x100418506L, "100418506: [t6 -> (t2 ^ t3)]"},
            new Object[] {0x100418507L, "100418507: [t7 -> (t2 ^ t5)]"},
            new Object[] {0x100418508L, "100418508: [t8 -> (t6 & t7)]"},
            new Object[] {0x100418509L, "100418509: [OF -> (t8 >> 15)]"},
            new Object[] {0x10041850AL, "10041850A: [t9 -> (t4 & 65536)]"},
            new Object[] {0x10041850BL, "10041850B: [CF -> (t9 >> 16)]"},
            new Object[] {0x10041850CL, "10041850C: [t10 -> (t4 & 65535)]"},
            new Object[] {0x10041850DL, "10041850D: [ZF -> (t10 != 0)]"},
            new Object[] {0x100418900L, "100418900: "},
            new Object[] {0x100418B00L, "100418B00: [t1 -> (ecx & 65535)]"},
            new Object[] {0x100418B01L, "100418B01: [t2 -> (t1 & 32768)]"},
            new Object[] {0x100418B02L, "100418B02: [t3 -> 0]"},
            new Object[] {0x100418B03L, "100418B03: [t4 -> (t1 - 9)]"},
            new Object[] {0x100418B04L, "100418B04: [t5 -> (t4 & 32768)]"},
            new Object[] {0x100418B05L, "100418B05: [SF -> (t5 >> 15)]"},
            new Object[] {0x100418B06L, "100418B06: [t6 -> (t2 ^ t3)]"},
            new Object[] {0x100418B07L, "100418B07: [t7 -> (t2 ^ t5)]"},
            new Object[] {0x100418B08L, "100418B08: [t8 -> (t6 & t7)]"},
            new Object[] {0x100418B09L, "100418B09: [OF -> (t8 >> 15)]"},
            new Object[] {0x100418B0AL, "100418B0A: [t9 -> (t4 & 65536)]"},
            new Object[] {0x100418B0BL, "100418B0B: [CF -> (t9 >> 16)]"},
            new Object[] {0x100418B0CL, "100418B0C: [t10 -> (t4 & 65535)]"},
            new Object[] {0x100418B0DL, "100418B0D: [ZF -> (t10 != 0)]"},
            new Object[] {0x100418F00L, "100418F00: [t0 -> (ZF != 0)]"},
            new Object[] {0x100418F01L, "100418F01: "},
            new Object[] {0x100419100L, "100419100: [t0 -> (eax & 2147483648)]"},
            new Object[] {0x100419101L, "100419101: [t1 -> (eax + 1)]"},
            new Object[] {0x100419102L, "100419102: [t2 -> (t1 & 2147483648)]"},
            new Object[] {0x100419103L, "100419103: [SF -> (t2 >> 31)]"},
            new Object[] {0x100419104L, "100419104: [t3 -> (t0 ^ 2147483648)]"},
            new Object[] {0x100419105L, "100419105: [t4 -> (t2 & t3)]"},
            new Object[] {0x100419106L, "100419106: [OF -> (t4 >> 31)]"},
            new Object[] {0x100419107L, "100419107: [t5 -> (t1 & 4294967295)]"},
            new Object[] {0x100419108L, "100419108: [ZF -> (t5 != 0)]"},
            new Object[] {0x100419109L, "100419109: [eax -> t5]"},
            new Object[] {0x100419200L, "100419200: [t0 -> (eax & 2147483648)]"},
            new Object[] {0x100419201L, "100419201: [t1 -> (eax + 1)]"},
            new Object[] {0x100419202L, "100419202: [t2 -> (t1 & 2147483648)]"},
            new Object[] {0x100419203L, "100419203: [SF -> (t2 >> 31)]"},
            new Object[] {0x100419204L, "100419204: [t3 -> (t0 ^ 2147483648)]"},
            new Object[] {0x100419205L, "100419205: [t4 -> (t2 & t3)]"},
            new Object[] {0x100419206L, "100419206: [OF -> (t4 >> 31)]"},
            new Object[] {0x100419207L, "100419207: [t5 -> (t1 & 4294967295)]"},
            new Object[] {0x100419208L, "100419208: [ZF -> (t5 != 0)]"},
            new Object[] {0x100419209L, "100419209: [eax -> t5]"},
            new Object[] {0x100419300L, "100419300: "},
            new Object[] {0x100419500L, "100419500: [t0 -> @esp]"},
            new Object[] {0x100419501L, "100419501: [t1 -> (esp + 4)]"},
            new Object[] {0x100419502L, "100419502: [esp -> (t1 & 4294967295)]"},
            new Object[] {0x100419503L, "100419503: [ebp -> t0]"},
            new Object[] {0x100419600L, "100419600: [t0 -> @esp]"},
            new Object[] {0x100419601L, "100419601: [t1 -> (esp + 4)]"},
            new Object[] {0x100419602L, "100419602: [esp -> (t1 & 4294967295)]"},
            new Object[] {0x100417C00L, "100417C00: [t0 -> (esp - 4)]"},};

    assertEquals((String) objects[counter][1], Long.toHexString((Long) objects[counter][0])
        .toUpperCase()
        + ": "
        + state.getState(getNodes(nodes, (Long) objects[counter][0])).toString());
  }

}
