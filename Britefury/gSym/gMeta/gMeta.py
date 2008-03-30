##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.DocModel.DMIO import readSX

from Britefury.GLisp.GLispCompiler import raiseCompilerError, GLispCompilerInvalidFormLength, GLispCompilerCouldNotCompileSpecial, compileGLispExprToPyFunction
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyDictLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gSymCompiler import GMetaComponentCompiler
from Britefury.gSym.gSymEnvironment import GSymEnvironment
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
		return PyVar( '__gsym__globals__' )['__gsym__env__'].methodCall( '_f_importModule', moduleName, PyVar( '__gsym__globals__' ) ).debug( xs )

	
	for component in _components:
		try:
			return component.compileSpecial( xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )
		except GLispCompilerCouldNotCompileSpecial:
			pass
	
	raise GLispCompilerCouldNotCompileSpecial( xs )
	



def _compileGMeta(name, xs):
	gMetaFunctionName = filterIdentifierForPy( 'gMetaFactory_%s'  %  ( name, ) )
	gMetaModuleName = filterIdentifierForPy( 'gMetaFactoryModule_%s'  %  ( name, ) )
	
	lcls = {}
	globalNames = []
	for component in _components:
		lcls.update( component.getConstants() )
		globalNames.extend( component.getGlobalNames() )
		
	
	py_prefix = []
	for name in globalNames:
		_py_setGlobal = PyVar( name ).assign_sideEffects( PyVar( '__gsym__globals__' ).methodCall( 'get', PyLiteralValue( name ) ) ).debug( xs )
		py_prefix.append( _py_setGlobal )
	
	return compileGLispExprToPyFunction( gMetaModuleName, gMetaFunctionName, [ '__gsym__globals__' ], xs, _compileSpecial, lcls, py_prefix )




class GMetaModuleFactory (object):
	def __init__(self, name, xs):
		super( GMetaModuleFactory, self ).__init__()
		self.name = name
		self.factoryFunction = _compileGMeta( name, xs )
		
		
	def instantiate(self, world, moduleGlobals):
		env = GSymEnvironment( world, self.name )

		g = copy( moduleGlobals )
		g['__gsym__env__'] = env
		g['__gsym__globals__'] = g
		
		return self.factoryFunction( g )
	
	
	@staticmethod
	def fromFile(self, name, f):
		if isinstance( f, str )  or  isinstance( f, unicode ):
			f = open( f, 'r' )
		return GMetaModuleFactory( name, readSX( f ) )
		

		
		
		
		