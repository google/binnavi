/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

/**
 * Holds the type system which can be used in various testing scenarios (e.g. to test other
 * components that need instances of BaseType and TypeMember). See {@link RawTestTypeSystem raw test
 * type system} for provided types.
 *
 */
public class TestTypeSystem {

  public final BaseType intType;
  public final BaseType uintType;
  public final BaseType uintPointerType;
  public final BaseType uintArrayType;
  public final TypeMember uintArrayTypeMember;
  public final BaseType simpleStruct;
  public final BaseType nestedStruct;
  public final BaseType doubleNestedStruct;
  public final BaseType simpleUnion;
  public final BaseType complexUnion;
  public final BaseType voidFunctionPrototype;

  public final TypeMember ssIntMember;
  public final TypeMember ssUintMember;
  public final TypeMember ssArrayMember;
  public final TypeMember nsIntMember;
  public final TypeMember nsSimpleStructMember;
  public final TypeMember dnsNestedStructMember;
  public final TypeMember dnsIntMember;
  public final TypeMember dnsPointerMember;
  public final TypeMember suIntMember;
  public final TypeMember suUintMember;
  public final TypeMember suArraymember;
  public final TypeMember cuIntMember;
  public final TypeMember cuSimpleStructMember;
  public final TypeMember cuDoubleNestedStructMember;

  public TestTypeSystem(final TypeManager typeManager) {
    final RawTestTypeSystem rawTypeSystem = new RawTestTypeSystem();
    intType = typeManager.getBaseType(rawTypeSystem.intType.getId());
    uintType = typeManager.getBaseType(rawTypeSystem.uintType.getId());
    uintPointerType = typeManager.getBaseType(rawTypeSystem.uintPointerType.getId());
    uintArrayType = typeManager.getBaseType(rawTypeSystem.uintArrayType.getId());
    uintArrayTypeMember = uintArrayType.getLastMember();
    simpleStruct = typeManager.getBaseType(rawTypeSystem.simpleStruct.getId());
    nestedStruct = typeManager.getBaseType(rawTypeSystem.nestedStruct.getId());
    doubleNestedStruct = typeManager.getBaseType(rawTypeSystem.doubleNestedStruct.getId());
    simpleUnion = typeManager.getBaseType(rawTypeSystem.simpleUnion.getId());
    complexUnion = typeManager.getBaseType(rawTypeSystem.complexUnion.getId());
    voidFunctionPrototype = typeManager.getBaseType(rawTypeSystem.voidFunctionPrototype.getId());

    final Iterator<TypeMember> simpleMembers = simpleStruct.iterator();
    ssIntMember = simpleMembers.next();
    ssUintMember = simpleMembers.next();
    ssArrayMember = simpleMembers.next();
    final Iterator<TypeMember> nestedMembers = nestedStruct.iterator();
    nsIntMember = nestedMembers.next();
    nsSimpleStructMember = nestedMembers.next();
    final Iterator<TypeMember> doubleNestedMembers = doubleNestedStruct.iterator();
    dnsNestedStructMember = doubleNestedMembers.next();
    dnsIntMember = doubleNestedMembers.next();
    dnsPointerMember = doubleNestedMembers.next();
    final Iterator<TypeMember> simpleUnionMembers = simpleUnion.iterator();
    suArraymember = simpleUnionMembers.next();
    suIntMember = simpleUnionMembers.next();
    suUintMember = simpleUnionMembers.next();
    final Iterator<TypeMember> complexUnionMembers = complexUnion.iterator();
    cuDoubleNestedStructMember = complexUnionMembers.next();
    cuIntMember = complexUnionMembers.next();
    cuSimpleStructMember = complexUnionMembers.next();
  }

  public ImmutableList<BaseType> getTypes() {
    return ImmutableList.<BaseType>of(simpleStruct,
        nestedStruct,
        doubleNestedStruct,
        simpleUnion,
        complexUnion,
        intType,
        uintType,
        uintPointerType,
        uintArrayType);
  }

  public ImmutableList<TypeMember> getTypeMembers() {
    return ImmutableList.<TypeMember>of(uintArrayTypeMember,
        ssIntMember,
        ssUintMember,
        ssArrayMember,
        nsIntMember,
        nsSimpleStructMember,
        dnsNestedStructMember,
        dnsIntMember,
        dnsPointerMember,
        suIntMember,
        suUintMember,
        suArraymember,
        cuIntMember,
        cuSimpleStructMember,
        cuDoubleNestedStructMember);
  }
}
