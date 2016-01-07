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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypesNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.PostgreSQLNotificationParser;
import com.google.security.zynamics.binnavi.Database.cache.InstructionCache;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.OperandOrderIterator;

import org.postgresql.PGNotification;

import java.math.BigInteger;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that parses and informs about {@link PGNotification notifications} related to
 * {@link TypeMember type members}, {@link BaseType base types} and {@link TypeSubstitution type
 * substitutions}.
 */
public class PostgreSQLTypesNotificationParser implements
PostgreSQLNotificationParser<TypesNotificationContainer> {

  /**
   * The regular expression pattern for a {@link CTableNames bn_expression_types}
   * {@link PGNotification notification}.
   */
  private static final String EXPRESSION_TYPES_NOTIFICATION_REGEX = "^("
      + CTableNames.EXPRESSION_TYPES_TABLE + ")" + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)"
      + "\\s(\\d*)" + "\\s(\\d*)" + "\\s(\\d*)";
  private static final Pattern EXPRESSION_TYPES_NOTIFICATION_PATTERN;

  /**
   * The regular expression pattern for a {@link CTableNames bn_types} {@link PGNotification
   * notification}.
   */
  private static final String TYPES_NOTIFICATION_REGEX = "^(" + CTableNames.TYPE_MEMBERS_TABLE + ")"
      + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)" + "\\s(\\d*)";
  private static final Pattern TYPES_NOTIFICATION_PATTERN;

  /**
   * The regular expression pattern for a {@link CTableNames bn_base_types} {@link PGNotification
   * notification}.
   */
  private static final String BASE_TYPE_NOTIFICATION_REGEX = "^(" + CTableNames.BASE_TYPES_TABLE
      + ")" + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)" + "\\s(\\d*)";
  private static final Pattern BASE_TYPES_NOTIFICATION_PATTERN;

  /**
   * Static initializer to only compile the used patterns in the class once.
   */
  static {
    EXPRESSION_TYPES_NOTIFICATION_PATTERN = Pattern.compile(EXPRESSION_TYPES_NOTIFICATION_REGEX);
    TYPES_NOTIFICATION_PATTERN = Pattern.compile(TYPES_NOTIFICATION_REGEX);
    BASE_TYPES_NOTIFICATION_PATTERN = Pattern.compile(BASE_TYPE_NOTIFICATION_REGEX);
  }

  /**
   * Only used internally to find the necessary {@link INaviOperandTreeNode operand node} where a
   * {@link TypeSubstitution type substitution} is associated to.
   * 
   * @param provider The {@link SQLProvider} used to differentiate between the different caches used
   *        for getting {@link INaviInstruction instructions}.
   * @param moduleId The id of the {@link INaviModule module} this {@link TypeSubstitution type
   *        substitution} is associated to.
   * @param address The {@link CAddress} of the {@link INaviInstruction instruction} where the
   *        {@link TypeSubstitution type substitution} is associated to.
   * @param position The {@link INaviOperandTree operand tree} position where the
   *        {@link TypeSubstitution type substitution} is associated to.
   * @param operandNodeId the id of the {@link INaviOperandTreeNode operand node} to which the
   *        {@link TypeSubstitution type substitution} is associated.
   * @return A {@link INaviOperandTreeNode} if it is in the cache otherwise null;
   */
  private INaviOperandTreeNode findOperandTreeNode(final SQLProvider provider, final int moduleId,
      final CAddress address, final int position, final int operandNodeId) {
    final INaviInstruction instruction =
        InstructionCache.get(provider).getInstructionByAddress(address, moduleId);
    if (instruction != null) {
      final INaviOperandTree operandTree = instruction.getOperands().get(position);
      final INaviOperandTreeNode root = operandTree.getRootNode();
      final OperandOrderIterator iterator = new OperandOrderIterator(root);
      while (iterator.next()) {
        final INaviOperandTreeNode currentNode = (INaviOperandTreeNode) iterator.current();
        if (currentNode.getId() == operandNodeId) {
          return currentNode;
        }
      }
    }
    return null;
  }

  /**
   * Informs the {@link TypeManager type manager} about the given change from the
   * {@link PGNotification notification}.
   *
   * @param container The {@link TypesNotificationContainer} which carries the parsed information.
   * @param provider The {@link SQLProvider} used to access the database.
   *
   * @throws CouldntLoadDataException if the necessary information could not be loaded from the
   *         database.
   */
  private void informBaseTypesNotification(final TypesNotificationContainer container,
      final SQLProvider provider) throws CouldntLoadDataException {

    final TypeManager typeManager = provider.findModule(container.getModuleId()).getTypeManager();
    if (container.getDatabaseOperation().equals("INSERT")) {
      typeManager.loadAndInitializeBaseType(container.getBaseTypeId().get());
    } else if (container.getDatabaseOperation().equals("UPDATE")) {
      typeManager.loadAndUpdateBaseType(container.getBaseTypeId().get());
    } else if (container.getDatabaseOperation().equals("DELETE")) {
      typeManager.removeBaseTypeInstance(container.getBaseTypeId().get());
    }
  }

  /**
   * Informs about necessary state changes related to {@link TypeSubstitution type substitutions}.
   *
   * @param container The {@link TypesNotificationContainer} holding the parsed
   *        {@link PGNotification notification} information.
   * @param provider The {@link SQLProvider} used to access the database with.
   *
   * @throws CouldntLoadDataException if the required information could not be loaded from the
   *         database.
   */
  private void informExpressionTypesNotification(final TypesNotificationContainer container,
      final SQLProvider provider) throws CouldntLoadDataException {
    final INaviModule module = provider.findModule(container.getModuleId());
    final TypeManager typeManager = module.getTypeManager();
    final INaviOperandTreeNode node =
        findOperandTreeNode(provider, container.getModuleId(), new CAddress(container.getAddress()
            .get()), container.position().get(), container.expressionId().get());
    if (node == null) {
      return;
    }
    if (container.getDatabaseOperation().equals("INSERT")) {
      final RawTypeSubstitution rawSubstitution = provider.loadTypeSubstitution(
          module, container.getAddress().get(), container.position().get(),
          container.expressionId().get());
      typeManager.initializeTypeSubstitution(node, rawSubstitution);
    } else if (container.getDatabaseOperation().equals("UPDATE")) {
      final RawTypeSubstitution rawSubstitution = provider.loadTypeSubstitution(
          module, container.getAddress().get(), container.position().get(),
          container.expressionId().get());
      typeManager.updateTypeSubstitution(node, rawSubstitution.getBaseTypeId(),
          rawSubstitution.getPath(), rawSubstitution.getOffset());
    } else if (container.getDatabaseOperation().equals("DELETE")) {
      typeManager.removeTypeSubstitutionInstance(node.getTypeSubstitution());
    } else {
      throw new IllegalStateException("Error: the database operation "
          + container.getDatabaseOperation() + " is currently not supported.");
    }
  }

  /**
   * Informs the {@link TypeManager type manager} about the given change from the
   * {@link PGNotification notification} for {@link TypeMember type members}.
   * 
   * @param container The {@link TypesNotificationContainer} which carries the parsed information.
   * @param provider The {@link SQLProvider} used to access the database.
   * 
   * @throws CouldntLoadDataException if the necessary information could not be loaded from the
   *         database.
   */
  private void informTypesNotification(final TypesNotificationContainer container,
      final SQLProvider provider) throws CouldntLoadDataException {

    final TypeManager typeManager = provider.findModule(container.getModuleId()).getTypeManager();
    if (container.getDatabaseOperation().equals("INSERT")) {
      typeManager.loadAndInitializeTypeMember(container.getTypeId().get());
    } else if (container.getDatabaseOperation().equals("UPDATE")) {
      typeManager.loadAndUpdateTypeMember(container.getTypeId().get());
    } else if (container.getDatabaseOperation().equals("DELETE")) {
      typeManager.removeMemberInstance(container.getTypeId().get());
    } else {
      throw new IllegalStateException("Error: the database operation "
          + container.getDatabaseOperation() + " is currently not supported.");
    }
  }

  /**
   * Parses base type {@link PGNotification notifications} for synchronizing the state of
   * {@link BaseType base types} between multiple instances of BinNavi.
   * 
   * @param notification THe {@link PGNotification} from the database.
   * @return A {@link TypesNotificationContainer container} with the parsed information.
   */
  private TypesNotificationContainer parseBaseTypesNotification(final PGNotification notification) {

    final Matcher matcher = BASE_TYPES_NOTIFICATION_PATTERN.matcher(notification.getParameter());

    if (!matcher.find()) {
      throw new IllegalStateException("Error: compiled pattern: " + BASE_TYPE_NOTIFICATION_REGEX
          + " did not match notification: " + notification.getParameter());
    }

    final String databaseOperation = matcher.group(2);
    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final Integer baseTypeId = Integer.parseInt(matcher.group(4));

    return new TypesNotificationContainer(databaseOperation, moduleId, Optional.of(baseTypeId),
        Optional.<Integer>absent(), Optional.<BigInteger>absent(), Optional.<Integer>absent(),
        Optional.<Integer>absent());
  }

  /**
   * Parses expression type aka type substitution {@link PGNotification notifications} for
   * synchronizing the state of {@link TypeSubstitution type substitutions} between multiple
   * instances of BinNavi.
   * 
   * @param notification The {@link PGNotification} from the database.
   * @return A {@link TypesNotificationContainer container} with the parsed information.
   */
  private TypesNotificationContainer parseExpressionTypesNotification(
      final PGNotification notification) {

    final Matcher matcher =
        EXPRESSION_TYPES_NOTIFICATION_PATTERN.matcher(notification.getParameter());

    if (!matcher.find()) {
      throw new IllegalStateException("Error: compliled pattern "
          + EXPRESSION_TYPES_NOTIFICATION_PATTERN.toString() + " did not match notification: "
          + notification.getParameter());
    }

    final String databaseOperation = matcher.group(2);
    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final BigInteger address = new BigInteger(matcher.group(4));
    final Integer position = Integer.parseInt(matcher.group(5));
    final Integer expressionId = Integer.parseInt(matcher.group(6));

    return new TypesNotificationContainer(databaseOperation, moduleId, Optional.<Integer>absent(),
        Optional.<Integer>absent(), Optional.of(address), Optional.of(position),
        Optional.of(expressionId));
  }

  /**
   * Parses type {@link PGNotification notifications} for synchronizing the state of
   * {@link TypeMember type members} between multiple instances of BinNavi.
   * 
   * @param notification The {@link PGNotification} from the database.
   * @return A {@link TypesNotificationContainer container} with the parsed information.
   */
  private TypesNotificationContainer parseTypesNotification(final PGNotification notification) {

    final Matcher matcher = TYPES_NOTIFICATION_PATTERN.matcher(notification.getParameter());

    if (!matcher.find()) {
      throw new IllegalStateException("Error: compiled pattern: " + TYPES_NOTIFICATION_REGEX
          + " did not match notification: " + notification.getParameter());
    }

    final String databaseOperation = matcher.group(2);
    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final Integer typeMemberId = Integer.parseInt(matcher.group(4));

    return new TypesNotificationContainer(databaseOperation, moduleId, Optional.<Integer>absent(),
        Optional.of(typeMemberId), Optional.<BigInteger>absent(), Optional.<Integer>absent(),
        Optional.<Integer>absent());

  }

  @Override
  public void inform(final Collection<TypesNotificationContainer> containers,
      final SQLProvider provider) throws CouldntLoadDataException {

    Preconditions.checkNotNull(containers, "Error: containers argument can not be null");
    Preconditions.checkNotNull(provider, "Error: provider argument can not be null");

    for (final TypesNotificationContainer container : containers) {
      if (!provider.findModule(container.getModuleId()).isLoaded()) {
        continue; // we do not need to look at notification for modules not loaded.
      }
      if (container.getAddress().isPresent()) {
        informExpressionTypesNotification(container, provider);
      } else if (container.getTypeId().isPresent()) {
        informTypesNotification(container, provider);
      } else if (container.getBaseTypeId().isPresent()) {
        informBaseTypesNotification(container, provider);
      } else {
        throw new IllegalStateException(
            "Error: the parsed notification does not contain a distinct element.");
      }
    }
  }

  @Override
  public Collection<TypesNotificationContainer> parse(
      final Collection<PGNotification> notifications, final SQLProvider provider) {

    Preconditions.checkNotNull(notifications, "Error: notifications argument can not be null");
    Preconditions.checkNotNull(provider, "Error: provider argument can not be null");

    final Collection<TypesNotificationContainer> containers = Lists.newArrayList();

    for (final PGNotification notification : notifications) {
      if (notification.getParameter().startsWith(CTableNames.EXPRESSION_TYPES_TABLE)) {
        containers.add(parseExpressionTypesNotification(notification));
      } else if (notification.getParameter().startsWith(CTableNames.TYPE_MEMBERS_TABLE)) {
        containers.add(parseTypesNotification(notification));
      } else if (notification.getParameter().startsWith(CTableNames.BASE_TYPES_TABLE)) {
        containers.add(parseBaseTypesNotification(notification));
      } else {
        throw new IllegalStateException("Error: Table name supplied in notification "
            + notification.getParameter()
            + " does not match tables where type notifications are expected on.");
      }
    }
    return containers;
  }
}
