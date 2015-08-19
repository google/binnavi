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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.types.DragAndDropSupportWrapper;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Tests for {@link BaseTypeTransferHandler}.
 *
 *  Since we are testing a class instance that derives from a Java API (TransferHandler) we cannot
 * easily create stubs for all dependencies. Specifically, the argument to importData cannot be
 * stubbed since it's a final class. We pass null instead and stub the corresponding dependencies.
 */
@RunWith(JUnit4.class)
public class BaseTypeTransferHandlerTest {
  private DragAndDropSupportWrapper wrapper;
  private TypeManager manager;
  private INaviOperandTreeNode node;
  private BaseType baseType;
  private static final int POSITION = 1;
  private static final int OFFSET = 0;
  private static final IAddress ADDRESS = new CAddress(0x123);

  private void initSubstitutionStubs() {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(wrapper.determineDropNode()).thenReturn(node);
    when(node.getType()).thenReturn(ExpressionType.REGISTER);
    when(node.getOperandPosition()).thenReturn(POSITION);
    when(node.getInstructionAddress()).thenReturn(ADDRESS);
    when(node.hasAddendSibling()).thenReturn(true);
    when(node.determineAddendValue()).thenReturn((long) 0);
  }

  @Before
  public void setup() throws UnsupportedFlavorException, IOException {
    baseType = mock(BaseType.class);
    wrapper = mock(DragAndDropSupportWrapper.class);
    when(wrapper.getDroppedBaseType()).thenReturn(baseType);
    manager = mock(TypeManager.class);
    node = mock(INaviOperandTreeNode.class);
  }

  @Test
  public void testImportData_DropNodeHasNegativeAddend() {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(node.determineAddendValue()).thenReturn((long) -1);
    assertFalse(new BaseTypeTransferHandler(manager, wrapper).importData(null));
  }

  @Test
  public void testImportData_DropNodeHasNoSibling() {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(node.hasAddendSibling()).thenReturn(false);
    assertFalse(new BaseTypeTransferHandler(manager, wrapper).importData(null));
  }

  @Test
  public void testImportData_DropNodeNull() {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(wrapper.determineDropNode()).thenReturn(null);
    assertFalse(new BaseTypeTransferHandler(manager, wrapper).importData(null));
  }

  @Test
  public void testImportData_OperandNull() {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(wrapper.determineDropNode()).thenReturn(null);
    assertFalse(new BaseTypeTransferHandler(manager, wrapper).importData(null));
  }

  @Test
  public void testImportData_OperandTypeNotRegister() {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(wrapper.determineDropNode()).thenReturn(node);
    when(node.getType()).thenReturn(ExpressionType.EXPRESSION_LIST, ExpressionType.IMMEDIATE_FLOAT,
        ExpressionType.IMMEDIATE_INTEGER, ExpressionType.MEMDEREF, ExpressionType.OPERATOR,
        ExpressionType.SIZE_PREFIX, ExpressionType.SYMBOL);
    final BaseTypeTransferHandler handler = new BaseTypeTransferHandler(manager, wrapper);
    assertFalse(handler.importData(null));
    assertFalse(handler.importData(null));
    assertFalse(handler.importData(null));
    assertFalse(handler.importData(null));
    assertFalse(handler.importData(null));
    assertFalse(handler.importData(null));
    assertFalse(handler.importData(null));
  }

  @Test
  public void testImportData_SingleExpression() throws CouldntSaveDataException {
    when(wrapper.acceptDrop()).thenReturn(true);
    when(wrapper.determineDropNode()).thenReturn(node);
    when(node.getType()).thenReturn(ExpressionType.REGISTER);
    when(node.getOperandPosition()).thenReturn(POSITION);
    when(node.getInstructionAddress()).thenReturn(ADDRESS);
    when(node.hasAddendSibling()).thenReturn(false);
    when(node.getChildren()).thenReturn(new ArrayList<INaviOperandTreeNode>());
    final BaseTypeTransferHandler handler = new BaseTypeTransferHandler(manager, wrapper);
    handler.importData(null);
    verify(manager).createTypeSubstitution(node, baseType, POSITION, OFFSET, ADDRESS);
  }

  @Test
  public void testImportData_SkipsImport() {
    when(wrapper.acceptDrop()).thenReturn(false);
    assertFalse(new BaseTypeTransferHandler(manager, wrapper).importData(null));
  }


  @Test
  public void testImportData_SubstitutionCreated() throws CouldntSaveDataException {
    initSubstitutionStubs();
    final BaseTypeTransferHandler handler = new BaseTypeTransferHandler(manager, wrapper);
    handler.importData(null);
    verify(manager).createTypeSubstitution(node, baseType, POSITION, OFFSET, ADDRESS);
  }

  @Test
  public void testImportData_SubstitutionUpdated() throws CouldntSaveDataException {
    initSubstitutionStubs();
    final TypeSubstitution substitution = mock(TypeSubstitution.class);
    when(node.getTypeSubstitution()).thenReturn(substitution);
    final BaseTypeTransferHandler handler = new BaseTypeTransferHandler(manager, wrapper);
    handler.importData(null);
    verify(manager).updateTypeSubstitution(node, substitution, baseType,
        new ArrayList<TypeMember>(), OFFSET);
  }
}
