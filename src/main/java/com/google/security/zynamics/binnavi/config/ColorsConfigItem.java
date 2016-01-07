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
package com.google.security.zynamics.binnavi.config;

import com.google.security.zynamics.common.config.AbstractConfigItem;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

import java.awt.Color;

public class ColorsConfigItem extends AbstractConfigItem {
  private static final String ADDRESS_COLOR = "AddressColor";
  private static final Color ADDRESS_COLOR_DEFAULT = new Color(-16777216);
  private Color addressColor = ADDRESS_COLOR_DEFAULT;

  private static final String MNEMONIC_COLOR = "MnemonicColor";
  private static final Color MNEMONIC_COLOR_DEFAULT = new Color(-14276960);
  private Color mnemonicColor = MNEMONIC_COLOR_DEFAULT;

  private static final String STRING_COLOR = "StringColor";
  private static final Color STRING_COLOR_DEFAULT = Color.BLACK;
  private Color stringColor = STRING_COLOR_DEFAULT;

  private static final String MEM_REF_COLOR = "MemRefColor";
  private static final Color MEM_REF_COLOR_DEFAULT = new Color(-16777216);
  private Color memRefColor = MEM_REF_COLOR_DEFAULT;

  private static final String OPERATOR_COLOR = "OperatorColor";
  private static final Color OPERATOR_COLOR_DEFAULT = new Color(-16777216);
  private Color operatorColor = OPERATOR_COLOR_DEFAULT;

  private static final String PREFIX_COLOR = "PrefixColor";
  private static final Color PREFIX_COLOR_DEFAULT = new Color(-8355712);
  private Color prefixColor = PREFIX_COLOR_DEFAULT;

  private static final String IMMEDIATE_COLOR = "ImmediateColor";
  private static final Color IMMEDIATE_COLOR_DEFAULT = new Color(-6270816);
  private Color immediateColor = IMMEDIATE_COLOR_DEFAULT;

  private static final String REGISTER_COLOR = "RegisterColor";
  private static final Color REGISTER_COLOR_DEFAULT = new Color(-16750615);
  private Color registerColor = REGISTER_COLOR_DEFAULT;

  private static final String FUNCTION_COLOR = "FunctionColor";
  private static final Color FUNCTION_COLOR_DEFAULT = new Color(6947071);
  private Color functionColor = FUNCTION_COLOR_DEFAULT;

  private static final String VARIABLE_COLOR = "VariableColor";
  private static final Color VARIABLE_COLOR_DEFAULT = new Color(4817457);
  private Color variableColor = VARIABLE_COLOR_DEFAULT;

  private static final String EXPRESSION_LIST_COLOR = "ExpressionListColor";
  private static final Color EXPRESSION_LIST_COLOR_DEFAULT = new Color(-16777216);
  private Color expressionListColor = EXPRESSION_LIST_COLOR_DEFAULT;

  private static final String NO_TYPE_COLOR = "NoTypeColor";
  private static final Color NO_TYPE_COLOR_DEFAULT = Color.BLACK;
  private Color noTypeColor = NO_TYPE_COLOR_DEFAULT;

  private static final String INVALID_TYPE_COLOR = "InvalidTypeColor";
  private static final Color INVALID_TYPE_COLOR_DEFAULT = Color.BLACK;
  private Color invalidTypeColor = INVALID_TYPE_COLOR_DEFAULT;

  private static final String OPERAND_SEPARATOR_COLOR = "OperandSeperatorColor";
  private static final Color OPERAND_SEPARATOR_COLOR_DEFAULT = new Color(-16777216);
  private Color operandSeperatorColor = OPERAND_SEPARATOR_COLOR_DEFAULT;

  private static final String GLOBAL_COMMENT_COLOR = "GlobalCommentColor";
  private static final Color GLOBAL_COMMENT_COLOR_DEFAULT = Color.BLACK;
  private Color globalCommentColor = GLOBAL_COMMENT_COLOR_DEFAULT;

  private static final String LOCAL_COMMENT_COLOR = "LocalCommentColor";
  private static final Color LOCAL_COMMENT_COLOR_DEFAULT = Color.BLACK;
  private Color localCommentColor = LOCAL_COMMENT_COLOR_DEFAULT;

