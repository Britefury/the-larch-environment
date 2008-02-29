##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.GLispInterpreter import specialform, isGLispList
from Britefury.GLisp.GLispCompiler import compileGLispCustomFunctionToPy, GLispCompilerError

from Britefury.gSym.gSymLanguage import GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, GSymLanguageFactory
from Britefury.gSym.MetaLanguage import GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, MetaLanguageFactory




class MetaMetaLanguageInstanceInterface (GSymLanguageInstanceInterface):
	"""Created in a meta-language document
	The meta-language document describes a meta-language via this"""
	pass


		
		
		



class MetaMetaLanguageInstanceControlInterface (GSymLanguageInstanceControlInterface):
	"""Created in a meta-language document
	The meta-language document describes a meta-language to this"""
	languageInterfaceClass = MetaMetaLanguageInstanceInterface

	@specialform
	def content(self, env, xs):
		languageDefinition = [ env.evaluate( x )   for x in xs[2] ]
		languageFactory = MetaLanguageFactory( *languageDefinition )
		env.rootScope()['languageFactory'] = languageFactory
		return languageFactory
	
	
		
		
	
class MetaMetaLanguageFactory (GSymLanguageFactory):
	"""Created internally
	Imported into meta-language documents
	Used to create a meta-meta-language instance; to which the meta-language document supplies a meta-language description"""
	languageControlInterfaceClass = MetaMetaLanguageInstanceControlInterface





metaMetaLanguageFactory = MetaMetaLanguageFactory()