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
package com.google.security.zynamics.reil.translators.x86;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


/**
 * Interface that must be implemented by generators that generate code for the conditions of
 * conditional jumps (JCC) or conditional moves (CMOVCC).
 */
public interface IConditionGenerator {

  /**
   * Generates code for the condition.
   * 
   * @param environment A valid translation environment (cannot be null)
   * @param offset Next usable REIL offset (must be >= 0)
   * @param instructions The condition code is added to this list of instructions
   * 
   * @throws InternalTranslationException if the argument environment is null or the argument offset
   *         is less than 0.
   * 
   * @return The name and size of the register that holds the result of the condition.
   */
  Pair<OperandSize, String> generate(ITranslationEnvironment environment, long offset,
      List<ReilInstruction> instructions) throws InternalTranslationException;

}
