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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Generates a consistent set of raw base types and raw members for testing, that represent atomic,
 * pointer, array and (nested) structure types.
 *
 * <pre>
 *
 *int
 *
 *unsigned int
 *
 *unsigned int*
 *
 *unsigned int[10]
 *
 *struct SimpleStruct {
 * int ss_int_member;
 * unsigned int ss_uint_member;
 * unsigned int ss_array_member[10];
 *}
 *
 *struct NestedStruct {
 * int ns_int_member;
 * SimpleStruct ns_simple_struct_member;
 *}
 *
 *struct DoubleNestedStruct {
 * NestedStruct dns_nested_struct_member;
 * int dns_int_member;
 * unsigned int* dns_pointer_member;
 *}
 *
 *union SimpleUnion {
 * unsigned int su_array_member[10];
 * int su_int_member;
 * unsigned int su_uint_member;
 *}
 *
 *union ComplexUnion {
 * DoubleNestedStruct cu_double_nested_struct_member;
 * int cu_int_member;
 * SimpleStruct cu_simple_struct_member;
 *}
 *
 *A function prototype:
 *void (void)
 *
 *</pre>
 *
 */
public final class RawTestTypeSystem {

  public final RawBaseType intType;
  public final RawBaseType uintType;
  public final RawBaseType uintPointerType;
  public final RawBaseType uintArrayType;
  public final RawBaseType simpleStruct;
  public final RawBaseType nestedStruct;
  public final RawBaseType doubleNestedStruct;
  public final RawBaseType simpleUnion;
  public final RawBaseType complexUnion;
  public final RawBaseType voidFunctionPrototype;

  private final List<RawTypeMember> rawMembers;
  private final List<RawBaseType> rawTypes;

  public RawTestTypeSystem() {
    int typeId = 0;
    intType = new RawBaseType(++typeId, "int", 32, null, true, BaseTypeCategory.ATOMIC);
    uintType = new RawBaseType(++typeId, "unsigned int", 32, null, false, BaseTypeCategory.ATOMIC);
    uintArrayType = new RawBaseType(
        ++typeId, "unsigned int[10]", intType.getSize() * 10, null, false, BaseTypeCategory.ARRAY);
    final RawTypeMember uintArrayMember =
        new RawTypeMember(++typeId, "", uintType.getId(), uintArrayType.getId(), null, null, 10);
    uintPointerType = new RawBaseType(
        ++typeId, "unsigned int*", 32, uintType.getId(), false, BaseTypeCategory.POINTER);

    simpleStruct = new RawBaseType(
        ++typeId, "SimpleStruct", intType.getSize() + uintType.getSize() + uintArrayType.getSize(),
        null, false, BaseTypeCategory.STRUCT);
    final RawTypeMember ssIntMember = new RawTypeMember(
        ++typeId, "ss_int_member", intType.getId(), simpleStruct.getId(), 0, null, null);
    final RawTypeMember ssUintMember = new RawTypeMember(
        ++typeId, "ss_uint_member", uintType.getId(), simpleStruct.getId(), intType.getSize(), null,
        null);
    final RawTypeMember ssArrayMember = new RawTypeMember(
        ++typeId, "ss_array_member", uintArrayType.getId(), simpleStruct.getId(),
        intType.getSize() + uintType.getSize(), null, null);

    nestedStruct = new RawBaseType(
        ++typeId, "NestedStruct", intType.getSize() + simpleStruct.getSize(), null, false,
        BaseTypeCategory.STRUCT);
    final RawTypeMember nsIntMember = new RawTypeMember(
        ++typeId, "ns_int_member", intType.getId(), nestedStruct.getId(), 0, null, null);
    final RawTypeMember nsSimpleMember = new RawTypeMember(
        ++typeId, "ns_simple_struct_member", simpleStruct.getId(), nestedStruct.getId(),
        intType.getSize(), null, null);

    doubleNestedStruct = new RawBaseType(++typeId, "DoubleNestedStruct",
        nestedStruct.getSize() + intType.getSize() + uintPointerType.getSize(), null, false,
        BaseTypeCategory.STRUCT);
    final RawTypeMember dnsNestedMember = new RawTypeMember(
        ++typeId, "dns_nested_struct_member", nestedStruct.getId(), doubleNestedStruct.getId(), 0,
        null, null);
    final RawTypeMember dnsIntMember = new RawTypeMember(
        ++typeId, "dns_int_member", intType.getId(), doubleNestedStruct.getId(),
        nestedStruct.getSize(), null, null);
    final RawTypeMember dnsPointerMember = new RawTypeMember(
        ++typeId, "dns_pointer_member", uintPointerType.getId(), doubleNestedStruct.getId(),
        nestedStruct.getSize() + intType.getSize(), null, null);

    // For unions, the order in which members are instantiated is important and must not be changed!
    // All members have the same offset, thus the type id determines the iteration order of members
    // and therefore the assignment to TypeMember instances (see TestTypeSystem constructor).
    simpleUnion = new RawBaseType(
        ++typeId, "SimpleUnion", uintArrayType.getSize(), null, false, BaseTypeCategory.UNION);
    final RawTypeMember suArrayMember = new RawTypeMember(
        ++typeId, "su_array_member", uintArrayType.getId(), simpleUnion.getId(), 0, null, null);
    final RawTypeMember suIntMember = new RawTypeMember(
        ++typeId, "su_int_member", intType.getId(), simpleUnion.getId(), 0, null, null);
    final RawTypeMember suUintMember = new RawTypeMember(
        ++typeId, "su_uint_member", uintType.getId(), simpleUnion.getId(), 0, null, null);

    complexUnion = new RawBaseType(
        ++typeId, "ComplexUnion", doubleNestedStruct.getSize(), null, false,
        BaseTypeCategory.UNION);
    final RawTypeMember cuDoubleNestedStructMember = new RawTypeMember(
        ++typeId, "cu_double_nested_struct_member", doubleNestedStruct.getId(),
        complexUnion.getId(), 0, null, null);
    final RawTypeMember cuIntMember = new RawTypeMember(
        ++typeId, "cu_int_member", intType.getId(), complexUnion.getId(), 0, null, null);
    final RawTypeMember cuSimpleStructMember = new RawTypeMember(
        ++typeId, "cu_simple_struct_member", simpleStruct.getId(), complexUnion.getId(), 0, null,
        null);

    voidFunctionPrototype =
        new RawBaseType(++typeId, "", 0, null, false, BaseTypeCategory.FUNCTION_PROTOTYPE);

    rawTypes = Lists.newArrayList(
        intType, uintType, uintArrayType, uintPointerType, simpleStruct, nestedStruct,
        doubleNestedStruct, simpleUnion, complexUnion, voidFunctionPrototype);
    rawMembers = Lists.newArrayList(uintArrayMember, ssIntMember, ssUintMember, ssArrayMember,
        nsIntMember, nsSimpleMember, dnsNestedMember, dnsIntMember, dnsPointerMember, suIntMember,
        suUintMember, suArrayMember, cuIntMember, cuSimpleStructMember, cuDoubleNestedStructMember);

  }

  public List<RawTypeMember> getRawMembers() {
    return rawMembers;
  }

  public List<RawBaseType> getRawTypes() {
    return rawTypes;
  }
}
