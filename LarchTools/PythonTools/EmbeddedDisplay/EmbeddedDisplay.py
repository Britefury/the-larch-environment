##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.awt import Color

from java.util.regex import Pattern

from copy import deepcopy

from BritefuryJ.Command import *

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.ObjectPres import *

from BritefuryJ.StyleSheet import *

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAtCaretAction, makeWrapSelectionInEmbeddedExpressionAction,	\
	makeWrapSelectedStatementRangeInEmbeddedObjectAction, chainActions
from LarchCore.Languages.Python25.Python25 import EmbeddedPython25
from LarchCore.Languages.Python25.PythonEditor.PythonEditOperations import getSelectedExpression, pyReplaceNode
from LarchCore.Languages.Python25.PythonEditor.View import perspective as pyPerspective



class EmbeddedDisplay (object):
	def __init__(self):
		self._expr = EmbeddedPython25.expression()
		self._code = None
		self._values = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		
	def __getstate__(self):
		return { 'expr' : self._expr }
	
	def __setstate__(self, state):
		self._expr = state['expr']
		self._code = None
		self._values = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
	
	
	def __get_trackable_contents__(self):
		return [ self._expr ]
		
		
	def __py_compile_visit__(self, codeGen):
		self._code = codeGen.compileForEvaluation( self._expr.model )
	
	def __py_eval__(self, _globals, _locals, codeGen):
		value = eval( self._code, _globals, _locals )
		self._values.append( value )
		self._incr.onChanged()
		return value
	
	def __py_replacement__(self):
		return deepcopy( self._expr.model['expr'] )
		
	
	def __present__(self, fragment, inheritedState):
		def _embeddedDisplayMenu(element, menu):
			def _onClear(item):
				del self._values[:]
				self._incr.onChanged()
			
			menu.add( MenuItem.menuItemWithLabel( 'Clear collected values', _onClear ) )
			
			return False
				
		
		
		self._incr.onAccess()
		#exprPres = pyPerspective.applyTo( self._expr )
		exprPres = self._expr
		
		valuesPres = ObjectBox( 'Values', Column( [ Paragraph( [ value ] )   for value in self._values ] ) )
		
		contents = Column( [ exprPres, valuesPres ] )
		return ObjectBox( 'Embedded display', contents ).withContextMenuInteractor( _embeddedDisplayMenu )



	
class _Frame (object):
	def __init__(self):
		self.values = []
		self.childFrames = []


		
class EmbeddedSuiteDisplay (object):
	def __init__(self):
		self._suite = EmbeddedPython25.suite()
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		self._namedValues = []
		self._rootFrames = []
		self._currentFrame = None
#		self._sequentialFrames = 
		
		
	def __getstate__(self):
		return { 'suite' : self._suite }
	
	def __setstate__(self, state):
		self._suite = state['suite']
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		self._namedValues = []
		self._rootFrames = []
		self._currentFrame = None
	
	
	def __get_trackable_contents__(self):
		return [ self._suite ]
		
		
	def _clearNamedValues(self):
		self._namedValues = []
	
	def _registerNamedValue(self, namedValue):
		self._namedValues.append( namedValue )
	
	
	def __py_compile_visit__(self, codeGen):
		self._clearNamedValues()
		
		prevSuite = NamedValue._currentSuite
		NamedValue._currentSuite = self
		
		self._code = codeGen.compileForEvaluation( self._suite.model )
		
		NamedValue._currentSuite = prevSuite
	
		
	def __py_exec__(self, _globals, _locals, codeGen):
		# Open a new frame
		prevFrame = self._currentFrame
		
		self._currentFrame = _Frame()
		if prevFrame is not None:
			prevFrame.childFrames.append( self._currentFrame )
		else:
			self._rootFrames.append( self._currentFrame )
		
		exec self._code in _globals, _locals
		
		self._currentFrame = prevFrame
		self._incr.onChanged()
		
	
	def __py_replacement__(self):
		return deepcopy( self._suite.model['suite'] )
		
	
	def _logValue(self, namedValue, value):
		if self._currentFrame is not None:
			self._currentFrame.values.append( namedValue, value )

	def _clear(self):
		self._rootFrames = []
		self._currentFrame = None
		self._incr.onChanged()
	
	
	def __present__(self, fragment, inheritedState):
		def _embeddedDisplayMenu(element, menu):
			def _onClear(item):
				self._clear()
			
			menu.add( MenuItem.menuItemWithLabel( 'Clear collected values', _onClear ) )
			
			return False
				
		
		
		self._incr.onAccess()
		suitePres = self._suite
		
		valuesPres = TabbedBox( [ [ Label( 'Table' ), Blank() ], [ Label( 'Trace' ), Blank() ] ], None )
		
		contents = Column( [ suitePres, valuesPres ] )
		return ObjectBox( 'Embedded suite display', contents ).withContextMenuInteractor( _embeddedDisplayMenu ).withCommands( _nvCommands )




