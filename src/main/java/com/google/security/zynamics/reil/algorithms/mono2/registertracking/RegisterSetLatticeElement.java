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
package com.google.security.zynamics.reil.algorithms.mono2.registertracking;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILatticeElement;

/**
 * This lattice element keeps track of information in relation with the register tracking.
 */
public class RegisterSetLatticeElement implements ILatticeElement<RegisterSetLatticeElement> {
  /**
   * The registers which have been newly tainted throughout a complete native instruction. Set on
   * native instruction exit edge only.
   */
  private ImmutableSet<String> m_nativeNewlyTaintedRegs = ImmutableSet.<String>builder().build();

  /**
   * The registers which have been untainted throughout a complete native instruction. Set on native
   * instruction exit edge only.
   */
  private ImmutableSet<String> m_nativeUntaintedRegs = ImmutableSet.<String>builder().build();

  /**
   * The registers which have been read throughout a complete native instruction. Set on native
   * instruction exit edge only.
   */
  private ImmutableSet<String> m_nativeReadRegs = ImmutableSet.<String>builder().build();

  /**
   * The registers which have been updated throughout a complete native instruction. Set on native
   * instruction exit edge only.
   */
  private ImmutableSet<String> m_nativeUpdatedRegs = ImmutableSet.<String>builder().build();

  /**
   * The currently tainted registers.
   */
  private final Set<String> m_taintedRegs;

  /**
   * The registers that have been added to the "tainted" set within this (native, not REIL)
   * instruction
   */
  private final Set<String> m_newlyTaintedRegs;

  /**
   * The registers that have been removed from the "tainted" set within this (native, not REIL)
   * instruction.
   */
  private final Set<String> m_untaintedRegs;

  /**
   * The (tainted) registers which are read in any way or form from this (native, not REIL)
   * instruction.
   */
  private final Set<String> m_readRegs;

  /**
   * The (tainted) registers which are written from itself or another already tainted register.
   */
  private final Set<String> m_updatedRegs;

  public RegisterSetLatticeElement() {
    m_taintedRegs = new TreeSet<String>();
    m_newlyTaintedRegs = new TreeSet<String>();
    m_untaintedRegs = new TreeSet<String>();
    m_readRegs = new TreeSet<String>();
    m_updatedRegs = new TreeSet<String>();
  }

  public RegisterSetLatticeElement(final Set<String> taintedRegs,
      final Set<String> newlyTaintedRegs, final Set<String> untaintedRegs,
      final Set<String> readRegs, final Set<String> updatedRegs) {
    Preconditions.checkNotNull(taintedRegs, "Error: Tainted registers argument can not be null");
    Preconditions.checkNotNull(newlyTaintedRegs,
        "Error: Newly tainted registers argument can not be null");
    Preconditions
        .checkNotNull(untaintedRegs, "Error: Untainted registers argument can not be null");
    Preconditions.checkNotNull(readRegs, "Error: Read registers argument can not be null");
    Preconditions.checkNotNull(updatedRegs, "Error: Updated registers argument can not be null");

    m_taintedRegs = new TreeSet<String>(taintedRegs);
    m_newlyTaintedRegs = new TreeSet<String>(newlyTaintedRegs);
    m_untaintedRegs = new TreeSet<String>(untaintedRegs);
    m_readRegs = new TreeSet<String>(readRegs);
    m_updatedRegs = new TreeSet<String>(updatedRegs);
  }

  /**
   * Initializing constructor to be used for generating the initial
   * {@link RegisterSetLatticeElement}
   * 
   * @param register The register which is initially tainted.
   */
  public RegisterSetLatticeElement(final String register) {
    Preconditions.checkNotNull(register, "Error: Register argument can not be null");

    (m_taintedRegs = new TreeSet<String>()).add(register);
    (m_newlyTaintedRegs = new TreeSet<String>()).add(register);
    m_untaintedRegs = new TreeSet<String>();
    m_readRegs = new TreeSet<String>();
    m_updatedRegs = new TreeSet<String>();

  }

