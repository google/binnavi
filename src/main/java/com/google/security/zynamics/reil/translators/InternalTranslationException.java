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

import com.google.security.zynamics.zylib.disassembly.IInstruction;


/**
 * This class is used for exceptions that are thrown when something goes wrong that's probably a bug
 * in the translator.
 */
public class InternalTranslationException extends Exception {

  /**
   * UID of the class
   */
  private static final long serialVersionUID = -7388260811571010342L;

  /**
   * Message of the exception
   */
  private final String message;

  private IInstruction m_instruction;

  /**
   * Creates a new InternalTranslationException object.
   * 
   * @param message The exception message
   */
  public InternalTranslationException(final String message) {
    this.message = message;
  }

  public IInstruction getInstruction() {
    return m_instruction;
  }

  /**
   * Returns the exception message.
   * 
   * @return The exception message
   */
  @Override
  public String getMessage() {
    return message;
  }

  public void setInstruction(final IInstruction instruction) {
    m_instruction = instruction;
  }
}
