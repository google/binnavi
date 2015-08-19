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

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.MonoReilSolverResult;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;
import com.google.security.zynamics.zylib.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

@RunWith(JUnit4.class)
public class TestFollowZFIncomingBackwards {

  private ReilGraph m_graph1;
  private ReilFunction m_function;
  private MockInstruction conditionalJumpInstruction1;
  private MockInstruction addInstruction;
  private RegisterTrackingOptions m_options;

  private void generateReilGraph(final List<List<String>> instructions, final List<String> edges) {

    final Map<Long, ReilBlock> blocks = new HashMap<Long, ReilBlock>();
    for (final List<String> currentBlockInstructions : instructions) {
      final List<ReilInstruction> reilInstructions = new ArrayList<ReilInstruction>();
      for (final String addressAndInstruction : currentBlockInstructions) {
        final StringTokenizer tokenizer =
            new StringTokenizer(addressAndInstruction, ": [,]", false);
        final long offset = Long.parseLong(tokenizer.nextToken(), 16);
        final String mnemonic = tokenizer.nextToken();
        if (mnemonic.equalsIgnoreCase("bisz") || mnemonic.equalsIgnoreCase("str")
            || mnemonic.equalsIgnoreCase("ldm") || mnemonic.equalsIgnoreCase("stm")
            || mnemonic.equalsIgnoreCase("jcc")) {
          final OperandSize firstSize = OperandSize.valueOf(tokenizer.nextToken());
          final String firstValue = tokenizer.nextToken();
          tokenizer.nextToken();
          final OperandSize thirdSize = OperandSize.valueOf(tokenizer.nextToken());
          final String thirdValue = tokenizer.nextToken();
          if (mnemonic.equalsIgnoreCase("bisz")) {
            reilInstructions.add(ReilHelpers.createBisz(offset, firstSize, firstValue, thirdSize,
                thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("str")) {
            reilInstructions.add(ReilHelpers.createStr(offset, firstSize, firstValue, thirdSize,
                thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("jcc")) {
            reilInstructions.add(ReilHelpers.createJcc(offset, firstSize, firstValue, thirdSize,
                thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("ldm")) {
            reilInstructions.add(ReilHelpers.createLdm(offset, firstSize, firstValue, thirdSize,
                thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("stm")) {
            reilInstructions.add(ReilHelpers.createStm(offset, firstSize, firstValue, thirdSize,
                thirdValue));
          }
        } else if (mnemonic.equalsIgnoreCase("nop")) {
          reilInstructions.add(ReilHelpers.createNop(offset));
        } else {
          final OperandSize firstSize = OperandSize.valueOf(tokenizer.nextToken());
          final String firstValue = tokenizer.nextToken();
          final OperandSize secondSize = OperandSize.valueOf(tokenizer.nextToken());
          final String secondValue = tokenizer.nextToken();
          final OperandSize thirdSize = OperandSize.valueOf(tokenizer.nextToken());
          final String thirdValue = tokenizer.nextToken();
          if (mnemonic.equalsIgnoreCase("add")) {
            reilInstructions.add(ReilHelpers.createAdd(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("and")) {
            reilInstructions.add(ReilHelpers.createAnd(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("bsh")) {
            reilInstructions.add(ReilHelpers.createBsh(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("div")) {
            reilInstructions.add(ReilHelpers.createDiv(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("mod")) {
            reilInstructions.add(ReilHelpers.createMod(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("mul")) {
            reilInstructions.add(ReilHelpers.createMul(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("or")) {
            reilInstructions.add(ReilHelpers.createOr(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("sub")) {
            reilInstructions.add(ReilHelpers.createSub(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
          if (mnemonic.equalsIgnoreCase("xor")) {
            reilInstructions.add(ReilHelpers.createXor(offset, firstSize, firstValue, secondSize,
                secondValue, thirdSize, thirdValue));
          }
        }
      }
      blocks.put(reilInstructions.get(0).getAddress().toLong(), new ReilBlock(reilInstructions));
    }

    final List<ReilEdge> reilEdges = new ArrayList<ReilEdge>();

    for (final String edge : edges) {
      final StringTokenizer edgeTokenizer = new StringTokenizer(edge, " []->");
      final Long sourceAddress = Long.parseLong(edgeTokenizer.nextToken(), 16);
      final EdgeType type = Enum.valueOf(EdgeType.class, edgeTokenizer.nextToken().toUpperCase());
      final Long targetAddress = Long.parseLong(edgeTokenizer.nextToken(), 16);

      final ReilEdge reilEdge =
          new ReilEdge(blocks.get(sourceAddress), blocks.get(targetAddress), type);

      ReilBlock.link(blocks.get(sourceAddress), blocks.get(targetAddress), reilEdge);
    }

    m_graph1 = new ReilGraph(Lists.newArrayList(blocks.values()), reilEdges);
  }

  @Before
  public void setUp() {



  }


  @Test
  public void testTransformFollowZFinStream2() {

    final MockOperandTree operandTreeFirst1 = new MockOperandTree();
    operandTreeFirst1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTreeFirst1.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "16827245"));

    final List<MockOperandTree> operandsFirst = Lists.newArrayList(operandTreeFirst1);


    m_options =
        new RegisterTrackingOptions(true, new HashSet<String>(), true, AnalysisDirection.UP);

    final List<String> instructionStrings0 = new ArrayList<String>();

    instructionStrings0.add("000000010025C500: str [DWORD edi, EMPTY , DWORD edi]");
    instructionStrings0.add("000000010025C700: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings0.add("000000010025C701: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings0.add("000000010025C702: stm [DWORD ebp, EMPTY , DWORD esp]");
    instructionStrings0.add("000000010025C800: str [DWORD esp, EMPTY , DWORD ebp]");
    instructionStrings0.add("000000010025CA00: add [DWORD 12, DWORD ebp, QWORD t0]");
    instructionStrings0.add("000000010025CA01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings0.add("000000010025CA02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings0.add("000000010025CA03: str [DWORD t2, EMPTY , DWORD eax]");
    instructionStrings0.add("000000010025CD00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings0.add("000000010025CD01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings0.add("000000010025CD02: stm [DWORD esi, EMPTY , DWORD esp]");
    instructionStrings0.add("000000010025CE00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings0.add("000000010025CE01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings0.add("000000010025CE02: stm [DWORD edi, EMPTY , DWORD esp]");
    instructionStrings0.add("000000010025CF00: str [DWORD ecx, EMPTY , DWORD esi]");
    instructionStrings0.add("000000010025D100: and [DWORD eax, DWORD 2147483648, DWORD t0]");
    instructionStrings0.add("000000010025D101: and [DWORD 4294967295, DWORD 2147483648, DWORD t1]");
    instructionStrings0.add("000000010025D102: sub [DWORD eax, DWORD 4294967295, QWORD t2]");
    instructionStrings0.add("000000010025D103: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings0.add("000000010025D104: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings0.add("000000010025D105: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings0.add("000000010025D106: xor [DWORD t0, DWORD t3, DWORD t5]");
    instructionStrings0.add("000000010025D107: and [DWORD t4, DWORD t5, DWORD t6]");
    instructionStrings0.add("000000010025D108: bsh [DWORD t6, DWORD -31, BYTE OF]");
    instructionStrings0.add("000000010025D109: and [QWORD t2, QWORD 4294967296, QWORD t7]");
    instructionStrings0.add("000000010025D10A: bsh [QWORD t7, QWORD -32, BYTE CF]");
    instructionStrings0.add("000000010025D10B: and [QWORD t2, QWORD 4294967295, DWORD t8]");
    instructionStrings0.add("000000010025D10C: bisz [DWORD t8, EMPTY , BYTE ZF]");
    instructionStrings0.add("000000010025D400: bisz [BYTE ZF, EMPTY , BYTE t0]");
    instructionStrings0.add("000000010025D401: jcc [BYTE t0, EMPTY , DWORD 16945901]");

    final List<String> instructionStrings1 = new ArrayList<String>();
    // mov
    instructionStrings1.add("000000010025DA00: add [DWORD 8, DWORD ebp, QWORD t0]");
    instructionStrings1.add("000000010025DA01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings1.add("000000010025DA02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings1.add("000000010025DA03: str [DWORD t2, EMPTY , DWORD edi]");
    // test
    instructionStrings1.add("000000010025DD00: and [DWORD edi, DWORD edi, DWORD t0]");
    instructionStrings1.add("000000010025DD01: and [DWORD t0, DWORD 2147483648, DWORD t1]");
    instructionStrings1.add("000000010025DD02: bsh [DWORD t1, DWORD -31, BYTE SF]");
    instructionStrings1.add("000000010025DD03: bisz [DWORD t0, EMPTY , BYTE ZF]");
    instructionStrings1.add("000000010025DD04: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings1.add("000000010025DD05: str [BYTE 0, EMPTY , BYTE OF]");
    // jz
    instructionStrings1.add("000000010025DF00: jcc [BYTE ZF, EMPTY , DWORD 16880362]");

    final List<String> instructionStrings2 = new ArrayList<String>();
    instructionStrings2.add("000000010025E500: add [DWORD 8, DWORD esi, QWORD t0]");
    instructionStrings2.add("000000010025E501: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings2.add("000000010025E502: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings2.add("000000010025E503: and [DWORD edi, DWORD 2147483648, DWORD t3]");
    instructionStrings2.add("000000010025E504: and [DWORD t2, DWORD 2147483648, DWORD t4]");
    instructionStrings2.add("000000010025E505: sub [DWORD edi, DWORD t2, QWORD t5]");
    instructionStrings2.add("000000010025E506: and [QWORD t5, QWORD 2147483648, DWORD t6]");
    instructionStrings2.add("000000010025E507: bsh [DWORD t6, DWORD -31, BYTE SF]");
    instructionStrings2.add("000000010025E508: xor [DWORD t3, DWORD t4, DWORD t7]");
    instructionStrings2.add("000000010025E509: xor [DWORD t3, DWORD t6, DWORD t8]");
    instructionStrings2.add("000000010025E50A: and [DWORD t7, DWORD t8, DWORD t9]");
    instructionStrings2.add("000000010025E50B: bsh [DWORD t9, DWORD -31, BYTE OF]");
    instructionStrings2.add("000000010025E50C: and [QWORD t5, QWORD 4294967296, QWORD t10]");
    instructionStrings2.add("000000010025E50D: bsh [QWORD t10, QWORD -32, BYTE CF]");
    instructionStrings2.add("000000010025E50E: and [QWORD t5, QWORD 4294967295, DWORD t11]");
    instructionStrings2.add("000000010025E50F: bisz [DWORD t11, EMPTY , BYTE ZF]");
    instructionStrings2.add("000000010025E800: or [BYTE CF, BYTE ZF, BYTE t0]");
    instructionStrings2.add("000000010025E801: jcc [BYTE t0, EMPTY , DWORD 16880391]");

    final List<String> instructionStrings3 = new ArrayList<String>();
    instructionStrings3.add("000000010025EE00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("000000010025EE01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("000000010025EE02: stm [DWORD edi, EMPTY , DWORD esp]");
    instructionStrings3.add("000000010025EF00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("000000010025EF01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("000000010025EF02: stm [DWORD 16786932, EMPTY , DWORD esp]");
    instructionStrings3.add("000000010025EF03: jcc [DWORD 1, EMPTY , DWORD 16792317]");

    final List<String> instructionStrings4 = new ArrayList<String>();
    instructionStrings4.add("000000010025F400: and [DWORD eax, BYTE 255, BYTE t1]");
    instructionStrings4.add("000000010025F401: and [DWORD eax, BYTE 255, BYTE t3]");
    instructionStrings4.add("000000010025F402: and [BYTE t1, BYTE t3, BYTE t4]");
    instructionStrings4.add("000000010025F403: and [BYTE t4, BYTE 128, BYTE t5]");
    instructionStrings4.add("000000010025F404: bsh [BYTE t5, BYTE -7, BYTE SF]");
    instructionStrings4.add("000000010025F405: bisz [BYTE t4, EMPTY , BYTE ZF]");
    instructionStrings4.add("000000010025F406: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings4.add("000000010025F407: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings4.add("000000010025F600: jcc [BYTE ZF, EMPTY , DWORD 16786960]");

    final List<String> instructionStrings5 = new ArrayList<String>();
    instructionStrings5.add("000000010025F800: add [DWORD 4, DWORD esi, QWORD t0]");
    instructionStrings5.add("000000010025F801: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings5.add("000000010025F802: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings5.add("000000010025F803: str [DWORD t2, EMPTY , DWORD eax]");

    final List<String> instructionStrings6 = new ArrayList<String>();
    instructionStrings6.add("000000010025FB00: str [DWORD edi, EMPTY , DWORD ecx]");
    instructionStrings6.add("000000010025FD00: and [DWORD ecx, DWORD 2147483648, DWORD t0]");
    instructionStrings6.add("000000010025FD01: and [DWORD eax, DWORD 2147483648, DWORD t1]");
    instructionStrings6.add("000000010025FD02: sub [DWORD ecx, DWORD eax, QWORD t2]");
    instructionStrings6.add("000000010025FD03: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings6.add("000000010025FD04: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings6.add("000000010025FD05: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings6.add("000000010025FD06: xor [DWORD t0, DWORD t3, DWORD t5]");
    instructionStrings6.add("000000010025FD07: and [DWORD t4, DWORD t5, DWORD t6]");
    instructionStrings6.add("000000010025FD08: bsh [DWORD t6, DWORD -31, BYTE OF]");
    instructionStrings6.add("000000010025FD09: and [QWORD t2, QWORD 4294967296, QWORD t7]");
    instructionStrings6.add("000000010025FD0A: bsh [QWORD t7, QWORD -32, BYTE CF]");
    instructionStrings6.add("000000010025FD0B: and [QWORD t2, QWORD 4294967295, DWORD t8]");
    instructionStrings6.add("000000010025FD0C: bisz [DWORD t8, EMPTY , BYTE ZF]");
    instructionStrings6.add("000000010025FD0D: str [DWORD t8, EMPTY , DWORD ecx]");
    instructionStrings6.add("000000010025FF00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings6.add("000000010025FF01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings6.add("000000010025FF02: stm [DWORD ecx, EMPTY , DWORD esp]");
    instructionStrings6.add("0000000100260000: ldm [DWORD esi, EMPTY , DWORD t0]");
    instructionStrings6.add("0000000100260001: str [DWORD t0, EMPTY , DWORD ecx]");
    instructionStrings6.add("0000000100260200: mul [DWORD 4, DWORD eax, QWORD t0]");
    instructionStrings6.add("0000000100260201: add [QWORD t0, DWORD ecx, QWORD t2]");
    instructionStrings6.add("0000000100260202: and [QWORD t2, DWORD 4294967295, DWORD t3]");
    instructionStrings6.add("0000000100260203: str [DWORD t3, EMPTY , DWORD eax]");
    instructionStrings6.add("0000000100260500: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings6.add("0000000100260501: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings6.add("0000000100260502: stm [DWORD eax, EMPTY , DWORD esp]");
    instructionStrings6.add("0000000100260600: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings6.add("0000000100260601: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings6.add("0000000100260602: stm [DWORD 16786955, EMPTY , DWORD esp]");
    instructionStrings6.add("0000000100260603: jcc [DWORD 1, EMPTY , DWORD 16792263]");

    final List<String> instructionStrings7 = new ArrayList<String>();
    instructionStrings7.add("0000000100260B00: add [DWORD 4, DWORD esi, QWORD t0]");
    instructionStrings7.add("0000000100260B01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings7.add("0000000100260B02: stm [DWORD edi, EMPTY , DWORD t1]");

    final List<String> instructionStrings8 = new ArrayList<String>();
    instructionStrings8.add("0000000100260E00: and [DWORD eax, DWORD 4294967040, DWORD t1]");
    instructionStrings8.add("0000000100260E01: or [BYTE 1, DWORD t1, DWORD eax]");

    final List<String> instructionStrings9 = new ArrayList<String>();
    instructionStrings9.add("0000000100261000: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings9.add("0000000100261001: add [DWORD esp, DWORD 4, QWORD t1]");
    instructionStrings9.add("0000000100261002: and [QWORD t1, DWORD 4294967295, DWORD esp]");
    instructionStrings9.add("0000000100261003: str [DWORD t0, EMPTY , DWORD edi]");
    instructionStrings9.add("0000000100261100: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings9.add("0000000100261101: add [DWORD esp, DWORD 4, QWORD t1]");
    instructionStrings9.add("0000000100261102: and [QWORD t1, DWORD 4294967295, DWORD esp]");
    instructionStrings9.add("0000000100261103: str [DWORD t0, EMPTY , DWORD esi]");
    instructionStrings9.add("0000000100261200: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings9.add("0000000100261201: add [DWORD esp, DWORD 4, QWORD t1]");
    instructionStrings9.add("0000000100261202: and [QWORD t1, DWORD 4294967295, DWORD esp]");
    instructionStrings9.add("0000000100261203: str [DWORD t0, EMPTY , DWORD ebp]");
    instructionStrings9.add("0000000100261300: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings9.add("0000000100261301: add [DWORD esp, DWORD 12, QWORD t1]");
    instructionStrings9.add("0000000100261302: and [QWORD t1, QWORD 4294967295, DWORD esp]");
    instructionStrings9.add("0000000100261303: jcc [DWORD 1, EMPTY , DWORD t0]");

    final List<String> instructionStrings10 = new ArrayList<String>();
    instructionStrings10.add("000000010192EA00: ldm [DWORD esi, EMPTY , DWORD t0]");
    instructionStrings10.add("000000010192EA01: str [DWORD t0, EMPTY , DWORD eax]");
    instructionStrings10.add("000000010192EC00: and [DWORD eax, DWORD eax, DWORD t0]");
    instructionStrings10.add("000000010192EC01: and [DWORD t0, DWORD 2147483648, DWORD t1]");
    instructionStrings10.add("000000010192EC02: bsh [DWORD t1, DWORD -31, BYTE SF]");
    instructionStrings10.add("000000010192EC03: bisz [DWORD t0, EMPTY , BYTE ZF]");
    instructionStrings10.add("000000010192EC04: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings10.add("000000010192EC05: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings10.add("000000010192EE00: jcc [BYTE ZF, EMPTY , DWORD 16880378]");

    final List<String> instructionStrings11 = new ArrayList<String>();
    instructionStrings11.add("000000010192F000: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings11.add("000000010192F001: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings11.add("000000010192F002: stm [DWORD eax, EMPTY , DWORD esp]");
    instructionStrings11.add("000000010192F100: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings11.add("000000010192F101: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings11.add("000000010192F102: stm [DWORD 16880375, EMPTY , DWORD esp]");
    instructionStrings11.add("000000010192F103: ldm [DWORD 16782844, EMPTY , DWORD t1]");
    instructionStrings11.add("000000010192F104: jcc [DWORD 1, EMPTY , DWORD t1]");


    final List<String> instructionStrings12 = new ArrayList<String>();
    instructionStrings12.add("000000010192F700: ldm [DWORD esi, EMPTY , DWORD t0]");
    instructionStrings12.add("000000010192F701: and [DWORD edi, DWORD t0, DWORD t1]");
    instructionStrings12.add("000000010192F702: and [DWORD t1, DWORD 2147483648, DWORD t2]");
    instructionStrings12.add("000000010192F703: bsh [DWORD t2, DWORD -31, BYTE SF]");
    instructionStrings12.add("000000010192F704: bisz [DWORD t1, EMPTY , BYTE ZF]");
    instructionStrings12.add("000000010192F705: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings12.add("000000010192F706: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings12.add("000000010192F707: stm [DWORD t1, EMPTY , DWORD esi]");
    instructionStrings12.add("000000010192F900: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings12.add("000000010192F901: add [DWORD esp, DWORD 4, QWORD t1]");
    instructionStrings12.add("000000010192F902: and [QWORD t1, DWORD 4294967295, DWORD esp]");
    instructionStrings12.add("000000010192F903: str [DWORD t0, EMPTY , DWORD ecx]");

    final List<String> instructionStrings13 = new ArrayList<String>();
    instructionStrings13.add("000000010192FA00: add [DWORD 4, DWORD esi, QWORD t0]");
    instructionStrings13.add("000000010192FA01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings13.add("000000010192FA02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings13.add("000000010192FA03: and [DWORD 0, DWORD t2, DWORD t3]");
    instructionStrings13.add("000000010192FA04: and [DWORD t3, DWORD 2147483648, DWORD t4]");
    instructionStrings13.add("000000010192FA05: bsh [DWORD t4, DWORD -31, BYTE SF]");
    instructionStrings13.add("000000010192FA06: bisz [DWORD t3, EMPTY , BYTE ZF]");
    instructionStrings13.add("000000010192FA07: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings13.add("000000010192FA08: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings13.add("000000010192FA09: stm [DWORD t3, EMPTY , DWORD t1]");
    instructionStrings13.add("000000010192FE00: add [DWORD 8, DWORD esi, QWORD t0]");
    instructionStrings13.add("000000010192FE01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings13.add("000000010192FE02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings13.add("000000010192FE03: and [DWORD 0, DWORD t2, DWORD t3]");
    instructionStrings13.add("000000010192FE04: and [DWORD t3, DWORD 2147483648, DWORD t4]");
    instructionStrings13.add("000000010192FE05: bsh [DWORD t4, DWORD -31, BYTE SF]");
    instructionStrings13.add("000000010192FE06: bisz [DWORD t3, EMPTY , BYTE ZF]");
    instructionStrings13.add("000000010192FE07: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings13.add("000000010192FE08: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings13.add("000000010192FE09: stm [DWORD t3, EMPTY , DWORD t1]");
    instructionStrings13.add("0000000101930200: jcc [BYTE 1, EMPTY , DWORD 16786958]");

    final List<String> instructionStrings14 = new ArrayList<String>();
    instructionStrings14.add("0000000101930700: add [DWORD 4, DWORD esi, QWORD t0]");
    instructionStrings14.add("0000000101930701: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings14.add("0000000101930702: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings14.add("0000000101930703: str [DWORD t2, EMPTY , DWORD eax]");
    instructionStrings14.add("0000000101930A00: and [DWORD edi, DWORD 2147483648, DWORD t0]");
    instructionStrings14.add("0000000101930A01: and [DWORD eax, DWORD 2147483648, DWORD t1]");
    instructionStrings14.add("0000000101930A02: sub [DWORD edi, DWORD eax, QWORD t2]");
    instructionStrings14.add("0000000101930A03: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings14.add("0000000101930A04: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings14.add("0000000101930A05: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings14.add("0000000101930A06: xor [DWORD t0, DWORD t3, DWORD t5]");
    instructionStrings14.add("0000000101930A07: and [DWORD t4, DWORD t5, DWORD t6]");
    instructionStrings14.add("0000000101930A08: bsh [DWORD t6, DWORD -31, BYTE OF]");
    instructionStrings14.add("0000000101930A09: and [QWORD t2, QWORD 4294967296, QWORD t7]");
    instructionStrings14.add("0000000101930A0A: bsh [QWORD t7, QWORD -32, BYTE CF]");
    instructionStrings14.add("0000000101930A0B: and [QWORD t2, QWORD 4294967295, DWORD t8]");
    instructionStrings14.add("0000000101930A0C: bisz [DWORD t8, EMPTY , BYTE ZF]");
    instructionStrings14.add("0000000101930C00: bisz [BYTE ZF, EMPTY , BYTE t0]");
    instructionStrings14.add("0000000101930C01: bisz [BYTE CF, EMPTY , BYTE t1]");
    instructionStrings14.add("0000000101930C02: and [BYTE t0, BYTE t1, BYTE t2]");
    instructionStrings14.add("0000000101930C03: jcc [BYTE t2, EMPTY , DWORD 16786939]");

    final List<String> instructionStrings15 = new ArrayList<String>();
    instructionStrings15.add("0000000101931200: jcc [BYTE 1, EMPTY , DWORD 16786955]");

    final List<String> instructionStrings16 = new ArrayList<String>();
    instructionStrings16.add("000000010292ED00: add [DWORD 12, DWORD esi, QWORD t0]");
    instructionStrings16.add("000000010292ED01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings16.add("000000010292ED02: stm [DWORD eax, EMPTY , DWORD t1]");
    instructionStrings16.add("000000010292F000: jcc [BYTE 1, EMPTY , DWORD 16786906]");


    final List<String> edgeStrings = new ArrayList<String>();
    edgeStrings.add("000000010025C500 [JUMP_CONDITIONAL_TRUE]-> 000000010292ED00");
    edgeStrings.add("000000010025C500 [JUMP_CONDITIONAL_FALSE]-> 000000010025DA00");
    edgeStrings.add("000000010025DA00 [JUMP_CONDITIONAL_FALSE]-> 000000010025E500");
    edgeStrings.add("000000010025DA00 [JUMP_CONDITIONAL_TRUE]-> 000000010192EA00");
    edgeStrings.add("000000010025E500 [JUMP_CONDITIONAL_FALSE]-> 000000010025EE00");
    edgeStrings.add("000000010025E500 [JUMP_CONDITIONAL_TRUE]-> 0000000101930700");
    edgeStrings.add("000000010025EE00 [JUMP_UNCONDITIONAL]-> 000000010025F400");
    edgeStrings.add("000000010025F400 [JUMP_CONDITIONAL_TRUE]-> 0000000100261000");
    edgeStrings.add("000000010025F400 [JUMP_CONDITIONAL_FALSE]-> 000000010025F800");
    edgeStrings.add("000000010025F800 [JUMP_UNCONDITIONAL]-> 000000010025FB00");
    edgeStrings.add("000000010025FB00 [JUMP_UNCONDITIONAL]-> 0000000100260B00");
    edgeStrings.add("0000000100260B00 [JUMP_UNCONDITIONAL]-> 0000000100260E00");
    edgeStrings.add("0000000100260E00 [JUMP_UNCONDITIONAL]-> 0000000100261000");
    edgeStrings.add("000000010192EA00 [JUMP_CONDITIONAL_TRUE]-> 000000010192FA00");
    edgeStrings.add("000000010192EA00 [JUMP_CONDITIONAL_FALSE]-> 000000010192F000");
    edgeStrings.add("000000010192F000 [JUMP_UNCONDITIONAL]-> 000000010192F700");
    edgeStrings.add("000000010192F700 [JUMP_UNCONDITIONAL]-> 000000010192FA00");
    edgeStrings.add("000000010192FA00 [JUMP_UNCONDITIONAL]-> 0000000100260E00");
    edgeStrings.add("0000000101930700 [JUMP_CONDITIONAL_FALSE]-> 0000000101931200");
    edgeStrings.add("0000000101930700 [JUMP_CONDITIONAL_TRUE]-> 000000010025FB00");
    edgeStrings.add("0000000101931200 [JUMP_UNCONDITIONAL]-> 0000000100260B00");
    edgeStrings.add("000000010292ED00 [JUMP_UNCONDITIONAL]-> 000000010025DA00");

    final List<List<String>> reilBlocks = new ArrayList<List<String>>();
    reilBlocks.add(instructionStrings0);
    reilBlocks.add(instructionStrings1);
    reilBlocks.add(instructionStrings2);
    reilBlocks.add(instructionStrings3);
    reilBlocks.add(instructionStrings4);
    reilBlocks.add(instructionStrings5);
    reilBlocks.add(instructionStrings6);
    reilBlocks.add(instructionStrings7);
    reilBlocks.add(instructionStrings8);
    reilBlocks.add(instructionStrings9);
    reilBlocks.add(instructionStrings10);
    reilBlocks.add(instructionStrings11);
    reilBlocks.add(instructionStrings12);
    reilBlocks.add(instructionStrings13);
    reilBlocks.add(instructionStrings14);
    reilBlocks.add(instructionStrings15);
    reilBlocks.add(instructionStrings16);

    generateReilGraph(reilBlocks, edgeStrings);
    m_function = new ReilFunction("FOLLOWZF", m_graph1);

    final String trackedRegister = "ZF";
    conditionalJumpInstruction1 =
        new MockInstruction(Long.parseLong("10025DF", 16), "jz", operandsFirst);

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(m_function, conditionalJumpInstruction1, trackedRegister, m_options);

    final Map<IAddress, RegisterSetLatticeElement> resultMap =
        result
            .generateAddressToStateMapping(conditionalJumpInstruction1, m_options.trackIncoming());

    for (final Entry<IAddress, RegisterSetLatticeElement> resultEntry : resultMap.entrySet()) {
      if (resultEntry.getKey().toLong() == Long.parseLong("000000010025DF00", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();

        assertTrue(jzElement.getNewlyTaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getReadRegisters().isEmpty());
        assertTrue(jzElement.getTaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getUntaintedRegisters().isEmpty());
        assertTrue(jzElement.getUpdatedRegisters().isEmpty());
      }
      if (resultEntry.getKey().toLong() == Long.parseLong("000000010025DD00", 16)) {
        final RegisterSetLatticeElement cmpElement = resultEntry.getValue();

        assertTrue(cmpElement.getNewlyTaintedRegisters().contains("edi"));
        assertTrue(cmpElement.getReadRegisters().contains("ZF"));
        assertTrue(cmpElement.getTaintedRegisters().contains("edi"));
        assertTrue(cmpElement.getUntaintedRegisters().contains("ZF"));
        assertTrue(cmpElement.getUpdatedRegisters().isEmpty());
      }
      if (resultEntry.getKey().toLong() == Long.parseLong("000000010025DA00", 16)) {
        final RegisterSetLatticeElement cmpElement = resultEntry.getValue();

        assertTrue(cmpElement.getNewlyTaintedRegisters().isEmpty());
        assertTrue(cmpElement.getReadRegisters().contains("edi"));
        assertTrue(cmpElement.getTaintedRegisters().isEmpty());
        assertTrue(cmpElement.getUntaintedRegisters().contains("edi"));
        assertTrue(cmpElement.getUpdatedRegisters().isEmpty());
      }
    }

    final Map<IAddress, RegisterSetLatticeElement> perInstructionElement =
        result
            .generateAddressToStateMapping(conditionalJumpInstruction1, m_options.trackIncoming());

    for (final Entry<IAddress, RegisterSetLatticeElement> element : perInstructionElement
        .entrySet()) {
      System.out.println(element.getKey() + ":::" + element.getValue());
    }
  }

  @Test
  public void testRegisterTrackFlagDirectionUpMultiEdgeIn() {

    final MockInstruction startInstruction =
        new MockInstruction(Long.parseLong("4"), "jz", new ArrayList<MockOperandTree>());

    m_options =
        new RegisterTrackingOptions(true, new HashSet<String>(), true, AnalysisDirection.UP);

    final List<String> nop1 = new ArrayList<String>();
    nop1.add("100: nop [,,]");

    final List<String> nop2 = new ArrayList<String>();
    nop2.add("200: nop [,,]");

    final List<String> inst = new ArrayList<String>();
    inst.add("300: bisz [DWORD eax, EMPTY , BYTE ZF]");
    inst.add("400: jcc [BYTE ZF, EMPTY, DWORD 123456]");

    
    final List<List<String>> blocks = Lists.newArrayList();
    blocks.add(nop1);
    blocks.add(nop2);
    blocks.add(inst);

    final List<String> edgeStrings = new ArrayList<String>();
    edgeStrings.add("100 [JUMP_UNCONDITIONAL]-> 300");
    edgeStrings.add("200 [JUMP_UNCONDITIONAL]-> 300");

    generateReilGraph(blocks, edgeStrings);
    m_function = new ReilFunction("FOLLOWZF", m_graph1);

    final String trackedRegister = "ZF";

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(m_function, startInstruction, trackedRegister, m_options);

    final Map<IAddress, RegisterSetLatticeElement> resultMap =
        result.generateAddressToStateMapping(startInstruction, m_options.trackIncoming());

    for (final Entry<IAddress, RegisterSetLatticeElement> resultEntry : resultMap.entrySet()) {
      if (resultEntry.getKey().toLong() == Long.parseLong("100", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();

        assertTrue(jzElement.getNewlyTaintedRegisters().isEmpty());
        assertTrue(jzElement.getReadRegisters().isEmpty());
        assertTrue(jzElement.getTaintedRegisters().contains("eax"));
        assertTrue(jzElement.getUntaintedRegisters().isEmpty());
        assertTrue(jzElement.getUpdatedRegisters().isEmpty());
      }
      if (resultEntry.getKey().toLong() == Long.parseLong("200", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();

        assertTrue(jzElement.getNewlyTaintedRegisters().isEmpty());
        assertTrue(jzElement.getReadRegisters().isEmpty());
        assertTrue(jzElement.getTaintedRegisters().contains("eax"));
        assertTrue(jzElement.getUntaintedRegisters().isEmpty());
        assertTrue(jzElement.getUpdatedRegisters().isEmpty());
      }
      if (resultEntry.getKey().toLong() == Long.parseLong("300", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();

        assertTrue(jzElement.getNewlyTaintedRegisters().contains("eax"));
        assertTrue(jzElement.getReadRegisters().contains("ZF"));
        assertTrue(jzElement.getTaintedRegisters().contains("eax"));
        assertTrue(jzElement.getUntaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getUpdatedRegisters().isEmpty());
      }
      if (resultEntry.getKey().toLong() == Long.parseLong("400", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();

        assertTrue(jzElement.getNewlyTaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getReadRegisters().isEmpty());
        assertTrue(jzElement.getTaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getUntaintedRegisters().isEmpty());
        assertTrue(jzElement.getUpdatedRegisters().isEmpty());
      }
    }
  }

  @Test
  public void testFollowESIInStream() {

    final MockOperandTree operandTreeFirst = new MockOperandTree();
    operandTreeFirst.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTreeFirst.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "esi"));

    final MockOperandTree operandTreeSecond = new MockOperandTree();
    operandTreeSecond.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTreeSecond.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTreeSecond.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTreeSecond.root.m_children.get(0).m_children.get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "esp"));
    operandTreeSecond.root.m_children.get(0).m_children.get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "16"));

    final List<MockOperandTree> operandsFirst =
        Lists.newArrayList(operandTreeFirst, operandTreeSecond);

    addInstruction = new MockInstruction(Long.parseLong("58AEE4CE", 16), "add", operandsFirst);

    m_options =
        new RegisterTrackingOptions(true, new HashSet<String>(), false, AnalysisDirection.DOWN);

    final List<String> instructionStrings1 = new ArrayList<String>();

    instructionStrings1.add("00000058AEE4C100: jcc [BYTE 1, EMPTY , DWORD 1487856843]");

    final List<String> instructionStrings2 = new ArrayList<String>();

    instructionStrings2.add("00000058AEE4CB00: add [DWORD 16, DWORD esi, QWORD t0]");
    instructionStrings2.add("00000058AEE4CB01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings2.add("00000058AEE4CB02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings2.add("00000058AEE4CB03: str [DWORD t2, EMPTY , DWORD esi]");
    instructionStrings2.add("00000058AEE4CE00: add [DWORD 16, DWORD esp, QWORD t0]");
    instructionStrings2.add("00000058AEE4CE01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings2.add("00000058AEE4CE02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings2.add("00000058AEE4CE03: and [DWORD t2, DWORD 2147483648, DWORD t3]");
    instructionStrings2.add("00000058AEE4CE04: and [DWORD esi, DWORD 2147483648, DWORD t4]");
    instructionStrings2.add("00000058AEE4CE05: add [DWORD t2, DWORD esi, QWORD t5]");
    instructionStrings2.add("00000058AEE4CE06: and [QWORD t5, QWORD 2147483648, DWORD t6]");
    instructionStrings2.add("00000058AEE4CE07: bsh [DWORD t6, DWORD -31, BYTE SF]");
    instructionStrings2.add("00000058AEE4CE08: xor [DWORD t3, DWORD t4, DWORD t7]");
    instructionStrings2.add("00000058AEE4CE09: xor [DWORD t7, DWORD 2147483648, DWORD t8]");
    instructionStrings2.add("00000058AEE4CE0A: xor [DWORD t3, DWORD t6, DWORD t9]");
    instructionStrings2.add("00000058AEE4CE0B: and [DWORD t8, DWORD t9, DWORD t10]");
    instructionStrings2.add("00000058AEE4CE0C: bsh [DWORD t10, DWORD -31, DWORD OF]");
    instructionStrings2.add("00000058AEE4CE0D: and [QWORD t5, QWORD 4294967296, QWORD t11]");
    instructionStrings2.add("00000058AEE4CE0E: bsh [QWORD t11, QWORD -32, BYTE CF]");
    instructionStrings2.add("00000058AEE4CE0F: and [QWORD t5, QWORD 4294967295, DWORD t12]");
    instructionStrings2.add("00000058AEE4CE10: bisz [DWORD t12, EMPTY , BYTE ZF]");
    instructionStrings2.add("00000058AEE4CE11: str [DWORD t12, EMPTY , DWORD esi]");
    instructionStrings2.add("00000058AEE4D200: and [DWORD ebx, DWORD 2147483648, DWORD t0]");
    instructionStrings2.add("00000058AEE4D201: and [DWORD eax, DWORD 2147483648, DWORD t1]");
    instructionStrings2.add("00000058AEE4D202: add [DWORD ebx, DWORD eax, QWORD t2]");
    instructionStrings2.add("00000058AEE4D203: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings2.add("00000058AEE4D204: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings2.add("00000058AEE4D205: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings2.add("00000058AEE4D206: xor [DWORD t4, DWORD 2147483648, DWORD t5]");
    instructionStrings2.add("00000058AEE4D207: xor [DWORD t0, DWORD t3, DWORD t6]");
    instructionStrings2.add("00000058AEE4D208: and [DWORD t5, DWORD t6, DWORD t7]");
    instructionStrings2.add("00000058AEE4D209: bsh [DWORD t7, DWORD -31, DWORD OF]");
    instructionStrings2.add("00000058AEE4D20A: and [QWORD t2, QWORD 4294967296, QWORD t8]");
    instructionStrings2.add("00000058AEE4D20B: bsh [QWORD t8, QWORD -32, BYTE CF]");
    instructionStrings2.add("00000058AEE4D20C: and [QWORD t2, QWORD 4294967295, DWORD t9]");
    instructionStrings2.add("00000058AEE4D20D: bisz [DWORD t9, EMPTY , BYTE ZF]");
    instructionStrings2.add("00000058AEE4D20E: str [DWORD t9, EMPTY , DWORD eax]");
    instructionStrings2.add("00000058AEE4D400: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings2.add("00000058AEE4D401: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings2.add("00000058AEE4D402: stm [DWORD eax, EMPTY , DWORD esp]");
    instructionStrings2.add("00000058AEE4D500: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings2.add("00000058AEE4D501: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings2.add("00000058AEE4D502: stm [DWORD ebx, EMPTY , DWORD esp]");
    instructionStrings2.add("00000058AEE4D600: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings2.add("00000058AEE4D601: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings2.add("00000058AEE4D602: stm [DWORD 1487856859, EMPTY , DWORD esp]");
    instructionStrings2.add("00000058AEE4D603: jcc [DWORD 1, EMPTY , DWORD 1487855744]");

    final List<String> instructionStrings3 = Lists.newArrayList();

    instructionStrings3.add("00000058AEE08000: and [DWORD esp, DWORD 2147483648, DWORD t0]");
    instructionStrings3.add("00000058AEE08001: and [DWORD 136, DWORD 2147483648, DWORD t1]");
    instructionStrings3.add("00000058AEE08002: sub [DWORD esp, DWORD 136, QWORD t2]");
    instructionStrings3.add("00000058AEE08003: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings3.add("00000058AEE08004: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings3.add("00000058AEE08005: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings3.add("00000058AEE08006: xor [DWORD t0, DWORD t3, DWORD t5]");
    instructionStrings3.add("00000058AEE08007: and [DWORD t4, DWORD t5, DWORD t6]");
    instructionStrings3.add("00000058AEE08008: bsh [DWORD t6, DWORD -31, BYTE OF]");
    instructionStrings3.add("00000058AEE08009: and [QWORD t2, QWORD 4294967296, QWORD t7]");
    instructionStrings3.add("00000058AEE0800A: bsh [QWORD t7, QWORD -32, BYTE CF]");
    instructionStrings3.add("00000058AEE0800B: and [QWORD t2, QWORD 4294967295, DWORD t8]");
    instructionStrings3.add("00000058AEE0800C: bisz [DWORD t8, EMPTY , BYTE ZF]");
    instructionStrings3.add("00000058AEE0800D: str [DWORD t8, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE08600: ldm [DWORD 1488871424, EMPTY , DWORD t0]");
    instructionStrings3.add("00000058AEE08601: str [DWORD t0, EMPTY , DWORD eax]");
    instructionStrings3.add("00000058AEE08B00: xor [DWORD esp, DWORD eax, DWORD t0]");
    instructionStrings3.add("00000058AEE08B01: and [DWORD t0, DWORD 2147483648, DWORD t1]");
    instructionStrings3.add("00000058AEE08B02: bsh [DWORD t1, DWORD -31, BYTE SF]");
    instructionStrings3.add("00000058AEE08B03: bisz [DWORD t0, EMPTY , BYTE ZF]");
    instructionStrings3.add("00000058AEE08B04: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings3.add("00000058AEE08B05: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings3.add("00000058AEE08B06: str [DWORD t0, EMPTY , DWORD eax]");
    instructionStrings3.add("00000058AEE08D00: add [DWORD 132, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE08D01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE08D02: stm [DWORD eax, EMPTY , DWORD t1]");
    instructionStrings3.add("00000058AEE09400: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE09401: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE09402: stm [DWORD ebx, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE09500: add [DWORD 144, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE09501: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE09502: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings3.add("00000058AEE09503: str [DWORD t2, EMPTY , DWORD ebx]");
    instructionStrings3.add("00000058AEE09C00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE09C01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE09C02: stm [DWORD 129, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0A100: add [DWORD 8, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE0A101: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0A102: str [DWORD t1, EMPTY , DWORD eax]");
    instructionStrings3.add("00000058AEE0A500: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0A501: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0A502: stm [DWORD 0, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0A700: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0A701: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0A702: stm [DWORD eax, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0A800: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0A801: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0A802: stm [DWORD 1487855789, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0A803: jcc [DWORD 1, EMPTY , DWORD 1488406128]");
    instructionStrings3.add("00000058AEE0AD00: add [DWORD 32, DWORD ebx, QWORD t0]");
    instructionStrings3.add("00000058AEE0AD01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0AD02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings3.add("00000058AEE0AD03: str [DWORD t2, EMPTY , DWORD ecx]");
    instructionStrings3.add("00000058AEE0B000: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0B001: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0B002: stm [DWORD 1, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0B200: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0B201: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0B202: stm [DWORD 0, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0B400: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0B401: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0B402: stm [DWORD 15, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0B600: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0B601: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0B602: stm [DWORD 128, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0BB00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0BB01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0BB02: stm [DWORD ecx, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0BC00: add [DWORD 36, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE0BC01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0BC02: str [DWORD t1, EMPTY , DWORD edx]");
    instructionStrings3.add("00000058AEE0C000: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0C001: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0C002: stm [DWORD edx, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0C100: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0C101: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0C102: stm [DWORD edi, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0C200: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0C201: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0C202: stm [DWORD 1487855815, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0C203: jcc [DWORD 1, EMPTY , DWORD 1487799776]");
    instructionStrings3.add("00000058AEE0C700: add [DWORD 44, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE0C701: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0C702: str [DWORD t1, EMPTY , DWORD eax]");
    instructionStrings3.add("00000058AEE0CB00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0CB01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0CB02: stm [DWORD eax, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0CC00: str [DWORD 129, EMPTY , DWORD edx]");
    instructionStrings3.add("00000058AEE0D100: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0D101: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0D102: stm [DWORD 1487855830, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0D103: jcc [DWORD 1, EMPTY , DWORD 1487663360]");
    instructionStrings3.add("00000058AEE0D600: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0D601: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0D602: stm [DWORD 1, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0D800: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0D801: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0D802: stm [DWORD 0, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0DA00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0DA01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0DA02: stm [DWORD 15, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0DC00: add [DWORD 4, DWORD esi, QWORD t0]");
    instructionStrings3.add("00000058AEE0DC01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0DC02: stm [DWORD eax, EMPTY , DWORD t1]");
    instructionStrings3.add("00000058AEE0DF00: add [DWORD 8, DWORD ebx, QWORD t0]");
    instructionStrings3.add("00000058AEE0DF01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0DF02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings3.add("00000058AEE0DF03: str [DWORD t2, EMPTY , DWORD ecx]");
    instructionStrings3.add("00000058AEE0E200: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0E201: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0E202: stm [DWORD 16, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0E400: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0E401: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0E402: stm [DWORD ecx, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0E500: add [DWORD 8, DWORD esi, QWORD t0]");
    instructionStrings3.add("00000058AEE0E501: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0E502: str [DWORD t1, EMPTY , DWORD edx]");
    instructionStrings3.add("00000058AEE0E800: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0E801: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0E802: stm [DWORD edx, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0E900: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0E901: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0E902: stm [DWORD edi, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0EA00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE0EA01: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE0EA02: stm [DWORD 1487855855, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE0EA03: jcc [DWORD 1, EMPTY , DWORD 1487799776]");
    instructionStrings3.add("00000058AEE0EF00: add [DWORD 208, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE0EF01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0EF02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings3.add("00000058AEE0EF03: str [DWORD t2, EMPTY , DWORD ecx]");
    instructionStrings3.add("00000058AEE0F600: add [DWORD 220, DWORD esp, QWORD t0]");
    instructionStrings3.add("00000058AEE0F601: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings3.add("00000058AEE0F602: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings3.add("00000058AEE0F603: str [DWORD t2, EMPTY , DWORD eax]");
    instructionStrings3.add("00000058AEE0FD00: and [DWORD 72, DWORD 2147483648, DWORD t0]");
    instructionStrings3.add("00000058AEE0FD01: and [DWORD esp, DWORD 2147483648, DWORD t1]");
    instructionStrings3.add("00000058AEE0FD02: add [DWORD 72, DWORD esp, QWORD t2]");
    instructionStrings3.add("00000058AEE0FD03: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings3.add("00000058AEE0FD04: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings3.add("00000058AEE0FD05: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings3.add("00000058AEE0FD06: xor [DWORD t4, DWORD 2147483648, DWORD t5]");
    instructionStrings3.add("00000058AEE0FD07: xor [DWORD t0, DWORD t3, DWORD t6]");
    instructionStrings3.add("00000058AEE0FD08: and [DWORD t5, DWORD t6, DWORD t7]");
    instructionStrings3.add("00000058AEE0FD09: bsh [DWORD t7, DWORD -31, DWORD OF]");
    instructionStrings3.add("00000058AEE0FD0A: and [QWORD t2, QWORD 4294967296, QWORD t8]");
    instructionStrings3.add("00000058AEE0FD0B: bsh [QWORD t8, QWORD -32, BYTE CF]");
    instructionStrings3.add("00000058AEE0FD0C: and [QWORD t2, QWORD 4294967295, DWORD t9]");
    instructionStrings3.add("00000058AEE0FD0D: bisz [DWORD t9, EMPTY , BYTE ZF]");
    instructionStrings3.add("00000058AEE0FD0E: str [DWORD t9, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE10000: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings3.add("00000058AEE10001: add [DWORD esp, DWORD 4, QWORD t1]");
    instructionStrings3.add("00000058AEE10002: and [QWORD t1, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE10003: str [DWORD t0, EMPTY , DWORD ebx]");
    instructionStrings3.add("00000058AEE10100: xor [DWORD esp, DWORD ecx, DWORD t0]");
    instructionStrings3.add("00000058AEE10101: and [DWORD t0, DWORD 2147483648, DWORD t1]");
    instructionStrings3.add("00000058AEE10102: bsh [DWORD t1, DWORD -31, BYTE SF]");
    instructionStrings3.add("00000058AEE10103: bisz [DWORD t0, EMPTY , BYTE ZF]");
    instructionStrings3.add("00000058AEE10104: str [BYTE 0, EMPTY , BYTE CF]");
    instructionStrings3.add("00000058AEE10105: str [BYTE 0, EMPTY , BYTE OF]");
    instructionStrings3.add("00000058AEE10106: str [DWORD t0, EMPTY , DWORD ecx]");
    instructionStrings3.add("00000058AEE10300: stm [DWORD eax, EMPTY , DWORD esi]");
    instructionStrings3.add("00000058AEE10500: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings3.add("00000058AEE10501: and [QWORD t0, DWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE10502: stm [DWORD 1487855882, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE10503: jcc [DWORD 1, EMPTY , DWORD 1488401218]");
    instructionStrings3.add("00000058AEE10A00: and [DWORD 136, DWORD 2147483648, DWORD t0]");
    instructionStrings3.add("00000058AEE10A01: and [DWORD esp, DWORD 2147483648, DWORD t1]");
    instructionStrings3.add("00000058AEE10A02: add [DWORD 136, DWORD esp, QWORD t2]");
    instructionStrings3.add("00000058AEE10A03: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings3.add("00000058AEE10A04: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings3.add("00000058AEE10A05: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings3.add("00000058AEE10A06: xor [DWORD t4, DWORD 2147483648, DWORD t5]");
    instructionStrings3.add("00000058AEE10A07: xor [DWORD t0, DWORD t3, DWORD t6]");
    instructionStrings3.add("00000058AEE10A08: and [DWORD t5, DWORD t6, DWORD t7]");
    instructionStrings3.add("00000058AEE10A09: bsh [DWORD t7, DWORD -31, DWORD OF]");
    instructionStrings3.add("00000058AEE10A0A: and [QWORD t2, QWORD 4294967296, QWORD t8]");
    instructionStrings3.add("00000058AEE10A0B: bsh [QWORD t8, QWORD -32, BYTE CF]");
    instructionStrings3.add("00000058AEE10A0C: and [QWORD t2, QWORD 4294967295, DWORD t9]");
    instructionStrings3.add("00000058AEE10A0D: bisz [DWORD t9, EMPTY , BYTE ZF]");
    instructionStrings3.add("00000058AEE10A0E: str [DWORD t9, EMPTY , DWORD esp]");
    instructionStrings3.add("00000058AEE11000: ldm [DWORD esp, EMPTY , DWORD t0]");
    instructionStrings3.add("00000058AEE11001: add [DWORD esp, DWORD 4, QWORD t1]");
    instructionStrings3.add("00000058AEE11002: and [QWORD t1, QWORD 4294967295, DWORD esp]");
    instructionStrings3.add("00000058AEE11003: jcc [DWORD 1, EMPTY , DWORD t0]");

    final List<String> instructionStrings4 = Lists.newArrayList();

    instructionStrings4.add("00000058AEE4DB00: add [DWORD 24, DWORD esp, QWORD t0]");
    instructionStrings4.add("00000058AEE4DB01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings4.add("00000058AEE4DB02: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings4.add("00000058AEE4DB03: and [DWORD 24, DWORD 2147483648, DWORD t3]");
    instructionStrings4.add("00000058AEE4DB04: and [DWORD t2, DWORD 2147483648, DWORD t4]");
    instructionStrings4.add("00000058AEE4DB05: add [DWORD 24, DWORD t2, QWORD t5]");
    instructionStrings4.add("00000058AEE4DB06: and [QWORD t5, QWORD 2147483648, DWORD t6]");
    instructionStrings4.add("00000058AEE4DB07: bsh [DWORD t6, DWORD -31, BYTE SF]");
    instructionStrings4.add("00000058AEE4DB08: xor [DWORD t3, DWORD t4, DWORD t7]");
    instructionStrings4.add("00000058AEE4DB09: xor [DWORD t7, DWORD 2147483648, DWORD t8]");
    instructionStrings4.add("00000058AEE4DB0A: xor [DWORD t3, DWORD t6, DWORD t9]");
    instructionStrings4.add("00000058AEE4DB0B: and [DWORD t8, DWORD t9, DWORD t10]");
    instructionStrings4.add("00000058AEE4DB0C: bsh [DWORD t10, DWORD -31, DWORD OF]");
    instructionStrings4.add("00000058AEE4DB0D: and [QWORD t5, QWORD 4294967296, QWORD t11]");
    instructionStrings4.add("00000058AEE4DB0E: bsh [QWORD t11, QWORD -32, BYTE CF]");
    instructionStrings4.add("00000058AEE4DB0F: and [QWORD t5, QWORD 4294967295, DWORD t12]");
    instructionStrings4.add("00000058AEE4DB10: bisz [DWORD t12, EMPTY , BYTE ZF]");
    instructionStrings4.add("00000058AEE4DB11: stm [DWORD t12, EMPTY , DWORD t1]");
    instructionStrings4.add("00000058AEE4E000: and [DWORD 8, DWORD 2147483648, DWORD t0]");
    instructionStrings4.add("00000058AEE4E001: and [DWORD esp, DWORD 2147483648, DWORD t1]");
    instructionStrings4.add("00000058AEE4E002: add [DWORD 8, DWORD esp, QWORD t2]");
    instructionStrings4.add("00000058AEE4E003: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings4.add("00000058AEE4E004: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings4.add("00000058AEE4E005: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings4.add("00000058AEE4E006: xor [DWORD t4, DWORD 2147483648, DWORD t5]");
    instructionStrings4.add("00000058AEE4E007: xor [DWORD t0, DWORD t3, DWORD t6]");
    instructionStrings4.add("00000058AEE4E008: and [DWORD t5, DWORD t6, DWORD t7]");
    instructionStrings4.add("00000058AEE4E009: bsh [DWORD t7, DWORD -31, DWORD OF]");
    instructionStrings4.add("00000058AEE4E00A: and [QWORD t2, QWORD 4294967296, QWORD t8]");
    instructionStrings4.add("00000058AEE4E00B: bsh [QWORD t8, QWORD -32, BYTE CF]");
    instructionStrings4.add("00000058AEE4E00C: and [QWORD t2, QWORD 4294967295, DWORD t9]");
    instructionStrings4.add("00000058AEE4E00D: bisz [DWORD t9, EMPTY , BYTE ZF]");
    instructionStrings4.add("00000058AEE4E00E: str [DWORD t9, EMPTY , DWORD esp]");
    instructionStrings4.add("00000058AEE4E300: and [DWORD 40, DWORD 2147483648, DWORD t0]");
    instructionStrings4.add("00000058AEE4E301: and [DWORD ebx, DWORD 2147483648, DWORD t1]");
    instructionStrings4.add("00000058AEE4E302: add [DWORD 40, DWORD ebx, QWORD t2]");
    instructionStrings4.add("00000058AEE4E303: and [QWORD t2, QWORD 2147483648, DWORD t3]");
    instructionStrings4.add("00000058AEE4E304: bsh [DWORD t3, DWORD -31, BYTE SF]");
    instructionStrings4.add("00000058AEE4E305: xor [DWORD t0, DWORD t1, DWORD t4]");
    instructionStrings4.add("00000058AEE4E306: xor [DWORD t4, DWORD 2147483648, DWORD t5]");
    instructionStrings4.add("00000058AEE4E307: xor [DWORD t0, DWORD t3, DWORD t6]");
    instructionStrings4.add("00000058AEE4E308: and [DWORD t5, DWORD t6, DWORD t7]");
    instructionStrings4.add("00000058AEE4E309: bsh [DWORD t7, DWORD -31, DWORD OF]");
    instructionStrings4.add("00000058AEE4E30A: and [QWORD t2, QWORD 4294967296, QWORD t8]");
    instructionStrings4.add("00000058AEE4E30B: bsh [QWORD t8, QWORD -32, BYTE CF]");
    instructionStrings4.add("00000058AEE4E30C: and [QWORD t2, QWORD 4294967295, DWORD t9]");
    instructionStrings4.add("00000058AEE4E30D: bisz [DWORD t9, EMPTY , BYTE ZF]");
    instructionStrings4.add("00000058AEE4E30E: str [DWORD t9, EMPTY , DWORD ebx]");
    instructionStrings4.add("00000058AEE4E600: add [DWORD 20, DWORD esp, QWORD t0]");
    instructionStrings4.add("00000058AEE4E601: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings4.add("00000058AEE4E602: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings4.add("00000058AEE4E603: and [DWORD t2, DWORD 2147483648, DWORD t3]");
    instructionStrings4.add("00000058AEE4E604: and [DWORD 1, DWORD 2147483648, DWORD t4]");
    instructionStrings4.add("00000058AEE4E605: sub [DWORD t2, DWORD 1, QWORD t5]");
    instructionStrings4.add("00000058AEE4E606: and [QWORD t5, QWORD 2147483648, DWORD t6]");
    instructionStrings4.add("00000058AEE4E607: bsh [DWORD t6, DWORD -31, BYTE SF]");
    instructionStrings4.add("00000058AEE4E608: xor [DWORD t3, DWORD t4, DWORD t7]");
    instructionStrings4.add("00000058AEE4E609: xor [DWORD t3, DWORD t6, DWORD t8]");
    instructionStrings4.add("00000058AEE4E60A: and [DWORD t7, DWORD t8, DWORD t9]");
    instructionStrings4.add("00000058AEE4E60B: bsh [DWORD t9, DWORD -31, BYTE OF]");
    instructionStrings4.add("00000058AEE4E60C: and [QWORD t5, QWORD 4294967296, QWORD t10]");
    instructionStrings4.add("00000058AEE4E60D: bsh [QWORD t10, QWORD -32, BYTE CF]");
    instructionStrings4.add("00000058AEE4E60E: and [QWORD t5, QWORD 4294967295, DWORD t11]");
    instructionStrings4.add("00000058AEE4E60F: bisz [DWORD t11, EMPTY , BYTE ZF]");
    instructionStrings4.add("00000058AEE4E610: stm [DWORD t11, EMPTY , DWORD t1]");
    instructionStrings4.add("00000058AEE4EB00: bisz [BYTE ZF, EMPTY , BYTE t0]");
    instructionStrings4.add("00000058AEE4EB01: jcc [BYTE t0, EMPTY , DWORD 1487856835]");

    final List<String> instructionStrings5 = Lists.newArrayList();

    instructionStrings5.add("00000058AEE4C300: add [DWORD 28, DWORD esp, QWORD t0]");
    instructionStrings5.add("00000058AEE4C301: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings5.add("00000058AEE4C302: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings5.add("00000058AEE4C303: str [DWORD t2, EMPTY , DWORD eax]");
    instructionStrings5.add("00000058AEE4C700: add [DWORD 32, DWORD esp, QWORD t0]");
    instructionStrings5.add("00000058AEE4C701: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings5.add("00000058AEE4C702: ldm [DWORD t1, EMPTY , DWORD t2]");
    instructionStrings5.add("00000058AEE4C703: str [DWORD t2, EMPTY , DWORD esi]");

    final List<String> instructionStrings6 = Lists.newArrayList();

    instructionStrings6.add("00000058AEE4ED00: sub [DWORD esp, DWORD 4, QWORD t0]");
    instructionStrings6.add("00000058AEE4ED01: and [QWORD t0, DWORD 4294967295, DWORD esp]");

    final List<List<String>> reilBlocks = new ArrayList<List<String>>();
    reilBlocks.add(instructionStrings1);
    reilBlocks.add(instructionStrings2);
    reilBlocks.add(instructionStrings3);
    reilBlocks.add(instructionStrings4);
    reilBlocks.add(instructionStrings5);
    reilBlocks.add(instructionStrings6);

    final List<String> edgeStrings = new ArrayList<String>();
    edgeStrings.add("00000058AEE4C100 [JUMP_UNCONDITIONAL]-> 00000058AEE4CB00");
    edgeStrings.add("00000058AEE4CB00 [ENTER_INLINED_FUNCTION]-> 00000058AEE08000");
    edgeStrings.add("00000058AEE08000 [LEAVE_INLINED_FUNCTION]-> 00000058AEE4DB00");
    edgeStrings.add("00000058AEE4DB00 [JUMP_CONDITIONAL_TRUE]-> 00000058AEE4C300");
    edgeStrings.add("00000058AEE4DB00 [JUMP_CONDITIONAL_FALSE]-> 00000058AEE4ED00");
    edgeStrings.add("00000058AEE4C300 [JUMP_UNCONDITIONAL_LOOP]-> 00000058AEE4CB00");

    generateReilGraph(reilBlocks, edgeStrings);
    m_function = new ReilFunction("FOLLOWESI", m_graph1);

    final String trackedRegister = "esi";

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(m_function, addInstruction, trackedRegister, m_options);

    final Map<IAddress, RegisterSetLatticeElement> resultMap =
        result.generateAddressToStateMapping(addInstruction, m_options.trackIncoming());

    System.out.println(m_graph1.toString());

    for (final Entry<IAddress, RegisterSetLatticeElement> resultEntry : resultMap.entrySet()) {
      System.out.println(" KEY: " + resultEntry.getKey() + " VALUE: "
          + resultEntry.getValue().toString());

      if (resultEntry.getKey().toLong() == Long.parseLong("0000058AEE4CE00", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();
        assertTrue(jzElement.getTaintedRegisters().contains("esi"));
      }
    }
  }

  @Test
  public void testTransformFollowZFinStream1() {

    final MockOperandTree operandTreeFirst1 = new MockOperandTree();
    operandTreeFirst1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTreeFirst1.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "16827245"));

    final List<MockOperandTree> operandsFirst = Lists.newArrayList(operandTreeFirst1);

    conditionalJumpInstruction1 =
        new MockInstruction(Long.parseLong("100C32F", 16), "jz", operandsFirst);

    m_options =
        new RegisterTrackingOptions(true, new HashSet<String>(), true, AnalysisDirection.UP);

    final List<String> instructionStrings1 = new ArrayList<String>();

    // cmp
    instructionStrings1.add("0000000100C32C00: add [DWORD 12, DWORD ebp, QWORD t0]");
    instructionStrings1.add("0000000100C32C01: and [QWORD t0, DWORD 4294967295, DWORD t1]");
    instructionStrings1.add("0000000100C32C02: ldm [DWORD t1, EMPTY , BYTE t2]");
    instructionStrings1.add("0000000100C32C03: and [DWORD ebx, BYTE 255, BYTE t4]");
    instructionStrings1.add("0000000100C32C04: and [BYTE t2, BYTE 128, BYTE t5]");
    instructionStrings1.add("0000000100C32C05: and [BYTE t4, BYTE 128, BYTE t6]");
    instructionStrings1.add("0000000100C32C06: sub [BYTE t2, BYTE t4, WORD t7]");
    instructionStrings1.add("0000000100C32C07: and [WORD t7, WORD 128, BYTE t8]");
    instructionStrings1.add("0000000100C32C08: bsh [BYTE t8, BYTE -7, BYTE SF]");
    instructionStrings1.add("0000000100C32C09: xor [BYTE t5, BYTE t6, BYTE t9]");
    instructionStrings1.add("0000000100C32C0A: xor [BYTE t5, BYTE t8, BYTE t10]");
    instructionStrings1.add("0000000100C32C0B: and [BYTE t9, BYTE t10, BYTE t11]");
    instructionStrings1.add("0000000100C32C0C: bsh [BYTE t11, BYTE -7, BYTE OF]");
    instructionStrings1.add("0000000100C32C0D: and [WORD t7, WORD 256, WORD t12]");
    instructionStrings1.add("0000000100C32C0E: bsh [WORD t12, WORD -8, BYTE CF]");
    instructionStrings1.add("0000000100C32C0F: and [WORD t7, WORD 255, BYTE t13]");
    instructionStrings1.add("0000000100C32C10: bisz [BYTE t13, EMPTY , BYTE ZF]");
    // jz
    instructionStrings1.add("0000000100C32F00: jcc [BYTE ZF, EMPTY , DWORD 16827245]");

    final List<List<String>> reilBlocks = new ArrayList<List<String>>();
    reilBlocks.add(instructionStrings1);

    generateReilGraph(reilBlocks, new ArrayList<String>());
    m_function = new ReilFunction("FOLLOWZF", m_graph1);

    final String trackedRegister = "ZF";

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(m_function, conditionalJumpInstruction1, trackedRegister, m_options);

    final Map<IAddress, RegisterSetLatticeElement> resultMap =
        result
            .generateAddressToStateMapping(conditionalJumpInstruction1, m_options.trackIncoming());

    System.out.println(m_graph1.toString());

    for (final Entry<IAddress, RegisterSetLatticeElement> resultEntry : resultMap.entrySet()) {
      if (resultEntry.getKey().toLong() == Long.parseLong("0000000100C32F00", 16)) {
        final RegisterSetLatticeElement jzElement = resultEntry.getValue();

        assertTrue(jzElement.getNewlyTaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getReadRegisters().isEmpty());
        assertTrue(jzElement.getTaintedRegisters().contains("ZF"));
        assertTrue(jzElement.getUntaintedRegisters().isEmpty());
        assertTrue(jzElement.getUpdatedRegisters().isEmpty());

      }
      if (resultEntry.getKey().toLong() == Long.parseLong("0000000100C32C00", 16)) {
        final RegisterSetLatticeElement cmpElement = resultEntry.getValue();

        assertTrue(cmpElement.getNewlyTaintedRegisters().contains("ebx"));
        assertTrue(cmpElement.getReadRegisters().contains("ZF"));
        assertTrue(cmpElement.getTaintedRegisters().contains("ebx"));
        assertTrue(cmpElement.getUntaintedRegisters().contains("ZF"));
        assertTrue(cmpElement.getUpdatedRegisters().isEmpty());
      }
    }
  }
}
