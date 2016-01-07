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
package com.google.security.zynamics.reil;

import com.google.security.zynamics.zylib.disassembly.IBlockContainer;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a program that was translated to REIL code.
 */
public class ReilProgram<InstructionType extends IInstruction> {

  /**
   * A mapping between original disassembled functions and their corresponding REIL functions.
   */
  private final List<Pair<IBlockContainer<InstructionType>, ReilFunction>> functions =
      new ArrayList<Pair<IBlockContainer<InstructionType>, ReilFunction>>();

  /**
   * Adds a new function to the list of translated functions.
   * 
   * @param original The original function
   * @param translated The translated function that corresponds to the original instruction
   */
  public void addFunction(final IBlockContainer<InstructionType> original,
      final ReilFunction translated) {
    functions.add(new Pair<IBlockContainer<InstructionType>, ReilFunction>(original, translated));
  }

  /**
   * Returns the list of translated functions.
   * 
   * @return The list of translated functions
   */
  public List<Pair<IBlockContainer<InstructionType>, ReilFunction>> getTranslatedFunctions() {
    return new ArrayList<Pair<IBlockContainer<InstructionType>, ReilFunction>>(functions);
  }
}
