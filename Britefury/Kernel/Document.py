##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os
import sys
import imp

#import cPickle

from datetime import datetime

from BritefuryJ.ChangeHistory import ChangeHistory, ChangeHistoryListener

from BritefuryJ.Isolation import IsolationPickle

from Britefury import LoadBuiltins

from Britefury.Kernel.World import World



class Document (ChangeHistoryListener):
	def __init__(self, world, contents):
		self._world = world
		self._contents = contents
		
		self._changeHistory = ChangeHistory()
		self._changeHistory.track( self._contents )
		self._changeHistory.setChangeHistoryListener( self )
		
		self._docName = ''
		self._location = None
		
		self._bHasUnsavedData = True
		self._filename = None
		self._saveTime = None
	
		self._changeHistoryListener = None
		self._unsavedDataListener = None
		
		self._documentModules = {}



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
	
		
	def getChangeHistory(self):
		return self._changeHistory

		
	
	def setChangeHistoryListener(self, listener):
		self._changeHistoryListener = listener
		

	def setUnsavedDataListener(self, listener):
		self._unsavedDataListener = listener
		
	def hasUnsavedData(self):
		return self._bHasUnsavedData
	
	
	
	def newModule(self, fullname, loader):
		mod = imp.new_module( fullname )
		LoadBuiltins.loadBuiltins( mod )
		sys.modules[fullname] = mod
		mod.__file__ = fullname
		mod.__loader__ = loader
		mod.__path__ = fullname.split( '.' )
		self._documentModules[fullname] = mod
		self._world.registerImportedModule( fullname )
		return mod
	
	def unloadImportedModules(self, moduleFullnames):
		modules = set( moduleFullnames )
		modulesToRemove = set( self._documentModules.keys() ) & modules
		for moduleFullname in modulesToRemove:
			del sys.modules[moduleFullname]
			del self._documentModules[moduleFullname]
		self._world.unregisterImportedModules( modulesToRemove )
		return modulesToRemove
	
	def unloadAllImportedModules(self):
		modulesToRemove = set( self._documentModules.keys() )
		for moduleFullname in modulesToRemove:
			del sys.modules[moduleFullname]
		self._documentModules = {}
		self._world.unregisterImportedModules( modulesToRemove )
		return modulesToRemove
	
	
	
	def newSubject(self, enclosingSubject, location, importName, title):
		return self.newModelSubject( self._contents, enclosingSubject, location, importName, title )

	def newModelSubject(self, model, enclosingSubject, location, importName, title):
		return model.__new_subject__( self, enclosingSubject, location, importName, title )

		
	
	
	def saveAs(self, filename):
		self._setFilename( filename )
		self.save()
		
		
	def save(self):
		data = IsolationPickle.dumps( self._contents )
		f = open( self._filename, 'w' )
		f.write( data )
		f.close()
		if self._bHasUnsavedData:
			self._bHasUnsavedData = False
			if self._unsavedDataListener is not None:
				self._unsavedDataListener( self )
		self._saveTime = datetime.now()

		
		
	def _setFilename(self, filename):
		self._filename = filename
		head, documentName = os.path.split( filename )
		documentName, ext = os.path.splitext( documentName )
		self._docName = documentName
	

	@staticmethod
	def readFile(world, filename):
		if os.path.exists( filename ):
			try:
				f = open( filename, 'rU' )
				documentRoot = IsolationPickle.load( f )
				f.close()

				document = Document( world, documentRoot )
				document._setFilename( filename )
				document._saveTime = datetime.now()
				return document
			except IOError:
				return None
				
		
			
			
	def onChangeHistoryChanged(self, history):
		if not self._bHasUnsavedData:
			self._bHasUnsavedData = True
			if self._unsavedDataListener is not None:
				self._unsavedDataListener( self )
		if self._changeHistoryListener is not None:
			self._changeHistoryListener.onChangeHistoryChanged( history )



			
