##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import math

from copy import deepcopy

from java.lang import Throwable

from java.awt.event import KeyEvent

from java.awt import Color

from java.util import List

from BritefuryJ.Parser import ParserExpression

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Kernel.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, ObjectDispatchMethod, redecorateDispatchMethod


from BritefuryJ.DocModel import DMObjectClass, DMObject

from BritefuryJ.AttributeTable import *
from BritefuryJ.Controls import *
from BritefuryJ.Graphics import *
from BritefuryJ.LSpace.Interactor import KeyElementInteractor

from BritefuryJ.Pres import ApplyPerspective
from BritefuryJ.Pres.Primitive import *

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.EditFilter import HandleEditResult
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment, ParsingEditFilter
from BritefuryJ.Editor.SyntaxRecognizing.SyntaxRecognizingEditor import EditMode

from BritefuryJ.ModelAccess.DocModel import *



from LarchTools.PythonTools.VisualRegex import Schema
from LarchTools.PythonTools.VisualRegex.Parser import VisualRegexGrammar
from LarchTools.PythonTools.VisualRegex.SREditor import VisualRegexSyntaxRecognizingEditor


PRECEDENCE_NONE = -1

def vreReplaceNode(data, replacement):
	data.become( replacement )


def _isValidUnparsedValue(value):
	return True

def _commitUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since vreReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	vreReplaceNode( model, deepcopy( unparsed ) )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since vreReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	vreReplaceNode( model, deepcopy( unparsed ) )


def _isValidExprOrTargetOuterUnparsed(value):
	return '\n' not in value


def _commitExprOuterValid(model, parsed):
	expr = model['expr']
	if parsed != expr:
		model['expr'] = parsed

def _commitExprOuterEmpty(model, parsed):
	model['expr'] = Schema.UNPARSED( value=[ '' ] )

def _commitExprOuterUnparsed(model, value):
	values = value.getItemValues()
	if values == []:
		values = [ '' ]
	model['expr'] = Schema.UNPARSED( value=values )





_unparsedTextStyle = StyleSheet.style( Primitive.textSquiggleUnderlinePaint( Color.RED ) )
_controlCharStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.0, 0.5 ) ) )
_invertControlCharStyle = StyleSheet.style( Primitive.foreground( Color( 1.0, 0.0, 0.0, 0.5 ) ) )

_specialCharStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ), Primitive.fontSize( 10 ) )
_specialBorder = SolidBorder( 1.0, 1.0, 4.0, 4.0, Color( 0.0, 0.8, 0.0 ), Color( 0.9, 1.0, 0.9 ) )

_charClassStyle = StyleSheet.style( Primitive.foreground( Color( 0.25, 0.5, 0.0 ) ), Primitive.fontSize( 10 ) )
_charClassBorder = SolidBorder( 1.0, 1.0, 4.0, 4.0, Color( 0.4, 0.8, 0.0 ), Color( 0.95, 1.0, 0.9 ) )


#_charClassStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.25, 0.5 ) ) )
#_charClassEscapeStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.25, 0.5, 0.5 ) ) )
#_charClassBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 0.0, 0.25, 0.5 ), Color( 0.8, 0.9, 1.0 ) )

_groupNameStyle = StyleSheet.style( Primitive.fontItalic( True ) )
_commentStyle = StyleSheet.style( Primitive.foreground( Color( 0.3, 0.3, 0.3 ) ) )
_flagsStyle = StyleSheet.style( Primitive.foreground( Color( 1.0, 0.5, 0.0 ) ) )

_escapeBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 0.5, 0.6, 0.75 ), Color( 0.75, 0.85, 1.0 ) )
_pythonEscapeBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 0.75, 0.6, 0.5 ), Color( 1.0, 0.85, 0.75 ) )

_charSetBorder = SolidBorder( 1.0, 4.0, 3.0, 3.0,Color( 0.5, 0.5, 0.6 ), Color( 0.7, 0.7, 0.8 ) )
_charSetItemBorder = FilledBorder( 1.0, 1.0, 1.0, 1.0, Color( 0.9, 0.9, 0.9 ) )

