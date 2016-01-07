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
package com.google.security.zynamics.binnavi.API.disassembly;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.zylib.disassembly.IReference;

// / Represents a single code or data reference.
/**
 * Represents a code or data reference.
 */
public final class Reference implements ApiObject<IReference> {
  /**
   * Wrapped internal reference object.
   */
  private final IReference m_reference;

  // / @cond INTERNAL
  /**
   * Creates a new API reference object.
   *
   * @param reference Wrapped internal reference object.
   */
  // / @endcond
  public Reference(final IReference reference) {
    m_reference = Preconditions.checkNotNull(reference, "Error: Reference argument can't be null");
  }

  @Override
  public IReference getNative() {
    return m_reference;
  }

  // ! Target of the reference.
  /**
   * Returns the address the reference refers to.
   *
   * @return The address the reference refers to.
   */
  public Address getTarget() {
    return new Address(m_reference.getTarget().toBigInteger());
  }

  // ! Type of the reference.
  /**
   * Returns the type of the reference.
   *
   * @return The type of the reference.
   */
  public ReferenceType getType() {
    return ReferenceType.convert(m_reference.getType());
  }

  @Override
  public String toString() {
    return String.format("%s Reference to %s", getType().name(), getTarget());
  }
}
