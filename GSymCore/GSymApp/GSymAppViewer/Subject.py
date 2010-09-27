##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.AttributeTable import SimpleAttributeTable

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.GSym import GSymSubject

from GSymCore.PythonConsole import Console

from GSymCore.GSymApp.GSymAppViewer.View import perspective




class _ConsoleListSubject (object):
	def __init__(self, appState, enclosingSubject):
		self._appState = appState
		self._enclosingSubject = enclosingSubject
		
		
	def __getitem__(self, key):
		for console in self._appState.getConsoles():
			if console.getIndex() == key:
				return Console.newConsoleSubject( console.getConsole(), self._enclosingSubject )
		raise KeyError, 'No console at index %s'  %  ( key, )
		

class _DocumentListSubject (object):
	def __init__(self, appState, enclosingSubject):
		self._appState = appState
		self._enclosingSubject = enclosingSubject
		
		
	def __getattr__(self, location):
		for appDocument in self._appState.getOpenDocuments():
			if appDocument.getLocation() == location:
				doc = appDocument.getDocument()
				return doc.newSubject( self._enclosingSubject, 'main.documents.' + location )
		raise AttributeError, 'no document at %s'  %  ( location, )
		

class GSymAppSubject (GSymSubject):
	def __init__(self, appState, world):
		self._appState = appState
		self._world = world
		self.consoles = _ConsoleListSubject( self._appState, self )
		self.documents = _DocumentListSubject( self._appState, self )

		
	def getFocus(self):
		return self._appState
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'gSym'
	
	def getSubjectContext(self):
		return SimpleAttributeTable.instance.withAttrs( world=self._world, document=None, documentLocation=None, location=Location( 'main' ) )
	
	def getCommandHistory(self):
		return None
	
	
	def find_module(self, fullname, path, world):
		for appDocument in self._appState.getOpenDocuments():
			doc = appDocument.getDocument()
			subject = doc.newSubject( self, 'main.documents.' + appDocument.getLocation() )
			try:
				f = subject.find_module
			except AttributeError:
				pass
			else:
				result = f( fullname, path, world )
				if result is not None:
					return result
		return None
		
	
