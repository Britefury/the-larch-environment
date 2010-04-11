##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import copy

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValue

from GSymCore.Terminal import TerminalSchema


class AppState (IncrementalOwner):
	def __init__(self):
		self._incr = IncrementalValue( self )
		
		self._openDocuments = []
		self._terminals = []
		self._configuration = AppConfiguration()
		
		
	def getOpenDocuments(self):
		self._incr.onLiteralAccess()
		return copy( self._openDocuments )
		
	def addOpenDocument(self, doc):
		self._openDocuments.append( doc )
		self._incr.onChanged()
		
		
	def getTerminals(self):
		self._incr.onLiteralAccess()
		return copy( self._terminals )
	
	def addTerminal(self, term):
		self._terminals.append( term )
		self._incr.onChanged()

		
	def getConfiguration(self):
		self._incr.onLiteralAccess()
		return self._configuration
	
		
	
class AppDocument (IncrementalOwner):
	def __init__(self, name, location):
		self._incr = IncrementalValue( self )
		
		self._name = name
		self._location = location
		
		
		
	def getName(self):
		self._incr.onLiteralAccess()
		return self._name
	
	def getLocation(self):
		self._incr.onLiteralAccess()
		return self._location
		
	

class AppTerminal (IncrementalOwner):
	def __init__(self, name):
		self._incr = IncrementalValue( self )
		
		self._name = name
		self._terminal = TerminalSchema.Terminal()
		
		
		
	def getName(self):
		self._incr.onLiteralAccess()
		return self._name
	
	def getTerminal(self):
		self._incr.onLiteralAccess()
		return self._terminal
		
	

class AppConfiguration (IncrementalOwner):
	def __init__(self):
		self._incr = IncrementalValue( self )
		


