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

import java.math.BigInteger;

/**
 * Interface for all memory expression binding objects.
 */
public interface IMemoryExpressionBinding {
  /**
   * Returns the value read from memory at a given address.
   *
   * @param address The address from which to read.
   *
   * @return The value read from the memory address.
   *
   * @throws CEvaluationException Thrown if the memory address could not be read.
   */
  BigInteger getValue(BigInteger address) throws CEvaluationException;

  /**
   * Returns the value of a given register.
   *
   * @param register The name of the register.
   *
   * @return The value of the register.
   *
   * @throws CEvaluationException Thrown if the register value could not be determined.
   */
  BigInteger getValue(String register) throws CEvaluationException;
}
