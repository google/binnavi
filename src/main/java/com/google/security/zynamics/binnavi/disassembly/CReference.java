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

import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;

/**
 * Represents an outgoing code or data reference.
 */
public final class CReference implements IReference {
  /**
   * The address of the reference.
   */
  private final IAddress m_target;

  /**
   * The type of the reference.
   */
  private final ReferenceType m_type;

  /**
   * Creates a new reference object.
   * 
   * @param target The address of the reference.
   * @param type The type of the reference.
   */
  public CReference(final IAddress target, final ReferenceType type) {
    m_type = type;
    m_target = target;
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof IReference)) {
      return false;
    }

    final IReference rhsReference = (IReference) rhs;

    return m_target.equals(rhsReference.getTarget()) && m_type.equals(rhsReference.getType());
  }

  @Override
  public IAddress getTarget() {
    return m_target;
  }

  @Override
  public ReferenceType getType() {
    return m_type;
  }

  @Override
  public int hashCode() {
    return m_target.hashCode() + m_type.hashCode();
  }
}
