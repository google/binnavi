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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * Stores parsed information coming from notifications originating from bn_type_instances or from
 * bn_expression_type_instances.
 */
public class TypeInstancesNotificationContainer {

  /**
   * The {@link String database operation} of the notification.
   */
  private final String databaseOperation;

  /**
   * The {@link Integer module id} of the notified element.
   */
  private final Integer moduleId;

  /**
   * The {@link Integer type instance id} of the notified element.
   */
  private final Integer typeInstanceId;

  /**
   * The {@link Optional instruction address} of the notification. Present when the notification was
   * an expression type instance notification
   */
  private final Optional<BigInteger> address;

  /**
   * The {@link Optional operand tree position} of the notification. Present when the notification
   * was an expression type instance notification.
   */
  private final Optional<Integer> position;

  /**
   * The {@link Optional operand node expression id} of the notification. Present when the
   * notification was an expression type instance notification.
   */
  private final Optional<Integer> expressionId;

  /**
   * Creates a new {@link TypeInstancesNotificationContainer}.
   * 
   * @param databaseOperation {@link String} Contains the parsed database operation.
   * @param moduleId {@link Integer} The module id in the notification.
   * @param typeInstanceId {@link Integer} The id of the type instance which has changed.
   * @param address {@link Optional} The address of the instruction in the case of an expression
   *        type instance notification.
   * @param position {@link Optional} The operand position within the instruction in the case of an
   *        expression type instance notification.
   * @param expressionId {@link Optional} The expression id of the operand node within the operand
   *        tree in the case of an expression type instance notification.
   */
  public TypeInstancesNotificationContainer(final String databaseOperation, final Integer moduleId,
      final Integer typeInstanceId, final Optional<BigInteger> address,
      final Optional<Integer> position, final Optional<Integer> expressionId) {

    this.databaseOperation =
        Preconditions.checkNotNull(databaseOperation,
            "Error: databaseOperation argument can not be null.");
    this.moduleId =
        Preconditions.checkNotNull(moduleId, "Error: moduleId argument can not be null.");
    this.typeInstanceId =
        Preconditions
            .checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null.");
    this.address = Preconditions.checkNotNull(address, "Error: address argument can not be null.");
    this.position =
        Preconditions.checkNotNull(position, "Error: position argument can not be null.");
    this.expressionId =
        Preconditions.checkNotNull(expressionId, "Error: type argument can not be null.");
  }

  public String getDatabaseOperation() {
    return databaseOperation;
  }

  public Integer getModuleId() {
    return moduleId;
  }

  public Integer getTypeInstanceId() {
    return typeInstanceId;
  }

  public Optional<BigInteger> getAddress() {
    return address;
  }

  public Optional<Integer> getPosition() {
    return position;
  }

  public Optional<Integer> getExpressionId() {
    return expressionId;
  }
}
