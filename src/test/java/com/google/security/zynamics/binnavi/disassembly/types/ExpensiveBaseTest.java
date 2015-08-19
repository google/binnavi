/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The base class that should be used by all expensive tests. It automatically instantiates the test
 * database (plus all other resources it provides) and releases it after a test has run.
 *
 *  It is safe to use the provided environment in derived classes since JUnit guarantees that Before
 * methods of super classes are executed before the ones in derived classes (it is also safe to use
 * the provided environment in After methods in derived classes).
 *
 * @author jannewger@google.com (Jan Newger)
 *
 */
public class ExpensiveBaseTest {

  private long setupStartTime;
  private long setupEndTime;
  private long testStartTime;
  private long testEndTime;

  private static final String Kernel32String = "kernel32.dll";
  private static final String NotepadString = "notepad.exe";

  private CDatabase database;
  private SQLProvider provider;
  private INaviModule kernel32Module;
  private INaviModule notepadModule;
  private List<INaviModule> modules;

  private INaviModule findModule(final String name) {
    for (final INaviModule module : modules) {
      if (module.getConfiguration().getName().equalsIgnoreCase(name)) {
        if (!module.isLoaded()) {
          return module;
        }
      }
    }
    return null;
  }

  private INaviModule loadModule(final String name)
      throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = findModule(name);
    if (!module.isLoaded()) {
      module.load();
    }
    return module;
  }

  protected SQLProvider getProvider() {
    return provider;
  }

  @After
  public void destroyEnvironment() {
    testEndTime = System.nanoTime();
    NaviLogger.info("Test took: " + String.valueOf(
        TimeUnit.SECONDS.convert(testEndTime - testStartTime, TimeUnit.NANOSECONDS)) + " seconds");
    if (kernel32Module != null && kernel32Module.isLoaded()) {
      kernel32Module.close();
    }
    if (notepadModule != null && notepadModule.isLoaded()) {
      notepadModule.close();
    }
    database.close();
  }

  public CDatabase getDatabase() {
    return database;
  }

  public INaviModule getKernel32Module() throws CouldntLoadDataException, LoadCancelledException {
    return loadModule(Kernel32String);
  }

  public INaviModule getNotepadModule() throws CouldntLoadDataException, LoadCancelledException {
    return loadModule(NotepadString);
  }

  public INaviEdge loadCallGraphEdge(final INaviModule module)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException {
    return loadCallGraphView(module).getGraph().getEdges().get(0);
  }

  public INaviView loadCallGraphView(final INaviModule module)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException {
    final INaviView view = module.getContent().getViewContainer().getNativeCallgraphView();
    view.load();
    return view;
  }

  public INaviCodeNode loadCodeNode(final INaviModule module, final String functionName)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException,
      MaybeNullException {
    return (INaviCodeNode) loadView(module, functionName).getGraph().getNodes().get(0);
  }

  public INaviEdge loadFlowGraphEdge(final INaviModule module, final String functionName)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException,
      MaybeNullException {
    return loadView(module, functionName).getGraph().getEdges().get(0);
  }

  public INaviFunction loadFunction(final INaviModule module, final String name)
      throws CouldntLoadDataException, MaybeNullException {
    final INaviFunction function = module.getContent().getFunctionContainer().getFunction(name);
    function.load();
    return function;
  }

  public INaviFunctionNode loadFunctionNode(final INaviModule module)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException {
    final INaviView view = loadCallGraphView(module);
    return (INaviFunctionNode) view.getGraph().getNodes().get(0);
  }

  public INaviInstruction loadInstruction(final INaviModule module, final String functionName)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException,
      MaybeNullException {
    return Iterables.getLast(loadCodeNode(module, functionName).getInstructions());
  }

  public INaviProject loadProject() throws CouldntSaveDataException {
    final List<INaviProject> projects = provider.getProjects();
    if (!projects.isEmpty()) {
      return projects.get(0);
    } else {
      return provider.createProject("PROJECT");
    }
  }

  public Section loadSection(final INaviModule module) {
    return module.getContent().getSections().getSections().get(0);
  }

  public INaviView loadView(final INaviModule module, final String name)
      throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException,
      MaybeNullException {
    final INaviView view = module.getContent()
        .getViewContainer().getView(module.getContent().getFunctionContainer().getFunction(name));
    view.load();
    return view;
  }

  // TODO(jannewger): we should probably use the decorator pattern to give deriving classes more
  // control over the test setups, e.g. whether to load a project, which modules to load, whether to
  // also load a view, etc
  @Before
  public void setUpEnvironment() throws CouldntLoadDriverException, CouldntConnectException,
      InvalidDatabaseException, CouldntInitializeDatabaseException,
      InvalidExporterDatabaseFormatException, LoadCancelledException, CouldntLoadDataException,
      InvalidDatabaseVersionException, IOException, SecurityException, NoSuchFieldException,
      IllegalArgumentException, IllegalAccessException, FileReadException {
    setupStartTime = System.nanoTime();
    final String[] parts = CConfigLoader.loadPostgreSQL();
    database = new CDatabase(
        "Expensive base test database", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0],
        "test_disassembly", parts[1], parts[2], parts[3], false, false);
    database.connect();
    database.load();

    final Field privateProviderField = CDatabase.class.getDeclaredField("provider");
    privateProviderField.setAccessible(true);
    provider = ((SQLProvider) privateProviderField.get(database));
    ConfigManager.instance().read();
    modules = provider.loadModules();
    kernel32Module = findModule(Kernel32String);
    notepadModule = findModule(NotepadString);
    setupEndTime = System.nanoTime();
    NaviLogger.info("Setting up took: " + String.valueOf(
        TimeUnit.SECONDS.convert(setupEndTime - setupStartTime, TimeUnit.NANOSECONDS))
        + " seconds");
    testStartTime = System.nanoTime();
  }
}
