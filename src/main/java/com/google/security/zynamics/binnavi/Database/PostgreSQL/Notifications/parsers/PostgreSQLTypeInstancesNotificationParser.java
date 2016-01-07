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

import java.math.BigInteger;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.PGNotification;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypeInstancesNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.PostgreSQLNotificationParser;
import com.google.security.zynamics.binnavi.Database.cache.InstructionCache;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.zylib.disassembly.OperandOrderIterator;

/**
 * Implements the handling of {@link PGNotification notifications} related to {@link TypeInstance
 * type instances} and {@link TypeInstanceReference type instance references}.
 */
public class PostgreSQLTypeInstancesNotificationParser implements
    PostgreSQLNotificationParser<TypeInstancesNotificationContainer> {

  /**
   * The regular expression pattern for a {@link CTableNames bn_type_instances}
   * {@link PGNotification notification}.
   */
  private static final String typeInstanceNotification = "(^" + CTableNames.TYPE_INSTANCE_TABLE
      + ")" + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)" + "\\s(\\d*)$";
  private static final Pattern typeInstanceNotificationPattern;

  /**
   * The regular expression pattern for a {@link CTableNames bn_expression_type_instances}
   * {@link PGNotification notification}.
   */
  private static final String expressionTypeInstanceNotification = "(^"
      + CTableNames.EXPRESSION_TYPE_INSTANCES_TABLE + ")" + "\\s(INSERT|UPDATE|DELETE)"
      + "\\s(\\d*)" + "\\s(\\d*)" + "\\s(\\d*)" + "\\s(\\d*)" + "\\s(\\d*)$";
  private static final Pattern expressionTypeInstanceNotificationPattern;

  /**
   * Static initializer to only compile the used patterns in the class once.
   */
  static {
    typeInstanceNotificationPattern = Pattern.compile(typeInstanceNotification);
    expressionTypeInstanceNotificationPattern = Pattern.compile(expressionTypeInstanceNotification);
  }

  /**
   * Parses type instance notifications. This function parses messages for synchronization of
   * changes in the {@link CTableNames bn_type_instances} table.
   * 
   * @param notification The {@link PGNotification} which carries the information to be parsed.
   * @return A {@link TypeInstancesNotificationContainer} with the parsed information.
   */
  private TypeInstancesNotificationContainer parseTypeInstanceNotification(
      final PGNotification notification) {

    final Matcher matcher = typeInstanceNotificationPattern.matcher(notification.getParameter());
    if (!matcher.find()) {
      throw new IllegalStateException("Error: compiled pattern: "
          + typeInstanceNotificationPattern.toString() + " did not match notification: "
          + notification.getParameter());
    }

    final String databaseOperation = matcher.group(2);
    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final Integer typeInstanceId = Integer.parseInt(matcher.group(4));

    return new TypeInstancesNotificationContainer(databaseOperation, moduleId, typeInstanceId,
        Optional.<BigInteger>absent(), Optional.<Integer>absent(), Optional.<Integer>absent());
  }

  /**
   * Parses expression type instances or simpler type substitutions on operands. This function
   * parses messages for synchronization of changes in the {@link CTableNames
   * bn_expression_type_instances} table.
   * 
   * @param notification The {@link PGNotification} which carries the information to be parsed.
   * @return A {@link TypeInstancesNotificationContainer} with the parsed information.
   */
  private TypeInstancesNotificationContainer parseExpressionTypeInstanceNotification(
      final PGNotification notification) {

    final Matcher matcher =
        expressionTypeInstanceNotificationPattern.matcher(notification.getParameter());
    if (!matcher.find()) {
      throw new IllegalStateException("Error: compiled pattern: "
          + expressionTypeInstanceNotificationPattern.toString() + " did not match notification: "
          + notification.getParameter());
    }

    final String databaseOperation = matcher.group(2);
    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final BigInteger address = new BigInteger(matcher.group(4));
    final Integer position = Integer.parseInt(matcher.group(5));
    final Integer expressionId = Integer.parseInt(matcher.group(6));
    final Integer typeInstanceId = Integer.parseInt(matcher.group(7));

    return new TypeInstancesNotificationContainer(databaseOperation, moduleId, typeInstanceId,
        Optional.of(address), Optional.of(position), Optional.of(expressionId));
  }

  @Override
  public Collection<TypeInstancesNotificationContainer> parse(
      final Collection<PGNotification> notifications, final SQLProvider provider) {

    Preconditions.checkNotNull(notifications, "Error: notifications argument can not be null");
    Preconditions.checkNotNull(provider, "Error: provider argument can not be null");

    final Collection<TypeInstancesNotificationContainer> containers = Lists.newArrayList();

    for (final PGNotification notification : notifications) {
      if (notification.getParameter().startsWith(CTableNames.TYPE_INSTANCE_TABLE)) {
        containers.add(parseTypeInstanceNotification(notification));
      } else if (notification.getParameter()
          .startsWith(CTableNames.EXPRESSION_TYPE_INSTANCES_TABLE)) {
        containers.add(parseExpressionTypeInstanceNotification(notification));
      } else {
        throw new IllegalStateException("Error: Table name supplied in notification "
            + notification.getParameter()
            + " does not match tables where type instance notifications are accepted on.");
      }
    }
    return containers;
  }

  @Override
  public void inform(final Collection<TypeInstancesNotificationContainer> containers,
      final SQLProvider provider) throws CouldntLoadDataException {

    for (final TypeInstancesNotificationContainer container : containers) {
      if (!provider.findModule(container.getModuleId()).isLoaded()) {
        continue; // we do not need to look at notifications for modules not loaded.
      }
      if (container.getAddress().isPresent()) {
        informExpressionTypeInstanceNotification(container, provider);
      } else {
        informTypeInstanceNotification(container, provider);
      }
    }
  }

  /**
   * This function informs the {@link TypeInstanceContainer} about changes related to type instances
   * changes.
   * 
   * @param container The {@link TypeInstancesNotificationContainer} holding the parsed information.
   * @param provider The {@link SQLProvider} used to access the database with.
   * 
   * @throws CouldntLoadDataException if the necessary data could not be loaded from the database.
   */
  private void informTypeInstanceNotification(final TypeInstancesNotificationContainer container,
      final SQLProvider provider) throws CouldntLoadDataException {
    final TypeInstanceContainer typeContainer =
        provider.findModule(container.getModuleId()).getContent().getTypeInstanceContainer();
    if (container.getDatabaseOperation().equals("INSERT")) {
      typeContainer.loadInstance(container.getTypeInstanceId());
    } else if (container.getDatabaseOperation().equals("UPDATE")) {
      typeContainer.reloadInstance(container.getTypeInstanceId());
    } else if (container.getDatabaseOperation().equals("DELETE")) {
      typeContainer.deleteInstance(container.getTypeInstanceId());
    } else {
      throw new IllegalStateException("Error: the database operation "
          + container.getDatabaseOperation() + " is currently not supported.");
    }
  }

  /**
   * This function informs the {@link TypeInstanceContainer} about changes related to expression
   * type instances also known as cross references for type instances.
   * 
   * @param container The {@link TypeInstancesNotificationContainer} holding the parsed information.
   * @param provider The {@link SQLProvider} used to access the database.
   * @throws CouldntLoadDataException
   */
  private void informExpressionTypeInstanceNotification(
      final TypeInstancesNotificationContainer container, final SQLProvider provider)
      throws CouldntLoadDataException {
    final TypeInstanceContainer typeContainer =
        provider.findModule(container.getModuleId()).getContent().getTypeInstanceContainer();
    if (container.getDatabaseOperation().equals("INSERT")) {
      final TypeInstanceReference reference =
          typeContainer.loadInstanceReference(container.getTypeInstanceId(), container.getAddress()
              .get(), container.getPosition().get(), container.getExpressionId().get());
      final INaviInstruction instruction =
          InstructionCache.get(provider).getInstructionByAddress(reference.getAddress(),
              reference.getTypeInstance().getModule().getConfiguration().getId());
      if (instruction != null) {
        final INaviOperandTree operandTree = instruction.getOperands().get(reference.getPosition());
        final INaviOperandTreeNode root = operandTree.getRootNode();
        final OperandOrderIterator iterator = new OperandOrderIterator(root);
        while (iterator.next()) {
          final INaviOperandTreeNode currentNode = (INaviOperandTreeNode) iterator.current();
          if (currentNode.getId() == container.getExpressionId().get()) {
            typeContainer.initializeTypeInstanceReference(reference.getAddress(),
                reference.getPosition(), container.getTypeInstanceId(), currentNode);
            break;
          }
        }
      }
    } else if (container.getDatabaseOperation().equals("UPDATE")) {
      // currently not be possible at all.
    } else if (container.getDatabaseOperation().equals("DELETE")) {
      typeContainer.deleteReference(container.getTypeInstanceId(), container.getAddress().get(),
          container.getPosition().get(), container.getExpressionId().get());
    } else {
      throw new IllegalStateException("Error: the database operation "
          + container.getDatabaseOperation() + " is currently not supported.");
    }
  }
}
