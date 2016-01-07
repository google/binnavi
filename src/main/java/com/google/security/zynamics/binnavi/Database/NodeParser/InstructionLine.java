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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds raw data about an instruction during the instruction of an instruction object.
 */
// ESCA-JAVA0100:
public final class InstructionLine {
  /**
   * X-coordinate of the basic block.
   */
  private double m_x;

  /**
   * X-coordinate of the basic block.
   */
  private double m_y;

  /**
   * Width of the basic block.
   */
  private double m_width;

  /**
   * Height of the basic block.
   */
  private double m_height;

  /**
   * Parent function of the basic block.
   */
  private INaviFunction m_parentFunction;

  /**
   * The local node comment id.
   */
  private Integer m_localNodeCommentId;

  /**
   * Visibility state of the basic block.
   */
  private boolean m_visible;

  /**
   * Selection state of the basic block.
   */
  private boolean m_selected;

  /**
   * Color of the basic block.
   */
  private Color m_color;

  /**
   * Border color of the basic block.
   */
  private Color m_borderColor;

  /**
   * ID of the basic block.
   */
  private int m_basicBlock;

  /**
   * Address of the instruction.
   */
  private IAddress m_address;

  /**
   * Mnemonic of the instruction.
   */
  private String m_mnemonic;

  /**
   * Operands of the instruction.
   */
  private final List<OperandTree> m_operands = new ArrayList<OperandTree>();

  /**
   * Local instruction comment.
   */
  private Integer m_localInstructionCommentId;

  /**
   * Binary data of the instruction.
   */
  private byte[] m_data;

  /**
   * Global instruction comment.
   */
  private Integer m_globalInstructionCommentId;

  /**
   * Module the instruction belongs to.
   */
  private INaviModule module;

  /**
   * Architecture of the instruction.
   */
  private String m_architecture;

  /**
   * ID of the instruction.
   */
  private int m_id;

  private Integer m_globalNodeCommentId;

  /**
   * Returns the address of the instruction.
   *
   * @return The address of the instruction.
   */
  public IAddress getAddress() {
    return m_address;
  }

  /**
   * Returns the architecture of the instruction.
   *
   * @return The architecture of the instruction.
   */
  public String getArchitecture() {
    return m_architecture;
  }

  /**
   * Returns the ID of the basic block.
   *
   * @return The ID of the basic block.
   */
  public int getBasicBlock() {
    return m_basicBlock;
  }

  /**
   * Returns the border color of the basic block.
   *
   * @return The border color of the basic block.
   */
  public Color getBorderColor() {
    return m_borderColor;
  }

  /**
   * Returns the color of the basic block.
   *
   * @return The color of the basic block.
   */
  public Color getColor() {
    return m_color;
  }

  /**
   * Returns the binary data of the instruction.
   *
   * @return The binary data of the instruction.
   */
  public byte[] getData() {
    return m_data.clone();
  }

  /**
   * Returns the global instruction comment id.
   *
   * @return The global instruction comment id.
   */
  public Integer getGlobalInstructionComment() {
    return m_globalInstructionCommentId;
  }

  /**
   * Returns the global node comment.
   *
   * @return The global node comment.
   */
  public Integer getGlobalNodeCommentId() {
    return m_globalNodeCommentId;
  }

  /**
   * Returns the height of the basic block.
   *
   * @return The height of the basic block.
   */
  public double getHeight() {
    return m_height;
  }

  /**
   * Returns the ID of the instruction.
   *
   * @return The ID of the instruction.
   */
  public int getId() {
    return m_id;
  }

  /**
   * Returns the local instruction comment.
   *
   * @return The local instruction comment.
   */
  public Integer getLocalInstructionCommentId() {
    return m_localInstructionCommentId;
  }

  /**
   * Returns the local node comment id.
   *
   * @return The local node comment id.
   */
  public Integer getLocalNodeCommentId() {
    return m_localNodeCommentId;
  }

  /**
   * Returns the instruction mnemonic.
   *
   * @return The instruction mnemonic.
   */
  public String getMnemonic() {
    return m_mnemonic;
  }

  /**
   * Returns the module the instruction belongs to.
   *
   * @return The module the instruction belongs to.
   */
  public INaviModule getModule() {
    return this.module;
  }

  /**
   * Returns the operands of the instruction.
   *
   * @return The operands of the instruction.
   */
  public List<OperandTree> getOperands() {
    return m_operands;
  }

  /**
   * Returns the parent function of the basic block.
   *
   * @return The parent function of the basic block.
   */
  public INaviFunction getParentFunction() {
    return m_parentFunction;
  }

  /**
   * Returns the width of the basic block.
   *
   * @return The width of the basic block.
   */
  public double getWidth() {
    return m_width;
  }

