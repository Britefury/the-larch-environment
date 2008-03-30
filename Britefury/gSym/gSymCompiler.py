##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction, GLispCompilerCouldNotCompileSpecial, GLispCompilerInvalidFormLength, GLispCompilerInvalidItem
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, PyCodeGenError, PyVar, PyLiteral, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent




class GSymCompiler (object):
	def __init__(self, targetFormat, compilerFunction):
		super( GSymCompiler, self ).__init__()
		self.targetFormat = targetFormat
		self._compilerFunction = compilerFunction
		
		
	def compileCode(self, node):
		return self._compilerFunction( node )

	
class GSymCompilerCollection (object):
	def __init__(self, compilers):
		super( GSymCompilerCollection, self ).__init__()
		self.compilers = compilers


		

class GMetaComponentCompiler (GMetaComponent):
	def compileSpecial(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		name = srcXs[0]
		
		if name == '$defineCompiler':
			"""
			($defineCompiler <target_language> <target_format> <function>)
			"""
			
			if len( srcXs )  !=  4:
				raise GLispCompilerInvalidFormLength( srcXs )
			
			targetLanguage = srcXs[1]
			targetFormat = srcXs[2]
		
			return PyVar( '_GSymCompiler' )( targetFormat, compileGLispExprToPyTree( srcXs[3], context, True, compileSpecial ) ).debug( srcXs )
		elif name == '$compilerCollection':
			"""
			($compilerCollection <compilers...>)
			"""
			
			return PyVar( '_GSymCompilerCollection' )( [ compileGLispExprToPyTree( x, context, True, compileSpecial )   for x in srcXs[1:] ] ).debug( srcXs )
	
		raise GLispCompilerCouldNotCompileSpecial( srcXs )


	def getConstants(self):
		return {
			'_GSymCompiler' : GSymCompiler,
			'_GSymCompilerCollection' : GSymCompilerCollection,
			}
	
	
	def getGlobalNames(self):
		return []