  private static final String NORMAL_FUNCTION_COLOR = "NormalFunctionColor";
  private static final Color NORMAL_FUNCTION_COLOR_DEFAULT = new Color(-131587);
  private Color normalFunctionColor = NORMAL_FUNCTION_COLOR_DEFAULT;

  private static final String LIBRARY_FUNCTION_COLOR = "LibraryFunctionColor";
  private static final Color LIBRARY_FUNCTION_COLOR_DEFAULT = new Color(-986886);
  private Color libraryFunctionColor = LIBRARY_FUNCTION_COLOR_DEFAULT;

  private static final String IMPORTED_FUNCTION_COLOR = "ImportedFunctionColor";
  private static final Color IMPORTED_FUNCTION_COLOR_DEFAULT = new Color(-331536);
  private Color importedFunctionColor = IMPORTED_FUNCTION_COLOR_DEFAULT;

  private static final String THUNK_FUNCTION_COLOR = "ThunkFunctionColor";
  private static final Color THUNK_FUNCTION_COLOR_DEFAULT = new Color(-984336);
  private Color thunkFunctionColor = THUNK_FUNCTION_COLOR_DEFAULT;

  private static final String ADJUSTOR_THUNK_FUNCTION_COLOR = "AdjustorThunkFunctionColor";
  private static final Color ADJUSTOR_THUNK_FUNCTION_COLOR_DEFAULT = new Color(-2237477);
  private Color adjustorThunkFunctionColor = ADJUSTOR_THUNK_FUNCTION_COLOR_DEFAULT;

  private static final String UNKNOWN_FUNCTION_COLOR = "UnknownFunctionColor";
  private static final Color UNKNOWN_FUNCTION_COLOR_DEFAULT = new Color(-1644826);
  private Color unknownFunctionColor = UNKNOWN_FUNCTION_COLOR_DEFAULT;

  private static final String BASIC_BLOCKS_COLOR = "BasicBlocksColor";
  private static final Color BASIC_BLOCKS_COLOR_DEFAULT = new Color(-68902);
  private Color basicBlocksColor = BASIC_BLOCKS_COLOR_DEFAULT;

  private static final String CONDITIONAL_JUMP_TRUE_COLOR = "ConditionalJumpTrueColor";
  private static final Color CONDITIONAL_JUMP_TRUE_COLOR_DEFAULT = new Color(-16736256);
  private Color conditionalJumpTrueColor = CONDITIONAL_JUMP_TRUE_COLOR_DEFAULT;

  private static final String CONDITIONAL_JUMP_FALSE_COLOR = "ConditionalJumpFalseColor";
  private static final Color CONDITIONAL_JUMP_FALSE_COLOR_DEFAULT = new Color(-6291456);
  private Color conditionalJumpFalseColor = CONDITIONAL_JUMP_FALSE_COLOR_DEFAULT;

  private static final String UNCONDITIONAL_JUMP_COLOR = "UnconditionalJumpColor";
  private static final Color UNCONDITIONAL_JUMP_COLOR_DEFAULT = Color.BLACK;
  private Color unconditionalJumpColor = UNCONDITIONAL_JUMP_COLOR_DEFAULT;

  private static final String SWITCH_JUMP_COLOR = "SwitchJumpColor";
  private static final Color SWITCH_JUMP_COLOR_DEFAULT = Color.BLACK;
  private Color switchJumpColor = SWITCH_JUMP_COLOR_DEFAULT;

  private static final String TEXT_EDGE_COLOR = "TextEdgeColor";
  private static final Color TEXT_EDGE_COLOR_DEFAULT = Color.BLACK;
  private Color textEdgeColor = TEXT_EDGE_COLOR_DEFAULT;

  private static final String ENTER_INLINED_JUMP_COLOR = "EnterInlinedJumpColor";
  private static final Color ENTER_INLINED_JUMP_COLOR_DEFAULT = new Color(-3360768);
  private Color enterInlinedJumpColor = ENTER_INLINED_JUMP_COLOR_DEFAULT;

  private static final String LEAVE_INLINED_JUMP_COLOR = "LeaveInlinedJumpColor";
  private static final Color LEAVE_INLINED_JUMP_COLOR_DEFAULT = new Color(-3360768);
  private Color leaveInlinedJumpColor = LEAVE_INLINED_JUMP_COLOR_DEFAULT;

