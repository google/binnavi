/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.CInliningHelper;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CInliningResult;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CReferenceFinder;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;

/**
 * Contains functions for inlining functions into graphs.
 */
public final class CGraphInliner {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphInliner() {
  }

  private static void exceptionDialog(final JFrame parent, final INaviFunction function,
      final CouldntLoadDataException e) {
    CUtilityFunctions.logException(e);

    final String innerMessage = "E00117: " + "Function could not be inlined";
    final String innerDescription =
        CUtilityFunctions.createDescription(String.format(
            "The function '%s' could not be inlined because it could not be loaded.",
            function.getName()),
            new String[] {"There was a problem with the database connection."},
            new String[] {"The graph remains unchanged because the function was not inlined."});

    NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
  }

  /**
   * Determines the function to inline with consideration of function forwarding.
   * 
   * @param parent Parent window used for dialogs.
   * @param viewContainer Contains the function to inline.
   * @param function The function to be inlined.
   * @param forwarderModuleId Module ID of the module the function is forwarded to.
   * @param forwarderAddress Address of the function the function is forwarded to. This argument can
   *        be null.
   * 
   * @return The function to be inlined.
   */
  private static INaviFunction getFunctionToInline(final JFrame parent,
      final IViewContainer viewContainer, final INaviFunction function,
      final int forwarderModuleId, final IAddress forwarderAddress) {
    if (forwarderAddress == null) {
      return function;
    } else {
      final IDatabase database = viewContainer.getDatabase();

      final INaviModule module = database.getContent().getModule(forwarderModuleId);

      if (!viewContainer.containsModule(module)) {
        CMessageBox.showInformation(parent, String
            .format("You are trying to inline an external function into a module view. "
                + "This is not possible.\n"
                + "To inline external functions it is necessary to create projects."));

        return null;
      }

      if (!module.isLoaded()) {
        try {
          module.load();
        } catch (final CouldntLoadDataException | LoadCancelledException e) {
          CUtilityFunctions.logException(e);
          return null;
        }
      }

      return module.getContent().getFunctionContainer().getFunction(forwarderAddress);
    }
  }

  /**
   * Inlines a node without prompting the user for anything.
   * 
   * @param parent Parent window used for dialogs.
   * @param viewContainer Contains the functions to be inlined.
   * @param graph Graph where the inline operation takes place.
   * @param node Node where the inlining operation takes place.
   * @param instruction Function call instruction to be inlined.
   * @param function Function to be inlined.
   * 
   * @return Result of the inlining operation or null if an error occurred.
   */
  private static CInliningResult inlineFunctionSilently(final JFrame parent,
      final IViewContainer viewContainer, final ZyGraph graph, final INaviCodeNode node,
      final INaviInstruction instruction, final INaviFunction function) {
    final INaviFunction inlineFunction =
        prepareFunctionInlining(parent, node, instruction, function, viewContainer);

    if (inlineFunction == null) {
      return null;
    } else if (inlineFunction.getBasicBlockCount() == 0) {
      return null;
    } else {
      try {
        if (!inlineFunction.isLoaded()) {
          inlineFunction.load();
        }

        return CInliningHelper
            .inlineCodeNode(graph.getRawView(), node, instruction, inlineFunction);
      } catch (final CouldntLoadDataException e) {
        exceptionDialog(parent, inlineFunction, e);
      }
    }
    return null;
  }

  /**
   * Prepares the function inlining by checking if the arguments are properly set and locating the
   * function to inline.
   * 
   * @param parent The parent JFrame.
   * @param node The node where the inlining will take place.
   * @param instruction The instruction which is the function call.
   * @param function
   * @param viewContainer The view Container.
   * 
   * @return The function to be inlined.
   */
  private static INaviFunction prepareFunctionInlining(final JFrame parent,
      final INaviCodeNode node, final INaviInstruction instruction, final INaviFunction function,
      final IViewContainer viewContainer) {
    Preconditions.checkNotNull(parent, "IE00825: Parent argument can not be null");
    Preconditions.checkNotNull(viewContainer, "IE00915: View container argument can not be null");
    Preconditions.checkNotNull(node, "IE00916: Node argument can't be null");
    Preconditions.checkNotNull(instruction, "IE01153: Instruction argument can't be null");
    Preconditions.checkNotNull(function, "IE01173: Function argument can't be null");

    final int forwarderModuleId = function.getForwardedFunctionModuleId();
    final IAddress forwarderAddress = function.getForwardedFunctionAddress();

    return getFunctionToInline(parent, viewContainer, function, forwarderModuleId, forwarderAddress);
  }

