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
 * Interface for all classes that want to serve as translators for native code to REIL code.
 */
public interface ITranslator<InstructionType extends IInstruction> {

  /**
   * Translates a single native instruction.
   * 
   * @param environment A valid translation environment
   * @param instruction The instruction to translate
   * 
   * @return The generated REIL code
   * 
   * @throws InternalTranslationException Thrown if an internal error occurs
   */
  List<ReilInstruction> translate(ITranslationEnvironment environment, InstructionType instruction,
      List<ITranslationExtension<InstructionType>> extensions) throws InternalTranslationException;

}
