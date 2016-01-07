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
package com.google.security.zynamics.reil.translators.ppc;
/*
 * package com.google.security.zynamics.reil.translators.ppc;
 * 
 * import java.util.List;
 * 
 * import com.google.security.zynamics.reil.OperandSize; import com.google.security.zynamics.reil.ReilHelpers; import
 * com.google.security.zynamics.reil.ReilInstruction; import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
 * import com.google.security.zynamics.reil.translators.InternalTranslationException; import
 * com.google.security.zynamics.zylib.disassembly.IInstruction; import
 * com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
 * 
 * public class LswxTranslator { public void translate(final ITranslationEnvironment environment,
 * final IInstruction instruction, final List<ReilInstruction> instructions) throws
 * InternalTranslationException { Preconditions.checkNotNull(environment,
 * "Error: Argument environment can't be null");
 * 
 * Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
 * 
 * Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");
 * 
 * final IOperandTreeNode countRegister =
 * instruction.getOperands().get(0).getRootNode().getChildren().get(0); final IOperandTreeNode
 * addressRegister1 = instruction.getOperands().get(1).getRootNode().getChildren().get(0); final
 * IOperandTreeNode addressRegister2 =
 * instruction.getOperands().get(2).getRootNode().getChildren().get(0);
 * 
 * 
 * final OperandSize dw = OperandSize.OPERAND_SIZE_DWORD; final OperandSize qw =
 * OperandSize.OPERAND_SIZE_QWORD;
 * 
 * final String tmpEffectiveAddress = environment.getNextVariableString(); final String
 * effectiveAddress = environment.getNextVariableString(); final String tmpData =
 * environment.getNextVariableString();
 * 
 * Long baseOffset = instruction.getAddress().toLong() * 0x100;
 * 
 * // always compute effective address instructions.add(ReilHelpers.createAdd(baseOffset++, dw,
 * addressRegister1.getValue(), dw, addressRegister2.getValue(), qw, tmpEffectiveAddress));
 * instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpEffectiveAddress, dw,
 * String.valueOf(0xFFFFFFFFL), dw, effectiveAddress));
 * 
 * int r = Helpers.getRegisterIndex(countRegister.getValue()) - 1;
 * 
 * // extract number of Bytes //int n = numBytes.getValue() == "0" ? 32 :
 * Integer.decode(numBytes.getValue()); while ( n > 0 ) { // increment target register r = (r +
 * 1)%32;
 * 
 * // do load instructions.add(ReilHelpers.createLdm(baseOffset++, dw, effectiveAddress, dw,
 * tmpData));
 * 
 * if ( n == 3 ) { instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpData, dw,
 * String.valueOf(0xFFFFFF00L), dw, "r" + r)); } else if ( n == 2 ) {
 * instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpData, dw,
 * String.valueOf(0xFFFF0000L), dw, "r" + r)); } else if ( n == 1 ) {
 * instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpData, dw,
 * String.valueOf(0xFF000000L), dw, "r" + r)); } else {
 * instructions.add(ReilHelpers.createStr(baseOffset++, dw, tmpData, dw, "r" + r));
 * instructions.add(ReilHelpers.createAdd(baseOffset++, dw, effectiveAddress, dw,
 * String.valueOf(4L), dw, effectiveAddress)); } n -= 4; } } }
 */