  /**
   * Returns the X-coordinate of the basic block.
   *
   * @return The X-coordinate of the basic block.
   */
  public double getX() {
    return m_x;
  }

  /**
   * Returns the Y-coordinate of the basic block.
   *
   * @return The Y-coordinate of the basic block.
   */
  public double getY() {
    return m_y;
  }

  /**
   * Returns the selection state of the basic block.
   *
   * @return The selection state of the basic block.
   */
  public boolean isSelected() {
    return m_selected;
  }

  /**
   * Returns the visibility state of the basic block.
   *
   * @return The visibility state of the basic block.
   */
  public boolean isVisible() {
    return m_visible;
  }

  /**
   * Sets the address of the instruction.
   *
   * @param address The address of the instruction.
   */
  public void setAddress(final IAddress address) {
    this.m_address = address;
  }

  /**
   * Sets the architecture of the instruction.
   *
   * @param architecture The new architecture value.
   */
  public void setArchitecture(final String architecture) {
    m_architecture = architecture;
  }

  /**
   * Sets the basic block ID.
   *
   * @param basicBlock The new basic block ID.
   */
  public void setBasicBlock(final int basicBlock) {
    this.m_basicBlock = basicBlock;
  }

  /**
   * Sets the border color of the node.
   *
   * @param color The new border color of the node.
   */
  public void setBorderColor(final Color color) {
    this.m_borderColor = color;
  }

  /**
   * Sets the color of the node.
   *
   * @param color The new color of the node.
   */
  public void setColor(final Color color) {
    this.m_color = color;
  }

  /**
   * Sets the binary data of the instruction.
   *
   * @param data The binary data of the instruction.
   */
  public void setData(final byte[] data) {
    this.m_data = data.clone();
  }

  /**
   * Sets the global comment of the instruction.
   *
   * @param globalCommentId The global comment of the instruction.
   */
  public void setGlobalInstructionComment(final Integer globalCommentId) {
    this.m_globalInstructionCommentId = globalCommentId;
  }

  /**
   * Sets the instruction comment.
   *
   * @param nodeCommentId The new global node comment id.
   */
  public void setGlobalNodeComment(final Integer nodeCommentId) {
    this.m_globalNodeCommentId = nodeCommentId;
  }

  /**
   * Sets the height of the basic block.
   *
   * @param height The new height of the basic block.
   */
  public void setHeight(final double height) {
    m_height = height;
  }

  /**
   * Sets the ID of the instruction.
   *
   * @param instructionId The new ID value.
   */
  public void setId(final int instructionId) {
    m_id = instructionId;
  }

  /**
   * Sets the local instruction comment id.
   *
   * @param localInstructionCommentId The new instruction comment id value.
   */
  public void setLocalInstructionComment(final Integer localInstructionCommentId) {
    this.m_localInstructionCommentId = localInstructionCommentId;
  }

  /**
   * Sets the local node comment id.
   *
   * @param commentid The new node comment id value.
   */
  public void setLocalNodeCommentId(final Integer commentid) {
    this.m_localNodeCommentId = commentid;
  }

  /**
   * Sets the mnemonic of the instruction.
   *
   * @param mnemonic The new mnemonic value.
   */
  public void setMnemonic(final String mnemonic) {
    this.m_mnemonic = mnemonic;
  }

  /**
   * Sets the module of the instruction.
   *
   * @param module The new module value.
   */
  public void setModule(final INaviModule module) {
    Preconditions.checkNotNull(module, "IE00715: Module argument can not be null");

    this.module = module;
  }

  /**
   * Sets the parent function of the instruction.
   *
   * @param parentFunction The new parent function value.
   */
  public void setParentFunction(final INaviFunction parentFunction) {
    this.m_parentFunction = parentFunction;
  }

  /**
   * Sets the selection state of the basic block.
   *
   * @param selected The new selection state.
   */
  public void setSelected(final boolean selected) {
    this.m_selected = selected;
  }

  /**
   * Sets the visibility state of the basic block.
   *
   * @param visible The new visibility state.
   */
  public void setVisible(final boolean visible) {
    this.m_visible = visible;
  }

  /**
   * Sets the width of the basic block.
   *
   * @param width The new height of the basic block.
   */
  public void setWidth(final double width) {
    m_width = width;
  }

  /**
   * Sets the x-coordinate of the basic block.
   *
   * @param x The new x-coordinate of the basic block.
   */
  public void setX(final double x) {
    this.m_x = x;
  }

  /**
   * Sets the y-coordinate of the basic block.
   *
   * @param y The new y-coordinate of the basic block.
   */
  public void setY(final double y) {
    this.m_y = y;
  }
}
