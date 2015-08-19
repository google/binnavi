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
package com.google.security.zynamics.reil.algorithms.mono2.registertracking;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterSetLattice;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterSetLatticeElement;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;

@RunWith(JUnit4.class)
public class RegisterSetLatticeTest {
  RegisterSetLatticeElement m_emptyRegisterSetLatticeElement;
  RegisterSetLatticeElement m_fullRegisterSetLatticeElement;
  Set<String> m_fullSet;

  @Test
  public void combine() {
    final RegisterSetLattice lattice = new RegisterSetLattice();
    final RegisterSetLatticeElement element =
        lattice.combine(Lists.newArrayList(m_fullRegisterSetLatticeElement,
            m_emptyRegisterSetLatticeElement));

    Assert.assertTrue(element.getTaintedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getTaintedRegisters()));
    Assert.assertTrue(element.getUntaintedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getUntaintedRegisters()));
    Assert.assertTrue(element.getReadRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getReadRegisters()));
    Assert.assertTrue(element.getUpdatedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getUpdatedRegisters()));
  }

  @Test
  public void getMinimalElement() {
    final RegisterSetLattice lattice = new RegisterSetLattice();
    lattice.getMinimalElement().getNewlyTaintedRegisters().isEmpty();
    lattice.getMinimalElement().getTaintedRegisters().isEmpty();
    lattice.getMinimalElement().getUntaintedRegisters().isEmpty();
    lattice.getMinimalElement().getUpdatedRegisters().isEmpty();
    lattice.getMinimalElement().getReadRegisters().isEmpty();
  }

  @Test
  public void isSmallerEqual() {
    final RegisterSetLattice lattice = new RegisterSetLattice();
    Assert.assertTrue(lattice.isSmallerEqual(lattice.getMinimalElement(),
        lattice.getMinimalElement()));
    Assert.assertTrue(lattice.isSmallerEqual(lattice.getMinimalElement(),
        m_fullRegisterSetLatticeElement));
    Assert.assertFalse(lattice.isSmallerEqual(m_fullRegisterSetLatticeElement,
        lattice.getMinimalElement()));
    Assert.assertTrue(lattice.isSmallerEqual(m_fullRegisterSetLatticeElement,
        m_fullRegisterSetLatticeElement));
  }

  @Before
  public void setUp() {
    m_emptyRegisterSetLatticeElement = new RegisterSetLatticeElement();
    m_fullSet = Sets.newHashSet("eax", "ebx", "ecx", "edx", "esi");
    m_fullRegisterSetLatticeElement =
        new RegisterSetLatticeElement(m_fullSet, m_fullSet, new HashSet<String>(), m_fullSet,
            m_fullSet);
  }
}
