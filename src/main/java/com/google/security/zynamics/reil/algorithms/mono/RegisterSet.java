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
package com.google.security.zynamics.reil.algorithms.mono;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1;

/**
 * Lattice element class used for register tracking.
 */
public final class RegisterSet implements ILatticeElementMono1<RegisterSet>, Iterable<String> {
  private final Set<String> registers = new HashSet<>();

  public RegisterSet(final List<String> registers) {
    this.registers.addAll(registers);
  }

  public RegisterSet(final String... registers) {
    for (final String register : registers) {
      this.registers.add(Preconditions.checkNotNull(register,
          "Error: register argument can not be null"));
    }
  }

  public static RegisterSet combine(final RegisterSet lhs, final List<String> rhs) {
    final RegisterSet set = new RegisterSet();

    for (final String string : lhs) {
      set.registers.add(string);
    }

    for (final String string : rhs) {
      set.registers.add(string);
    }

    return set;
  }

  public static RegisterSet combine(final RegisterSet lhs, final RegisterSet rhs) {
    final RegisterSet set = new RegisterSet();

    for (final String string : lhs) {
      set.registers.add(string);
    }

    for (final String string : rhs) {
      set.registers.add(string);
    }

    return set;
  }

  public static RegisterSet combine(final RegisterSet state, final String value) {
    final RegisterSet set = new RegisterSet();

    for (final String string : state) {
      set.registers.add(string);
    }

    set.registers.add(value);

    return set;
  }

  public static RegisterSet remove(final RegisterSet state, final String value) {
    final RegisterSet set = new RegisterSet();

    for (final String string : state) {
      set.registers.add(string);
    }

    set.registers.remove(value);

    return set;
  }

  public boolean contains(final String string) {
    return registers.contains(string);
  }

  @Override
  public boolean equals(final RegisterSet rhs) // NOPMD by sp on 04.11.08 14:15
  {
    return rhs.registers.equals(registers);
  }

  public Set<String> getRegisters() {
    return new HashSet<String>(registers);
  }

  @Override
  public Iterator<String> iterator() {
    return registers.iterator();
  }

  @Override
  public boolean lessThan(final RegisterSet rhs) {
    return false;
  }

  @Override
  public String toString() {

    return registers.stream()
            .collect(Collectors.joining(", ", "{", "}"));
  }
}