  /**
   * This function clears all REIL temporary registers from a Set of registers.
   * 
   * @param registerSet The Set of registers where the REIL temporary registers are cleared from.
   */
  private static void clearTemporaryRegisters(final Set<String> registerSet) {
    Sets.filter(registerSet, new Predicate<String>() {
      @Override
      public boolean apply(final String register) {
        return ReilHelpers.isTemporaryRegister(register);
      }
    }).clear();
  }

  /**
   * This function copies the state from the input states into the current lattice element.
   * 
   * @param inputState The Collection of input state to copy the information from.
   */
  public void addAll(final Collection<RegisterSetLatticeElement> inputState) {
    Preconditions.checkNotNull(inputState, "Error: Input state argument can not be null");

    final Set<String> tempNativeNewlyTaintedRegisters = Sets.newHashSet();
    final Set<String> tempNativeUntaintedRegisters = Sets.newHashSet();
    final Set<String> tempNativeReadRegisters = Sets.newHashSet();
    final Set<String> tempNativeUpdatedRegisters = Sets.newHashSet();

    for (final RegisterSetLatticeElement element : inputState) {
      tempNativeNewlyTaintedRegisters.addAll(element.m_nativeNewlyTaintedRegs);
      tempNativeUntaintedRegisters.addAll(element.m_nativeUntaintedRegs);
      tempNativeReadRegisters.addAll(element.m_nativeReadRegs);
      tempNativeUpdatedRegisters.addAll(element.m_nativeUpdatedRegs);
      m_taintedRegs.addAll(element.m_taintedRegs);
      m_newlyTaintedRegs.addAll(element.m_newlyTaintedRegs);
      m_untaintedRegs.addAll(element.m_untaintedRegs);
      m_readRegs.addAll(element.m_readRegs);
      m_updatedRegs.addAll(element.m_updatedRegs);
    }

    m_nativeNewlyTaintedRegs = ImmutableSet.copyOf(tempNativeNewlyTaintedRegisters);
    m_nativeUntaintedRegs = ImmutableSet.copyOf(tempNativeUntaintedRegisters);
    m_nativeReadRegs = ImmutableSet.copyOf(tempNativeReadRegisters);
    m_nativeUpdatedRegs = ImmutableSet.copyOf(tempNativeUpdatedRegisters);
  }

  /**
   * This function adds a register to the set of read registers.
   * 
   * @param reg The register to be added to the read register set.
   */
  public void addReadReg(final String reg) {
    m_readRegs.add(Preconditions.checkNotNull(reg, "Error: Register argument can not be null"));
  }

  @Override
  public RegisterSetLatticeElement copy() {
    return new RegisterSetLatticeElement(m_taintedRegs, m_newlyTaintedRegs, m_untaintedRegs,
        m_readRegs, m_updatedRegs);
  }

  public Collection<String> getNewlyTaintedRegisters() {
    return Collections.unmodifiableCollection(m_nativeNewlyTaintedRegs);
  }

  public Collection<String> getReadRegisters() {
    return Collections.unmodifiableCollection(m_nativeReadRegs);
  }

  public Collection<String> getTaintedRegisters() {
    return Collections.unmodifiableCollection(m_taintedRegs);
  }

  public Collection<String> getUntaintedRegisters() {
    return Collections.unmodifiableCollection(m_nativeUntaintedRegs);
  }

  public Collection<String> getUpdatedRegisters() {
    return Collections.unmodifiableCollection(m_nativeUpdatedRegs);
  }

  public boolean isSmallerEqual(final RegisterSetLatticeElement other) {
    Preconditions.checkNotNull(other, "Error: Other argument can not be null");

    final boolean result =
        other.m_taintedRegs.containsAll(m_taintedRegs)
            && other.m_newlyTaintedRegs.containsAll(m_newlyTaintedRegs)
            && other.m_untaintedRegs.containsAll(m_untaintedRegs)
            && other.m_readRegs.containsAll(m_readRegs)
            && other.m_updatedRegs.containsAll(m_updatedRegs)
            && other.m_nativeNewlyTaintedRegs.containsAll(m_nativeNewlyTaintedRegs)
            && other.m_nativeReadRegs.containsAll(m_nativeReadRegs)
            && other.m_nativeUntaintedRegs.containsAll(m_nativeUntaintedRegs)
            && other.m_nativeUpdatedRegs.containsAll(m_nativeUpdatedRegs);
    return result;
  }

