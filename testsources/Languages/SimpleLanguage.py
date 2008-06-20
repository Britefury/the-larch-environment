##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f


from Britefury.Parser import Parser
from Britefury.Parser.GrammarUtils import Tokens
from Britefury.Parser.GrammarUtils import SeparatedList

from Britefury.GLisp.GLispDispatch import dispatch

from Britefury.gSym.gSymCodeGenerator import GSymCodeGenerator

from Britefury.gSym.View.Tokeniser import TokenDefinition, Tokeniser

from Britefury.gSym.View.gSymView import activeBorder, border, indent, highlight, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, wrappedHBox, wrappedHBoxSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import WrappedListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, tokenListEventMethod, Interactor

from Britefury.gSym.View.EditOperations import replace

from Britefury.gSym.View.gSymStyleSheet import GSymStyleSheet

from Britefury.gSym.View.UnparsedText import UnparsedText

from Britefury.gSym.gSymLanguage import GSymLanguage


class SimpleLanguageCodeGenerator (GSymCodeGenerator):
	def kwParam(self, node, name, value):
		return name + '=' + self( value )
	
	def call(self, node, target, *params):
		return self( target ) + '( ' + ', '.join( [ self( p )   for p in params ] ) + ' )'
	
	def subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	
	def slice(self, node, target, first, second):
		return self( target ) + '[' + self( first ) + ':' + self( second ) + ']'
	
	def attr(self, node, target, name):
		return self( target ) + '.' + name

	
	def add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	def sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	def mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	def div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	def mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	def pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	def loadLocal(self, node, name):
		return name
	
	def nilExpr(self, node):
		return '<NIL>'
	
	def listLiteral(self, node, *x):
		return '[ '  +  ', '.join( [ self( i )   for i in x ] )  +  ' ]'
	
	
	
	
	
divBoxStyle = GSymStyleSheet( alignment='expand' )
nilStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
unparsedStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )


tokeniser = Tokeniser( [] )


_paramName = Parser.Production( Tokens.identifier )
_attrName = Parser.Production( Tokens.identifier )



_expression = Parser.Forward()

_loadLocal = Parser.Production( Tokens.identifier ).action( lambda input, begin, xs: [ 'loadLocal', xs ] )

_kwParam = Parser.Production( _paramName + '=' + _expression ).action( lambda input, begin, xs: [ 'kwParam', xs[0], xs[2] ] )
_param = Parser.Production( _kwParam | _expression )
_parameterList = Parser.Production( Parser.Suppress( '(' )  -  SeparatedList.separatedList( _param )  -  Parser.Suppress( ')' ) )




_listLiteral = Parser.Production( Parser.Literal( '[' )  +  SeparatedList.separatedList( _expression )  +  Parser.Literal( ']' ) ).action( lambda input, begin, xs: [ 'listLiteral' ]  +  xs[1] )

_parenExp = Parser.Production( Parser.Literal( '(' ) + _expression + ')' ).action( lambda input, begin, xs: xs[1] )

_enclosure = Parser.Production( _parenExp | _listLiteral )

_atom = Parser.Production( _enclosure | _loadLocal )

_primary = Parser.Forward()

_call = Parser.Production( ( _primary + _parameterList ).action( lambda input, begin, tokens: [ 'call', tokens[0] ] + tokens[1] ) )
_subscript = Parser.Production( ( _primary + '[' + _expression + ']' ).action( lambda input, begin, tokens: [ 'subscript', tokens[0], tokens[2] ] ) )
_slice = Parser.Production( ( _primary + '[' + _expression + ':' + _expression + ']' ).action( lambda input, begin, tokens: [ 'slice', tokens[0], tokens[2], tokens[4] ] ) )
_attr = Parser.Production( _primary + '.' + _attrName ).action( lambda input, begin, tokens: [ 'attr', tokens[0], tokens[2] ] )
_primary  <<  Parser.Production( _call | _subscript | _slice | _attr | _atom )

