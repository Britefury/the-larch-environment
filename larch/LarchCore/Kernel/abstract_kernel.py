##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************





class AbstractLiveModule (object):
	def __init__(self, kernel):
		self._kernel = kernel

	def new_execution_result(self):
		return self._kernel.new_execution_result()

	def evaluate(self, expr, result):
		"""
		Evaluate and expression within the module

		:param expr: the expression to execute
		:param result: an execution result structure created with AbstractKernel.new_execution_result that will be filled in as execution completes
		"""
		raise NotImplementedError, 'abstract'

	def execute(self, code, evaluate_last_expression, result):
		"""
		Execute code within the module

		:param code: the code to execute
		:param evaluate_last_expression: if the last non-blank, non-comment line contains an expression, evaluate it
		and place the resulting value in the result structure passed as `result`
		:param result: an execution result structure created with AbstractKernel.new_execution_result that will be filled in as execution completes
		"""
		raise NotImplementedError, 'abstract'


class AbstractKernel (object):
	def new_execution_result(self):
		raise NotImplementedError, 'abstract'

	def get_live_module(self):
		raise NotImplementedError, 'abstract'