  private static final String TAG_COLOR = "TagColor";
  private static final Color TAG_COLOR_DEFAULT = Color.BLACK;
  private Color tagColor = TAG_COLOR_DEFAULT;

  @Override
  public void load(final TypedPropertiesWrapper properties) {
    addressColor = properties.getColor(ADDRESS_COLOR, ADDRESS_COLOR_DEFAULT);
    mnemonicColor = properties.getColor(MNEMONIC_COLOR, MNEMONIC_COLOR_DEFAULT);
    stringColor = properties.getColor(STRING_COLOR, STRING_COLOR_DEFAULT);
    memRefColor = properties.getColor(MEM_REF_COLOR, MEM_REF_COLOR_DEFAULT);
    operatorColor = properties.getColor(OPERATOR_COLOR, OPERATOR_COLOR_DEFAULT);
    prefixColor = properties.getColor(PREFIX_COLOR, PREFIX_COLOR_DEFAULT);
    immediateColor = properties.getColor(IMMEDIATE_COLOR, IMMEDIATE_COLOR_DEFAULT);
    registerColor = properties.getColor(REGISTER_COLOR, REGISTER_COLOR_DEFAULT);
    functionColor = properties.getColor(FUNCTION_COLOR, FUNCTION_COLOR_DEFAULT);
    variableColor = properties.getColor(VARIABLE_COLOR, VARIABLE_COLOR_DEFAULT);
    expressionListColor = properties.getColor(EXPRESSION_LIST_COLOR, EXPRESSION_LIST_COLOR_DEFAULT);
    noTypeColor = properties.getColor(NO_TYPE_COLOR, NO_TYPE_COLOR_DEFAULT);
    invalidTypeColor = properties.getColor(INVALID_TYPE_COLOR, INVALID_TYPE_COLOR_DEFAULT);
    operandSeperatorColor =
        properties.getColor(OPERAND_SEPARATOR_COLOR, OPERAND_SEPARATOR_COLOR_DEFAULT);
    globalCommentColor = properties.getColor(GLOBAL_COMMENT_COLOR, GLOBAL_COMMENT_COLOR_DEFAULT);
    localCommentColor = properties.getColor(LOCAL_COMMENT_COLOR, LOCAL_COMMENT_COLOR_DEFAULT);
    normalFunctionColor = properties.getColor(NORMAL_FUNCTION_COLOR, NORMAL_FUNCTION_COLOR_DEFAULT);
    libraryFunctionColor =
        properties.getColor(LIBRARY_FUNCTION_COLOR, LIBRARY_FUNCTION_COLOR_DEFAULT);
    importedFunctionColor =
        properties.getColor(IMPORTED_FUNCTION_COLOR, IMPORTED_FUNCTION_COLOR_DEFAULT);
    thunkFunctionColor = properties.getColor(THUNK_FUNCTION_COLOR, THUNK_FUNCTION_COLOR_DEFAULT);
    adjustorThunkFunctionColor =
        properties.getColor(ADJUSTOR_THUNK_FUNCTION_COLOR, ADJUSTOR_THUNK_FUNCTION_COLOR_DEFAULT);
    unknownFunctionColor =
        properties.getColor(UNKNOWN_FUNCTION_COLOR, UNKNOWN_FUNCTION_COLOR_DEFAULT);
    basicBlocksColor = properties.getColor(BASIC_BLOCKS_COLOR, BASIC_BLOCKS_COLOR_DEFAULT);
    conditionalJumpTrueColor =
        properties.getColor(CONDITIONAL_JUMP_TRUE_COLOR, CONDITIONAL_JUMP_TRUE_COLOR_DEFAULT);
    conditionalJumpFalseColor =
        properties.getColor(CONDITIONAL_JUMP_FALSE_COLOR, CONDITIONAL_JUMP_FALSE_COLOR_DEFAULT);
    unconditionalJumpColor =
        properties.getColor(UNCONDITIONAL_JUMP_COLOR, UNCONDITIONAL_JUMP_COLOR_DEFAULT);
    switchJumpColor = properties.getColor(SWITCH_JUMP_COLOR, SWITCH_JUMP_COLOR_DEFAULT);
    textEdgeColor = properties.getColor(TEXT_EDGE_COLOR, TEXT_EDGE_COLOR_DEFAULT);
    enterInlinedJumpColor =
        properties.getColor(ENTER_INLINED_JUMP_COLOR, ENTER_INLINED_JUMP_COLOR_DEFAULT);
    leaveInlinedJumpColor =
        properties.getColor(LEAVE_INLINED_JUMP_COLOR, LEAVE_INLINED_JUMP_COLOR_DEFAULT);
    tagColor = properties.getColor(TAG_COLOR, TAG_COLOR_DEFAULT);
  }

