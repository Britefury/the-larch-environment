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

from LarchCore.Languages.Python25.PythonCommands import PythonCommandSet, EmbeddedExpressionAtCaretAction, WrapSelectionInEmbeddedExpressionAction,\
WrapSelectedStatementRangeInEmbeddedObjectAction, chainActions
from LarchCore.Languages.Python25.Embedded import EmbeddedPython25Expr, EmbeddedPython25Suite
from LarchCore.Languages.Python25 import Schema



class InlineTest (object):
	_currentTestingBlock = None

	def __index__(self):
		raise NotImplementedError


class TestingBlock (object):
	def __init__(self):
		self._name = LiveValue( 'test' )
		self._suite = EmbeddedPython25Suite()
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None

		self._inlineTests = []


	def __getstate__(self):
		return { 'name' : self._name.getValue(), 'suite' : self._suite }

	def __setstate__(self, state):
		self._name = LiveValue( state['name' ] )
		self._suite = state['suite']
		self._code = None
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None

		self._inlineTests = []


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def _registerInlineTest(self, test):
		self._inlineTests.append( test )


	def _clear(self):
		self._inlineTests = []


	def __py_compile_visit__(self, codeGen):
		self._clear()

		prevBlock = InlineTest._currentTestingBlock
		InlineTest._currentTestingBlock = self

		self._code = codeGen.compileForExecution( self._suite.model )

		InlineTest._currentTestingBlock = prevBlock


	def __py_execmodel__(self, codeGen):
		self._clear()

		stmts = self._suite.model['suite']

		# Get each inline test to create its method body
		methods = [ test._createTestMethodDeclaration( self )   for test in self._inlineTests ]

		# Create the class suite
		first = False
		clsSuite = []
		for method in methods:
			if not first:
				clsSuite.append( Schema.BlankLine() )
			clsSuite.append( method )


		# Now, we need to create the test class declaration
		clsName = self._getTestClassName()
		testCls = Schema.ClassStmt( name=clsName, bases=[], suite=clsSuite )



	def __py_replacement__(self):
		return deepcopy( self._suite.model['suite'] )



	def _getTestClassName(self):
		return 'Test_' + self._name.value


	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()
		suitePres = self._suite

		valuesPres = TabbedBox( [ [ Label( 'Tree' ), self._treeView ],  [ Label( 'Table' ), self._tableView ] ], None )

		contents = Column( [ suitePres, valuesPres ] )
		return ObjectBox( 'Trace visualisation', contents ).withContextMenuInteractor( _embeddedDisplayMenu ).withCommands( _mxCommands )





#
#@EmbeddedExpressionAtCaretAction
#def _newMonitoredExpressionAtCaret(caret):
#	return MonitoredExpression()
#
#@WrapSelectionInEmbeddedExpressionAction
#def _newMonitoredExpressionAtSelection(expr, selection):
#	d = MonitoredExpression()
#	d._expr.model['expr'] = deepcopy( expr )
#	if expr.isInstanceOf( Schema.Load ):
#		d._name = expr['name']
#	return d
#
#_mxCommand = Command( '&Monitored E&xpression', chainActions( _newMonitoredExpressionAtSelection, _newMonitoredExpressionAtCaret ) )
#
#_mxCommands = CommandSet( 'LarchTools.PythonTools.EmbeddedDisplay.MonitoredExpression', [ _mxCommand ] )
#
#
#
#
#@WrapSelectedStatementRangeInEmbeddedObjectAction
#def _newTraceVisAtStatementRange(statements, selection):
#	d = TraceVisualisation()
#	d._suite.model['suite'][:] = deepcopy( statements )
#	return d
#
#
#_tvCommand = Command( '&Trace &Visualisation', chainActions( _newTraceVisAtStatementRange ) )
#
#PythonCommandSet( 'LarchTools.PythonTools.TraceVis', [ _tvCommand ] )
