##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import deepcopy

from BritefuryJ.Command import *

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.ObjectPres import *

from BritefuryJ.Editor.Table.ObjectList import AttributeColumn, ObjectListTableEditor

from Britefury.Util.TrackedList import TrackedListProperty

import GSymCore.Languages.Python25.Schema as Py
from GSymCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedStatementAction
from GSymCore.Languages.Python25.Python25 import EmbeddedPython25
from GSymCore.Languages.Python25.PythonEditor.View import perspective as pyPerspective



class ConstantDefinition (object):
	def __init__(self):
		self._target = EmbeddedPython25.target()
		self._value = EmbeddedPython25.expression()
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		
	def __getstate__(self):
		return { 'target' : self._target, 'value' : self._value }
	
	def __setstate__(self, state):
		self._target = state['target']
		self._value = state['value']
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
	
	
	def __get_trackable_contents__(self):
		return [ self.target, self.value ]
	
	
	def __copy__(self):
		d = ConstantDefinition()
		d.target = self.target
		d.value = self.value
		return d
	
	
	def __deepcopy__(self, memo):
		d = ConstantDefinition()
		d.target = deepcopy( self.target, memo )
		d.value = deepcopy( self.value, memo )
		return d
	
	
	def _getTarget(self):
		self._incr.onAccess()
		return self._target

	def _setTarget(self, target):
		self._target = deepcopy( target )
		self._incr.onChanged()

		
	def _getValue(self):
		self._incr.onAccess()
		return self._value

	def _setValue(self, value):
		self._value = deepcopy( value )
		self._incr.onChanged()
	
	
	target = property( _getTarget, _setTarget )
	value = property( _getValue, _setValue )

	
	
	
class ConstantTable (object):
	def __init__(self):
		self._definitions_ = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		
	def __getstate__(self):
		return { 'definitions' : self._definitions_ }
	
	def __setstate__(self, state):
		self._definitions_ = state['definitions']
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
	
	
	def __get_trackable_contents__(self):
		return self._definitions.__get_trackable_contents__()
	
	
	
	def __len__(self):
		self._incr.onAccess()
		return len( self._definitions )
	
	def __iter__(self):
		self._incr.onAccess()
		return iter( self._definitions )
	
	def __getitem__(self, i):
		self._incr.onAccess()
		return self._definitions[i]
	
	def __setitem__(self, i, x):
		self._definitions[i] = x
		self._incr.onChanged()
	
	def __delitem__(self, i):
		del self._definitions[i]
		self._incr.onChanged()
		
	def append(self, x):
		self._definitions.append( x )
		self._incr.onChanged()
		
		
	def __py_model__(self):
		assigns = []
		for definition in self._definitions_:
			target = definition.target.model['target']
			value = definition.value.model['expr']
			assign = Py.AssignStmt( targets=[ target ], value=value )
			assigns.append( assign )
		return Py.PythonSuite( suite=assigns )
	
	__py_hide_expansion__ = True
	
	__py_model_type__ = 'stmt'
		
	
	def __present__(self, fragment, inheritedState):
		return _tableEditor.editTable( self )

	
	_definitions = TrackedListProperty( '_definitions_' )



def _targetFromText(text):
	return EmbeddedPython25.targetFromText( text )

def _defaultTarget():
	return EmbeddedPython25.target()

def _valueFromText(text):
	return EmbeddedPython25.expressionFromText( text )

def _defaultValue():
	return EmbeddedPython25.expression()


_targetColumn = AttributeColumn( 'Name', 'target', _targetFromText, _defaultTarget )
_valueColumn = AttributeColumn( 'Value', 'value', _valueFromText, _defaultValue )

_tableEditor = ObjectListTableEditor( [ _targetColumn, _valueColumn ], ConstantDefinition, True, True )



def _newConstantTable():
	return ConstantTable()


_ctCommand = Command( '&Python &Constant &Table', makeInsertEmbeddedStatementAction( _newConstantTable ) )

_ctCommands = CommandSet( 'GSymTools.PythonTools.ConstantTable', [ _ctCommand ] )

pythonCommands.registerCommandSet( _ctCommands )
