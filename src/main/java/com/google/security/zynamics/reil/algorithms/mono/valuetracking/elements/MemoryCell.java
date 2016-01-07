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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements;

import com.google.common.base.Preconditions;

public class MemoryCell implements IAloc {
  private final IValueElement m_element;

  public MemoryCell(final IValueElement element) {
    m_element = Preconditions.checkNotNull(element, "Error: element argument can not be null");
    Preconditions.checkArgument(!(element instanceof Undefined),
        "Error: name argument can not be Undefined");
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof MemoryCell) && ((MemoryCell) rhs).m_element.equals(m_element);
  }

  @Override
  public int hashCode() {
    return m_element.hashCode();
  }

  @Override
  public String toString() {
    return "@" + m_element;
  }
}
