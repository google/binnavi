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

import com.google.security.zynamics.zylib.general.Convert;


public class Register implements IAloc {
  private final String m_register;

  public Register(final String register) {
    if (Convert.isDecString(register)) {
      throw new IllegalStateException();
    }

    m_register = register;
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Register) && ((Register) rhs).m_register.equals(m_register);
  }

  public String getName() {
    return m_register;
  }

  @Override
  public int hashCode() {
    return m_register.hashCode();
  }

  @Override
  public String toString() {
    return m_register;
  }
}
