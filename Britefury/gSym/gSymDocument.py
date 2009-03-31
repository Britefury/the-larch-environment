##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMNode

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.Util.NodeUtil import isListNode
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

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





class GSymUnit (object):
	def __init__(self, languageModuleName, contentSX):
		self.languageModuleName = languageModuleName
		self.contentSX = DMNode.coerce( contentSX )
		
		
		
	def writeSX(self):
		sx = [ '$unit', self.languageModuleName, self.contentSX ]
		return DMList( sx )
	

	@staticmethod
	def readSX(unitSX):
		"""
		($unit <language_module_to_import> <content>)
		"""
		
		if not isListNode( unitSX ):
			raise GSymDocumentInvalidStructure
		
		if len( unitSX ) != 3:
			raise GSymDocumentInvalidStructure
		

		if unitSX[0] != '$unit':
			raise GSymDocumentInvalidStructure
		
		languageModuleName = unitSX[1]
		
		return GSymUnit( languageModuleName, unitSX[2] )
		

		
		
class GSymDocument (object):
	def __init__(self, unit):
		self.unit = unit
	
	
	def writeSX(self):
		sx = [ '$gSymDocument', '0.1-alpha', self.unit.writeSX() ]
		return DMList( sx )

	
	
	@staticmethod
	def readSX(world, docSX):
		"""
		($gSymDocument <gsym_version> <document_content>)
		"""
		if not isListNode( docSX ):
			raise GSymDocumentInvalidStructure
		
		if len( docSX ) < 3:
			raise GSymDocumentInvalidStructure
		
		header = docSX[0]
		version = docSX[1]
		contentSX = docSX[2]
		
		if header != "$gSymDocument":
			raise GSymDocumentInvalidHeader
		
		try:
			versionCmp = compareVersions( version, gSymVersion )
		except TypeError:
			raise GSymDocumentInvalidVersion
		except ValueError:
			raise GSymDocumentInvalidVersion
		
		if versionCmp > 0:
			raise GSymDocumentUnsupportedVersion
		
		
		
		return GSymDocument( GSymUnit.readSX( contentSX ) )
	
	
	
	

def viewUnit(unit, world, commandHistory):
	language = world.getModuleLanguage( unit.languageModuleName )
	languageViewFactory = language.getViewFactory()
	return languageViewFactory.createDocumentView( unit.contentSX, commandHistory )


def viewUnitLisp(unit, world, commandHistory):
	language = LISP.language
	languageViewFactory = language.getViewFactory()
	return languageViewFactory.createDocumentView( unit.contentSX, commandHistory )


def transformUnit(unit, world, xform):
	language = world.getModuleLanguage( unit.languageModuleName )
	transformModifyFn = language.getTransformModifyFn()
	xs2 = xform( xs )
	xs2 = DMList( xs2 )
	transformModifyFn( xs, xs2 )
	return xs2
	
	
		
	
	
			