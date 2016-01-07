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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionShowReilCodeNode;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CChangeFunctionNameAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.COpenOriginalFunction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CRemoveFromGroupAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.CMenuBuilder;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.CFunctionReplacement;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviReplacement;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CReferenceFinder;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CUnInliner;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

import javax.swing.JPopupMenu;

/**
 * The popup menu that is shown when the user right-clicks on a code node in a graph.
 *
 * @author jannewger (Jan Newger)
 *
 */
public final class CCodeNodeMenu extends JPopupMenu {

  /**
   * Creates a new code node menu.
   *
   * @param model The graph model that provides information about the graph.
   * @param node The node whose menu is created.
   * @param clickedObject The object that was clicked.
   * @param y The y-position in pixels relative to the top of the clicked node.
   * @param extensions The list of code node extensions that extend the menu.
   */
  public CCodeNodeMenu(final CGraphModel model, final NaviNode node, final Object clickedObject,
      final double y, final List<ICodeNodeExtension> extensions) {
    Preconditions.checkNotNull(model, "IE02368: model arguement can not be null");
    Preconditions.checkNotNull(extensions, "IE02369: extensions argument can not be null");
    Preconditions.checkNotNull(node, "IE00967: Node argument can't be null");

    final int line = node.positionToRow(y);
    final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();

    // If we are below the last instruction we must make sure that we have
    // no instruction selected, this serves as indicator what type of menu
    // is displayed.
    final INaviInstruction instruction =
        (line != -1) ? CCodeNodeHelpers.lineToInstruction(codeNode, line) : null;
    CFollowInDumpMenu.addFollowInDumpMenu(this, model, node, clickedObject, y);
    // We are above the first instruction of the node.
    if (line == 0) {
      addRenameFunctionMenu(codeNode, model);
    }

    if (clickedObject instanceof COperandTreeNode) {
      addOperandTreeNodeMenu(model, (COperandTreeNode) clickedObject, node, instruction,
          extensions);
    }

    CMenuBuilder.addCommentMenu(this, model, node.getRawNode());
    CMenuBuilder.addSelectionMenus(this, model.getGraph(), node);

    if (node.getRawNode().getParentGroup() != null) {
      add(new CRemoveFromGroupAction(node));
      addSeparator();
    }

    addOpenOriginalFunctionMenu(model, node);

    final boolean allowUninlining = canUninline((INaviCodeNode) node.getRawNode());
    final List<Pair<INaviInstruction, INaviFunction>> functions =
        CReferenceFinder.getCodeReferenceList((INaviCodeNode) node.getRawNode());
    add(new CSubFunctionMenu(model, functions, allowUninlining));
    add(new CInliningMenu(model, node, functions, allowUninlining));
    addSeparator();

    CMenuBuilder.addTaggingMenu(this, model, node);
    add(new CClipboardMenu(node, line));
    addSeparator();
    add(CActionProxy.proxy(
        new CActionShowReilCodeNode(model.getParent(), (INaviCodeNode) node.getRawNode())));
    if (instruction != null) {
      addSeparator();
      add(new CInstructionMenu(model, node, instruction, extensions));
    }
  }

  private static boolean canUninline(final INaviCodeNode node) {
    return CUnInliner.getInlinedNodes(node) != null;
  }

  private void addFunctionOperandMenu(final CGraphModel model, final INaviReplacement replacement) {
    final INaviFunction function = ((CFunctionReplacement) replacement).getFunction();
    final INaviView view = function.getModule().getContent().getViewContainer().getView(function);
    add(new CChangeFunctionNameAction(model.getParent(), view));
    addSeparator();
  }

  private void addImmediateOperandMenu(final COperandTreeNode node, final SectionContainer sections,
      final INaviModule module) {
    add(new CIntegerOperandMenu(node, node.getReplacement()));
    final long address = Long.parseLong(node.getValue());
    final List<Section> containingSections = sections.findSections(new CAddress(address));
    if (containingSections.size() == 1) {
      add(new GotoSectionAction(containingSections.get(0), address, module));
    } else if (containingSections.size() > 1) {
      add(new GotoSectionMenu(containingSections, address, module));
    }
    addSeparator();
  }