  @Override
  public void store(final TypedPropertiesWrapper properties) {
    properties.setColor(ADDRESS_COLOR, addressColor);
    properties.setColor(MNEMONIC_COLOR, mnemonicColor);
    properties.setColor(STRING_COLOR, stringColor);
    properties.setColor(MEM_REF_COLOR, memRefColor);
    properties.setColor(OPERATOR_COLOR, operatorColor);
    properties.setColor(PREFIX_COLOR, prefixColor);
    properties.setColor(IMMEDIATE_COLOR, immediateColor);
    properties.setColor(REGISTER_COLOR, registerColor);
    properties.setColor(FUNCTION_COLOR, functionColor);
    properties.setColor(VARIABLE_COLOR, variableColor);
    properties.setColor(EXPRESSION_LIST_COLOR, expressionListColor);
    properties.setColor(NO_TYPE_COLOR, noTypeColor);
    properties.setColor(INVALID_TYPE_COLOR, invalidTypeColor);
    properties.setColor(OPERAND_SEPARATOR_COLOR, operandSeperatorColor);
    properties.setColor(GLOBAL_COMMENT_COLOR, globalCommentColor);
    properties.setColor(LOCAL_COMMENT_COLOR, localCommentColor);
    properties.setColor(NORMAL_FUNCTION_COLOR, normalFunctionColor);
    properties.setColor(LIBRARY_FUNCTION_COLOR, libraryFunctionColor);
    properties.setColor(IMPORTED_FUNCTION_COLOR, importedFunctionColor);
    properties.setColor(THUNK_FUNCTION_COLOR, thunkFunctionColor);
    properties.setColor(ADJUSTOR_THUNK_FUNCTION_COLOR, adjustorThunkFunctionColor);
    properties.setColor(UNKNOWN_FUNCTION_COLOR, unknownFunctionColor);
    properties.setColor(BASIC_BLOCKS_COLOR, basicBlocksColor);
    properties.setColor(CONDITIONAL_JUMP_TRUE_COLOR, conditionalJumpTrueColor);
    properties.setColor(CONDITIONAL_JUMP_FALSE_COLOR, conditionalJumpFalseColor);
    properties.setColor(UNCONDITIONAL_JUMP_COLOR, unconditionalJumpColor);
    properties.setColor(SWITCH_JUMP_COLOR, switchJumpColor);
    properties.setColor(TEXT_EDGE_COLOR, textEdgeColor);
    properties.setColor(ENTER_INLINED_JUMP_COLOR, enterInlinedJumpColor);
    properties.setColor(LEAVE_INLINED_JUMP_COLOR, leaveInlinedJumpColor);
    properties.setColor(TAG_COLOR, tagColor);
  }

  public Color getAddressColor() {
    return addressColor;
  }

  public void setAddressColor(final Color value) {
    this.addressColor = value;
  }

  public Color getMnemonicColor() {
    return mnemonicColor;
  }

  public void setMnemonicColor(final Color value) {
    this.mnemonicColor = value;
  }

  public Color getStringColor() {
    return stringColor;
  }

  public void setStringColor(final Color value) {
    this.stringColor = value;
  }

  public Color getMemRefColor() {
    return memRefColor;
  }

  public void setMemRefColor(final Color value) {
    this.memRefColor = value;
  }

  public Color getOperatorColor() {
    return operatorColor;
  }

  public void setOperatorColor(final Color value) {
    this.operatorColor = value;
  }

  public Color getPrefixColor() {
    return prefixColor;
  }

  public void setPrefixColor(final Color value) {
    this.prefixColor = value;
  }

  public Color getImmediateColor() {
    return immediateColor;
  }

  public void setImmediateColor(final Color value) {
    this.immediateColor = value;
  }

  public Color getRegisterColor() {
    return registerColor;
  }

