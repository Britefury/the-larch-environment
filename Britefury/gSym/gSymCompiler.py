##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispCompiler import compileGLispCustomFunctionToPy, GLispCompilerError
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
	
	return GSymCompilerDefinition( name, sourceFormat, targetFormat, _compileEval )
