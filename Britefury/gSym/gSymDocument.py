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
from Britefury.gSym.gSymFormat import GSymFormat










class GSymDocumentInvalidLocation (Exception):
	pass

class GSymDocumentInvalidType (Exception):
	pass

class GSymDocumentInvalidStructure (Exception):
	pass

class GSymDocumentUnknownForm (Exception):
	pass

class GSymDocumentInvalidFormStructure (Exception):
	pass



class GSymDocumentLocationResolver (object):
	def defineFormat(self, env, xs, name, title, *forms):
		return name, forms
	
	def defineDefaultView(self, env, xs, *forms):
		return 'defineDefaultView', []
	
	def content(self, env, xs, *forms):
		return None, forms
	

	def _f_resolve(self, location, env, xs):
		if not isGLispList( xs ):
			raise GSymDocumentInvalidType
		
		if len( xs ) < 1:
			raise GSymDocumentInvalidStructure
		
		funcNameXs = xs[0]

		if funcNameXs[0] != '$':
			raise GSymDocumentInvalidStructure
		
		funcName = funcNameXs[1:]
		
		try:
			func = getattr( self, funcName )
		except AttributeError:
			raise GSymDocumentUnknownForm
		else:
			formName, subForms = func( env, xs, *xs[1:] )
			
			if formName is not None:
				locationName = location[0]
				subLocation = location[1:]
			else:
				locationName = None
				subLocation = location
			
			if locationName == formName:
				for form in subForms:
					try:
						return self._f_resolve( subLocation, env, form )
					except GSymDocumentInvalidLocation:
						pass
			
			raise GSymDocumentInvalidLocation





class GSymDocumentFormHandler (object):
	def handleForm(self, env, xs):
		if not isGLispList( xs ):
			raise GSymDocumentInvalidType
		
		if len( xs ) < 1:
			raise GSymDocumentInvalidStructure
		
		funcNameXs = xs[0]
		
		if funcNameXs[0] != '$':
			raise GSymDocumentInvalidStructure
		
		funcName = funcNameXs[1:]
		
		try:
			func = getattr( self, funcName )
		except AttributeError:
			raise GSymDocumentUnknownForm
		else:
			try:
				return func( env, xs, *xs[1:] )
			except TypeError:
				raise GSymDocumentInvalidFormStructure

			
			


	
			




		
		
		

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




def loadDocument(env, xs, formHandler):
	"""
	($gSymDocument <gsym_version> <content>)
	"""
	if not isGLispList( xs ):
		raise GSymDocumentInvalidStructure
	
	if len( xs ) != 3:
		raise GSymDocumentInvalidStructure
	
	header = xs[0]
	version = xs[1]
	contentXs = xs[2]
	
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
	
	
	return formHandler.handleForm( env, contentXs )









class DefineFormatFormHandler (GSymDocumentFormHandler):
	def __init__(self, format):
		super( GSymDefineFormatFormHandler, self ).__init__()
		self._format = format

	def defineDefaultView(self, env, xs, contentXs):
		"""
		($defineDefaultView <content>)
		"""
		formatName = self._format.name
		viewDefinition = GSymViewDefinition( env, formatName, contentXs )
		self._format.defaultViewDefinition = viewDefinition
		return viewDefinition


	
	
class ContentFormHandler (GSymDocumentFormHandler):
	defineFormatFormHandlerClass = DefineFormatFormHandler

	def defineFormat(self, env, xs, name, title, *content):
		"""
		($defineFormat <name> <title> <content...>)
		"""
		fomat = GSymFormat( name, title )
		env.registerFormat( name, format )
		
		defineFormatFormHandler = self.defineFormatFormHandlerClass( format )
		
		for form in content:
			defineFormatFormHandler.handleForm( env, form )

		return format
	
	


class DocumentFormHandler (GSymDocumentFormHandler):
	contentFormHandlerClass = ContentFormHandler

	def content(self, env, xs, *forms):
		"""
		($content <forms...>)
		"""
		contentFormHandler = self.contentFormHandlerClass()

		for form in forms:
			result = contentFormHandler.handleForm( env, formXs )


			
			
	
		

