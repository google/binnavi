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
package com.google.security.zynamics.reil.translators;

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.List;


/**
 * Interface for all classes that want to serve as translators that translate native instructions to
 * REIL instructions.
 */
public interface IInstructionTranslator {

  /**
   * Translates a native instruction to REIL instructions.
   * 
   * @param environment A valid translation environment
   * @param instruction The instruction to translate
   * @param instructions A list of instructions where the translated REIL instructions are added to.
   * 
   * @throws InternalTranslationException Thrown if some internal error occurs.
   */
  void translate(ITranslationEnvironment environment, IInstruction instruction,
      List<ReilInstruction> instructions) throws InternalTranslationException;

}
