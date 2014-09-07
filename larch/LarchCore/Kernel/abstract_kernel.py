##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************





class AbstractModule (object):
	def assign_variable(self, name, value):
		raise NotImplementedError, 'abstract'

	def getResultOfExecution(self, code, evaluate_last_expression, result_callback):
		"""
		Execute code within the module

		:param code: the code to execute
		:param evaluate_last_expression: if the last non-blank, non-comment line contains an expression, evaluate it
		and place the resulting value in the result structure passed to result_callback
		:param result_callback: a callback function of the form f(result) that is passed the result when execution completes
		"""
		raise NotImplementedError, 'abstract'

class AbstractKernel (object):
	def new_module(self, name):
		raise NotImplementedError, 'abstract'
