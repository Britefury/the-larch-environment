##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from copy import deepcopy

from BritefuryJ.Command import CommandSetRegistry, CommandSet, Command

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import TrackedLiveValue

from BritefuryJ.Controls import EditableLabel

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Row, Column
from BritefuryJ.Pres.UI import SectionHeading2, SectionHeading3

from BritefuryJ.Parser.Utils import Tokens

from BritefuryJ.StyleSheet import StyleSheet

from Britefury.Util.UniqueNameTable import UniqueNameTable
from BritefuryJ.Util.Jython import JythonException

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedStatementAtCaretAction, WrapSelectedStatementRangeInEmbeddedObjectAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Suite
from LarchCore.Languages.Python2 import Schema


_nameBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.6, 0.6, 0.6 ), Color( 0.95, 0.95, 0.95 ) )

_notSet = StyleSheet.style( Primitive.fontItalic( True ) )( Label( 'not set' ) )




_testValueBorder = SolidBorder( 1.5, 2.5, 4.0, 4.0, Color( 0.75, 0.0, 0.0 ), None )


_comparisonStyle = StyleSheet.style( Primitive.fontBold( True ), Primitive.foreground( Color( 0.75, 0.0, 0.0 ) ) )


class TestCase (object):
	def assertEqual(self, first, second):
		if not ( first == second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not ==' ) ), _testValueBorder.surround( second ) ] )


	def assertNotEqual(self, first, second):
		if not ( first != second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not !=' ) ), _testValueBorder.surround( second ) ] )


	def assertTrue(self, first):
		if not first:
			raise AssertionError, Row( [ _comparisonStyle( Label( 'Not True' ) ) ] )


	def assertFalse(self, first):
		if first:
			raise AssertionError, Row( [ _comparisonStyle( Label( 'Not False' ) ) ] )


	def assertIs(self, first, second):
		if not ( first is second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'is not' ) ), _testValueBorder.surround( second ) ] )


	def assertIsNot(self, first, second):
		if not ( first is not second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'is' ) ), _testValueBorder.surround( second ) ] )


	def assertIn(self, first, second):
		if not ( first in second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not in' ) ), _testValueBorder.surround( second ) ] )


	def assertNotIn(self, first, second):
		if not ( first not in second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'in' ) ), _testValueBorder.surround( second ) ] )


	def assertIsInstance(self, first, second):
		if not isinstance( first, second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'is not instance of' ) ), _testValueBorder.surround( second ) ] )


	def assertNotIsInstance(self, first, second):
		if isinstance( first, second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'is instance of' ) ), _testValueBorder.surround( second ) ] )


	def assertGreater(self, first, second):
		if not ( first > second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not >' ) ), _testValueBorder.surround( second ) ] )


	def assertGreaterEqual(self, first, second):
		if not ( first >= second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not >=' ) ), _testValueBorder.surround( second ) ] )


	def assertLess(self, first, second):
		if not ( first < second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not <' ) ), _testValueBorder.surround( second ) ] )


	def assertLessEqual(self, first, second):
		if not ( first <= second ):
			raise AssertionError, Row( [ _testValueBorder.surround( first ), _comparisonStyle( Label( 'not <=' ) ), _testValueBorder.surround( second ) ] )




class AbstractInlineTest (object):
	__current_testing_block__ = None


	def __init__(self):
		self.__testedBlock = None
		self.__testClassName = None
		self._testClass = None


	def __getstate__(self):
		return {}

	def __setstate__(self, state):
		self.__testedBlock = None
		self.__testClassName = None
		self._testClass = None


	def reset(self):
		self.__testClassName = None
		self._testClass = None


	@property
	def _desiredClassName(self):
		raise NotImplementedError, 'abstract'


	@property
	def _className(self):
		if self.__testClassName is None:
			self.__testClassName = self.__testedBlock._uniqueClassName( self._desiredClassName )
		return self.__testClassName


	@property
	def _baseClass(self):
		return TestCase


	def createInstance(self):
		return self._testClass()   if self._testClass is not None   else None


	def run(self):
		instance = self.createInstance()
		if instance is not None:
			self._runTestsOnInstance( instance )


	def _runTestsOnInstance(self, instance):
		raise NotImplementedError, 'abstract'


	def _registerTestClass(self, testCls):
		self._testClass = testCls


	@property
	def _scope(self):
		return self.__testedBlock._scope   if self.__testedBlock is not None   else ( None, None, None )


	def _createTestClass(self, codeGen):
		baseClassAsAST = codeGen.embeddedValue( self._baseClass )
		body = self._createTestClassBodyStmts( codeGen, self.__testedBlock )
		if len( body ) == 0:
			body.append( Schema.PassStmt() )
		return Schema.ClassStmt( name=self._className, decorators=[], bases=[ baseClassAsAST ], suite=body )


	def _createTestClassBodyStmts(self, codeGen, testedBlock):
		raise NotImplementedError, 'abstract'


	def __py_execmodel__(self, codeGen):
		if self.__current_testing_block__ is not None:
			self.__current_testing_block__._registerInlineTest( self )
			self.__testedBlock = self.__current_testing_block__
		return Schema.PythonSuite( suite=[] )



_standardInlineTestBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )
_standardCodeBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.6, 0.6, 0.6 ), None )
_standardResultsBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.7, 0.7, 0.7 ), Color( 0.95, 0.95, 0.95 ) )
_standardPassStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) )
_standardFailStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.0, 0.0 ) ) )
_standardFailedTestStyle = StyleSheet.style( Primitive.foreground( Color( 0.75, 0.0, 0.0 ) ) )
_standardResultsBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.7, 0.7, 0.7 ), Color( 0.95, 0.95, 0.95 ) )

