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
package com.google.security.zynamics.reil.translators.mips;

import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;

import java.util.List;


public class OperandLoader {
  public static Pair<IOperandTreeNode, IOperandTreeNode> load(final IOperandTreeNode root)
      throws InternalTranslationException {

    final IOperandTreeNode base = root.getChildren().get(0);

    if (base.getType() == ExpressionType.MEMDEREF) {
      return new Pair<IOperandTreeNode, IOperandTreeNode>(base.getChildren().get(0), null);
    }

    if (base.getType() != ExpressionType.OPERATOR) {
      throw new InternalTranslationException("Malformed operand");
    }

    if (base.getChildren().size() != 2) {
      throw new InternalTranslationException("Malformed operand");
    }

    return new Pair<IOperandTreeNode, IOperandTreeNode>(base.getChildren().get(0), base
        .getChildren().get(1));
  }

  public static Triple<IOperandTree, IOperandTree, IOperandTree> loadDuplicateFirst(
      final IInstruction instruction) {
    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree operand1 = operands.get(0);
    final IOperandTree operand2 = operands.size() == 2 ? operands.get(0) : operands.get(1);
    final IOperandTree operand3 = operands.size() == 2 ? operands.get(1) : operands.get(2);

    return new Triple<IOperandTree, IOperandTree, IOperandTree>(operand1, operand2, operand3);
  }
}