_groupBorder = SolidBorder( 1.0, 4.0, 3.0, 3.0, Color( 1.0, 0.7, 0.4 ), Color( 1.0, 1.0, 0.8 ) )
_repeatBorder = SolidBorder( 1.0, 1.0, 3.0, 3.0, Color( 0.0, 0.7, 0.0 ), Color( 0.85, 1.0, 0.85 ) )
_choiceBorder = SolidBorder( 1.0, 4.0, 3.0, 3.0, Color( 0.7, 0.4, 1.0 ), Color( 0.9, 0.8, 1.0 ) )

_commentBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 0.4, 0.4, 0.4 ), Color( 0.9, 0.9, 0.9 ) )
_flagsBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 1.0, 0.6, 0.2 ), Color( 1.0, 1.0, 0.8 ) )


_charClasses = {
	'A' : 'START',
	'b' : 'WORD-START',
	'B' : '!WORD-START',
	'd' : 'DEC',
	'D' : '!DEC',
	's' : 'WS',
	'S' : '!WS',
	'w' : 'WORD',
	'W' : '!WORD',
	'Z' : 'END'
}


def unparseableText(text):
	return _unparsedTextStyle( Text( text ) )

def unparsedElements(elts):
	return Span( elts )

def literalChar(char):
	return Text( char )

def escapedChar(char):
	return _escapeBorder.surround( Row( [ _controlCharStyle( Text( '\\' ) ), Text( char ) ] ) )

def pythonEscapedChar(char):
	return _pythonEscapeBorder.surround( Row( [ _controlCharStyle( Text( '\\' ) ), Text( char ) ] ) )

def _specialChar(char, name):
	return _specialBorder.surround( _specialCharStyle( Segment( Row( [ HiddenText( char ), Label( name ) ] ) ) ) )

def anyChar():
	return _specialChar( '.', 'ANY' )

def startOfLine():
	return _specialChar( '^', 'SOL' )

def endOfLine():
	return _specialChar( '$', 'EOL' )

def charClass(cls):
	name = _charClasses[cls]
	return _charClassBorder.surround( _charClassStyle( Segment( Row( [ HiddenText( '\\' + cls ), Label( name ) ] ) ) ) )

def charSetChar(char):
	return _charSetItemBorder.surround( char )

def charSetRange(min, max):
	return _charSetItemBorder.surround( Row( [ min, _controlCharStyle( Text( '-' ) ), max ] ) )

def charSet(invert, items):
	items = [ Row( [ Segment( item ) ] )   for item in items ]
	if len( items ) == 1:
		itemsCollection = items[0]
	else:
		width = int( math.ceil( math.sqrt( float( len( items ) ) ) ) )
		itemsCollection = FlowGrid( width, items )
	contents = [ _controlCharStyle( Text( '[' ) ) ]
	if invert:
		contents.append( _invertControlCharStyle( Text( '^' ) ) )
	contents.extend( [ itemsCollection,  _controlCharStyle( Text( ']' ) ) ] )
	return _charSetBorder.surround( Row( contents ) )

def group(subexp, capturing):
	contents = [ subexp ]   if capturing   else [ _controlCharStyle( Text( '?:' ) ), subexp ]
	return _groupBorder.surround( Row( [ Segment( Span( [ _controlCharStyle( Text( '(' ) ) ] + contents + [ _controlCharStyle( Text( ')' ) ) ] ) ) ] ) )

def defineNamedGroup(subexp, name):
	groupName = _groupNameStyle( Text( '<' + name + '>' ) )
	return _groupBorder.surround( Row( [ _controlCharStyle( Text( '(?P' ) ), groupName, subexp, _controlCharStyle( Text( ')' ) ) ] ) )

def matchNamedGroup(name):
	groupName = _groupNameStyle( Text( name ) )
	return _groupBorder.surround( Row( [ _controlCharStyle( Text( '(?P=' ) ), groupName, _controlCharStyle( Text( ')' ) ) ] ) )

def lookahead(subexp, positive):
	return _groupBorder.surround( Row( [ _controlCharStyle( Text( '(?='   if positive   else '(?!' ) ), subexp, _controlCharStyle( Text( ')' ) ) ] ) )

def lookbehind(subexp, positive):
	return _groupBorder.surround( Row( [ _controlCharStyle( Text( '(?<='   if positive   else '(?<!' ) ), subexp, _controlCharStyle( Text( ')' ) ) ] ) )

def setFlags(flags):
	flagsText = _flagsStyle( Text( flags ) )
	return _flagsBorder.surround( Row( [ _controlCharStyle( Text( '(?' ) ), flagsText, _controlCharStyle( Text( ')' ) ) ] ) )

