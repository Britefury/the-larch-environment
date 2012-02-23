##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import deepcopy

from java.lang import Throwable

from java.awt.event import KeyEvent

from java.awt import Color

from java.util import List

from BritefuryJ.Parser import ParserExpression

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Kernel.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, ObjectDispatchMethod


from BritefuryJ.DocModel import DMObjectClass, DMObject

from BritefuryJ.AttributeTable import *
from BritefuryJ.Controls import *
from BritefuryJ.DocPresent import ElementValueFunction, TextEditEvent, DPText
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor
from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder
from BritefuryJ.DocPresent.Input import ObjectDndHandler

from BritefuryJ.Pres import ApplyPerspective
from BritefuryJ.Pres.Primitive import *

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment
from BritefuryJ.Editor.SyntaxRecognizing.SRFragmentEditor import EditMode

from BritefuryJ.ModelAccess.DocModel import *



from LarchTools.PythonTools.SWYN import Schema
from LarchTools.PythonTools.SWYN.Parser import SWYNGrammar
from LarchTools.PythonTools.SWYN.SREditor import SWYNSyntaxRecognizingEditor


PRECEDENCE_NONE = -1

def swynReplaceNode(data, replacement):
	data.become( replacement )


def _isValidUnparsedValue(value):
	return True

def _commitUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	swynReplaceNode( model, deepcopy( unparsed ) )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	swynReplaceNode( model, deepcopy( unparsed ) )


#
#
# EDIT LISTENERS
#
#

class SWYNExpressionEditListener (ParsingEditListener):
	def getSyntaxRecognizingEditor(self):
		return SWYNSyntaxRecognizingEditor.instance

	def getLogName(self):
		return 'Top level expression'


	def handleEmptyValue(self, element, fragment, event, model):
		model['expr'] = Schema.UNPARSED( value=[ '' ] )
		return HandleEditResult.HANDLED

	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		expr = model['expr']
		if parsed != expr:
			model['expr'] = parsed
		return HandleEditResult.HANDLED

	def handleParseFailure(self, element, sourceElement, fragment, event, model, value):
		if '\n' not in value:
			values = value.getItemValues()
			if values == []:
				values = [ '' ]
			model['expr'] = Schema.UNPARSED( value=values )
			return HandleEditResult.HANDLED
		else:
			return HandleEditResult.NOT_HANDLED




def _setUnwrappedMethod(method, m):
	m.__dispatch_unwrapped_method__ = method
	m.__name__ = method.__name__
	return m


def Unparsed(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._unparsedFragmentEditor.editFragment( v, model, inheritedState )
	return _setUnwrappedMethod( method, _m )


def Expression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._expressionFragmentEditor.editFragment( v, model, inheritedState )
	return _setUnwrappedMethod( method, _m )



_unparsedTextStyle = StyleSheet.style( Primitive.textSquiggleUnderlinePaint( Color.RED ) )
_specialBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color.BLACK, Color( 1.0, 1.0, 0.75 ) )
_charClassBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color.BLACK, Color( 0.7, 0.8, 1.0 ) )
_rangeHypenStyle = StyleSheet.style( Primitive.foreground( Color.BLUE ), Primitive.fontBold( True ) )
_charSetBorder = SolidBorder( 1.0, 6.0, 4.0, 4.0, Color.BLACK, Color( 0.7, 0.7, 0.7 ) )
_groupParenStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ), Primitive.fontBold( True ) )
_groupBorder = SolidBorder( 1.0, 6.0, 4.0, 4.0, Color( 1.0, 0.7, 0.0 ), Color( 1.0, 1.0, 0.8 ) )
_notificationStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.0, 0.5 ) ) )


def unparseableText(text):
	return _unparsedTextStyle( Text( text ) )

def unparsedElements(elts):
	return Span( elts )

def escapedChar(char):
	return Span( [ Text( '\\' ), Text( char ) ] )

def anyChar():
	return _specialBorder.surround( Text( '.' ) )

def startOfLine():
	return _specialBorder.surround( Text( '^' ) )

def endOfLine():
	return _specialBorder.surround( Text( '$' ) )

def charClass(cls):
	return _charClassBorder.surround( Text( cls ) )

def charSetChar(char):
	return char

def charSetRange(min, max):
	return Span( [ min, _rangeHypenStyle( Text( '-' ) ), max ] )

