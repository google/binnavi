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
package com.google.security.zynamics.binnavi.disassembly.views;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes the configuration properties of a view.
 */
public final class CViewConfiguration implements IViewConfiguration {
  /**
   * View described by this configuration object.
   */
  private final INaviView view;

  /**
   * Listeners that are notified about changes in the view configuration.
   */
  private final ListenerProvider<INaviViewListener> listeners;

  /**
   * Synchronizes the view configuration with the database.
   */
  private final SQLProvider provider;

  /**
   * ID of the view. This ID equals the view as it is stored in the database.
   */
  private int id;

  /**
   * Name of the view.
   */
  private String viewName;

  /**
   * Description of the view.
   */
  private String viewDescription;

  /**
   * Creation date of the view.
   */
  private final Date creationDate;

  /**
   * Modification date of the view.
   */
  private Date modificationDate;

  /**
   * Says whether the view is a native view or not.
   */
  private final ViewType type;

  /**
   * Module the view belongs to. If the view belongs to a project, this field is null.
   */
  private INaviModule module;

  /**
   * Project the view belongs to. If the view belongs to a module, this field is null.
   */
  private INaviProject project;

  /**
   * The tags that were assigned to the view.
   */
  private final Set<CTag> viewTags;

  /**
   * Flag that says whether the view is starred or not.
   */
  private boolean isStarred = false;

  /**
   * Makes sure that changes in the tags are reflected in the set of assigned tags.
   */
  private final InternalViewTagListener tagListener = new InternalViewTagListener();

  /**
   * Creates a new view configuration object.
   *
   * @param view View described by this configuration object.
   * @param listeners Listeners that are notified about changes in the view configuration.
   * @param provider Synchronizes the view configuration with the database.
   * @param viewId ID of the view. This ID equals the view as it is stored in the database.
   * @param description Description of the view.
   * @param name Name of the view.
   * @param type Says whether the view is a native view or not.
   * @param creationDate Creation date of the view.
   * @param modificationDate Modification date of the view.
   * @param isStared Flag that says whether the view is stared or not.
   * @param tags The tags that were assigned to the view.
   */
  public CViewConfiguration(final INaviView view,
      final ListenerProvider<INaviViewListener> listeners, final SQLProvider provider,
      final int viewId, final String description, final String name, final ViewType type,
      final Date creationDate, final Date modificationDate, final Set<CTag> tags,
      final boolean isStared) {
    this.view = view;
    this.listeners = listeners;
    this.provider = provider;

    this.id = viewId;
    this.viewDescription = description;
    this.viewName = name;
    this.type = type;
    this.creationDate = new Date(creationDate.getTime());
    this.modificationDate = new Date(modificationDate.getTime());
    this.viewTags = new HashSet<CTag>(tags);
    this.isStarred = isStared;

    for (final CTag tag : tags) {
      tag.addListener(tagListener);
    }
  }

  @Override
  public Date getCreationDate() {
    return new Date(creationDate.getTime());
  }

