##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f

from Britefury.gSym.View.gSymView import activeBorder, border, indent, highlight, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, wrappedHBox, wrappedHBoxSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import WrappedListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, tokenListEventMethod, Interactor

from Britefury.gSym.View.EditOperations import replace

from Britefury.gSym.View.gSymStyleSheet import GSymStyleSheet

from Britefury.gSym.View.UnparsedText import UnparsedText


from GSymCore.Languages.Python25.Parser import expression as expressionParser, arg as argParser



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


	

PRECEDENCE_OR = 14
PRECEDENCE_AND = 13
PRECEDENCE_NOT = 12
PRECEDENCE_IN = 11
PRECEDENCE_IS = 10
PRECEDENCE_CMP = 9
PRECEDENCE_BITOR = 8
PRECEDENCE_BITXOR = 7
PRECEDENCE_BITAND = 6
PRECEDENCE_SHIFT = 5
PRECEDENCE_ADDSUB = 4
PRECEDENCE_MULDIVMOD = 3
PRECEDENCE_INVERT_NEGATE_POS = 2
PRECEDENCE_POW = 1
PRECEDENCE_LOADLOCAL = 0
PRECEDENCE_LISTLITERAL = 0
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_SLICE = 0
PRECEDENCE_ATTR = 0
PRECEDENCE_ARG = 0
	

divBoxStyle = GSymStyleSheet( alignment='expand' )
nilStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
unparsedStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
numericLiteralStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.5, 0.5 ), font='Sans 11' )
literalFormatStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.0, 0.5 ), font='Sans 11' )
punctuationStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.0, 1.0 ), font='Sans 11' )
operatorStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.5, 0.0 ), font='Sans 11' )



def _paren(x):
	return '( ' + x + ' )'

def _unparsePrefixOpView(x, op, precedence):
	xPrec = x.state
	if precedence is not None:
		if xPrec > precedence:
			x = _paren( x )
	return UnparsedText( op + ' ' + x,  state=precedence )

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



def nodeEditor(node, contents, text, parser=expressionParser):
	if parser is None:
		parser = expressionParser
	return interact( customEntry( highlight( contents ), text.getText() ),  ParsedNodeInteractor( node, parser ) ),   text


def binOpView(state, node, x, y, unparsedOp, widgetFactory, precedence):
	xView = viewEval( x )
	yView = viewEval( y )
	unparsed = _unparseBinOpView( xView.text, yView.text, unparsedOp, precedence )
	return nodeEditor( node,
			widgetFactory( state, node, x, y, xView, yView ),
			unparsed,
			state )

def horizontalBinOpView(state, node, x, y, op, precedence):
	xView = viewEval( x )
	yView = viewEval( y )
	unparsed = _unparseBinOpView( xView.text, yView.text, op, precedence )
	return nodeEditor( node,
			ahbox( [ xView, label( op, operatorStyle ), yView ] ),
			unparsed,
			state )

def horizontalPrefixOpView(state, node, x, op, precedence):
	xView = viewEval( x )
	unparsed = _unparsePrefixOpView( xView.text, op, precedence )
	return nodeEditor( node,
			ahbox( [ label( op, operatorStyle ), xView ] ),
			unparsed,
			state )



