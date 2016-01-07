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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CFunctionListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.disassembly.IFunctionListener;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * Contains functions for working with the functions of a module.
 */
public final class CFunctionContainer {
  /**
   * The module the functions belong to.
   */
  private final INaviModule m_module;

  /**
   * Address => Function map for fast lookup of functions by address.
   */
  private final Map<IAddress, INaviFunction> m_functionMap = new HashMap<IAddress, INaviFunction>();

  /**
   * The functions of the module.
   */
  private final IFilledList<INaviFunction> m_functions;

  /**
   * Synchronizes the module with its functions.
   */
  private final IFunctionListener<IComment> m_functionListener = new InternalFunctionListener();

  /**
   * Creates a new function container object.
   * 
   * @param module The module the functions belong to.
   * @param functions The functions of the module.
   */
  public CFunctionContainer(final INaviModule module, final List<INaviFunction> functions) {
    m_module = Preconditions.checkNotNull(module, "IE02399: module argument can not be null");
    m_functions =
        new FilledList<INaviFunction>(Preconditions.checkNotNull(functions,
            "IE02400: functions argument can not be null"));

    for (final INaviFunction function : functions) {
      m_functionMap.put(function.getAddress(), function);
    }

    for (final INaviFunction function : functions) {
      function.addListener(m_functionListener);
    }
  }

  /**
   * Closes the function container.
   * 
   * @return True, if the container was closed. False, if something prevented the container from
   *         closing.
   */
  public boolean close() {
    for (final INaviFunction function : m_functions) {
      if (function.isLoaded() && function.close()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Tries to find a function inside the module that starts at a given start address.
   * 
   * @param address The start address of the function.
   * 
   * @return The function of the module that starts at the given address or null if there is no such
   *         address.
   */
  public INaviFunction getFunction(final IAddress address) {
    Preconditions.checkNotNull(address, "IE00197: Address argument can not be null");

    return m_functionMap.get(address);
  }

  /**
   * Returns a function identified by name.
   * 
   * @param name The function name to search for.
   * 
   * @return The function with the given name.
   * 
   * @throws MaybeNullException Thrown if there is no function with the given name in the module.
   */
  public INaviFunction getFunction(final String name) throws MaybeNullException {
    Preconditions.checkNotNull(name, "IE00176: Name argument can not be null");

    for (final INaviFunction function : m_functions) {
      if (function.getName().equals(name)) {
        return function;
      }
    }

    throw new MaybeNullException();
  }

  /**
   * Returns the number of functions in this module. This number equals the number of native Flow
   * graph views.
   * 
   * @return The number of functions in this module.
   */
  public int getFunctionCount() {
    return m_functions.size();
  }

  /**
   * Returns all functions of the module.
   * 
   * @return All functions of the module.
   */
  public List<INaviFunction> getFunctions() {
    return new ArrayList<INaviFunction>(m_functions);
  }

  /**
   * Synchronizes the module with its functions.
   */
  private class InternalFunctionListener extends CFunctionListenerAdapter {
    @Override
    public void changedName(final IFunction function, final String name) {
      final INaviView view =
          m_module.getContent().getViewContainer().getView((INaviFunction) function);

      try {
        view.getConfiguration().setName(name);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);
      }
    }
  }
}