  /**
   * Function to check if any of the registers in a collection is in the current taint set.
   * 
   * @param registers {@link Collection} of register strings to be checked.
   * 
   * @return True if any of the registers in the parameter is currently tainted.
   */
  public boolean isTainted(final Collection<String> registers) {
    Preconditions.checkNotNull(registers, "Error: Registers argument can not be null");

    for (final String register : registers) {
      if (m_taintedRegs.contains(register)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Function to check if a register is in the current taint set.
   * 
   * @param register The register to be checked.
   * 
   * @return True if the register is in the current taint set.
   */
  public boolean isTainted(final String register) {
    return m_taintedRegs.contains(Preconditions.checkNotNull(register,
        "Error: Register argument can not be null"));
  }

  /**
   * Function to be called if a native instruction exit edge is traversed. Moves all internal state
   * to the external visible native state. Also clears the temporary registers from all of the sets
   * to prevent tracking REIL temporary registers past native instruction boundaries.
   */
  @Override
  public void onInstructionExit() {
    clearTemporaryRegisters(m_taintedRegs);
    clearTemporaryRegisters(m_newlyTaintedRegs);
    clearTemporaryRegisters(m_untaintedRegs);
    clearTemporaryRegisters(m_readRegs);
    clearTemporaryRegisters(m_updatedRegs);

    m_nativeNewlyTaintedRegs = ImmutableSet.copyOf(m_newlyTaintedRegs);
    m_nativeUntaintedRegs = ImmutableSet.copyOf(m_untaintedRegs);
    m_nativeReadRegs = ImmutableSet.copyOf(m_readRegs);
    m_nativeUpdatedRegs = ImmutableSet.copyOf(m_updatedRegs);
    m_newlyTaintedRegs.clear();
    m_untaintedRegs.clear();
    m_readRegs.clear();
    m_updatedRegs.clear();
  }

  /**
   * Function to taint a register and update all the sets accordingly.
   */
  public void taint(final String register) {
    Preconditions.checkNotNull(register, "Error: Register argument can not be null");

    if (isTainted(register) || m_untaintedRegs.contains(register)) {
      m_updatedRegs.add(register);
    } else {
      m_newlyTaintedRegs.add(register);
    }
    m_taintedRegs.add(register);
    m_untaintedRegs.remove(register);
  }

  /**
   * Function that taints all the registers in the given state.
   * 
   * @param registers The {@link RegisterSetLatticeElement} which contains the registers to be
   *        tainted in its own m_taintedRegs Set.
   */
  public void taintAll(final Collection<String> registers) {
    Preconditions.checkNotNull(registers, "Error: State argument can not be null");

    for (final String taintedRegister : registers) {
      taint(taintedRegister);
    }
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Preserved added registers: " + m_nativeNewlyTaintedRegs + "\n");
    builder.append("Preserved untainted registers: " + m_nativeUntaintedRegs + "\n");
    builder.append("Preserved read registers: " + m_nativeReadRegs + "\n");
    builder.append("Preserved updated registers: " + m_nativeUpdatedRegs + "\n");
    builder.append("Tainted registers: " + m_taintedRegs + "\n");
    builder.append("Added registers: " + m_newlyTaintedRegs + "\n");
    builder.append("Untainted registers: " + m_untaintedRegs + "\n");
    builder.append("Read registers: " + m_readRegs + "\n");
    builder.append("Updated registers: " + m_updatedRegs + "\n");

    return builder.toString();
  }

  /**
   * Function that untaints a register.
   * 
   * @param register The register to be removed from the taint set and added to the untainted set.
   */
  public void untaint(final String register) {
    Preconditions.checkNotNull(register, "Error: Register argument can not be null");

    if (isTainted(register)) {
      m_untaintedRegs.add(register);
      m_taintedRegs.remove(register);
    }
  }

  /**
   * Function that untaints all registers in the {@link Collection} of registers.
   * 
   * @param registers The {@link Collection} of registers to be untainted.
   */
  public void untaintAll(final Collection<String> registers) {
    Preconditions.checkNotNull(registers, "Error: Registers argument can not be null");

    for (final String register : registers) {
      untaint(register);
    }
  }
}
