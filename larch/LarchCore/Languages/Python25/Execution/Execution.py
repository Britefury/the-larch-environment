##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import sys

from java.lang import Throwable

from BritefuryJ.Util.RichString import RichStringBuilder

from BritefuryJ.Util.Jython import JythonException

from LarchCore.Languages.Python25 import CodeGenerator
from LarchCore.Languages.Python25.Execution import ExecutionPresCombinators


class _OutputStream (object):
	def __init__(self):
		self._builder = None
		
	def write(self, text):
		if not ( isinstance( text, str )  or  isinstance( text, unicode ) ):
			raise TypeError, 'argument 1 must be string, not %s' % type( text )
		if self._builder is None:
			self._builder = RichStringBuilder()
		self._builder.appendTextValue( text )
	
	def display(self, value):
		if self._builder is None:
			self._builder = RichStringBuilder()
		self._builder.appendStructuralValue( value )
		
		
	def getRichString(self):
		return self._builder.richString()   if self._builder is not None   else None

		
		
class ExecutionResult (object):
	def __init__(self, stdout, stderr, caughtException, result=None):
		super( ExecutionResult, self ).__init__()
		self._stdout = stdout
		self._stderr = stderr
		self._caughtException = caughtException
		self._result = result


	def suppressStdOut(self):
		return ExecutionResult( None, self._stderr, self._caughtException, self._result )

	def suppressStdErr(self):
		return ExecutionResult( self._stdout, None, self._caughtException, self._result )

	def suppressCaughtException(self):
		return ExecutionResult( self._stdout, self._stderr, None, self._result )

	def suppressResult(self):
		return ExecutionResult( self._stdout, self._stderr, self._caughtException, None )

		
		
	def getStdOutStream(self):
		return self._stdout
	
	def getStdErrStream(self):
		return self._stderr

	def getCaughtException(self):
		return self._caughtException
	
	def getResult(self):
		return self._result


	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return ExecutionPresCombinators.executionResultBox( self._stdout, self._stderr, self._caughtException, self._result, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return ExecutionPresCombinators.minimalExecutionResultBox( self._stdout, self._stderr, self._caughtException, self._result, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )



def getResultOfExecutionWithinModule(pythonCode, module, bEvaluate):
	stdout = _OutputStream()
	stderr = _OutputStream()

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
		sys.stdout = stdout
		sys.stderr = stderr
		setattr( module, 'display', stdout.display )
		setattr( module, 'displayerr', stderr.display )

		def _exec():
			exec execCode in module.__dict__
			if evalCode is not None:
				return [ eval( evalCode, module.__dict__ ) ]
			else:
				return None

		try:
			exec execCode in module.__dict__
			if evalCode is not None:
				result = [ eval( evalCode, module.__dict__ ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr

	return ExecutionResult( stdout.getRichString(), stderr.getRichString(), caughtException, result )


def getResultOfExecutionInScopeWithinModule(pythonCode, globals, locals, module, bEvaluate):
	stdout = _OutputStream()
	stderr = _OutputStream()

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
		sys.stdout = stdout
		sys.stderr = stderr
		setattr( module, 'display', stdout.display )
		setattr( module, 'displayerr', stderr.display )

		def _exec():
			exec execCode in globals, locals
			if evalCode is not None:
				return [ eval( evalCode, globals, locals ) ]
			else:
				return None

		try:
			exec execCode in globals, locals
			if evalCode is not None:
				result = [ eval( evalCode, globals, locals ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr

	return ExecutionResult( stdout.getRichString(), stderr.getRichString(), caughtException, result )





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