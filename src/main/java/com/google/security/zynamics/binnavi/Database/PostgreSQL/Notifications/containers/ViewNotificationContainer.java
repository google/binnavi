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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * The view notification container stores parsed notification information
 * related to views.
 */
public class ViewNotificationContainer {

  private final Integer viewId;
  private final Optional<INaviView> view;
  private final Optional<Integer> notificationObjectId;
  private final Optional<INaviModule> notificationModule;
  private final Optional<INaviProject> notificationProject;
  private final String databaseOperation;

  public ViewNotificationContainer(final Integer viewId,
      final Optional<INaviView> view,
      final Optional<Integer> notificationObjectId,
      final Optional<INaviModule> notificationModule,
      final Optional<INaviProject> notificationProject,
      final String databaseOperation) {

    this.viewId = Preconditions.checkNotNull(viewId, "IE02627: viewId argument can not be null.");
    this.view = Preconditions.checkNotNull(view, "IE02628: view argument can not be null.");
    this.notificationObjectId = Preconditions.checkNotNull(
        notificationObjectId, "Error: notificationObjectId argument can not be null.");
    this.notificationModule = Preconditions.checkNotNull(
        notificationModule, "Error: notification Module argument can not be null.");
    this.notificationProject = Preconditions.checkNotNull(
        notificationProject, "Error: notification Project argument can not be null.");
    this.databaseOperation = Preconditions.checkNotNull(
        databaseOperation, "Error: databaseOperation argument can not be null.");
  }

  public Integer getViewId() {
    return viewId;
  }

  public Optional<INaviView> getView() {
    return view;
  }

  public Optional<Integer> getNotificationObjectId() {
    return notificationObjectId;
  }

  public Optional<INaviModule> getNotificationModule() {
    return notificationModule;
  }

  public Optional<INaviProject> getNotificationProject() {
    return notificationProject;
  }

  public String getDatabaseOperation() {
    return databaseOperation;
  }
}
