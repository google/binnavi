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
package com.google.security.zynamics.binnavi.API.reil;

import com.google.security.zynamics.binnavi.API.disassembly.Instruction;

/**
 * Exception that is thrown when REIL translation fails.
 */
public final class InternalTranslationException extends Exception {

  /**
   * The instruction that could not be translated.
   */
  private final Instruction m_instruction;

  /**
   * Creates a new exception object.
   *
   * @param exception Cause of the exception.
   * @param instruction The instruction that could not be converted.
   */
  public InternalTranslationException(final Exception exception, final Instruction instruction) {
    super(exception);

    m_instruction = instruction;
  }

  /**
   * Returns the instruction that caused the translation error.
   *
   * @return The instruction that caused the translation error.
   */
  public Instruction getInstruction() {
    return m_instruction;
  }
}
