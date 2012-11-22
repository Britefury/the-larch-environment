##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.AttributeTable import SimpleAttributeTable

from BritefuryJ.Projection import Subject
from Britefury.Kernel import AppLocationPath
from Britefury.Kernel.Document import Document

from LarchCore.PythonConsole import Console

from LarchCore.MainApp.MainAppViewer.View import perspective


class _ConsoleListSubject (object):
	def __init__(self, appState, enclosingSubject):
		self._appState = appState
		self._enclosingSubject = enclosingSubject

		
	def __resolve__(self, key):
		index = int( key[1:] )
		for console in self._appState.getConsoles():
			if console.getIndex() == index:
				return Console.ConsoleSubject( console.getConsole(), self._enclosingSubject )
		raise KeyError, 'No console at index %s'  %  ( key, )
		

class _DocumentListSubject (object):
	def __init__(self, appState, enclosingSubject):
		self._appState = appState
		self._enclosingSubject = enclosingSubject


	def __resolve__(self, relativeLocation):
		for appDocument in self._appState.getOpenDocuments():
			if appDocument.getRelativeLocation() == relativeLocation:
				doc = appDocument.getDocument()
				return doc.newSubject( self._enclosingSubject, self._enclosingSubject._rootLocation + '.documents.' + relativeLocation, None, appDocument.getName() )
		raise AttributeError, 'no document at %s'  %  ( relativeLocation, )
		

class MainAppSubject (Subject):
	def __init__(self, appState, world):
		super( MainAppSubject, self ).__init__( world.worldSubject )
		self._appState = appState
		self._world = world
		self.consoles = _ConsoleListSubject( self._appState, self )
		self.documents = _DocumentListSubject( self._appState, self )


	@property
	def appLocationPath(self):
		return self.enclosingSubject.appLocationPath.withPathEntry( 'Home', self )


	def getFocus(self):
		return self._appState
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Larch'
	

	def loadDocument(self, filename):
		document = Document.readFile( self._world, filename )
		if document is not None:
			self._appState.registerOpenDocument( document )
			return document
		return None
		
	
	
	def import_resolve(self, name, fullname, path):
		for appDocument in self._appState.getOpenDocuments():
			doc = appDocument.getDocument()
			subject = doc.newSubject( self, self._rootLocation + '.documents.' + appDocument.getRelativeLocation(), None, appDocument.getName() )
			try:
				resolve = subject.import_resolve
			except AttributeError:
				pass
			else:
				result = resolve( name, fullname, path )
				if result is not None:
					return result
		return None
		
	
