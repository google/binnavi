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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import com.google.security.zynamics.binnavi.Database.CDatabaseLoader;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import java.awt.Window;

import javax.swing.JTree;
import javax.swing.SwingUtilities;



/**
 * Contains code for opening databases.
 */
public final class CDatabaseConnectionFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CDatabaseConnectionFunctions() {
  }

  /**
   * Opens a connection to a database in a thread.
   * 
   * @param parent Parent window used for dialogs.
   * @param projectTree Project tree to expand once the database is loaded. This argument can be
   *        null.
   * @param database Database to load.
   */
  private static void openDatabaseThreaded(final Window parent, final JTree projectTree,
      final IDatabase database) {
    new Thread() {
      @Override
      public void run() {
        boolean loadRequired = true;

        while (database.isLoading() || database.isConnecting()) {
          loadRequired = false;

          try {
            Thread.sleep(100);
          } catch (final InterruptedException e) {
            // restore the interrupted status of the thread.
            // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
          }
        }

        if (loadRequired) {
          CDatabaseLoader.loadDatabase(parent, database);
        }

        if ((projectTree != null) && database.isConnected()) {
          new SwingInvoker() {
            @Override
            protected void operation() {
              CNodeExpander.expandNode(projectTree, database);
            }
          }.invokeLater();
        }
      }
    }.start();
  }

  /**
   * Terminates the connection to given databases.
   * 
   * @param parent Parent frame used to display dialogs.
   * @param database The database to be closed.
   */
  public static void closeDatabase(final Window parent, final IDatabase database) {
    if (!database.close()) {
      CMessageBox
          .showInformation(
              parent,
              "Could not close the selected database because views or other elements from the database are still open.");
    }
  }

  /**
   * Creates a connection to a given database.
   * 
   * @param projectTree Project tree of the main window.
   * @param database The database to connect to.
   */
  public static void openDatabase(final JTree projectTree, final IDatabase database) {
    openDatabaseThreaded(SwingUtilities.getWindowAncestor(projectTree), projectTree, database);
  }

  /**
   * Creates a connection to a given database.
   * 
   * @param parent Parent window used for dialogs.
   * @param database The database to connect to.
   */
  public static void openDatabase(final Window parent, final IDatabase database) {
    openDatabaseThreaded(parent, null, database);
  }

}
