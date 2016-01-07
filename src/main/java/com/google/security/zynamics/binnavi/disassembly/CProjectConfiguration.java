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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Contains configuration data for projects.
 */
public final class CProjectConfiguration {
  /**
   * Project configured by this object.
   */
  private final INaviProject m_project;

  /**
   * Listeners that are notified about changes in the project.
   */
  private final ListenerProvider<IProjectListener> m_listeners;

  /**
   * Synchronizes configuration settings with the database.
   */
  private final SQLProvider m_provider;

  /**
   * The ID of the project as you can find it in the projects table.
   */
  private final int m_id;

  /**
   * The name of the project. This is a short name that the user can set that makes it easier for
   * him to identify a project.
   */
  private String m_name;

  /**
   * The description of the project. The user can set a longer description of the project here which
   * contains more detailed information about the project.
   */
  private String m_description;

  /**
   * The creation date of the project. The project was created on that date.
   */
  private final Date m_creationDate;

  /**
   * The modification date of the project. This is the date when the project was saved the last
   * time.
   */
  private Date m_modificationDate;

  /**
   * List of debugger templates that are registered with the project.
   */
  private final List<DebuggerTemplate> m_assignedDebuggers;

  /**
   * Creates a new configuration object.
   * 
   * @param project Project configured by this object.
   * @param listeners Listeners that are notified about changes in the project.
   * @param provider Synchronizes configuration settings with the database.
   * @param projectId ID of the project.
   * @param name Name of the project.
   * @param description Description of the project.
   * @param creationDate Creation date of the project.
   * @param modificationDate Modification date of the project.
   * @param assignedDebuggers List of debugger templates that are registered with the project.
   */
  public CProjectConfiguration(final INaviProject project,
      final ListenerProvider<IProjectListener> listeners, final SQLProvider provider,
      final int projectId, final String name, final String description, final Date creationDate,
      final Date modificationDate, final List<DebuggerTemplate> assignedDebuggers) {
    m_project = project;
    m_listeners = listeners;
    m_provider = provider;

    m_id = projectId;
    m_name = name;
    m_description = description;
    m_creationDate = new Date(creationDate.getTime());
    m_modificationDate = new Date(modificationDate.getTime());
    m_assignedDebuggers = new ArrayList<DebuggerTemplate>(assignedDebuggers);
  }

  /**
   * Assigns a debugger to the project.
   * 
   * @param debugger The debugger that is added to the project.
   * 
   * @throws CouldntSaveDataException Thrown if the debugger could not be added.
   */
  public void addDebugger(final DebuggerTemplate debugger) throws CouldntSaveDataException {
    Preconditions.checkNotNull(debugger, "IE00237: Debugger argument can't be null");
    Preconditions.checkArgument(debugger.inSameDatabase(m_provider),
        "IE00238: Debugger template and project are not in the same database");

    m_provider.addDebugger(m_project, debugger);

    m_assignedDebuggers.add(debugger);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.addedDebugger(m_project, debugger);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Returns the creation date of the project.
   * 
   * @return The creation date of the project.
   */
  public Date getCreationDate() {
    return new Date(m_creationDate.getTime());
  }

  /**
   * Returns the debuggers assigned to the project.
   * 
   * @return The debuggers assigned to the project.
   */
  public List<DebuggerTemplate> getDebuggers() {
    return new ArrayList<DebuggerTemplate>(m_assignedDebuggers);
  }

  /**
   * Returns the description of the project.
   * 
   * @return The description of the project.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Returns the ID of the project. This ID is ID of the project as it can be found in the projects
   * table in the database.
   * 
   * @return The ID of the project.
   */
  public int getId() {
    return m_id;
  }

  /**
   * Returns the modification date of the project.
   * 
   * @return The modification date of the project.
   */
  public Date getModificationDate() {
    return new Date(m_modificationDate.getTime());
  }

  /**
   * Returns the name of the project.
   * 
   * @return The name of the project.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Determines whether the project uses a given debugger.
   * 
   * @param debugger The debugger to check for.
   * 
   * @return True, if the project uses the debugger. False, otherwise.
   */
  public boolean hasDebugger(final DebuggerTemplate debugger) {
    return m_assignedDebuggers.contains(debugger);
  }

  /**
   * Removes an assigned debugger template from the project.
   * 
   * @param debugger The debugger template to remove.
   * 
   * @throws CouldntSaveDataException Thrown if the change could not be saved to the database.
   */
  public void removeDebugger(final DebuggerTemplate debugger) throws CouldntSaveDataException {
    Preconditions.checkNotNull(debugger, "IE00254: Debugger argument can't be null");
    Preconditions.checkArgument(m_assignedDebuggers.contains(debugger),
        "IE00255: Debugger template was not assigned to the project");
    m_provider.removeDebugger(m_project, debugger);

    m_assignedDebuggers.remove(debugger);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.removedDebugger(m_project, debugger);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Changes the description of the project. The new description is immediately saved to the
   * database.
   * 
   * This function is guaranteed to be exception-safe. If something goes wrong during the saving
   * process, the state of the project remains unchanged.
   * 
   * @param description The new description of the project.
   * 
   * @throws CouldntSaveDataException Thrown if the new project description could not be saved to
   *         the database.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00256: Project description can't be null");

    // We don't have to do anything if the old description equals the new description.
    if (m_description.equals(description)) {
      return;
    }

    m_provider.setDescription(m_project, description);

    m_description = description;

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.changedDescription(m_project, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Changes the name of the database. The new project name is immediately saved to the database.
   * 
   * This function is guaranteed to be exception-safe. If something goes wrong during the saving
   * process, the state of the project remains unchanged.
   * 
   * @param name The new name of the project.
   * 
   * @throws CouldntSaveDataException Thrown if the name could not changed.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00257: Project name can't be null");

    // We don't have to do anything if the new name equals the old name.
    if (m_name.equals(name)) {
      return;
    }

    m_provider.setName(m_project, name);

    m_name = name;

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.changedName(m_project, name);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Updates the modification date of the project.
   */
  public void updateModificationDate() {
    try {
      m_modificationDate = m_provider.getModificationDate(m_project);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.changedModificationDate(m_project, m_modificationDate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);
    }
  }
}
