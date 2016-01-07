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
package com.google.security.zynamics.binnavi.Gui.Scripting;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.LastDirFileChooser;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.gui.scripting.ConsoleWriter;
import com.google.security.zynamics.zylib.gui.scripting.IScriptConsole;
import com.google.security.zynamics.zylib.gui.scripting.IScriptPanel;
import com.google.security.zynamics.zylib.gui.scripting.ScriptRunner;
import com.google.security.zynamics.zylib.gui.scripting.ScriptingMenuBar;
import com.google.security.zynamics.zylib.gui.scripting.console.ConsolePane;
import com.google.security.zynamics.zylib.io.FileUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Dialog where the user can enter and execute scripts that make use of the BinNavi API.
 */
public final class CScriptingDialog extends JDialog {
  /**
   * Tab where the individual scripts panels are shown.
   */
  private final JTabbedPane scriptTab = new JTabbedPane();

  /**
   * Menu bar of the scripting dialog.
   */
  private final MenuBar menuBar;

  /**
   * Object bindings passed to each scripting panel.
   */
  private final Map<String, Object> m_bindings = new HashMap<String, Object>();

  /**
   * Scripting language selected by default when a new scripting panel is opened.
   */
  private final String defaultLanguage;

  /**
   * Path to the BinNavi scripts directory.
   */
  private final String scriptsDirectory;

  /**
   * Creates a new scripting dialog.
   *
   * @param owner Parent window of the dialog.
   * @param defaultLanguage Scripting language selected by default when a new scripting panel is
   *        opened.
   * @param pluginInterface Plugin interface that makes the BinNavi API accessible for the scripts.
   */
  public CScriptingDialog(
      final Frame owner, final String defaultLanguage, final PluginInterface pluginInterface) {
    this(owner, defaultLanguage, pluginInterface, new HashMap<String, Object>());
  }

  /**
   * Creates a new scripting dialog.
   *
   * @param owner Parent window of the dialog.
   * @param defaultLanguage Scripting language selected by default when a new scripting panel is
   *        opened.
   * @param pluginInterface Plugin interface that makes the BinNavi API accessible for the scripts.
   * @param bindings Additional object bindings passed to the scripts.
   */
  public CScriptingDialog(final Frame owner, final String defaultLanguage,
      final PluginInterface pluginInterface, final Map<String, Object> bindings) {
    super(owner, "Scripting", false);

    this.defaultLanguage = defaultLanguage;
    scriptsDirectory = PluginInterface.instance().getProgramPath() + File.separator + "scripts";

    addBinding("navi", pluginInterface);
    addBinding("dbs", pluginInterface.getDatabaseManager());

    for (final Entry<String, Object> binding : bindings.entrySet()) {
      addBinding(binding.getKey(), binding.getValue());
    }

    setLayout(new BorderLayout());
    menuBar = new MenuBar();
    setJMenuBar(menuBar);
    scriptTab.addChangeListener(new InternalChangeListener());
    final ConsolePane initialPanel = new ConsolePane();
    initPanel(initialPanel);
    scriptTab.addTab("Console", initialPanel);
    add(scriptTab, BorderLayout.CENTER);
    setResizable(true);
    pack();
  }

  /**
   * Adds an additional object binding.
   *
   * @param key Variable name of the object binding.
   * @param value Object to bind the variable to.
   */
  private void addBinding(final String key, final Object value) {
    m_bindings.put(key, value);
  }

  /**
   * Initializes a new scripting console panel.
   *
   * @param panel The panel to initialize.
   */
  private void initPanel(final ConsolePane panel) {
    panel.setLanguage(defaultLanguage);

    panel.setProgressWindowTitle("Scripting");

    for (final Entry<String, Object> binding : m_bindings.entrySet()) {
      panel.addBinding(binding.getKey(), binding.getValue());
    }

    panel.setConsoleName("SCRIPT_CONSOLE");
    panel.setLibraryPath(scriptsDirectory + File.separator + "lib");
  }

  /**
   * Updates the dialog on changes in the open tabs.
   */
  private class InternalChangeListener implements ChangeListener {
    @Override
    public void stateChanged(final ChangeEvent event) {
      final Component currentTab = scriptTab.getSelectedComponent();

      if (currentTab == null) {
        // Close the dialog if no tab is open
        dispose();
        return;
      }

      menuBar.updateGui(currentTab instanceof ConsolePane);
    }
  }

