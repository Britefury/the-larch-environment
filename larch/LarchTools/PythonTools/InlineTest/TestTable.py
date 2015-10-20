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


_resultFailStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.4, 0.0 ) ) )
_resultPass = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ).applyTo( Label( 'PASS' ) )
_resultNone = StyleSheet.style( Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ).applyTo( Label( 'NONE' ) )




class AbstractTestTableRow (object):
	def __init__(self):
		self.__result = LiveValue( _resultNone )
		# Form: either ( 'value', value )  or  ( 'exception', exceptionType )  or  None
		self._expected = TrackedLiveValue( None )
		self._actual = TrackedLiveValue( None )

		self.__change_history__ = None



	def __getstate__(self):
		return { 'expected' : self._expected.getStaticValue() }

	def __setstate__(self, state):
		self.__change_history__ = None
		self.__result = LiveValue( _resultNone )
		self._expected = TrackedLiveValue( state['expected'] )
		self._actual = TrackedLiveValue( None )


	def __get_trackable_contents__(self):
		return [ self._expected ]



	def reset(self):
		self.__result.setLiteralValue( _resultNone )
		self._actual.setLiteralValue( None )


	def _debug(self):
		return None


	def __debugWrap(self, p):
		d = self._debug()
		if d is not None:
			return Column( [ p, Pres.coerce( d ).alignHPack() ] ).alignVTop()
		else:
			return p


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



AbstractTestTableRow._resultColumn = AttributeColumn( 'Result', 'result' )
AbstractTestTableRow._expectedColumn = AttributeColumn( 'Expected', 'expected', None, None )





_nameBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.6, 0.6, 0.6 ), Color( 0.95, 0.95, 0.95 ) )
_notSet = StyleSheet.style( Primitive.fontItalic( True ) )( Label( 'not set' ) )
_inlineTestTableBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )


class AbstractTestTable (object):
	_tableEditor = NotImplemented


	def __init__(self, name='test'):
		super( AbstractTestTable, self ).__init__()
		self._name = TrackedLiveValue( name )
		self._tests = LiveList()
		self.__change_history__ = None



	def __getstate__(self):
		state = {}
		state['name'] = self._name.getStaticValue()
		state['tests'] = self._tests
		return state

	def __setstate__(self, state):
		self._name = TrackedLiveValue( state['name'] )
		self._tests = state['tests']
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._name, self._tests ]



	def reset(self):
		for test in self._tests:
			test.reset()




	__embed_hide_frame__ = True

	def __present__(self, fragment, inheritedState):
		title = SectionHeading2( 'Unit tests' )

		nameEntry = _nameBorder.surround( EditableLabel( self._name, _notSet ).regexValidated( Tokens.identifierPattern, 'Please enter a valid identifier' ) )

		header = Row( [ title, Spacer( 25.0, 0.0 ), nameEntry ] )

		if self._tableEditor is NotImplemented:
			raise NotImplementedError, 'Table editor is abstract'

		table = self._tableEditor.editTable( self._tests )

		return _inlineTestTableBorder.surround( Column( [ header, Spacer( 0.0, 5.0 ), table ] ) )





