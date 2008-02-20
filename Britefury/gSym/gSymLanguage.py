##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispInterpreter import specialform


class GSymLanguageInterface (object):
	def __init__(self, controlInterface):
		super( GSymLanguageInterface, self ).__init__()
		self._controlInterface = controlInterface




class GSymLanguageControlInterface (object):
	languageInterfaceClass = GSymLanguageInterface
	
	def __init__(self):
		super( GSymLanguageControlInterface, self ).__init__()
		self._languageInterface = self.languageInterfaceClass( self )
		
	def getLanguageInterface(self):
		return self._languageInterface
	
	
	@specialform
	def content(self, env, xs):
		return env.evaluate( xs[2:] )



class GSymLanguageFactory (object):
	languageControlInterfaceClass = GSymLanguageControlInterface
	
	
	
	def __init__(self, *languageDefinition):
		pass
	
	
	def createLanguageControlInterface(self):
		return self.languageControlInterfaceClass()


