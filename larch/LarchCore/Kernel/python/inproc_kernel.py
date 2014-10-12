##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import imp, sys

from BritefuryJ.Util.Jython import JythonException

from Britefury import LoadBuiltins

from LarchCore.Languages.Python2 import CodeGenerator

from .. import abstract_kernel, execution_result, execution_pres
from . import python_kernel



class InProcessModule (python_kernel.AbstractPythonModule):
	def __init__(self, name):
		self.__module = imp.new_module(name)
		LoadBuiltins.loadBuiltins(self.__module)


	def assign_variable(self, name, value):
		setattr(self.__module, name, value)

	def evaluate(self, expr, result_callback):
		result = getResultOfEvaluationWithinModule(expr, self.__module)
		result_callback(result)

	def execute(self, code, evaluate_last_expression, result_callback):
		if isinstance(code, str)  or  isinstance(code, unicode):
			raise NotImplementedError, 'InProcessModule.execute: executing of code as strings not yet supported'
		result = getResultOfExecutionWithinModule(code, self.__module, evaluate_last_expression)
		result_callback(result)



class InProcessKernel (python_kernel.AbstractPythonKernel):
	def new_module(self, name):
		return InProcessModule(name)




class InProcessExecutionResult (execution_result.AbstractExecutionResult):
	def __init__(self, streams=None, caughtException=None, result_in_tuple=None):
		super(InProcessExecutionResult, self).__init__(streams)
		self._caught_exception = caughtException
		self._result_in_tuple = result_in_tuple


	@property
	def caught_exception(self):
		return self._caught_exception


	def has_result(self):
		return self._result_in_tuple is not None

	@property
	def result(self):
		return self._result_in_tuple[0]   if self._result_in_tuple is not None   else None


	def was_aborted(self):
		return False


	def errorsOnly(self):
		return InProcessExecutionResult( self._streams.suppress_stream( 'out' ), self._caught_exception, None )



	def hasErrors(self):
		return self._caught_exception is not None  or  self._streams.has_content_for( 'err' )


	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return execution_pres.execution_result_box( self._streams, self._caught_exception, self._result_in_tuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return execution_pres.minimal_execution_result_box( self._streams, self._caught_exception, self._result_in_tuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )





def getResultOfExecutionWithinModule(pythonCode, module, bEvaluate):
	std = execution_result.MultiplexedRichStream(['stdout', 'stderr'])

	evalCode = execCode = None
	caughtException = None
	result = None
	if bEvaluate:
		try:
			execCode, evalCode = CodeGenerator.compileForModuleExecutionAndEvaluation( module, pythonCode, module.__name__ )
		except:
			caughtException = JythonException.getCurrentException()
	else:
		try:
			execCode = CodeGenerator.compileForModuleExecution( module, pythonCode, module.__name__ )
		except:
			caughtException = JythonException.getCurrentException()

	if execCode is not None  or  evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = std.stdout
		sys.stderr = std.stderr
		setattr( module, 'display', std.stdout.display )
		setattr( module, 'displayerr', std.stderr.display )

		try:
			exec execCode in module.__dict__
			if evalCode is not None:
				result = [ eval( evalCode, module.__dict__ ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return InProcessExecutionResult( std, caughtException, result )


def getResultOfExecutionInScopeWithinModule(pythonCode, globals, locals, module, bEvaluate):
	std = execution_result.MultiplexedRichStream(['stdout', 'stderr'])

	evalCode = execCode = None
	caughtException = None
	result = None
	if bEvaluate:
		try:
			execCode, evalCode = CodeGenerator.compileForModuleExecutionAndEvaluation( module, pythonCode, module.__name__ )
		except:
			caughtException = JythonException.getCurrentException()
	else:
		try:
			execCode = CodeGenerator.compileForModuleExecution( module, pythonCode, module.__name__ )
		except:
			caughtException = JythonException.getCurrentException()

	if globals is None:
		globals = module.__dict__
	if locals is None:
		locals = module.__dict__

	if execCode is not None  or  evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = std.stdout
		sys.stderr = std.stderr
		setattr( module, 'display', std.stdout.display )
		setattr( module, 'displayerr', std.stderr.display )

		try:
			exec execCode in globals, locals
			if evalCode is not None:
				result = [ eval( evalCode, globals, locals ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return InProcessExecutionResult( std, caughtException, result )





def getResultOfEvaluationWithinModule(pythonExpr, module):
	std = execution_result.MultiplexedRichStream(['stdout', 'stderr'])

	evalCode = None
	caughtException = None
	result = None
	if isinstance(pythonExpr, str)  or  isinstance(pythonExpr, unicode):
		evalCode = pythonExpr
	else:
		try:
			evalCode = CodeGenerator.compileForModuleEvaluation( module, pythonExpr, module.__name__ )
		except:
			caughtException = JythonException.getCurrentException()

	if evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = std.stdout
		sys.stderr = std.stderr

		try:
			result = [ eval( evalCode, module.__dict__ ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return InProcessExecutionResult( std, caughtException, result )


def getResultOfEvaluationInScopeWithinModule(pythonExpr, globals, locals, module):
	std = execution_result.MultiplexedRichStream(['stdout', 'stderr'])

	evalCode = None
	caughtException = None
	result = None
	try:
		evalCode = CodeGenerator.compileForModuleEvaluation( module, pythonExpr, module.__name__ )
	except:
		caughtException = JythonException.getCurrentException()

	if globals is None:
		globals = module.__dict__
	if locals is None:
		locals = module.__dict__

	if evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = std.stdout
		sys.stderr = std.stderr

		try:
			result = [ eval( evalCode, globals, locals ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return InProcessExecutionResult( std, caughtException, result )





def executeWithinModule(pythonCode, module, bEvaluate):
	if bEvaluate:
		execCode, evalCode = CodeGenerator.compileForModuleExecutionAndEvaluation( module, pythonCode, module.__name__ )
	else:
		execCode = CodeGenerator.compileForModuleExecution( module, pythonCode, module.__name__ )
		evalCode = None

	if execCode is not None  or  evalCode is not None:
		exec execCode in module.__dict__
		if evalCode is not None:
			return [ eval( evalCode, module.__dict__ ) ]
		else:
			return None
	return None


def executeInScopeWithinModule(pythonCode, globals, locals, module, bEvaluate):
	if bEvaluate:
		execCode, evalCode = CodeGenerator.compileForModuleExecutionAndEvaluation( module, pythonCode, module.__name__ )
	else:
		execCode = CodeGenerator.compileForModuleExecution( module, pythonCode, module.__name__ )
		evalCode = None

	if globals is None:
		globals = module.__dict__
	if locals is None:
		locals = module.__dict__

	if execCode is not None  or  evalCode is not None:
		exec execCode in globals, locals
		if evalCode is not None:
			return [ eval( evalCode, globals, locals ) ]
		else:
			return None
	return None