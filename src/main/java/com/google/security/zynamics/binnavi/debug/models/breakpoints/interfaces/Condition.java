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
package com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces;

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNode;

/**
 * Interface of all breakpoint conditions.
 */
public interface Condition {
  /**
   * Returns the root node of the breakpoint condition.
   *
   * @return The root node of the breakpoint condition.
   *
   * @throws MaybeNullException Thrown if there is no root node.
   */
  ConditionNode getRoot() throws MaybeNullException;
}
