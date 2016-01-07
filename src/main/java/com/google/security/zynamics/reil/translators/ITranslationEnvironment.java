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

import com.google.security.zynamics.reil.OperandSize;


/**
 * Interface for all translation environments that can be used to translate native instructions to
 * REIL instructions.
 */
public interface ITranslationEnvironment {

  /**
   * Returns the index of the next unused REIL register. Afterwards the index is incremented.
   * 
   * @return The index of the next unused REIL register.
   */
  int generateNextVariable();

  /**
   * Returns the size of the standard registers of the source architecture.
   * 
   * @return The size of the standard registers of the source architecture.
   */
  OperandSize getArchitectureSize();

  /**
   * Returns the index of the next unused REIL register.
   * 
   * @return The index of the next unused REIL register.
   */
  int getNextVariable();

  /**
   * Returns the name of the next unused REIL register. Afterwards the index is incremented.
   * 
   * @return The name of the next unused REIL register.
   */
  String getNextVariableString();

  /**
   * This function is called before a new native instruction is translated.
   * 
   */
  void nextInstruction();

  /**
   * Sets the index of the next unused REIL register.
   * 
   * @param nextVariable The index of the next unused REIL register.
   */
  void setNextVariable(int nextVariable);

}
