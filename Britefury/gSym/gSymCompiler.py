##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction, GLispCompilerCouldNotCompileSpecial
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects



class GSymCompilerDefinition (object):
	def __init__(self, name, sourceFormat, targetFormat, compileFunction):
		self.name = name
		self.sourceFormat = sourceFormat
		self.targetFormat = targetFormat
		self._compileFunction = compileFunction
		
	def compileContent(self, xs):
		return self._compileFunction( xs )

	
	
def defineCompiler(env, compilerXs, name, sourceFormat, targetFormat, spec):
	def _compileEval(content):
		return compilerFunction( content )

	
	def compileSpecial(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		if srcXs[0] == '$compileEval':
			return PyCall( PyVar( '_compileEval', dbgSrc=srcXs ), [ compileGLispExprToPyTree(srcXs[1], context, True, compileSpecial ) ], dbgSrc=srcXs )
		else:
			raise GLispCompilerCouldNotCompileSpecial( srcXs )
		
		
	compilerFunctionName = filterIdentifierForPy( 'compilerFactory_%s'  %  ( name, ) )
	compilerModuleName = filterIdentifierForPy( 'compilerFactoryModule_%s'  %  ( name, ) )
	
	compilerFactory = compileGLispExprToPyFunction( compilerModuleName, compilerFunctionName, [], spec, compileSpecial, lcls={ '_compileEval' : _compileEval } )
	compilerFunction = compilerFactory()
	
	return GSymCompilerDefinition( name, sourceFormat, targetFormat, compilerFunction )
