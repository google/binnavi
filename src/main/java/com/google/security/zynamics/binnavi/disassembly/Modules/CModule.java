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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.COperandTypeConverter;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainer;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainerBackend;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainerBackend;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManagerDatabaseBackend;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CModule class represents a module that is stored in the database. Simple information provided
 * by the CModule object comes from the modules database table. More complex information comes from
 * the individual module tables.
 */
public final class CModule implements INaviModule {
  /**
   * The SQL provider that is used to communicate with the database where the module represented by
   * this class is stored.
   */
  private final SQLProvider m_provider;

  /**
   * List of listeners that are notified about changes in the module.
   */
  private volatile ListenerProvider<IModuleListener> m_listeners =
      new ListenerProvider<IModuleListener>();

  /**
   * Reports module initialization events to listeners.
   */
  private final CModuleInitializeReporter m_reporter = new CModuleInitializeReporter(m_listeners);

  /**
   * Flag that indicates whether the module is currently being loaded from the database.
   */
  private boolean m_isLoading = false;

  /**
   * Reports module loading events to listeners.
   */
  private final CModuleLoaderReporter m_loadReporter = new CModuleLoaderReporter(m_listeners);

  /**
   * Number of native functions in the module. This value is only used until the module is loaded.
   * Afterwards this number is determined using the loaded functions list.
   */
  private final int m_functionCount;

  /**
   * The number of custom views in the module. This number is only used until the module is loaded.
   * Afterwards the loaded custom views list is used to determine this number.
   */
  private final int m_customViewCount;

  /**
   * Binary data of this module.
   */
  private byte[] m_data = new byte[0];

  /**
   * Initialization state of the module (between 0 and MAXINT).
   */
  private int m_initializationState;

  /**
   * Contains the configuration properties of the module.
   */
  private final CModuleConfiguration m_configuration;

  /**
   * Contains the content of loaded modules.
   */
  private CModuleContent m_content;

  /**
   * Flag that indicates whether the module is being initialized.
   */
  private Boolean m_isInitializing = false;

  private final Map<String, String> m_settings = new HashMap<String, String>();

  /**
   * Note: the type manager can not be moved to the module content since it is required before all
   * the content has been gathered (e.g. when loading functions a corresponding stack frame might be
   * needed).
   */
  private TypeManager typeManager;

  /**
   * Creates a new CModule object.
   *
   * @param moduleId The ID of the module.
   * @param name The name of the module.
   * @param comment The module description.
   * @param creationDate The creation date of the module.
   * @param modificationDate The modification date of the module.
   * @param md5 The MD5 hash of the module input file.
   * @param sha1 The SHA1 hash of the module input file.
   * @param functionCount The number of functions in the module.
   * @param customViewCount The number of custom views in the module.
   * @param fileBase The file base of the module.
   * @param imageBase The image base of the module.
   * @param debuggerTemplate The comment associated with the module.
   * @param rawModule Raw module which backs the module. This value can be null.
   * @param initializationState Initialization state of the module (between 0 and MAXINT).
   * @param isStared The star state of the module.
   * @param provider The SQL provider that is used to load more information about the module.
   */
  public CModule(final int moduleId, final String name, final String comment,
      final Date creationDate, final Date modificationDate, final String md5, final String sha1,
      final int functionCount, final int customViewCount, final IAddress fileBase,
      final IAddress imageBase, final DebuggerTemplate debuggerTemplate,
      final INaviRawModule rawModule, final int initializationState, final boolean isStared,
      final SQLProvider provider) {
    Preconditions.checkArgument(moduleId > 0, "IE00135: Module ID must be larger than 0");
    Preconditions.checkNotNull(name, "IE00136: Module Name can not be null");
    Preconditions.checkNotNull(comment, "IE00137: Module Comment can not be null");
    Preconditions.checkNotNull(creationDate, "IE00139: Module Creation Date can not be null");
    Preconditions.checkNotNull(
        modificationDate, "IE00140: Module Modification Date can not be null");
    Preconditions.checkNotNull(md5, "IE00141: Module MD5 hash can not be null");
    Preconditions.checkArgument(Convert.isMD5String(md5), "IE00142: Invalid MD5 string");
    Preconditions.checkNotNull(sha1, "IE00143: Module SHA1 hash can not be null");
    Preconditions.checkArgument(Convert.isSha1String(sha1), "IE00144: Invalid SHA1 string");
    Preconditions.checkArgument(functionCount >= 0, "IE00145: Function count can not be negative");
    Preconditions.checkArgument(
        customViewCount >= 0, "IE00146: Custom View Count argument can not be null");
    Preconditions.checkNotNull(fileBase, "IE00147: File Base argument can not be null");
    Preconditions.checkNotNull(imageBase, "IE00148: Image Base argument can not be null");
    Preconditions.checkArgument(
        !((debuggerTemplate != null) && !debuggerTemplate.inSameDatabase(provider)),
        "IE00149: Module is not in same database as debugger template argument");
    Preconditions.checkNotNull(provider, "IE00150: Invalid SQL provider");

    m_configuration = new CModuleConfiguration(
        this, provider, m_listeners, moduleId, name, comment, creationDate, modificationDate, md5,
        sha1, fileBase, imageBase, debuggerTemplate, isStared, rawModule);

    m_functionCount = functionCount;
    m_customViewCount = customViewCount;
    m_initializationState = initializationState;
    m_provider = provider;
  }

