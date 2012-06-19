##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys

from java.awt import Color

from copy import deepcopy

from BritefuryJ.Command import *

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import TrackedLiveValue, LiveValue

from BritefuryJ.Controls import *

from BritefuryJ.Graphics import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.Pres.ObjectPres import *

from BritefuryJ.Parser.Utils import Tokens

from BritefuryJ.Editor.Table.ObjectList import *

from BritefuryJ.StyleSheet import *

from Britefury.Util.LiveList import LiveList
from Britefury.Util.UniqueNameTable import UniqueNameTable
from BritefuryJ.Util.Jython import JythonException

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedStatementAtCaretAction, WrapSelectedStatementRangeInEmbeddedObjectAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr, EmbeddedPython2Suite
from LarchCore.Languages.Python2 import Schema

from LarchTools.PythonTools.InlineTest.InlineTest import AbstractInlineTest, inlineTestCommandSet


_resultFailStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.4, 0.0 ) ) )
_resultPass = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ).applyTo( Label( 'PASS' ) )
_resultNone = StyleSheet.style( Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ).applyTo( Label( 'NONE' ) )




class AbstractInlineTestTableRow (object):
	def __init__(self):
		self.__testTable = None
		self.__result = LiveValue( _resultNone )
		self.__testMethodName = None
		# Form: either ( 'value', value )  or  ( 'exception', exceptionType )  or  None
		self._expected = TrackedLiveValue( None )
		self._actual = TrackedLiveValue( None )

		self.__change_history__ = None



	def __getstate__(self):
		return { 'expected' : self._expected.getStaticValue() }

	def __setstate__(self, state):
		self.__testTable = None
		self.__result = LiveValue( _resultNone )
		self.__testMethodName = None
		self._expected = TrackedLiveValue( state['expected'] )
		self._actual = TrackedLiveValue( None )


	def __get_trackable_contents__(self):
		return [ self._expected ]



	def reset(self):
		self.__result.setLiteralValue( _resultNone )
		self.__testMethodName = None
		self._actual.setLiteralValue( None )


	def _regsiterTestTable(self, testTable):
		self.__testTable = testTable


	def runOnInstance(self, instance):
		getattr( instance, self._methodName )()


	@property
	def _desiredMethodName(self):
		raise NotImplementedError, 'abstract'


	@property
	def _methodName(self):
		if self.__testMethodName is None:
			self.__testMethodName = self.__testTable._uniqueMethodName( self._desiredMethodName )
		return self.__testMethodName


	def _createValueStmtsAndResultVarName(self, codeGen):
		raise NotImplementedError, 'abstract'


	@property
	def _scope(self):
		return self.__testTable._scope   if self.__testTable is not None   else ( None, None, None )


	def _debug(self):
		return None


	def __debugWrap(self, p):
		d = self._debug()
		if d is not None:
			return Column( [ p, Pres.coerce( d ).alignHPack() ] ).alignVTop()
		else:
			return p


	def _createMethodAST(self, codeGen):
		valueStmts, resultVarName = self._createValueStmtsAndResultVarName( codeGen )

		JythonExceptionAST = codeGen.embeddedValue( JythonException )
		sysAST = codeGen.embeddedValue( sys )
		getCurrentExceptionCallAST = Schema.Call( target=Schema.AttributeRef( target=JythonExceptionAST, name='getCurrentException' ), args=[] )
		getExcInfoTypeAST = Schema.Subscript( target=Schema.Call( target=Schema.AttributeRef( target=sysAST, name='exc_info' ), args=[] ),
						      index=Schema.IntLiteral( format='decimal', numType='int', value='0' ) )
		selfAST = codeGen.embeddedValue( self )
		testValueAST = Schema.AttributeRef( target=selfAST, name='_testValue' )
		kindExceptionAST = Schema.StringLiteral( format='ascii', quotation='single', value='exception' )
		kindValueAST = Schema.StringLiteral( format='ascii', quotation='single', value='value' )

		exceptStmts = [ Schema.ExprStmt( expr=Schema.Call( target=testValueAST, args=[ kindExceptionAST, getCurrentExceptionCallAST, getExcInfoTypeAST ] ) ) ]
		elseStmts = [ Schema.ExprStmt( expr=Schema.Call( target=testValueAST, args=[ kindValueAST, Schema.Load( name=resultVarName ) ] ) ) ]
		methodBody = [ Schema.TryStmt( suite=valueStmts, exceptBlocks=[ Schema.ExceptBlock( suite=exceptStmts ) ], elseSuite=elseStmts ) ]

		methodAST = Schema.DefStmt( name=self._methodName, decorators=[], params=[ Schema.SimpleParam( name='self' ) ], suite=methodBody )
		return methodAST



	@staticmethod
	def _statementsForExecutionAndEvaluationIntoValue(stmts, varName):
		for i in xrange( len( stmts ) - 1, -1, -1 ):
			stmt = stmts[i]
			if stmt.isInstanceOf( Schema.ExprStmt ):
				return stmts[:i] + [ Schema.AssignStmt( targets=[ Schema.SingleTarget( name=varName ) ], value=stmt['expr'] ) ] + stmts[i+1:]
			elif stmt.isInstanceOf( Schema.BlankLine )  or  stmt.isInstanceOf( Schema.CommentStmt ):
				pass
			else:
				break
		return deepcopy( stmts ) + [ Schema.AssignStmt( targets=[ Schema.SingleTarget( name=varName ) ], value=Schema.Load( name='None' ) ) ]




	def _testValue(self, kind, data, excType=None):
		self._actual.setLiteralValue( ( kind, data ) )
		expected = self._expected.getStaticValue()
		if expected is not None:
			expectedKind, expectedData = expected
			if kind == expectedKind:
				if expectedKind == 'exception':
					if excType == expectedData:
						self.__result.setLiteralValue( _resultPass )
					else:
						title = _resultFailStyle( Label( 'FAIL' ) )
						heading = Label( 'Expected exception of type %s, got:' % expectedData.__name__ )
						res = Column( [ title, heading, data ] )
						self.__result.setLiteralValue( res )
				elif expectedKind == 'value':
					if data == expectedData:
						self.__result.setLiteralValue( _resultPass )
					else:
						title = _resultFailStyle( Label( 'FAIL' ) )
						heading1 = Label( 'Expected:' )
						heading2 = Label( 'Got:' )
						res = Column( [ title, heading1, expectedData, heading2, data ] )
						self.__result.setLiteralValue( res )
				else:
					raise TypeError, 'unknown expected result kind %s' % expectedKind
			else:
				resContents = [ _resultFailStyle( Label( 'FAIL' ) ) ]

				if expectedKind == 'exception':
					resContents.append( Label( 'Expected exception of type %s:' % expectedData.__name__ ) )
				elif expectedKind == 'value':
					resContents.append( Label( 'Expected:' ) )
					resContents.append( expectedData )
				else:
					raise TypeError, 'unknown expected result kind %s' % expectedKind

				if kind == 'exception':
					resContents.append( Label( 'Got exception:' ) )
					resContents.append( data )
				elif kind == 'value':
					resContents.append( Label( 'Expected:' ) )
					resContents.append( data )
				else:
					raise TypeError, 'unknown result kind %s' % kind

				self.__result.setLiteralValue( Column( resContents ) )
		else:
			def _onFix(button, event):
				if kind == 'exception':
					self._expected.setLiteralValue( ( kind, excType ) )
				elif kind == 'value':
					self._expected.setLiteralValue( ( kind, data ) )
				else:
					raise TypeError, 'unknown result kind %s' % kind
				self.__result.setLiteralValue( _resultNone )

			fixButton = Button.buttonWithLabel( 'Set expected result', _onFix )
			title = Label( 'No expected result, received:' )
			self.__result.setLiteralValue( Column( [ title, Pres.coerce( data ).pad( 5.0, 0.0, 5.0, 5.0 ), fixButton ] ).alignHPack().alignVTop() )







	@property
	def result(self):
		return self.__debugWrap( self.__result )


	@property
	def expected(self):
		expected = self._expected.getValue()
		if expected is not None:
			expectedKind, expectedData = expected
			if expectedKind == 'exception':
				return Label( 'Exception of type %s' % expectedData.__name__ )
			elif expectedKind == 'value':
				return expectedData
			else:
				raise TypeError, 'unknown expected result kind %s' % expectedKind
		else:
			return Blank()


	@expected.setter
	def expected(self, x):
		if x is None:
			self._expected.setLiteralValue( None )




