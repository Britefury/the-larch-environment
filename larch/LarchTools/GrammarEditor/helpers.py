##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr, EmbeddedPython2Suite
from LarchCore.Languages.Python2 import Schema as py_schema
from LarchTools.PythonTools.VisualRegex.VisualRegex import VisualPythonRegex


def new_empty_regex():
	return VisualPythonRegex()


def new_python_action():
	params = [py_schema.SimpleParam(name=p) for p in ['input', 'begin', 'end', 'value', 'bindings']]
	fn = py_schema.LambdaExpr(params=params, expr=py_schema.Load(name='value'))
	return EmbeddedPython2Expr(fn)


def new_python_helper_suite():
	py_block = [py_schema.CommentStmt(comment='Helper Python code goes here'), py_schema.BlankLine()]
	suite = py_schema.PythonSuite(suite=py_block)
	return EmbeddedPython2Suite(suite)