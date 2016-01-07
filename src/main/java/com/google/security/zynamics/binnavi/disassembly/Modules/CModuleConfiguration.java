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

import java.util.Date;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.TcpDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModuleConfiguration;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Contains the configurable values of a module.
 */
public final class CModuleConfiguration implements INaviModuleConfiguration {
  /**
   * The module to configure.
   */
  private final INaviModule m_module;

  /**
   * Synchronizes the configurable data with the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listeners that are notified about changes in the properties.
   */
  private final ListenerProvider<IModuleListener> m_listeners;

  /**
   * The ID of the module as it can be found in the database.
   */
  private final int m_id;

  /**
   * The name of the module.
   */
  private String m_name;

  /**
   * The description of the module.
   */
  private String m_description;

  /**
   * The date when the module was created.
   */
  private final Date m_creationDate;

  /**
   * The date when the module was last modified.
   */
  private Date m_modificationDate;

  /**
   * The MD5 hash of the original input file.
   */
  private final String m_md5;

  /**
   * The SHA1 hash of the original input file.
   */
  private final String m_sha1;

  /**
   * The file base of the module.
   */
  private IAddress m_fileBase;

  /**
   * The image base of the module.
   */
  private IAddress m_imageBase;

  /**
   * Flag that says whether the module is stared or not.
   */
  private boolean m_isStared;

  /**
   * The debugger template that was used to create the debugger object that is used to debug this
   * module.
   */
  private DebuggerTemplate m_debuggerTemplate = null;

  /**
   * The debugger object that can be used to debug this module.
   */
  private TcpDebugger m_debugger = null;

  /**
   * Raw module which backs the module. This value can be null.
   */
  private final INaviRawModule m_rawModule;

  /**
   * Creates a new configuration object.
   * 
   * @param module The module to configure.
   * @param provider The SQL provider that is used to load more information about the module.
   * @param listeners Listeners that are notified about changes in the properties.
   * @param moduleId The ID of the module.
   * @param name The name of the module.
   * @param comment The module description.
   * @param creationDate The creation date of the module.
   * @param modificationDate The modification date of the module.
   * @param md5 The MD5 hash of the module input file.
   * @param sha1 The SHA1 hash of the module input file.
   * @param fileBase The file base of the module.
   * @param imageBase The image base of the module.
   * @param debuggerTemplate The comment associated with the module.
   * @param isStared Flag that says whether the module is stared or not.
   * @param rawModule Raw module which backs the module. This value can be null.
   */
  public CModuleConfiguration(final INaviModule module, final SQLProvider provider,
      final ListenerProvider<IModuleListener> listeners, final int moduleId, final String name,
      final String comment, final Date creationDate, final Date modificationDate, final String md5,
      final String sha1, final IAddress fileBase, final IAddress imageBase,
      final DebuggerTemplate debuggerTemplate, final boolean isStared,
      final INaviRawModule rawModule) {
    m_module = module;
    m_provider = provider;
    m_listeners = listeners;

    m_id = moduleId;
    m_name = name;
    m_description = comment;
    m_creationDate = new Date(creationDate.getTime());
    m_modificationDate = new Date(modificationDate.getTime());
    m_md5 = md5;
    m_sha1 = sha1;
    m_fileBase = fileBase;
    m_imageBase = imageBase;
    m_debuggerTemplate = debuggerTemplate;
    m_isStared = isStared;
    m_rawModule = rawModule;

    updateDebugger(debuggerTemplate);
  }

  /**
   * Takes a debugger template and updates the debugger object of the module accordingly if
   * possible.
   * 
   * @param template The debugger template that provides the debugger information.
   */
  private void updateDebugger(final DebuggerTemplate template) {
    // The requirements that enable the construction of a new debugger object are
    // as follows:
    //
    // 1. There must be a debugger template. Having a null-template is possible too.
    // 2. No debugger exists yet or the existing debugger is not active. We can
    // not replace a debugger that's currently in use.

    if ((m_debugger == null) || !m_debugger.isConnected()) {
      if (template == null) {
        m_debugger = null;
      } else {
        m_debugger = new TcpDebugger(template, new ModuleTargetSettings(m_module));

        m_debugger.setAddressTranslator(m_module, m_fileBase, m_imageBase);
      }

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedDebugger(m_module, m_debugger);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  @Override
  public Date getCreationDate() {
    return new Date(m_creationDate.getTime());
  }

  @Override
  public IDebugger getDebugger() {
    return m_debugger;
  }

  @Override
  public DebuggerTemplate getDebuggerTemplate() {
    return m_debuggerTemplate;
  }

  @Override
  public String getDescription() {
    return m_description;
  }

  @Override
  public IAddress getFileBase() {
    return m_fileBase;
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public IAddress getImageBase() {
    return m_imageBase;
  }

  @Override
  public String getMD5() {
    return m_md5;
  }

  @Override
  public Date getModificationDate() {
    return new Date(m_modificationDate.getTime());
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public INaviRawModule getRawModule() {
    return m_rawModule;
  }

  @Override
  public String getSha1() {
    return m_sha1;
  }

  @Override
  public boolean isStared() {
    return m_isStared;
  }

  @Override
  public void setDebuggerTemplate(final DebuggerTemplate template) throws CouldntSaveDataException {
    if (template == m_debuggerTemplate) {
      return;
    }

    m_provider.assignDebugger(m_module, template);

    m_debuggerTemplate = template;

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedDebuggerTemplate(m_module, template);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateDebugger(template);

    updateModificationDate();
  }

  @Override
  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00199: Description string can not be null");

    // Do nothing if the old description equals the new description.
    if (description.equals(m_description)) {
      return;
    }

    m_provider.setDescription(m_module, description);

    m_description = description;

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedDescription(m_module, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  @Override
  public void setFileBase(final IAddress fileBase) throws CouldntSaveDataException {
    Preconditions.checkNotNull(fileBase, "IE00200: File base argument can not be null");

    if (fileBase.equals(m_fileBase)) {
      return;
    }

    m_provider.setFileBase(m_module, fileBase);

    m_fileBase = fileBase;

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedFileBase(m_module, fileBase);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  @Override
  public void setImageBase(final IAddress imageBase) throws CouldntSaveDataException {
    Preconditions.checkNotNull(imageBase, "IE00201: Image base argument can not be null");

    if (imageBase.equals(m_imageBase)) {
      return;
    }

    m_provider.setImageBase(m_module, imageBase);

    m_imageBase = imageBase;

    if (m_debugger != null) {
      m_debugger.setAddressTranslator(m_module, m_fileBase, m_imageBase);
    }

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedImageBase(m_module, imageBase);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  @Override
  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00202: Name string can not be null");

    // Do nothing if the old name equals the new name.
    if (name.equals(m_name)) {
      return;
    }

    m_provider.setName(m_module, name);

    m_name = name;

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedName(m_module, name);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  @Override
  public void setStared(final boolean isStared) throws CouldntSaveDataException {
    if (m_isStared == isStared) {
      return;
    }

    m_provider.setStared(m_module, isStared);

    m_isStared = isStared;

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.changedStarState(m_module, isStared);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void updateModificationDate() {
    try {
      m_modificationDate = m_provider.getModificationDate(m_module);

      for (final IModuleListener listener : m_listeners) {
        try {
          listener.changedModificationDate(m_module, m_modificationDate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);
    }
  }
}
