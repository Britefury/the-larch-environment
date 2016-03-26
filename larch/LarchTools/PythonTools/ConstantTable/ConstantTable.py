##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from copy import deepcopy

from BritefuryJ.Command import Command

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Editor.Table.ObjectList import AttributeColumn, ObjectListTableEditor

from Britefury.Util.TrackedList import TrackedListProperty

import LarchCore.Languages.Python2.Schema as Py
from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedStatementAtCaretAction
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Target, EmbeddedPython2Expr



class ConstantDefinition (object):
	def __init__(self):
		self._target = EmbeddedPython2Target()
		self._value = EmbeddedPython2Expr()
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
		
		
	def __py_execmodel__(self, codeGen):
		assigns = []
		for definition in self._definitions_:
			target = definition.target.model['target']
			value = definition.value.model['expr']
			assign = Py.AssignStmt( targets=[ target ], value=value )
			assigns.append( assign )
		return Py.PythonSuite( suite=assigns )
	
	__py_hide_expansion__ = True
	
		
	
	def __present__(self, fragment, inheritedState):
		return _tableEditor.editTable( self )


	@TrackedListProperty
	def _definitions(self):
		return self._definitions_




_targetColumn = AttributeColumn( 'Name', 'target', EmbeddedPython2Target )
_valueColumn = AttributeColumn( 'Value', 'value', EmbeddedPython2Expr )

_tableEditor = ObjectListTableEditor( [ _targetColumn, _valueColumn ], ConstantDefinition, False, True, True, True )



@EmbeddedStatementAtCaretAction
def _newConstantTableAtCaret(caret):
	return ConstantTable()


_ctCommand = Command( '&Python &Constant &Table', _newConstantTableAtCaret )

pythonCommandSet( 'LarchTools.PythonTools.ConstantTable', [ _ctCommand ] )
