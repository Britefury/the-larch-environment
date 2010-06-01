##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import StringBuilder

import sys
from copy import copy

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25 import CodeGenerator
from GSymCore.Languages.Python25 import Schema as PySchema


_codeGen = CodeGenerator.Python25CodeGenerator()


class Console (IncrementalOwner):
	class Output (object):
		def __init__(self):
			self._builder = None
			
		def write(self, text):
			if not ( isinstance( text, str )  or  isinstance( text, unicode ) ):
				raise TypeError, 'argument 1 must be string, not %s' % type( text )
			if self._builder is None:
				self._builder = StringBuilder()
			self._builder.append( text )
			
		def getText(self):
			if self._builder is not None:
				return self._builder.toString()
			else:
				return None


			
	def __init__(self):
		self._incr = IncrementalValueMonitor( self )
		
		self._blocks = []
		self._currentPythonModule = Python25.py25NewModule()
		self._before = []
		self._after = []
		self._globalVars = {}
		
		
	def getBlocks(self):
		self._incr.onAccess()
		return copy( self._blocks )
	
	def getCurrentPythonModule(self):
		self._incr.onAccess()
		return self._currentPythonModule
	
		
	def commit(self, outText, errText, caughtException, result=None):
		self._blocks.append( ConsoleBlock( self._currentPythonModule, outText, errText, caughtException, result ) )
		blank = Python25.py25NewModule()
		for a in self._after:
			if a != blank:
				self._before.append( a )
		if self._currentPythonModule != blank:
			self._before.append( self._currentPythonModule.deepCopy() )
		self._after = []
		self._currentPythonModule = Python25.py25NewModule()
		self._incr.onChanged()
	
	def backwards(self):
		if len( self._before ) > 0:
			self._after.insert( 0, self._currentPythonModule )
			self._currentPythonModule = self._before.pop()
			self._incr.onChanged()
			
	def forwards(self):
		if len( self._after ) > 0:
			self._before.append( self._currentPythonModule )
			self._currentPythonModule = self._after[0]
			del self._after[0]
			self._incr.onChanged()
		
		
	def setGlobalVar(self, name, value):
		self._globalVars[name] = value
		
		
	def _initStdOutErr(self):
		stdout, stderr = sys.stdout, sys.stderr
		sys.stdout = self.Output()
		sys.stderr = self.Output()
		return stdout, stderr

	def _shutdownStdOurErr(self, stdout, stderr):
		outout, outerr = sys.stdout, sys.stderr
		sys.stdout, sys.stderr = stdout, stderr
		return outout, outerr
	
		
		
	def execute(self, bEvaluate):
		module = self.getCurrentPythonModule()
		if module != Python25.py25NewModule():
			if bEvaluate:
				try:
					execCode, evalCode = CodeGenerator.compileForExecutionAndEvaluation( module, '<console>' )
				except CodeGenerator.Python25CodeGeneratorError:
					print 'Code generation error'
					execCode = None
					evalCode = None
			else:
				try:
					execCode = CodeGenerator.compileForExecution( module, '<console>' )
				except CodeGenerator.Python25CodeGeneratorError:
					print 'Code generation error'
					execCode = None
				
			if execCode is not None:
				caughtException = None
				stdout, stderr = self._initStdOutErr()
				try:
					exec execCode in self._globalVars
					if evalCode is not None:
						result = [ eval( evalCode, self._globalVars ) ]
					else:
						result = None
				except Exception, exc:
					caughtException = exc
					result = None
				outout, outerr = self._shutdownStdOurErr( stdout, stderr )
				self.commit( outout.getText(), outerr.getText(), caughtException, result )
				return
					
		
		
	
class ConsoleBlock (IncrementalOwner):
	def __init__(self, pythonModule, stdout, stderr, caughtException, result=None):
		self._incr = IncrementalValueMonitor( self )
		
		self._pythonModule = pythonModule
		self._stdout = stdout
		self._stderr = stderr
		self._caughtException = caughtException
		self._result = result
		
		
		
	def getPythonModule(self):
		self._incr.onAccess()
		return self._pythonModule
	
	def getStdOut(self):
		self._incr.onAccess()
		return self._stdout
	
	def getStdErr(self):
		self._incr.onAccess()
		return self._stderr
	
	def getCaughtException(self):
		self._incr.onAccess()
		return self._caughtException
	
	def getResult(self):
		self._incr.onAccess()
		return self._result
		
	
		
