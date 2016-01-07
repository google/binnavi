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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.zylib.disassembly.IReplacement;

/**
 * Interface for all classes that want to act as operand expression replacements.
 */
public interface INaviReplacement extends IReplacement {
  /**
   * Adds a listener that is notified about changes in the replacement.
   * 
   * @param listener The listener to add.
   */
  void addListener(INaviReplacementListener listener);

  /**
   * Clones the replacement object.
   * 
   * @return The cloned replacement object.
   */
  INaviReplacement cloneReplacement();

  /**
   * Closes the object.
   */
  void close();

  /**
   * Removes a listener that was notified about changes in the replacement.
   * 
   * @param listener The listener to remove.
   */
  void removeListener(INaviReplacementListener listener);
}
