##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import imp

from Britefury import LoadBuiltins

from . import abstract_kernel

from LarchCore.Languages.Python2.Execution import Execution



class InProcessModule (abstract_kernel.AbstractModule):
	def __init__(self, name):
		self.__module = imp.new_module(name)
		LoadBuiltins.loadBuiltins(self.__module)


	def assign_variable(self, name, value):
		setattr(self.__module, name, value)

	def evaluate(self, expr, result_callback):
		result = Execution.getResultOfEvaluationWithinModule(expr, self.__module)
		result_callback(result)

	def execute(self, code, evaluate_last_expression, result_callback):
		if isinstance(code, str)  or  isinstance(code, unicode):
			raise NotImplementedError, 'InProcessModule.execute: executing of code as strings not yet supported'
		result = Execution.getResultOfExecutionWithinModule(code, self.__module, evaluate_last_expression)
		result_callback(result)



class InProcessKernel (abstract_kernel.AbstractKernel):
	def new_module(self, name):
		return InProcessModule(name)
