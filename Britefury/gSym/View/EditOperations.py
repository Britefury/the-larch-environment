##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.PyCodeGen import pyt_compare, pyt_coerce, PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyIsInstance, PyReturn, PyRaise, PyTry, PyIf, PySimpleIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects
from Britefury.GLisp.GLispCompiler import raiseCompilerError, raiseRuntimeError, compileGLispExprToPyFunction, compileGLispCallParamToPyTree, GLispCompilerCouldNotCompileSpecial, GLispCompilerInvalidFormType, GLispCompilerInvalidFormLength, GLispCompilerInvalidItem

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent
from Britefury.gSym.RelativeNode import RelativeNode, RelativeList



def _sanitiseInputData(data):
	if isinstance( data, RelativeNode ):
		return _sanitiseInputData( data.node )
	elif isinstance( data, list ):
		return [ _sanitiseInputData( x )   for x in data ]
	elif isinstance( data, tuple ):
		return tuple( [ _sanitiseInputData( x )   for x in data ] )
	else:
		return data


def _runtime_replace(data, replacement):
	if isinstance( data, RelativeNode ):
		if not isinstance( data.parent, DMListInterface ):
			raise TypeError, '$replace: @data.parent must be a DMListInterface'
		data.parent[data.indexInParent] = _sanitiseInputData( replacement )
		return data.parent[data.indexInParent]
	else:
		raise TypeError, '$replace: @data must be a RelativeNode'



class GMetaComponentEditOperations (GMetaComponent):
	def compileSpecial(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		name = srcXs[0]
		
		compileSubExp = lambda xs: compileGLispExprToPyTree( xs, context, True, compileSpecial )

		if name == '$replace':
			"""
			($replace <data> <replacement_data>)
			"""
			if len( srcXs ) != 3:
				raiseCompilerError( GLispCompilerInvalidFormLength, srcXs, '$replace requires 2 parameters' )
			
			return PyVar( '__gsym__replace' )( compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ) )
	
		raise GLispCompilerCouldNotCompileSpecial( srcXs )


	def getConstants(self):
		return {
			'__gsym__replace' : _runtime_replace,
			}
	
	
	def getGlobalNames(self):
		return []

	