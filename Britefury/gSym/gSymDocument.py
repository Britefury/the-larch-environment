##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from BritefuryJ.DocModel import DMNode, DMModule

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.Util.NodeUtil import isObjectNode
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.gSymWorld import GSymWorld

from Britefury.gSym.gSymEnvironment import GSymEnvironment
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
	


	
class GSymDocument (object):
	def __init__(self, world, unit):
		self._world = world
		self._unit = DMNode.coerce( unit )
		self._commandHistory = CommandHistory()
		self._commandHistory.track( self._unit )
	
	
	def write(self):
		return nodeClass_GSymDocument( version='0.1-alpha', content=self._unit )

	
	
	def viewDocLocationAsPage(self, location, app):
		return self.viewUnitLocationAsPage( self._unit, location, app )
	
	
	def viewDocLocationAsLispPage(self, location, app):
		return self.viewUnitLocationAsLispPage( self._unit, location, app )


	
	def viewUnitLocationAsPage(self, unit, location, app):
		language = self._world.getModuleLanguage( gSymUnit_getLanguageModuleName( unit ) )
		viewLocationAsPageFn = language.getViewLocationAsPageFn()
		return viewLocationAsPageFn( self, gSymUnit_getContent( unit ), location, self._commandHistory, app )
	
	
	def viewUnitLocationAsLispPage(self, unit, location, app):
		language = LISP.language
		viewLocationAsPageFn = language.getViewLocationAsPageFn()
		return viewLocationAsPageFn( self, gSymUnit_getContent( unit ), location, self._commandHistory, app )



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


