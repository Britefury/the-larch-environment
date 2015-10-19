##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2015.
##-*************************
import sys

from java.util import List
from java.awt import Color

from BritefuryJ.Util.RichString import RichString
from BritefuryJ.Util.Jython import JythonException

from BritefuryJ.LSpace import PageController

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Column
from BritefuryJ.Pres.UI import SectionHeading2
from BritefuryJ.Controls import Hyperlink, EditableLabel

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Live import LiveValue, TrackedLiveValue

from BritefuryJ.Command import Command

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Editor.Table.ObjectList import AttributeColumn, ObjectListTableEditor
from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Suite
from LarchCore.Languages.Python2.PythonCommands import EmbeddedStatementAtCaretAction, chainActions

from LarchTools.PythonTools.InlineTest.InlineTest import inlineTestCommandSet, TestCase
from LarchTools.PythonTools.InlineTest.TestTable import AbstractTestTableRow, AbstractTestTable


#
#Implementation
class _GrammarTestResult (object):
	pass


_successStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.5, 0.0)), Primitive.fontSize(10))
_incompleteStyle = StyleSheet.style(Primitive.foreground(Color(0.5, 0.5, 0.0)), Primitive.fontSize(10))
_failStyle = StyleSheet.style(Primitive.foreground(Color(0.5, 0.0, 0.0)), Primitive.fontSize(10))
_traceHyperlinkStyle = StyleSheet.style(Primitive.fontSize(10))

_resultFailStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.4, 0.0 ) ) )
_resultPass = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ).applyTo( Label( 'PASS' ) )
_resultNone = StyleSheet.style( Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ).applyTo( Label( 'NONE' ) )


class _GrammarTestResultSuccess (_GrammarTestResult):
	def __init__(self, value):
		self.value = value


	def __eq__(self, other):
		if isinstance(other, _GrammarTestResultSuccess):
			return self.value == other.value
		else:
			return NotImplemented

	def __ne__(self, other):
		if isinstance(other, _GrammarTestResultSuccess):
			return self.value != other.value
		else:
			return NotImplemented


	def __present__(self, fragment, inheritedState):
		return Column([_successStyle(Label('Success')), Pres.coerce(self.value).padX(10.0, 0.0)])


class _GrammarTestResultIncomplete (_GrammarTestResult):
	def __init__(self, value, parsed, total):
		self.value = value
		self.parsed = parsed
		self.total = total


	def __eq__(self, other):
		if isinstance(other, _GrammarTestResultIncomplete):
			return self.value == other.value and self.parsed == other.parsed and self.total == other.total
		else:
			return NotImplemented

	def __ne__(self, other):
		if isinstance(other, _GrammarTestResultIncomplete):
			return self.value != other.value or self.parsed != other.parsed or self.total != other.total
		else:
			return NotImplemented


	def __present__(self, fragment, inheritedState):
		return Column([_incompleteStyle(Label('Incomplete - %d/%d' % (self.parsed, self.total))), Pres.coerce(self.value).padX(10.0, 0.0)])


class _GrammarTestResultFail (_GrammarTestResult):
	def __eq__(self, other):
		if isinstance(other, _GrammarTestResultFail):
			return True
		else:
			return NotImplemented

	def __ne__(self, other):
		if isinstance(other, _GrammarTestResultFail):
			return False
		else:
			return NotImplemented


	def __present__(self, fragment, inheritedState):
		return _failStyle(Label('Fail'))


