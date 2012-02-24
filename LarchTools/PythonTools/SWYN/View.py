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
from BritefuryJ.Graphics import *
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
from BritefuryJ.Editor.Sequential.EditListener import HandleEditResult
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment, ParsingEditListener
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
_controlCharStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.0, 0.5 ) ) )

_specialCharStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0, 0.5 ) ), Primitive.fontSize( 10 ) )
_specialCharPurposeStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ), Primitive.fontSize( 10 ) )
_specialBorder = SolidBorder( 1.0, 1.0, 4.0, 4.0, Color( 0.0, 0.8, 0.0 ), Color( 0.9, 1.0, 0.9 ) )

_charClassStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.25, 0.5 ) ) )
_charClassEscapeStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.25, 0.5, 0.5 ) ) )
_charClassBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 0.0, 0.25, 0.5 ), Color( 0.8, 0.9, 1.0 ) )

_groupNameStyle = StyleSheet.style( Primitive.fontItalic( True ) )
_commentStyle = StyleSheet.style( Primitive.foreground( Color( 0.2, 0.2, 0.2, 0.5 ) ) )
_flagsStyle = StyleSheet.style( Primitive.foreground( Color( 1.0, 0.5, 0.0 ) ) )

_escapeBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color.BLACK, Color( 1.0, 0.85, 0.75 ) )
_charSetBorder = SolidBorder( 1.0, 6.0, 4.0, 4.0, Color.BLACK, Color( 0.7, 0.7, 0.7 ) )
_groupBorder = SolidBorder( 1.0, 6.0, 4.0, 4.0, Color( 1.0, 0.7, 0.0 ), Color( 1.0, 1.0, 0.8 ) )
_repeatBorder = SolidBorder( 1.0, 6.0, 4.0, 4.0, Color( 0.0, 0.7, 0.0 ), Color( 0.8, 1.0, 0.8 ) )
_choiceBorder = SolidBorder( 1.0, 6.0, 4.0, 4.0, Color.BLACK, Color( 0.8, 0.6, 1.0 ) )

_commentBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 0.4, 0.4, 0.4 ), Color( 0.9, 0.9, 0.9 ) )
_flagsBorder = SolidBorder( 1.0, 2.0, 4.0, 4.0, Color( 1.0, 0.6, 0.2 ), Color( 1.0, 1.0, 0.8 ) )

_swynBorder = SolidBorder( 3.0, 10.0, 10.0, 10.0, Color( 0.8, 1.0, 0.8 ), None )



def unparseableText(text):
	return _unparsedTextStyle( Text( text ) )

def unparsedElements(elts):
	return Span( elts )

def literalChar(char):
	return Text( char )

def escapedChar(char):
	return _escapeBorder.surround( Row( [ _controlCharStyle( Text( '\\' ) ), Text( char ) ] ) )

def anyChar():
	return _specialBorder.surround( Row( [ _specialCharStyle( Text( '.' ) ), _specialCharPurposeStyle( Label( ' ANY' ) ) ] ) )

def startOfLine():
	return _specialBorder.surround( Row( [ _specialCharStyle( Text( '^' ) ), _specialCharPurposeStyle( Label( ' SOL' ) ) ] ) )

def endOfLine():
	return _specialBorder.surround( Row( [ _specialCharStyle( Text( '$' ) ), _specialCharPurposeStyle( Label( ' EOL' ) ) ] ) )

def charClass(cls):
	return _charClassBorder.surround( Row( [ _charClassEscapeStyle( Text( '\\' ) ), _charClassStyle( Text( cls ) ) ] ) )

def charSetChar(char):
	return char

def charSetRange(min, max):
	return Row( [ min, _controlCharStyle( Text( '-' ) ), max ] )

def charSet(items):
	return _charSetBorder.surround( Row( [ _controlCharStyle( Text( '[' ) ), Column( items ), _controlCharStyle( Text( ']' ) ) ] ) )

def group(subexp, capturing):
	contents = [ subexp ]   if capturing   else [ _controlCharStyle( Text( '?:' ) ), subexp ]
	return _groupBorder.surround( Row( [ _controlCharStyle( Text( '(' ) ) ] + contents + [ _controlCharStyle( Text( ')' ) ) ] ) )

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
	return Script.scriptRSuper( _repeatBorder.surround( Row( [ subexp ] ) ), repetitions )

def repeat(subexp, repetitions):
	return _repetition( subexp, Text( '{' + repetitions + '}' ) )

def zeroOrMore(subexp, greedy):
	return _repetition( subexp, Text( '*?'   if greedy   else '*' ) )

def oneOrMore(subexp, greedy):
	return _repetition( subexp, Text( '+?'   if greedy   else '+' ) )

def optional(subexp, greedy):
	return _repetition( subexp, Text( '??'   if greedy   else '?' ) )

def repeatRange(subexp, min, max, greedy):
	return _repetition( subexp, Text( '{%s,%s}' % ( min, max )  + ( ''   if greedy   else '?' ) ) )

def sequence(subexps):
	return Paragraph( subexps )

def choice(subexps):
	rows = [ [ subexp, _controlCharStyle( Text( '|' ) ) ]   for subexp in subexps[:-1] ]  +  [ [ subexps[-1], None ] ]
	return _choiceBorder.surround( Table( rows ) )



def _displayNode(char):
	return SREInnerFragment( char, PRECEDENCE_NONE, EditMode.DISPLAY )



def _editTopLevelNode(char):
	return SREInnerFragment( char, PRECEDENCE_NONE, EditMode.EDIT )



class SWYNView (MethodDispatchView):
	def __init__(self, grammar):
		super( SWYNView, self ).__init__()
		self._parser = grammar

		editor = SWYNSyntaxRecognizingEditor.instance

		self._expr = editor.parsingNodeEditListener( 'Expression', grammar.regex(), swynReplaceNode )
		self._exprOuter = SWYNExpressionEditListener( grammar.regex() )
		self._topLevel = editor.topLevelNodeEditListener()

		self._exprUnparsed = editor.unparsedNodeEditListener( 'Unparsed expression', _isValidUnparsedValue, _commitUnparsed, _commitInnerUnparsed )


		self._expressionFragmentEditor = editor.fragmentEditor( False, [ self._expr, self._exprUnparsed ] )
		self._unparsedFragmentEditor = editor.fragmentEditor( False, [ self._expr ] )





	@DMObjectNodeDispatchMethod( Schema.SWYNRegEx )
	def SWYNRegEx(self, fragment, inheritedState, model, expr):
		exprView =_editTopLevelNode( expr )
		seg = Segment( exprView )
		e = Paragraph( [ seg ] ).alignHPack().alignVRefY()
		e = _swynBorder.surround( e )
		e = EditableStructuralItem( SWYNSyntaxRecognizingEditor.instance, [ self._exprOuter, self._topLevel ],  model,  e )
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
	def CharSet(self, fragment, inheritedState, model, items):
		itemViews = [ _displayNode( item )   for item in items ]
		return charSet( itemViews )



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




_parser = SWYNGrammar()
_view = SWYNView( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, SWYNSyntaxRecognizingEditor.instance )
