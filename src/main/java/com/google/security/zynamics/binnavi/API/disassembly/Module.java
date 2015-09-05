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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphEdge;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphNode;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CTraceContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.ITraceContainerListener;
import com.google.security.zynamics.binnavi.disassembly.Modules.ModuleInitializeEvents;
import com.google.security.zynamics.binnavi.disassembly.Modules.ModuleLoadEvents;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// / Represents a single module.
/**
 * A module represents a single disassembled file and everything you can do with it. This means that
 * modules contain the original functions and the original Call graph as well as advanced features
 * like views and the debugger associated with a module.
 */
public class Module implements ApiObject<INaviModule>, ViewContainer {

  /**
   * Database the module belongs to.
   */
  private final Database m_database;

  /**
   * Wrapped internal module object.
   */
  private final INaviModule m_module;

  /**
   * Node tag manager of the module.
   */
  private final TagManager m_nodeTagManager;

  /**
   * View tag manager of the module.
   */
  private final TagManager m_viewTagManager;

  /**
   * Native call graph of the module.
   */
  private Callgraph m_callgraph;

  /**
   * Functions of the module.
   */
  private List<Function> m_functions;

  /**
   * Views of the module.
   */
  private List<View> m_views;

  /**
   * Debugger used to debug the module.
   */
  private Debugger m_debugger;

  /**
   * Debugger template of the module.
   */
  private DebuggerTemplate m_debuggerTemplate;

  /**
   * Traces recorded for the module.
   */
  private List<Trace> m_traces;

  /**
   * Listeners that are notified about changes in the module.
   */
  private final ListenerProvider<IModuleListener> m_listeners =
      new ListenerProvider<IModuleListener>();

  /**
   * Keeps the API module object synchronized with the internal module object.
   */
  private final InternalModuleListener m_listener = new InternalModuleListener();

  /**
   * For performance reasons we keep a map between internal function objects and API function
   * objects.
   */
  private final Map<INaviFunction, Function> m_functionMap = new HashMap<INaviFunction, Function>();

  private final ITraceContainerListener m_traceListener = new InternalTraceListener();

  // / @cond INTERNAL
  /**
   * Creates a new API module object.
   *
   * @param database Database the module belongs to.
   * @param module Wrapped internal module object.
   * @param nodeTagManager Node tag manager of the module.
   * @param viewTagManager View tag manager of the module.
   */
  // / @endcond
  public Module(final Database database, final INaviModule module, final TagManager nodeTagManager,
      final TagManager viewTagManager) {
    m_database = Preconditions.checkNotNull(database, "Error: Database argument can't be null");
    m_module = Preconditions.checkNotNull(module, "Error: Module argument can't be null");
    m_nodeTagManager = Preconditions.checkNotNull(nodeTagManager,
        "Error: Node tag manager argument can't be null");
    m_viewTagManager = Preconditions.checkNotNull(viewTagManager,
        "Error: View tag manager argument can't be null");

    if (m_module.getConfiguration().getDebugger() != null) {
      m_debugger = new Debugger(m_module.getConfiguration().getDebugger());
    }

    if (module.isLoaded()) {
      convertData();
    }

    module.addListener(m_listener);
  }
  
