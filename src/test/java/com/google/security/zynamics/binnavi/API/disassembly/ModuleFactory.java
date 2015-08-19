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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.TagManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

public class ModuleFactory {
  public static Module get() {
    final MockSqlProvider provider = new MockSqlProvider();

    final Date creationDate = new Date();
    final Date modificationDate = new Date();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", creationDate, modificationDate,
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);

    final TagManager nodeTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.NODE_TAG, provider))), TagType.NODE_TAG, provider));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database db = new Database(new MockDatabase());

    return new Module(db, internalModule, nodeTagManager, viewTagManager);
  }

  public static Module get(final INaviModule module) {
    final MockSqlProvider provider = new MockSqlProvider();

    final TagManager nodeTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.NODE_TAG, provider))), TagType.NODE_TAG, provider));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database db = new Database(new MockDatabase());

    return new Module(db, module, nodeTagManager, viewTagManager);
  }

  public static Module get(final INaviModule module, final SQLProvider provider) {
    final TagManager nodeTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.NODE_TAG, provider))), TagType.NODE_TAG, provider));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database db = new Database(new MockDatabase());

    return new Module(db, module, nodeTagManager, viewTagManager);
  }
}
