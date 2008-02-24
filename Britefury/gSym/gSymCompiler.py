##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispCompiler import compileGLispCustomFunctionToPy, compileGLispExprToPySrc, GLispCompilerError, filterIdentifierForPy
from Britefury.GLisp.GuardExpression import compileGuardExpression, GuardError



class GSymCompilerDefinition (object):
	def __init__(self, name, sourceFormat, targetFormat, compileFunction):
		self.name = name
		self.sourceFormat = sourceFormat
		self.targetFormat = targetFormat
		self._compileFunction = compileFunction
		
	def testCompile(self, xs):
		return [ self._compileFunction( x )   for x in xs ]

	
	
def defineCompiler(env, xs, name, sourceFormat, targetFormat, spec):
	def compileSpecial(src):
		if src[0] == '/compileEval':
			return '_compileEval( %s )'  %  ( compileGLispExprToPySrc( src[1], compileSpecial ), )
		else:
			env.glispError( GLispCompilerError, xs, 'cannot compile special \'%s\''  %  ( src[0], ) )
	
	def _compileEval(content):
		try:
			varValues, index = guardFunction( content )
		except GuardError:
			raise
		f = compileExprFunctions[index]
		return f( **varValues )

	guardFunction, varNameToValueIndirectionByGuard = compileGuardExpression( spec, [0], filterIdentifierForPy( 'compiler_guard_%s'  %  ( name, ) ) )
	
	def generateExprFunction(guardAndCompileExpr, i, varNamesSet):
		functionName = filterIdentifierForPy( 'compiler_expr_%s_%d'  %  ( name, i ) )
		return compileGLispCustomFunctionToPy( guardAndCompileExpr[1], functionName, list( varNamesSet ), compileSpecial, { '_compileEval' : _compileEval } )

	compileExprFunctions = [ generateExprFunction( guardAndCompileExpr, i, varNamesToValueIndirection.keys() )   for i, ( guardAndCompileExpr, varNamesToValueIndirection ) in enumerate( zip( spec, varNameToValueIndirectionByGuard ) ) ]
	
	return GSymCompilerDefinition( name, sourceFormat, targetFormat, _compileEval )
