##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import weakref

from Britefury.Cell.CellEvaluator import CellEvaluator, CellEvaluationError
from Britefury.Cell.CellInterface import CellInterface



class _CellEvaluatorPythonExpressionLocals (object):
	def __init__(self):
		self._locals = {}


	def __getitem__(self, key):
		item = self._locals[key]
		if isinstance( item, CellInterface ):
			return item.value
		else:
			return item

	def __setitem__(self, key, value):
		self._locals[key] = value

	def __delitem__(self, key):
		del self._locals[key]



class CellEvaluatorPythonExpressionCompilationError (Exception):
	pass


class CellEvaluatorPythonExpression (CellEvaluator):
	__slots__ = [ '_expression', '_code', '_locals' ]


	def __init__(self, expression):
		super( CellEvaluatorPythonExpression, self ).__init__()
		self._locals = _CellEvaluatorPythonExpressionLocals()
		self._expression = expression
		self._code = None
		try:
			self._code = compile( self._expression, '<string>', 'eval' )
		except Exception:
			raise CellEvaluatorPythonExpressionCompilationError


	def evaluate(self, cell):
		self._locals._locals = {}
		if cell.owner is not None:
			self._locals._locals = cell.owner.cellScope
		try:
			return eval( self._code, {}, self._locals )
		except Exception, e:
			print e
			raise CellEvaluationError
