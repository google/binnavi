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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.zylib.system.IdaException;
import com.google.security.zynamics.zylib.system.IdaHelpers;
import com.google.security.zynamics.zylib.system.SystemHelpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UnknownFormatConversionException;

/**
 * This class is used to import IDB files with BinExport.
 */
public final class CBinExportImporter extends CBaseExporter {
  /**
   * Path to the BinExport IDC file.
   */
  private static final String BINEXPORT_VERSION = "zynamics_binexport_9";
  private static final String BINEXPORT_IDC_FILE_CONTENT = "#include <idc.idc>\n" + //
      "static main() {\n" + //
      "  Batch(0);\n" + //
      "  Wait();\n" + //
      "  RunPlugin(\"" + BINEXPORT_VERSION + "\", 1);\n" + //
      "  Exit(0);\n" + //
      "}";
  private static final String BINEXPORT_IDC_FILE = "binexport.idc";

  /**
   * Creates the IDA Pro process that is used to export the data from the IDB file to the database.
   *
   * @param idaExe The location of the IDA Pro executable.
   * @param idbFileName The location of the IDB file to import.
   * @param host Host of the database.
   * @param port Port of the database.
   * @param user Name of the user used to connect to the database.
   * @param password Password of the user.
   * @param name Name of the database to connect to.
   *
   * @return The spawned IDA Pro process.
   *
   * @throws IdaException Thrown if the IDA Pro process could not be created.
   */
  private static Process createIdaProcess(final String idaExe,
      final String idbFileName,
      final String host,
      final int port,
      final String user,
      final String password,
      final String name) throws IdaException {

    final String tempPath = SystemHelpers.getTempDirectory();
    final File idcFile = new File(tempPath + BINEXPORT_IDC_FILE);
    try {
      if (!idcFile.exists()) {
        idcFile.createNewFile();
      }
      FileWriter fw = new FileWriter(idcFile.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(BINEXPORT_IDC_FILE_CONTENT);
      bw.close();
    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);
    }
    final String idcPath = idcFile.getAbsolutePath();

    // Setup the invocation of the IDA to SQL exporter
    final ProcessBuilder processBuilder = new ProcessBuilder(idaExe,
        "-A",
        "-OExporterHost:" + host,
        "-OExporterPort:" + port,
        "-OExporterUser:" + user,
        "-OExporterPassword:" + password,
        "-OExporterDatabase:" + name,
        "-OExporterSchema:public",
        IdaHelpers.getSArgument(idcPath, SystemHelpers.isRunningWindows()),
        idbFileName);

    // Now launch the exporter to export the IDB to the database
    try {
      Process processInfo = null;

      processBuilder.redirectErrorStream(true);
      processInfo = processBuilder.start();

      // Java manages the streams internally - if they are full, the process blocks, i.e. IDA
      // hangs, so we need to consume them.
      final BufferedReader reader =
          new BufferedReader(new InputStreamReader(processInfo.getInputStream()));
      @SuppressWarnings("unused")
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      } catch (final IOException exception) {
        reader.close();
      }
      reader.close();

      return processInfo;
    } catch (final Exception exception) {
      try {
        CUtilityFunctions.logException(exception);
      } catch (final UnknownFormatConversionException e) {
        // Some Windows error messages contain %1 characters.
      }

      throw new IdaException(
          "E00210: Failed attempting to launch the importer with IDA: " + exception, exception);
    }
  }

  /**
   * Removes the port information from the host string.
   *
   * @param host The host string.
   *
   * @return The host string without the port information.
   */
  private static String getHost(final String host) {
    final int colonIndex = host.indexOf(':');

    if (colonIndex == -1) {
      return host;
    }

    return host.substring(0, colonIndex);
  }

  /**
   * Extracts the database server port from the host string.
   *
   * @param host Host string.
   *
   * @return The server port of the host string or the default database server port.
   */
  private static int getPort(final String host) {
    final int colonIndex = host.indexOf(':');
    if (colonIndex == -1) {
      return 5432;
    }

    final String portString = host.substring(colonIndex + 1);
    try {
      return Integer.valueOf(portString);
    } catch (final NumberFormatException exception) {
      return 5432;
    }
  }

  /**
   * Imports an IDB File. Use this function if you want to import an IDB file without showing any
   * dialogs and if you want different exception types depending on what goes wrong while importing.
   *
   * @param idaDirectory Path to the IDA Pro installation directory
   * @param idbFile Path to the IDB file to import.
   * @param database Import target.
   *
   * @throws ConfigFileException Thrown if the exporter configuration file could not be saved.
   * @throws IdaException Thrown if the IDA Pro process could not be started.
   * @throws ExporterException Thrown if the exporter failed for some reason.
   */
  @Override
  protected void importModuleInternal(final String idbFile, final String idaDirectory,
      final IDatabase database) throws ConfigFileException, IdaException, ExporterException {
    CImporterManager.instance().startImporting(database, idbFile);

    try {
      int exitCode = 0;
      do {
        final String host = getHost(database.getConfiguration().getHost());
        final int port = getPort(database.getConfiguration().getHost());
        final String user = database.getConfiguration().getUser();
        final String password = database.getConfiguration().getPassword();
        final String name = database.getConfiguration().getName();

        final Process processInfo = createIdaProcess(idaDirectory + File.separatorChar
            + (idbFile.endsWith("idb") ? IdaHelpers.IDA32_EXECUTABLE : IdaHelpers.IDA64_EXECUTABLE),
            idbFile,
            host,
            port,
            user,
            password,
            name);

        try {
          exitCode = processInfo.waitFor();

          handleExitCode(exitCode);
        } catch (final InterruptedException e) {
          CUtilityFunctions.logException(e);
          // restore the interrupted status of the thread.
          // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
          Thread.currentThread().interrupt();
        }
      } while ((exitCode & 0xFE) == 0xFE);
    } finally {
      CImporterManager.instance().finishImporting(database, idbFile);
    }
  }
}
