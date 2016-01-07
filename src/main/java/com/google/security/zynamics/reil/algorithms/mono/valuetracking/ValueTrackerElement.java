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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IAloc;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.MemoryCell;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Register;
import com.google.security.zynamics.zylib.disassembly.IAddress;


public class ValueTrackerElement implements ILatticeElementMono1<ValueTrackerElement>, Cloneable {
  private final Map<IAloc, IValueElement> m_values;

  private final Set<ReilInstruction> m_influences;

  private final Map<String, Set<IAddress>> m_lastWritten;

  public ValueTrackerElement() {
    this(new LinkedHashSet<ReilInstruction>(), new HashMap<IAloc, IValueElement>(),
        new HashMap<String, Set<IAddress>>());
  }

  public ValueTrackerElement(final Set<ReilInstruction> influences,
      final Map<IAloc, IValueElement> values, final Map<String, Set<IAddress>> lastWritten) {
    m_values = new HashMap<IAloc, IValueElement>(values);
    m_influences = new LinkedHashSet<ReilInstruction>(influences);
    m_lastWritten = new HashMap<String, Set<IAddress>>(lastWritten);
  }

  private static Map<String, Set<IAddress>> createLastWritten(
      final Map<String, Set<IAddress>> written, final ReilInstruction influence) {
    final HashMap<String, Set<IAddress>> newWritten = new HashMap<String, Set<IAddress>>(written);

    newWritten.remove(influence.getAddress());

    newWritten.put(influence.getThirdOperand().getValue(), Sets.newHashSet(influence.getAddress()));

    return newWritten;
  }

  @Override
  public ValueTrackerElement clone() {
    return new ValueTrackerElement(m_influences, m_values, m_lastWritten);
  }

  @Override
  public boolean equals(final ValueTrackerElement rhs) {
    return m_values.equals(rhs.m_values) && m_lastWritten.equals(rhs.m_lastWritten);
  }

  public Set<ReilInstruction> getInfluences() {
    return new HashSet<ReilInstruction>(m_influences);
  }

  public Map<String, Set<IAddress>> getLastWritten() {
    return new HashMap<String, Set<IAddress>>(m_lastWritten);
  }

  public IValueElement getState(final IAloc aloc) {
    return m_values.get(aloc);
  }

  public IValueElement getState(final String register) {
    return getState(new Register(register));
    // final List<IValueElement> states = new ArrayList<IValueElement>();
    //
    // for (final IAddress address : getLastWritten(register))
    // {
    // final IValueElement state = getState(new Register(address, register));
    //
    // if (state != null)
    // {
    // states.add(state);
    // }
    // }
    //
    // if (states.size() == 0)
    // {
    // return new Symbol(new CAddress(0), register);
    // }
    // else if (states.size() == 1)
    // {
    // return states.get(0);
    // }
    // else if (states.size() == 2)
    // {
    // return new Either(states.get(0), states.get(1)).getSimplified();
    // }
    //
    // for (final IValueElement valueElement : states) {
    // System.out.println(valueElement);
    // }
    //
    // throw new IllegalStateException();
  }

  public Map<IAloc, IValueElement> getStates() {
    return new HashMap<IAloc, IValueElement>(m_values);
  }

  @Override
  public boolean lessThan(final ValueTrackerElement rhs) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    final List<Map.Entry<IAloc, IValueElement>> entries =
        new ArrayList<Map.Entry<IAloc, IValueElement>>(m_values.entrySet());

    Collections.sort(entries, new Comparator<Map.Entry<IAloc, IValueElement>>() {
      @Override
      public int compare(final Entry<IAloc, IValueElement> o1, final Entry<IAloc, IValueElement> o2) {
        final IAloc lhs = o1.getKey();
        final IAloc rhs = o2.getKey();

        if ((lhs instanceof Register) && (rhs instanceof Register)
            && ReilHelpers.isTemporaryRegister(((Register) lhs).getName())
            && ReilHelpers.isTemporaryRegister(((Register) rhs).getName())) {
          return ((Register) lhs).getName().compareToIgnoreCase(((Register) rhs).getName());
        } else if ((lhs instanceof Register) && (rhs instanceof Register)
            && ReilHelpers.isTemporaryRegister(((Register) lhs).getName())
            && !ReilHelpers.isTemporaryRegister(((Register) rhs).getName())) {
          return 1;
        } else if ((lhs instanceof Register) && (rhs instanceof Register)
            && !ReilHelpers.isTemporaryRegister(((Register) lhs).getName())
            && ReilHelpers.isTemporaryRegister(((Register) rhs).getName())) {
          return -1;
        } else if ((lhs instanceof Register) && (rhs instanceof Register)
            && !ReilHelpers.isTemporaryRegister(((Register) lhs).getName())
            && !ReilHelpers.isTemporaryRegister(((Register) rhs).getName())) {
          return ((Register) lhs).getName().compareTo(((Register) rhs).getName());
        } else if ((lhs instanceof MemoryCell) && (rhs instanceof MemoryCell)) {
          return ((MemoryCell) lhs).toString().compareTo(((MemoryCell) rhs).toString());
        } else if ((lhs instanceof MemoryCell) && (rhs instanceof Register)) {
          return 1;
        } else if ((lhs instanceof Register) && (rhs instanceof MemoryCell)) {
          return -1;
        } else {
          System.out.println(lhs);
          System.out.println(rhs);
          throw new IllegalStateException();
        }
      }
    });

    for (final Map.Entry<IAloc, IValueElement> element : entries) {
      sb.append("[");
      sb.append(element.getKey());
      sb.append(" -> ");
      sb.append(element.getValue());
      sb.append("]");
    }

    return sb.toString();
  }

  public ValueTrackerElement update(final ReilInstruction influence, final IAloc aloc,
      final IValueElement value) {
    if (m_influences.contains(influence) && (getState(aloc) != null)
        && !influence.getThirdOperand().getValue().equals(aloc.toString())
        && !value.equals(getState(aloc))) {
      System.out.println(influence);
      System.out.println(aloc + " -> " + value);
      System.out.println(getState(aloc));
      System.out.println(m_influences);
      System.out.println(m_values);
      throw new IllegalStateException();
    }

    final Set<ReilInstruction> newInfluences = new HashSet<ReilInstruction>(m_influences);

    newInfluences.add(influence);

    final Map<String, Set<IAddress>> newLastWritten = createLastWritten(m_lastWritten, influence);

    final HashMap<IAloc, IValueElement> updatedValues = new HashMap<IAloc, IValueElement>(m_values);

    updatedValues.put(aloc, value);

    return new ValueTrackerElement(newInfluences, updatedValues, newLastWritten);
  }
}