  /**
   * Menu bar of the script window.
   */
  private class MenuBar extends ScriptingMenuBar {
    /**
     * Executes a script file without displaying it in the dialog.
     *
     * @param scriptFile The file to execute.
     */
    private void executeScriptFile(final File scriptFile) {
      final List<Pair<String, Object>> bindings = toPairList(m_bindings);

      final ScriptEngineManager manager = new ScriptEngineManager();

      final ScriptEngine engine =
          manager.getEngineByExtension(FileUtils.getFileExtension(scriptFile));

      final IScriptConsole console = new ConsoleWriter(new StringWriter());

      engine.getContext().setWriter(console.getWriter());

      bindings.add(new Pair<String, Object>("SCRIPT_CONSOLE", console));

      final ScriptThread thread = new ScriptThread(engine, scriptFile, bindings);

      CProgressDialog.showEndless(
          getOwner(), String.format("Executing '%s'", scriptFile.getAbsolutePath()), thread);

      if (thread.getException() != null) {
        CUtilityFunctions.logException(thread.getException());

        final String message = "E00108: " + "Script file could not be executed";
        final String description = CUtilityFunctions.createDescription(String.format(
            "The script file '%s' could not be executed.",
            scriptFile.getAbsolutePath()), new String[] {
            "The script file is in use by another program and can not be read.",
            "You do not have sufficient rights to read the file",
            "The script contains a bug that caused an exception."},
            new String[] {"BinNavi can not read the script file."});

        NaviErrorDialog.show(CScriptingDialog.this, message, description, thread.getException());
      }

      final IScriptPanel panel = (IScriptPanel) scriptTab.getSelectedComponent();

      panel.setOutput(console.getOutput());

      toFront();
    }

    /**
     * Converts a hash map of bindings to a list of binding pairs.
     *
     * @param bindings The hash map of bindings to convert.
     *
     * @return The list of binding pairs.
     */
    private List<Pair<String, Object>> toPairList(final Map<String, Object> bindings) {
      final List<Pair<String, Object>> blist = new ArrayList<Pair<String, Object>>();

      for (final Map.Entry<String, Object> pair : bindings.entrySet()) {
        blist.add(new Pair<String, Object>(pair.getKey(), pair.getValue()));
      }

      return blist;
    }

    @Override
    protected void closeTabMenuClicked() {
      final Component activePanel = scriptTab.getSelectedComponent();

      if (activePanel == null) {
        return;
      }

      scriptTab.remove(activePanel);
    }

    @Override
    protected void copyMenuClicked() {
      final ConsolePane panel = (ConsolePane) scriptTab.getSelectedComponent();

      if (panel == null) {
        return;
      }
    }

    @Override
    protected void cutMenuClicked() {
      final ConsolePane panel = (ConsolePane) scriptTab.getSelectedComponent();

      if (panel == null) {
        return;
      }
    }

    @Override
    protected void executeAgainMenuClicked() {
      executeScriptFile(getLastExecutedScriptFile());
    }

    @Override
    protected void executeMenuClicked() {
      final LastDirFileChooser chooser = new LastDirFileChooser();
      if (chooser.showOpenDialog(CScriptingDialog.this) == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = chooser.getSelectedFile();
        executeScriptFile(selectedFile);
        setLastExecutedScriptFile(selectedFile);
      }
    }

    @Override
    protected void newConsoleTabMenuClicked() {
      final ConsolePane panel = new ConsolePane();
      initPanel(panel);
      scriptTab.addTab("Console", panel);
      scriptTab.setSelectedComponent(panel);
    }

    @Override
    protected void pasteMenuClicked() {
      final ConsolePane panel = (ConsolePane) scriptTab.getSelectedComponent();

      if (panel == null) {
        return;
      }
    }
  }

  /**
   * Helper thread to execute a script in the background while a progress bar is shown.
   */
  private static class ScriptThread extends CEndlessHelperThread {
    /**
     * Script engine used to execute the script.
     */
    private final ScriptEngine m_engine;

    /**
     * The script file to execute.
     */
    private final File m_selectedFile;

    /**
     * List of variable bindings for the script.
     */
    private final List<Pair<String, Object>> m_pairList;

    /**
     * Creates a new thread object.
     *
     * @param engine Script engine used to execute the script.
     * @param selectedFile The script file to execute.
     * @param bindings List of variable bindings for the script.
     */
    public ScriptThread(final ScriptEngine engine, final File selectedFile,
        final List<Pair<String, Object>> bindings) {
      m_engine = engine;
      m_selectedFile = selectedFile;
      m_pairList = bindings;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      ScriptRunner.runScript(m_engine, m_selectedFile, m_pairList);
    }

    @Override
    public void closeRequested() {
      finish();
    }
  }
}
