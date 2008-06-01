##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f


from Britefury.Parser import Parser

from Britefury.GLisp.GLispDispatch import dispatch

from Britefury.gSym.gSymCompiler import GSymCompiler
from Britefury.gSym.gSymUnparser import GSymUnparser

from Britefury.gSym.View.Tokeniser import TokenDefinition, Tokeniser

from Britefury.gSym.View.gSymView import activeBorder, border, indent, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, wrappedHBox, wrappedHBoxSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import WrappedListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, tokenListEventMethod, Interactor

from Britefury.gSym.View.EditOperations import replace

from Britefury.gSym.View.gSymStyleSheet import GSymStyleSheet

from Britefury.gSym.gSymLanguage import GSymLanguage


class SimpleLanguageCompiler (GSymCompiler):
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
	
	
	
	
	
def _paren(x):
	return '( ' + x + ' )'

def _unparseBinOp(x, y, op, precedence):
	xVal, xPrec = x
	yVal, yPrec = y
	if precedence is not None:
		if xPrec > precedence:
			xVal = _paren( xVal )
		if yPrec >= precedence:
			yVal = _paren( yVal )
	return xVal + ' ' + op + ' ' + yVal,  precedence

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
	

class SimpleLanguageUnparser (GSymUnparser):
	def kwParam(self, precedence, node, name, value):
		return name  +  '='  +  self.__dispatch( value )[0],  PRECEDENCE_KWPARAM
	
	def call(self, precedence, node, target, *params):
		return self.__dispatch( target )[0] + '( ' + ', '.join( [ self.__dispatch( p )[0]   for p in params ] ) + ' )',  PRECEDENCE_CALL
	
	def subscript(self, precedence, node, target, index):
		return self.__dispatch( target )[0] + '[' + self.__dispatch( index )[0] + ']',  PRECEDENCE_SUBSCRIPT
	
	def slice(self, precedence, node, target, first, second):
		return self.__dispatch( target )[0] + '[' + self.__dispatch( first )[0] + ':' + self.__dispatch( second )[0] + ']',  PRECEDENCE_SLICE
	
	def attr(self, precedence, node, target, name):
		return self.__dispatch( target )[0] + '.' + name,  PRECEDENCE_ATTR
	
	
	def add(self, precedence, node, x, y):
		return _unparseBinOp( self.__dispatch( x ), self.__dispatch( y ), '+', PRECEDENCE_ADDSUB )
	
	def sub(self, precedence, node, x, y):
		return _unparseBinOp( self.__dispatch( x ), self.__dispatch( y ), '-', PRECEDENCE_ADDSUB )
	
	def mul(self, precedence, node, x, y):
		return _unparseBinOp( self.__dispatch( x ), self.__dispatch( y ), '*', PRECEDENCE_MULDIVMOD )
	
	def div(self, precedence, node, x, y):
		return _unparseBinOp( self.__dispatch( x ), self.__dispatch( y ), '/', PRECEDENCE_MULDIVMOD )
	
	def mod(self, precedence, node, x, y):
		return _unparseBinOp( self.__dispatch( x ), self.__dispatch( y ), '%', PRECEDENCE_MULDIVMOD )
	
	def pow(self, precedence, node, x, y):
		return _unparseBinOp( self.__dispatch( x ), self.__dispatch( y ), '**', PRECEDENCE_POW )
	
	def loadLocal(self, precedence, node, name):
		return name, PRECEDENCE_LOADLOCAL
	
	def listLiteral(self, precedence, node, *xs):
		return '[ '  +  ', '.join( [ self.__dispatch( x )[0]   for x in xs ] )  +  ' ]',  PRECEDENCE_LISTLITERAL
	
	def nilExpr(self, precedence, node):
		return '<NIL>', PRECEDENCE_LOADLOCAL
	
	
	def UNPARSED(self, precedence, node, value):
		return value, 0
	
	
	def __dispatch(self, xs, precedence=None):
		return dispatch( self, xs, precedence )

	def __call__(self, xs):
		return self.__dispatch( xs )[0]
	
	
unparser = SimpleLanguageUnparser()
		
	
	

divBoxStyle = GSymStyleSheet( alignment='expand' )
nilStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
unparsedStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )


