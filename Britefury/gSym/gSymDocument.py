##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from datetime import datetime

from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from BritefuryJ.DocModel import DMNode, DMModule, DMIOReader, DMIOWriter

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.Util.NodeUtil import isObjectNode
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.gSymWorld import GSymWorld

from GSymCore.Languages.LISP import LISP



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




module = DMModule( 'GSymDocument', 'gsd', 'org.Britefury.gSym.Internal.GSymDocument' )

nodeClass_GSymUnit = module.newClass( 'GSymUnit', [ 'languageModuleName', 'content' ] )
nodeClass_GSymDocument = module.newClass( 'GSymDocument', [ 'version', 'content' ] )


GSymWorld.registerInternalDMModule( module )





def gSymUnit(languageModuleName, content):
	return nodeClass_GSymUnit( languageModuleName=languageModuleName, content=content )

def gSymUnit_getLanguageModuleName(unit):
	if not unit.isInstanceOf( nodeClass_GSymUnit ):
		raise GSymDocumentInvalidStructure
	return unit['languageModuleName']
	
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
		
		self._bHasUnsavedData = False
		self._filename = None
		self._saveTime = None
	
		self._commandHistoryListener = None
		self._unsavedDataListener = None



	def hasUnsavedData(self):
		return self._bHasUnsavedData
	
	def getFilename(self):
		return self._filename
	
	def getSaveTime(self):
		return self._saveTime
	
	
	def getDocumentName(self):
		return self._docName
	
	def setDocumentName(self, name):
		self._docName = name

		
	
	def setCommandHistoryListener(self, listener):
		self._commandHistoryListener = listener
		

	def setUnsavedDataListener(self, listener):
		self._unsavedDataListener = listener
		
		
	
	def viewDocLocationAsPage(self, locationPrefix, location, app):
		return self.viewUnitLocationAsPage( self._unit, locationPrefix, location, app )
	
	
	def viewDocLocationAsLispPage(self, locationPrefix, location, app):
		return self.viewUnitLocationAsLispPage( self._unit, locationPrefix, location, app )


	
	def viewUnitLocationAsPage(self, unit, locationPrefix, location, app):
		resolveResult = self.resolveUnitLocation( unit, locationPrefix, location, app )
		if resolveResult is not None:
			viewLocationAsPageFn = resolveResult.language.getViewDocNodeAsPageFn()
			return viewLocationAsPageFn( resolveResult.document, resolveResult.docNode, resolveResult.locationPrefix, resolveResult.location, self._commandHistory, app )
		else:
			return None
	
	
	def viewUnitLocationAsLispPage(self, unit, locationPrefix, location, app):
		resolveResult = self.resolveUnitLocation( unit, locationPrefix, location, app )
		if resolveResult is not None:
			viewLocationAsPageFn = LISP.language.getViewDocNodeAsPageFn()
			return viewLocationAsPageFn( resolveResult.document, resolveResult.docNode, resolveResult.locationPrefix, resolveResult.location, self._commandHistory, app )
		else:
			return None


	
	def viewDocLocationAsElement(self, locationPrefix, location, app):
		return self.viewUnitLocationAsElement( self._unit, locationPrefix, location, app )
	
	
	def viewDocLocationAsLispElement(self, locationPrefix, location, app):
		return self.viewUnitLocationAsLispElement( self._unit, locationPrefix, location, app )


	
	def viewUnitLocationAsElement(self, unit, locationPrefix, location, app):
		resolveResult = self.resolveUnitLocation( unit, locationPrefix, location, app )
		if resolveResult is not None:
			viewLocationAsElementFn = resolveResult.language.getViewDocNodeAsElementFn()
			return viewLocationAsElementFn( resolveResult.document, resolveResult.docNode, resolveResult.locationPrefix, resolveResult.location, self._commandHistory, app )
		else:
			return None
	
	
	def viewUnitLocationAsLispElement(self, unit, locationPrefix, location, app):
		resolveResult = self.resolveUnitLocation( unit, locationPrefix, location, app )
		if resolveResult is not None:
			viewLocationAsElementFn = LISP.language.getViewDocNodeAsElementFn()
			return viewLocationAsElementFn( resolveResult.document, resolveResult.docNode, resolveResult.locationPrefix, resolveResult.location, self._commandHistory, app )
		else:
			return None


	
	def resolveLocation(self, locationPrefix, location, app):
		return self.resolveUnitLocation( self._unit, locationPrefix, location, app )
	
	def resolveUnitLocation(self, unit, locationPrefix, location, app):
		language = self._world.getLanguage( gSymUnit_getLanguageModuleName( unit ) )
		resolveLocationFn = language.getResolveLocationFn()
		return resolveLocationFn( language, self, gSymUnit_getContent( unit ), locationPrefix, location, app )
		
	
	
	def saveAs(self, filename):
		self._filename = filename
		self.save()
		
		
	def save(self):
		f = open( self._filename, 'w' )
		if f is not None:
			f.write( DMIOWriter.writeAsString( self._write() ) )
			f.close()
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
		f = open( filename, 'r' )
		if f is not None:
			try:
				documentRoot = DMIOReader.readFromString( f.read(), world.resolver )
				documentRoot = DMNode.coerce( documentRoot )
				document = GSymDocument.read( world, documentRoot )
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



			
