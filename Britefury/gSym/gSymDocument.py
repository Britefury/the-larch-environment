##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

import cPickle

from datetime import datetime

from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from Britefury.gSym.gSymWorld import GSymWorld



class GSymDocument (CommandHistoryListener):
	def __init__(self, world, contents):
		self._world = world
		self._contents = contents
		
		self._commandHistory = CommandHistory()
		self._commandHistory.track( self._contents )
		self._commandHistory.setCommandHistoryListener( self )
		
		self._docName = ''
		self._location = None
		
		self._bHasUnsavedData = True
		self._filename = None
		self._saveTime = None
	
		self._commandHistoryListener = None
		self._unsavedDataListener = None



	def hasUnsavedData(self):
		return self._bHasUnsavedData
	
	def getFilename(self):
		return self._filename
	
	def hasFilename(self):
		return self._filename is not None
	
	def getSaveTime(self):
		return self._saveTime
	
	
	def getWorld(self):
		return self._world
	
	
	def getDocumentName(self):
		return self._docName
	
	def setDocumentName(self, name):
		self._docName = name
		
		
	def setLocation(self, location):
		self._location = location
	
	def getLocation(self):
		return self._location
	
	def relativeToAbsoluteLocation(self, relativeLocation):
		return self._location + relativeLocation
	
		
	def getCommandHistory(self):
		return self._commandHistory

		
	
	def setCommandHistoryListener(self, listener):
		self._commandHistoryListener = listener
		

	def setUnsavedDataListener(self, listener):
		self._unsavedDataListener = listener
		
	def hasUnsavedData(self):
		return self._bHasUnsavedData
		
	
	def newSubject(self, enclosingSubject, location, title):
		return self.newModelSubject( self._contents, enclosingSubject, location, title )

	def newModelSubject(self, model, enclosingSubject, location, title):
		return model.__new_subject__( self, enclosingSubject, location, title )

		
	
	
	def saveAs(self, filename):
		self._filename = filename
		self.save()
		
		
	def save(self):
		f = open( self._filename, 'w' )
		cPickle.dump( self._contents, f )
		f.close()
		if self._bHasUnsavedData:
			self._bHasUnsavedData = False
			if self._unsavedDataListener is not None:
				self._unsavedDataListener( self )
		self._saveTime = datetime.now()

	

	@staticmethod
	def readFile(world, filename):
		if os.path.exists( filename ):
			try:
				f = open( filename )
				documentRoot = cPickle.load( f )
				f.close()

				document = GSymDocument( world, documentRoot )
				document._filename = filename
				document._saveTime = datetime.now()
				return document
			except IOError:
				return None
				
		
			
			
	def onCommandHistoryChanged(self, history):
		if not self._bHasUnsavedData:
			self._bHasUnsavedData = True
			if self._unsavedDataListener is not None:
				self._unsavedDataListener( self )
		if self._commandHistoryListener is not None:
			self._commandHistoryListener.onCommandHistoryChanged( history )



			