  //! Creates a new Trace, i.e., a list of events obtained by observing runtime behavior of a
  //  program. The trace is saved to the database.
  /**
   * Creates a new Trace and saves it to the database.
   * 
   * @param name The name of the trace.
   * @param description Each trace can have a description.
   * @return The trace that was created.
   * @throws CouldntSaveDataException Thrown if the trace couldn't be saved to the database.
   */
  public Trace createTrace(String name, String description) throws CouldntSaveDataException {
    try {
      return new Trace(m_module.getContent().getTraceContainer().createTrace(name, description));
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Converts internal module data to API module data.
   */
  private void convertData() {
    m_traces = new ArrayList<Trace>();

    for (final TraceList trace : m_module.getContent().getTraceContainer().getTraces()) {
      m_traces.add(new Trace(trace));
    }

    m_module.getContent().getTraceContainer().addListener(m_traceListener);

    m_functions = new ArrayList<Function>();

    for (final INaviFunction function :
        m_module.getContent().getFunctionContainer().getFunctions()) {
      m_functions.add(new Function(this, function));
    }

    for (final Function function : m_functions) {
      m_functionMap.put(function.getNative(), function);
    }

    m_views = new ArrayList<View>();

    for (final INaviView view : m_module.getContent().getViewContainer().getViews()) {
      m_views.add(new View(this, view, m_nodeTagManager, m_viewTagManager));
    }

    createCallgraph();
  }

  /**
   * Creates the native call graph.
   */
  private void createCallgraph() {
    final DirectedGraph<ICallgraphNode, ICallgraphEdge> graph =
        m_module.getContent().getNativeCallgraph();

    final List<FunctionBlock> blocks = new ArrayList<FunctionBlock>();
    final List<FunctionEdge> edges = new ArrayList<FunctionEdge>();

    final HashMap<ICallgraphNode, FunctionBlock> blockMap =
        new HashMap<ICallgraphNode, FunctionBlock>();

    final HashMap<INaviFunction, Function> functionMap = new HashMap<INaviFunction, Function>();

    for (final Function function : m_functions) {
      functionMap.put(function.getNative(), function);
    }

    for (final ICallgraphNode block : graph.getNodes()) {
      final FunctionBlock newBlock = new FunctionBlock(functionMap.get(block.getFunction()));

      blockMap.put(block, newBlock);

      blocks.add(newBlock);
    }

    for (final ICallgraphEdge edge : graph.getEdges()) {
      final FunctionBlock source = blockMap.get(edge.getSource());
      final FunctionBlock target = blockMap.get(edge.getTarget());

      edges.add(new FunctionEdge(source, target));
    }

    m_callgraph = new Callgraph(blocks, edges);
  }

  @Override
  public INaviModule getNative() {
    return m_module;
  }

  // ! Adds a module listener.
  /**
   * Adds an object that is notified about changes in the module.
   *
   * @param listener The listener object that is notified about changes in the module.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the module.
   */
  public void addListener(final IModuleListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Closes the module.
  /**
   * Closes the module. After a module is closed, using its content leads to undefined behaviour.
   */
  public void close() {
    m_module.close();
  }

  // ! Creates a new view.
  /**
   * Creates a new view that is added to the module.
   *
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The newly created view.
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are null.
   */
  @Override
  public View createView(final String name, final String description) {
    final CView newView = m_module.getContent().getViewContainer().createView(name, description);

    return ObjectFinders.getObject(newView, m_views);
  }

  // ! Deletes a view from the module.
  /**
   * Deletes a non-native view from the module and from the database.
   *
   * @param view The view to delete.
   *
   * @throws CouldntDeleteException Thrown if the view could not be deleted.
   */
  public void deleteView(final View view) throws CouldntDeleteException {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    if (view.getType() == ViewType.Native) {
      throw new IllegalArgumentException("Error: Native views can not be deleted");
    }

    if (!isLoaded()) {
      throw new IllegalArgumentException(
          "Error: Module must be opened before views can be deleted");
    }

    if (!m_views.contains(view)) {
      throw new IllegalArgumentException("Error: View does not belong to this module");
    }

    try {
      m_module.getContent().getViewContainer().deleteView(view.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  // / @cond INTERNAL
  /**
   * Frees allocated resources.
   */
  // / @endcond
  public void dispose() {
    m_module.removeListener(m_listener);
  }

  // ! Call graph of the module.
  /**
   * Returns the Call graph of the module. This graph contains all functions of the module as nodes
   * and the function calls as edges between the nodes.
   *
   * @return The Call graph of the module.
   *
   * @throws IllegalStateException Thrown if the module is not loaded.
   */
  public Callgraph getCallgraph() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The module is not loaded");
    }

    return m_callgraph;
  }

  // ! Creation date of the module.
  /**
   * Returns the creation date of the module. This is the date when the module was first written to
   * the database.
   *
   * @return The creation date of the module.
   */
  public Date getCreationDate() {
    return m_module.getConfiguration().getCreationDate();
  }

  // ! The database the module belongs to.
  /**
   * Returns the database the module belongs to.
   *
   * @return The database the module belongs to.
   */
  @Override
  public Database getDatabase() {
    return m_database;
  }

  // ! Debugger of the module.
  /**
   * Returns the debugger that is used to debug the module.
   *
   * @return The debugger that is used to debug the module.
   */
  public Debugger getDebugger() {
    return m_debugger;
  }

  // ! Debugger template of the module.
  /**
   * Returns the debugger template that defines the module debugger.
   *
   * @return The debugger template that defines the module debugger. This value can be null if no
   *         debugger template is set for the module.
   */
  public DebuggerTemplate getDebuggerTemplate() {
    return m_debuggerTemplate;
  }

  // ! Description of the module.
  /**
   * Returns the description of the module.
   *
   * @return The description of the module.
   */
  public String getDescription() {
    return m_module.getConfiguration().getDescription();
  }

  // ! File base of the module.
  /**
   * Returns the file base of the module. This is the base address of the module according to the
   * header of the original input file.
   *
   * @return The file base of the module.
   */
  public Address getFilebase() {
    return new Address(m_module.getConfiguration().getFileBase().toBigInteger());
  }

  @Override
  public Function getFunction(final INaviFunction function) {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The module is not loaded");
    }

    return m_functionMap.get(function);
  }

  // ! Returns the function of a view.
  /**
   * Returns the function associated with a view.
   *
   * @param view The view whose associated function is returned. Please note that this view must be
   *        a native view because only native views have associated functions.
   *
   * @return The associated function.
   */
  public Function getFunction(final View view) {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    if (!m_views.contains(view)) {
      throw new IllegalArgumentException("Error: View is not part of this module");
    }

    if (view.getType() != ViewType.Native) {
      throw new IllegalArgumentException("Error: View is not a native view");
    }

    return m_functionMap.get(m_module.getContent().getViewContainer().getFunction(view.getNative()));
  }

  // ! Functions inside the module.
  /**
   * Returns a list of all functions that can be found in the module.
   *
   * @return A list of functions.
   */
  @Override
  public List<Function> getFunctions() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The module is not loaded");
    }

    return new ArrayList<Function>(m_functions);
  }

  // ! Database ID of the module.
  /**
   * Returns the database ID of the module.
   *
   * @return The database ID of the module.
   */
  public int getId() {
    return m_module.getConfiguration().getId();
  }

  // ! Image base of the module.
  /**
   * Returns the image base of the module. This is the base address of the module as it really
   * occurs in memory after potential relocation operations.
   *
   * @return The image base of the module.
   */
  public Address getImagebase() {
    return new Address(m_module.getConfiguration().getImageBase().toBigInteger());
  }

  // ! MD5 hash of the original input file.
  /**
   * Returns the MD5 hash of the original input file.
   *
   * @return The MD5 hash of the original input file.
   */
  public String getMD5() {
    return m_module.getConfiguration().getMD5();
  }

  // ! Modification date of the module.
  /**
   * Returns the modification date of the module. This is the date when the module was last written
   * to the database.
   *
   * @return The modification date of the module.
   */
  public Date getModificationDate() {
    return m_module.getConfiguration().getModificationDate();
  }

  // ! Name of the module.
  /**
   * Returns the name of the module.
   *
   * @return The name of the module.
   */
  public String getName() {
    return m_module.getConfiguration().getName();
  }

  // ! SHA1 hash of the original input file.
  /**
   * Returns the SHA1 hash of the original input file.
   *
   * @return The SHA1 hash of the original input file.
   */
  public String getSHA1() {
    return m_module.getConfiguration().getSha1();
  }

  // ! Recorded module traces.
  /**
   * Returns all debug traces recorded for this module.
   *
   * @return A list of debug traces.
   */
  public List<Trace> getTraces() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The module is not loaded");
    }

