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

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValue

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25 import CodeGenerator
from GSymCore.Languages.Python25 import Schema as PySchema


_codeGen = CodeGenerator.Python25CodeGenerator()


class Terminal (IncrementalOwner):
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
		self._incr = IncrementalValue( self )
		
		self._blocks = []
		self._currentPythonModule = Python25.py25NewModule()
		self._globalVars = {}
		
		
	def getBlocks(self):
		self._incr.onLiteralAccess()
		return copy( self._blocks )
	
	def addBlock(self, block):
		self._blocks.append( block )
		self._incr.onChanged()
		
	
	def getCurrentPythonModule(self):
		self._incr.onLiteralAccess()
		return self._currentPythonModule
	
	def setCurrentPythonModule(self, m):
		self._currentPythonModule = m
		self._incr.onChanged()
		
		
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
		
		if bEvaluate:
			execModule = None
			evalExpr = None
			for i, stmt in reversed( list( enumerate( module['suite'] ) ) ):
				if stmt.isInstanceOf( PySchema.ExprStmt ):
					execModule = PySchema.PythonModule( suite=module['suite'][:i] )
					evalExpr = stmt['expr']
					break
				elif stmt.isInstanceOf( PySchema.BlankLine )  or  stmt.isInstanceOf( PySchema.CommentStmt ):
					pass
				else:
					break
			
			if execModule is not None  and  evalExpr is not None:
				try:
					execSource = _codeGen( execModule )
					evalSource = _codeGen( evalExpr )
				except CodeGenerator.Python25CodeGeneratorError:
					print 'Code generation error'
					execSource = None
					evalSource = None
			
				if execSource is not None  and  evalSource is not None:
					caughtException = None
					stdout, stderr = self._initStdOutErr()
					try:
						exec execSource in self._globalVars
						result = [ eval( evalSource, self._globalVars ) ]
					except Exception, exc:
						caughtException = exc
						result = None
					outout, outerr = self._shutdownStdOurErr( stdout, stderr )
					self.addBlock( TerminalBlock( module, outout.getText(), outerr.getText(), caughtException, result ) )
					self.setCurrentPythonModule( Python25.py25NewModule() )
					return

		try:
			source = _codeGen( module )
		except CodeGenerator.Python25CodeGeneratorError:
			print 'Code generation error'
			source = None
		
		if source is not None:
			caughtException = None
			stdout, stderr = self._initStdOutErr()
			try:
				exec source in self._globalVars
			except Exception, exc:
				caughtException = exc
			outout, outerr = self._shutdownStdOurErr( stdout, stderr )
			self.addBlock( TerminalBlock( module, outout.getText(), outerr.getText(), caughtException ) )
			self.setCurrentPythonModule( Python25.py25NewModule() )
		
		
	
class TerminalBlock (IncrementalOwner):
	def __init__(self, pythonModule, stdout, stderr, caughtException, result=None):
		self._incr = IncrementalValue( self )
		
		self._pythonModule = pythonModule
		self._stdout = stdout
		self._stderr = stderr
		self._caughtException = caughtException
		self._result = result
		
		
		
	def getPythonModule(self):
		self._incr.onLiteralAccess()
		return self._pythonModule
	
	def getStdOut(self):
		self._incr.onLiteralAccess()
		return self._stdout
	
	def getStdErr(self):
		self._incr.onLiteralAccess()
		return self._stderr
	
	def getCaughtException(self):
		self._incr.onLiteralAccess()
		return self._caughtException
	
	def getResult(self):
		self._incr.onLiteralAccess()
		return self._result
		
	
		
