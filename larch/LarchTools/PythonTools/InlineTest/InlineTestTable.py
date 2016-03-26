##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import sys

from java.awt import Color

from copy import deepcopy

from BritefuryJ.Command import Command

from BritefuryJ.Live import TrackedLiveValue, LiveValue

from BritefuryJ.Controls import Button, EditableLabel

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Blank, Spacer, Row, Column
from BritefuryJ.Pres.UI import SectionHeading2

from BritefuryJ.Parser.Utils import Tokens

from BritefuryJ.Editor.Table.ObjectList import AttributeColumn, ObjectListTableEditor

from BritefuryJ.StyleSheet import StyleSheet

from Britefury.Util.LiveList import LiveList
from Britefury.Util.UniqueNameTable import UniqueNameTable
from BritefuryJ.Util.Jython import JythonException

from LarchCore.Languages.Python2.PythonCommands import EmbeddedStatementAtCaretAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Suite
from LarchCore.Languages.Python2 import Schema

from LarchTools.PythonTools.InlineTest.InlineTest import AbstractInlineTest, inlineTestCommandSet
from LarchTools.PythonTools.InlineTest.TestTable import AbstractTestTableRow, AbstractTestTable


_resultFailStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.4, 0.0 ) ) )
_resultPass = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ).applyTo( Label( 'PASS' ) )
_resultNone = StyleSheet.style( Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ).applyTo( Label( 'NONE' ) )




class AbstractInlineTestTableRow (AbstractTestTableRow):
	def __init__(self):
		super(AbstractInlineTestTableRow, self).__init__()
		self.__testTable = None
		self.__testMethodName = None



	def __setstate__(self, state):
		super(AbstractInlineTestTableRow, self).__setstate__(state)
		self.__testTable = None
		self.__testMethodName = None



	def reset(self):
		self.__testMethodName = None


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







_nameBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.6, 0.6, 0.6 ), Color( 0.95, 0.95, 0.95 ) )
_notSet = StyleSheet.style( Primitive.fontItalic( True ) )( Label( 'not set' ) )
_inlineTestTableBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )


class AbstractInlineTestTable (AbstractInlineTest, AbstractTestTable):
	_tableEditor = NotImplemented


	def __init__(self, name='test'):
		super( AbstractInlineTestTable, self ).__init__()
		self.__methodNames = UniqueNameTable()



	def __getstate__(self):
		state = AbstractInlineTest.__getstate__(self)
		state.update(AbstractTestTable.__getstate__(self))
		return state

	def __setstate__(self, state):
		AbstractInlineTest.__setstate__(self, state)
		AbstractTestTable.__setstate__(self, state)
		self.__methodNames = UniqueNameTable()



	def reset(self):
		super( AbstractInlineTestTable, self ).reset()
		self.__methodNames.clear()



	@property
	def _desiredClassName(self):
		return 'Test_' + self._name.getStaticValue()


	def _uniqueMethodName(self, name):
		return self.__methodNames.uniqueName( name )

	def _runTestsOnInstance(self, instance):
		for test in self._tests:
			test.runOnInstance( instance )


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
