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

import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;

/**
 * Rerpesents a function call in a call graph.
 */
public final class CCallgraphEdge extends DefaultEdge<ICallgraphNode> implements ICallgraphEdge {
  /**
   * Creates a new call graph edge object.
   * 
   * @param source Node that represents the calling function.
   * @param target Node that represents the called function.
   */
  public CCallgraphEdge(final ICallgraphNode source, final ICallgraphNode target) {
    super(source, target);
  }
}