    return new ArrayList<Trace>(m_traces);
  }

  // ! Views inside this module.
  /**
   * Returns a list of all views that can be found in the module.
   *
   * @return A list of views.
   */
  public List<View> getViews() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The module is not loaded");
    }

    return new ArrayList<View>(m_views);
  }

  // ! Initialize the module in the database.
  /**
   * Initialize the module in the database.
   *
   * @throws CouldntSaveDataException Thrown if an error occurred while saving the initialized data
   *         to the database.
   */
  public void initialize() throws CouldntSaveDataException {
    try {
      getNative().initialize();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  // ! Checks whether he module has been initialized.
  /**
   * Check whether this module has been initialized.
   *
   * @return True if the module has been initialized.
   */
  public boolean isInitialized() {
    return getNative().isInitialized();
  }

  // ! Checks if the module is loaded.
  /**
   * Returns a flag that indicates whether the module is loaded.
   *
   * @return True, if the module is loaded. False, otherwise.
   */
  public boolean isLoaded() {
    return m_module.isLoaded();
  }

  // ! Loads the module.
  /**
   * Loads the module data from the database.
   *
   * @throws IllegalStateException Thrown if the module is already loaded.
   * @throws CouldntLoadDataException Thrown if the module data could not be loaded from the
   *         database.
   */
  public void load() throws CouldntLoadDataException {
    if (isLoaded()) {
      return;
    }

    try {
      m_module.load();
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException | LoadCancelledException e) {
      throw new CouldntLoadDataException(e);
    } 
  }

  // ! Removes a module listener.
  /**
   * Removes a listener object from the module.
   *
   * @param listener The listener object to remove from the module.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the module.
   */
  public void removeListener(final IModuleListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the debugger template of the module.
  /**
   * Changes the debugger template of the module.
   *
   * @param template The new debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the new debugger template could not be saved to the
   *         database.
   */
  public void setDebuggerTemplate(final DebuggerTemplate template) throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setDebuggerTemplate(template == null ? null : template.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the module description.
  /**
   * Changes the description of the module.
   *
   * @param description The new description of the module.
   *
   * @throws IllegalArgumentException Thrown if the description argument is null.
   * @throws CouldntSaveDataException Thrown if the description of the module could not be changed.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the module file base.
  /**
   * Changes the file base of the module.
   *
   * @param address The new file base of the module.
   *
   * @throws IllegalArgumentException Thrown if the address argument is null.
   * @throws CouldntSaveDataException Thrown if the file base of the module could not be changed.
   */
  public void setFilebase(final Address address) throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setFileBase(new CAddress(address.toLong()));
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the module image base.
  /**
   * Changes the image base of the module.
   *
   * @param address The new image base of the module.
   *
   * @throws IllegalArgumentException Thrown if the address argument is null.
   * @throws CouldntSaveDataException Thrown if the image base of the module could not be changed.
   */
  public void setImagebase(final Address address) throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setImageBase(new CAddress(address.toLong()));
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the module name.
  /**
   * Changes the name of the module.
   *
   * @param name The new name of the module.
   *
   * @throws IllegalArgumentException Thrown if the name argument is null.
   * @throws CouldntSaveDataException Thrown if the name of the module could not be changed.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Printable representation of the module.
  /**
   * Returns the string representation of the module.
   *
   * @return The string representation of the module.
   */
  @Override
  public String toString() {
    return String.format("Module '%s'", getName());
  }

  /**
   * Keeps the API module object synchronized with the internal module object.
   */
  private class InternalModuleListener implements
      com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      final View newView = new View(Module.this, view, m_nodeTagManager, m_viewTagManager);

      m_views.add(newView);

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.addedView(Module.this, newView);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedData(final CModule module, final byte[] data) {
      // TODO (timkornau): forward this functionality to the API.
    }

    @Override
    public void changedDebugger(final INaviModule module, final IDebugger debugger) {
      m_debugger = debugger == null ? null : new Debugger(debugger);

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedDebugger(Module.this, m_debugger);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDebuggerTemplate(final INaviModule module,
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate template) {
      final DebuggerTemplate newTemplate = template == null ? null
          : ObjectFinders.getObject(template,
              m_database.getDebuggerTemplateManager().getDebuggerTemplates());

      m_debuggerTemplate = newTemplate;

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedDebuggerTemplate(Module.this, newTemplate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDescription(final INaviModule module, final String description) {
      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedDescription(Module.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedFileBase(final INaviModule module, final IAddress fileBase) {
      final Address newAddress = new Address(fileBase.toBigInteger());

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedFilebase(Module.this, newAddress);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedImageBase(final INaviModule module, final IAddress imageBase) {
      final Address newAddress = new Address(imageBase.toBigInteger());

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedImagebase(Module.this, newAddress);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedModificationDate(final INaviModule module, final Date date) {
      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedModificationDate(Module.this, date);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedName(Module.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedStarState(final INaviModule module, final boolean isStared) {
      // TODO (timkornau): forward this functionality to the API.
    }

    @Override
    public void closedModule(final CModule module, final ICallgraphView callgraphView,
        final List<IFlowgraphView> flowgraphs) {

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.closedModule(Module.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean closingModule(final CModule module) {
      for (final IModuleListener listener : m_listeners) {
        try {
          if (!listener.closingModule(Module.this)) {
            return false;
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      return true;
    }

    @Override
    public void deletedView(final INaviModule module, final INaviView view) {
      final View deletedView = ObjectFinders.getObject(view, m_views);

      m_views.remove(deletedView);

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.deletedView(Module.this, deletedView);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean initializing(final ModuleInitializeEvents event, final int counter) {
      return true;
    }

    @Override
    public void loadedModule(final INaviModule module) {
      convertData();

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.loadedModule(Module.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean loading(final ModuleLoadEvents event, final int counter) {
      return true;
    }

    @Override
    public void initializedModule(final INaviModule module) {
      // TODO (timkornau): forward this functionality to the API.
    }
  }

  private class InternalTraceListener implements ITraceContainerListener {
    @Override
    public void addedTrace(final CTraceContainer container, final TraceList trace) {
      final Trace newTrace = new Trace(trace);

      m_traces.add(newTrace);

      for (final IModuleListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedTrace(Module.this, newTrace);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedTrace(final CTraceContainer container, final TraceList trace) {
      final Trace deletedTrace = ObjectFinders.getObject(trace, m_traces);

      m_traces.remove(deletedTrace);

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.deletedTrace(Module.this, deletedTrace);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
