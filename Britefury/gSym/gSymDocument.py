##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMList

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.Util.NodeUtil import isListNode
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.View.gSymView import GSymViewFactory
from Britefury.gSym.gSymEnvironment import GSymEnvironment
from GSymCore.Languages.LISP import LISP



class GSymDocumentContentHandler (object):
	def withLanguageModule(self, world, importLanguage, xs):
		pass



class GSymDocumentViewContentHandler (GSymDocumentContentHandler):
	def __init__(self, commandHistory):
		super( GSymDocumentViewContentHandler, self ).__init__()
		self._commandHistory = commandHistory 
	
	def withLanguageModule(self, world, importLanguage, xs):
		language = world.getModuleLanguage( importLanguage )
		languageViewFactory = language.getViewFactory()
		return languageViewFactory.createDocumentView( xs, self._commandHistory )
		
		


class GSymDocumentLISPViewContentHandler (GSymDocumentContentHandler):
	def __init__(self, commandHistory):
		super( GSymDocumentLISPViewContentHandler, self ).__init__()
		self._commandHistory = commandHistory 
	
	def withLanguageModule(self, world, importLanguage, xs):
		language = LISP.language
		languageViewFactory = language.getViewFactory()
		return languageViewFactory.createDocumentView( xs, self._commandHistory )
	
	
	
	
class GSymDocumentTransformContentHandler (GSymDocumentContentHandler):
	def __init__(self, xform):
		super( GSymDocumentTransformContentHandler, self ).__init__()
		self._xform = xform 
		
	def withLanguageModule(self, world, importLanguage, xs):
		language = world.getModuleLanguage( importLanguage )
		transformModifyFn = language.getTransformModifyFn()
		xs2 = self._xform( xs )
		xs2 = DMList( xs2 )
		transformModifyFn( xs, xs2 )
		return xs2
		
		


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


class GSymDocumentContentInvalidStructure (Exception):
	pass

class GSymDocumentContentInvalidType (Exception):
	pass




def loadDocument(world, xs, contentHandler):
	"""
	($gSymDocument <gsym_version> <module>)
	"""
	if not isListNode( xs ):
		raise GSymDocumentInvalidStructure
	
	if len( xs ) < 3:
		raise GSymDocumentInvalidStructure
	
	header = xs[0]
	version = xs[1]
	unitXs = xs[2]
	
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
	
	
	
	if not isListNode( unitXs ):
		raise GSymDocumentInvalidStructure
	
	if len( unitXs ) < 1:
		raise GSymDocumentInvalidStructure
	
	
	"""
	($unit <content>)
	"""
	if unitXs[0] != '$unit':
		raise GSymDocumentInvalidStructure
	
	docXs = unitXs[1]
	
	
	
	
	if not isListNode( docXs ):
		raise GSymDocumentInvalidStructure
	
	if len( docXs ) < 1:
		raise GSymDocumentInvalidStructure
	
		
		
	if docXs[0] == '$withLanguageModule':
		"""
		($withLanguageModule <language_module_to_import> <content>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		languageImport = docXs[1]
		
		return contentHandler.withLanguageModule( world, languageImport, docXs[2] )
	else:
		raise GSymDocumentContentInvalidType


	
def newDocument(content):
	doc = [ '$gSymDocument', '0.1-alpha', [ '$unit', content ] ]
	return DMList( doc )




class GSymUnit (object):
	def __init__(self, languageModuleName, contentSX):
		self.languageModuleName = languageModuleName
		self.contentSX = contentSX
		
		
	def writeSX(self):
		sx = [ '$unit', self.languageModuleName, self.contentSX ]
		return DMList( sx )
	

	@staticmethod
	def readSX(self, unitSX):
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
			