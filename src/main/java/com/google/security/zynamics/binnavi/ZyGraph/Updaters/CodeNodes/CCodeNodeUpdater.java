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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters.CodeNodes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyCodeNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.CDefaultModifier;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CBreakpointPainter;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CDebuggerPainter;
import com.google.security.zynamics.binnavi.debug.debugger.DefaultAddressConverter;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.CNaviCodeNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CReferenceFinder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for updating code nodes when the underlying raw data changes.
 */
public final class CCodeNodeUpdater implements IRealizerUpdater<NaviNode> {
  /**
   * The graph where the code node is shown.
   */
  private final ZyGraph graph;

  /**
   * The node that is updated by the updater.
   */
  private final NaviNode node;

  /**
   * The code node that is updated by the updater.
   */
  private final INaviCodeNode codeNode;

  /**
   * Debugger provider that provides debuggers that can influence the code node.
   */
  private final BackEndDebuggerProvider provider;

  /**
   * Calculates the addresses shown in the code node depending on the active settings.
   */
  private final CDefaultModifier nodeModifier;

  /**
   * The realizer of the code node.
   */
  private IZyNodeRealizer realizer;

  /**
   * Listener object that listens on all relevant objects and updates the code node if necessary.
   */
  private final CModuleUpdater moduleUpdater;

  /**
   * Updates the code node on changes to its instructions.
   */
  private final CInstructionUpdater instructionUpdater;

  /**
   * Updates the code node on changes to relevant functions.
   */
  private final CFunctionUpdater functionUpdater;

  /**
   * Updates the code node on changes to its underlying model.
   */
  private final InternalCodeNodeListener codeNodeListener = new InternalCodeNodeListener();

  /**
   * Changes the code node on changes to its tagging state.
   */
  private final CTagUpdater tagUpdater;

  /**
   * Updates the code node on important debugger events.
   */
  private final CDebuggerUpdater debuggerUpdater;

  /**
   * Updates the code node on important settings changes.
   */
  private final CSettingsUpdater settingsUpdater;

  /**
   * Updates the code node on changes to operands.
   */
  private final COperandUpdater operandTreeUpdater;

  private final TypeSubstitutionsUpdater substitutionsUpdater;

  /**
   * Updates the code node on changes to the debugger provider.
   */
  private final CDebuggerProviderUpdater debuggerProviderListener;

  /**
   * Creates a new code node updater object.
   *
   * @param graph The graph where the code node is shown.
   * @param node The node that is updated by the updater.
   * @param codeNode The code node that is updated by the updater.
   * @param debuggerProvider Debugger provider that provides debuggers that can influence the code
   *        node.
   */
  public CCodeNodeUpdater(final ZyGraph graph, final NaviNode node, final INaviCodeNode codeNode,
      final BackEndDebuggerProvider debuggerProvider) {
    this.graph = Preconditions.checkNotNull(graph, "IE00984: Graph argument can't be null");
    this.node = Preconditions.checkNotNull(node, "IE02238: Node argument can not be null");
    this.codeNode =
        Preconditions.checkNotNull(codeNode, "IE00985: Code node argument can't be null");
    provider = Preconditions.checkNotNull(
        debuggerProvider, "IE02239: Debugger provider argument can not be null");

    nodeModifier = new CDefaultModifier(graph.getSettings(), debuggerProvider);
    tagUpdater = new CTagUpdater(graph);
    settingsUpdater = new CSettingsUpdater(graph);
    operandTreeUpdater = new COperandUpdater(graph);
    moduleUpdater = new CModuleUpdater(graph);
    instructionUpdater = new CInstructionUpdater(graph);
    functionUpdater = new CFunctionUpdater(graph);
    debuggerUpdater = new CDebuggerUpdater(graph);
    debuggerProviderListener = new CDebuggerProviderUpdater(debuggerUpdater);
    substitutionsUpdater = new TypeSubstitutionsUpdater(codeNode, node);

    initializeListeners();
  }

  /**
   * Initializes the listeners that are responsible for updating the code node.
   */
  private void initializeListeners() {
    try {
      codeNode.getParentFunction().addListener(functionUpdater);
      codeNode.getParentFunction().getModule().addListener(moduleUpdater);
    } catch (final MaybeNullException exception) {
      // The code nodes does not have a parent function, therefore the information
      // about the parent function is not shown in the code and does not have to
      // be processed when updating.
    }

    final HashMap<INaviInstruction, INaviFunction> referenceMap =
        CReferenceFinder.getCodeReferenceMap(codeNode);

    for (final INaviFunction functionReference : Sets.newHashSet(referenceMap.values())) {
      functionReference.addListener(functionUpdater);
    }

    codeNode.addListener(codeNodeListener);

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      instruction.addListener(instructionUpdater);

      for (final COperandTree tree : instruction.getOperands()) {
        for (final INaviOperandTreeNode currentNode : tree.getNodes()) {
          currentNode.addListener(operandTreeUpdater);
        }
      }
    }

    final Iterator<CTag> it = codeNode.getTagsIterator();
    while (it.hasNext()) {
      it.next().addListener(tagUpdater);
    }

    for (final IDebugger debugger : provider.getDebuggers()) {
      debugger.getProcessManager().addListener(debuggerUpdater);
    }

