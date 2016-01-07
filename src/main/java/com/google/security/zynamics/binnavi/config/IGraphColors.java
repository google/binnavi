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

import java.awt.Color;

/**
 * This interface can be implemented by classes that want to provide colors that are used when the
 * content of nodes is created.
 */
public interface IGraphColors extends IDebugSettings {
 
  // Getters/setters for the graph colors.
  Color getAddressColor();
  void setAddressColor(Color color);
  Color getAdjustorThunkFunctionColor();
  void setAdjustorThunkFunctionColor(Color color);
  Color getBasicBlocksColor();
  void setBasicBlocksColor(Color color);
  Color getConditionalJumpFalseColor();
  void setConditionalJumpFalseColor(Color color);
  Color getConditionalJumpTrueColor();
  void setConditionalJumpTrueColor(Color color);
  Color getEnterInlinedJumpColor();
  void setEnterInlinedJumpColor(Color color);
  Color getExpressionListColor();
  void setExpressionListColor(Color color);
  Color getFunctionColor();
  void setFunctionColor(Color color);
  Color getGlobalCommentColor();
  void setGlobalCommentColor(Color color);
  Color getImmediateColor();
  void setImmediateColor(Color color);
  Color getImportedFunctionColor();
  void setImportedFunctionColor(Color color);
  Color getLeaveInlinedJumpColor();
  void setLeaveInlinedJumpColor(Color color);
  Color getLibraryFunctionColor();
  void setLibraryFunctionColor(Color color);
  Color getLocalCommentColor();
  void setLocalCommentColor(Color color);
  Color getMemRefColor();
  void setMemRefColor(Color color);
  Color getMnemonicColor();
  void setMnemonicColor(Color color);
  Color getNormalFunctionColor();
  void setNormalFunctionColor(Color color);
  Color getOperandSeparatorColor();
  void setOperandSeparatorColor(Color color);
  Color getOperatorColor();
  void setOperatorColor(Color color);
  Color getPrefixColor();
  void setPrefixColor(Color color);
  Color getRegisterColor();
  void setRegistersColor(Color color);
  Color getStringColor();
  void setStringColor(Color color);
  Color getSwitchEdgeColor();
  void setSwitchEdgeColor(Color color);
  Color getTagColor();
  void setTagColor(Color color);
  Color getTextEdgeColor();
  void setTextEdgeColor(Color color);
  Color getThunkFunctionColor();
  void setThunkFunctionColor(Color color);
  Color getUnconditionalJumpColor();
  void setUnconditionalJumpColor(Color color);
  Color getUnknownFunctionColor();
  void setUnknownFunctionColor(Color color);
  Color getVariableColor();
  void setVariableColor(Color color);
}
