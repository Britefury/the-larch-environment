##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.DocModel.DMIO import readSX

from Britefury.GLisp.GLispCompiler import raiseCompilerError, GLispCompilerInvalidFormLength,   compileGLispExprToPyFunction
from Britefury.GLisp.PyCodeGen import PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyDictLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gSymCompiler import GMetaComponentCompiler
from Britefury.gSym.View.gSymView import GMetaComponentView
from Britefury.gSym.View.Interactor import GMetaComponentInteractor





_components = [
	GMetaComponentCompiler(),
	GMetaComponentView(),
	GMetaComponentInteractor(),
	]





def _compileSpecial(xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
	name = xs[0]
	
	if name == '$import':
		"""
		($import <modulename>)
		"""
		if len( xs ) != 2:
			raiseCompilerError( GLispCompilerInvalidFormLength, xs, 'need 1 parameter; the module name' )

		moduleName = xs[1]
		return PyVar( '__gsym__env__' ).methodCall( '_f_instantiateModule', moduleName, PyVar( '__gsym__globals__' ) ).debug( xs )

	
	for component in _components:
		try:
			return component.compileSpecial( xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )
		except GLispCompilerCouldNotCompileSpecial:
			pass
	
	raise GLispCompilerCouldNotCompileSpecial( xs )
	



class GMetaModuleFactory (object):
	def __init__(self, name, xs):
		gMetaFunctionName = filterIdentifierForPy( 'gMetaFactory_%s'  %  ( name, ) )
		gMetaModuleName = filterIdentifierForPy( 'gMetaFactoryModule_%s'  %  ( name, ) )
		
		lcls = {}
		for component in _components:
			lcls.update( component.getConstants() )
		
		_py_updateLocals = PyVar( 'locals' )().methodCall( 'update', PyVar( '__gmeta__globals__' ) ).debug( xs )
		
		py_prefix = [ _py_updateLocals ]
		
		self._moduleFactoryFunction = compileGLispExprToPyFunction( gMetaModuleName, gMetaFunctionName, [ '__gmeta__globals__' ], xs, _compileSpecial, lcls, py_prefix )
		self._name = name
		
		
	def instantiate(self, env, 



def instantiateGMetaModule(moduleFunction, env, moduleGlobals):
	g = copy( moduleGlobals )
	g['__gsym__env__'] = env
	g['__gsym__globals__'] = g
	
	return moduleFunction( g )

		
		
		
		