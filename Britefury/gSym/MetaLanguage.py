##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.gSym.gSymLanguage import GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, GSymLanguageFactory

from Britefury.GLisp.GLispInterpreter import specialform, isGLispList, gLispSrcToString, GLispParameterListError, GLispItemTypeError

from Britefury.gSym.gSymCompiler import defineCompiler



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
			
		
		#def compileSpecial(src):
			#if src[0] == '/viewEval':
				#return '_buildView( %s )'  %  ( src[1][1:], )
			#else:
				#env.glispError( GLispCompilerError, xs, 'cannot compile special \'%s\''  %  ( src[0], ) )
		
		#def _buildView(source):
			#try:
				#varValues, index = guardFunction( source )
			#except GuardError:
				#raise
			#f = compileExprFunctions[index]
			#return f( **varValues )

		#guardFunction, varNamesByGuard = compileGuardExpression( spec, [0], 'compileTest' )
		
		#def generateExprFunction(guardAndCompileExpr, i, varNamesSet):
			#return compileGLispCustomFunctionToPy( guardAndCompileExpr[1], 'buildViewExpr%d' % ( i, ), list( varNamesSet ), compileSpecial, { '_buildView' : _buildView } )

		#compileExprFunctions = [ generateExprFunction( guardAndCompileExpr, i, varNamesSet )   for i, ( guardAndCompileExpr, varNamesSet ) in enumerate( zip( spec, varNamesByGuard ) ) ]
		
		#return GSymLanguageCompilerDefinition( compilerName, sourceFormat, targetFormat, _compileEval )
		return None

		
		
		



class MetaLanguageInstanceControlInterface (GSymLanguageInstanceControlInterface):
	"""Created in a language document
	The language document describes a language to this"""
	languageInterfaceClass = MetaLanguageInstanceInterface

	@specialform
	def content(self, env, xs):
		languageDefinition = [ env.evaluate( x )   for x in xs[2:] ]
		languageFactory = GSymLanguageFactory( *languageDefinition )
		env.rootScope()['languageFactory'] = languageFactory
		return languageFactory
	
	
		
		
	
class MetaLanguageFactory (GSymLanguageFactory):
	"""Generated in a meta-language document
	Imported into language documents
	Used to create a meta-language instance; to which the language document supplies a language description"""
	languageControlInterfaceClass = MetaLanguageInstanceControlInterface