  /**
   * Inlines all function calls of a given graph.
   * 
   * @param parent Parent window used for dialogs.
   * @param container Contains the functions to be inlined.
   * @param graph Graph where the inline operation takes place.
   */
  public static void inlineAll(final JFrame parent, final IViewContainer container,
      final ZyGraph graph) {
    Preconditions.checkNotNull(parent, "IE02285: Parent argument can not be null");
    Preconditions.checkNotNull(container, "IE02286: Container argument can not be null");
    Preconditions.checkNotNull(graph, "IE02287: Graph Argument can not be null");

    final MutableDirectedGraph<INaviViewNode, INaviEdge> mutableGraph =
        (MutableDirectedGraph<INaviViewNode, INaviEdge>) graph.getRawView().getGraph();
    final List<INaviViewNode> nodes = mutableGraph.getNodes();

    final HashMap<INaviInstruction, INaviFunction> instructionToFunctionMap =
        new HashMap<INaviInstruction, INaviFunction>();

    for (final INaviViewNode iNaviViewNode : nodes) {
      if (iNaviViewNode instanceof INaviCodeNode) {
        instructionToFunctionMap.putAll(CReferenceFinder
            .getCodeReferenceMap((INaviCodeNode) iNaviViewNode));
      }
    }

    for (final INaviInstruction iNaviInstruction : instructionToFunctionMap.keySet()) {
      INaviCodeNode updatedNode = null;
      for (final INaviViewNode iNaviViewNode2 : graph.getRawView().getGraph().getNodes()) {
        final INaviCodeNode codeNode = (INaviCodeNode) iNaviViewNode2;
        if (codeNode.hasInstruction(iNaviInstruction)) {
          updatedNode = codeNode;
        }
      }
      if (updatedNode != null) {
        inlineFunctionSilently(parent, container, graph, updatedNode, iNaviInstruction,
            instructionToFunctionMap.get(iNaviInstruction));
      } else {
        throw new IllegalStateException(
            "IE01174: Graph final has been rendered final to an final inconsitant state");
      }
    }
  }

  /**
   * Inserts the nodes of a given function after a given instruction in a given block. The original
   * block is split if necessary.
   * 
   * @param parent Parent window that is used to display error messages.
   * @param viewContainer Container that contains the view.
   * @param graph Graph where the inlining operation happens.
   * @param node The node that contains the call instruction.
   * @param instruction The call instruction where the inlining happens.
   * @param function The function to inline.
   */
  public static void inlineFunction(final JFrame parent, final IViewContainer viewContainer,
      final ZyGraph graph, final INaviCodeNode node, final INaviInstruction instruction,
      final INaviFunction function) {
    final INaviFunction inlineFunction =
        prepareFunctionInlining(parent, node, instruction, function, viewContainer);

    if (inlineFunction == null) {
      CMessageBox.showError(parent,
          "Could not inline the function because it is forwarded to an unknown function.");
    } else if (inlineFunction.getBasicBlockCount() == 0) {
      CMessageBox.showError(parent, "Could not inline the function because it has 0 basic blocks.");
    } else {
      try {
        if (!inlineFunction.isLoaded()) {
          inlineFunction.load();
        }

        CInliningHelper.inlineCodeNode(graph.getRawView(), node, instruction, inlineFunction);

        if (graph.getSettings().getLayoutSettings().getAutomaticLayouting()) {
          CGraphLayouter.refreshLayout(parent, graph);
        }
      } catch (final CouldntLoadDataException e) {
        exceptionDialog(parent, inlineFunction, e);
      }
    }
  }

  /**
   * Replaces a function node with the basic blocks of a function.
   * 
   * @param parent Parent window that is used to display error messages.
   * @param graph Graph where the inlining operation happens.
   * @param node The function node that is replaced by the basic blocks of the corresponding
   *        function.
   */
  public static void inlineFunction(final JFrame parent, final ZyGraph graph,
      final INaviFunctionNode node) {
    Preconditions.checkNotNull(parent, "IE01743: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01744: Graph argument can not be null");
    Preconditions.checkNotNull(node, "IE01745: Node argument can not be null");

    final INaviView view = graph.getRawView();

    final INaviFunction function = node.getFunction();

    try {
      if (!function.isLoaded()) {
        function.load();
      }

      CInliningHelper.inlineFunctionNode(view, node);

      if (graph.getSettings().getLayoutSettings().getAutomaticLayouting()) {
        CGraphLayouter.refreshLayout(parent, graph);
      }
    } catch (final CouldntLoadDataException e) {
      exceptionDialog(parent, function, e);
    }
  }
}