  public void setRegisterColor(final Color value) {
    this.registerColor = value;
  }

  public Color getFunctionColor() {
    return functionColor;
  }

  public void setFunctionColor(final Color value) {
    this.functionColor = value;
  }

  public Color getVariableColor() {
    return variableColor;
  }

  public void setVariableColor(final Color value) {
    this.variableColor = value;
  }

  public Color getExpressionListColor() {
    return expressionListColor;
  }

  public void setExpressionListColor(final Color value) {
    this.expressionListColor = value;
  }

  public Color getNoTypeColor() {
    return noTypeColor;
  }

  public void setNoTypeColor(final Color value) {
    this.noTypeColor = value;
  }

  public Color getInvalidTypeColor() {
    return invalidTypeColor;
  }

  public void setInvalidTypeColor(final Color value) {
    this.invalidTypeColor = value;
  }

  public Color getOperandSeparatorColor() {
    return operandSeperatorColor;
  }

  public void setOperandSeperatorColor(final Color value) {
    this.operandSeperatorColor = value;
  }

  public Color getGlobalCommentColor() {
    return globalCommentColor;
  }

  public void setGlobalCommentColor(final Color value) {
    this.globalCommentColor = value;
  }

  public Color getLocalCommentColor() {
    return localCommentColor;
  }

  public void setLocalCommentColor(final Color value) {
    this.localCommentColor = value;
  }

  public Color getNormalFunctionColor() {
    return normalFunctionColor;
  }

  public void setNormalFunctionColor(final Color value) {
    this.normalFunctionColor = value;
  }

  public Color getLibraryFunctionColor() {
    return libraryFunctionColor;
  }

  public void setLibraryFunctionColor(final Color value) {
    this.libraryFunctionColor = value;
  }

  public Color getImportedFunctionColor() {
    return importedFunctionColor;
  }

  public void setImportedFunctionColor(final Color value) {
    this.importedFunctionColor = value;
  }

  public Color getThunkFunctionColor() {
    return thunkFunctionColor;
  }

  public void setThunkFunctionColor(final Color value) {
    this.thunkFunctionColor = value;
  }

  public Color getAdjustorThunkFunctionColor() {
    return adjustorThunkFunctionColor;
  }

  public void setAdjustorThunkFunctionColor(final Color value) {
    this.adjustorThunkFunctionColor = value;
  }

  public Color getUnknownFunctionColor() {
    return unknownFunctionColor;
  }

  public void setUnknownFunctionColor(final Color value) {
    this.unknownFunctionColor = value;
  }

  public Color getBasicBlocksColor() {
    return basicBlocksColor;
  }

  public void setBasicBlocksColor(final Color value) {
    this.basicBlocksColor = value;
  }

  public Color getConditionalJumpTrueColor() {
    return conditionalJumpTrueColor;
  }

  public void setConditionalJumpTrueColor(final Color value) {
    this.conditionalJumpTrueColor = value;
  }

  public Color getConditionalJumpFalseColor() {
    return conditionalJumpFalseColor;
  }

  public void setConditionalJumpFalseColor(final Color value) {
    this.conditionalJumpFalseColor = value;
  }

  public Color getUnconditionalJumpColor() {
    return unconditionalJumpColor;
  }

  public void setUnconditionalJumpColor(final Color value) {
    this.unconditionalJumpColor = value;
  }

  public Color getSwitchJumpColor() {
    return switchJumpColor;
  }

  public void setSwitchJumpColor(final Color value) {
    this.switchJumpColor = value;
  }

  public Color getTextEdgeColor() {
    return textEdgeColor;
  }

  public void setTextEdgeColor(final Color value) {
    this.textEdgeColor = value;
  }

  public Color getEnterInlinedJumpColor() {
    return enterInlinedJumpColor;
  }

  public void setEnterInlinedJumpColor(final Color value) {
    this.enterInlinedJumpColor = value;
  }

  public Color getLeaveInlinedJumpColor() {
    return leaveInlinedJumpColor;
  }

  public void setLeaveInlinedJumpColor(final Color value) {
    this.leaveInlinedJumpColor = value;
  }

  public Color getTagColor() {
    return tagColor;
  }

  public void setTagColor(final Color value) {
    this.tagColor = value;
  }
}
