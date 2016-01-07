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
package com.google.security.zynamics.binnavi.config;

import com.google.security.zynamics.common.config.AbstractConfigItem;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

public class PluginConfigItem extends AbstractConfigItem {
  private static final String NAME = "Name";
  private static final String NAME_DEFAULT = "";
  private String name = NAME_DEFAULT;

  private static final String GUID = "GUID";
  private static final long GUID_DEFAULT = -1;
  private long guid = GUID_DEFAULT;

  private static final String LOAD = "Load";
  private static final boolean LOAD_DEFAULT = true;
  private boolean load = LOAD_DEFAULT;

  @Override
  public void load(final TypedPropertiesWrapper properties) {
    name = properties.getString(NAME, NAME_DEFAULT);
    guid = properties.getLong(GUID, GUID_DEFAULT);
    load = properties.getBoolean(LOAD, LOAD_DEFAULT);
  }

  @Override
  public void store(final TypedPropertiesWrapper properties) {
    properties.setString(NAME, name);
    properties.setLong(GUID, guid);
    properties.setBoolean(LOAD, load);
  }

  public String getName() {
    return name;
  }

  public void setName(final String value) {
    this.name = value;
  }

  public long getGUID() {
    return guid;
  }

  public void setGUID(final long value) {
    this.guid = value;
  }

  public boolean isLoad() {
    return load;
  }

  public void setLoad(final boolean value) {
    this.load = value;
  }
}
