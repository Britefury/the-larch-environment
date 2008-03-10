##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.gSym.gSymLanguage import GSymLanguageApplicationInterface, GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, GSymLanguageFactory

from Britefury.GLisp.GLispUtil import isGLispList
from Britefury.GLisp.GLispInterpreter import specialform, GLispParameterListError, GLispItemTypeError

from Britefury.gSym.gSymCompiler import defineCompiler
from Britefury.gSym.gSymView import defineView



class MetaLanguageInstanceInterface (GSymLanguageInstanceInterface):
	"""Created in a language document
	The language document describes a language content via this"""
	@specialform
	def compilerDefinition(self, env, xs):
		if len( xs ) < 5:
			env.glispError( GLispParameterListError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: needs at least 3 parameters; the name, the source format, and the target format' )
		
		name = xs[2]
		sourceFormat = xs[3]
		targetFormat = xs[4]
		spec = xs[5:]
		
		if not isinstance( name, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: 1st parameter (name) must be a string' )
			
		if not isinstance( sourceFormat, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: 2nd parameter (source format) must be a string' )
			
		if not isinstance( targetFormat, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: 3rd parameter (target format) must be a string' )
			
		return defineCompiler( env, xs, name, sourceFormat, targetFormat, spec )


	
	
	@specialform
	def displayDefinition(self, env, xs):
		if len( xs ) < 4:
			env.glispError( GLispParameterListError, xs, 'MetaLanguageInstanceInterface#displayDefinition: needs at least 2 parameters; the name, and the document format' )
		
		name = xs[2]
		docFormat = xs[3]
		spec = xs[4:]
		
		if not isinstance( name, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: 1st parameter (compiler name) must be a string' )
			
		if not isinstance( docFormat, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: 2nd parameter (document format) must be a string' )
			
		return defineView( env, xs, name, docFormat, spec )

		
		
		



class MetaLanguageInstanceControlInterface (GSymLanguageInstanceControlInterface):
	"""Created in a language document
	The language document describes a language to this"""
	languageInterfaceClass = MetaLanguageInstanceInterface

	@specialform
	def content(self, env, xs):
		languageDefinition = [ env.evaluate( x )   for x in xs[2] ]
		languageFactory = GSymLanguageFactory( *languageDefinition )
		env.rootScope()['languageFactory'] = languageFactory
		return GSymLanguageApplicationInterface( self._factory, xs )
	
	
		
		
	
class MetaLanguageFactory (GSymLanguageFactory):
	"""Generated in a meta-language document
	Imported into language documents
	Used to create a meta-language instance; to which the language document supplies a language description"""
	languageControlInterfaceClass = MetaLanguageInstanceControlInterface

