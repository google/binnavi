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
package com.google.security.zynamics.binnavi.Gui.SettingsDialog;

import com.google.security.zynamics.binnavi.config.ColorsConfigItem;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.DebugColorsConfigItem;
import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * In this panel of the settings dialog, the user can configure all kinds of different color
 * settings.
 */
public final class CColorSettingsPanel extends CAbstractSettingsPanel {

  /**
   * Width of all color panels.
   */
  private static final int COLORPANEL_WIDTH = 200;

  /**
   * Height of all color panels.
   */
  private static final int COLORPANEL_HEIGHT = 25;

  /**
   * Color panel used to edit the color of normal functions.
   */
  private final ColorPanel m_normalFunctionColorPanel;

  /**
   * Color panel used to edit the color of imported functions.
   */
  private final ColorPanel m_importFunctionColorPanel;

  /**
   * Color panel used to edit the color of library functions.
   */
  private final ColorPanel m_libraryFunctionColorPanel;

  /**
   * Color panel used to edit the color of thunk functions.
   */
  private final ColorPanel m_thunkFunctionColorPanel;

  /**
   * Color panel used to edit the color of adjustor thunk functions.
   */
  private final ColorPanel m_adjustorThunkFunctionColorPanel;

  /**
   * Color panel used to edit the color of addresses in graphs.
   */
  private final ColorPanel m_addressColorPanel;

  /**
   * Color panel used to edit the color of mnemonics.
   */
  private final ColorPanel m_mnemonicColorPanel;

  /**
   * Color panel used to edit the color of literals.
   */
  private final ColorPanel m_literalsColorPanel;

  /**
   * Color panel used to edit the color of registers.
   */
  private final ColorPanel m_registersColorPanel;

  /**
   * Color panel used to edit the color of function references.
   */
  private final ColorPanel m_functionColorPanel;

  /**
   * Color panel used to edit the color of variables.
   */
  private final ColorPanel m_variableColorPanel;

  /**
   * Color panel used to edit the color of expression lists.
   */
  private final ColorPanel m_expressionListColorPanel;

  /**
   * Color panel used to edit the color of memory references.
   */
  private final ColorPanel m_memoryReferencesColorPanel;

  /**
   * Color panel used to edit the color of operators.
   */
  private final ColorPanel m_operatorColorPanel;

  /**
   * Color panel used to edit the color of operand separators.
   */
  private final ColorPanel m_operandSeparatorColorPanel;

  /**
   * Color panel used to edit the color of prefixes.
   */
  private final ColorPanel m_prefixColorPanel;

  /**
   * Color panel used to edit the color of native basic blocks.
   */
  private final ColorPanel m_basicblocksPanel;

  /**
   * Color panel used to edit the color of unconditional jump edges.
   */
  private final ColorPanel m_unconditionalJumpsPanel;

  /**
   * Color panel used to edit the color of conditional jump edges (taken).
   */
  private final ColorPanel m_conditionalJumpsTakenPanel;

  /**
   * Color panel used to edit the color of conditional jump edges (not taken).
   */
  private final ColorPanel m_conditionalJumpsNotTakenPanel;

  /**
   * Color panel used to edit the color of inlined jumps.
   */
  private final ColorPanel m_enterInlinedJumpsPanel;

  /**
   * Color panel used to edit the color of leaving inlined jumps.
   */
  private final ColorPanel m_leaveInlinedJumpsPanel;

  /**
   * Color panel used to edit the color of switch edges.
   */
  private final ColorPanel m_switchPanel;

  /**
   * Color panel used to edit the color of text node edges.
   */
  private final ColorPanel m_textJumpsPanel;

  /**
   * Color panel used to edit the color of the PC during debugging.
   */
  private final ColorPanel m_activeLineColorPanel;

  /**
   * Color panel used to edit the color of active breakpoints.
   */
  private final ColorPanel m_activeBreakpointColorPanel;

  /**
   * Color panel used to edit the color of inactive breakpoints.
   */
  private final ColorPanel m_inactiveBreakpointColorPanel;

  /**
   * Color panel used to edit the color of enabled breakpoints.
   */
  private final ColorPanel m_enabledBreakpointColorPanel;

  /**
   * Color panel used to edit the color of disabled breakpoints.
   */
  private final ColorPanel m_disabledBreakpointColorPanel;

