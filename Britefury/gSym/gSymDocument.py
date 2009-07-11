##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
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
	def __init__(self, unit):
		self.unit = DMNode.coerce( unit )
	
	
	def write(self):
		return nodeClass_GSymDocument( version='0.1-alpha', content=self.unit )

	
	
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
		
		return GSymDocument( content )
	
	
	
	

def viewUnitLocationAsPage(unit, location, world, commandHistory, app):
	language = world.getModuleLanguage( gSymUnit_getLanguageModuleName( unit ) )
	viewLocationAsPageFn = language.getViewLocationAsPageFn()
	return viewLocationAsPageFn( gSymUnit_getContent( unit ), location, commandHistory, app )


def viewUnitLispLocationAsPage(unit, location, world, commandHistory, app):
	language = LISP.language
	viewLocationAsPageFn = language.getViewLocationAsPageFn()
	return viewLocationAsPageFn( gSymUnit_getContent( unit ), location, commandHistory, app )


def transformUnit(unit, world, xform):
	assert False, 'not implemented'
	language = world.getModuleLanguage( gSymUnit_getLanguageModuleName( unit ) )
	transformModifyFn = language.getTransformModifyFn()
	xs2 = xform( gSymUnit_getContent( unit ) )
	xs2 = DMList( xs2 )
	transformModifyFn( xs, xs2 )
	return xs2
	
	
		
	
	
			