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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.IGraphPanelExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * Panel where function cross references are shown.
 */
public final class CCrossReferencesPanel extends JPanel implements IGraphPanelExtension {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5050102470539067670L;

  /**
   * Table model that shows the cross references.
   */
  private final CCrossReferencesModel m_tableModel = new CCrossReferencesModel();

  /**
   * Table that shows the cross references.
   */
  private final JTable m_table = new CCrossReferencesTable(m_tableModel);

  /**
   * Updates the references on changes in the graph.
   */
  private final InternalViewListener m_internalListener = new InternalViewListener();

  /**
   * For performance reasons we cache the parent functions of the nodes in the graph and how often
   * each parent function appears.
   */
  private final Map<INaviFunction, Integer> m_nodeCounter = new HashMap<INaviFunction, Integer>();

  /**
   * List of current cross references.
   */
  private final List<CCrossReference> m_crossReferences = new ArrayList<CCrossReference>();

  /**
   * Parent window of the panel.
   */
  private CGraphWindow m_parent;

  /**
   * Provides the views considered for cross references.
   */
  private IViewContainer m_viewContainer;

  /**
   * Creates a new panel object.
   */
  public CCrossReferencesPanel() {
    super(new BorderLayout());

    m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    add(new JScrollPane(m_table));

    m_table.addMouseListener(new InternalMouseListener());
  }

  /**
   * Searches for a forwarded function.
   *
   * @param module The module to search through.
   * @param function The target function to search for.
   *
   * @return The found function.
   *
   * @throws MaybeNullException Throws if the forwarded function could not be found.
   */
  private INaviFunction findForwardedFunction(
      final INaviModule module, final INaviFunction function) throws MaybeNullException {
    if (module.isLoaded()) {
      for (final INaviFunction sourceFunction :
          module.getContent().getFunctionContainer().getFunctions()) {
        if (sourceFunction.getForwardedFunctionModuleId()
            == function.getModule().getConfiguration().getId()
            && sourceFunction.getForwardedFunctionAddress().equals(function.getAddress())) {
          return sourceFunction;
        }
      }
    }

    throw new MaybeNullException();
  }

  /**
   * Processes a new node and updates the list of cross references.
   *
   * @param node The node to process.
   *
   * @return True, if the given node has a parent function that is not yet known.
   */
  private boolean processNewNode(final INaviViewNode node) {
    if (node instanceof INaviCodeNode) {
      final INaviCodeNode cnode = (INaviCodeNode) node;

      try {
        final INaviFunction targetFunction = cnode.getParentFunction();

        if (m_nodeCounter.containsKey(targetFunction)) {
          m_nodeCounter.put(targetFunction, m_nodeCounter.get(targetFunction) + 1);
        } else {
          m_nodeCounter.put(targetFunction, 1);

          for (final INaviModule sourceModule : m_viewContainer.getModules()) {
            if (sourceModule == targetFunction.getModule()) {
              // Source and target module are the same
              for (final INaviFunction sourceFunction :
                  sourceModule.getContent().getNativeCallgraph().getCallers(targetFunction)) {
                m_crossReferences.add(new CCrossReference(sourceFunction, targetFunction));
              }
            } else {
              // Source and target module are different, for example in address spaces.

              try {
                final INaviFunction forwardedFunction =
                    findForwardedFunction(sourceModule, targetFunction);

                for (final INaviFunction sourceFunction :
                    sourceModule.getContent().getNativeCallgraph().getCallers(forwardedFunction)) {
                  m_crossReferences.add(new CCrossReference(sourceFunction, targetFunction));
                }
              } catch (final MaybeNullException exception) {
                // Function is not forwarded and can therefore not be what we are looking for.
              }
            }
          }

          return true;
        }
      } catch (final MaybeNullException exception) {
        // Code nodes that do not have a parent function can obviously not be called.
      }
    }

    return false;
  }

  /**
   * Processes a removed node and updates the references list accordingly.
   *
   * @param node The node to remove.
   *
   * @return True, if the removed node was the last node with its parent function in the graph.
   */
  private boolean processRemovedNode(final INaviViewNode node) {
    if (node instanceof INaviCodeNode) {
      final INaviCodeNode cnode = (INaviCodeNode) node;

      try {
        final INaviFunction targetFunction = cnode.getParentFunction();

        if (m_nodeCounter.containsKey(targetFunction)) {
          final int newCounter = m_nodeCounter.get(targetFunction) - 1;

          if (newCounter == 0) {
            m_nodeCounter.remove(targetFunction);

            final Set<CCrossReference> toDelete = new HashSet<CCrossReference>();

            for (final CCrossReference reference : m_crossReferences) {
              if (reference.getCalledFunction() == targetFunction) {
                toDelete.add(reference);
              }
            }

            m_crossReferences.removeAll(toDelete);

            return true;
          } else {
            m_nodeCounter.put(targetFunction, newCounter);
          }
        }
        // else
        // {
        // TODO: How is this even possible?
        // }
      } catch (final MaybeNullException exception) {
        // Code nodes that do not have a parent function can obviously not be called.
      }
    }

    return false;
  }

  @Override
  public void dispose() {
  }

  @Override
  public void visit(final CGraphModel model, final IGraphPanelExtender extender) {
    m_parent = model.getParent();
    m_viewContainer = model.getViewContainer();

    for (final INaviViewNode node : model.getGraph().getRawView().getGraph().getNodes()) {
      processNewNode(node);
    }

    m_tableModel.setCrossReferences(m_crossReferences);

    extender.addTab("Calling Functions", this);

    model.getGraph().getRawView().addListener(m_internalListener);
  }

  /**
   * Handles clicks on the mouse.
   */
  private class InternalMouseListener extends MouseAdapter {
    /**
     * Shows the context menu for a given mouse event.
     *
     * @param event The mouse event.
     */
    private void displayPopupMenu(final MouseEvent event) {
      final int selectedIndex = m_table.rowAtPoint(event.getPoint());

      if (selectedIndex != -1) {
        final CCrossReference reference = m_crossReferences.get(selectedIndex);

        final CCrossReferencesTableMenu popupMenu = new CCrossReferencesTableMenu(
            m_parent, m_viewContainer, reference.getCallingFunction());

        popupMenu.show(m_table, event.getX(), event.getY());
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }
  }

  /**
   * Updates the references on changes in the graph.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      if (processNewNode(node)) {
        m_tableModel.setCrossReferences(m_crossReferences);
      }
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      if (processRemovedNode(node)) {
        m_tableModel.setCrossReferences(m_crossReferences);
      }
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      boolean update = false;

      for (final INaviViewNode node : nodes) {
        update |= processRemovedNode(node);
      }

      if (update) {
        m_tableModel.setCrossReferences(m_crossReferences);
      }
    }
  }
}