  @Override
  public String getDescription() {
    return viewDescription;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public Date getModificationDate() {
    return new Date(modificationDate.getTime());
  }

  @Override
  public INaviModule getModule() {
    return module;
  }

  @Override
  public String getName() {
    return viewName;
  }

  @Override
  public INaviProject getProject() {
    return project;
  }

  @Override
  public ViewType getType() {
    return type;
  }

  @Override
  public Set<CTag> getViewTags() {
    return new HashSet<CTag>(viewTags);
  }

  @Override
  public boolean isStared() {
    return isStarred;
  }

  /**
   * Determines whether the view was previously stored to the database.
   *
   * @return True, if the view was previously stored. False, otherwise.
   */
  public boolean isStored() {
    return Integer.signum(id) != -1;
  }

  @Override
  public boolean isTagged() {
    return viewTags.size() != 0;
  }

  @Override
  public boolean isTagged(final CTag tag) {
    for (final CTag t : viewTags) {
      // m_tags.contains(tag); Can NOT be used here!
      if (t.getId() == tag.getId()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00316: Description string can not be null");

    if (description.equals(viewDescription)) {
      return;
    }

    if (isStored()) {
      provider.setDescription(view, description);
    }

    viewDescription = description;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedDescription(view, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  @Override
  public void setId(final int viewId) {
    id = viewId;
  }

  public void setModule(final INaviModule module) {
    this.module = module;
  }

  @Override
  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00317: Name string can not be null");
    Preconditions.checkArgument(!name.equals(""), "IE00318: Names can not be empty");

    if (name.equals(viewName)) {
      return;
    }

    if (isStored()) {
      provider.setName(view, name);
    }

    viewName = name;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedName(view, name);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  public void setProject(final INaviProject project) {
    this.project = project;
  }

  @Override
  public void setStared(final boolean isStared) throws CouldntSaveDataException {
    if (isStarred == isStared) {
      return;
    }

    provider.setStared(view, isStared);

    isStarred = isStared;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedStarState(view, isStared);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void tagView(final CTag tag) throws CouldntSaveDataException {
    Preconditions.checkNotNull(tag, "IE00319: Tag argument can not be null");
    Preconditions.checkArgument(tag.inSameDatabase(provider),
        "IE00320: Tag and view are not stored in the same database");
    Preconditions
        .checkArgument(tag.getType() == TagType.VIEW_TAG, "IE00321: Tag is not a view tag");

    if (!viewTags.contains(tag)) {
      if (isStored()) {
        provider.tagView(view, tag);
      }

      tag.addListener(tagListener);

      viewTags.add(tag);

      for (final INaviViewListener listener : listeners) {
        try {
          listener.taggedView(view, tag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  @Override
  public void untagView(final CTag tag) throws CouldntSaveDataException {
    Preconditions.checkNotNull(tag, "IE00322: Tag argument can not be null");
    Preconditions.checkArgument(viewTags.contains(tag),
        "IE00323: View is not tagged by the given tag");

    if (isStored()) {
      provider.removeTag(view, tag);
    }

    tag.removeListener(tagListener);

    viewTags.remove(tag);

    for (final INaviViewListener listener : listeners) {
      try {
        listener.untaggedView(view, tag);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void updateModificationDate() {
    try {
      modificationDate = isStored() ? provider.getModificationDate(view) : new Date();

      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedModificationDate(view, modificationDate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);
    }
  }

  /**
   * Makes sure that changes in the tags are reflected in the set of assigned tags.
   */
  private class InternalViewTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      // Do nothing
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      // Do nothing
    }

    @Override
    public void deletedTag(final CTag tag) {
      tag.removeListener(tagListener);

      viewTags.remove(tag);

      for (final INaviViewListener listener : listeners) {
        try {
          listener.untaggedView(view, tag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  @Override
  public void setNameInternal(final String name) {
    Preconditions.checkNotNull(name, "IE02803: name argument can not be null");
    if (viewName.equals(name)) {
      return;
    }

    viewName = name;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedName(view, name);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setDescriptionInternal(final String description) {
    Preconditions.checkNotNull(description, "IE02804: description argument can not be null");
    if (viewDescription != null && viewDescription.equals(description)) {
      return;
    }

    viewDescription = description;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedDescription(view, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setStaredInternal(final boolean isStared) {
    if (isStarred == isStared) {
      return;
    }

    isStarred = isStared;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedStarState(view, isStared);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setModificationDateInternal(final Date modificationDate) {
    this.modificationDate =
        Preconditions.checkNotNull(modificationDate,
            "Error: modificationDate argument can not be null");

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedModificationDate(view, this.modificationDate);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

  }

  // The following methods are declared in the interface, and can't be easily removed there.
  // Therefore, these dummy implementations are required.
  @Override
  public int getUnloadedEdgeCount() {
    return 0;
  }

  @Override
  public GraphType getUnloadedGraphType() {
    return null;
  }

  @Override
  public int getUnloadedNodeCount() {
    return 0;
  }
}
