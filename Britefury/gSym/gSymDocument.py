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

from Britefury.gSym.View.gSymView import GSymViewDefinition
from Britefury.gSym.gSymEnvironment import GSymEnvironment





class GSymDocumentContentHandler (object):
	@abstractmethod
	def metaLanguage(self, env, name, xs):
		pass
	
	@abstractmethod
	def language(self, env, name, xs):
		pass
	
	@abstractmethod
	def page(self, env, importLanguage, xs):
		pass



class GSymDocumentExecuteContentHandler (GSymDocumentContentHandler):
	def metaLanguage(self, env, name, xs):
		env._f_setMetaLanguageViewDefinition( GSymViewDefinition( env, name, xs ) )
	
	def language(self, env, name, xs):
		pass
	
	def page(self, env, importLanguage, xs):
		pass



class GSymDocumentViewContentHandler (GSymDocumentContentHandler):
	def __init__(self, commandHistory, stylesheetDispatcher):
		super( GSymDocumentViewContentHandler, self ).__init__()
		self._commandHistory = commandHistory 
		self._stylesheetDispatcher = stylesheetDispatcher 
	
	def metaLanguage(self, env, name, xs):
		metaLang = env._f_getMetaLanguageViewDefinition()
		return metaLang.createDocumentView( xs, self._commandHistory, self._stylesheetDispatcher )
	
	def language(self, env, name, xs):
		pass
	
	def page(self, env, importLanguage, xs):
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
	($gSymDocument <gsym_version> <module>)
	"""
	if not isGLispList( xs ):
		raise GSymDocumentInvalidStructure
	
	if len( xs ) < 3:
		raise GSymDocumentInvalidStructure
	
	header = xs[0]
	version = xs[1]
	moduleXs = xs[2]
	
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
	
	
	
	if not isGLispList( moduleXs ):
		raise GSymDocumentInvalidStructure
	
	if len( moduleXs ) < 1:
		raise GSymDocumentInvalidStructure
	
	
	"""
	($module <content>)
	"""
	if moduleXs[0] != '$module':
		raise GSymDocumentInvalidStructure
	
	docXs = moduleXs[1]
	
	
	
	
	if not isGLispList( docXs ):
		raise GSymDocumentInvalidStructure
	
	if len( docXs ) < 1:
		raise GSymDocumentInvalidStructure
	
		
		
	if docXs[0] == '$metaLanguage':
		"""
		($metaLanguage <name> <view_definition>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		return contentHandler.metaLanguage( env,docXs[1], docXs[2] )
	elif docXs[0] == '$language':
		"""
		($language <name> <content>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		languageName = docXs[1]
	
		return contentHandler.language( env, languageName, docXs[2] )
	elif docXs[0] == '$use':
		"""
		($page <language_import> <content>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		languageImport = docXs[1]
		
		return contentHandler.page( env, languageImport, docXs[2] )
	else:
		raise GSymDocumentContentInvalidType