def comment(text):
	commentText = _commentStyle( Text( text ) )
	return _commentBorder.surround( Row( [ _controlCharStyle( Text( '(?#' ) ), commentText, _controlCharStyle( Text( ')' ) ) ] ) )

def _repetition(subexp, repetitions):
	return Script.scriptRSuper( Row( [ subexp ] ), _repeatBorder.surround( repetitions ) )

def repeat(subexp, repetitions):
	return _repetition( subexp, Row( [ _controlCharStyle( Text( '{' ) ), Text( repetitions ), _controlCharStyle( Text( '}' ) ) ] ) )

def zeroOrMore(subexp, greedy):
	return _repetition( subexp, Text( '*?'   if greedy   else '*' ) )

def oneOrMore(subexp, greedy):
	return _repetition( subexp, Text( '+?'   if greedy   else '+' ) )

def optional(subexp, greedy):
	return _repetition( subexp, Text( '??'   if greedy   else '?' ) )

def repeatRange(subexp, min, max, greedy):
	greedyness = []   if greedy   else [ _controlCharStyle( Text( '?' ) ) ]
	return _repetition( subexp, Row( [ _controlCharStyle( Text( '{' ) ), Text( min ),  _controlCharStyle( Text( ',' ) ), Text( max ), _controlCharStyle( Text( '}' ) ) ]  +  greedyness ) )

def sequence(subexps):
	return Paragraph( subexps )

def choice(subexps):
	rows = [ [ subexp, _controlCharStyle( Text( '|' ) ) ]   for subexp in subexps[:-1] ]  +  [ [ subexps[-1], None ] ]
	return _choiceBorder.surround( Table( rows ) )



def _displayNode(char):
	return SREInnerFragment( char, PRECEDENCE_NONE, EditMode.DISPLAY )



def _editTopLevelNode(char):
	return SREInnerFragment( char, PRECEDENCE_NONE, EditMode.EDIT )



def Unparsed(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._unparsedEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )


def Expression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._expressionEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )


def ExpressionTopLevel(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._expressionTopLevelEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )





