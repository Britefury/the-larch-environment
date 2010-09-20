##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import StringBuilder

import sys
import imp
from copy import copy

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25 import CodeGenerator
from GSymCore.Languages.Python25 import Schema as PySchema
from GSymCore.Languages.Python25.Execution import Execution
from GSymCore.Languages.Python25 import Prelude


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


			
	def __init__(self, name):
		self._incr = IncrementalValueMonitor( self )
		
		self._blocks = []
		self._currentPythonModule = Python25.py25NewModule()
		self._before = []
		self._after = []
		self._module = imp.new_module( name )
		Prelude.executePrelude( self._module )
		
		
	def getBlocks(self):
		self._incr.onAccess()
		return copy( self._blocks )
	
	def getCurrentPythonModule(self):
		self._incr.onAccess()
		return self._currentPythonModule
	
		
	def commit(self, execResult):
		self._blocks.append( ConsoleBlock( self._currentPythonModule, execResult ) )
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
		
		
	def assignVariable(self, name, value):
		setattr( self._module, name, value )
		self._blocks.append( ConsoleVarAssignment( name, type( value ) ) )
		self._incr.onChanged()
		
		
		
	def execute(self, bEvaluate):
		module = self.getCurrentPythonModule()
		if module != Python25.py25NewModule():
			execResult = Execution.executePythonModule( module, self._module, bEvaluate )
			self.commit( execResult )
					
		
		
	
class ConsoleBlock (IncrementalOwner):
	def __init__(self, pythonModule, execResult):
		self._incr = IncrementalValueMonitor( self )
		
		self._pythonModule = pythonModule
		self._execResult = execResult
		
		
		
	def getPythonModule(self):
		self._incr.onAccess()
		return self._pythonModule
	
	def getExecResult(self):
		self._incr.onAccess()
		return self._execResult



class ConsoleVarAssignment (object):
	def __init__(self, varName, valueType):
		self._varName = varName
		self._valueType = valueType
	
	
	def getVarName(self):
		return self._varName
	
	def getValueType(self):
		return self._valueType

		
	
		
