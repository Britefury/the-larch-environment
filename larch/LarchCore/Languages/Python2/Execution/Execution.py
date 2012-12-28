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

from LarchCore.Languages.Python2 import CodeGenerator
from LarchCore.Languages.Python2.Execution import ExecutionPresCombinators


class RichStream (object):
	def __init__(self, name):
		self.name = name
		self.__builder = None
		
	def write(self, text):
		if not ( isinstance( text, str )  or  isinstance( text, unicode ) ):
			raise TypeError, 'argument 1 must be string, not %s' % type( text )
		self._builder.appendTextValue( text )

	def display(self, value):
		self._builder.appendStructuralValue( value )


	@property
	def _builder(self):
		if self.__builder is None:
			self.__builder = RichStringBuilder()
		return self.__builder
		

	@property
	def richString(self):
		return self.__builder.richString()   if self.__builder is not None   else None



class MultiplexedRichStream (object):
	class _SingleStream (object):
		def __init__(self, multiplexedStream, name):
			self.name = name
			self.__multi = multiplexedStream
			self.__stream = RichStream( name )

		def write(self, text):
			self.__stream.write( text )
			self.__multi._write( self.name, text )

		def display(self, value):
			self.__stream.display( value )
			self.__multi._display( self.name, value )

		@property
		def richString(self):
			return self.__stream.richString


	def __init__(self, streamNames):
		self.__streamsByName = { name : self._SingleStream( self, name )   for name in streamNames }
		self.__multiplexed = []


	def __getattr__(self, item):
		try:
			return self.__streamsByName[item]
		except KeyError:
			raise AttributeError, 'No stream named {0}'.format( item )


	def __iter__(self):
		return iter( self.__multiplexed )

	def __getitem__(self, item):
		return self.__multiplexed[item]

	def __len__(self):
		return len( self.__multiplexed )


	def suppressStream(self, name):
		result = MultiplexedRichStream([])
		for n, stream in self.__streamsByName.items():
			if n != name:
				result.__streamsByName[n] = stream
		for s in self.__multiplexed:
			if s.name != name:
				result.__multiplexed.append( s )
		return result


	def hasContentFor(self, name):
		for s in self.__multiplexed:
			if s.name == name:
				return True
		return False


	def _write(self, streamName, text):
		stream = self.__multiplexedForName(streamName)
		stream.write( text )


	def _display(self, streamName, value):
		stream = self.__multiplexedForName(streamName)
		stream.display( value )


	def __multiplexedForName(self, name):
		if len( self.__multiplexed ) > 0:
			top = self.__multiplexed[-1]
			if top.name == name:
				return top
		stream = RichStream( name )
		self.__multiplexed.append( stream )
		return stream




class ExecutionResult (object):
	def __init__(self, streams, caughtException, result=None):
		super( ExecutionResult, self ).__init__()
		self._streams = streams
		self._caughtException = caughtException
		self._result = result


	def suppressStdOut(self):
		return ExecutionResult( self._streams.suppressStream( 'out' ), self._caughtException, self._result )

	def suppressStdErr(self):
		return ExecutionResult( self._streams.suppressStream( 'err' ), self._caughtException, self._result )

	def suppressCaughtException(self):
		return ExecutionResult( self._streams, None, self._result )

	def suppressResult(self):
		return ExecutionResult( self._streams, self._caughtException, None )

	def errorsOnly(self):
		return ExecutionResult( self._streams.suppressStream( 'out' ), self._caughtException, None )

		
		
	def getStreams(self):
		return self._streams
	
	def getCaughtException(self):
		return self._caughtException
	
	def getResult(self):
		return self._result


	def hasErrors(self):
		return self._caughtException is not None  or  self._streams.hasContentFor( 'err' )


	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return ExecutionPresCombinators.executionResultBox( self._streams, self._caughtException, self._result, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		return ExecutionPresCombinators.minimalExecutionResultBox( self._streams, self._caughtException, self._result, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )



def getResultOfExecutionWithinModule(pythonCode, module, bEvaluate):
	std = MultiplexedRichStream( [ 'out', 'err' ] )

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
		sys.stdout = std.out
		sys.stderr = std.err
		setattr( module, 'display', std.out.display )
		setattr( module, 'displayerr', std.err.display )

		try:
			exec execCode in module.__dict__
			if evalCode is not None:
				result = [ eval( evalCode, module.__dict__ ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return ExecutionResult( std, caughtException, result )


def getResultOfExecutionInScopeWithinModule(pythonCode, globals, locals, module, bEvaluate):
	std = MultiplexedRichStream( [ 'out', 'err' ] )

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
		sys.stdout = std.out
		sys.stderr = std.err
		setattr( module, 'display', std.out.display )
		setattr( module, 'displayerr', std.err.display )

		try:
			exec execCode in globals, locals
			if evalCode is not None:
				result = [ eval( evalCode, globals, locals ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return ExecutionResult( std, caughtException, result )





def getResultOfEvaluationWithinModule(pythonExpr, module):
	std = MultiplexedRichStream( [ 'out', 'err' ] )

	evalCode = None
	caughtException = None
	result = None
	try:
		evalCode = CodeGenerator.compileForModuleEvaluation( module, pythonExpr, module.__name__ )
	except:
		caughtException = JythonException.getCurrentException()

	if evalCode is not None:
		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = std.out
		sys.stderr = std.err

		try:
			result = [ eval( evalCode, module.__dict__ ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return ExecutionResult( std, caughtException, result )


def getResultOfEvaluationInScopeWithinModule(pythonExpr, globals, locals, module):
	std = MultiplexedRichStream( [ 'out', 'err' ] )

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
		sys.stdout = std.out
		sys.stderr = std.err

		try:
			result = [ eval( evalCode, globals, locals ) ]
		except:
			caughtException = JythonException.getCurrentException()

		sys.stdout, sys.stderr = savedStdout, savedStderr
	return ExecutionResult( std, caughtException, result )





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