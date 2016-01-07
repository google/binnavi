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

/**
 * Interface for classes that want to be notified about changes in operand expression replacements.
 */
public interface INaviReplacementListener {
  /**
   * Invoked after the value of a replacement change.
   * 
   * @param replacement The replacement whose value changed.
   */
  void changed(INaviReplacement replacement);
}
