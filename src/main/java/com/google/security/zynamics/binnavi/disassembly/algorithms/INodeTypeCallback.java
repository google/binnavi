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
package com.google.security.zynamics.binnavi.disassembly.algorithms;

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;

/**
 * This interface must be implemented by all objects that want to switch over the concrete
 * subclasses of INaviViewNode objects.
 * 
 * @param <T> The type of the return value of the methods of this interface.
 */
public interface INodeTypeCallback<T> {
  /**
   * Invoked if the node type switcher encounters a code node.
   * 
   * @param node The concrete code node object.
   * 
   * @return The value that should be passed back to the caller.
   */
  T handle(INaviCodeNode node);

  /**
   * Invoked if the node type switcher encounters a function node.
   * 
   * @param node The concrete function node object.
   * 
   * @return The value that should be passed back to the caller.
   */
  T handle(INaviFunctionNode node);

  /**
   * Invoked if the node type switcher encounters a group node.
   * 
   * @param node The concrete group node object.
   * 
   * @return The value that should be passed back to the caller.
   */
  T handle(INaviGroupNode node);

  /**
   * Invoked if the node type switcher encounters a text node.
   * 
   * @param node The concrete text node object.
   * 
   * @return The value that should be passed back to the caller.
   */
  T handle(INaviTextNode node);
}
