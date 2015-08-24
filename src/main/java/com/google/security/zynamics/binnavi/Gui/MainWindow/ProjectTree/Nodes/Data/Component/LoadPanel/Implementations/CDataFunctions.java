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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.LoadPanel.Implementations;



import java.awt.Window;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.common.io.ByteStreams;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

/**
 * This class contains the implementations of all the actions that are available from the load
 * panel.
 */
public final class CDataFunctions {
  /**
   * Static helper class.
   */
  private CDataFunctions() {
    // You are not supposed to instantiate this.
  }

  /**
   * Loads the data of a module from the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module whose data is stored to the database.
   */
  public static void loadFromDatabase(final Window parent, final INaviModule module) {
    final LoadFromDatabaseThread thread = new LoadFromDatabaseThread(module);

    CProgressDialog.showEndless(parent, "Loading data from the database" + " ...", thread);

    final Exception exception = thread.getException();

    if (exception != null) {
      CUtilityFunctions.logException(exception);

      final String message = "E00199: " + "Could not load module data";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not load the module data of module '%s' from the database.", module
                  .getConfiguration().getName()),
              new String[] {"There was a problem with the database connection.",},
              new String[] {"The module data was not loaded from the database."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }

  /**
   * Loads module data from a file.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module whose data is updated.
   * @param filename Name of the file from which the data is read.
   */
  public static void loadFromFile(final Window parent, final INaviModule module,
      final String filename) {
    try {
      final byte[] data = ByteStreams.toByteArray(new FileInputStream(filename));
      module.setData(data);
    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00207: " + "Could not load module data from file";
      final String description =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not load the data of file '%s'.", filename),
              new String[] {"There was a problem reading the file.",},
              new String[] {"The module data was not loaded."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }

  /**
   * Stores the binary data of a module to the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module whose data is stored.
   */
  public static void storeData(final Window parent, final INaviModule module) {
    final SaveToDatabaseThread thread = new SaveToDatabaseThread(module);

    CProgressDialog.showEndless(parent, "Saving data to the database" + " ...", thread);

    final Exception exception = thread.getException();

    if (exception != null) {
      CUtilityFunctions.logException(exception);

      final String message = "E00200: " + "Could not save module data";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not save the module data of module '%s' to the database.", module
                  .getConfiguration().getName()),
              new String[] {"There was a problem with the database connection.",},
              new String[] {"The module data was not written to the database."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }

  /**
   * Background thread for loading the data of a module while a progress dialog is shown.
   */
  private static class LoadFromDatabaseThread extends CEndlessHelperThread {
    /**
     * Module whose data is loaded.
     */
    private final INaviModule m_module;

    /**
     * Creates a new thread object.
     * 
     * @param module Module whose data is loaded.
     */
    public LoadFromDatabaseThread(final INaviModule module) {
      m_module = module;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      m_module.loadData();
    }
  }

  /**
   * Background thread for saving the data of a module while a progress dialog is shown.
   */
  private static class SaveToDatabaseThread extends CEndlessHelperThread {
    /**
     * Module whose data is saved.
     */
    private final INaviModule m_module;

    /**
     * Creates a new thread object.
     * 
     * @param module Module whose data is saved.
     */
    public SaveToDatabaseThread(final INaviModule module) {
      m_module = module;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      m_module.saveData();
    }
  }
}
