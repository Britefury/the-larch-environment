##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispInterpreter import specialform


class GSymLanguageCompilerDefinition (object):
	def __init__(self, targetName, compileFunction):
		self.targetName = targetName
		self._compileFunction = compileFunction
		
	def testCompile(self, xs):
		return [ self._compileFunction( x )   for x in xs ]
		



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
		return self._factory._compilerTest.testCompile( xs[2:] )



class GSymLanguageFactory (object):
	"""Generated in a language document
	Imported into documents
	Used to create a language instance; to which the document supplies content"""
	languageControlInterfaceClass = GSymLanguageInstanceControlInterface
	
	
	
	def __init__(self, *languageDefinition):
		super( GSymLanguageFactory, self ).__init__()
		def getParamOfType(t):
			for l in languageDefinition:
				if isinstance( l, t ):
					return l
			return None
		self._compilerTest = getParamOfType( GSymLanguageCompilerDefinition )
	
	
	def createLanguageInstanceControlInterface(self):
		return self.languageControlInterfaceClass( self )