  /**
   * Color panel used to edit the color of invalid breakpoints.
   */
  private final ColorPanel m_invalidBreakpointColorPanel;

  /**
   * Color panel used to edit the color of deleting breakpoints.
   */
  private final ColorPanel m_deletingBreakpointColorPanel;

  /**
   * Color panel used to edit the color of hit breakpoints.
   */
  private final ColorPanel m_hitBreakpointColorPanel;

  /**
   * Creates a new color settings panel.
   */
  public CColorSettingsPanel() {
    super(new BorderLayout());

    final JPanel mainPanel = new JPanel(new BorderLayout());
    final JPanel innerMainPanel = new JPanel(new GridBagLayout());
    final JPanel functionTypeColorPanel = new JPanel(new GridLayout(5, 1, 3, 3));

    final ColorsConfigItem colors = ConfigManager.instance().getColorSettings();

    m_normalFunctionColorPanel =
        buildRow(functionTypeColorPanel, "Normal Function" + ":",
            "Color used to paint normal functions in callgraphs and view tables", new ColorPanel(
                colors.getNormalFunctionColor(), true, true), false);
    m_importFunctionColorPanel =
        buildRow(functionTypeColorPanel, "Imported Function" + ":",
            "Color used to paint imported functions in callgraphs and view tables", new ColorPanel(
                colors.getImportedFunctionColor(), true, true), false);
    m_libraryFunctionColorPanel =
        buildRow(functionTypeColorPanel, "Library Function" + ":",
            "Color used to paint library functions in callgraphs and view tables", new ColorPanel(
                colors.getLibraryFunctionColor(), true, true), false);
    m_thunkFunctionColorPanel =
        buildRow(functionTypeColorPanel, "Thunk Function" + ":",
            "Color used to paint thunk functions in callgraphs and view tables", new ColorPanel(
                colors.getThunkFunctionColor(), true, true), false);
    m_adjustorThunkFunctionColorPanel =
        buildRow(functionTypeColorPanel, "Unknown Function" + ":",
            "Color used to paint thunk adjustor functions in callgraphs and view tables",
            new ColorPanel(colors.getAdjustorThunkFunctionColor(), true, true), true);

    functionTypeColorPanel.setBorder(new TitledBorder("Function Colors"));

    final JPanel instructionColorPanel = new JPanel(new GridLayout(11, 1, 3, 3));

    instructionColorPanel.setBorder(new TitledBorder("Instruction Colors"));

    m_addressColorPanel =
        buildRow(instructionColorPanel, "Addresses" + ":",
            "Color used to paint addresses in graphs", new ColorPanel(colors.getAddressColor(),
                true, true), false);
    m_mnemonicColorPanel =
        buildRow(instructionColorPanel, "Mnemonics" + ":",
            "Color used to paint mnemonics in graphs", new ColorPanel(colors.getMnemonicColor(),
                true, true), false);
    m_literalsColorPanel =
        buildRow(instructionColorPanel, "Immediates" + ":",
            "Color used to paint immediate values in graphs",
            new ColorPanel(colors.getImmediateColor(), true, true), false);
    m_registersColorPanel =
        buildRow(instructionColorPanel, "Registers" + ":",
            "Color used to paint registers in graphs", new ColorPanel(colors.getRegisterColor(),
                true, true), false);
    m_functionColorPanel =
        buildRow(instructionColorPanel, "Functions" + ":",
            "Color used to paint function references in graphs",
            new ColorPanel(colors.getFunctionColor(), true, true), false);
    m_variableColorPanel =
        buildRow(instructionColorPanel, "Variables" + ":",
            "Color used to paint variables in graphs", new ColorPanel(colors.getVariableColor(),
                true, true), false);
    m_expressionListColorPanel =
        buildRow(instructionColorPanel, "Expression List" + ":",
            "Color used to paint expression lists in graphs",
            new ColorPanel(colors.getExpressionListColor(), true, true), false);
    m_memoryReferencesColorPanel =
        buildRow(instructionColorPanel, "Memory References" + ":",
            "Color used to paint memory references in graphs",
            new ColorPanel(colors.getMemRefColor(), true, true), false);
    m_operatorColorPanel =
        buildRow(instructionColorPanel, "Operators" + ":",
            "Color used to paint operators in graphs", new ColorPanel(colors.getOperatorColor(),
                true, true), false);
    m_operandSeparatorColorPanel =
        buildRow(instructionColorPanel, "Operand Separators" + ":",
            "Color used to paint operand separators in graphs",
            new ColorPanel(colors.getOperandSeparatorColor(), true, true), false);
    m_prefixColorPanel =
        buildRow(instructionColorPanel, "Prefixes" + ":", "Color used to paint prefixes in graphs",
            new ColorPanel(colors.getPrefixColor(), true, true), true);

    final JPanel graphColorPanel = new JPanel(new GridLayout(8, 1, 3, 3));

    graphColorPanel.setBorder(new TitledBorder("Graph Colors"));

    m_basicblocksPanel =
        buildRow(graphColorPanel, "Basic Blocks" + ":",
            "Color used to paint basic blocks in graphs",
            new ColorPanel(colors.getBasicBlocksColor(), true, true), false);
    m_unconditionalJumpsPanel =
        buildRow(graphColorPanel, "Unconditional Jumps" + ":", "Color of unconditional jumps",
            new ColorPanel(colors.getUnconditionalJumpColor(), true, true), false);
    m_conditionalJumpsTakenPanel =
        buildRow(graphColorPanel, "Conditional Jumps (Taken)" + ":",
            "Color of conditional jumps which are taken",
            new ColorPanel(colors.getConditionalJumpTrueColor(), true, true), false);
    m_conditionalJumpsNotTakenPanel =
        buildRow(graphColorPanel, "Conditional Jumps (Not taken)" + ":",
            "Color of conditional jumps which are not taken",
            new ColorPanel(colors.getConditionalJumpFalseColor(), true, true), false);
    m_enterInlinedJumpsPanel =
        buildRow(graphColorPanel, "Entering inlined functions" + ":",
            "Color of edges used to enter inlined functions",
            new ColorPanel(colors.getEnterInlinedJumpColor(), true, true), false);
    m_leaveInlinedJumpsPanel =
        buildRow(graphColorPanel, "Leaving inlined functions" + ":",
            "Color of edges used to leave inlined functions",
            new ColorPanel(colors.getLeaveInlinedJumpColor(), true, true), true);
    m_switchPanel =
        buildRow(graphColorPanel, "Switches" + ":",
            "Color of edges that belong to switch statements",
            new ColorPanel(colors.getSwitchJumpColor(), true, true), true);
    m_textJumpsPanel =
        buildRow(graphColorPanel, "Edges to comment nodes" + ":",
            "Color of edges that connect comment nodes to other nodes",
            new ColorPanel(colors.getTextEdgeColor(), true, true), true);

    final JPanel debuggerColorPanel = new JPanel(new GridLayout(8, 1, 3, 3));

    final DebugColorsConfigItem debuggerColors =
        ConfigManager.instance().getDebuggerColorSettings();
    m_activeLineColorPanel =
        buildRow(debuggerColorPanel, "Active Line" + ":",
            "Color used to show the line at the program counter while debugging.", new ColorPanel(
                debuggerColors.getActiveLine(), true, true), false);
    m_activeBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Active Breakpoint" + ":",
            "Color used to show active breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointActive(), true, true), false);
    m_inactiveBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Inactive Breakpoint" + ":",
            "Color used to show inactive breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointInactive(), true, true), false);
    m_enabledBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Enabled Breakpoint" + ":",
            "Color used to show enabled breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointEnabled(), true, true), false);
    m_disabledBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Disabled Breakpoint" + ":",
            "Color used to show disabled breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointDisabled(), true, true), false);
    m_hitBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Hit Breakpoint" + ":",
            "Color used to show hit breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointHit(), true, true), false);
    m_invalidBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Invalid Breakpoint" + ":",
            "Color used to show invalid breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointInvalid(), true, true), false);
    m_deletingBreakpointColorPanel =
        buildRow(debuggerColorPanel, "Deleting Breakpoint" + ":",
            "Color used to show deleting breakpoints while debugging.",
            new ColorPanel(debuggerColors.getBreakpointDeleting(), true, true), true);

    debuggerColorPanel.setBorder(new TitledBorder("Debugger Colors"));

    final GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    innerMainPanel.add(functionTypeColorPanel, constraints);

    constraints.gridy = 1;
    innerMainPanel.add(instructionColorPanel, constraints);

    constraints.gridy = 2;
    innerMainPanel.add(graphColorPanel, constraints);

    constraints.gridy = 3;
    innerMainPanel.add(debuggerColorPanel, constraints);

    mainPanel.add(innerMainPanel, BorderLayout.NORTH);

    add(new JScrollPane(mainPanel));
  }

