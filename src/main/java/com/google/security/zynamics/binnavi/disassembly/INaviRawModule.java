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
 * Interface that represents raw modules.
 */
public interface INaviRawModule extends IDatabaseObject {
  /**
   * Returns the number of functions in the raw module.
   * 
   * @return The number of functions in the raw module.
   */
  int getFunctionCount();

  /**
   * Returns the ID of the raw module.
   * 
   * @return The ID of the raw module.
   */
  int getId();

  /**
   * Returns the name string of the raw module.
   * 
   * @return The name string.
   */
  String getName();

  /**
   * Returns whether the raw module is complete.
   * 
   * @return True, if the raw module is complete. False, otherwise.
   */
  boolean isComplete();

}
