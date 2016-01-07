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
// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.security.zynamics.reil.translators.mips;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.List;


public class ClzTranslator implements IInstructionTranslator {

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "clz");

    final String targetRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String sourceRegister =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    final String isZeroCondition = environment.getNextVariableString();

    // if Rm == 0
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, sourceRegister, dw, isZeroCondition));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0x0L), dw,
        isZeroCondition, dw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, String.valueOf(32L), dw,
        tmpVar2));

    // else
    final String a = sourceRegister;
    final String b = environment.getNextVariableString();
    final String c = environment.getNextVariableString();
    final String d = environment.getNextVariableString();
    final String e = environment.getNextVariableString();
    final String f = environment.getNextVariableString();
    final String g = environment.getNextVariableString();
    final String h = environment.getNextVariableString();
    final String i = environment.getNextVariableString();
    final String j = environment.getNextVariableString();
    final String k = environment.getNextVariableString();
    final String l = environment.getNextVariableString();
    final String m = environment.getNextVariableString();
    final String n = environment.getNextVariableString();
    final String o = environment.getNextVariableString();
    final String p = environment.getNextVariableString();
    final String q = environment.getNextVariableString();
    final String r = environment.getNextVariableString();
    final String s = environment.getNextVariableString();
    final String t = environment.getNextVariableString();
    final String u = environment.getNextVariableString();
    final String v = environment.getNextVariableString();
    final String w = environment.getNextVariableString();
    final String x = environment.getNextVariableString();
    final String y = environment.getNextVariableString();
    final String z = environment.getNextVariableString();

    // y = a >> 1;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, a, dw, String.valueOf(-1L), dw, y));

    // p = y | a;
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, y, dw, a, dw, p));

    // q = p >> 2;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, p, dw, String.valueOf(-2L), dw, q));

    // r = q | p;
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, q, dw, p, dw, r));

    // s = r >> 4;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, r, dw, String.valueOf(-4L), dw, s));

    // t = s | r;
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, s, dw, r, dw, t));

    // u = t >> 8;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, t, dw, String.valueOf(-8L), dw, u));

    // v = u | t;
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, u, dw, t, dw, v));

    // w = v >> 16;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, v, dw, String.valueOf(-16L), dw, w));

    // z = w | v;
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, w, dw, v, dw, z));

    // x = z >> 1;
    instructions.add(ReilHelpers.createBsh(baseOffset, dw, z, dw, String.valueOf(-1L), dw, x));

    // b = x & 0x55555555;
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, x, dw, String.valueOf(0x55555555L),
        dw, b));

    // c = z - b;
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, z, dw, b, qw, c));

    // d = c & 0x33333333;
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, c, dw, String.valueOf(0x33333333L),
        dw, d));

    // e = c >> 2;
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, c, dw, String.valueOf(-2L), dw, e));

    // f = e & 0x33333333;
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, e, dw, String.valueOf(0x33333333), dw,
        f));

    // g = f + d;
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, f, dw, d, dw, g));

    // h = g >> 4;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, g, dw, String.valueOf(-4L), dw, h));

    // i = h + g;
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, h, dw, g, dw, i));

    // j = i & 0x0f0f0f0f;
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, i, dw, String.valueOf(0x0F0F0F0F), dw,
        j));

    // k = j >> 8;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, j, dw, String.valueOf(-8L), dw, k));

    // l = k + j;
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, k, dw, j, dw, l));

    // m = l >> 16;
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, l, dw, String.valueOf(-16L), dw, m));

    // n = m + l;
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, m, dw, l, dw, n));

    // o = n & 0x0000003f;
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, n, dw, String.valueOf(0x0000003F), dw,
        o));

    // result = 32 - o
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(32L), dw, o, dw,
        tmpVar3));

    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar2, dw, tmpVar3, dw,
        targetRegister));

  }

}
