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
package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * This class is a helper thread to use with the CEndlessProgressDialog class. To use this class do
 * the following.
 * 
 * 1. Subclass this class and provide the implementation of runExpensiveCommand 2. Create an
 * instance (called for example thread) of the subclassed class. 3. Create the endless progress
 * dialog (dlg) and pass the instance to the dialog constructor 4. Execute thread.start() on the
 * instance from step #2 5. Execute dlg.setVisible(true)
 */
public abstract class CEndlessHelperThread extends Thread implements IEndlessProgressModel,
    IEndlessDescriptionUpdater {
  private final ListenerProvider<IEndlessProgressListener> m_listeners =
      new ListenerProvider<IEndlessProgressListener>();

  private Exception m_exception;

  private void notifyListeners() {
    for (final IEndlessProgressListener listener : m_listeners) {
      listener.finished();
    }
  }

  @SuppressWarnings("deprecation")
  protected void finish() {
    notifyListeners();

    // TODO: Check, who is notified? If it's NOT the thread itself, "interrupt" could possible be
    // called in
    // order to stop this thread in a non deprecated way (but only if isInterruped() is proved
    // within
    // the threads
    // run routine, and when true, return from thread function)
    stop();
  }

  protected abstract void runExpensiveCommand() throws Exception;

  @Override
  public final void addProgressListener(final IEndlessProgressListener listener) {
    m_listeners.addListener(listener);
  }

  public Exception getException() {
    return m_exception;
  }

  @Override
  public final void removeProgressListener(final IEndlessProgressListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public final void run() {
    try {
      runExpensiveCommand();
    } catch (final Exception exception) {
      m_exception = exception;
    } finally {
      notifyListeners();
    }
  }

  @Override
  public void setDescription(final String description) {
    for (final IEndlessProgressListener listener : m_listeners) {
      listener.changedDescription(description);
    }
  }

  public void setGeneralDescription(final String description) {
    for (final IEndlessProgressListener listener : m_listeners) {
      listener.changedGeneralDescription(description);
    }
  }
}
