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
package com.google.security.zynamics.binnavi.Database.MockClasses;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.Database.NodeParser.ICodeNodeProvider;
import com.google.security.zynamics.binnavi.Database.NodeParser.ParserException;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public final class MockCodeNodeProvider implements ICodeNodeProvider {
  private int index = -1;

  public List<MockCodeNodeData> data = new ArrayList<MockCodeNodeData>();

  @Override
  public IAddress getInstructionAddress() {
    return data.get(index).address;
  }

  @Override
  public String getInstructionArchitecture() {
    return data.get(index).architecture;
  }

  @Override
  public int getBorderColor() {
    return data.get(index).borderColor;
  }

  @Override
  public int getColor() {
    return data.get(index).color;
  }

  @Override
  public byte[] getData() {
    return data.get(index).data;
  }

  @Override
  public int getExpressionTreeId() {
    return data.get(index).expressionId;
  }

  @Override
  public int getExpressionTreeType() {
    return data.get(index).expressionType;
  }

  @Override
  public IAddress getFunctionAddress() {
    return null;
  }

  @Override
  public Integer getGlobalInstructionCommentId() {
    return index;
    // TODO(timkornau)
  }

  @Override
  public Integer getGlobalNodeCommentId() {
    // TODO(timkornau)
    return null;
  }

  @Override
  public double getHeight() {
    return 0;
  }

  @Override
  public String getImmediate() {
    return data.get(index).immediate;
  }

  @Override
  public Integer getLocalInstructionCommentId() {
    // TODO(timkornau)
    return null;
  }

  @Override
  public Integer getLocalNodeCommentId() {
    // TODO(timkornau)
    return null;
  }

  @Override
  public String getMnemonic() {
    return data.get(index).mnemonic;
  }

  @Override
  public int getModule() {
    return data.get(index).module;
  }

  @Override
  public int getNodeId() {
    return data.get(index).nodeId;
  }

  @Override
  public Integer getOperandPosition() {
    return data.get(index).operandPosition;
  }

  @Override
  public IAddress getParentFunction() {
    return data.get(index).parentFunction;
  }

  @Override
  public int getParentId() {
    return data.get(index).parentId;
  }

  @Override
  public CReference getReference() {
    return data.get(index).reference;
  }

  @Override
  public String getReplacement() {
    return data.get(index).replacement;
  }

  @Override
  public int getSubstitutionOffset() throws ParserException {
    // TODO(jannewger): Auto-generated method stub
    return 0;
  }

  @Override
  public int getSubstitutionPosition() {
    // TODO(jannewger): Auto-generated method stub
    return 0;
  }

  @Override
  public Integer getSubstitutionTypeId() throws ParserException {
    // TODO(jannewger): Auto-generated method stub
    return null;
  }

  @Override
  public String getSymbol() {
    return data.get(index).symbol;
  }

  @Override
  public Integer getTypeInstanceId() throws ParserException {
    return data.get(index).typeInstanceId;
  }

  @Override
  public double getWidth() {
    return 0;
  }

  @Override
  public double getX() {
    return 0;
  }

  @Override
  public double getY() {
    return 0;
  }

  @Override
  public boolean isAfterLast() {
    return index >= data.size();
  }

  @Override
  public boolean isSelected() {
    return data.get(index).selected;
  }

  @Override
  public boolean isVisible() {
    return data.get(index).visible;
  }

  @Override
  public boolean next() {
    index++;

    return !isAfterLast();
  }

  @Override
  public boolean prev() {
    index--;

    return index >= 0;
  }

  @Override
  public Integer[] getSubstitutionPath() throws ParserException {
    // TODO Auto-generated method stub
    return null;
  }
}
