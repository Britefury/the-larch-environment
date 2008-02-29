##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispCompiler import compileGLispCustomFunctionToPy, compileGLispExprToPySrc, GLispCompilerError, filterIdentifierForPy
from Britefury.GLisp.PatternMatch import compileMatchExpression, NoMatchError



class GSymCompilerDefinition (object):
	def __init__(self, name, sourceFormat, targetFormat, compileFunction):
		self.name = name
		self.sourceFormat = sourceFormat
		self.targetFormat = targetFormat
		self._compileFunction = compileFunction
		
	def compileContent(self, xs):
		return self._compileFunction( xs )

	
	
def defineCompiler(env, xs, name, sourceFormat, targetFormat, spec):
	def compileSpecial(src):
		if src[0] == '/compileEval':
			return '_compileEval( %s )'  %  ( compileGLispExprToPySrc( src[1], compileSpecial ), )
		else:
			env.glispError( GLispCompilerError, xs, 'cannot compile special \'%s\''  %  ( src[0], ) )
	
	def _compileEval(content):
		try:
			varValues, index = matchFunction( content )
		except NoMatchError:
			env.glispError( NoMatchError, xs, 'compileEval: cannot process; no suitable match expression found' )
		f = compileExprFunctions[index]
		return f( **varValues )

	matchFunction, varNameToValueIndirectionByMatch = compileMatchExpression( spec, [0], filterIdentifierForPy( 'compiler_match_%s'  %  ( name, ) ) )
	
	def generateExprFunction(matchAndCompileExpr, i, varNamesSet):
		functionName = filterIdentifierForPy( 'compiler_expr_%s_%d'  %  ( name, i ) )
		return compileGLispCustomFunctionToPy( matchAndCompileExpr[1], functionName, list( varNamesSet ), compileSpecial, { '_compileEval' : _compileEval } )

	compileExprFunctions = [ generateExprFunction( matchAndCompileExpr, i, varNamesToValueIndirection.keys() )   for i, ( matchAndCompileExpr, varNamesToValueIndirection ) in enumerate( zip( spec, varNameToValueIndirectionByMatch ) ) ]
	
	return GSymCompilerDefinition( name, sourceFormat, targetFormat, _compileEval )
