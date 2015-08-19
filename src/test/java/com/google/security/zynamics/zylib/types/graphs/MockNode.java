/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.zylib.types.graphs;

import java.util.ArrayList;
import java.util.List;

public class MockNode implements IGraphNode<MockNode> {
  private final String text;

  private final List<MockNode> children = new ArrayList<MockNode>();
  private final List<MockNode> parents = new ArrayList<MockNode>();

  public MockNode(final String text) {
    this.text = text;
  }

  public static void link(final MockNode parent, final MockNode child) {
    parent.children.add(child);
    child.parents.add(parent);
  }

  @Override
  public List<MockNode> getChildren() {
    return children;
  }

  @Override
  public List<MockNode> getParents() {
    return parents;
  }

  @Override
  public String toString() {
    return text;
  }
}