  private void addInstanceReferenceMenu(final CGraphModel model, final COperandTreeNode treeNode) {
    // We can possibly have more than one reference for a single tree node if we have multiple
    // instances of the same section.
    final INaviModule module = model.getViewContainer().getModules().get(0);
    final List<TypeInstanceReference> references = treeNode.getTypeInstanceReferences();
    add(new ShowTypeInstanceReferencesAction(model.getParent(), references, module));
    add(new GotoTypeInstanceAction(references.get(0).getTypeInstance()));
    add(new RenameTypeInstanceAction(model.getParent(),
        module.getContent().getTypeInstanceContainer(), references.get(0).getTypeInstance()));
  }

  private void addOpenOriginalFunctionMenu(final CGraphModel model, final NaviNode node) {
    final INaviCodeNode rawNode = (INaviCodeNode) node.getRawNode();
    try {
      final INaviFunction nodeFunction = rawNode.getParentFunction();
      final INaviFunction viewFunction =
          model.getViewContainer().getFunction(model.getGraph().getRawView());
      if (nodeFunction != viewFunction) {
        add(CActionProxy.proxy(
            new COpenOriginalFunction(model.getParent(), model.getViewContainer(), nodeFunction)));
      }
    } catch (final MaybeNullException e) {
      // If there is no original function then we can not open it.
    }
  }

  /**
   * Adds menus for the clicked operand.
   *
   * @param model The graph model that provides information about the graph.
   * @param treeNode The clicked operand node.
   * @param extensions The extension menu items for the "Operands" menu.
   * @param instruction The instruction that was clicked.
   * @param node The basic block that contains the clicked instruction.
   */
  private void addOperandTreeNodeMenu(final CGraphModel model, final COperandTreeNode treeNode,
      final NaviNode node, final INaviInstruction instruction,
      final List<ICodeNodeExtension> extensions) {
    final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();
    final INaviModule module = model.getViewContainer().getModules().get(0);
    // We only show the goto address if we have no associated type instance for the given immediate.
    if (treeNode.getType() == ExpressionType.IMMEDIATE_INTEGER
        && treeNode.getTypeInstanceReferences().isEmpty()) {
      addImmediateOperandMenu(treeNode, module.getContent().getSections(), module);
    }

    if (treeNode.getType() == ExpressionType.REGISTER) {
      addRegisterOperandMenu(model, treeNode, instruction, extensions, codeNode);
    }

    final INaviReplacement replacement = treeNode.getReplacement();
    // TODO(jannewger): until we have function pointers in place, the function replacement takes
    // precedence over type instances.
    if (!treeNode.getTypeInstanceReferences().isEmpty()
        && !(replacement instanceof CFunctionReplacement)) {
      addInstanceReferenceMenu(model, treeNode);
    }

    if (replacement instanceof CFunctionReplacement) {
      addFunctionOperandMenu(model, replacement);
    }
  }

  private static BaseType getStackFrame(CGraphModel model) {
    // TODO(jannewger): This code fails for inlining and needs fixing. The current null check is
    // only a work around and needs to be addressed. b/11568317
    if (model.getViewContainer().getFunction(model.getGraph().getRawView()) != null) {
      return model.getViewContainer().getFunction(model.getGraph().getRawView()).getStackFrame();
    }
    return null;
  }

  private void addRegisterOperandMenu(final CGraphModel model, final COperandTreeNode treeNode,
      final INaviInstruction instruction, final List<ICodeNodeExtension> extensions,
      final INaviCodeNode codeNode) {
    try {
      add(new COperandsMenu(codeNode, instruction, extensions));
    } catch (final InternalTranslationException | MaybeNullException exception) {
      CUtilityFunctions.logException(exception);
    }
    final TypeManager typeManager = model.getViewContainer().getModules().get(0).getTypeManager();
    if (treeNode.getTypeSubstitution() == null) {
      add(TypeSubstitutionAction.instantiateCreateTypeSubstitution(model.getParent(), typeManager,
          getStackFrame(model), treeNode));
    } else {
      add(new DeleteTypeSubstitutionMenuAction(typeManager, treeNode));
      add(TypeSubstitutionAction.instantiateEditTypeSubstitution(model.getParent(), typeManager,
          getStackFrame(model), treeNode));
    }
    addSeparator();
  }

  private void addRenameFunctionMenu(final INaviCodeNode codeNode, final CGraphModel model) {
    try {
      final INaviFunction function = codeNode.getParentFunction();
      final INaviView view = function.getModule().getContent().getViewContainer().getView(function);
      add(new CChangeFunctionNameAction(model.getParent(), view));
    } catch (final MaybeNullException e) {
      // no parent function no menu entry we are ok with this.
    }
  }
}
