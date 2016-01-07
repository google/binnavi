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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

import java.math.BigInteger;

/**
 * A {@link TypeInstance} is an instance of a base type at some address in memory.
 */
public final class TypeInstance {
  /**
   * The id of the {@link TypeInstance}.
   */
  private final int id;

  /**
   * The name of the {@link TypeInstance}.
   */
  private String name;

  /**
   * The {@link BaseType} of the {@link TypeInstance}.
   */
  private final BaseType baseType;

  /**
   * The {@link Section} where the {@link TypeInstance} is located in.
   */
  final Section section;

  /**
   * The {@link INaviModule} where the {@link TypeInstance} is located in.
   */
  private final INaviModule module;

  /**
   * The {@link TypeInstanceAddress} of the {@link TypeInstance}.
   */
  private final TypeInstanceAddress address;

  /**
   * Creates a new {@link TypeInstance}
   *
   * @param id The unique id the the {@link TypeInstance}.
   * @param name The name {@link String} of the {@link TypeInstance}.
   * @param baseType The {@link BaseType} on which the {@link TypeInstance} is based.
   * @param section The {@link Section} where the {@link TypeInstance} is located in.
   * @param sectionOffset The offset within the {@link Section} where the {@link TypeInstance} is
   *        located.
   * @param module The {@link INaviModule} where the {@link TypeInstance} belongs to.
   */
  public TypeInstance(final int id, final String name, final BaseType baseType,
      final Section section, final long sectionOffset, final INaviModule module) {
    Preconditions.checkArgument(id >= 0, "Error: id argument must be greater or equal to zero");
    this.id = id;
    // TODO (timkornau) we do currently not know if we want to accept empty strings here as well.
    // Preconditions.checkArgument(name == null || !name.isEmpty(),
    // "Error: name can either be null or a non empty string");
    this.name = name;
    this.baseType =
        Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null");
    this.section = Preconditions.checkNotNull(section, "Error: section argument can not be null");
    Preconditions.checkArgument(
        sectionOffset >= 0, "Error: section offset must be greater or equal to zero and ");
    Preconditions.checkArgument(
        section.getStartAddress()
            .toBigInteger()
            .add(BigInteger.valueOf(sectionOffset))
            .compareTo(section.getEndAddress().toBigInteger()) <= 0,
        "Error: the offset of the type instance is not within section boundaries.");
    this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
    address = new TypeInstanceAddress(section.getStartAddress(), sectionOffset);
  }

  /**
   * Sets the name of the {@link TypeInstance}.
   *
   * @param name the new name of the {@link TypeInstance}.
   */
  void setName(final String name) {
    this.name = Preconditions.checkNotNull(name, "Error: name argument can not be null");
  }

  public TypeInstanceAddress getAddress() {
    return address;
  }

  /**
   * Returns the {@link BaseType} of this {@link TypeInstance}.
   *
   * @return the {@link BaseType} of this {@link TypeInstance}.
   */
  public BaseType getBaseType() {
    return baseType;
  }

  /**
   * Returns the id of the {@link TypeInstance}.
   *
   * @return the id of the {@link TypeInstance}.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the {@link INaviModule} where the {@link TypeInstance} is located in.
   *
   * @return the {@link INaviModule} where the {@link TypeInstance} is located in.
   */
  public INaviModule getModule() {
    return module;
  }

  /**
   * Returns the name of the {@link TypeInstance}.
   *
   * @return the name of the {@link TypeInstance}.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the {@link Section} where the {@link TypeInstance} is located in.
   *
   * @return the {@link Section} where the {@link TypeInstance} is located in.
   */
  public Section getSection() {
    return section;
  }
}
