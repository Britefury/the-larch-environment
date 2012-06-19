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
from BritefuryJ.Live import LiveValue

from BritefuryJ.Controls import *

from BritefuryJ.LSpace.Interactor import *
from BritefuryJ.Graphics import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.ObjectPres import *

from BritefuryJ.Editor.Table.Generic import *

from BritefuryJ.StyleSheet import *

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedExpressionAtCaretAction, WrapSelectionInEmbeddedExpressionAction,\
	WrapSelectedStatementRangeInEmbeddedObjectAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr, EmbeddedPython2Suite
from LarchCore.Languages.Python2 import Schema



class _TableSchema (object):
	def __init__(self):
		self._monitoredExpressions = []
		self._monitoredExpressionToindex = {}

	def registerMonitoredExpression(self, monitoredExpression):
		index = len( self._monitoredExpressions )
		self._monitoredExpressions.append( monitoredExpression )
		self._monitoredExpressionToindex[monitoredExpression] = index


class _TableView (object):
	def __init__(self):
		self._incr = IncrementalValueMonitor()
		self._schema = None
		self._tableEditor = None
		self._tableContent = None
		self._tableRow = None
		self._numTableRows = None


	def initialise(self, schema):
		self._schema = schema
		self._tableEditor = GenericTableEditor( [ v._name   for v in schema._monitoredExpressions ], True, True, False, False )
		self._tableContent = GenericTableModel( lambda: '', lambda x: x )
		self._tableRow = None
		self._numTableRows = None


	def __getstate__(self):
		return { None : None }


	def __setstate__(self, state):
		self._incr = IncrementalValueMonitor()
		self._schema = None
		self._tableEditor = None
		self._tableContent = None
		self._tableRow = None
		self._numTableRows = None



	def begin(self):
		state = self._tableRow
		self._numTableRows = self._numTableRows + 1   if self._numTableRows is not None   else   0
		self._tableRow = self._numTableRows
		return state

	def end(self, state):
		self._tableRow = state
		self._incr.onChanged()


	def logValue(self, monitoredExpression, value):
		index = self._schema._monitoredExpressionToindex[monitoredExpression]
		self._tableContent.set( index, self._tableRow, value )


	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()
		return self._tableEditor.editTable( self._tableContent )   if self._tableEditor is not None   else   Blank()




class _FrameValues (object):
	def __init__(self):
		self._values = []


	def logValue(self, monitoredExpression, value):
		self._values.append( ( monitoredExpression, value ) )


	def __present__(self, fragment, inheritedState):
		fields = [ VerticalField( monitoredExpression._name, value )   for monitoredExpression, value in self._values ]
		return ObjectBorder( Column( fields ) )



class _FrameInteractor (PushElementInteractor):
	def __init__(self, frame, valuesLive):
		self._frame = frame
		self._valuesLive = valuesLive

	def buttonPress(self, element, event):
		return event.getButton() == 1

	def buttonRelease(self, element, event):
		self._valuesLive.setLiteralValue( self._frame.values )


class _FrameBox (object):
	def __init__(self, frame, valuesLive):
		self._frame = frame
		self._valuesLive = valuesLive

	def __present__(self, fragment, inheritedState):
		interactor = _FrameInteractor( self._frame, self._valuesLive )
		box = Box( 15.0, 5.0 ).withElementInteractor( interactor )
		if self._valuesLive.getValue() is self._frame.values:
			return self._selectedTabFrameStyle.applyTo( box )
		else:
			return self._tabFrameStyle.applyTo( box )

	_tabFrameStyle = StyleSheet.style( Primitive.shapePainter( FilledOutlinePainter( Color( 0.85, 0.9, 0.85 ), Color( 0.6, 0.8, 0.6 ) ) ),
					   Primitive.hoverShapePainter( FilledOutlinePainter( Color( 0.6, 0.8, 0.6 ), Color( 0.0, 0.5, 0.0 ) ) ) )
	_selectedTabFrameStyle = StyleSheet.style( Primitive.shapePainter( FilledOutlinePainter( Color( 1.0, 1.0, 0.85 ), Color( 1.0, 0.8, 0.0 ) ) ),
						   Primitive.hoverShapePainter( FilledOutlinePainter( Color( 1.0, 1.0, 0.6 ), Color( 1.0, 0.5, 0.0 ) ) ) )


class _Frame (object):
	def __init__(self):
		self.values = _FrameValues()
		self.childFrames = []

	def _presentFrameSubtree(self, valuesLive):
		frameBox = _FrameBox( self, valuesLive )
		tab = Pres.coerce( frameBox ).pad( 2.0, 2.0 ).alignVExpand()
		return Row( [ tab, Column( [ x._presentFrameSubtree( valuesLive )   for x in self.childFrames ] ) ] )



class _TreeView (object):
	def __init__(self):
		self._incr = IncrementalValueMonitor()
		self.initialise()



	def __getstate__(self):
		return { None : None }


	def __setstate__(self, state):
		self._incr = IncrementalValueMonitor()
		self.initialise()



	def initialise(self):
		self._incr.onChanged()
		self._rootFrames = []
		self._currentFrame = None



	def begin(self):
		self._incr.onChanged()
		# Open a new frame
		prevFrame = self._currentFrame

		self._currentFrame = _Frame()
		if prevFrame is not None:
			prevFrame.childFrames.append( self._currentFrame )
		else:
			self._rootFrames.append( self._currentFrame )

		return prevFrame


	def end(self, state):
		prevFrame = state
		self._currentFrame = prevFrame


	def logValue(self, monitoredExpression, value):
		self._incr.onChanged()
		if self._currentFrame is not None:
			self._currentFrame.values.logValue( monitoredExpression, value )


	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()
		valuesLive = LiveValue( Blank() )
		tree = Column( [ x._presentFrameSubtree( valuesLive )   for x in self._rootFrames ] )
		return Column( [ tree, Spacer( 0.0, 10.0 ), valuesLive ] )



