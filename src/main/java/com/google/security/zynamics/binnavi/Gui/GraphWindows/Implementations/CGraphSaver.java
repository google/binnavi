/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CViewCommentDialog;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CViewInserter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

import java.awt.Window;

/**
 * Contains functions for saving graphs to the database.
 */
public final class CGraphSaver {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphSaver() {
  }

  /**
   * Creates a copy of a graph and opens it.
   *
   * @param parent Parent window where the graph is shown after cloning.
   * @param view The view to clone.
   * @param container The view container where the cloned graph is stored.
   */
  public static void clone(final CGraphWindow parent, final INaviView view,
      final IViewContainer container) {
    final CloneThread cloneThread = new CloneThread(parent, view, container);

    CProgressDialog.showEndless(parent, String.format("Cloning view '%s'", view.getName()),
        cloneThread);

    if (cloneThread.getException() != null) {
      CUtilityFunctions.logException(cloneThread.getException());

      final String innerMessage = "E00118: " + "View could not be cloned";
      final String innerDescription =
          CUtilityFunctions
              .createDescription(
                  String.format("The view '%s' could not be cloned.", view.getName()),
                  new String[] {"It is unclear why this problem occurred. Please check the stack trace for more information."},
                  new String[] {"The view was not cloned."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, cloneThread.getException());
    }
  }

  /**
   * Stores a graph to the database.
   *
   * @param parent Parent window used to display dialogs.
   * @param graph Graph to be written to the database.
   *
   * @return Information about the save progress.
   */
  public static CSaveProgress save(final Window parent, final ZyGraph graph) {
    Preconditions.checkNotNull(parent, "IE01752: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01753: Graph argument can not be null");

    final CSaveProgress progress = new CSaveProgress(false);

    new Thread() {
      @Override
      public void run() {
        final CViewSaverOperation operation =
            new CViewSaverOperation(String.format("Saving view '%s'", graph.getRawView().getName()));

        try {
          if (!graph.save()) {
            throw new CouldntSaveDataException("Something went wrong saving");
          }
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);

          final String innerMessage = "E00120: " + "Could not save graph";
          final String innerDescription =
              CUtilityFunctions.createDescription(String.format(
                  "The function '%s' could not be saved.", graph.getRawView().getName()),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The graph remains unsaved."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
        } finally {
          operation.stop();

          progress.setDone();
        }
      }
    }.start();

    return progress;
  }

  /**
   * Prompts the user for a new graph description and stores a copy of the graph in the database.
   *
   * @param parent Parent window used to display dialogs.
   * @param graph Graph to be written to the database.
   * @param container View container the graph is written to.
   *
   * @return Information about the save progress.
   */
  public static CSaveProgress saveAs(final Window parent, final ZyGraph graph,
      final IViewContainer container) {
    Preconditions.checkNotNull(parent, "IE01754: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01755: Graph argument can not be null");

    final INaviView view = graph.getRawView();

    final CViewCommentDialog dlg =
        new CViewCommentDialog(parent, "Save", view.getName(), view.getConfiguration()
            .getDescription());

    dlg.setVisible(true);

    if (dlg.wasCancelled()) {
      return new CSaveProgress(true);
    }

    final String newName = dlg.getName();
    final String newDescription = dlg.getComment();

    final CSaveProgress progress = new CSaveProgress(false);

    new Thread() {
      @Override
      public void run() {
        final CViewSaverOperation operation =
            new CViewSaverOperation(String.format("Saving view '%s'", newName));

        try {
          if (graph.saveAs(container, newName, newDescription) == null) {
            throw new CouldntSaveDataException("Failure saving the view.");
          }
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);

          final String innerMessage = "E00121: " + "Could not save graph";
          final String innerDescription =
              CUtilityFunctions.createDescription(
                  String.format("The view '%s' could not be saved.", newName),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The new view was not created."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
        } finally {
          operation.stop();

          progress.setDone();
        }
      }
    }.start();

    return progress;
  }

  /**
   * Used to display a progress dialog while cloning.
   */
  private static class CloneThread extends CEndlessHelperThread {
    /**
     * Parent window where the graph is shown after cloning.
     */
    private final CGraphWindow m_parent;

    /**
     * The view to clone.
     */
    private final INaviView m_view;

    /**
     * The view container where the cloned graph is stored.
     */
    private final IViewContainer m_container;

    /**
     * Creates a new thread object.
     *
     * @param parent Parent window where the graph is shown after cloning.
     * @param view The view to clone.
     * @param container The view container where the cloned graph is stored.
     */
    private CloneThread(final CGraphWindow parent, final INaviView view,
        final IViewContainer container) {
      m_parent = Preconditions.checkNotNull(parent, "IE02387: parent argument can not be null");
      m_view = Preconditions.checkNotNull(view, "IE02388: view argument can not be null");
      m_container =
          Preconditions.checkNotNull(container, "IE02389: container argument can not be null");
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      final INaviView newView =
          m_container.createView(String.format("Clone of %s", m_view.getName()), m_view
              .getConfiguration().getDescription());
      CViewInserter.insertView(m_view, newView);
      CViewOpener.showView(m_parent, m_container, newView, m_parent);
    }
  }

  /**
   * Progress operation for view saving.
   */
  private static final class CViewSaverOperation implements IProgressOperation {
    /**
     * Displays progress information about the module load operation.
     */
    private final CProgressPanel m_progressPanel = new CProgressPanel("", true, false);

    /**
     * Creates a new operation object.
     *
     * @param description Description string shown during view saving.
     */
    public CViewSaverOperation(final String description) {
      CGlobalProgressManager.instance().add(this);
      m_progressPanel.setText(description);
      m_progressPanel.start();
    }

    @Override
    public String getDescription() {
      return "Saving view";
    }

    @Override
    public CProgressPanel getProgressPanel() {
      return m_progressPanel;
    }

    /**
     * Stops the load operation.
     */
    public void stop() {
      CGlobalProgressManager.instance().remove(this);
    }
  }
}
