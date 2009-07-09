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





class GSymUnit (object):
	def __init__(self, languageModuleName, content):
		self.languageModuleName = languageModuleName
		self.content = DMNode.coerce( content )
		
		
		
	def write(self):
		return nodeClass_GSymUnit( languageModuleName=self.languageModuleName, content=self.content )
	

	@staticmethod
	def read(unit):
		if not isObjectNode( unit ):
			raise GSymDocumentInvalidStructure
		
		if not unit.isInstanceOf( nodeClass_GSymUnit ):
			raise GSymDocumentInvalidStructure
		
		return GSymUnit( unit['languageModuleName'], unit['content'] )
		

		
		
class GSymDocument (object):
	def __init__(self, unit):
		self.unit = unit
	
	
	def write(self):
		return nodeClass_GSymDocument( version='0.1-alpha', content=self.unit.write() )

	
	
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
		
		
		
		return GSymDocument( GSymUnit.read( content ) )
	
	
	
	

def viewUnitLocationAsPage(unit, location, world, commandHistory):
	language = world.getModuleLanguage( unit.languageModuleName )
	viewLocationAsPageFn = language.getViewLocationAsPageFn()
	return viewLocationAsPageFn( unit.content, location, commandHistory )


def viewUnitLispLocationAsPage(unit, location, world, commandHistory):
	language = LISP.language
	viewLocationAsPageFn = language.getViewLocationAsPageFn()
	return viewLocationAsPageFn( unit.content, location, commandHistory )


def transformUnit(unit, world, xform):
	assert False, 'not implemented'
	language = world.getModuleLanguage( unit.languageModuleName )
	transformModifyFn = language.getTransformModifyFn()
	xs2 = xform( unit.contentxs )
	xs2 = DMList( xs2 )
	transformModifyFn( xs, xs2 )
	return xs2
	
	
		
	
	
			