class NamedValue (object):
	_currentSuite = None
	
	def __init__(self):
		self._name = 'value'
		self._expr = EmbeddedPython25.expression()
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		
	def __getstate__(self):
		return { 'name' : self._name,  'expr' : self._expr }
	
	def __setstate__(self, state):
		self._name = state['name']
		self._expr = state['expr']
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
	
	
	def __get_trackable_contents__(self):
		return [ self._name, self._expr ]
		
		
	def __py_compile_visit__(self, codeGen):
		self._code = codeGen.compileForEvaluation( self._expr.model )
		if self._currentSuite is not None:
			self._currentSuite._registerNamedValue( self )
	
	
	def __py_eval__(self, _globals, _locals, codeGen):
		value = eval( self._code, _globals, _locals )
		if self._currentSuite is not None:
			self._currentSuite._logValue( self, value )
		return value
	
	def __py_replacement__(self):
		return deepcopy( self._expr.model['expr'] )
		
	
	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()
		
		def _setName(editableLabel, text):
			self._name = text
			self._incr.onChanged()
			

		namePres = EditableLabel( self._name, self._nameNotSetStyle( Label( '<not set>' ) ), _setName, Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' ), 'Please enter a valid identifier' )
		nameLabel = Label( 'Name: ' )
		name = Row( [ nameLabel, namePres ] )
		
		exprPres = self._expr
		
		contents = Column( [ name, exprPres ] )
		return ObjectBox( 'Named value', contents )
		
	_nameNotSetStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.5, 0.0, 0.0 ) ).withAttr( Primitive.fontItalic, True )

	
	
def _newNamedValueAtCaret(caret):
	return NamedValue()

def _newNamedValueAtSelection(expr, selection):
	d = NamedValue()
	d._expr.model['expr'] = deepcopy( expr )
	return d

_exprNamedValueAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newNamedValueAtCaret )
_exprNamedValueAtSelection = makeWrapSelectionInEmbeddedExpressionAction( _newNamedValueAtSelection )

_nvCommand = Command( '&Named &Value', chainActions( _exprNamedValueAtSelection, _exprNamedValueAtCaret ) )

_nvCommands = CommandSet( 'LarchTools.PythonTools.EmbeddedDisplay.NamedValue', [ _nvCommand ] )

	


def _newEmbeddedDisplayAtCaret(caret):
	return EmbeddedDisplay()

def _newEmbeddedDisplayAtSelection(expr, selection):
	d = EmbeddedDisplay()
	d._expr.model['expr'] = deepcopy( expr )
	return d

def _newEmbeddedSuiteDisplayAtStatementRange(statements, selection):
	d = EmbeddedSuiteDisplay()
	d._suite.model['suite'][:] = deepcopy( statements )
	return d

_exprAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newEmbeddedDisplayAtCaret )
_exprAtSelection = makeWrapSelectionInEmbeddedExpressionAction( _newEmbeddedDisplayAtSelection )

_stmtRangeAtSelection = makeWrapSelectedStatementRangeInEmbeddedObjectAction( _newEmbeddedSuiteDisplayAtStatementRange )


_edCommand = Command( '&Embedded &Display', chainActions( _exprAtSelection, _exprAtCaret ) )
_esdCommand = Command( '&Embedded &Suite &Display', chainActions( _stmtRangeAtSelection ) )

_edCommands = CommandSet( 'LarchTools.PythonTools.EmbeddedDisplay', [ _edCommand, _esdCommand ] )

pythonCommands.registerCommandSet( _edCommands )
