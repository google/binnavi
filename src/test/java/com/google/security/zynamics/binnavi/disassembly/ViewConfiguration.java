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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import java.util.Date;
import java.util.Set;

public class ViewConfiguration implements IViewConfiguration {

  private final SQLProvider provider;
  private final INaviView view;
  private final ViewType type;
  private final int viewId;
  private final String name;
  private final String description;
  private final Date modificationDate;
  private final Date creationDate;
  private final INaviModule module;
  private final Set<CTag> viewTags;
  private final boolean staredState;
  private final INaviProject project;
  private final int edgeCount;
  private final int nodeCount;
  private final GraphType graphType;

  private ViewConfiguration(final Builder builder) {
    provider = builder.provider;
    view = builder.view;
    type = builder.type;
    viewId = builder.viewId; // TODO: critical point we need to distinguish between different
                             // unsaved views!!!
    name = builder.name;
    description = builder.description;
    modificationDate = builder.modificationDate;
    creationDate = builder.creationDate;
    module = builder.module;
    project = builder.project;
    viewTags = builder.viewTags;
    staredState = builder.staredState;
    edgeCount = builder.edgeCount;
    nodeCount = builder.nodeCount;
    graphType = builder.graphType;
  }

  public static class Builder {

    private final SQLProvider provider;
    private final INaviView view;
    private final ViewType type;
    private int viewId;
    private String name;
    private String description;
    private Date modificationDate;
    private Date creationDate;
    private INaviModule module;
    private Set<CTag> viewTags;
    private boolean staredState;
    private INaviProject project;
    private int edgeCount;
    private int nodeCount;
    private GraphType graphType;

    public Builder(final SQLProvider provider, final INaviView view, final ViewType type) {
      this.provider =
          Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
      this.view = Preconditions.checkNotNull(view, "Error: view argument can not be null");
      this.type = Preconditions.checkNotNull(type, "Error: type argument can not be null");
    }

    public Builder(final ViewConfiguration configuration, final int viewId) {
      provider = configuration.provider;
      view = configuration.view;
      type = configuration.type;
      this.viewId = viewId;
      name = configuration.name;
      description = configuration.description;
      modificationDate = configuration.modificationDate;
      creationDate = configuration.creationDate;
      module = configuration.module;
      project = configuration.project;
      viewTags = configuration.viewTags;
      staredState = configuration.staredState;
      edgeCount = configuration.edgeCount;
      nodeCount = configuration.nodeCount;
      graphType = configuration.graphType;
    }

    public Builder setId(final int viewId) {
      this.viewId = viewId;
      return this;
    }

    public Builder setName(final String name) {
      this.name = Preconditions.checkNotNull(name, "Error: name argument can not be null");
      return this;
    }

    public ViewConfiguration build() {
      Preconditions.checkArgument(module != null || project != null,
          "Error: the final module is not final associated to a final module or final a project");
      return new ViewConfiguration(this);
    }

    public Builder setDescription(final String description) {
      this.description =
          Preconditions.checkNotNull(description, "Error: description argument can not be null");
      return this;
    }

    public Builder setModificationDate(final Date date) {
      modificationDate = Preconditions.checkNotNull(date, "Error: date argument can not be null");
      return this;
    }

    public Builder setCreationDate(final Date date) {
      creationDate = Preconditions.checkNotNull(date, "Error: date argument can not be null");
      return this;
    }

    public Builder setModule(final INaviModule module) {
      this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
      return this;
    }

    public Builder setProject(final INaviProject project) {
      this.project = Preconditions.checkNotNull(project, "Error: project argument can not be null");
      return this;
    }

    public Builder setTags(final Set<CTag> tags) {
      viewTags = Preconditions.checkNotNull(tags, "Error: tags argument can not be null");
      return this;
    }

    public Builder setStaredState(final boolean staredState) {
      this.staredState = staredState;
      return this;
    }

    public Builder setUnloadedEdgeCount(final int edgeCount) {
      this.edgeCount = edgeCount;
      return this;
    }

    public Builder setUnloadedNodeCount(final int nodeCount) {
      this.nodeCount = nodeCount;
      return this;
    }

    public Builder setGraphType(final GraphType graphType) {
      this.graphType =
          Preconditions.checkNotNull(graphType, "Error: graphType argument can not be null");
      return this;
    }
  }

  @Override
  public int getUnloadedEdgeCount() {
    return edgeCount;
  }


  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getId() {
    return viewId;
  }

  @Override
  public Date getModificationDate() {
    return modificationDate;
  }

  @Override
  public INaviModule getModule() {
    return module;
  }

  @Override
  public String getName() {
    return name;
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
    return viewTags;
  }

  @Override
  public boolean isStared() {
    return staredState;
  }

  @Override
  public boolean isTagged() {
    return !viewTags.isEmpty();
  }

  @Override
  public boolean isTagged(final CTag tag) {
    return Iterables.tryFind(viewTags, new Predicate<CTag>() {
      @Override
      public boolean apply(final CTag currentTag) {
        return currentTag.getId() == tag.getId();
      }
    }).isPresent();
  }

  @Override
  public void setDescription(final String description) {
    throw new IllegalStateException();
  }

  @Override
  public void setId(final int viewId) {
    throw new IllegalStateException();
  }

  @Override
  public void setName(final String name) {
    throw new IllegalStateException();
  }

  @Override
  public void setNameInternal(final String name) {
    throw new IllegalStateException();
  }

  @Override
  public void setDescriptionInternal(final String description) {
    throw new IllegalStateException();
  }

  @Override
  public void setStaredInternal(final boolean isStared) {
    throw new IllegalStateException();
  }

  @Override
  public void setModificationDateInternal(final Date modificationDate) {
    throw new IllegalStateException();
  }

  @Override
  public void setStared(final boolean stared) {
    throw new IllegalStateException();
  }

  @Override
  public void tagView(final CTag tag) {
    // TODO Auto-generated method stub
  }

  @Override
  public void untagView(final CTag tag) {
    // TODO Auto-generated method stub
  }

  @Override
  public void updateModificationDate() {
    // TODO Auto-generated method stub
  }

  @Override
  public GraphType getUnloadedGraphType() {
    return graphType;
  }

  @Override
  public int getUnloadedNodeCount() {
    return nodeCount;
  }
}