_power = Parser.Forward()
_power  <<  Parser.Production( ( _primary  +  '**'  +  _power ).action( lambda input, begin, xs: [ 'pow', xs[0], xs[2] ] )   |   _primary )

_symToOp = { '+' : 'add',  '-' : 'sub',  '*' : 'mul',  '/' : 'div',  '%' : 'mod' }
_mulDivMod = Parser.Forward()
_mulDivMod  <<  Parser.Production( ( _mulDivMod + ( Parser.Literal( '*' ) | '/' | '%' ) + _power ).action( lambda input, begin, xs:  [ _symToOp[xs[1]], xs[0], xs[2] ] )  |  _power )
_addSub = Parser.Forward()
_addSub  <<  Parser.Production( ( _addSub + ( Parser.Literal( '+' ) | '-' ) + _mulDivMod ).action( lambda input, begin, xs: [ _symToOp[xs[1]], xs[0], xs[2] ] )  |  _mulDivMod )
			 
_expression  <<  Parser.Production( _addSub )



class ParsedNodeInteractor (Interactor):
	@textEventMethod()
	def tokData(self, value, node, parser):
		res, pos = parser.parseString( value )
		parsed = None
		if res is not None:
			if pos == len( value ):
				parsed = res.result
			else:
				print '<INCOMPLETE>'
		else:
			print '<FAIL>'
		if parsed is not None:
			return replace( node, parsed )
		else:
			print 'FULL TEXT:', value
			print 'PARSED:', value[:pos]
			return replace( node, [ 'UNPARSED', value ] )
	
	eventMethods = [ tokData ]


	

PRECEDENCE_ADDSUB = 3
PRECEDENCE_MULDIVMOD = 2
PRECEDENCE_POW = 1
PRECEDENCE_LOADLOCAL = 0
PRECEDENCE_LISTLITERAL = 0
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_SLICE = 0
PRECEDENCE_ATTR = 0
PRECEDENCE_KWPARAM = 0
	

def _paren(x):
	return '( ' + x + ' )'

def _unparseBinOpView(x, y, op, precedence, bRightAssociative=False):
	xPrec = x.state
	yPrec = y.state
	if precedence is not None:
		if bRightAssociative:
			if xPrec >= precedence:
				x = _paren( x )
			if yPrec > precedence:
				y = _paren( y )
		else:
			if xPrec > precedence:
				x = _paren( x )
			if yPrec >= precedence:
				y = _paren( y )
	return UnparsedText( x + ' ' + op + ' ' + y,  state=precedence )



def nodeEditor(node, contents, text, parser=_expression):
	if parser is None:
		parser = _expression
	return interact( customEntry( highlight( contents ), text.getText() ),  ParsedNodeInteractor( node, parser ) ),   text




