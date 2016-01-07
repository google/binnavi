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

import java.math.BigInteger;

import com.google.common.base.Optional;

public class TypesNotificationContainer {

  private final String databaseOperation;
  private final int moduleId;
  private final Optional<Integer> baseTypeId;
  private final Optional<Integer> typeMemberId;
  private final Optional<BigInteger> typeSubstitutionAddress;
  private final Optional<Integer> typeSubstitutionPosition;
  private final Optional<Integer> typeSubstitutionExpressionId;

  public TypesNotificationContainer(final String databaseOperation, final int moduleId,
      final Optional<Integer> baseTypeId, final Optional<Integer> typeMemberId,
      final Optional<BigInteger> typeSubstitutionAddress,
      final Optional<Integer> typeSubstitutionPosition,
      final Optional<Integer> typeSubstitutionExpressionId) {
    this.databaseOperation = databaseOperation;
    this.moduleId = moduleId;
    this.baseTypeId = baseTypeId;
    this.typeMemberId = typeMemberId;
    this.typeSubstitutionAddress = typeSubstitutionAddress;
    this.typeSubstitutionPosition = typeSubstitutionPosition;
    this.typeSubstitutionExpressionId = typeSubstitutionExpressionId;
  }

  public String getDatabaseOperation() {
    return databaseOperation;
  }

  public int getModuleId() {
    return moduleId;
  }

  public Optional<BigInteger> getAddress() {
    return typeSubstitutionAddress;
  }

  public Optional<Integer> position() {
    return typeSubstitutionPosition;
  }

  public Optional<Integer> expressionId() {
    return typeSubstitutionExpressionId;
  }

  public Optional<Integer> getTypeId() {
    return typeMemberId;
  }

  public Optional<Integer> getBaseTypeId() {
    return baseTypeId;
  }
}
