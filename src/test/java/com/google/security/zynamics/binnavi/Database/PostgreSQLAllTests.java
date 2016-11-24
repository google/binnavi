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
package com.google.security.zynamics.binnavi.Database;

import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLEmptyDatabaseTest;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLProviderTest;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLProviderTestDestroy;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLProviderTestSetup;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLProviderTestTeardown;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLSectionFunctionsTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLTypeInstanceFunctionsTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLVerifyTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLNotificationProviderTest;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLFunctionCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLFunctionNodeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLGlobalCodeNodeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLGlobalEdgeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLGlobalInstructionCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLGroupNodeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLLocalCodeNodeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLLocalEdgeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLLocalInstructionCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLSectionCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLTextNodeCommentTests;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests.PostgreSQLTypeInstancesCommentTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({PostgreSQLVerifyTests.class, PostgreSQLEmptyDatabaseTest.class,
/* PostgreSQLUpdateDatabaseTest.class no update possible currently */
    PostgreSQLProviderTestSetup.class,
    PostgreSQLProviderTest.class,
    PostgreSQLLocalInstructionCommentTests.class,
    PostgreSQLLocalCodeNodeCommentTests.class,
    PostgreSQLLocalEdgeCommentTests.class,
    PostgreSQLFunctionNodeCommentTests.class,
    PostgreSQLFunctionCommentTests.class,
    PostgreSQLGlobalCodeNodeCommentTests.class,
    PostgreSQLGlobalEdgeCommentTests.class,
    PostgreSQLGlobalInstructionCommentTests.class,
    PostgreSQLGroupNodeCommentTests.class,
    PostgreSQLTextNodeCommentTests.class,
    PostgreSQLSectionCommentTests.class,
    PostgreSQLTypeInstancesCommentTests.class,
    PostgreSQLTypeInstanceFunctionsTests.class,
    PostgreSQLSectionFunctionsTests.class,
    PostgreSQLProviderTestTeardown.class,
    PostgreSQLUserFunctionsTests.class,
    PostgreSQLNotificationProviderTest.class,
    PostgreSQLProviderTestDestroy.class})
public class PostgreSQLAllTests {

}
