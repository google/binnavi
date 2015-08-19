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
package com.google.security.zynamics.reil.misc;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReilInstructionTest
{
	private ReilInstruction createReilInstruction()
	{
		return ReilHelpers.createAdd(0x666, OperandSize.DWORD, "1", OperandSize.DWORD, "2", OperandSize.DWORD, "eax");
	}

	@Test
	public void testEqualsNull()
	{
		assertFalse(createReilInstruction().equals(null));
	}

	@Test
	public void testEqualsReflexivity()
	{
		final ReilInstruction instr0 = createReilInstruction();
		assertTrue(instr0.equals(instr0));
	}

	@Test
	public void testEqualsSymmetry()
	{
		final ReilInstruction instr0 = createReilInstruction();
		final ReilInstruction instr1 = createReilInstruction();
		assertTrue(instr0.equals(instr1));
		assertTrue(instr1.equals(instr0));
	}

	@Test
	public void testEqualsTransitivity()
	{
		final ReilInstruction instr0 = createReilInstruction();
		final ReilInstruction instr1 = createReilInstruction();
		final ReilInstruction instr2 = createReilInstruction();
		assertTrue(instr0.equals(instr1));
		assertTrue(instr1.equals(instr2));
		assertTrue(instr0.equals(instr2));
	}

	@Test
	public void testHashCodeSame()
	{
		final ReilInstruction instr0 = createReilInstruction();
		final ReilInstruction instr1 = createReilInstruction();
		assertTrue(instr0.hashCode() == instr1.hashCode());
	}
}
