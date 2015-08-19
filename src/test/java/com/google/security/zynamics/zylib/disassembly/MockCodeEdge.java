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
package com.google.security.zynamics.zylib.disassembly;

import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

public class MockCodeEdge<T> implements ICodeEdge<T> {
  private final T m_source;
  private final T m_target;
  private final EdgeType m_type;

  public MockCodeEdge(final T source, final T target, final EdgeType type) {
    m_source = source;
    m_target = target;
    m_type = type;
  }

  @Override
  public T getSource() {
    return m_source;
  }

  @Override
  public T getTarget() {
    return m_target;
  }

  @Override
  public EdgeType getType() {
    return m_type;
  }

}
