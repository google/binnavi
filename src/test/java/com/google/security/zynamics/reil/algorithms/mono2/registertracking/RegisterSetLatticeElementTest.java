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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;

@RunWith(JUnit4.class)
public class RegisterSetLatticeElementTest {
  RegisterSetLatticeElement m_emptyRegisterSetLatticeElement;
  RegisterSetLatticeElement m_fullRegisterSetLatticeElement;
  Set<String> m_fullSet;

  @Test
  public void addAll() {
    m_emptyRegisterSetLatticeElement.addAll(Lists.newArrayList(m_fullRegisterSetLatticeElement));

    m_emptyRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getTaintedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getTaintedRegisters()));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getUntaintedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getUntaintedRegisters()));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getReadRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getReadRegisters()));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getUpdatedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getUpdatedRegisters()));
  }

  @Test
  public void addAllMultiple() {

    final RegisterSetLatticeElement element1 =
        new RegisterSetLatticeElement(Sets.newHashSet("register1"), Sets.newHashSet("register2"),
            Sets.newHashSet("register3"), Sets.newHashSet("register4"),
            Sets.newHashSet("register5"));

    final RegisterSetLatticeElement element2 =
        new RegisterSetLatticeElement(Sets.newHashSet("register6"), Sets.newHashSet("register7"),
            Sets.newHashSet("register8"), Sets.newHashSet("register9"),
            Sets.newHashSet("register10"));

    m_emptyRegisterSetLatticeElement.addAll(Lists.newArrayList(element1, element2));

    m_emptyRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getTaintedRegisters().containsAll(
        Lists.newArrayList("register1", "register6")));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getUntaintedRegisters().containsAll(
        Lists.newArrayList("register3", "register8")));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getReadRegisters().containsAll(
        Lists.newArrayList("register4", "register9")));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getUpdatedRegisters().containsAll(
        Lists.newArrayList("register5", "register10")));
  }

  @Test
  public void copy() {
    final RegisterSetLatticeElement copiedElement = m_fullRegisterSetLatticeElement.copy();

    m_fullRegisterSetLatticeElement.onInstructionExit();
    copiedElement.onInstructionExit();

    Assert.assertTrue(copiedElement.getTaintedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getTaintedRegisters()));
    Assert.assertTrue(copiedElement.getUntaintedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getUntaintedRegisters()));
    Assert.assertTrue(copiedElement.getReadRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getReadRegisters()));
    Assert.assertTrue(copiedElement.getUpdatedRegisters().containsAll(
        m_fullRegisterSetLatticeElement.getUpdatedRegisters()));
  }

  @Test
  public void isSmallerEqual() {
    Assert.assertTrue(m_emptyRegisterSetLatticeElement
        .isSmallerEqual(m_fullRegisterSetLatticeElement));
    Assert.assertFalse(m_fullRegisterSetLatticeElement
        .isSmallerEqual(m_emptyRegisterSetLatticeElement));
    Assert.assertTrue(m_emptyRegisterSetLatticeElement
        .isSmallerEqual(m_emptyRegisterSetLatticeElement));
    Assert.assertTrue(m_fullRegisterSetLatticeElement
        .isSmallerEqual(m_fullRegisterSetLatticeElement));
  }

  @Test
  public void isTaintedRegister() {
    Assert.assertTrue(m_fullRegisterSetLatticeElement.isTainted("eax"));
    Assert.assertFalse(m_emptyRegisterSetLatticeElement.isTainted("eax"));
  }

  @Test
  public void isTaintedRegisterCollection() {
    Assert.assertTrue(m_fullRegisterSetLatticeElement.isTainted(m_fullSet));
    Assert.assertFalse(m_emptyRegisterSetLatticeElement.isTainted(m_fullSet));
  }

  @Test
  public void onInstructionExit() {
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getUntaintedRegisters().isEmpty());
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getReadRegisters().isEmpty());
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getUpdatedRegisters().isEmpty());
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getTaintedRegisters().containsAll(m_fullSet));

    m_fullRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_fullRegisterSetLatticeElement.getNewlyTaintedRegisters().containsAll(
        m_fullSet));
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getUntaintedRegisters().isEmpty());
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getReadRegisters().containsAll(m_fullSet));
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getUpdatedRegisters().containsAll(m_fullSet));
    Assert.assertTrue(m_fullRegisterSetLatticeElement.getTaintedRegisters().containsAll(m_fullSet));
  }

  @Before
  public void setUp() {
    m_emptyRegisterSetLatticeElement = new RegisterSetLatticeElement();
    m_fullSet = Sets.newHashSet("eax", "ebx", "ecx", "edx", "esi");

    m_fullRegisterSetLatticeElement =
        new RegisterSetLatticeElement(m_fullSet, m_fullSet, new HashSet<String>(), m_fullSet,
            m_fullSet);
  }

  @Test
  public void taint() {
    m_emptyRegisterSetLatticeElement.taint("eax");

    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getTaintedRegisters().contains("eax"));

    m_emptyRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getNewlyTaintedRegisters().contains("eax"));
  }

  @Test
  public void taintAll() {
    m_emptyRegisterSetLatticeElement.taintAll(m_fullSet);

    Assert
        .assertTrue(m_emptyRegisterSetLatticeElement.getTaintedRegisters().containsAll(m_fullSet));

    m_emptyRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_emptyRegisterSetLatticeElement.getNewlyTaintedRegisters().containsAll(
        m_fullSet));
  }

  @Test
  public void testUpdatedSet() {
    final RegisterSetLatticeElement element = new RegisterSetLatticeElement();
    element.taint("eax");
    element.taint("eax");
  }

  @Test
  public void untaint() {
    m_fullRegisterSetLatticeElement.untaint("eax");

    Assert.assertFalse(m_fullRegisterSetLatticeElement.getTaintedRegisters().contains("eax"));

    m_fullRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_fullRegisterSetLatticeElement.getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void untaintAll() {
    m_fullRegisterSetLatticeElement.untaintAll(m_fullSet);

    Assert.assertTrue(m_fullRegisterSetLatticeElement.getTaintedRegisters().isEmpty());

    m_fullRegisterSetLatticeElement.onInstructionExit();

    Assert.assertTrue(m_fullRegisterSetLatticeElement.getUntaintedRegisters()
        .containsAll(m_fullSet));
  }
}