    provider.addListener(debuggerProviderListener);
    graph.getSettings().getDisplaySettings().addListener(settingsUpdater);
    try {
      codeNode.getParentFunction().getModule().getTypeManager().addListener(substitutionsUpdater);
    } catch (final MaybeNullException exception) {
      // If the code node doesn't have a a parent function, it is not in the database and therefore
      // cannot receive type substitutions.
    }
  }

  /**
   * Regenerates the content of the node and updates the graph view.
   */
  private void rebuildNode() {
    realizer.regenerate();
    graph.updateViews();
  }

  /**
   * Removes all listeners this class has attached.
   */
  private void removeListeners() {
    try {
      codeNode.getParentFunction().removeListener(functionUpdater);
      codeNode.getParentFunction().getModule().removeListener(moduleUpdater);
    } catch (final MaybeNullException exception) {
      // The code nodes does not have a parent function, therefore the information
      // about the parent function is not shown in the code and does not have to
      // be processed when updating.
    }

    codeNode.removeListener(codeNodeListener);

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      instruction.removeListener(instructionUpdater);
    }

    final Iterator<CTag> it = codeNode.getTagsIterator();
    while (it.hasNext()) {
      it.next().removeListener(tagUpdater);
    }

    for (final IDebugger debugger : provider.getDebuggers()) {
      debugger.getProcessManager().removeListener(debuggerUpdater);
    }

    provider.removeListener(debuggerProviderListener);

    graph.getSettings().getDisplaySettings().removeListener(settingsUpdater);
  }

  @Override
  public void dispose() {
    removeListeners();
  }

  @Override
  public void generateContent(final IZyNodeRealizer realizer, final ZyLabelContent content) {
    ZyCodeNodeBuilder.buildContent(content, codeNode, graph.getSettings(), nodeModifier);

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      final INaviModule module = instruction.getModule();

      if ((provider != null) && (provider.getDebugger(module) != null) && graph.getSettings()
          .getDisplaySettings().getShowMemoryAddresses(provider.getDebugger(module))) {
        final int line = CCodeNodeHelpers.instructionToLine(codeNode, instruction);

        if (line != -1) {
          final ZyLineContent lineContent = this.realizer.getNodeContent().getLineContent(line);

          // TODO(timkornau) x64
          lineContent.setTextColor(0, 8, Color.RED);
        }
      }
    }

    // Set highlighting for breakpoints and the instruction pointer.
    final INaviInstruction instruction = codeNode.getInstructions().iterator().next();
    if (instruction != null) {
      final INaviModule module = instruction.getModule();
      final IDebugger debugger = provider.getDebugger(module);
      if (debugger == null) {
        return;
      }
      final BreakpointManager manager = debugger.getBreakpointManager();
      CBreakpointPainter.paintBreakpoints(manager, node, codeNode);

      if (debugger.getProcessManager().getActiveThread() != null) {
        final RelocatedAddress instructionPointer =
            debugger.getProcessManager().getActiveThread().getCurrentAddress();
        final MemoryModule memoryModule =
            debugger.getProcessManager().getModule(instructionPointer);
        final UnrelocatedAddress unrelocatedIP = new DefaultAddressConverter(
            memoryModule.getBaseAddress().getAddress(), module.getConfiguration().getFileBase())
        .memoryToFile(instructionPointer);
        CDebuggerPainter.updateSingleNodeDebuggerHighlighting(graph, unrelocatedIP, node);
      }
    }
  }

  @Override
  public void setRealizer(final IZyNodeRealizer realizer) {
    this.realizer = realizer;

    debuggerUpdater.setRealizer(realizer);
    functionUpdater.setRealizer(realizer);
    instructionUpdater.setRealizer(realizer);
    moduleUpdater.setRealizer(realizer);
    operandTreeUpdater.setRealizer(realizer);
    settingsUpdater.setRealizer(realizer);
    tagUpdater.setRealizer(realizer);
  }

  /**
   * Updates the code node on changes to its underlying model.
   */
  private class InternalCodeNodeListener extends CNaviCodeNodeListenerAdapter {
    @Override
    public void addedInstruction(final INaviCodeNode codeNode, final INaviInstruction instruction) {
      rebuildNode();
    }

    @Override
    public void appendedGlobalCodeNodeComment(
        final INaviCodeNode codeNode, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void appendedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void appendedLocalInstructionComment(
        final INaviCodeNode codeNode, final INaviInstruction instruction, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void changedInstructionColor(final CCodeNode codeNode,
        final INaviInstruction instruction, final int level, final Color color) {
      if (color == null) {
        node.clearHighlighting(
            level, CCodeNodeHelpers.instructionToLine(CCodeNodeUpdater.this.codeNode, instruction));
      } else {
        node.setHighlighting(level,
            CCodeNodeHelpers.instructionToLine(CCodeNodeUpdater.this.codeNode, instruction), color);
      }
    }

    @Override
    public void deletedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void deletedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void deletedLocalInstructionComment(
        final INaviCodeNode codeNode, final INaviInstruction instruction, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void editedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void editedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void editedLocalInstructionComment(
        final INaviCodeNode codeNode, final INaviInstruction instruction, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void initializedGlobalCodeNodeComment(
        final INaviCodeNode codeNode, final List<IComment> comment) {
      rebuildNode();
    }

    @Override
    public void initializedLocalCodeNodeComment(
        final INaviCodeNode codeNode, final List<IComment> comment) {
      rebuildNode();
    }

    @Override
    public void initializedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final List<IComment> comment) {
      rebuildNode();
    }

    @Override
    public void removedInstruction(
        final INaviCodeNode codeNode, final INaviInstruction instruction) {
      rebuildNode();
    }

    @Override
    public void taggedNode(final INaviViewNode node, final CTag tag) {
      tag.addListener(tagUpdater);

      rebuildNode();
    }

    @Override
    public void untaggedNodes(final INaviViewNode node, final List<CTag> tags) {
      for (final CTag tag : tags) {
        tag.removeListener(tagUpdater);
      }

      rebuildNode();
    }
  }
}
