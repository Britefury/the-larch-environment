##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from javax.swing import JOptionPane
from BritefuryJ.AttributeTable import SimpleAttributeTable

from BritefuryJ.Command import CommandConsole

from BritefuryJ.Browser import Location

from Britefury.Windows.Window import Window

		
class WindowManager (object):
	def __init__(self, world, location=Location( '' )):
		self._world = world
		
		
		self._browserContext = world.getBrowserContext()

		def _createCommandConsole(presentationComponent, browser):
			return CommandConsole( browser, self.getBrowserContext(), presentationComponent )
		
		self._createCommandConsole = _createCommandConsole

		self._appState = world.getRootSubject().getFocus()

		self._rootWindow = Window( self, self._createCommandConsole, location )
		self._rootWindow.setCloseRequestListener( self._onWindowCloseRequest )
		self._openWindows = set( [ self._rootWindow ] )
		
		self.onCloseLastWindow = None
		

		
	def getWorld(self):
		return self._world
	
	
	def showRootWindow(self):
		self._rootWindow.show()

		
	def getBrowserContext(self):
		return self._world.getBrowserContext()
	
	
	def setCloseLastWindowListener(self, listener):
		self.onCloseLastWindow = listener



	def close(self):
		for window in self._openWindows:
			window.close()
		self._openWindows = set()

		self.__windowClosed()

		
	def _createNewWindow(self, location):
		newWindow = Window( self, self._createCommandConsole, location )
		newWindow.setCloseRequestListener( self._onWindowCloseRequest )
		newWindow.show()
		self._openWindows.add( newWindow )
		
		
	def _onWindowCloseRequest(self, window):
		if len( self._openWindows ) == 1:
			# Only one window open

			# Invoke the application state's onCloseRequest method to determine if closing
			# is allowed
			try:
				onCloseRequestFn = self._appState.onCloseRequest
			except AttributeError:
				pass
			else:
				if not onCloseRequestFn( self, window ):
					# Request denied: don't close
					return

		window.close()
		self._openWindows.remove( window )

		self.__windowClosed()

	

	def __windowClosed(self):
		if len( self._openWindows ) == 0:
			if self.onCloseLastWindow is not None:
				self.onCloseLastWindow( self )
			try:
				onCloseAppFn = self._appState.onCloseApp
			except AttributeError:
				pass
			else:
				onCloseAppFn( self )

	