class TraceVisualisation (object):
	def __init__(self):
		self._suite = EmbeddedPython2Suite()
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None

		self._tableSchema = None
		self._tableView = _TableView()

		self._treeView = _TreeView()


	def __getstate__(self):
		return { 'suite' : self._suite, 'tableView' : self._tableView, 'treeView' : self._treeView }

	def __setstate__(self, state):
		self._suite = state['suite']
		self._tableView = state.get( 'tableView', _TableView() )
		self._treeView = state.get( 'treeView', _TreeView() )
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None

		self._tableSchema = None


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def _initTableSchema(self):
		self._tableSchema = _TableSchema()

	def _initTableView(self):
		self._tableView.initialise( self._tableSchema )
		self._incr.onChanged()


	def _initTreeView(self):
		self._treeView.initialise()
		self._incr.onChanged()


	def _registerMonitoredExpression(self, monitoredExpression):
		self._tableSchema.registerMonitoredExpression( monitoredExpression )



	def _logValue(self, monitoredExpression, value):
		self._tableView.logValue( monitoredExpression, value )
		self._treeView.logValue( monitoredExpression, value )


	def _clear(self):
		self._initTableView()
		self._initTreeView()


	def __py_compile_visit__(self, codeGen):
		self._initTableSchema()

		prevSuite = MonitoredExpression._currentSuite
		MonitoredExpression._currentSuite = self

		self._code = codeGen.compileForExecution( self._suite.model )

		MonitoredExpression._currentSuite = prevSuite

		self._initTableView()
		self._initTreeView()


	def __py_exec__(self, _globals, _locals, codeGen):
		tableState = self._tableView.begin()
		treeState = self._treeView.begin()

		exec self._code in _globals, _locals

		self._treeView.end( treeState )
		self._tableView.end( tableState )


	def __py_replacement__(self):
		return deepcopy( self._suite.model['suite'] )


	def __present__(self, fragment, inheritedState):
		def _embeddedDisplayMenu(element, menu):
			def _onClear(item):
				self._clear()

			menu.add( MenuItem.menuItemWithLabel( 'Clear collected values', _onClear ) )

			return False



		self._incr.onAccess()
		suitePres = self._suite

		valuesPres = TabbedBox( [ [ Label( 'Tree' ), self._treeView ],  [ Label( 'Table' ), self._tableView ] ], None )

		contents = Column( [ suitePres, valuesPres ] )
		return ObjectBox( 'Trace visualisation', contents ).withContextMenuInteractor( _embeddedDisplayMenu ).withCommands( _mxCommands )




class MonitoredExpression (object):
	_currentSuite = None

	def __init__(self):
		self._name = 'value'
		self._expr = EmbeddedPython2Expr()
		self._suite = None
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __getstate__(self):
		return { 'name' : self._name,  'expr' : self._expr }

	def __setstate__(self, state):
		self._name = state['name']
		self._expr = state['expr']
		self._suite = None
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._name, self._expr ]


	def __py_compile_visit__(self, codeGen):
		self._code = codeGen.compileForEvaluation( self._expr.model )
		self._suite = self._currentSuite
		if self._suite is not None:
			self._suite._registerMonitoredExpression( self )


	def __py_eval__(self, _globals, _locals, codeGen):
		value = eval( self._code, _globals, _locals )
		if self._suite is not None:
			self._suite._logValue( self, value )
		return value

	def __py_replacement__(self):
		return deepcopy( self._expr.model['expr'] )


	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()

		def _setName(editableLabel, text):
			self._name = text
			self._incr.onChanged()


		namePres = EditableLabel.regexValidated( self._name, self._nameNotSetStyle( Label( '<not set>' ) ), _setName, Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' ), 'Please enter a valid identifier' )

		exprPres = self._expr

		contents = Row( [ namePres, Label( ': ' ), exprPres ] )
		return ObjectBox( 'Monitored exp.', contents )

	_nameNotSetStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.0, 0.0 ) ), Primitive.fontItalic( True ) )



@EmbeddedExpressionAtCaretAction
def _newMonitoredExpressionAtCaret(caret):
	return MonitoredExpression()

@WrapSelectionInEmbeddedExpressionAction
def _newMonitoredExpressionAtSelection(expr, selection):
	d = MonitoredExpression()
	d._expr.model['expr'] = deepcopy( expr )
	if expr.isInstanceOf( Schema.Load ):
		d._name = expr['name']
	return d

_mxCommand = Command( '&Monitored E&xpression', chainActions( _newMonitoredExpressionAtSelection, _newMonitoredExpressionAtCaret ) )

_mxCommands = CommandSet( 'LarchTools.PythonTools.EmbeddedDisplay.MonitoredExpression', [ _mxCommand ] )




@WrapSelectedStatementRangeInEmbeddedObjectAction
def _newTraceVisAtStatementRange(statements, selection):
	d = TraceVisualisation()
	d._suite.model['suite'][:] = deepcopy( statements )
	return d


_tvCommand = Command( '&Trace &Visualisation', chainActions( _newTraceVisAtStatementRange ) )

pythonCommandSet( 'LarchTools.PythonTools.TraceVis', [ _tvCommand ] )
