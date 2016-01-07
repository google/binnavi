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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog;

/**
 * The basic idea behind the Select by Criteria dialog is to have a dialog that allows you to select
 * the nodes of a graph given a atomic criteria which you can connect using boolean expressions.
 * This allows you to create arbitrarily complex formulas for node selection.
 *
 *  The classes in this package are roughly split into the following logical components:
 *
 *  - There is the GUI code that drives the visible tree where you can create you formula. You can
 * find this code in the ExpressionTree package.
 *
 *  - There is model code that represents the formula behind the created expression. This model code
 * is basically a copy of the GUI tree, just optimized for evaluating the formula. You can find this
 * code in ExpressionModel.
 *
 *  - The individual conditions which can be used in the criterium formulas can be found in the
 * Conditions package. Each condition has a subpackage where all the relevant code is stored.
 *
 *  The general workflow (formula creation and evaluation) of the Select by Criteria dialog goes
 * like this:
 *
 *  - The dialog figures out what criteria are even available (CCriteriaFactory) - The GUI tree is
 * built (JCriteriumTree) - The user creates his formula - The formula is evaluated
 * (CCriteriumExecuter)
 *
 *  After a criterium tree was executed by a the criterium executer, a cached copy of it is
 * generated and stored for later reuse (CCachedExpressionTree).
 *
 *  There is an unclear separation between GUI code and model code in the individual conditions. The
 * source of this confusion is that each individual criterium implements both ICriterium (GUI) and
 * IAbstractCriterium (Model). That's a bit uncool but not problematic enough that it needs to be
 * changed.
 *
 *  The best way to extend the dialog using new criteria is to copy/paste the code existing
 * condition code and change that. This should be really simple.
 */
