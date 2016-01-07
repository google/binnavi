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
 * Listener interface for objects that want to listen on raw modules.
 */
public interface IRawModuleListener {
  /**
   * Invoked after the description of a raw module changed.
   * 
   * @param rawModule The raw module whose description changed.
   * @param description The new description of the raw module.
   */
  void changedDescription(CRawModule rawModule, String description);

  /**
   * Invoked after the name of a raw module changed.
   * 
   * @param rawModule The raw module whose description changed.
   * @param name The new name of the raw module.
   */
  void changedName(CRawModule rawModule, String name);
}
