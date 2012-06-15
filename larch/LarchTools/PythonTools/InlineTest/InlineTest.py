##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import unittest

from java.awt import Color

from java.util.regex import Pattern

from copy import deepcopy

from BritefuryJ.Command import *

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import TrackedLiveValue

from BritefuryJ.Controls import *

from BritefuryJ.LSpace.Interactor import *
from BritefuryJ.Graphics import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.Pres.ObjectPres import *

from BritefuryJ.Parser.Utils import Tokens

from BritefuryJ.Editor.Table.Generic import *

from BritefuryJ.StyleSheet import *

from Britefury.Util.LiveList import LiveList
from Britefury.Util.UniqueNameTable import UniqueNameTable

from LarchCore.Languages.Python25.PythonCommands import pythonCommandSet, EmbeddedStatementAtCaretAction, WrapSelectedStatementRangeInEmbeddedObjectAction, chainActions
from LarchCore.Languages.Python25.Embedded import EmbeddedPython25Expr, EmbeddedPython25Suite
from LarchCore.Languages.Python25 import Schema


_nameBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.6, 0.6, 0.6 ), Color( 0.95, 0.95, 0.95 ) )

_notSet = StyleSheet.style( Primitive.fontItalic( True ) )( Label( 'not set' ) )






class AbstractIndividualTest (object):
	def __init__(self, inlineTest):
		self._inlineTest = inlineTest
		self._methodName = None


	@property
	def name(self):
		raise NotImplementedError, 'abstract'

	@property
	def methodName(self):
		if self._methodName is None:
			self._methodName = self._inlineTest._testingBlock._uniqueMethodName( 'test_%s' % self.name )
		return self._methodName


	def reset(self):
		self._methodName = None


	def _createTestMethodDeclaration(self, codeGen):
		return Schema.DefStmt( name=self.methodName, params=[ Schema.SimpleParam( name='self' ) ], suite=self._createTestMethodBody( codeGen ) )

	def _createTestMethodBody(self, codeGen):
		raise NotImplementedError, 'abstract'



class AbstractInlineTest (object):
	__current_testing_block__ = None


	def __init__(self):
		self.__testedBlock = None
		self.__testClassName = None


	def reset(self):
		self.__testClassName = None


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
		return unittest.TestCase


	def _createTestClass(self, codeGen):
		baseClassAsAST = codeGen.embeddedValue( self._baseClass )
		body = self._createTestClassBody( codeGen, self.__testedBlock )
		if len( body ) == 0:
			body.append( Schema.PassStmt() )
		return Schema.ClassStmt( name=self._className, decorators=[], bases=[ baseClassAsAST ], suite=body )


	def _createTestClassBody(self, codeGen):
		raise NotImplementedError, 'abstract;'


	def __py_execmodel__(self, codeGen):
		if self.__current_testing_block__ is not None:
			self.__current_testing_block__._registerInlineTest( self )
			self.__testedBlock = self.__current_testing_block__
		return Schema.PythonSuite( suite=[] )



class AbstractInlineTestCollection (AbstractInlineTest):
	__current_testing_block__ = None


	def __init__(self):
		self._testingBlock = None


	def reset(self):
		for test in self.individualTests:
			test.reset()


	@property
	def individualTests(self):
		raise NotImplementedError, 'abstract'


	def statements(self, codeGen):
		statements = []
		first = False
		for test in self.individualTests:
			if not first:
				statements.append( Schema.BlankLine() )
			statements.append( test._createTestMethodDeclaration( codeGen ) )



_standardInlineTestBorder = SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )

class StandardInlineTest (AbstractInlineTest):
	def __init__(self):
		self._suite = EmbeddedPython25Suite()
		self.__change_history__ = None


	def __getstate__(self):
		return { 'suite' : self._suite }

	def __setstate__(self, state):
		self._suite = state['suite']
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def statements(self, codeGen):
		return self._suite.model['suite']


	def __present__(self, fragment, inheritedState):
		title = SectionHeading2( 'Tests:' )

		return _standardInlineTestBorder.surround( Column( [ title, self._suite ] ) )



class TestedBlock (object):
	def __init__(self):
		self._suite = EmbeddedPython25Suite()
		self.__change_history__ = None

		self._inlineTests = []
		self.__classNames = UniqueNameTable()


	def __getstate__(self):
		return { 'suite' : self._suite }

	def __setstate__(self, state):
		self._suite = state['suite']
		self.__change_history__ = None

		self._inlineTests = []
		self.__classNames = UniqueNameTable()


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def _registerInlineTest(self, test):
		self._inlineTests.append( test )


	def _clear(self):
		for test in self._inlineTests:
			test.reset()
		self._inlineTests = []
		self.__classNames.clear(0)


	def _uniqueClassName(self, name):
		return self.__classNames.uniqueName( name )


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
		def unitTestClasses(codeGen):
			# Create the class suite
			first = True
			classes = []
			for test in self._inlineTests:
				if not first:
					classes.append( Schema.BlankLine() )
				classes.append( test._createTestClass( codeGen ) )
				first = False

			return Schema.PythonSuite( suite=classes )


		return Schema.PythonSuite( suite=[ mainContent, unitTestClasses ] )



	def __py_replacement__(self):
		return deepcopy( self._suite.model['suite'] )



	def __present__(self, fragment, inheritedState):
		title = SectionHeading2( 'Tested block' )
		contents = Column( [ title, self._suite ] )
		return ObjectBox( 'Unit test', contents ).withCommands( inlineTestCommands )




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
