"""
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
"""
# This BinNavi plugin which highlights loops.

import sys

from java.lang import Thread
from javax.swing import JMenuItem as JMenuItem
from javax.swing import AbstractAction as AbstractAction

from com.google.security.zynamics.binnavi.API.disassembly import CouldntSaveDataException as CouldntSaveDataException
from com.google.security.zynamics.binnavi.API.disassembly import EdgeType as EdgeType
from com.google.security.zynamics.binnavi.API.helpers import MessageBox as MessageBox
from com.google.security.zynamics.binnavi.API.helpers import GraphAlgorithms as GraphAlgorithms
from com.google.security.zynamics.binnavi.API.plugins import IGraphMenuPlugin as IGraphMenuPlugin
from com.google.security.zynamics.binnavi.API.helpers import Logger as Logger
from com.google.security.zynamics.binnavi.API.helpers import TreeAlgorithms as TreeAlgorithms

import java.awt.Color

def highlight_loops(view):
        """Takes a view, calculates its dominator tree, and highlights its loops."""
        
        if len(view.graph.nodes) == 0:
                MessageBox.showError(None, "Can not create dominator tree of empty views")
                return

        nodeListArray = GraphAlgorithms.getGraphLoops(view.graph)

        for nodeList in nodeListArray:
            for node2 in nodeList:
                c = node2.getColor()
                node2.setColor( java.awt.Color(c.getRed()-20, c.getGreen(), c.getBlue()))
                c2 = node2.getBorderColor()
                node2.setBorderColor( java.awt.Color( 255, 0, 0 )) #c2.getRed()+20, c2.getGreen()+20, c2.getBlue()))

class MessageAction(AbstractAction):

        def __init__(self, pi, frame):
                AbstractAction.__init__(self, "Highlight loops in view")
                self.pi = pi
                self.frame = frame

        def actionPerformed(self, e):
                highlight_loops(self.frame.view2D.view)
                

class LoopHighlightPlugin(IGraphMenuPlugin):
        def getName(self):
                return "Loop Highlight"
        
        def getGuid(self):
                return 945436890433
                
        def getDescription(self):
                return "Highlights and groups loops in a view"
                
        def init(self, pi):
                self.pi = pi
                
        def closed(self, frame):
                pass
                
        def unload(self):
                pass
        
        def extendPluginMenu(self, frame):
                return [ JMenuItem(MessageAction(self.pi, frame)) ]

class WorkaroundThread(Thread):
        def __init__(self, pi, window, view):
                self.pi = pi
                self.window = window
                self.view = view
                
        def run(self):
                new_view2d = self.pi.showInWindow(self.window, self.view)
                new_view2d.doHierarchicalLayout()
                
loopHighLighter = LoopHighlightPlugin()
navi.getPluginRegistry().addPlugin(loopHighLighter)
