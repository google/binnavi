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
 package com.google.security.zynamics.binnavi.Gui.Scripting;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.IStandardDescriptionUpdater;
import com.google.security.zynamics.zylib.gui.scripting.ConsoleWriter;
import com.google.security.zynamics.zylib.gui.scripting.ScriptRunner;
import com.google.security.zynamics.zylib.io.DirUtils;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.io.IDirectoryTraversalCallback;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads all plugin scripts from the default scripts directory.
 */
public final class ScriptLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private ScriptLoader() {}

  /**
   * Executes a single script file.
   *
   * @param file The script file to execute.
   * @param pluginInterface Plugin interface that makes the BinNavi API accessible for the scripts.
   * @param <T> The interface which is used by plugins to interface with
   *        com.google.security.zynamics.binnavi.
   */
  private static <T> void executeScript(final File file, final T pluginInterface) {
    final List<Pair<String, Object>> bindings = new ArrayList<>();

    final ConsoleWriter consoleWriter = new ConsoleWriter(new StringWriter());

    bindings.add(new Pair<String, Object>("navi", pluginInterface));
    bindings.add(new Pair<String, Object>("SCRIPT_CONSOLE", consoleWriter));

    try {
      ScriptRunner.runScript(file, bindings);

      if (consoleWriter.getOutput() != null) {
        NaviLogger.info(consoleWriter.getOutput());
      }
    } catch (final Exception exception) {
      final String message = "E00061: " + "Script caused an error while loading";
      final String description = CUtilityFunctions.createDescription(String.format(
          "The script file '%s' could not be loaded because it caused an exception. "
          + "You should check the stdout console to see errors in the "
          + "script which were not passed to BinNavi.", file.getAbsoluteFile()),
          new String[] {"The script contains a bug that caused the exception"}, new String[] {
              "The script was not loaded and the functionality of the "
              + "script will not be available in BinNavi"});

      NaviLogger.severe(message);
      CUtilityFunctions.logException(exception);
      NaviLogger.severe(consoleWriter.getOutput());

      NaviErrorDialog.show(null, message, description, exception);
    }
  }

  /**
   * Determines whether a directory is the scripts/lib directory
   *
   * @param scriptDir The BinNavi scripts directory.
   * @param directory The directory to test.
   *
   * @return True, if the directory is the scripts/lib directory. False, otherwise.
   */
  private static boolean isLibDirectory(final File scriptDir, final File directory) {
    if (directory.getParentFile().equals(scriptDir)) {
      final String[] splitPath =
          directory.getAbsolutePath().split("\\".equals(File.separator) ? "\\\\" : File.separator);

      final String lastPath = splitPath[splitPath.length - 1];

      return "lib".equals(lastPath);
    }

    return false;
  }

  /**
   * Collects all script files that can be found in the default scripts directory.
   *
   * @param startupPath BinNavi startup path.
   *
   * @return Script files found in the scripts path.
   */
  public static Set<File> collectScripts(final String startupPath) {
    final Set<File> scriptFiles = new HashSet<>();

    final String scriptPath = startupPath + File.separator + "scripts";

    final File scriptDir = new File(scriptPath);

    DirUtils.traverse(scriptDir, new IDirectoryTraversalCallback() {

      private boolean skip = false;

      @Override
      public void entering(final File directory) {
        skip = isLibDirectory(scriptDir, directory);
      }

      @Override
      public void leaving(final File directory) {
        skip = false;
      }

      @Override
      public void nextFile(final File file) {
        if (skip) {
          return;
        }

        final String filename = FileUtils.getFileBasename(file);

        if ("init".equals(filename) || !ScriptRunner.canRun(file)) {
          return;
        }

        scriptFiles.add(file);
      }
    });

    return scriptFiles;
  }

  /**
   * Loads and executes a number of script files.
   *
   * @param scriptFiles Script files to execute.
   * @param pluginInterface Plugin interface that makes the BinNavi API accessible for the scripts.
   * @param descriptionUpdater Optional updater for the progress dialog.
   * @param <T> The interface which is used by plugins to interface with
   *        com.google.security.zynamics.binnavi.
   */
  public static <T> void init(final Set<File> scriptFiles, final T pluginInterface,
      final IStandardDescriptionUpdater descriptionUpdater) {
    descriptionUpdater.reset();
    descriptionUpdater.setMaximum(scriptFiles.size());

    for (final File file : scriptFiles) {
      final String rawFilename = file.getName();

      descriptionUpdater.next();
      descriptionUpdater.setDescription(String.format("Loading script file '%s'", rawFilename));

      executeScript(file, pluginInterface);
    }
  }
}