  @Override
  public synchronized void addListener(final IModuleListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Closes the module.
   *
   * @return True, if the module was closed. False, if the module was vetoed.
   */
  @Override
  public boolean close() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00156: Module is not loaded");
    }

    for (final IModuleListener listener : m_listeners) {
      try {
        if (!listener.closingModule(this)) {
          return false;
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    final ICallgraphView oldNativeCallgraphView =
        m_content.getViewContainer().getNativeCallgraphView();
    final List<IFlowgraphView> oldFlowgraphs =
        m_content.getViewContainer().getNativeFlowgraphViews();

    if (!m_content.close()) {
      return false;
    }

    m_content = null;

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.closedModule(this, oldNativeCallgraphView, oldFlowgraphs);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return true;
  }

  @Override
  public INaviInstruction createInstruction(final IAddress address, final String mnemonic,
      final List<COperandTree> operands, final byte[] data, final String architecture) {
    return new CInstruction(
        false, this, address, mnemonic, operands, data, architecture, m_provider);
  }

  @Override
  public COperandTree createOperand(final COperandTreeNode node) {
    return new COperandTree(
        node, m_provider, getTypeManager(), m_content.getTypeInstanceContainer());
  }

  @Override
  public COperandTreeNode createOperandExpression(final String value, final ExpressionType type) {
    return new COperandTreeNode(-1, COperandTypeConverter.convert(type), value, null,
        new ArrayList<IReference>(), m_provider, getTypeManager(),
        m_content.getTypeInstanceContainer());
  }

  @Override
  public CModuleConfiguration getConfiguration() {
    return m_configuration;
  }

  @Override
  public CModuleContent getContent() {
    return Preconditions.checkNotNull(m_content, "IE00462: Module is not loaded");
  }

  @Override
  public int getCustomViewCount() {
    return isLoaded() ? m_content.getViewContainer().getCustomViewCount() : m_customViewCount;
  }

  @Override
  public byte[] getData() {
    return m_data.clone();
  }

  @Override
  public int getFunctionCount() {
    return isLoaded() ? m_content.getFunctionContainer().getFunctionCount() : m_functionCount;
  }

  @Override
  public TypeManager getTypeManager() {
    return typeManager;
  }

  /**
   * Returns a list that contains all non-native views that can be found in this module.
   *
   * @return A list of non-native views.
   */
  public List<INaviView> getUserViews() {
    return m_content.getViewContainer().getUserViews();
  }

  @Override
  public int getViewCount() {
    return 1 + getFunctionCount() + getCustomViewCount();
  }

  @Override
  public List<INaviView> getViewsWithAddresses(
      final List<UnrelocatedAddress> address, final boolean all)
      throws CouldntLoadDataException {
    return m_provider.getViewsWithAddresses(this, address, all);
  }

  @Override
  public void initialize() throws CouldntSaveDataException {
    synchronized (m_isInitializing) {
      if (isInitialized()) {
        return;
      }

      m_isInitializing = true;

      try {
        m_provider.initializeModule(this, m_reporter);
      } finally {
        m_isInitializing = false;
      }
    }

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.initializedModule(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00193: Object argument can not be null");

    return object.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE00194: Provider argument can not be null");

    return provider.equals(m_provider);
  }

  @Override
  public boolean isInitialized() {
    return m_initializationState == Integer.MAX_VALUE;
  }

  @Override
  public boolean isInitializing() {
    return m_isInitializing;
  }

  @Override
  public boolean isLoaded() {
    return m_content != null;
  }

  @Override
  public boolean isLoading() {
    return m_isLoading;
  }

  @Override
  public boolean isStared() {
    return m_configuration.isStared();
  }

  @Override
  public void load() throws CouldntLoadDataException, LoadCancelledException {
    synchronized (m_loadReporter) {
      if (isLoaded()) {
        return;
      }

      if (!isInitialized()) {
        throw new IllegalStateException("IE02617: The module is not initialized yet");
      }

      m_isLoading = true;

      // TODO: Move loading into the content constructor

      try {
        if (!m_loadReporter.report(ModuleLoadEvents.Starting)) {
          throw new LoadCancelledException();
        }

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingCallgraphView)) {
          throw new LoadCancelledException();
        }
        final ICallgraphView nativeCallgraph = m_provider.loadNativeCallgraph(this);

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingFlowgraphs)) {
          throw new LoadCancelledException();
        }

        // When loading views, an empty flow graph (imported function) can not
        // be distinguished
        // from an empty call graph. This leads to problems in modules without
        // functions. In
        // these cases, the empty call graph is loaded as an imported function
        // view. This is
        // obviously wrong, so we are correcting this here.
        final ImmutableList<IFlowgraphView> nativeFlowgraphs = nativeCallgraph.getNodeCount() == 0
            ? new ImmutableList.Builder<IFlowgraphView>().build()
            : m_provider.loadNativeFlowgraphs(this);
        if (!m_loadReporter.report(ModuleLoadEvents.LoadingCallgraphViews)) {
          throw new LoadCancelledException();
        }
        final List<ICallgraphView> userCallgraphs = m_provider.loadCallgraphViews(this);

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingFlowgraphViews)) {
          throw new LoadCancelledException();
        }
        final ImmutableList<IFlowgraphView> userFlowgraphs = m_provider.loadFlowgraphs(this);

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingMixedViews)) {
          throw new LoadCancelledException();
        }
        final List<INaviView> userMixedGraphs = m_provider.loadMixedgraphs(this);

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingCallgraph)) {
          throw new LoadCancelledException();
        }

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingFunctions)) {
          throw new LoadCancelledException();
        }

        // Note: the type manager needs to be loaded prior to functions, since a function might
        // have an associated stack frame, which in turn needs the type system.
        typeManager = new TypeManager(new TypeManagerDatabaseBackend(m_provider, this));

        final List<INaviFunction> functions = m_provider.loadFunctions(this, nativeFlowgraphs);
        final ImmutableBiMap<INaviView, INaviFunction> viewFunctionMap =
            m_provider.loadViewFunctionMapping(nativeFlowgraphs, functions, this);
        final CCallgraph callgraph =
            m_provider.loadCallgraph(this, nativeCallgraph.getConfiguration().getId(), functions);

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingTraces)) {
          throw new LoadCancelledException();
        }
        final List<TraceList> traces = m_provider.loadTraces(this);

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingGlobalVariables)) {
          throw new LoadCancelledException();
        }

        final List<INaviView> customViews = new ArrayList<INaviView>();
        customViews.addAll(userCallgraphs);
        customViews.addAll(userFlowgraphs);
        customViews.addAll(userMixedGraphs);

        final List<INaviView> currentViews = new ArrayList<INaviView>(nativeFlowgraphs);
        currentViews.addAll(customViews);

        Collections.sort(currentViews, new Comparator<INaviView>() {
          @Override
          public int compare(final INaviView lhs, final INaviView rhs) {
            return lhs.getConfiguration().getId() - rhs.getConfiguration().getId();
          }
        });

        // Map viewId to corresponding index in currentViews.
        final Map<Integer, Integer> viewIdToIndex = new HashMap<Integer, Integer>();
        for (int i = 0; i < currentViews.size(); ++i) {
          viewIdToIndex.put(currentViews.get(i).getConfiguration().getId(), i);
        }

        if (!m_loadReporter.report(ModuleLoadEvents.LoadingTypes)) {
          throw new LoadCancelledException();
        }

        final SectionContainer sections =
            new SectionContainer(new SectionContainerBackend(m_provider, this));

        final TypeInstanceContainer typeInstances = new TypeInstanceContainer(
            new TypeInstanceContainerBackend(m_provider, this, typeManager, sections), m_provider);
        m_content = new CModuleContent(
            this, m_provider, m_listeners, callgraph, functions, nativeCallgraph, nativeFlowgraphs,
            customViews, viewFunctionMap, traces, sections, typeInstances);
        typeInstances.initialize();

      } catch (final CouldntLoadDataException e) {
        m_isLoading = false;
        throw e;
      } catch (final LoadCancelledException e) {
        m_isLoading = false;
        throw e;
      } finally {
        m_loadReporter.report(ModuleLoadEvents.Finished);
      }

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.loadedModule(this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      m_isLoading = false;
    }
  }

  @Override
  public void loadData() throws CouldntLoadDataException {
    setData(m_provider.loadData(this));
  }

  @Override
  public String readSetting(final String key) throws CouldntLoadDataException {
    if (m_settings.containsKey(key)) {
      return m_settings.get(key);
    } else {
      return m_provider.readSetting(this, key);
    }
  }

  @Override
  public void removeListener(final IModuleListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void saveData() throws CouldntSaveDataException {
    m_provider.saveData(this, m_data);
  }

  @Override
  public void setData(final byte[] data) {
    Preconditions.checkNotNull(data, "IE00198: Data argument can not be null");

    m_data = data.clone();

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedData(this, data);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setInitialized() {
    m_initializationState = Integer.MAX_VALUE;
  }

  @Override
  public String toString() {
    return String.format(
        "BinNavi Module %d: %s", getConfiguration().getId(), getConfiguration().getName());
  }

  @Override
  public void writeSetting(final String key, final String value) throws CouldntSaveDataException {
    m_settings.put(key, value);
    m_provider.writeSetting(this, key, value);
  }
}
