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
package com.google.security.zynamics.reil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.interpreter.ReilRegister;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

public class TestHelpers {
  public static final String SUB_PC_REGISTER = "sub_PC";

  public static HashMap<BigInteger, List<ReilInstruction>> createMapping(
      final ArrayList<ReilInstruction> instructions) {
    final HashMap<BigInteger, List<ReilInstruction>> out =
        new HashMap<BigInteger, List<ReilInstruction>>();

    out.put(BigInteger.valueOf(instructions.get(0).getAddress().toLong()), instructions);

    return out;
  }

  public static List<ReilRegister> filterNativeRegisters(final List<ReilRegister> registers) {
    return CollectionHelpers.filter(registers, new ICollectionFilter<ReilRegister>() {
      @Override
      public boolean qualifies(final ReilRegister register) {
        return (register.getRegister().startsWith("t") == false)
            && (register.getRegister().equals(SUB_PC_REGISTER) == false);
      }
    });
  }
}
