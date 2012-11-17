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
		def createCommandConsole(presentationComponent, browser):
			return CommandConsole( browser, self.__world.browserContext, presentationComponent )

		self.__createCommandConsole = createCommandConsole

		self.__world = world
		
		self.__appState = world.rootSubject.getFocus()

		self.__rootWindow = Window( self, createCommandConsole, location )
		self.__rootWindow.onCloseRequestListener = self.__onWindowCloseRequest
		self.__openWindows = { self.__rootWindow }
		
		self.onCloseLastWindow = None
		

		
	@property
	def world(self):
		return self.__world


	@property
	def browserContext(self):
		return self.__world.browserContext


	def showRootWindow(self):
		self.__rootWindow.show()

		

	def closeAllWindows(self):
		for window in self.__openWindows:
			window.close()
		self.__openWindows = set()

		self.__windowClosed()

		
	def _createNewWindow(self, location):
		newWindow = Window( self, self.__createCommandConsole, location )
		newWindow.onCloseRequestListener = self.__onWindowCloseRequest
		newWindow.show()
		self.__openWindows.add( newWindow )


	def __onWindowCloseRequest(self, window):
		if len( self.__openWindows ) == 1:
			# Only one window open

			# Invoke the application state's onCloseRequest method to determine if closing
			# is allowed
			try:
				onCloseRequestFn = self.__appState.onCloseRequest
			except AttributeError:
				pass
			else:
				if not onCloseRequestFn( self, window ):
					# Request denied: don't close
					return

		window.close()
		self.__openWindows.remove( window )

		self.__windowClosed()

	

	def __windowClosed(self):
		if len( self.__openWindows ) == 0:
			if self.onCloseLastWindow is not None:
				self.onCloseLastWindow( self )
			try:
				onCloseAppFn = self.__appState.onCloseApp
			except AttributeError:
				pass
			else:
				onCloseAppFn( self )

	


