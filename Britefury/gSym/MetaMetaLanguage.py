##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.GLispUtil import isGLispList
from Britefury.GLisp.GLispInterpreter import specialform

from Britefury.gSym.gSymLanguage import GSymLanguageApplicationInterface, GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, GSymLanguageFactory
from Britefury.gSym.MetaLanguage import GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, MetaLanguageFactory

from Britefury.gSym.gSymView import defineView




class MetaMetaLanguageInstanceInterface (GSymLanguageInstanceInterface):
	"""Created in a meta-language document
	The meta-language document describes a meta-language via this"""
	@specialform
	def displayDefinition(self, env, xs):
		if len( xs ) < 4:
			env.glispError( GLispParameterListError, xs, 'MetaMetaLanguageInstanceInterface#displayDefinition: needs at least 2 parameters; the name, and the document format' )
		
		name = xs[2]
		docFormat = xs[3]
		spec = xs[4:]
		
		if not isinstance( name, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaMetaLanguageInstanceInterface#compilerDefinition: 1st parameter (compiler name) must be a string' )
			
		if not isinstance( docFormat, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaMetaLanguageInstanceInterface#compilerDefinition: 2nd parameter (document format) must be a string' )
			
		return defineView( env, xs, name, docFormat, spec )


		
		
		



class MetaMetaLanguageInstanceControlInterface (GSymLanguageInstanceControlInterface):
	"""Created in a meta-language document
	The meta-language document describes a meta-language to this"""
	languageInterfaceClass = MetaMetaLanguageInstanceInterface

	@specialform
	def content(self, env, xs):
		languageDefinition = [ env.evaluate( x )   for x in xs[2] ]
		languageFactory = MetaLanguageFactory( *languageDefinition )
		env.rootScope()['languageFactory'] = languageFactory
		return GSymLanguageApplicationInterface( self._factory, xs )
	
	
		
		
	
class MetaMetaLanguageFactory (GSymLanguageFactory):
	"""Created internally
	Imported into meta-language documents
	Used to create a meta-meta-language instance; to which the meta-language document supplies a meta-language description"""
	languageControlInterfaceClass = MetaMetaLanguageInstanceControlInterface





metaMetaLanguageFactory = MetaMetaLanguageFactory()