class StandardInlineTest (AbstractInlineTest):
	def __init__(self, name='test'):
		super( StandardInlineTest, self ).__init__()
		self._name = TrackedLiveValue( name )
		self._suite = EmbeddedPython2Suite()
		self.__change_history__ = None

		self.__passes = None
		self.__failures = None

		self._incr = IncrementalValueMonitor()


	def __getstate__(self):
		state = super( StandardInlineTest, self ).__getstate__()
		state['name'] = self._name.getStaticValue()
		state['suite'] = self._suite
		return state

	def __setstate__(self, state):
		super( StandardInlineTest, self ).__setstate__( state )
		self._name = TrackedLiveValue( state['name'] )
		self._suite = state['suite']
		self.__change_history__ = None

		self.__passes = None
		self.__failures = None

		self._incr = IncrementalValueMonitor()


	def __get_trackable_contents__(self):
		return [ self._name, self._suite ]


	def reset(self):
		super( StandardInlineTest, self ).reset()
		self.__passes = None
		self.__failures = None
		self._incr.onChanged()



	def _runTestsOnInstance(self, instance):
		self.__passes = 0
		self.__failures = []

		for name in self._testClass.__dict__:
			if name.startswith( 'test' ):
				# Run this test method
				try:
					getattr( instance, name )()
				except Exception:
					caughtException = JythonException.getCurrentException()
					self.__failures.append( (name, caughtException) )
				else:
					self.__passes += 1

		self._incr.onChanged()



	@property
	def _desiredClassName(self):
		return 'Test_' + self._name.getValue()

	def _createTestClassBodyStmts(self, codeGen, testedBlock):
		return self._suite.model['suite']


	__embed_hide_frame__ = True

	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()

		title = SectionHeading2( 'Unit tests' )

		nameEntry = _nameBorder.surround( EditableLabel( self._name, _notSet ).regexValidated( Tokens.identifierPattern, 'Please enter a valid identifier' ) )

		header = Row( [ title, Spacer( 25.0, 0.0 ), nameEntry ] )

		contents = [ header.padY( 0.0, 5.0 ), _standardCodeBorder.surround( self._suite ).padY( 3.0 ).alignHExpand() ]

		if self.__passes is not None  and  self.__failures is not None:
			resultsTitle = SectionHeading3( 'Test results:' )
			passes = _standardPassStyle( Label( '%d / %d test(s) passed' % ( self.__passes, self.__passes + len( self.__failures ) ) ) )
			failuresLabel = _standardFailStyle( Label( '%d test(s) failed:' % len( self.__failures ) ) )
			failures = [ Column( [ _standardFailedTestStyle( Label( name ) ), Pres.coerce( exception ).padX( 5.0, 0.0 ) ] ).padX( 5.0, 0.0 )   for name, exception in self.__failures ]
			results = _standardResultsBorder.surround( Column( [ resultsTitle, passes, failuresLabel ] + failures ) ).pad( 3.0, 3.0 )
			contents.append( results )


		return _standardInlineTestBorder.surround( Column( contents ) )




_testedBlockBorder = SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.5, 0.5, 0.45 ), None )