class GrammarTestTableRow (AbstractTestTableRow):
	def __init__(self):
		super(GrammarTestTableRow, self).__init__()
		self._rule_name = TrackedLiveValue()
		self._inputCode = EmbeddedPython2Suite()
		self._incr = IncrementalValueMonitor()

		self.__parser = None
		self.__input = None


	def __getstate__(self):
		state = super(GrammarTestTableRow, self).__getstate__()
		state['rule_name'] = self._rule_name.getStaticValue()
		state['inputCode'] = self._inputCode
		return state

	def __setstate__(self, state):
		super(GrammarTestTableRow, self).__setstate__(state)
		self._rule_name = TrackedLiveValue(state['rule_name'])
		self._inputCode = state['inputCode']
		self._incr = IncrementalValueMonitor()

		self.__parser = None
		self.__input = None


	def __get_trackable_contents__(self):
		return super(GrammarTestTableRow, self).__get_trackable_contents__() + [self._rule_name, self._inputCode]


	def _parserTest(self, parser, inputData):
		if isinstance(inputData, str) or isinstance(inputData, unicode):
			parseResult = parser.parseStringChars(inputData)
			inputLength = len(inputData)
		elif isinstance(inputData, list) or isinstance(inputData, List):
			parseResult = parser.parseListItems(inputData)
			inputLength = len(inputData)
		elif isinstance(inputData, RichString):
			parseResult = parser.parseRichStringItems(inputData)
			inputLength = len(inputData)
		elif isinstance(inputData, DMNode):
			parseResult = parser.parseNode(inputData)
			inputLength = 1
		else:
			raise TypeError, 'Cannot handle input of type %s' % type(inputData)

		if parseResult.isValid():
			if parseResult.getEnd() == inputLength:
				return _GrammarTestResultSuccess(parseResult.getValue())
			else:
				return _GrammarTestResultIncomplete(parseResult.getValue(), parseResult.getEnd(), inputLength)
		else:
			return _GrammarTestResultFail()





	def run_test(self, module, name_to_rule):
		try:
			rule = name_to_rule[self._rule_name.getStaticValue()]
			parser_input = self._inputCode.executeAndEvaluateWithinModule(module)
			self.__parser = rule
			self.__input = parser_input
			test_value = self._parserTest(rule, parser_input)
		except:
			self._testValue('exception', JythonException.getCurrentException(), sys.exc_info()[0])
		else:
			self._testValue('value', test_value)




	def _debug(self):
		def _onTrace(hyperlink, event):
			self._onTrace(hyperlink)
		traceHyperlink = _traceHyperlinkStyle.applyTo(Hyperlink('Trace', _onTrace))
		return traceHyperlink

	def _onTrace(self, hyperlink):
		if self.__parser is not None and self.__input is not None:
			if isinstance(self.__input, str) or isinstance(self.__input, unicode):
				parseResult = self.__parser.traceParseStringChars(self.__input)
			elif isinstance(self.__input, list) or isinstance(self.__input, List):
				parseResult = self.__parser.traceParseListItems(self.__input)
			elif isinstance(self.__input, RichString):
				parseResult = self.__parser.traceParseRichStringItems(self.__input)
			elif isinstance(self.__input, DMNode):
				parseResult = self.__parser.traceParseNode(self.__input)
			else:
				raise TypeError, 'Cannot handle input of type %s' % type(self.__input)

			element = hyperlink.element
			subject = DefaultPerspective.instance.objectSubject(parseResult)
			element.rootElement.pageController.openSubject(subject, PageController.OpenOperation.OPEN_IN_NEW_WINDOW)



	@property
	def rule_name(self):
		self._incr.onAccess()
		return self._rule_name.getValue()

	@rule_name.setter
	def rule_name(self, name):
		self._rule_name.setLiteralValue(name)
		self._incr.onChanged()

	@property
	def inputCode(self):
		self._incr.onAccess()
		return self._inputCode

	@inputCode.setter
	def inputCode(self, x):
		self._inputCode = x
		self._incr.onChanged()


_inlineTestTableBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )



class GrammarTestTable (AbstractTestTable):
	def __init__(self, name='test'):
		super( GrammarTestTable, self ).__init__()
		self._tests = LiveList()
		self.__change_history__ = None



	def __getstate__(self):
		state = super( GrammarTestTable, self ).__getstate__()
		state['tests'] = self._tests
		return state

	def __setstate__(self, state):
		super( GrammarTestTable, self ).__setstate__( state )
		self._tests = state['tests']
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._tests ]



	def reset(self):
		for test in self._tests:
			test.reset()



	def run_tests(self, module, name_to_rule):
		for test in self._tests:
			test.run_test( module, name_to_rule )


	_resultColumn = AttributeColumn( 'Result', 'result' )
	_expectedColumn = AttributeColumn( 'Expected', 'expected', None, None )
	_ruleNameColumn = AttributeColumn('Rule name', 'rule_name', str)
	_inputCodeColumn = AttributeColumn('Input code', 'inputCode', EmbeddedPython2Suite)

	_tableEditor = ObjectListTableEditor([_resultColumn, _ruleNameColumn, _inputCodeColumn, _expectedColumn],
					     GrammarTestTableRow, True, True, True, True)


