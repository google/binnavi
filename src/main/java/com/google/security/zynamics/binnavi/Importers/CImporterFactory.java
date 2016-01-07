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
package com.google.security.zynamics.binnavi.Importers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.IdaSelectionDialog.CIdaSelectionDialog;
import com.google.security.zynamics.binnavi.Gui.IdbSelection.CIdbSelectionDialog;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.system.IdaException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;

/**
 * Factory class for importing modules depending on a chosen exporter.
 */
public final class CImporterFactory {
  /**
   * You are not supposed to instantiate this class.
   */
  private CImporterFactory() {
  }

  /**
   * Returns the location of the IDA Pro installation directory
   *
   * @param parent Parent frame of the file dialog.
   * @return The absolute path to the IDA Pro installation directory
   * @throws FileNotFoundException Thrown if IDA Pro can't be found.
   */
  private static String getIdaDirectory(final JFrame parent)
      throws FileNotFoundException {
    final String idaLocation = ConfigManager.instance().getGeneralSettings().getIdaDirectory();

    final File idaExec = new File(idaLocation);

    if (idaExec.exists()) {
      return idaExec.getAbsolutePath();
    }

    do {
      final String newIdaLocation = promptForIdaDirectory(parent);

      if (newIdaLocation != null) {
        return newIdaLocation;
      }
    } while (true);
  }

  /**
   * Imports an IDB file while showing a progress dialog.
   *
   * @param idaDirectory Location of the IDA Pro installation directory
   * @param idbFile Location of the IDB file to import.
   * @param database Import target.
   * @param exporter
   * @throws ImportFailedException Thrown if some kind of problem occurred during the import
   *         process.
   */
  private static void importIdbFileInternal(final String idaDirectory, final String idbFile,
      final IDatabase database, final CBaseExporter exporter) throws ImportFailedException {
    try {
      exporter.importModule(idbFile, idaDirectory, database);
    } catch (final ConfigFileException exception) {
      throw new ImportFailedException(String.format(
          "Could not create the IDA2SQL temp file." + "\n" + "Importing project failed."));
    } catch (final IdaException exception) {
      throw new ImportFailedException(String.format(
          "Could not start IDA Pro." + "\n" + "Importing project failed."));
    } catch (final ExporterException exception) {
      throw new ImportFailedException(exception.getLocalizedMessage());
    }
  }

  /**
   * Shows a dialog that asks the user for the location of the IDA Pro installation directory
   *
   * @param parent Parent window used for dialogs.
   * @return The location of the IDA Pro executable or null if the selected file does not exist.
   * @throws FileNotFoundException Thrown if no IDA Pro executable file was selected.
   */
  private static String promptForIdaDirectory(final JFrame parent)
      throws FileNotFoundException {
    // If the IDA location is invalid, tell the user to select the location
    CMessageBox.showError(
        parent, "Could not locate IDA Pro. Please select the IDA Pro installation directory");

    final CIdaSelectionDialog fileChooser = CIdaSelectionDialog.show(
        parent, ConfigManager.instance().getGeneralSettings().getIdaDirectory());

    if (fileChooser.getSelectedFile() != null) {
      final File idaDirectory = fileChooser.getSelectedFile();

      // The returned file path does not exist, keep asking...
      if (!idaDirectory.exists()) {
        return null;
      }

      ConfigManager.instance().getGeneralSettings().setIdaDirectory(idaDirectory.getAbsolutePath());

      return idaDirectory.getAbsolutePath();
    }

    throw new FileNotFoundException(
        "E00211: The IDA directory is invalid or not set, please configure the location of your IDA.");
  }

  /**
   * Updates the list of previous directories.
   *
   * @param lastDirectories The list to update.
   * @param selectedFiles The files selected by the user.
   */
  private static void updatePreviousFiles(
      final List<String> lastDirectories, final List<File> selectedFiles) {
    final int MAXIMUM_LIST_SIZE = 5;

    final Set<String> selectedDirectories = new LinkedHashSet<String>();

    for (final File file : selectedFiles) {
      selectedDirectories.add(file.getParent());

      if (selectedDirectories.size() == MAXIMUM_LIST_SIZE) {
        break;
      }
    }

    for (final String lastDirectory : lastDirectories) {
      selectedDirectories.add(lastDirectory);

      if (selectedDirectories.size() == MAXIMUM_LIST_SIZE) {
        break;
      }
    }

    lastDirectories.clear();
    lastDirectories.addAll(selectedDirectories);
  }

  /**
   * Imports IDB Files. Use this function if you want to import IDB files by showing a dialog to let
   * the user choose the IDB files.
   *
   * @param parent Parent frame of the file chooser.
   * @param database Import target.
   * @param failedImports The list will return the modules which failed to import.
   * @return The result of the import operation.
   *
   * @throws FileNotFoundException Thrown if the IDA Pro executable could not be found.
   * @throws ExecutionException Thrown if fetching the result from the background worker threads
   *         failed because the thread itself threw an exception.
   * @throws InterruptedException Thrown if one of the background worker threads have been
   *         interrupted during processing.
   */
  public static boolean importModules(final JFrame parent, final IDatabase database,
      final List<CFailedImport> failedImports)
      throws FileNotFoundException, InterruptedException, ExecutionException {
    Preconditions.checkNotNull(parent, "IE02358: parent argument can not be null");
    Preconditions.checkNotNull(database, "IE02359: database argument can not be null");
    Preconditions.checkNotNull(failedImports, "IE02361: failedImports argument can not be null");

    final List<String> lastDirectories =
        ConfigManager.instance().getGeneralSettings().getIdbDirectories();

    // Try to reopen the directory of the last selected IDB file
    final CIdbSelectionDialog dialogIdb = CIdbSelectionDialog.show(parent, lastDirectories);

    final List<File> selectedFiles =
        dialogIdb == null ? new ArrayList<File>() : dialogIdb.getSelectedFiles();

    if (selectedFiles.isEmpty()) {
      return false;
    }

    final CBaseExporter exporter = new CBinExportImporter();
    ConfigManager.instance()
        .getGeneralSettings().setDefaultExporter(dialogIdb.getSelectedExporter().ordinal());
    updatePreviousFiles(lastDirectories, selectedFiles);

    final ExecutorService executor = Executors.newFixedThreadPool(
        dialogIdb.getNumberOfParallelImports());
    final List<Future<CFailedImport>> results = new ArrayList<Future<CFailedImport>>();
    try {
      // To import from IDA, it's necessary to know where IDA is.
      final String idaDirectory = getIdaDirectory(parent);

      for (final File file : selectedFiles) {
        if (!file.exists()) {
          CMessageBox.showError(parent,
              String.format("The selected IDB file '%s' does not exist.", file.getAbsolutePath()));
          continue;
        }

        results.add(executor.submit(new Callable<CFailedImport>() {
          @Override
          public CFailedImport call() {
            try {
              importIdbFileInternal(idaDirectory, file.getAbsolutePath(), database, exporter);
              return null;
            } catch (final ImportFailedException exception) {
              CUtilityFunctions.logException(exception);
              return new CFailedImport(file.getAbsolutePath(), exception);
            }
          }
        }));
      }

      for (final Future<CFailedImport> future : results) {
        if (future.get() != null) {
          failedImports.add(future.get());
        }
      }

      return true;
    } catch (final FileNotFoundException e) {
      CUtilityFunctions.logException(e);

      throw new FileNotFoundException(String.format(
          "Invalid IDA Pro executable file." + "\n" + "Importing project failed."));
    }
  }
}
