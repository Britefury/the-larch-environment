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
from . import python_kernel, module_finder



class InProcModuleSource (module_finder.AbstractModuleSource):
	def __init__(self, source):
		self.__source = source

	def execute(self, module):
		if isinstance(self.__source, str)  or  isinstance(self.__source, unicode):
			exec self.__source in module.__dict__
		elif isinstance(self.__source, list):
			for x in self.__source:
				if isinstance(x, str)  or  isinstance(x, unicode):
					exec x in module.__dict__
				else:
					code = CodeGenerator.compileForModuleExecution(module, x, module.__name__)
					exec code in module.__dict__
		else:
			raise TypeError, 'source should be a str, unicode or a list, it is a {0} ({1})'.format(type(self.__source), self.__source)



class InProcessLiveModule (python_kernel.AbstractPythonLiveModule):
	def __init__(self, kernel, name):
		super(InProcessLiveModule, self).__init__(kernel)
		self.__module = imp.new_module(name)
		self.__execution_count = 0
		LoadBuiltins.loadBuiltins(self.__module)


	def assign_variable(self, name, value):
		setattr(self.__module, name, value)

	def evaluate(self, expr, result):
		result.clear()
		return _evaluateWithinModuleIntoResult(result, expr, self.__module)

	def execute(self, code, evaluate_last_expression, result):
		if isinstance(code, str)  or  isinstance(code, unicode):
			raise NotImplementedError, 'InProcessModule.execute: executing of code as strings not yet supported'
		result.clear()
		self.__execution_count += 1
		return _executeWithinModuleIntoResult(result, code, self.__module, evaluate_last_expression, self.__execution_count)



class InProcessKernel (python_kernel.AbstractPythonKernel):
	def __init__(self, ctx):
		super(InProcessKernel, self).__init__(ctx)
		self.__module_finder = module_finder.ModuleFinder()
		self.__module_finder.install_hooks()
		self.__live_module = InProcessLiveModule(self, '__live__')

	def shutdown(self):
		self.__module_finder.unload_all_modules()
		self.__module_finder.uninstall_hooks()
		super(InProcessKernel, self).shutdown()

	def new_execution_result(self):
		return InProcessExecutionResult()

	def get_live_module(self):
		return self.__live_module


	def set_module_source(self, fullname, source):
		self.__module_finder.set_module_source(fullname, InProcModuleSource(source))

	def remove_module(self, fullname):
		self.__module_finder.remove_module(fullname)

	def is_in_process(self):
		return True


class InProcessContext (python_kernel.AbstractPythonContext):
	def __init__(self):
		self.__kernels = []

	def start_kernel(self, on_kernel_started):
		"""
		Start an in-process kernel
		"""
		kernel = InProcessKernel(self)
		on_kernel_started(kernel)
		self.__kernels.append(kernel)



	def _notify_kernel_shutdown(self, kernel):
		self.__kernels.remove(kernel)




class InProcessExecutionResult (execution_result.AbstractExecutionResult):
	def __init__(self, streams=None):
		super(InProcessExecutionResult, self).__init__(streams)
		self._caught_exception = None
		self._result_in_tuple = None
		self._execution_count = None


	def clear(self):
		super(InProcessExecutionResult, self).clear()
		self._caught_exception = None
		self._result_in_tuple = None
		self._execution_count = None
		self._incr.onChanged()


	@property
	def caught_exception(self):
		return self._caught_exception

	def set_error(self, error):
		self._caught_exception = error
		self._incr.onChanged()

	def has_errors(self):
		return self._caught_exception is not None  or  self._streams.has_content_for( 'err' )


	def has_result(self):
		return self._result_in_tuple is not None

	@property
	def result(self):
		return self._result_in_tuple[0]   if self._result_in_tuple is not None   else None

	def set_result(self, result):
		self._result_in_tuple = (result,)
		self._incr.onChanged()
		super(InProcessExecutionResult, self).set_result(result)


	def was_aborted(self):
		return False


	def errorsOnly(self):
		res = InProcessExecutionResult(self._streams.suppress_stream( 'out' ))
		if self._caught_exception is not None:
			res.set_error(self._caught_exception)
		return res



	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return execution_pres.execution_result_box( self._streams, self._caught_exception, self._result_in_tuple,
							    bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult, self._execution_count )


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return execution_pres.minimal_execution_result_box( self._streams, self._caught_exception, self._result_in_tuple,
								    bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )





def _executeWithinModuleIntoResult(result, pythonCode, module, bEvaluate, execution_count=None):
	evalCode = execCode = None
	if bEvaluate:
		try:
			execCode, evalCode = CodeGenerator.compileForModuleExecutionAndEvaluation( module, pythonCode, module.__name__ )
		except:
			result.set_error(JythonException.getCurrentException())
	else:
		try:
			execCode = CodeGenerator.compileForModuleExecution( module, pythonCode, module.__name__ )
		except:
			result.set_error(JythonException.getCurrentException())

	if execCode is not None  or  evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = result.streams.stdout
		sys.stderr = result.streams.stderr
		setattr( module, 'display', result.streams.stdout.display )
		setattr( module, 'displayerr', result.streams.stderr.display )

		try:
			exec execCode in module.__dict__
			if evalCode is not None:
				result.set_result(eval( evalCode, module.__dict__ ))
		except:
			result.set_error(JythonException.getCurrentException())

		sys.stdout, sys.stderr = savedStdout, savedStderr


def getResultOfExecutingWithinModule(pythonCode, module, bEvaluate, execution_count=None):
	result = InProcessExecutionResult()
	_executeWithinModuleIntoResult(result, pythonCode, module, bEvaluate, execution_count)
	return result


def getResultOfExecutionInScopeWithinModule(pythonCode, globals, locals, module, bEvaluate, execution_count=None):
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
	return InProcessExecutionResult( std, caughtException, result, execution_count )





def _evaluateWithinModuleIntoResult(result, pythonExpr, module):
	evalCode = None
	if isinstance(pythonExpr, str)  or  isinstance(pythonExpr, unicode):
		evalCode = pythonExpr
	else:
		try:
			evalCode = CodeGenerator.compileForModuleEvaluation( module, pythonExpr, module.__name__ )
		except:
			result.set_error(JythonException.getCurrentException())

	if evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = result.streams.stdout
		sys.stderr = result.streams.stderr

		try:
			result.set_result(eval( evalCode, module.__dict__ ))
		except:
			result.set_error(JythonException.getCurrentException())

		sys.stdout, sys.stderr = savedStdout, savedStderr


def getResultOfEvaluationWithinModule(pythonExpr, module):
	result = InProcessExecutionResult()
	_evaluateWithinModuleIntoResult(result, pythonExpr, module)
	return result



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