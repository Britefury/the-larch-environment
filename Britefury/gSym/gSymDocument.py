##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispUtil import isGLispList, isGLispString
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.gSymView import GSymViewDefinition
from Britefury.gSym.gSymEnvironment import GSymEnvironment





class GSymDocumentFormHandler (object):
	@abstractmethod
	def defineView(self, env, xs, name, title, spec):
		pass
	
	@abstractmethod
	def defineStructure(self, env, xs, name, title, spec):
		pass
	
	@abstractmethod
	def document(self, env, xs, contentHandler):
		pass

	
	
	
class GSymDocumentFormHandlerBoot (GSymDocumentFormHandler):
	def defineView(self, env, xs, name, title, spec):
		viewDefinition = GSymViewDefinition( env, name, content )
		env.registerViewDefinition( name, viewDefinition )
		return viewDefinition

	def defineStructure(self, env, xs, name, title, spec):
		pass

	def document(self, env, xs, contentHandler):
		pass

	

class GSymDocumentFormHandlerView (GSymDocumentFormHandler):
	def defineView(self, env, xs, name, title, spec):
		pass

	def defineStructure(self, env, xs, name, title, spec):
		pass
	
	def document(self, env, xs, contentHandler):
		pass

	
	

class GSymDocumentContentHandler (object):
	def __init__(self, formHandler):
		super( GSymDocumentContentHandler, self ).__init__()
		self.formHandler = formHandler

		
		
		
		

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
	($gSymDocument <gsym_version> <content_header>)
	"""
	if not isGLispList( xs ):
		raise GSymDocumentInvalidStructure
	
	if len( xs ) != 3:
		raise GSymDocumentInvalidStructure
	
	header = xs[0]
	version = xs[1]
	contentHeaderXs = xs[2]
	
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
	
	
	return _processContentHeader( env, contentHeaderXs, contentHandler )
	




class GSymDocumentContentHeaderInvalidStructure (Exception):
	pass

class GSymDocumentContentHeaderInvalidType (Exception):
	pass


def _processContentHeader(env, xs, contentHandler):
	"""
	($content <boot> <content>)
	or
	($content <content>)
	"""
	
	if not isGLispList( xs ):
		raise GSymDocumentContentHeaderInvalidType
	
	if len( xs )  <  2  or  len( xs )  >  3:
		raise GSymDocumentContentHeaderInvalidStructure
	
	if xs[0] != '$content':
		raise GSymDocumentContentHeaderInvalidStructure
	
	
	if len( xs ) == 3:
		_processBoot( env, xs[1], contentHandler )
		return _processDocumentContent( env, xs[2], contentHandler )
	else:
		return _processDocumentContent( env, xs[1], contentHandler )
		

	

class GSymDocumentBootInvalidStructure (Exception):
	pass

class GSymDocumentBootInvalidType (Exception):
	pass


def _processBoot(env, xs, contentHandler):
	"""
	($boot <boot_code...>)
	"""
	if not isGLispList( xs ):
		raise GSymDocumentBootInvalidType
	
	
	if len( xs ) < 1:
		raise GSymDocumentBootInvalidStructure
	
	if xs[0] != '$boot':
		raise GSymDocumentBootInvalidStructure
	
	
	bootCode = xs[1:]
	
	result = None
	for x in bootCode:
		result = _executeForm( env, x, contentHandler )
	return result
	



def _processDocumentContent(env, xs, contentHandler):
	return _executeForm( env, xs, contentHandler )

	
	


class GSymDocumentContentInvalidStructure (Exception):
	pass

class GSymDocumentContentInvalidType (Exception):
	pass

class GSymDocumentContentInvalidForm (Exception):
	pass


def _executeForm(env, xs, contentHandler):
	if not isGLispList( xs ):
		raise GSymDocumentContentInvalidType
	
	if len( xs ) < 1:
		raise GSymDocumentContentInvalidStructure

	# Determine the type of form
	if xs[0] == '$defineView':
		return _defineView( env, xs, contentHandler )
	elif xs[0] == '$defineStructure':
		return _defineStructure( env, xs, contentHandler )
	elif xs[0] == 'document':
		return _document( env, xs, contentHandler )
	else:
		raise GSymDocumentContentInvalidForm


	
	
	
class GSymDocumentDefineViewInvalidStructure (Exception):
	pass

class GSymDocumentDefineViewInvalidType (Exception):
	pass


def _defineView(env, xs, contentHandler):
	"""
	($defineView <identifier> <title> <content>)
	"""
	assert xs[0] == '$defineView'
	
	if len( xs ) != 4:
		raise GSymDocumentDefineVuewInvalidStructure
	
	name = xs[1]
	title = xs[2]
	spec = xs[3]
	
	
	if not isGLispString( name ):
		raise GSymDocumentDefineViewInvalidType
	
	if not isGLispString( title ):
		raise GSymDocumentDefineViewInvalidType
	
	return contentHandler.formHandler.defineView( env, xs, name, title, spec )





class GSymDocumentDefineStructureInvalidStructure (Exception):
	pass

class GSymDocumentDefineStructureInvalidType (Exception):
	pass


def _defineStructure(env, xs, contentHandler):
	"""
	($defineStructure <identifier> <title> <content>)
	"""
	assert xs[0] == '$defineStructure'
	
	if len( xs ) != 4:
		raise GSymDocumentDefineStructureInvalidStructure
	
	name = xs[1]
	title = xs[2]
	spec = xs[3]
	
	
	if not isGLispString( name ):
		raise GSymDocumentDefineStructureInvalidType
	
	if not isGLispString( title ):
		raise GSymDocumentDefineStructureInvalidType
	
	return contentHandler.formHandler.defineStructure( env, xs, name, title, spec )




class GSymDocumentDocFormInvalidStructure (Exception):
	pass

class GSymDocumentDocFormInvalidType (Exception):
	pass


def _document(env, xs, contentHandler):
	"""
	($document <format> <content>)
	"""
	assert xs[0] == '$document'
	
	if len( xs ) != 3:
		raise GSymDocumentDocFormInvalidStructure
	
	name = xs[1]
	spec = xs[2]
	
	
	if not isGLispString( name ):
		raise GSymDocumentDocFormInvalidType
	
	return contentHandler.formHandler.document( env, xs, name, spec )