AbstractInlineTestTableRow._resultColumn = AttributeColumn( 'Result', 'result' )
AbstractInlineTestTableRow._expectedColumn = AttributeColumn( 'Expected', 'expected', None, None )



_nameBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.6, 0.6, 0.6 ), Color( 0.95, 0.95, 0.95 ) )
_notSet = StyleSheet.style( Primitive.fontItalic( True ) )( Label( 'not set' ) )
_inlineTestTableBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )


class AbstractInlineTestTable (AbstractInlineTest):
	_tableEditor = NotImplemented


	def __init__(self, name='test'):
		super( AbstractInlineTestTable, self ).__init__()
		self._name = TrackedLiveValue( name )
		self._tests = LiveList()
		self.__change_history__ = None

		self.__methodNames = UniqueNameTable()



	def __getstate__(self):
		state = super( AbstractInlineTestTable, self ).__getstate__()
		state['name'] = self._name.getStaticValue()
		state['tests'] = self._tests
		return state

	def __setstate__(self, state):
		super( AbstractInlineTestTable, self ).__setstate__( state )
		self._name = TrackedLiveValue( state['name'] )
		self._tests = state['tests']
		self.__change_history__ = None

		self.__methodNames = UniqueNameTable()


	def __get_trackable_contents__(self):
		return [ self._name, self._tests ]



	def reset(self):
		for test in self._tests:
			test.reset()
		self.__methodNames.clear()



	@property
	def _desiredClassName(self):
		return 'Test_' + self._name.getStaticValue()


	def _runTestsOnInstance(self, instance):
		for test in self._tests:
			test.runOnInstance( instance )


	def _uniqueMethodName(self, name):
		return self.__methodNames.uniqueName( name )




	def _createTestClassBodyStmts(self, codeGen, testedBlock):
		stmts = []
		first = True
		for test in self._tests:
			if not first:
				stmts.append( Schema.BlankLine() )
			test._regsiterTestTable( self )
			stmts.append( test._createMethodAST( codeGen ) )
			first = False
		return stmts




	__embed_hide_frame__ = True

	def __present__(self, fragment, inheritedState):
		title = SectionHeading2( 'Unit tests' )

		nameEntry = _nameBorder.surround( EditableLabel.regexValidated( self._name, _notSet, Tokens.identifierPattern, 'Please enter a valid identifier' ) )

		header = Row( [ title, Spacer( 25.0, 0.0 ), nameEntry ] )

		if self._tableEditor is NotImplemented:
			raise NotImplementedError, 'Table editor is abstract'

		table = self._tableEditor.editTable( self._tests )

		return _inlineTestTableBorder.surround( Column( [ header, Spacer( 0.0, 5.0 ), table ] ) )








