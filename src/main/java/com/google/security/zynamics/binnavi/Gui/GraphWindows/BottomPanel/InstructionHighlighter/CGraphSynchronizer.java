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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.INaviGraphListener;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviGraphListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;


/**
 * Synchronizes a view with the special instruction highlighting model.
 */
public final class CGraphSynchronizer {
  /**
   * The graph to synchronize.
   */
  private final ZyGraph m_graph;

  /**
   * The special instructions model to synchronize.
   */
  private final CSpecialInstructionsModel m_model;

  /**
   * Maps between addresses and the instructions found on that address.
   */

  private final ListMultimap<IAddress, INaviInstruction> m_instructionMap = ArrayListMultimap
      .create();

  /**
   * Keeps view and model synchronized.
   */
  private final INaviViewListener m_listener = new InternalViewListener();

  /**
   * Keeps the highlighting synchronized with changing settings.
   */
  private final ITypeDescriptionListener m_descriptionListener = new InternalDescriptionListener();

  private final INaviGraphListener m_graphListener = new InternalGraphListener();

  /**
   * Creates a new synchronizer object.
   * 
   * @param graph The graph to synchronize.
   * @param model The special instructions model to synchronize.
   */
  public CGraphSynchronizer(final ZyGraph graph, final CSpecialInstructionsModel model) {
    m_graph = Preconditions.checkNotNull(graph, "IE02843: graph argument can not be null");
    m_model = Preconditions.checkNotNull(model, "IE02844: model argument can not be null");

    updateInstructionMap();
    m_model.setInstructions(updateInstructions());
    m_graph.addListener(m_graphListener);
    m_graph.getRawView().addListener(m_listener);
    for (final ITypeDescription description : model.getDescriptions()) {
      description.addListener(m_descriptionListener);
    }
  }

  /**
   * Checks whether any instructions at all should be highlighted.
   * 
   * @return True, if any kind of instruction highlighting is active.
   */
  private boolean highlightAny() {
    return CollectionHelpers.any(m_model.getDescriptions(),
        new ICollectionFilter<ITypeDescription>() {
          @Override
          public boolean qualifies(final ITypeDescription item) {
            return item.isEnabled();
          }
        });
  }

  /**
   * Recalculates the internally cached structures when necessary.
   */
  private void update() {
    updateInstructionMap();
    m_model.setInstructions(updateInstructions());

    CTypeResultsHighlighter.updateHighlighting(m_graph, m_model.getInstructions());
  }

  /**
   * Updates the cached Address => Instruction map.
   */
  private void updateInstructionMap() {
    m_instructionMap.clear();

    for (final INaviViewNode node : m_graph.getRawView().getGraph()) {
      if (node instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) node;

        for (final INaviInstruction instruction : cnode.getInstructions()) {
          final IAddress address = instruction.getAddress();

          m_instructionMap.put(address, instruction);
        }
      }
    }
  }

  /**
   * Calculates the instructions to highlight.
   * 
   * @return The instructions to highlight.
   */
  private List<CSpecialInstruction> updateInstructions() {
    final List<CSpecialInstruction> instructions = new ArrayList<CSpecialInstruction>();

    if (highlightAny()) {
      try {
        final ReilFunction reilCode = m_graph.getRawView().getContent().getReilCode();

        for (final ITypeDescription description : m_model.getDescriptions()) {
          if (description.isEnabled()) {
            instructions.addAll(description.visit(reilCode, m_instructionMap));
          }
        }
      } catch (final InternalTranslationException e) {
        CUtilityFunctions.logException(e);
      }
    }

    return instructions;
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_graph.getRawView().removeListener(m_listener);

    for (final ITypeDescription description : m_model.getDescriptions()) {
      description.removeListener(m_descriptionListener);
    }
  }

  /**
   * Keeps the highlighting synchronized with changing settings.
   */
  private class InternalDescriptionListener implements ITypeDescriptionListener {
    @Override
    public void changedColor(final Color color) {
      update();
    }

    @Override
    public void changedStatus(final boolean enabled) {
      update();
    }
  }

  private class InternalGraphListener extends NaviGraphListenerAdapter {
    @Override
    public void changedView(final INaviView oldView, final INaviView newView) {
      oldView.removeListener(m_listener);
      newView.addListener(m_listener);
    }
  }

  /**
   * Updates the cached structures when the view changes.
   */
  private class InternalViewListener extends CViewListenerAdapter {

    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      update();
    }

    @Override
    public void addedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      update();
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      update();
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      update();
    }

    @Override
    public void savedView(final INaviView view) {
      update();
    }
  }
}
