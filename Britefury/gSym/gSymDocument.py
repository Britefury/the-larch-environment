##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from java.io import IOException

from datetime import datetime

from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from BritefuryJ.DocModel import DMNode, DMSchema, DMIOReader, DMIOWriter

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.Util.NodeUtil import isObjectNode
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.gSymWorld import GSymWorld



class GSymDocumentInvalidStructure (Exception):
	pass

class GSymDocumentInvalidHeader (Exception):
	pass

class GSymDocumentInvalidVersion (Exception):
	pass

class GSymDocumentUnsupportedVersion (Exception):
	pass

class GSymDocumentUnknownItemType (Exception):
	pass




schema = DMSchema( 'GSymDocument', 'gsd', 'org.Britefury.gSym.Internal.GSymDocument' )

nodeClass_GSymUnit = schema.newClass( 'GSymUnit', [ 'schemaLocation', 'content' ] )
nodeClass_GSymDocument = schema.newClass( 'GSymDocument', [ 'version', 'content' ] )






def gSymUnit(schema, content):
	if isinstance( schema, str )  or  isinstance( schema, unicode ):
		schemaLocation = schema
	elif isinstance( schema, DMSchema ):
		schemaLocation = schema.getLocation()
	else:
		raise TypeError, 'gSymUnit(): schema must be a DMSchema or a schema location'
	return nodeClass_GSymUnit( schemaLocation=schemaLocation, content=content )

def gSymUnit_getSchemaLocation(unit):
	if not unit.isInstanceOf( nodeClass_GSymUnit ):
		raise GSymDocumentInvalidStructure
	return unit['schemaLocation']
	
def gSymUnit_getContent(unit):
	if not unit.isInstanceOf( nodeClass_GSymUnit ):
		raise GSymDocumentInvalidStructure
	return unit['content']
	




class GSymDocument (CommandHistoryListener):
	def __init__(self, world, unit):
		self._world = world
		self._unit = DMNode.coerce( unit )
		self._commandHistory = CommandHistory()
		self._commandHistory.track( self._unit )
		self._commandHistory.setCommandHistoryListener( self )
		self._docName = ''
		
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
		
		
	def getCommandHistory(self):
		return self._commandHistory

		
	
	def setCommandHistoryListener(self, listener):
		self._commandHistoryListener = listener
		

	def setUnsavedDataListener(self, listener):
		self._unsavedDataListener = listener
		
	def hasUnsavedData(self):
		return self._bHasUnsavedData
		
		
	
	def newSubject(self, enclosingSubject, location):
		return self.newUnitSubject( self._unit, enclosingSubject, location )

	def newUnitSubject(self, unit, enclosingSubject, location):
		unitClass = self._world.getUnitClass( gSymUnit_getSchemaLocation( unit ) )
		subjectFactory = unitClass.getUnitSubjectFactory()
		if subjectFactory is None:
			raise TypeError, 'cannot create subject for schema ' + gSymUnit_getSchemaLocation( unit )
		return subjectFactory( self, gSymUnit_getContent( unit ), enclosingSubject, location )

		
	
	
	def saveAs(self, filename):
		self._filename = filename
		self.save()
		
		
	def save(self):
		DMIOWriter.writeToFile( self._filename, self._write() )
		if self._bHasUnsavedData:
			self._bHasUnsavedData = False
			if self._unsavedDataListener is not None:
				self._unsavedDataListener( self )
		self._saveTime = datetime.now()
		
	
	
	def _write(self):
		return nodeClass_GSymDocument( version='0.1-alpha', content=self._unit )

	

	@staticmethod
	def read(world, doc):
		if not isObjectNode( doc ):
			raise GSymDocumentInvalidStructure
		
		if not doc.isInstanceOf( nodeClass_GSymDocument ):
			raise GSymDocumentInvalidStructure
		
		version = doc['version']
		content = doc['content']
		
		try:
			versionCmp = compareVersions( version, gSymVersion )
		except TypeError:
			raise GSymDocumentInvalidVersion
		except ValueError:
			raise GSymDocumentInvalidVersion
		
		if versionCmp > 0:
			raise GSymDocumentUnsupportedVersion
		
		return GSymDocument( world, content )


	@staticmethod
	def readFile(world, filename):
		if os.path.exists( filename ):
			try:
				documentRoot = DMIOReader.readFromFile( filename )
				documentRoot = DMNode.coerce( documentRoot )
				document = GSymDocument.read( world, documentRoot )
				document._filename = filename
				document._saveTime = datetime.now()
				return document
			except IOException:
				return None
		
			
			
	def onCommandHistoryChanged(self, history):
		if not self._bHasUnsavedData:
			self._bHasUnsavedData = True
			if self._unsavedDataListener is not None:
				self._unsavedDataListener( self )
		if self._commandHistoryListener is not None:
			self._commandHistoryListener.onCommandHistoryChanged( history )



			