class Python25View (GSymView):
	# STRING LITERALS
	def stringLiteral(self, state, node, format, quotation, value):
		valueUnparsed = UnparsedText( repr( value ) )
		valueLabel = label( value )
		valueUnparsed.associateWith( valueLabel )
		boxContents = []
		
		if format == 'ascii':
			pass
		elif format == 'unicode':
			boxContents.append( label( 'u', literalFormatStyle ) )
		elif format == 'ascii-regex':
			boxContents.append( label( 'r', literalFormatStyle ) )
		elif format == 'unicode-regex':
			boxContents.append( label( 'ur', literalFormatStyle ) )
		else:
			raise ValueError, 'invalid string literal format'
		
		if quotation == 'single':
			boxContents.append( label( "'", punctuationStyle ) )
			boxContents.append( None )
			boxContents.append( label( "'", punctuationStyle ) )
		else:
			boxContents.append( label( '"', punctuationStyle ) )
			boxContents.append( None )
			boxContents.append( label( '"', punctuationStyle ) )
			
		boxContents[-2] = valueLabel
		
		return nodeEditor( node,
				ahbox( boxContents ),
				valueUnparsed,
				state )
	
	
	def intLiteral(self, state, node, format, numType, value):
		boxContents = []
		
		if numType == 'int':
			if format == 'decimal':
				unparsed = '%d'  %  int( value )
			elif format == 'hex':
				unparsed = '%x'  %  int( value )
			valueLabel = label( unparsed, numericLiteralStyle )
			boxContents.append( valueLabel )
		elif numType == 'long':
			if format == 'decimal':
				unparsed = '%dL'  %  long( value )
			elif format == 'hex':
				unparsed = '%xL'  %  long( value )
			valueLabel = label( unparsed[:-1], numericLiteralStyle )
			boxContents.append( valueLabel )
			boxContents.append( label( 'L', literalFormatStyle ) )
			
		valueUnparsed = UnparsedText( unparsed )
		valueUnparsed.associateWith( valueLabel )

		return nodeEditor( node,
				ahbox( boxContents ),
				valueUnparsed,
				state )
	

	
	def floatLiteral(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( value, numericLiteralStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				valueLabel,
				valueUnparsed,
				state )
	

	
	def imaginaryLiteral(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( value, numericLiteralStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				valueLabel,
				valueUnparsed,
				state )
	

	
	def kwArg(self, state, node, name, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ label( name ), label( '=', punctuationStyle ), valueView ] ),
				UnparsedText( name  +  '='  +  valueView.text,  PRECEDENCE_ARG ),
				state )

	def argList(self, state, node, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ label( '*', punctuationStyle ), valueView ] ),
				UnparsedText( '*'  +  valueView.text,  PRECEDENCE_ARG ),
				state )

	def kwArgList(self, state, node, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ label( '**', punctuationStyle ), valueView ] ),
				UnparsedText( '**'  +  valueView.text,  PRECEDENCE_ARG ),
				state )

	def call(self, state, node, target, *params):
		targetView = viewEval( target )
		paramViews = mapViewEval( params, None, argParser )
		paramWidgets = []
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramWidgets.append( p )
				paramWidgets.append( label( ',', punctuationStyle ) )
			paramWidgets.append( paramViews[-1] )
		return nodeEditor( node,
				ahbox( [ viewEval( target ), label( '(', punctuationStyle ) ]  +  paramWidgets  +  [ label( ')', punctuationStyle ) ] ),
				UnparsedText( targetView.text + '( ' + UnparsedText( ', ' ).join( [ p.text   for p in paramViews ] ) + ' )',  PRECEDENCE_CALL ),
				state )
	
	def subscript(self, state, node, target, index):
		targetView = viewEval( target )
		indexView = viewEval( index )
		return nodeEditor( node,
				ahbox( [ targetView,  label( '[' ),  indexView,  label( ']' ) ] ),
				UnparsedText( targetView.text + '[' + indexView.text + ']',  PRECEDENCE_SUBSCRIPT ),
				state )
	
	def slice(self, state, node, target, first, second):
		targetView = viewEval( target )
		firstView = viewEval( first )
		secondView = viewEval( second )
		return nodeEditor( node,
				ahbox( [ targetView,  label( '[' ),  firstView,  label( ':' ),  secondView,  label( ']' ) ] ),
				UnparsedText( targetView.text + '[' + firstView.text + ':' + secondView.text + ']',  PRECEDENCE_SUBSCRIPT ),
				state )
	
	def attr(self, state, node, target, name):
		targetView = viewEval( target )
		nameUnparsed = UnparsedText( name )
		nameLabel = label( name )
		nameUnparsed.associateWith( nameLabel )
		return nodeEditor( node,
				ahbox( [ viewEval( target ),  label( '.' ),  nameLabel ] ),
				UnparsedText( targetView.text + '.' + nameUnparsed,  PRECEDENCE_ATTR ),
				state )

	
	
	def pow(self, state, node, x, y):
		return binOpView( state, node, x, y, '**',
				lambda state, node, x, y, xView, yView: scriptRSuper( xView, yView ),
				PRECEDENCE_POW )
	
	
	def invert(self, state, node, x):
		return horizontalPrefixOpView( state, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS )
	
	def negate(self, state, node, x):
		return horizontalPrefixOpView( state, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS )
	
	def pos(self, state, node, x):
		return horizontalPrefixOpView( state, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS )
	
	
	def mul(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '*', PRECEDENCE_MULDIVMOD )
	
	def div(self, state, node, x, y):
		return binOpView( state, node, x, y, '/',
				  lambda state, node, x, y, xView, yView: vbox( [ xView, hline( operatorStyle ), yView ], divBoxStyle ),
				  PRECEDENCE_POW )
	
	def mod(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '%', PRECEDENCE_MULDIVMOD )
	
	def add(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '+', PRECEDENCE_ADDSUB )

	def sub(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '-', PRECEDENCE_ADDSUB )
	
	
	def lshift(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '<<', PRECEDENCE_SHIFT )

	def rshift(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '>>', PRECEDENCE_SHIFT )

	
	def bitAnd(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '&', PRECEDENCE_BITAND )

	def bitXor(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '^', PRECEDENCE_BITXOR )

	def bitOr(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '|', PRECEDENCE_BITOR )

	
	def lte(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '<=', PRECEDENCE_CMP )

	def lt(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '<', PRECEDENCE_CMP )

	def gte(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '>=', PRECEDENCE_CMP )

	def gt(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '>', PRECEDENCE_CMP )

	def eq(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '==', PRECEDENCE_CMP )

	def neq(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, '!=', PRECEDENCE_CMP )


	def cmpIsNot(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, 'is not', PRECEDENCE_IS )

	def cmpIs(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, 'is', PRECEDENCE_IS )

	def cmpNotIn(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, 'not in', PRECEDENCE_IN )

	def cmpIn(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, 'in', PRECEDENCE_IN )


	def boolNot(self, state, node, x):
		return horizontalPrefixOpView( state, node, x, 'not', PRECEDENCE_NOT )
	
	def boolAnd(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, 'and', PRECEDENCE_AND )

	def boolOr(self, state, node, x, y):
		return horizontalBinOpView( state, node, x, y, 'or', PRECEDENCE_OR )


	def var(self, state, node, name):
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
	
	def listDisplay(self, state, node, *xs):
		xViews = mapViewEval( xs )
		return nodeEditor( node,
				   listView( VerticalListViewLayout( 0.0, 0.0, 45.0 ), '[', ']', ',', xViews ),
				   UnparsedText( '[ '  +  UnparsedText( ', ' ).join( [ x.text   for x in xViews ] )  +  ' ]', PRECEDENCE_LISTLITERAL ),
				   state )

	
	
	
	def python25Document(self, state, node, content):
		return viewEval( content )
	
	
	
	
	def UNPARSED(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( '<' + value + '>', unparsedStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				valueLabel,
				valueUnparsed,
				state )
	
	
	

