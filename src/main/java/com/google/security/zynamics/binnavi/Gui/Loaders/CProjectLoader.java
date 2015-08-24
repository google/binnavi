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
package com.google.security.zynamics.binnavi.Gui.Loaders;



import java.awt.Window;

import javax.swing.JTree;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CNodeExpander;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.IProjectListener;
import com.google.security.zynamics.binnavi.disassembly.ProjectLoadEvents;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

/**
 * This class can be used to load a project while showing the corresponding progress dialog.
 */
public final class CProjectLoader {
  /**
   * Static helper class.
   */
  private CProjectLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads a project inside a thread.
   * 
   * @param parent Parent window used for dialogs.
   * @param project Project to load.
   * @param projectTree Project tree to expand on project loading. This argument can be null.
   */
  private static void loadProjectInternal(final Window parent, final INaviProject project,
      final JTree projectTree) {
    final CProjectLoaderOperation operation = new CProjectLoaderOperation(project);

    try {
      project.load();

      if (projectTree != null) {
        new SwingInvoker() {
          @Override
          protected void operation() {
            CNodeExpander.expandNode(projectTree, project);
          }
        }.invokeLater();
      }
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00178: " + "Project data could not be loaded";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not load the project '%s'.", project.getConfiguration().getName()),
              new String[] {"The connection dropped while the data was loaded."},
              new String[] {"BinNavi can not open the project. To fix this situation try to "
                  + "load the project again. Restart BinNavi if necessary and contact the "
                  + "BinNavi support if the problem persists."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final LoadCancelledException e) {
      // Don't show the user that he cancelled the operation.
    } finally {
      operation.stop();
    }
  }

  /**
   * Loads a project inside a thread.
   * 
   * @param parent Parent window used for dialogs.
   * @param project Project to load.
   * @param projectTree Project tree to expand on project loading. This argument can be null.
   */
  private static void loadProjectThreaded(final Window parent, final INaviProject project,
      final JTree projectTree) {
    Preconditions.checkNotNull(parent, "IE00005: Parent argument can not be null");

    Preconditions.checkNotNull(project, "IE01284: Project argument can not be null");

    if (project.isLoading()) {
      return;
    }

    new Thread() {
      @Override
      public void run() {
        loadProjectInternal(parent, project, projectTree);
      }
    }.start();
  }

  /**
   * Loads a project while showing a progress dialog.
   * 
   * @param tree Project tree to expand on loading.
   * @param project The project to load.
   */
  public static void loadProject(final JTree tree, final INaviProject project) {
    Preconditions.checkNotNull(tree, "IE01435: Tree argument can not be null");

    Preconditions.checkNotNull(project, "IE01436: Project argument can not be null");

    loadProjectThreaded(SwingUtilities.getWindowAncestor(tree), project, tree);
  }

  /**
   * Operation class for project loading.
   */
  private static class CProjectLoaderOperation implements IProgressOperation {
    /**
     * Project to be loaded.
     */
    private final INaviProject m_project;

    /**
     * Displays progress information about the project load operation.
     */
    private final CProgressPanel m_progressPanel = new CProgressPanel("", false, true) {
      /**
       * Used for serialization.
       */
      private static final long serialVersionUID = -1163585238482641129L;


      @Override
      protected void closeRequested() {
        setText("Cancelling project loading");

        m_continue = false;
      }
    };

    /**
     * Used to cancel project initializations.
     */
    private boolean m_continue = true;

    /**
     * Updates the GUI on relevant changes in the project.
     */
    private final IProjectListener m_listener = new CProjectListenerAdapter() {
      /**
       * Flag that indicates whether the next event to arrive is the first one for a database load
       * operation.
       */
      private boolean m_first = true;

      @Override
      public boolean loading(final ProjectLoadEvents event, final int counter) {
        if (!m_continue) {
          m_continue = true;

          return false;
        }

        m_progressPanel.next();

        if (event == ProjectLoadEvents.Finished) {
          m_progressPanel.setVisible(false);

          m_first = true;
          m_continue = true;
        } else if (m_first) {
          m_progressPanel.setText("Loading project");
          m_progressPanel.setMaximum(ProjectLoadEvents.values().length);

          m_progressPanel.setValue(counter);

          m_first = false;
        }

        return true;
      }
    };

    /**
     * Creates a new loader operation.
     * 
     * @param project Project to be loaded.
     */
    public CProjectLoaderOperation(final INaviProject project) {
      m_project = project;

      CGlobalProgressManager.instance().add(this);

      project.addListener(m_listener);
    }

    @Override
    public String getDescription() {
      return "Loading project";
    }

    @Override
    public CProgressPanel getProgressPanel() {
      return m_progressPanel;
    }

    /**
     * Stops the load operation.
     */
    public void stop() {
      m_project.removeListener(m_listener);

      CGlobalProgressManager.instance().remove(this);
    }
  }
}
