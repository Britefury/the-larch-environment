##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import copy

from BritefuryJ.Browser import Location

from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchCore.PythonConsole import Console
from LarchCore.MainApp import FragmentInspector



class AppState (object):
	def __init__(self):
		self._incr = IncrementalValueMonitor( self )
		
		self._openDocuments = []
		self._docToAppDoc = {}
		self._documentIDCounter = 1
		self._consoles = []
		
		
	def getOpenDocuments(self):
		self._incr.onAccess()
		return copy( self._openDocuments )
	
	def registerOpenDocument(self, document, documentCollectionLocation):
		assert isinstance( documentCollectionLocation, Location )
		relativeLocation = 'Doc%03d'  %  ( self._documentIDCounter, )
		location = documentCollectionLocation + '.' + relativeLocation
		self._documentIDCounter += 1
		appDocument = AppDocument( document, relativeLocation, self )
		document.setLocation( location )
		self._openDocuments.append( appDocument )
		self._docToAppDoc[document] = appDocument
		self._incr.onChanged()
		return appDocument

	def closeDocument(self, document):
		appDocument = self._docToAppDoc[document]
		del self._docToAppDoc[document]
		self._openDocuments.remove( appDocument )
		self._incr.onChanged()
		
		
	def hasUnsavedData(self):
		for doc in self._openDocuments:
			if doc.hasUnsavedData():
				return True
		return False



	def getConsoles(self):
		self._incr.onAccess()
		return copy( self._consoles )
	
	def addConsole(self, console):
		self._consoles.append( console )
		self._incr.onChanged()
	
		
	
class AppDocument (object):
	def __init__(self, doc, relativeLocation, appState):
		self._incr = IncrementalValueMonitor( self )
		
		self._doc = doc
		self._relativeLocation = relativeLocation
		self.__appState = appState
		
		
		
	def getName(self):
		self._incr.onAccess()
		return self._doc.getDocumentName()
	
	def getDocument(self):
		self._incr.onAccess()
		return self._doc
	
	def getRelativeLocation(self):
		self._incr.onAccess()
		return self._relativeLocation
	
	
	def hasUnsavedData(self):
		return self._doc.hasUnsavedData()


	@property
	def appState(self):
		return self.__appState
		
	

class AppConsole (object):
	def __init__(self, index):
		self._incr = IncrementalValueMonitor( self )
		
		self._index = index
		self._console = Console.Console( '<console%d>'  %  ( index, ) )
		
		
		
	def getIndex(self):
		self._incr.onAccess()
		return self._index
	
	def getConsole(self):
		self._incr.onAccess()
		return self._console
		


