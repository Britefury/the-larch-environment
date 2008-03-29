##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction

from Britefury.gSym.View.gSymView import compileViewSpecial, compileViewLocals




_gMetaFnParams = [ '__view_node_instance_stack__' ]


def _compileSpecial(xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
	try:
		return compileViewSpecial( xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )
	except GLispCompilerCouldNotCompileSpecial:
		pass
	
	raise GLispCompilerCouldNotCompileSpecial( xs )
	


def compileGMeta(name, xs):
	gMetaFunctionName = filterIdentifierForPy( 'gMetaFactory_%s'  %  ( name, ) )
	gMetaModuleName = filterIdentifierForPy( 'gMetaFactoryModule_%s'  %  ( name, ) )
	
	compileGLispExprToPyFunction( gMetaModuleName, gMetaFunctionName, _gMetaFnParams, xs, _compileSpecial, lcls )