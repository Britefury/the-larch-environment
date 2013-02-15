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

from java.lang import Object

from datetime import datetime

from BritefuryJ.ChangeHistory import ChangeHistory, ChangeHistoryListener

from BritefuryJ.Isolation import IsolationPickle

from BritefuryJ.LSpace.Input import ObjectDndHandler

from BritefuryJ.Projection import SubjectPath, TransientSubjectPathEntry

from Britefury import LoadBuiltins



class Document (ChangeHistoryListener):
	def __init__(self, world, contents):
		self._world = world
		self._contents = contents
		
		self._changeHistory = ChangeHistory()
		self._changeHistory.track( self._contents )
		self._changeHistory.addChangeHistoryListener( self )
		
		self._docName = ''
		self._subject = None

		self._bHasUnsavedData = True
		self._filename = None
		self._saveTime = None
	
		self._documentModules = {}



	@property
	def contents(self):
		return self._contents


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
		
		
	def getChangeHistory(self):
		return self._changeHistory

		
	
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


	def shutdown(self):
		print 'Document.shutdown: WARNING (TODO): commands have not been uninstalled'
		self.unloadAllImportedModules()

	def close(self):
		self.shutdown()

	
	
	def newSubject(self, enclosingSubject, importName, title):
		path = SubjectPath( _DocumentPathEntry( self, importName, title ) )
		return self.newModelSubject( self._contents, enclosingSubject, path, importName, title )

	def newModelSubject(self, model, enclosingSubject, path, importName, title):
		return model.__new_subject__( self, enclosingSubject, path, importName, title )

		
	
	
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
		self._saveTime = datetime.now()


	def writeAsBytes(self):
		return IsolationPickle.dumpToBytes( self._contents )


	def writeToOutputStream(self, stream):
		IsolationPickle.dumpToOutputStream( self._contents, stream )


	def reload(self):
		# Shut down
		self.shutdown()

		# Reload
		if self._filename is None:
			raise ValueError, 'Cannot reload document - no filename'
		f = open( self._filename, 'rU' )
		documentRoot = IsolationPickle.load( f )
		f.close()

		# New contents
		self._contents = documentRoot

		self._changeHistory.removeChangeHistoryListener( self )

		# New change history
		self._changeHistory = ChangeHistory()
		self._changeHistory.track( self._contents )
		self._changeHistory.addChangeHistoryListener( self )



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


	@staticmethod
	def readFromBytes(world, buf, documentName):
		documentRoot = IsolationPickle.loadFromBytes( buf )

		document = Document( world, documentRoot )
		document._docName = documentName
		return document


	@staticmethod
	def readFromInputStream(world, stream, documentName):
		documentRoot = IsolationPickle.loadFromInputStream( stream )

		document = Document( world, documentRoot )
		document._docName = documentName
		return document




	def onChangeHistoryChanged(self, history):
		if not self._bHasUnsavedData:
			self._bHasUnsavedData = True




class _DocumentPathEntry (TransientSubjectPathEntry):
	def __init__(self, document, importName, title):
		self.__document = document
		self.__importName = importName
		self.__title = title


	def follow(self, outerSubject):
		return self.__document.newModelSubject( self.__document._contents, outerSubject, SubjectPath( self ), self.__importName, self.__title)


	def canPersist(self):
		return False


			


class LinkSubjectDrag (Object):
	def __init__(self, subject):
		self.subject = subject
