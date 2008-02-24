##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispInterpreter import specialform

from Britefury.gSym.gSymCompiler import GSymCompilerDefinition
from Britefury.gSym.gSymView import GSymViewDefinition



class GSymLanguageApplicationInterface (object):
	"""Created by GSymLanguageInstanceControlInterface.content()"""
	def __init__(self, factory, xs):
		super( GSymLanguageApplicationInterface, self ).__init__()
		self._factory = factory
		self._xs = xs
		
		self.actions = [ self._p_makeAction( entry )   for entry in self._factory._languageDefinition   if self._p_isAction( entry ) ]
		self.views = [ self._p_makeView( entry )   for entry in self._factory._languageDefinition   if self._p_isView( entry ) ]
		
		
	def _p_isAction(self, entry):
		return isinstance( entry, GSymCompilerDefinition )
	
	def _p_makeAction(self, entry):
		assert self._p_isAction( entry ), 'entry is not an action'
		def _compile():
			return entry.compileContent( self._xs[2] )
		return 'Compile; %s'  %  ( entry.name, ), _compile 
	

	
	def _p_isView(self, entry):
		return isinstance( entry, GSymViewDefinition )
	
	def _p_makeView(self, entry):
		assert self._p_isView( entry ), 'entry is not a view'
		def _view(commandHistory, styleSheetDispatcher):
			return entry.createDocumentView( self._xs[2], commandHistory, styleSheetDispatcher )
		return _view



class GSymLanguageInstanceInterface (object):
	"""Created in a document
	The document supplies content via this"""
	def __init__(self, controlInterface):
		super( GSymLanguageInstanceInterface, self ).__init__()
		self._controlInterface = controlInterface



class GSymLanguageInstanceControlInterface (object):
	"""Created in a document
	The document supplies content to this"""
	languageInterfaceClass = GSymLanguageInstanceInterface
	
	def __init__(self, factory):
		super( GSymLanguageInstanceControlInterface, self ).__init__()
		self._factory = factory
		self._languageInterface = self.languageInterfaceClass( self )
		
	def getLanguageInstanceInterface(self):
		return self._languageInterface
	
	
	@specialform
	def content(self, env, xs):
		return GSymLanguageApplicationInterface( self._factory, xs )



class GSymLanguageFactory (object):
	"""Generated in a language document
	Imported into documents
	Used to create a language instance; to which the document supplies content"""
	languageControlInterfaceClass = GSymLanguageInstanceControlInterface
	
	
	
	def __init__(self, *languageDefinition):
		super( GSymLanguageFactory, self ).__init__()
		self._languageDefinition = languageDefinition
		def getParamOfType(t):
			for l in languageDefinition:
				if isinstance( l, t ):
					return l
			return None
		self._compilerTest = getParamOfType( GSymCompilerDefinition )
		self._viewTest = getParamOfType( GSymViewDefinition )
	
	
	def createLanguageInstanceControlInterface(self):
		return self.languageControlInterfaceClass( self )


