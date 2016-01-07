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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * This class contains the SQL queries for working with address spaces.
 */
public final class PostgreSQLAddressSpaceFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLAddressSpaceFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks the validity of a given SQL provider and a given address space. If there is a problem
   * with the arguments, an exception is thrown.
   * 
   * @param provider The SQL provider to check.
   * @param addressSpace The address space to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider,
      final INaviAddressSpace addressSpace) {
    Preconditions.checkNotNull(provider, "IE00387: Provider argument can not be null");
    Preconditions.checkNotNull(addressSpace, "IE00388: Address space argument can not be null");
    Preconditions.checkArgument(addressSpace.inSameDatabase(provider),
        "IE00389: Address space is not part of this database");
  }

  /**
   * Adds a module to an address space. If adding the module was successful, the modification date
   * of the address space is updated.
   * 
   * The module and the address space must both be stored in the database connected to by the
   * provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space where the module is added.
   * @param module The module that is added to the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the module could not be added to the address space.
   */
  public static void addModule(final AbstractSQLProvider provider,
      final INaviAddressSpace addressSpace, final INaviModule module)
      throws CouldntSaveDataException {
    checkArguments(provider, addressSpace);

    Preconditions.checkNotNull(module, "IE01859: Module argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE01860: Module is not part of this database");

    final CConnection connection = provider.getConnection();

    final int addressSpaceId = addressSpace.getConfiguration().getId();
    final int moduleId = module.getConfiguration().getId();

    NaviLogger.info("Adding module %s (%d) to address space %s (%d)", addressSpace
        .getConfiguration().getName(), addressSpaceId, module.getConfiguration().getName(),
        moduleId);

    final String query =
        "INSERT INTO " + CTableNames.SPACE_MODULES_TABLE + " VALUES(" + moduleId + ", "
            + addressSpaceId + ", 0)";

    try {
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }

  /**
   * Sets or removes the debugger of an address space. If assigning the debugger is successful, the
   * modification date of the address space is updated.
   * 
   * The address space and the debugger must both reside in the database connected to by the
   * provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space whose debugger is set.
   * @param debugger The debugger that is assigned to the address space or null to remove the
   *        current debugger from the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the debugger could not be assigned to the address
   *         space.
   */
  public static void assignDebugger(final AbstractSQLProvider provider,
      final CAddressSpace addressSpace, final DebuggerTemplate debugger)
      throws CouldntSaveDataException {
    checkArguments(provider, addressSpace);

    // ATTENTION: Argument "debugger" can be null

    if ((debugger != null) && !debugger.inSameDatabase(provider)) {
      throw new IllegalArgumentException("IE00392: Debugger is not part of this database");
    }

    final CConnection connection = provider.getConnection();

    try {
      final String debuggerValue = debugger == null ? "NULL" : String.valueOf(debugger.getId());

      final String query =
          String.format("UPDATE %s SET debugger_id = %s WHERE id= %d",
              CTableNames.ADDRESS_SPACES_TABLE, debuggerValue, addressSpace.getConfiguration()
                  .getId());

      connection.executeUpdate(query, true);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }

  /**
   * Deletes an address space from the database.
   * 
   * The address space must be stored in the database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space to be deleted.
   * 
   * @throws CouldntDeleteException Thrown if the address space could not be deleted from the
   *         database.
   */
  public static void deleteAddressSpace(final AbstractSQLProvider provider,
      final INaviAddressSpace addressSpace) throws CouldntDeleteException {
    checkArguments(provider, addressSpace);

    NaviLogger.info("Deleting address space %s", addressSpace.getConfiguration().getName());

    PostgreSQLHelpers.deleteById(provider.getConnection(), CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }

  /**
   * Returns the modification date of an address space.
   * 
   * The address space must be stored in the database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space whose modification date is returned.
   * 
   * @return The modification date of the given address space.
   * 
   * @throws CouldntLoadDataException Thrown if the modification date of the address space could not
   *         be determined.
   */
  public static Date getModificationDate(final AbstractSQLProvider provider,
      final CAddressSpace addressSpace) throws CouldntLoadDataException {
    checkArguments(provider, addressSpace);

    return PostgreSQLHelpers.getModificationDate(provider.getConnection(),
        CTableNames.ADDRESS_SPACES_TABLE, addressSpace.getConfiguration().getId());
  }

  /**
   * Loads the modules of an address space and their respective image bases inside the address
   * space.
   * 
   * The address space must be stored in the database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space whose modules are loaded.
   * 
   * @return A list of the modules of the address space and their image base.
   * 
   * @throws CouldntLoadDataException Thrown if the modules that belong to the address space could
   *         not be loaded.
   */
  public static List<Pair<IAddress, INaviModule>> loadModules(final AbstractSQLProvider provider,
      final CAddressSpace addressSpace) throws CouldntLoadDataException {
    checkArguments(provider, addressSpace);

    final CConnection connection = provider.getConnection();

    final List<Pair<IAddress, INaviModule>> modules = new ArrayList<Pair<IAddress, INaviModule>>();

    final String query =
        "SELECT id, name, md5, sha1, description, import_time, " + CTableNames.SPACE_MODULES_TABLE
            + ".image_base " + " FROM " + CTableNames.SPACE_MODULES_TABLE + " JOIN "
            + CTableNames.MODULES_TABLE + " ON id = module_id WHERE address_space_id = "
            + addressSpace.getConfiguration().getId();

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        final IAddress imageBase = PostgreSQLHelpers.loadAddress(resultSet, "image_base");
        final INaviModule module = provider.findModule(resultSet.getInt("id"));
        modules.add(new Pair<IAddress, INaviModule>(imageBase, module));
        }
      return modules;
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  /**
   * Removes a module from an address space. If removing the module was successful, the modification
   * date of the address space is updated.
   * 
   * The address space and the module must both reside in the database connected to by the provider
   * argument.
   * 
   * TODO (timkornau): What happens if the module does not belong to the address space? This
   * situation should be handled explicitly in the future.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space from which the module is removed.
   * @param module The module to be removed from the address space.
   * 
   * @throws CouldntDeleteException Thrown if the module could not be removed from the address
   *         space.
   * @throws CouldntSaveDataException Thrown if the modification time could not be set.
   */
  public static void removeModule(final AbstractSQLProvider provider,
      final INaviAddressSpace addressSpace, final INaviModule module)
      throws CouldntDeleteException, CouldntSaveDataException {
    checkArguments(provider, addressSpace);

    Preconditions.checkNotNull(module, "IE00393: Module argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00394: Module is not part of this database");

    final CConnection connection = provider.getConnection();

    final String query =
        "DELETE FROM " + CTableNames.SPACE_MODULES_TABLE + " WHERE address_space_id = "
            + addressSpace.getConfiguration().getId() + " AND module_id = "
            + module.getConfiguration().getId();

    try {
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntDeleteException(e);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }

  /**
   * Changes the description of the address space. If updating the description was successful, the
   * modification date of the address space is updated.
   * 
   * The address space must be stored in the database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space whose description is changed.
   * @param description The new description of the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the new description of the address space could not
   *         be stored in the database.
   */
  public static void setDescription(final AbstractSQLProvider provider,
      final CAddressSpace addressSpace, final String description) throws CouldntSaveDataException {
    checkArguments(provider, addressSpace);
    Preconditions.checkNotNull(description, "IE00395: Description argument can not be null");
    final CConnection connection = provider.getConnection();
    PostgreSQLHelpers.setDescription(connection, addressSpace.getConfiguration().getId(),
        description, CTableNames.ADDRESS_SPACES_TABLE);
    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }

  /**
   * Changes the image base of a module inside an address space. If updating the image base was
   * successful, the modification date of the address space is updated.
   * 
   * The address space and the module must both be stored in the database connected to by the
   * provider argument.
   * 
   * TODO (timkornau): What happens if the module does not belong to the address space? This
   * situation should be handled explicitly in the future.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space the module belongs to.
   * @param module The module whose image base is changed.
   * @param address The new image base of the module in the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the new image base value could not be stored in the
   *         database.
   */
  public static void setImageBase(final AbstractSQLProvider provider,
      final INaviAddressSpace addressSpace, final INaviModule module, final IAddress address)
      throws CouldntSaveDataException {
    checkArguments(provider, addressSpace);
    Preconditions.checkNotNull(module, "IE00396: Module argument can not be null");
    Preconditions.checkNotNull(address, "IE00397: Address argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00398: Module is not part of this database");

    final CConnection connection = provider.getConnection();

    try {
      final String query =
          String.format("UPDATE %s SET image_base = %s "
              + " WHERE module_id = %d AND address_space_id = %d", CTableNames.SPACE_MODULES_TABLE,
              address.toBigInteger().toString(), module.getConfiguration().getId(), addressSpace
                  .getConfiguration().getId());
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }

  /**
   * Changes the name of an address space. If updating the name was successful, the modification
   * date of the address space is updated.
   * 
   * The address space must be stored in the database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the database connection.
   * @param addressSpace The address space whose name is changed.
   * @param name The new name of the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the name of the address space could not be changed.
   */
  public static void setName(final AbstractSQLProvider provider, final CAddressSpace addressSpace,
      final String name) throws CouldntSaveDataException {
    checkArguments(provider, addressSpace);

    Preconditions.checkNotNull(name, "IE00399: Name argument can not be null");

    final CConnection connection = provider.getConnection();

    PostgreSQLHelpers.setName(connection, addressSpace.getConfiguration().getId(), name,
        CTableNames.ADDRESS_SPACES_TABLE);

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.ADDRESS_SPACES_TABLE,
        addressSpace.getConfiguration().getId());
  }
}