tokeniser = Tokeniser( [] )


_paramName = Parser.Production( Parser.identifier )
_attrName = Parser.Production( Parser.identifier )



_expression = Parser.Forward()

_loadLocal = Parser.Production( Parser.identifier ).action( lambda input, begin, xs: [ 'loadLocal', xs ] )

_kwParam = Parser.Production( _paramName + '=' + _expression ).action( lambda input, begin, xs: [ 'kwParam', xs[0], xs[2] ] )
_param = Parser.Production( _kwParam | _expression )
_parameterList = Parser.Production( Parser.Suppress( '(' )  -  Parser.delimitedList( _param )  -  Parser.Suppress( ')' ) )




_listLiteral = Parser.Production( Parser.Literal( '[' )  +  Parser.delimitedList( _expression )  +  Parser.Literal( ']' ) ).action( lambda input, begin, xs: [ 'listLiteral' ]  +  xs[1] )

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



class NodeInteractor (Interactor):
	@tokenListEventMethod( '' )
	def tokData(self, value, node):
		res, pos = _expression.parseString( value )
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



class ParsedNodeInteractor (Interactor):
	@tokenListEventMethod( '' )
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


	

def nodeEditor(view, node, contents):
	return interact( customEntry( contents, unparser( node ), tokeniser ),  NodeInteractor( node ) )

def viewNode(node, parser=_expression):
	return interact( customEntry( viewEval( node ), unparser( node ), tokeniser ),  ParsedNodeInteractor( node, parser ) )
	


class SimpleLanguageView (GSymView):
	def kwParam(self, node, name, value):
		return activeBorder( hbox( [ label( name ), label( '=' ), viewNode( value ) ] ) )

	def call(self, node, target, *params):
		paramViews = []
		if len( params ) > 0:
			for p in params[:-1]:
				paramViews.append( viewNode( p, _param ) )
				paramViews.append( label( ',' ) )
			paramViews.append( viewNode( params[-1], _param ) )
		return activeBorder( hbox( [ viewNode( target ), label( '(' ) ]  +  paramViews  +  [ label( ')' ) ] ) )
	
	def subscript(self, node, target, index):
		return activeBorder( hbox( [ viewNode( target ),  label( '[' ),  viewNode( index, _expression ),  label( ']' ) ] ) )
	
	def slice(self, node, target, first, second):
		return activeBorder( hbox( [ viewNode( target ),  label( '[' ),  viewNode( first ),  label( ':' ),  viewNode( second ),  label( ']' ) ] ) )
	
	def attr(self, node, target, name):
		return activeBorder( hbox( [ viewNode( target ),  label( '.' ),  label( name ) ] ) )

	def add(self, node, x, y):
		return activeBorder( hbox( [ viewNode( x ), label( '+' ), viewNode( y ) ] ) )

	def sub(self, node, x, y):
		return activeBorder( hbox( [ viewNode( x ), label( '-' ), viewNode( y ) ] ) )
	
	def mul(self, node, x, y):
		return activeBorder( hbox( [ viewNode( x ), label( '*' ), viewNode( y ) ] ) )
	
	def div(self, node, x, y):
		return activeBorder( vbox( [ viewNode( x ), hline(), viewNode( y ) ], divBoxStyle ) )
	
	def mod(self, node, x, y):
		return activeBorder( hbox( [ viewNode( x ), label( '%' ), viewNode( y ) ] ) )
	
	def pow(self, node, x, y):
		return activeBorder( scriptRSuper( viewNode( x ), hbox( [ label( '**' ), viewNode( y ) ] ) ) )
	
	def loadLocal(self, node, name):
		return activeBorder( label( name ) )
	
	def nilExpr(self, node):
		return activeBorder( label( '<expr>' ) )
	
	def listLiteral(self, node, *x):
		return listView( VerticalListViewLayout( 0.0, 0.0, 45.0 ), '[', ']', ',', mapViewEval( x ) )
	
	def UNPARSED(self, node, value):
		return activeBorder( label( '<' + value + '>', unparsedStyle ) )
	
	
	

	
language = GSymLanguage()
language.registerCompilerFactory( 'ascii', SimpleLanguageCompiler)
language.registerViewFactory( SimpleLanguageView )
	
	
	