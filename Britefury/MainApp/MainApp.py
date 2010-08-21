##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from javax.swing import JOptionPane
from BritefuryJ.AttributeTable import SimpleAttributeTable

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.GSym import GSymBrowserContext, GSymSubject

from Britefury.gSym.Presentation.GenericPresenterRegistry import genericPresenterRegistry
from Britefury.MainApp.AppWindow import AppWindow

		

class MainApp (object):
	def __init__(self, world, location=Location( '' )):
		class _ModelSubject (GSymSubject):
			def __init__(self, innerSubject):
				self._innerSubject = innerSubject
		
				
			def getFocus(self):
				return self._innerSubject.getFocus()
			
			def getPerspective(self):
				return None
			
			def getTitle(self):
				return '[model]'
			
			def getSubjectContext(self):
				return self._innerSubject.getSubjectContext()
			
			def getCommandHistory(self):
				return self._innerSubject.getCommandHistory()
		
		

		self._world = world

		self._browserContext = GSymBrowserContext( genericPresenterRegistry, True )
		
		self._appState = world.getAppStateSubject().getFocus()
		self._browserContext.registerMainSubject( world.getAppStateSubject() )
		self._browserContext.registerNamedSubject( 'model', _ModelSubject )
		
		self._rootWindow = AppWindow( self, location )
		self._rootWindow.setCloseRequestListener( self._onWindowCloseRequest )
		self._openWindows = set( [ self._rootWindow ] )
		
		self.onAppClose = None
		

		
	def getWorld(self):
		return self._world
	
	
	def show(self):
		self._rootWindow.show()

		
	def getBrowserContext(self):
		return self._browserContext
	
	
	def setCloseListener(self, listener):
		self.onAppClose = listener

		
	def _createNewWindow(self, location):
		newWindow = AppWindow( self, location )
		newWindow.setCloseRequestListener( self._onWindowCloseRequest )
		newWindow.show()
		self._openWindows.add( newWindow )
		
		
	def _onWindowCloseRequest(self, window):
		if len( self._openWindows ) == 1:
			if self._appState.hasUnsavedData():
				# Dialog here
				response = JOptionPane.showOptionDialog( window.getFrame(),
				                                         'You have not saved your work. Close anyway?', 'Unsaved data', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Close', 'Cancel' ], 'Cancel' )
				if response == JOptionPane.YES_OPTION:
					pass
				else:
					return
				
		window.close()
		self._openWindows.remove( window )
		
		if len( self._openWindows ) == 0:
			if self.onAppClose is not None:
				self.onAppClose( self )
	
	

	

	