class TestedBlock (object):
	def __init__(self):
		self._suite = EmbeddedPython2Suite()
		self.__change_history__ = None

		self._inlineTests = []
		self.__classNames = UniqueNameTable()

		self.__module = None
		self.__globalScope = None
		self.__localScope = None


	def __getstate__(self):
		return { 'suite' : self._suite }

	def __setstate__(self, state):
		self._suite = state['suite']
		self.__change_history__ = None

		self._inlineTests = []
		self.__classNames = UniqueNameTable()

		self.__module = None
		self.__globalScope = None
		self.__localScope = None


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def _registerInlineTest(self, test):
		self._inlineTests.append( test )


	def _clear(self):
		for test in self._inlineTests:
			test.reset()
		self._inlineTests = []
		self.__classNames.clear()


	def _uniqueClassName(self, name):
		return self.__classNames.uniqueName( name )


	def runTests(self):
		for test in self._inlineTests:
			test.run()


	def _registerScope(self, module, globalScope, localScope):
		self.__module = module
		self.__globalScope = globalScope
		self.__localScope = localScope


	@property
	def _scope(self):
		return self.__module, self.__globalScope, self.__localScope


	def __py_execmodel__(self, codeGen):
		self._clear()

		prevTestingBlock = [ None ]


		# Use a guard to push self onto the TestingBlock stack
		def beginGuard(codeGen):
			prevTestingBlock[0] = AbstractInlineTest.__current_testing_block__
			AbstractInlineTest.__current_testing_block__ = self

		def endGuard(codeGen):
			AbstractInlineTest.__current_testing_block__ = prevTestingBlock[0]

		mainContent = codeGen.guard( beginGuard, Schema.PythonSuite( suite=self._suite.model['suite'] ), endGuard )


		# Defer the generation of the unit test class
		@codeGen.deferred
		def unitTesting(codeGen):
			# Create the class suite
			first = True
			testing = []
			for test in self._inlineTests:
				if not first:
					testing.append( Schema.BlankLine() )
					testing.append( Schema.BlankLine() )
				testing.append( test._createTestClass( codeGen ) )
				first = False

			testing.append( Schema.BlankLine() )
			testing.append( Schema.BlankLine() )

			for test in self._inlineTests:
				testAst = codeGen.embeddedValue( test )
				testing.append( Schema.ExprStmt( expr=Schema.Call( target=Schema.AttributeRef( target=testAst, name='_registerTestClass' ), args=[ Schema.Load( name=test._className ) ] ) ) )

			selfAST = codeGen.embeddedValue( self )
			moduleAST = codeGen.embeddedValue( codeGen.module )
			globalsAST = Schema.Call( target=Schema.Load( name='globals' ), args=[] )
			localsAST = Schema.Call( target=Schema.Load( name='locals' ), args=[] )
			testing.append( Schema.ExprStmt( expr=Schema.Call( target=Schema.AttributeRef( target=selfAST, name='runTests' ), args=[] ) ) )
			testing.append( Schema.ExprStmt( expr=Schema.Call( target=Schema.AttributeRef( target=selfAST, name='_registerScope' ), args=[ moduleAST, globalsAST, localsAST ] ) ) )

			return Schema.PythonSuite( suite=testing )


		return Schema.PythonSuite( suite=[ mainContent, unitTesting ] )



	def __py_replacement__(self):
		return deepcopy( self._suite.model['suite'] )



	__embed_hide_frame__ = True

	def __present__(self, fragment, inheritedState):
		title = SectionHeading2( 'Tested block' )

		contents = Column( [ title, self._suite ] )
		return _testedBlockBorder.surround( contents ).withCommands( inlineTestCommands )




#
# Command set registry and class definition for inline test commands
#

inlineTestCommands = CommandSetRegistry( 'LarchTools.PythonTools.InlineTest.TestedBlock' )


def inlineTestCommandSet(name, commands):
	commandSet = CommandSet( name, commands )
	inlineTestCommands.registerCommandSet( commandSet )
	return commandSet




#
# Commands for inserting standard tests
#

@EmbeddedStatementAtCaretAction
def _newStandardInlineTestAtCaret(caret):
	return StandardInlineTest()

_sitCommand = Command( '&Standard &Inline &Test', chainActions( _newStandardInlineTestAtCaret ) )

inlineTestCommandSet( 'LarchTools.PythonTools.InlineTest.StandardTests', [ _sitCommand ] )




@EmbeddedStatementAtCaretAction
def _newTestedBlockAtCaret(caret):
	return TestedBlock()

@WrapSelectedStatementRangeInEmbeddedObjectAction
def _newTestedBlockAtStatementRange(statements, selection):
	d = TestedBlock()
	d._suite.model['suite'][:] = deepcopy( statements )
	return d


_tbCommand = Command( '&Tested &Block', chainActions( _newTestedBlockAtStatementRange, _newTestedBlockAtCaret ) )

pythonCommandSet( 'LarchTools.PythonTools.InlineTest', [ _tbCommand ] )
