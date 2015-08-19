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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.ArrayList;
import java.util.List;

public final class MockInstruction implements INaviInstruction {

  private final ArrayList<IComment> m_globalComment;
  public String m_mnemonic;
  private IAddress m_address;
  private final List<COperandTree> m_operands;
  private final INaviModule m_module;
  private boolean m_isSaved;
  private final SQLProvider m_provider = new MockSqlProvider();

  public MockInstruction() {
    this("nop", new ArrayList<COperandTree>(), null);
  }

  public MockInstruction(final String mnemonic, final List<COperandTree> operands,
      final ArrayList<IComment> globalComment) {
    m_mnemonic = mnemonic;
    m_globalComment = globalComment;
    m_address = new CAddress(0x123);
    m_operands = operands;
    m_module = new MockModule();
  }

  public MockInstruction(final CAddress cAddress, final String mnemonic,
      final ArrayList<COperandTree> operands, final ArrayList<IComment> globalComment,
      final INaviModule module) {
    m_mnemonic = mnemonic;
    m_globalComment = globalComment;
    m_address = cAddress;
    m_operands = operands;
    m_module = module;
  }

  public MockInstruction(final IAddress cAddress, final String mnemonic,
      final ArrayList<COperandTree> operands, final ArrayList<IComment> globalComment) {
    m_mnemonic = mnemonic;
    m_globalComment = globalComment;
    m_address = cAddress;
    m_operands = operands;
    m_module = new MockModule();
  }

  public MockInstruction(final IAddress cAddress, final String mnemonic,
      final List<COperandTree> operands, final ArrayList<IComment> globalComment) {
    m_mnemonic = mnemonic;
    m_globalComment = globalComment;
    m_address = cAddress;
    m_operands = operands;
    m_module = new MockModule();
  }

  public MockInstruction(final long address) {
    this("nop", new ArrayList<COperandTree>(), null);

    m_address = new CAddress(address);
  }

  @Override
  public void addListener(final IInstructionListener listener) {}

  @Override
  public List<IComment> appendGlobalComment(final String commentText) {
    final int id = m_globalComment.size() + 1;
    final IComment parent =
        m_globalComment.size() == 0 ? null : m_globalComment.get(m_globalComment.size() - 1);
    final IUser user = CUserManager.get(m_provider).getCurrentActiveUser();
    final IComment comment = new CComment(id, user, parent, commentText);
    m_globalComment.add(comment);
    return m_globalComment;
  }

  @Override
  public INaviInstruction cloneInstruction() {
    final INaviInstruction instruction =
        new MockInstruction(m_address, m_mnemonic, m_operands, m_globalComment);

    return instruction;
  }

  @Override
  public void close() {
    return;
  }

  @Override
  public void deleteGlobalComment(final IComment comment) {
    m_globalComment.remove(comment);
  }

  @Override
  public IComment editGlobalComment(final IComment comment, final String commentText) {
    Preconditions.checkNotNull(comment, "Error: comment argument can not be null");
    Preconditions.checkNotNull(commentText, "Error: newComment argument can not be null");
    Preconditions.checkArgument(
        CUserManager.get(m_provider).getCurrentActiveUser().equals(comment.getUser()));

    final int index = m_globalComment.indexOf(comment);
    final IComment newComment =
        new CComment(comment.getId(), comment.getUser(), comment.getParent(), commentText);
    m_globalComment.set(index, newComment);
    return newComment;
  }

  @Override
  public IAddress getAddress() {
    return m_address;
  }

  @Override
  public String getArchitecture() {
    return "x86-32";
  }

  @Override
  public byte[] getData() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public ArrayList<IComment> getGlobalComment() {
    return m_globalComment;
  }

  @Override
  public String getInstructionString() {
    return null;
  }

  @Override
  public long getLength() {
    return 4;
  }

  @Override
  public String getMnemonic() {
    return m_mnemonic;
  }

  @Override
  public Integer getMnemonicCode() {
    return m_mnemonic.hashCode();
  }

  @Override
  public INaviModule getModule() {
    return m_module;
  }

  @Override
  public int getOperandPosition(final INaviOperandTree operand) {
    return m_operands.indexOf(operand);
  }

  @Override
  public List<COperandTree> getOperands() {
    return new ArrayList<COperandTree>(m_operands);
  }

  @Override
  public void initializeGlobalComment(final ArrayList<IComment> comment) {
    m_globalComment.clear();
    m_globalComment.addAll(comment);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return true;
  }

  @Override
  public boolean isOwner(final IComment second) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isStored() {
    return m_isSaved;
  }

  @Override
  public void removeListener(final IInstructionListener listener) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void setSaved(final boolean saved) {
    m_isSaved = true;
  }

  @Override
  public String toString() {
    return getAddress() + ": " + getMnemonic();
  }
}
