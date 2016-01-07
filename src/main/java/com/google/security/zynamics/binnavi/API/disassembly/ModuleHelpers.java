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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CTableNames;

// / Convenience functions for working with modules.
/**
 * Offers convenience functions for working with modules. Please note that many convenience
 * functions are just straight-forward implementations of commonly used algorithms and therefore can
 * have significant runtime costs.
 */
public final class ModuleHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private ModuleHelpers() {
  }

  /**
   * Returns the start addresses of all basic blocks of a module. This function is probably the
   * fastest way for retrieving the basic block addresses of a module.
   * 
   * @param module The module whose basic block addresses are returned.
   * 
   * @return A set that contains all basic block addresses of the module.
   * 
   * @throws CouldntLoadDataException Thrown if the basic block addresses could not be determined.
   */
  public static Set<Address> getBasicBlockAddresses(final Module module)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    final String query =
        "SELECT address FROM " + CTableNames.CODENODE_INSTRUCTIONS_TABLE + " AS cit JOIN "
            + CTableNames.NODES_TABLE + " AS nt ON node_id = nt.id " + " JOIN "
            + CTableNames.VIEWS_TABLE + " AS vt ON nt.view_id = vt.id "
            + " WHERE position = 0 AND vt.type = 'native' AND cit.module_id = "
            + module.getNative().getConfiguration().getId() + " GROUP BY address ";

    try {
      final ResultSet resultSet = module.getDatabase().executeQuery(query);

      final Set<Address> addresses = new HashSet<Address>();

      try {
        while (resultSet.next()) {
          addresses.add(new Address(BigInteger.valueOf(resultSet.getLong("address"))));
        }
      } finally {
        resultSet.close();
      }

      return addresses;
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  // ! Finds the function that starts at a given address.
  /**
   * Returns the function of a module that starts at a given address.
   * 
   * This function is guaranteed to work in O(n) where n is the number of functions of the module.
   * 
   * @param module The module that contains all functions.
   * @param address The address of the function to search for.
   * 
   * @return The function that starts at the given address or null if there is no such function.
   */
  public static Function getFunction(final Module module, final Address address) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    for (final Function function : module.getFunctions()) {
      if (function.getAddress().equals(address)) {
        return function;
      }
    }

    return null;
  }

  // ! Finds the function that starts at the given address.
  /**
   * Returns the function of a module that starts at a given address.
   * 
   * This function is guaranteed to work in O(n) where n is the number of functions of the module.
   * 
   * @param module The module that contains all functions.
   * @param address The address of the function to search for.
   * 
   * @return The function that starts at the given address or null if there is no such function.
   */
  public static Function getFunction(final Module module, final long address) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    if (address < 0) {
      throw new IllegalArgumentException("Error: Address argument can not be negative");
    }

    for (final Function function : module.getFunctions()) {
      if (function.getAddress().toLong() == address) {
        return function;
      }
    }

    return null;
  }

  // ! Finds the function with a given name.
  /**
   * Returns the function of a module that has a given name. Please note that function names are not
   * unique and in case of more than one function with the given name it is undefined exactly which
   * of those functions is returned.
   * 
   * This function is guaranteed to work in O(n) where n is the number of functions of the module.
   * 
   * @param module The module that contains all functions.
   * @param name The name of the function to search for.
   * 
   * @return The function that has the given name or null if there is no such function.
   */
  public static Function getFunction(final Module module, final String name) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    Preconditions.checkNotNull(name, "Error: Name argument can not be null");

    for (final Function function : module.getFunctions()) {
      if (function.getName().equals(name)) {
        return function;
      }
    }

    return null;
  }
}
