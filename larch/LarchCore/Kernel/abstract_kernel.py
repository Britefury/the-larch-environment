##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************





class AbstractLiveModule (object):
	def evaluate(self, expr):
		"""
		Evaluate and expression within the module

		:param expr: the expression to execute
		:return: an execution result structure that will be filled in as execution completes when executing asynchronously, or will already be filled in when executing synchronously
		"""
		raise NotImplementedError, 'abstract'

	def execute(self, code, evaluate_last_expression):
		"""
		Execute code within the module

		:param code: the code to execute
		:param evaluate_last_expression: if the last non-blank, non-comment line contains an expression, evaluate it
		and place the resulting value in the result structure passed to result_callback
		:return: an execution result structure that will be filled in as execution completes when executing asynchronously, or will already be filled in when executing synchronously
		"""
		raise NotImplementedError, 'abstract'


class AbstractKernel (object):
	def get_live_module(self):
		raise NotImplementedError, 'abstract'
