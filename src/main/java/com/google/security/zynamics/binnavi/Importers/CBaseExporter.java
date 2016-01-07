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

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;
import com.google.security.zynamics.zylib.system.IdaException;

/**
 * Base class for all concrete IDA import classes.
 */
public abstract class CBaseExporter {
  /**
   * Converts IDA Pro exit codes into exceptions with messages that explain the problem.
   *
   * @param exitCode The IDA Pro exit code to turn into an exception.
   *
   * @throws ExporterException The exception created from the given exit code.
   */
  protected static void handleExitCode(final int exitCode) throws ExporterException {
    if ((exitCode != 0) && ((exitCode & 0xFE) != 0xFE)) {
      // the process terminated with an error code

      if (exitCode == 1) {
        throw new ExporterException("Error: IDA could not open the input file (Return Code: 1)");
      }
      if (exitCode == 4) {
        throw new ExporterException("Error: Invalid IDB file (Return Code: 4)");
      }
      if (exitCode == 5) {
        throw new ExporterException("Error: IDA can not process archive files (Return Code: 5)");
      }
      if (exitCode == 0x10) {
        throw new ExporterException("Error: The exporter could not connect to the database");
      }
      if (exitCode == 0x11) {
        throw new ExporterException(
            "Error: Can't import. The module already exists in the database");
      }
      if (exitCode == 0x12) {
        throw new ExporterException(
            "Error: Can't import. Database contains tables from old versions of BinNavi.");
      }
      throw new ExporterException("Error: IDA terminated with error code " + exitCode);
    }
  }

  /**
   * Imports an IDB File. Use this function if you want to import an IDB file without showing any
   * dialogs and if you want different exception types depending on what goes wrong while importing.
   *
   * @param idaLocation Path to the IDA Pro executable file.
   * @param idbFile Path to the IDB file to import.
   * @param database Import target.
   *
   * @throws ConfigFileException Thrown if the exporter configuration file could not be saved.
   * @throws IdaException Thrown if the IDA Pro process could not be started.
   * @throws ExporterException Thrown if the exporter failed for some reason.
   */
  protected abstract void importModuleInternal(final String idbFile, final String idaLocation,
      final IDatabase database) throws ConfigFileException, ExporterException, IdaException;

  /**
   * Imports an IDB File. Use this function if you want to import an IDB file without showing any
   * dialogs and if you only want one exception type.
   *
   * @param idaDirectory Path to the IDA Pro installation directory.
   * @param idbFile Path to the IDB file to import.
   * @param database Import target.
   *
   * @throws ImportFailedException Thrown if importing the module failed.
   */
  public void importIdbFile(final String idaDirectory, final String idbFile,
      final IDatabase database) throws ImportFailedException {
    try {
      importModule(idbFile, idaDirectory, database);
    } catch (final ConfigFileException configFileException) {
      throw new ImportFailedException(
          "Error: Could not create the IDA2SQL temp file.\nImporting project failed.\n" + configFileException);
    } catch (final IdaException idaException) {
      throw new ImportFailedException(
          "Error: Could not start IDA Pro.\nImporting project failed.\n" + idaException);
    } catch (final ExporterException exporterException) {
      throw new ImportFailedException(
          "Error: Importing project failed.\n" + exporterException);
    }
  }

  /**
   * Imports an IDB File. Use this function if you want to import an IDB file without showing any
   * dialogs and if you want different exception types depending on what goes wrong while importing.
   *
   * @param idaDirecory Path to the IDA Pro installation directory
   * @param idbFile Path to the IDB file to import.
   * @param database Import target.
   *
   * @throws ConfigFileException Thrown if the exporter configuration file could not be saved.
   * @throws IdaException Thrown if the IDA Pro process could not be started.
   * @throws ExporterException Thrown if the exporter failed for some reason.
   */
  public void importModule(final String idbFile, final String idaDirecory, final IDatabase database)
      throws ConfigFileException, IdaException, ExporterException {
    final CIdaImporterOperation operation = new CIdaImporterOperation(idbFile);

    try {
      importModuleInternal(idbFile, idaDirecory, database);
    } finally {
      operation.stop();
    }
  }

  /**
   * Progress operation class for importing IDA files.
   */
  private static class CIdaImporterOperation implements IProgressOperation {
    /**
     * Displays progress information about the load operation.
     */
    private final CProgressPanel m_progressBar;

    /**
     * Creates a new operation object.
     *
     * @param filename Name of the file to import.
     */
    public CIdaImporterOperation(final String filename) {
      m_progressBar =
          new CProgressPanel(String.format("Importing IDB file '%s'", filename), true, false);

      m_progressBar.start();

      CGlobalProgressManager.instance().add(this);
    }

    @Override
    public String getDescription() {
      return "Importing IDB file";
    }

    @Override
    public CProgressPanel getProgressPanel() {
      return m_progressBar;
    }

    /**
     * Stops the operation.
     */
    public void stop() {
      m_progressBar.stop();

      CGlobalProgressManager.instance().remove(this);
    }
  }
}
