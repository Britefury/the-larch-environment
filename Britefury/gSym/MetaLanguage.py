##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.gSym.gSymLanguage import GSymLanguageInstanceInterface, GSymLanguageInstanceControlInterface, GSymLanguageFactory, GSymLanguageCompilerDefinition

from Britefury.GLisp.GLispInterpreter import specialform, isGLispList, gLispSrcToString, GLispParameterListError, GLispItemTypeError
from Britefury.GLisp.GLispCompiler import compileGLispCustomFunctionToPy, GLispCompilerError
from Britefury.GLisp.GuardExpression import compileGuardExpression, GuardError



class MetaLanguageInstanceInterface (GSymLanguageInstanceInterface):
	"""Created in a language document
	The language document describes a language content via this"""
	@specialform
	def compilerDefinition(self, env, xs):
		if len( xs ) < 3:
			env.glispError( GLispParameterListError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: needs at least 1 parameter; the target name' )
		
		targetName = xs[2]
		spec = xs[3:]
		
		if not isinstance( targetName, str ):
			env.glispError( GLispItemTypeError, xs, 'MetaLanguageInstanceInterface#compilerDefinition: first parameter (target name) must be a string' )
			
		
		def compileSpecial(src):
			if src[0] == '/eval':
				return '_compileEval( %s )'  %  ( src[1][1:], )
			else:
				env.glispError( GLispCompilerError, xs, 'cannot compile special \'%s\''  %  ( src[0], ) )
		
		def _compileEval(source):
			try:
				varValues, index = guardFunction( source )
			except GuardError:
				raise
			f = compileExprFunctions[index]
			return f( **varValues )

		guardFunction, varNamesByGuard = compileGuardExpression( spec, [0], 'compileTest' )
		
		def generateExprFunction(guardAndCompileExpr, i, varNamesSet):
			return compileGLispCustomFunctionToPy( guardAndCompileExpr[1], 'compileExpr%d' % ( i, ), list( varNamesSet ), compileSpecial, { '_compileEval' : _compileEval } )

		compileExprFunctions = [ generateExprFunction( guardAndCompileExpr, i, varNamesSet )   for i, ( guardAndCompileExpr, varNamesSet ) in enumerate( zip( spec, varNamesByGuard ) ) ]
		
		return GSymLanguageCompilerDefinition( targetName, _compileEval )


		
		
		



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

