##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocPresent.Browser import Location

from Britefury.MainApp.AppWindow import AppWindow

		

class MainApp (object):
	def __init__(self, world, location=Location( '' )):
		self._world = world
		self._appState = world.getAppState()
		
		self._rootWindow = AppWindow( self, location )
		

		
	def getWorld(self):
		return self._world
	
	
	def show(self):
		self._rootWindow.show()

	
	
	def _createNewWindow(self, location):
		newWindow = AppWindow( self, location )
		newWindow.show()
	
	
	


	