class SimpleInlineTableTestRow (AbstractInlineTestTableRow):
	def __init__(self):
		super( SimpleInlineTableTestRow, self ).__init__()
		self._suite = EmbeddedPython2Suite()


	def __getstate__(self):
		state = super( SimpleInlineTableTestRow, self ).__getstate__()
		state['suite'] = self._suite
		return state

	def __setstate__(self, state):
		super( SimpleInlineTableTestRow, self ).__setstate__( state )
		self._suite = state['suite']


	def __get_trackable_contents__(self):
		return super( SimpleInlineTableTestRow, self ).__get_trackable_contents__()  +  [ self._suite ]



	@property
	def _desiredMethodName(self):
		return 'row_'



	def _createValueStmtsAndResultVarName(self, codeGen):
		varName = '__inline_table_test_value'
		stmts = self._suite.model['suite']
		stmts = self._statementsForExecutionAndEvaluationIntoValue( stmts, varName )
		return stmts, varName


	@property
	def code(self):
		return self._suite

	@code.setter
	def code(self, x):
		self._suite = x


SimpleInlineTableTestRow._codeColumn = AttributeColumn( 'Code', 'code', EmbeddedPython2Suite )




class SimpleInlineTableTest (AbstractInlineTestTable):
	pass


SimpleInlineTableTest._tableEditor = ObjectListTableEditor( [ SimpleInlineTableTestRow._resultColumn,
								 SimpleInlineTableTestRow._codeColumn,
								 SimpleInlineTableTestRow._expectedColumn ], SimpleInlineTableTestRow, True, True, True, True )





#
# Commands for inserting standard tests
#

@EmbeddedStatementAtCaretAction
def _newSimpleInlineTableTestAtCaret(caret):
	return SimpleInlineTableTest()

_sittCommand = Command( '&Simple &Inline &Table &Test', chainActions( _newSimpleInlineTableTestAtCaret ) )

inlineTestCommandSet( 'LarchTools.PythonTools.InlineTest.TableTests', [ _sittCommand ] )