def charSet(items):
	return _charSetBorder.surround( Column( items ) )

def group(subexp, capturing):
	contents = [ _notificationStyle( Text( '?:' ) ), subexp ]   if capturing   else [ subexp ]
	return _groupBorder.surround( Row( [ _groupParenStyle( Text( '(' ) ) ] + contents + [ _groupParenStyle( Text( '(' ) ) ] ) )




def _displayChar(char):
	if isinstance( char, DMObject ):
		if char.isInstanceOf( Schema.Node ):
			return SREInnerFragment( expr, PRECEDENCE_NONE, EditMode.DISPLAY )
		else:
			raise TypeError, 'unknown node type'
	else:
		return Text( char )



class SWYNView (MethodDispatchView):
	def __init__(self, grammar):
		super( SWYNView, self ).__init__()
		self._parser = grammar

		editor = SWYNSyntaxRecognizingEditor.instance

		self._expr = editor.parsingNodeEditListener( 'Expression', grammar.expression(), swynReplaceNode )
		self._exprOuter = SWYNExpressionEditListener( grammar.regex() )
		self._topLevel = editor.topLevelNodeEditListener()

		self._exprUnparsed = editor.unparsedNodeEditListener( 'Unparsed expression', _isValidUnparsedValue, _commitUnparsed, _commitInnerUnparsed )


		self._expressionFragmentEditor = editor.fragmentEditor( False, _pythonPrecedenceHandler, [ self._expr ] )
		self._unparsedFragmentEditor = editor.fragmentEditor( False, [ self._expr ] )





	@DMObjectNodeDispatchMethod( Schema.SWYNRegEx )
	def SWYNRegEx(self, fragment, inheritedState, model, expr):
		exprView = SREInnerFragment( expr, PRECEDENCE_NONE, EditMode.DISPLAY )
		seg = Segment( exprView )
		e = Paragraph( [ seg ] ).alignHPack().alignVRefY()
		e = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._exprOuter, self._topLevel ],  model,  e )
		return e


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	@Unparsed
	def UNPARSED(self, fragment, inheritedState, model, value):
		def _viewItem(x):
			if x is model:
				raise ValueError, 'SWYNView.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseableText( x )
				return view
			elif isinstance( x, DMObjectInterface ):
				view = SREInnerFragment( x, PRECEDENCE_CONTAINER_UNPARSED, EditMode.DISPLAY )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( x, view )
				return view
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsedElements( views )


	@DMObjectNodeDispatchMethod( Schema.EscapedChar )
	@Expression
	def EscapedChar(self, fragment, inheritedState, model, char):
		return escapedChar( char )


	@DMObjectNodeDispatchMethod( Schema.AnyChar )
	@Expression
	def AnyChar(self, fragment, inheritedState, model):
		return anyChar()


	@DMObjectNodeDispatchMethod( Schema.StartOfLine )
	@Expression
	def StartOfLine(self, fragment, inheritedState, model):
		return startOfLine()


	@DMObjectNodeDispatchMethod( Schema.EndOfLine )
	@Expression
	def EndOfLine(self, fragment, inheritedState, model):
		return endOfLine()


	@DMObjectNodeDispatchMethod( Schema.CharClass )
	@Expression
	def CharClass(self, fragment, inheritedState, model, cls):
		return charClass( cls )


	@DMObjectNodeDispatchMethod( Schema.CharSetChar )
	@Expression
	def CharSetChar(self, fragment, inheritedState, model, char):
		return charSetChar( _displayChar( char ) )


	@DMObjectNodeDispatchMethod( Schema.CharSet )
	@Expression
	def CharSet(self, fragment, inheritedState, model, items):
		itemViews = [ SREInnerFragment( item, PRECEDENCE_NONE, EditMode.DISPLAY )   for item in items ]
		return charSet( itemViews )


	@DMObjectNodeDispatchMethod( Schema.Group )
	@Expression
	def Group(self, fragment, inheritedState, model, subexp, capturing):
		subexpView = SREInnerFragment( subexp, PRECEDENCE_NONE, EditMode.DISPLAY )
		return group( subexpView, capturing is not None )




_parser = SWYNGrammar()
_view = SWYNView( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, SWYNSyntaxRecognizingEditor.instance )
