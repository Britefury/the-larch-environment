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

from GSymCore.Languages.Python25 import CodeGenerator


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
	if bEvaluate:
		execCode, evalCode = CodeGenerator.compileForExecutionAndEvaluation( pythonModule, module.__name__ )
	else:
		execCode = CodeGenerator.compileForExecution( pythonModule, module.__name__ )
		evalCode = None
		
	caughtException = None
	
	savedStdout, savedStderr = sys.stdout, sys.stderr
	stdout = _OutputStream()
	stderr = _OutputStream()
	sys.stdout = stdout
	sys.stderr = stderr
	setattr( module, 'display', stdout.display )
	setattr( module, 'displayerr', stderr.display )
	
	try:
		exec execCode in module.__dict__
		if evalCode is not None:
			result = [ eval( evalCode, module.__dict__ ) ]
		else:
			result = None
	except Exception, exc:
		caughtException = exc
		result = None
	except Throwable, exc:
		caughtException = exc
		result = None
	
	sys.stdout, sys.stderr = savedStdout, savedStderr
	
	return ExecutionResult( stdout.getStream(), stderr.getStream(), caughtException, result )
	