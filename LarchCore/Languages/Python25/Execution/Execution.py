##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import sys

from java.lang import Throwable

from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder

from BritefuryJ.Util import InvokePyFunction

from LarchCore.Languages.Python25 import CodeGenerator


class _OutputStream (object):
	def __init__(self):
		self._builder = None
		
	def write(self, text):
		if not ( isinstance( text, str )  or  isinstance( text, unicode ) ):
			raise TypeError, 'argument 1 must be string, not %s' % type( text )
		if self._builder is None:
			self._builder = StreamValueBuilder()
		self._builder.appendTextValue( text )
	
	def display(self, value):
		if self._builder is None:
			self._builder = StreamValueBuilder()
		self._builder.appendStructuralValue( value )
		
		
	def getStream(self):
		return self._builder.stream()   if self._builder is not None   else None

		
		
class ExecutionResult (object):
	def __init__(self, stdout, stderr, caughtException, result=None):
		super( ExecutionResult, self ).__init__()
		self._stdout = stdout
		self._stderr = stderr
		self._caughtException = caughtException
		self._result = result
		
		
		
	def getStdOut(self):
		return self._stdout
	
	def getStdErr(self):
		return self._stderr
	
	def getCaughtException(self):
		return self._caughtException
	
	def getResult(self):
		return self._result



	
	
	
def executePythonModule(pythonModule, module, bEvaluate):
	stdout = _OutputStream()
	stderr = _OutputStream()
	
	def _compileForEval():
		return CodeGenerator.compileForModuleExecutionAndEvaluation( module, pythonModule, module.__name__ )
	
	def _compileForExec():
		return CodeGenerator.compileForModuleExecution( module, pythonModule, module.__name__ )
	
	evalCode = execCode = None
	caughtException = None
	result = None
	if bEvaluate:
		r, caughtException = InvokePyFunction.invoke( _compileForEval )
		if r is not None:
			execCode, evalCode = r
	else:
		execCode, caughtException = InvokePyFunction.invoke( _compileForExec )
	
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

		result, caughtException = InvokePyFunction.invoke( _exec )
	
		sys.stdout, sys.stderr = savedStdout, savedStderr
	
	return ExecutionResult( stdout.getStream(), stderr.getStream(), caughtException, result )
	