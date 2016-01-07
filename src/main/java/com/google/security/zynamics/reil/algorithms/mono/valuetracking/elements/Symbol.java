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

import java.math.BigInteger;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Convert;

public class Symbol implements IValueElement {
  private final String m_value;
  private final IAddress m_address;

  public Symbol(final IAddress address, final String value) {
    if (Convert.isDecString(value)) {
      throw new IllegalStateException();
    }

    m_address = address;
    m_value = value;
  }

  @Override
  public Symbol clone() {
    return new Symbol(m_address, m_value);
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Symbol) && ((Symbol) rhs).m_value.equals(m_value)
        && ((Symbol) rhs).m_address.equals(m_address);
  }

  @Override
  public BigInteger evaluate() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IValueElement getSimplified() {
    return this;
  }

  @Override
  public Set<String> getVariables() {
    return Sets.newHashSet(m_value);
  }

  @Override
  public int hashCode() {
    return m_address.hashCode() * m_value.hashCode();
  }

  @Override
  public String toString() {
    return m_value + "/" + m_address.toHexString();
  }
}
