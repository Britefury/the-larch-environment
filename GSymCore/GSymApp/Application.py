##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import copy

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor

from GSymCore.PythonConsole import ConsoleSchema



class AppState (IncrementalOwner):
	def __init__(self):
		self._incr = IncrementalValueMonitor( self )
		
		self._openDocuments = []
		self._documentIDCounter = 1
		self._consoles = []
		
		
	def getOpenDocuments(self):
		self._incr.onAccess()
		return copy( self._openDocuments )
	
	def registerOpenDocument(self, gsymDocument):
		location = 'Doc%03d'  %  ( self._documentIDCounter, )
		self._documentIDCounter += 1
		appDocument = AppDocument( gsymDocument, location )
		self._openDocuments.append( appDocument )
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
	
		
	
class AppDocument (IncrementalOwner):
	def __init__(self, doc, location):
		self._incr = IncrementalValueMonitor( self )
		
		self._doc = doc
		self._location = location
		
		
		
	def getName(self):
		self._incr.onAccess()
		return self._doc.getDocumentName()
	
	def getDocument(self):
		self._incr.onAccess()
		return self._doc
	
	def getLocation(self):
		self._incr.onAccess()
		return self._location
	
	
	def hasUnsavedData(self):
		return self._doc.hasUnsavedData()
		
	

class AppConsole (IncrementalOwner):
	def __init__(self, index):
		self._incr = IncrementalValueMonitor( self )
		
		self._index = index
		self._console = ConsoleSchema.Console( '<console%d>'  %  ( index, ) )
		
		
		
	def getIndex(self):
		self._incr.onAccess()
		return self._index
	
	def getConsole(self):
		self._incr.onAccess()
		return self._console
		


