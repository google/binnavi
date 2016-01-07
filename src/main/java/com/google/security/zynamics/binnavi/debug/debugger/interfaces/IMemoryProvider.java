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
package com.google.security.zynamics.binnavi.debug.debugger.interfaces;

import java.math.BigInteger;

/**
 * Interface that must be implemented by all objects that want to provide memory data from the
 * target process.
 */
public interface IMemoryProvider {
  /**
   * Checks whether memory data is available.
   *
   * @param offset Start address of the memory chunk to check.
   * @param size Number of bytes to check for.
   * @return True, if the given memory range is available. False, otherwise.
   */
  boolean hasData(BigInteger offset, int size);

  /**
   * Checks whether memory data is available.
   *
   * @param offset Start address of the memory chunk to check.
   * @param size Number of bytes to check for.
   * @param rangeOffset Start address of the adjusted memory chunk.
   * @param rangeSize Number of bytes of the adjusted memory chunk.
   *
   * @return True, if the given memory range is available. False, otherwise.
   */
  boolean hasData(BigInteger offset, int size, BigInteger rangeOffset, int rangeSize);
}
