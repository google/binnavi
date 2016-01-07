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
package com.google.security.zynamics.binnavi.Gui.Actions;

import com.google.security.zynamics.binnavi.Gui.Tutorials.CTutorialDialog;
import com.google.security.zynamics.binnavi.Tutorials.CTutorial;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.swing.Action;

/**
 * Action proxy object that is used to wrap normal actions to extend their behavior in a default
 * way.
 */
public final class CActionProxy implements InvocationHandler {
  /**
   * The wrapped action object.
   */
  private final Action m_action;

  /**
   * Creates a new action proxy object.
   * 
   * @param action The wrapped action object.
   */
  private CActionProxy(final Action action) {
    this.m_action = action;
  }

  /**
   * Checks whether a method is an actionPerformed method.
   * 
   * @param method The method to check.
   * 
   * @return True, if the function is an actionPerformed method.
   */
  private static boolean isExecuteMethod(final Method method) {
    return method.getName().contains("actionPerformed");
  }

  /**
   * Creates a new proxy object.
   * 
   * @param action The wrapped action object.
   * 
   * @return The created proxy object.
   */
  public static Action proxy(final Action action) {
    return (Action) java.lang.reflect.Proxy.newProxyInstance(action.getClass().getClassLoader(),
        new Class<?>[] {Action.class}, new CActionProxy(action));
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {
    if (GraphicsEnvironment.isHeadless() || !isExecuteMethod(method)) {
      return method.invoke(m_action, args);
    }

    final CTutorial currentTutorial = CTutorialDialog.instance().getCurrentTutorial();

    if (currentTutorial == null) {
      return method.invoke(m_action, args);
    }

    final long actionId =
        (Long) ReflectionHelpers.getStaticField(m_action.getClass(), "serialVersionUID");

    if (currentTutorial.getCurrentStep().handles(actionId)) {
      final Object result = method.invoke(m_action, args);

      if (currentTutorial.getCurrentStep().mandates(actionId)) {
        currentTutorial.next();
      }

      return result;
    } else {
      CTutorialDialog.instance().wrongAction(actionId);

      return null;
    }
  }
}
