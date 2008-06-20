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


from GSymCore.Languages.Python25.Parser import _expression, _param



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
	

divBoxStyle = GSymStyleSheet( alignment='expand' )
nilStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
unparsedStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
numericLiteralStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.5, 0.5 ), font='Sans 11' )
literalFormatStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.0, 0.5 ), font='Sans 11' )
stringLiteralQuotationStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.5, 0.0 ), font='Sans 11' )



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
			boxContents.append( label( "'", stringLiteralQuotationStyle ) )
			boxContents.append( None )
			boxContents.append( label( "'", stringLiteralQuotationStyle ) )
		else:
			boxContents.append( label( '"', stringLiteralQuotationStyle ) )
			boxContents.append( None )
			boxContents.append( label( '"', stringLiteralQuotationStyle ) )
			
		boxContents[-2] = valueLabel
		
		return nodeEditor( node,
				   hbox( boxContents ),
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
				   hbox( boxContents ),
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
	
	def UNPARSED(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( '<' + value + '>', unparsedStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				   valueLabel,
				   valueUnparsed,
				   state )
	
	
	

