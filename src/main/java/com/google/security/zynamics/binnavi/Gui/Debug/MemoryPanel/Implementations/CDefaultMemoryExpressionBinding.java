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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations;

import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.zylib.general.ByteHelpers;
import com.google.security.zynamics.zylib.general.memmanager.Memory;

import java.math.BigInteger;



/**
 * Default variable bindings used to evaluate memory expressions.
 */
public class CDefaultMemoryExpressionBinding implements IMemoryExpressionBinding {
  /**
   * Thread that provides the register values.
   */
  private final TargetProcessThread m_thread;

  /**
   * Memory on which memory access expressions are evaluated.
   */
  private final Memory m_memory;

  /**
   * Creates a new default binding object.
   *
   * @param thread Thread that provides the register values.
   * @param memory Memory on which memory access expressions are evaluated.
   */
  public CDefaultMemoryExpressionBinding(final TargetProcessThread thread, final Memory memory) {
    m_thread = thread;
    m_memory = memory;
  }

  @Override
  public BigInteger getValue(final BigInteger address) throws CEvaluationException {
    if (m_memory.hasData(address.longValue(), 4)) {
      return BigInteger.valueOf(ByteHelpers.readDwordLittleEndian(m_memory.getData(
          address.longValue(), 4), 0));
    } else {
      throw new CEvaluationException(String.format(
          "Unknown memory address %s", address.toString(16)));
    }
  }

  @Override
  public BigInteger getValue(final String register) throws CEvaluationException {
    for (final RegisterValue r : m_thread.getRegisterValues()) {
      if (r.getName().equalsIgnoreCase(register)) {
        return r.getValue();
      }
    }

    throw new CEvaluationException(String.format("Unknown register name %s", register));
  }
}