class VREView (MethodDispatchView):
	def __init__(self, grammar):
		super( VREView, self ).__init__()
		self._parser = grammar

		editor = VisualRegexSyntaxRecognizingEditor.instance

		self._expr = editor.parsingEditFilter( 'Expression', grammar.regex(), vreReplaceNode )
		self._exprOuterValid = editor.parsingEditFilter( 'Expression-outer-valid', grammar.regex(), _commitExprOuterValid, _commitExprOuterEmpty )
		self._exprOuterInvalid = editor.unparsedEditFilter( 'Expression-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitExprOuterUnparsed )
		self._topLevel = editor.topLevelEditFilter()

		self._exprUnparsed = editor.unparsedEditFilter( 'Unparsed expression', _isValidUnparsedValue, _commitUnparsed, _commitInnerUnparsed )


		self._expressionEditRule = editor.editRule( [ self._expr, self._exprUnparsed ] )
		self._unparsedEditRule = editor.editRule( [ self._expr ] )
		self._expressionTopLevelEditRule = editor.softStructuralEditRule( [ self._exprOuterValid, self._exprOuterInvalid, self._topLevel ] )





	@DMObjectNodeDispatchMethod( Schema.PythonRegEx )
	@ExpressionTopLevel
	def PythonRegEx(self, fragment, inheritedState, model, expr):
		exprView =_editTopLevelNode( expr )
		seg = Segment( exprView )
		e = Paragraph( [ seg ] ).alignHPack().alignVRefY()
		return e


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	@Unparsed
	def UNPARSED(self, fragment, inheritedState, model, value):
		def _viewItem(x):
			if x is model:
				raise ValueError, 'VREView.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseableText( x )
				return view
			elif isinstance( x, DMObjectInterface ):
				view = SREInnerFragment( x, PRECEDENCE_CONTAINER_UNPARSED, EditMode.DISPLAY )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( VisualRegexSyntaxRecognizingEditor.instance, x, view )
				return view
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsedElements( views )



	@DMObjectNodeDispatchMethod( Schema.EscapedChar )
	@Expression
	def EscapedChar(self, fragment, inheritedState, model, char):
		return escapedChar( char )


	@DMObjectNodeDispatchMethod( Schema.PythonEscapedChar )
	@Expression
	def PythonEscapedChar(self, fragment, inheritedState, model, char):
		return pythonEscapedChar( char )


	@DMObjectNodeDispatchMethod( Schema.LiteralChar )
	@Expression
	def LiteralChar(self, fragment, inheritedState, model, char):
		return literalChar( char )



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
		return charSetChar( _displayNode( char ) )


	@DMObjectNodeDispatchMethod( Schema.CharSetRange )
	@Expression
	def CharSetRange(self, fragment, inheritedState, model, min, max):
		return charSetRange( _displayNode( min ), _displayNode( max ) )


	@DMObjectNodeDispatchMethod( Schema.CharSet )
	@Expression
	def CharSet(self, fragment, inheritedState, model, invert, items):
		itemViews = [ _displayNode( item )   for item in items ]
		return charSet( invert is not None, itemViews )



	@DMObjectNodeDispatchMethod( Schema.Group )
	@Expression
	def Group(self, fragment, inheritedState, model, subexp, capturing):
		subexpView = _displayNode( subexp )
		return group( subexpView, capturing is not None )


	@DMObjectNodeDispatchMethod( Schema.DefineNamedGroup )
	@Expression
	def DefineNamedGroup(self, fragment, inheritedState, model, subexp, name):
		subexpView = _displayNode( subexp )
		return defineNamedGroup( subexpView, name )


	@DMObjectNodeDispatchMethod( Schema.MatchNamedGroup )
	@Expression
	def MatchNamedGroup(self, fragment, inheritedState, model, name):
		return matchNamedGroup( name )



	@DMObjectNodeDispatchMethod( Schema.Lookahead )
	@Expression
	def Lookahead(self, fragment, inheritedState, model, subexp, positive):
		subexpView = _displayNode( subexp )
		return lookahead( subexpView, positive is not None )


	@DMObjectNodeDispatchMethod( Schema.Lookbehind )
	@Expression
	def Lookbehind(self, fragment, inheritedState, model, subexp, positive):
		subexpView = _displayNode( subexp )
		return lookbehind( subexpView, positive is not None )



	@DMObjectNodeDispatchMethod( Schema.SetFlags )
	@Expression
	def SetFlags(self, fragment, inheritedState, model, flags):
		return setFlags( flags )

	@DMObjectNodeDispatchMethod( Schema.Comment )
	@Expression
	def Comment(self, fragment, inheritedState, model, text):
		return comment( text )



	@DMObjectNodeDispatchMethod( Schema.Repeat )
	@Expression
	def Repeat(self, fragment, inheritedState, model, subexp, repetitions):
		subexpView = _displayNode( subexp )
		return repeat( subexpView, repetitions )


	@DMObjectNodeDispatchMethod( Schema.ZeroOrMore )
	@Expression
	def ZeroOrMore(self, fragment, inheritedState, model, subexp, greedy):
		subexpView = _displayNode( subexp )
		return zeroOrMore( subexpView, greedy is not None )


	@DMObjectNodeDispatchMethod( Schema.OneOrMore )
	@Expression
	def OneOrMore(self, fragment, inheritedState, model, subexp, greedy):
		subexpView = _displayNode( subexp )
		return oneOrMore( subexpView, greedy is not None )


	@DMObjectNodeDispatchMethod( Schema.Optional )
	@Expression
	def Optional(self, fragment, inheritedState, model, subexp, greedy):
		subexpView = _displayNode( subexp )
		return optional( subexpView, greedy is not None )


	@DMObjectNodeDispatchMethod( Schema.RepeatRange )
	@Expression
	def RepeatRange(self, fragment, inheritedState, model, subexp, min, max, greedy):
		subexpView = _displayNode( subexp )
		return repeatRange( subexpView, min, max, greedy is not None )



	@DMObjectNodeDispatchMethod( Schema.Sequence )
	@Expression
	def Sequence(self, fragment, inheritedState, model, subexps):
		subexpViews = [ _displayNode( subexp )   for subexp in subexps ]
		return sequence( subexpViews )


	@DMObjectNodeDispatchMethod( Schema.Choice )
	@Expression
	def Choice(self, fragment, inheritedState, model, subexps):
		subexpViews = [ _displayNode( subexp )   for subexp in subexps ]
		return choice( subexpViews )




_parser = VisualRegexGrammar()
_view = VREView( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, VisualRegexSyntaxRecognizingEditor.instance )
