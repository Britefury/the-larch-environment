##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguageInterface, GSymLanguageControlInterface, GSymLanguageFactory

from Britefury.GLisp.GLispInterpreter import specialform




class MetaLanguageInterface (GSymLanguageInterface):
	pass



class MetaLanguageControlInterface (GSymLanguageControlInterface):
	languageInterfaceClass = MetaLanguageInterface

	@specialform
	def content(self, env, xs):
		languageDefinition = [ env.evaluate( x )   for x in xs[2:] ]
		languageFactory = GSymLanguageFactory( languageDefinition )
		env.rootScope()['languageFactory'] = languageFactory
		return languageFactory
		
	
class MetaLanguageFactory (GSymLanguageFactory):
	languageControlInterfaceClass = MetaLanguageControlInterface





metaLanguageFactory = MetaLanguageFactory()