  /**
   * Builds a single row of components in the panel.
   * 
   * @param <T> Type of the editing component.
   * 
   * @param panel Panel the editing component is added to.
   * @param labelText Text of the label that describes the option.
   * @param hint Hint shown as a tooltip.
   * @param component The component to add to the panel.
   * @param isLast True, if the component is the last component to be added to the panel.
   * 
   * @return The panel passed to the function.
   */
  private static <T extends Component> T buildRow(final JPanel panel, final String labelText,
      final String hint, final T component, final boolean isLast) {
    component.setPreferredSize(new Dimension(COLORPANEL_WIDTH, COLORPANEL_HEIGHT));

    final JPanel rowPanel = new JPanel(new BorderLayout());
    rowPanel.setBorder(new EmptyBorder(0, 2, isLast ? 2 : 0, 2));

    rowPanel.add(new JLabel(labelText), BorderLayout.CENTER);
    rowPanel.add(CHintCreator.createHintPanel(component, hint), BorderLayout.EAST);

    panel.add(rowPanel);

    return component;
  }

  @Override
  protected boolean save() {
    final ColorsConfigItem colors = ConfigManager.instance().getColorSettings();
    colors.setNormalFunctionColor(m_normalFunctionColorPanel.getColor());
    colors.setImportedFunctionColor(m_importFunctionColorPanel.getColor());
    colors.setLibraryFunctionColor(m_libraryFunctionColorPanel.getColor());
    colors.setThunkFunctionColor(m_thunkFunctionColorPanel.getColor());
    colors.setAdjustorThunkFunctionColor(m_adjustorThunkFunctionColorPanel.getColor());

    colors.setAddressColor(m_addressColorPanel.getColor());
    colors.setMnemonicColor(m_mnemonicColorPanel.getColor());
    colors.setImmediateColor(m_literalsColorPanel.getColor());
    colors.setRegisterColor(m_registersColorPanel.getColor());
    colors.setFunctionColor(m_functionColorPanel.getColor());
    colors.setVariableColor(m_variableColorPanel.getColor());
    colors.setExpressionListColor(m_expressionListColorPanel.getColor());
    colors.setMemRefColor(m_memoryReferencesColorPanel.getColor());
    colors.setOperatorColor(m_operatorColorPanel.getColor());
    colors.setOperandSeperatorColor(m_operandSeparatorColorPanel.getColor());
    colors.setPrefixColor(m_prefixColorPanel.getColor());
    colors.setAddressColor(m_addressColorPanel.getColor());

    colors.setBasicBlocksColor(m_basicblocksPanel.getColor());
    colors.setUnconditionalJumpColor(m_unconditionalJumpsPanel.getColor());
    colors.setConditionalJumpTrueColor(m_conditionalJumpsTakenPanel.getColor());
    colors.setConditionalJumpFalseColor(m_conditionalJumpsNotTakenPanel.getColor());
    colors.setEnterInlinedJumpColor(m_enterInlinedJumpsPanel.getColor());
    colors.setLeaveInlinedJumpColor(m_leaveInlinedJumpsPanel.getColor());
    colors.setSwitchJumpColor(m_switchPanel.getColor());
    colors.setTextEdgeColor(m_textJumpsPanel.getColor());

    final DebugColorsConfigItem debuggerColors =
        ConfigManager.instance().getDebuggerColorSettings();
    debuggerColors.setActiveLine(m_activeLineColorPanel.getColor());
    debuggerColors.setBreakpointActive(m_activeBreakpointColorPanel.getColor());
    debuggerColors.setBreakpointInactive(m_inactiveBreakpointColorPanel.getColor());
    debuggerColors.setBreakpointEnabled(m_enabledBreakpointColorPanel.getColor());
    debuggerColors.setBreakpointDisabled(m_disabledBreakpointColorPanel.getColor());
    debuggerColors.setBreakpointInvalid(m_invalidBreakpointColorPanel.getColor());
    debuggerColors.setBreakpointDeleting(m_deletingBreakpointColorPanel.getColor());
    debuggerColors.setBreakpointHit(m_hitBreakpointColorPanel.getColor());

    return false;
  }
}
