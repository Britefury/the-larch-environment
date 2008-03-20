##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispUtil import isGLispList
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.gSymView import GSymViewDefinition
from Britefury.gSym.gSymEnvironment import GSymEnvironment





class GSymDocumentContentHandler (object):
	@abstractmethod
	def metaLanguage(self, env, name, xs):
		pass
	
	@abstractmethod
	def singlePage(self, env, xs):
		pass
	
	@abstractmethod
	def use(self, importName, env, xs):
		pass



class GSymDocumentExecuteContentHandler (GSymDocumentContentHandler):
	def metaLanguage(self, env, name, xs):
		env._f_setMetaLanguageViewDefinition( GSymViewDefinition( env, name, xs ) )
	
	def singlePage(self, env, xs):
		pass
	
	def use(self, importName, env, xs):
		pass



class GSymDocumentViewContentHandler (GSymDocumentContentHandler):
	def __init__(self, commandHistory, stylesheetDispatcher):
		super( GSymDocumentViewContentHandler, self ).__init__()
		self._commandHistory = commandHistory 
		self._stylesheetDispatcher = stylesheetDispatcher 
	
	def metaLanguage(self, env, name, xs):
		metaLang = env._f_getMetaLanguageViewDefinition()
		return metaLang.createDocumentView( xs, self._commandHistory, self._stylesheetDispatcher )
	
	def singlePage(self, env, xs):
		pass
	
	def use(self, importName, env, xs):
		pass



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




def loadDocument(env, xs, contentHandler):
	"""
	($gSymDocument <gsym_version> <content>)
	"""
	if not isGLispList( xs ):
		raise GSymDocumentInvalidStructure
	
	if len( xs ) < 3:
		raise GSymDocumentInvalidStructure
	
	header = xs[0]
	version = xs[1]
	docXs = xs[2]
	
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
	
	
	
	if not isGLispList( docXs ):
		raise GSymDocumentContentInvalidStructure
	
	if len( docXs ) < 1:
		raise GSymDocumentContentInvalidStructure
	
	
	if docXs[0] == '$metaLanguage':
		"""
		($metaLanguage <name> <view_definition>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		return contentHandler.metaLanguage( env,docXs[1], docXs[2] )
	elif docXs[0] == '$singlePage':
		"""
		($singlePage <content>)
		"""
		if len( docXs ) != 2:
			raise GSymDocumentContentInvalidStructure
	
		return contentHandler.singlePage( env, docXs[1] )
	elif docXs[0] == '$use':
		"""
		($use <import> <content>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		importName = docXs[1]
		
		return contentHandler.use( importName, env,docXs[2] )
	else:
		raise GSymDocumentContentInvalidType






