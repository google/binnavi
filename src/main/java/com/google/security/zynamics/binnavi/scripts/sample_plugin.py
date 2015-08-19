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
import sys

from javax.swing import JMenuItem as JMenuItem
from javax.swing import AbstractAction as AbstractAction

from com.google.security.zynamics.binnavi.API.plugins import IMainWindowMenuPlugin as IMainWindowMenuPlugin
from com.google.security.zynamics.binnavi.API.helpers import MessageBox as MessageBox
from com.google.security.zynamics.binnavi.API.gui import LogConsole as console

class MessageAction(AbstractAction):
	def __init__(self, pi):
		AbstractAction.__init__(self, "Python Sample Plugin")
		self.pi = pi

	def actionPerformed(self, e):
                console.log("Hello from the python sample script")
                MessageBox.showInformation(self.pi.mainWindow.frame, "Hello from the python sample script")

class SamplePlugin(IMainWindowMenuPlugin):
	def getName(self):
		return "Python Sample Plugin"
	
	def getGuid(self):
		return 545845490
		
	def getDescription(self):
		return "Displays a message box"
		
	def init(self, pi):
		self.pi = pi
		
	def unload(self):
		pass
	
	def extendPluginMenu(self):
		return [ JMenuItem(MessageAction(self.pi)) ]

sample = SamplePlugin()
navi.getPluginRegistry().addPlugin(sample)
