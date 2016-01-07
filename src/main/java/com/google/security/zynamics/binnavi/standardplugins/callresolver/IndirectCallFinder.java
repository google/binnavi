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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.security.zynamics.binnavi.API.database.TableNames;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.ExpressionType;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Module;

/**
 * Contains code for finding the indirect call instructions of a module.
 */
public final class IndirectCallFinder {
  /**
   * Returns the direct function call addresses for a given module.
   * 
   * @param module The module whose direct function call addresses are returned.
   * 
   * @return The direct function call addresses of the module.
   */
  private static Set<Address> getDirectFunctionCalls(final Module module) {
    final Set<Address> set = new HashSet<Address>();

    final String query =
        "SELECT it.address" + " FROM " + TableNames.INSTRUCTIONS_TABLE + " AS it" + " JOIN "
            + TableNames.ADDRESS_REFERENCES_TABLE + " AS art ON it.address = art.address "
            + " AND it.module_id = art.module_id"
            + " WHERE type = 'call_direct' AND art.module_id = " + module.getId();

    try {
      final ResultSet resultSet = module.getDatabase().executeQuery(query);

      try {
        while (resultSet.next()) {
          set.add(new Address(resultSet.getLong("address")));
        }
      } finally {
        resultSet.close();
      }
    } catch (final SQLException exception) {
      exception.printStackTrace();
    }

    return set;
  }

  /**
   * Returns information about all indirect call instructions of a module.
   * 
   * @param module The module whose indirect call instructions are found.
   * 
   * @return A list of indirect call information.
   */
  public static List<IndirectCall> find(final Module module) {
    final Set<Address> importedFunctionCalls = getDirectFunctionCalls(module);

    final Map<Address, Function> functionMap = new HashMap<Address, Function>();

    for (final Function function : module.getFunctions()) {
      functionMap.put(function.getAddress(), function);
    }

    // TODO (timkornau): make sure to only include the call sides which we are willing to
    // take a look at depending on the architecture of the module.

    final String callMnemonics = "'call', " + // x86
        "'bal', 'bgezal', 'bgezall', 'bltzal', 'bltzall', 'jal', 'jalr', " + // MIPS
        "'bl', 'blx', " + // ARM
        "'bcctrl', 'bcctr'" // PowerPC
    ;

    final String registerOrdinal = String.valueOf(ExpressionType.Register.ordinal() + 1);
    final String dereferenceOrdinal = String.valueOf(ExpressionType.MemDeref.ordinal() + 1);

    final String query =
        "SELECT ft.address AS faddress, it.address AS iaddress " + " FROM "
            + TableNames.FUNCTIONS_TABLE + " AS ft " + " JOIN " + TableNames.FUNCTION_VIEWS_TABLE
            + " AS fvt ON ft.address = fvt.function " + " AND ft.module_id = fvt.module_id" +

            " JOIN " + TableNames.NODES_TABLE + " AS nt ON fvt.view_id = nt.view_id " +

            " JOIN " + TableNames.CODENODE_INSTRUCTIONS_TABLE + " AS cit ON nt.id = cit.node_id "
            + " AND cit.module_id = ft.module_id " +

            " JOIN " + TableNames.INSTRUCTIONS_TABLE + " AS it ON it.address = cit.address "
            + " AND it.module_id = cit.module_id" +

            " JOIN " + TableNames.OPERANDS_TABLE + " AS ot ON it.address = ot.address "
            + " AND it.module_id = ot.module_id" +

            " JOIN " + TableNames.EXPRESSION_TREE_MAPPING_TABLE
            + " AS etm ON ot.expression_tree_id = etm.tree_id "
            + " AND etm.module_id = ft.module_id" +

            " JOIN " + TableNames.EXPRESSION_TREE_TABLE + " AS et ON et.id = etm.tree_node_id "
            + " AND et.module_id = ft.module_id" +

            " WHERE ft.module_id = " + module.getId() + " and mnemonic in (" + callMnemonics + ") "
            + " AND (et.type in (" + registerOrdinal + ", " + dereferenceOrdinal + "))"
            + " GROUP BY faddress, iaddress";

    final List<IndirectCall> addresses = new ArrayList<IndirectCall>();

    try {
      final ResultSet resultSet = module.getDatabase().executeQuery(query);

      try {
        while (resultSet.next()) {
          final Address address = new Address(resultSet.getLong("iaddress"));

          if (importedFunctionCalls.contains(address)) {
            continue;
          }

          final Address faddress = new Address(resultSet.getLong("faddress"));

          final Function function = functionMap.get(faddress);

          addresses.add(new IndirectCall(module, function, address));
        }
      } finally {
        resultSet.close();
      }

      return addresses;
    } catch (final SQLException exception) {
      exception.printStackTrace();

      return new ArrayList<IndirectCall>();
    }
  }
}
