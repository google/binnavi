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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.PostgreSQLNotificationParser;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.functions.FunctionManager;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.postgresql.PGNotification;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgreSQLFunctionNotificationParser implements
    PostgreSQLNotificationParser<FunctionNotificationContainer> {

  private final static String functionNotificationPattern = "^(" + CTableNames.FUNCTIONS_TABLE + ")"
      + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)" + "\\s(\\d*)$";

  private void informFunctionNotification(final FunctionNotificationContainer container,
      final SQLProvider provider) throws CouldntLoadDataException {
    if (container.getDatabaseOperation().equals("INSERT")) {
      return; // we do not care about function creation currently.
    } else if (container.getDatabaseOperation().equals("UPDATE")) {
      final IAddress functionAddress = container.getFunctionAddress();
      final Integer moduleId = container.getModuleId();
      final INaviModule module = provider.findModule(moduleId);
      final INaviFunction function =
          FunctionManager.get(provider).getFunction(functionAddress, moduleId);
      final INaviFunction databaseFunction = provider.loadFunction(module, functionAddress);

      function.setDescriptionInternal(databaseFunction.getDescription());
      function.setNameInternal(databaseFunction.getName());
      if (databaseFunction.isForwarded()) {
        function.setForwardedFunctionInternal(FunctionManager.get(provider).getFunction(
            databaseFunction.getForwardedFunctionAddress(),
            databaseFunction.getForwardedFunctionModuleId()));
      } else {
        function.removeForwardedFunctionInternal();
      }
      function.setStackFrame(databaseFunction.getStackFrame());
      function.setPrototype(databaseFunction.getPrototype());
    } else if (container.getDatabaseOperation().equals("DELETE")) {
      return; // functions can currently not really be deleted.
    } else {
      throw new IllegalStateException(
          "IE02741: database operation: " + container.getDatabaseOperation() + " is not supported");
    }
  }

  private FunctionNotificationContainer parseFunctionNotification(final PGNotification notification,
      final SQLProvider provider) {

    final Pattern pattern = Pattern.compile(functionNotificationPattern);
    final Matcher matcher = pattern.matcher(notification.getParameter());
    if (!matcher.find()) {
      throw new IllegalStateException("IE02739: compiled pattern: " + pattern.toString()
          + " did not match notification: " + notification.getParameter());
    }

    final String databaseOperation = matcher.group(2);
    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final IAddress functionAddress = new CAddress(new BigInteger(matcher.group(4)));
    final INaviModule module = provider.findModule(moduleId);
    return new FunctionNotificationContainer(moduleId, module, functionAddress, databaseOperation);
  }

  @Override
  public void inform(final Collection<FunctionNotificationContainer> parsedFunctionNotifications,
      final SQLProvider provider) throws CouldntLoadDataException {
    Preconditions.checkNotNull(parsedFunctionNotifications,
        "Error: parsedFunctionNotifications argument can not be null");
    Preconditions.checkNotNull(provider, "IE02740: provider argument can not be null");

    for (final FunctionNotificationContainer container : parsedFunctionNotifications) {
      informFunctionNotification(container, provider);
    }
  }

  @Override
  public Collection<FunctionNotificationContainer> parse(
      final Collection<PGNotification> notifications, final SQLProvider provider) {

    Preconditions.checkNotNull(notifications, "IE02629: notifications argument can not be null");
    Preconditions.checkNotNull(provider, "IE02630: provider argument can not be null");

    final Collection<FunctionNotificationContainer> containers =
        new ArrayList<FunctionNotificationContainer>();

    for (final PGNotification notification : notifications) {
      if (notification.getParameter().startsWith(CTableNames.FUNCTIONS_TABLE)) {
        containers.add(parseFunctionNotification(notification, provider));
      } else {
        throw new IllegalStateException("IE02738: Table name supplied in notification: "
            + notification.getParameter()
            + " does not match tables where function notifications are accepted on.");
      }
    }
    return containers;
  }
}