class SimpleLanguageView (GSymView):
	def kwParam(self, state, node, name, value):
		return nodeEditor( node,
				   hbox( [ label( name ), label( '=' ), viewEval( value ) ] ),
				   UnparsedText( name  +  '='  +  valueView.text,  PRECEDENCE_KWPARAM ),
				   state )

	def call(self, state, node, target, *params):
		targetView = viewEval( target )
		paramViews = mapViewEval( params, _param )
		paramWidgets = []
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramWidgets.append( p )
				paramWidgets.append( label( ',' ) )
			paramWidgets.append( paramViews[-1] )
		return nodeEditor( node,
				   hbox( [ viewEval( target ), label( '(' ) ]  +  paramViews  +  [ label( ')' ) ] ),
				   UnparsedText( targetView.text + '( ' + UnparsedText( ', ' ).join( [ p.text   for p in paramViews ] ) + ' )',  PRECEDENCE_CALL ),
				   state )
	
	def subscript(self, state, node, target, index):
		targetView = viewEval( target )
		indexView = viewEval( index )
		return nodeEditor( node,
				   hbox( [ targetView,  label( '[' ),  indexView,  label( ']' ) ] ),
				   UnparsedText( targetView.text + '[' + indexView.text + ']',  PRECEDENCE_SUBSCRIPT ),
				   state )
	
	def slice(self, state, node, target, first, second):
		targetView = viewEval( target )
		firstView = viewEval( first )
		secondView = viewEval( second )
		return nodeEditor( node,
				   hbox( [ targetView,  label( '[' ),  firstView,  label( ':' ),  secondView,  label( ']' ) ] ),
				   UnparsedText( targetView.text + '[' + firstView.text + ':' + secondView.text + ']',  PRECEDENCE_SUBSCRIPT ),
				   state )
	
	def attr(self, state, node, target, name):
		targetView = viewEval( target )
		nameUnparsed = UnparsedText( name )
		nameLabel = label( name )
		nameUnparsed.associateWith( nameLabel )
		return nodeEditor( node,
				   hbox( [ viewEval( target ),  label( '.' ),  nameLabel ] ),
				   UnparsedText( targetView.text + '.' + nameUnparsed,  PRECEDENCE_ATTR ),
				   state )

	def add(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		unparsed = _unparseBinOpView( xView.text, yView.text, '+', PRECEDENCE_ADDSUB )
		return nodeEditor( node,
				   hbox( [ xView, label( '+' ), yView ] ),
				   unparsed,
				   state )

	def sub(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		unparsed = _unparseBinOpView( xView.text, yView.text, '-', PRECEDENCE_ADDSUB )
		return nodeEditor( node,
				   hbox( [ viewEval( x ), label( '-' ), viewEval( y ) ] ),
				   unparsed,
				   state )
	
	def mul(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		unparsed = _unparseBinOpView( xView.text, yView.text, '*', PRECEDENCE_MULDIVMOD )
		return nodeEditor( node,
				   hbox( [ viewEval( x ), label( '*' ), viewEval( y ) ] ),
				   unparsed,
				   state )
	
	def div(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		unparsed = _unparseBinOpView( xView.text, yView.text, '/', PRECEDENCE_MULDIVMOD )
		return nodeEditor( node,
				   vbox( [ viewEval( x ), hline(), viewEval( y ) ], divBoxStyle ),
				   unparsed,
				   state )
	
	def mod(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		unparsed = _unparseBinOpView( xView.text, yView.text, '%', PRECEDENCE_MULDIVMOD )
		return nodeEditor( node,
				   hbox( [ viewEval( x ), label( '%' ), viewEval( y ) ] ),
				   unparsed,
				   state )
	
	def pow(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		unparsed = _unparseBinOpView( xView.text, yView.text, '**', PRECEDENCE_POW )
		return nodeEditor( node,
				   scriptRSuper( viewEval( x ), viewEval( y ) ),
				   unparsed,
				   state )
	
	def loadLocal(self, state, node, name):
		nameUnparsed = UnparsedText( name )
		nameLabel = label( name )
		nameUnparsed.associateWith( nameLabel )
		return nodeEditor( node,
				   nameLabel,
				   nameUnparsed,
				   state )
	
	def nilExpr(self, state, node):
		return nodeEditor( node,
				   label( '<expr>' ),
				   UnparsedText( 'None' ),
				   state )
	
	def listLiteral(self, state, node, *xs):
		xViews = mapViewEval( xs )
		return nodeEditor( node,
			listView( VerticalListViewLayout( 0.0, 0.0, 45.0 ), '[', ']', ',', xViews ),
			UnparsedText( '[ '  +  UnparsedText( ', ' ).join( [ x.text   for x in xViews ] )  +  ' ]', PRECEDENCE_LISTLITERAL ),
			state )
	
	def UNPARSED(self, state, node, value):
		valueUnparsed = UnparsedText( name )
		valueLabel = label( '<' + value + '>', unparsedStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				   valueLabel,
				   valueUnparsed,
				   state )
	
	
	

	
language = GSymLanguage()
language.registerCodeGeneratorFactory( 'ascii', SimpleLanguageCodeGenerator )
language.registerViewFactory( SimpleLanguageView )
	

	