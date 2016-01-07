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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * This class handles clicks on the menu item that was added to the context menu of modules in the
 * project tree of the main window.
 */
public final class ResolveCallsAction extends AbstractAction {

  /**
   * Parent window of the resolver dialog.
   */
  private final JFrame parent;

  /**
   * The module whose calls should be resolved.
   */
  private final ICallResolverTarget module;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window of the resolver dialog.
   * @param module The module whose calls should be resolved.
   */
  public ResolveCallsAction(final JFrame parent, final ICallResolverTarget module) {
    super("Resolve Calls");

    this.parent = parent;
    this.module = module;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    CallResolverDialog.show(parent, module);
  }
}
