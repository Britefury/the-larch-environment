##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.View.gSymView import activeBorder, border, indent, highlight, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, flow, flowSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import FlowListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, backspaceStartMethod, deleteEndMethod, Interactor

from Britefury.gSym.View.EditOperations import replace, append, prepend, insertBefore, insertAfter

from Britefury.gSym.View.UnparsedText import UnparsedText

import traceback


from GSymCore.Languages.Python25.Parser import targetItem as targetItemParser, targetList as targetListParser, listComprehensionItem as listComprehensionItemParser, generatorExpressionItem as generatorExpressionItemParser, keyValuePair as keyValuePairParser, subscriptItem as subscriptItemParser, subscriptIndex as subscriptIndexParser, callArg as callArgParser, param as paramParser, expression as expressionParser, oldExpression as oldExpressionParser, oldTupleOrExpression as oldTupleOrExpressionParser, orTest as orTestParser, tupleOrExpression as tupleOrExpressionParser, tupleOrExpressionOrYieldExpression as tupleOrExpressionOrYieldExpressionParser, statement as statementParser
from GSymCore.Languages.Python25.Styles import *
from GSymCore.Languages.Python25.Keywords import *




def _mixedCaps(x):
	x = x.upper()
	return x[0] + '<span size="small">' + x[1:] + '</span>'


def keywordLabel(keyword):
	return markupLabel( _mixedCaps( keyword ), keywordStyle )



def _parseText(parser, text):
	res, pos = parser.parseString( text )
	if res is not None:
		if pos == len( text ):
			return res.result
		else:
			print '<INCOMPLETE>'
			print 'FULL TEXT:', text
			print 'PARSED:', text[:pos]
			return None
	else:
		print 'FULL TEXT:', text
		print '<FAIL>'
		return None


class ParsedNodeInteractor (Interactor):
	@textEventMethod()
	def tokData(self, bUserEvent, bChanged, value, node, parser):
		if bChanged:
			parsed = _parseText( parser, value )
			if parsed is not None:
				replace( node, parsed )
			else:
				replace( node, [ 'UNPARSED', value ] )
	
	eventMethods = [ tokData ]



def _isSuiteStmt(node):
	return node[0] == 'ifStmt'
	

class ParsedLineInteractor (Interactor):
	@textEventMethod()
	def tokData(self, bUserEvent, bChanged, value, node, parser):
		if bChanged:
			if value.strip() == '':
				node = replace( node, [ 'blankLine' ] )
			else:
				parsed = _parseText( parser, value )
				if parsed is not None:
					if _isSuiteStmt( parsed ):
						print 'Parsed is a suite statement'
						if _isSuiteStmt( node ):
							print 'Original is a suite statement'
							parsed[-1] = node[-1]
						node = replace( node, parsed )
						if bUserEvent:
							print 'Inserting blankline into suite'
							return prepend( node[-1], [ 'blankLine' ] )
						else:
							return node
					else:
						node = replace( node, parsed )
				else:
					node = replace( node, [ 'UNPARSED', value ] )
		if bUserEvent:
			print 'Inserting...'
			return insertAfter( node, [ 'blankLine' ] )
		
		
		
	@backspaceStartMethod()
	def backspaceStart(self, node, parser):
		print 'Backspace start'
	

	@deleteEndMethod()
	def deleteEnd(self, node, parser):
		print 'Delete end'

	eventMethods = [ tokData, backspaceStart, deleteEnd ]


	

PRECEDENCE_TUPLE = 200

PRECEDENCE_STMT = 100

PRECEDENCE_LAMBDAEXPR = 50

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
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_ATTR = 0

PRECEDENCE_LOADLOCAL = 0
PRECEDENCE_LISTLITERAL = 0
PRECEDENCE_LISTCOMPREHENSION = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_CONDITIONALEXPRESSION = 0
PRECEDENCE_DICTLITERAL = 0
PRECEDENCE_YIELDEXPR = 0

PRECEDENCE_SUBSCRIPTSLICE = 0
PRECEDENCE_ARG = 0
PRECEDENCE_PARAM = 0


	




def _paren(x):
	return '( ' + x + ' )'

def _unparsePrecedenceGT(x, outerPrecedence):
	if outerPrecedence is not None  and  x.state is not None  and  x.state > outerPrecedence:
		return _paren( x )
	else:
		return x

def _unparsePrecedenceGTE(x, outerPrecedence):
	if outerPrecedence is not None  and  x.state is not None  and  x.state >= outerPrecedence:
		return _paren( x )
	else:
		return x

def _unparsePrefixOpView(x, op, precedence):
	x = _unparsePrecedenceGT( x, precedence )
	return UnparsedText( op + ' ' + x,  state=precedence )

def _unparseBinOpView(x, y, op, precedence, bRightAssociative=False):
	if bRightAssociative:
		x = _unparsePrecedenceGTE( x, precedence )
		y = _unparsePrecedenceGT( y, precedence )
	else:
		x = _unparsePrecedenceGT( x, precedence )
		y = _unparsePrecedenceGTE( y, precedence )
	return UnparsedText( x + ' ' + op + ' ' + y,  state=precedence )

def _unparsedListViewNeedsDelims(x, outerPrecedence):
	return outerPrecedence is not None  and  x.state is not None  and  x.state > outerPrecedence



MODE_EXPRESSION = 0
MODE_LINE = 1



def python25ViewState(parser, mode=MODE_EXPRESSION):
	return parser, mode



def nodeEditor(node, contents, text, state):
	if state is None:
		parser = expressionParser
		mode = MODE_EXPRESSION
	else:
		parser, mode = state

	if mode == MODE_EXPRESSION:
		return interact( focus( customEntry( highlight( contents, 'ctrl', 'ctrl' ), text.getText(), 'ctrl', 'ctrl' ) ),  ParsedNodeInteractor( node, parser ) ),   text
	elif mode == MODE_LINE:
		return interact( focus( customEntry( highlight( contents, style=lineEditorStyle ), text.getText() ) ),  ParsedLineInteractor( node, parser ) ),   text
	else:
		raise ValueError
		


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


def tupleView(state, node, xs, parser=None):
	def tupleWidget(x):
		if x.text.state == PRECEDENCE_TUPLE:
			return ahbox( [ label( '(', punctuationStyle ), x, label( ')', punctuationStyle ) ] )
		else:
			return x
	def tupleText(x):
		if x.state == PRECEDENCE_TUPLE:
			return '(' + x + ')'
		else:
			return x
	if parser is not None:
		xViews = mapViewEval( xs, None, python25ViewState( parser ) )
	else:
		xViews = mapViewEval( xs )
	xWidgets = [ tupleWidget( x )   for x in xViews ]
	xTexts = [ tupleText( x.text )   for x in xViews ]
	return nodeEditor( node,
			   listView( FlowListViewLayout( 5.0, 0.0 ), None, None, ',', xWidgets ),
			   UnparsedText( UnparsedText( ', ' ).join( [ x   for x in xTexts ] ), PRECEDENCE_TUPLE ),
			   state )


class Python25View (GSymView):
	# MISC
	def python25Module(self, state, node, *content):
		lineViews = mapViewEval( content, None, python25ViewState( statementParser, MODE_LINE ) )
		return listView( VerticalListViewLayout( 0.0, 0.0, 0.0 ), None, None, None, lineViews ), ''
	

	
	def nilExpr(self, state, node):
		return nodeEditor( node,
				label( '<expr>' ),
				UnparsedText( 'None' ),
				state )
	
	
	def blankLine(self, state, node):
		return nodeEditor( node,
				label( ' ' ),
				UnparsedText( '' ),
				state )
	
	
	def UNPARSED(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( '<' + value + '>', unparsedStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				valueLabel,
				valueUnparsed,
				state )
	
	
	# String literal
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
	
	
	# Integer literal
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
	

	
	# Float literal
	def floatLiteral(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( value, numericLiteralStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				valueLabel,
				valueUnparsed,
				state )
	

	
	# Imaginary literal
	def imaginaryLiteral(self, state, node, value):
		valueUnparsed = UnparsedText( value )
		valueLabel = label( value, numericLiteralStyle )
		valueUnparsed.associateWith( valueLabel )
		return nodeEditor( node,
				valueLabel,
				valueUnparsed,
				state )
	

	
	# Targets
	def singleTarget(self, state, node, name):
		nameUnparsed = UnparsedText( name )
		nameLabel = label( name )
		nameUnparsed.associateWith( nameLabel )
		return nodeEditor( node,
				nameLabel,
				nameUnparsed,
				state )
	
	def tupleTarget(self, state, node, *xs):
		return tupleView( state, node, xs, targetItemParser )
	
	def listTarget(self, state, node, *xs):
		xViews = mapViewEval( xs, None, python25ViewState( targetItemParser ) )
		return nodeEditor( node,
				   listView( FlowListViewLayout( 5.0, 0.0 ), '[', ']', ',', xViews ),
				   UnparsedText( '[ '  +  UnparsedText( ', ' ).join( [ x.text   for x in xViews ] )  +  ' ]', PRECEDENCE_LISTLITERAL ),
				   state )

	
	
	# Variable reference
	def var(self, state, node, name):
		nameUnparsed = UnparsedText( name )
		nameLabel = label( name )
		nameUnparsed.associateWith( nameLabel )
		return nodeEditor( node,
				nameLabel,
				nameUnparsed,
				state )
	

	
	# Tuple literal
	def tupleLiteral(self, state, node, *xs):
		return tupleView( state, node, xs )

	
	
	# List literal
	def listLiteral(self, state, node, *xs):
		xViews = mapViewEval( xs )
		return nodeEditor( node,
				   listView( FlowListViewLayout( 5.0, 0.0 ), '[', ']', ',', xViews ),
				   UnparsedText( '[ '  +  UnparsedText( ', ' ).join( [ x.text   for x in xViews ] )  +  ' ]', PRECEDENCE_LISTLITERAL ),
				   state )

	
	
	# List comprehension
	def listFor(self, state, node, target, source):
		targetView = viewEval( target, None, python25ViewState( targetListParser ) )
		sourceView = viewEval( source, None, python25ViewState( oldTupleOrExpressionParser ) )
		return nodeEditor( node,
				   ahbox( [ keywordLabel( forKeyword ), targetView, keywordLabel( inKeyword ), sourceView ] ),
				   UnparsedText( forKeyword  +  ' '  +  targetView.text  +  ' '  +  inKeyword  +  sourceView.text, PRECEDENCE_LISTCOMPREHENSION ),
				   state )
	
	def listIf(self, state, node, condition):
		conditionView = viewEval( condition, None, python25ViewState( oldExpressionParser ) )
		return nodeEditor( node,
				   ahbox( [ keywordLabel( ifKeyword ), conditionView ] ),
				   UnparsedText( ifKeyword  +  ' '  +  conditionView.text, PRECEDENCE_LISTCOMPREHENSION ),
				   state )
	
	def listComprehension(self, state, node, expr, *xs):
		exprView = viewEval( expr )
		xViews = mapViewEval( xs, None, python25ViewState( listComprehensionItemParser ) )
		return nodeEditor( node,
				   ahbox( [ label( '[', punctuationStyle ),  ahbox( [ exprView ]  +  xViews, spacing=15.0 ), label( ']', punctuationStyle ) ] ),
				   UnparsedText( '[ '  +  exprView.text  +  '   '  +  UnparsedText( '   ' ).join( [ x.text   for x in xViews ] )  +  ' ]', PRECEDENCE_LISTCOMPREHENSION ),
				   state )
	
	
	
	# List comprehension
	def genFor(self, state, node, target, source):
		targetView = viewEval( target, None, python25ViewState( targetListParser ) )
		sourceView = viewEval( source, None, python25ViewState( orTestParser ) )
		return nodeEditor( node,
				   ahbox( [ keywordLabel( forKeyword ), targetView, keywordLabel( inKeyword ), sourceView ] ),
				   UnparsedText( forKeyword  +  ' '  +  targetView.text  +  ' '  +  inKeyword  +  sourceView.text, PRECEDENCE_GENERATOREXPRESSION ),
				   state )
	
	def genIf(self, state, node, condition):
		conditionView = viewEval( condition, None, python25ViewState( oldExpressionParser ) )
		return nodeEditor( node,
				   ahbox( [ keywordLabel( ifKeyword ), conditionView ] ),
				   UnparsedText( ifKeyword  +  ' '  +  conditionView.text, PRECEDENCE_GENERATOREXPRESSION ),
				   state )
	
	def generatorExpression(self, state, node, expr, *xs):
		exprView = viewEval( expr )
		xViews = mapViewEval( xs, None, python25ViewState( generatorExpressionItemParser ) )
		return nodeEditor( node,
				   ahbox( [ label( '(', punctuationStyle ),  exprView ]  +  xViews  +  [ label( ')', punctuationStyle ) ], spacing=15.0 ),
				   UnparsedText( '( '  +  exprView.text  +  '   '  +  UnparsedText( '   ' ).join( [ x.text   for x in xViews ] )  +  ' ]', PRECEDENCE_GENERATOREXPRESSION ),
				   state )
	
	
	
	# Dictionary literal
	def keyValuePair(self, state, node, key, value):
		keyView = viewEval( key )
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ keyView, label( ':', punctuationStyle ), valueView ] ),
				UnparsedText( keyView.text  +  ':'  +  valueView.text,  PRECEDENCE_DICTLITERAL ),
				state )

	def dictLiteral(self, state, node, *xs):
		xViews = mapViewEval( xs, None, keyValuePairParser )
		return nodeEditor( node,
				   listView( FlowListViewLayout( 10.0, 5.0 ), '{', '}', ',', xViews ),
				   UnparsedText( '{ '  +  UnparsedText( ', ' ).join( [ x.text   for x in xViews ] )  +  ' }', PRECEDENCE_DICTLITERAL ),
				   state )

	
	
	# Yield expression
	def yieldExpr(self, state, node, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ keywordLabel( yieldKeyword ),  valueView ] ),
				UnparsedText( yieldKeyword  +  ' '  +  valueView.text,  PRECEDENCE_YIELDEXPR ),
				state )

	def yieldAtom(self, state, node, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ label( '(', punctuationStyle ),  keywordLabel( yieldKeyword ),  valueView,  label( ')', punctuationStyle ) ] ),
				UnparsedText( '(' + yieldKeyword  +  ' '  +  valueView.text + ')',  PRECEDENCE_YIELDEXPR ),
				state )

	
	
	# Attribute ref
	def attributeRef(self, state, node, target, name):
		targetView = viewEval( target )
		nameUnparsed = UnparsedText( name )
		nameLabel = label( name )
		nameUnparsed.associateWith( nameLabel )
		return nodeEditor( node,
				ahbox( [ viewEval( target ),  label( '.' ),  nameLabel ] ),
				UnparsedText( _unparsePrecedenceGT( targetView.text, PRECEDENCE_ATTR ) + '.' + nameUnparsed,  PRECEDENCE_ATTR ),
				state )


	
	# Subscript
	def subscriptSlice(self, state, node, x, y):
		xView = viewEval( x )
		yView = viewEval( y )
		return nodeEditor( node,
				ahbox( [ xView, label( ':', punctuationStyle ), yView  ] ),
				UnparsedText( xView.text  +  ':'  +  yView.text,  PRECEDENCE_SUBSCRIPTSLICE ),
				state )

	def subscriptLongSlice(self, state, node, x, y, z):
		xView = viewEval( x )
		yView = viewEval( y )
		zView = viewEval( z )
		return nodeEditor( node,
				ahbox( [ xView, label( ':', punctuationStyle ), yView, label( ':', punctuationStyle ), zView  ] ),
				UnparsedText( xView.text  +  ':'  +  yView.text  +  ':'  +  zView.text,  PRECEDENCE_SUBSCRIPTSLICE ),
				state )
	
	def ellipsis(self, state, node):
		return nodeEditor( node,
				label( '...', punctuationStyle ),
				UnparsedText( '...',  PRECEDENCE_SUBSCRIPTSLICE ),
				state )
	
	def subscriptTuple(self, state, node, *xs):
		xViews = mapViewEval( xs, None, python25ViewState( subscriptItemParser ) )
		return nodeEditor( node,
				   listView( FlowListViewLayout( 5.0, 0.0 ), None, None, ',', xViews ),
				   UnparsedText( '( '  +  UnparsedText( ', ' ).join( [ x.text   for x in xViews ] )  +  ' )', PRECEDENCE_TUPLE ),
				   state )

	def subscript(self, state, node, target, index):
		targetView = viewEval( target )
		indexView = viewEval( index, None, python25ViewState( subscriptIndexParser ) )
		#return nodeEditor( node,
				#scriptRSub( targetView,  ahbox( [ label( '[', punctuationStyle ),  indexView,  label( ']', punctuationStyle ) ] ) ),
				#UnparsedText( _unparsePrecedenceGT( targetView.text, PRECEDENCE_SUBSCRIPT ) + '[' + indexView.text + ']',  PRECEDENCE_SUBSCRIPT ),
				#state )
		return nodeEditor( node,
				ahbox( [ targetView,  label( '[', punctuationStyle ),  indexView,  label( ']', punctuationStyle ) ] ),
				UnparsedText( _unparsePrecedenceGT( targetView.text, PRECEDENCE_SUBSCRIPT ) + '[' + indexView.text + ']',  PRECEDENCE_SUBSCRIPT ),
				state )

	
	
	# Call
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

	def call(self, state, node, target, *args):
		targetView = viewEval( target )
		argViews = mapViewEval( args, None, python25ViewState( callArgParser ) )
		argWidgets = []
		if len( args ) > 0:
			for a in argViews[:-1]:
				argWidgets.append( a )
				argWidgets.append( label( ',', punctuationStyle ) )
			argWidgets.append( argViews[-1] )
		return nodeEditor( node,
				ahbox( [ viewEval( target ), label( '(', punctuationStyle ) ]  +  argWidgets  +  [ label( ')', punctuationStyle ) ] ),
				UnparsedText( _unparsePrecedenceGT( targetView.text, PRECEDENCE_CALL ) + '( ' + UnparsedText( ', ' ).join( [ a.text   for a in argViews ] ) + ' )',  PRECEDENCE_CALL ),
				state )
	
	
	
	# Operators
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
				  lambda state, node, x, y, xView, yView: \
				  	vbox( [
							vbox( [ xView ], alignment='centre' ),
							hline( operatorStyle ),
							vbox( [ yView ], alignment='centre' ) ],
						alignment='expand' ),
				  PRECEDENCE_MULDIVMOD )
	
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


	
	# Parameters
	def simpleParam(self, state, node, name):
		return nodeEditor( node,
				label( name ),
				UnparsedText( name,  PRECEDENCE_PARAM ),
				state )

	def defaultValueParam(self, state, node, name, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ label( name ), label( '=', punctuationStyle ), valueView ] ),
				UnparsedText( name  +  '='  +  valueView.text,  PRECEDENCE_PARAM ),
				state )

	def paramList(self, state, node, name):
		return nodeEditor( node,
				ahbox( [ label( '*', punctuationStyle ), label( name ) ] ),
				UnparsedText( '*'  +  name,  PRECEDENCE_PARAM ),
				state )

	def kwParamList(self, state, node, name):
		return nodeEditor( node,
				ahbox( [ label( '**', punctuationStyle ), label( name ) ] ),
				UnparsedText( '**'  +  name,  PRECEDENCE_PARAM ),
				state )

	
	
	# Conditional expression
	def conditionalExpr(self, node, condition, expr, elseExpr):
		return self( expr )  +  '   '  +  'if '  +  self( condition )  +  ' else '  +  self( elseExpr )

	
	
	# Lambda expression
	def lambdaExpr(self, state, node, params, expr):
		exprView = viewEval( expr )
		paramViews = mapViewEval( params, None, python25ViewState( paramParser ) )
		paramWidgets = []
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramWidgets.append( p )
				paramWidgets.append( label( ',', punctuationStyle ) )
			paramWidgets.append( paramViews[-1] )
		return nodeEditor( node,
				ahbox( [ keywordLabel( lambdaKeyword ) ]  +  paramWidgets  +  [ label( ':', punctuationStyle ), exprView ] ),
				UnparsedText( lambdaKeyword  +  ' ' + UnparsedText( ', ' ).join( [ p.text   for p in paramViews ] ) + ': '  +  exprView.text,  PRECEDENCE_LAMBDAEXPR ),
				state )

	
	
	# Conditional expression
	def conditionalExpr(self, state, node, condition, expr, elseExpr):
		conditionView = viewEval( condition, None, python25ViewState( orTestParser ) )
		exprView = viewEval( expr, None, python25ViewState( orTestParser ) )
		elseExprView = viewEval( elseExpr, None, python25ViewState( expressionParser ) )
		return nodeEditor( node,
				   ahbox( [ exprView,   ahbox( [ keywordLabel( ifKeyword ), conditionView ] ), ahbox( [ keywordLabel( elseKeyword ), elseExprView ] )   ], spacing=15.0 ),
				   UnparsedText( exprView.text  +  '   '  +  ifKeyword  +  ' '  +  conditionView.text  +  ' '  +  elseKeyword  +  '   '   +  elseExprView.text, PRECEDENCE_CONDITIONALEXPRESSION ),
				   state )
	
	
	
	
	
	# Assert statement
	def assertStmt(self, state, node, condition, *xs):
		conditionView = viewEval( condition )
		xView = viewEval( xs[0] )   if len( xs ) > 0   else   None
		xViewWidgets = [ label( ',', punctuationStyle ), xView ]   if xView is not None   else   []
		return nodeEditor( node,
				   ahbox( [ keywordLabel( assertKeyword ), conditionView ] + xViewWidgets ),
				   UnparsedText( UnparsedText( assertKeyword )  +  ' '  +  conditionView.text  +  ( ', ' + xView.text ) if xView is not None  else '',  PRECEDENCE_STMT ),
				   state )
	
	
	# Assignment statement
	def assignmentStmt(self, state, node, targets, value):
		targetViews = mapViewEval( targets, None, python25ViewState( targetListParser ) )
		valueView = viewEval( value, None, python25ViewState( tupleOrExpressionOrYieldExpressionParser ) )
		targetWidgets = []
		for t in targetViews:
			targetWidgets.append( t )
			targetWidgets.append( label( '=', punctuationStyle ) )
		return nodeEditor( node,
				ahbox( targetWidgets  +  [ valueView ] ),
				UnparsedText( UnparsedText( ' = ' ).join( [ t.text   for t in targetViews ] )  +  ' = '  +  valueView.text, PRECEDENCE_STMT ),
				state )
	

	# Augmented assignment statement
	def augAssignStmt(self, state, node, op, target, value):
		targetView = viewEval( target, None, python25ViewState( targetItemParser ) )
		valueView = viewEval( value, None, python25ViewState( tupleOrExpressionOrYieldExpressionParser ) )
		return nodeEditor( node,
				ahbox( [ targetView,  label( op, punctuationStyle ),  valueView ] ),
				UnparsedText( targetView.text  +  op  +  valueView.text, PRECEDENCE_STMT ),
				state )
	

	# Pass statement
	def passStmt(self, state, node):
		return nodeEditor( node,
				   keywordLabel( passKeyword ),
				   UnparsedText( passKeyword, PRECEDENCE_STMT ),
				   state )
	
	
	# Del statement
	def delStmt(self, state, node, target):
		targetView = viewEval( target, None, python25ViewState( targetListParser ) )
		return nodeEditor( node,
				ahbox( [ keywordLabel( delKeyword ),  targetView ] ),
				UnparsedText( delKeyword  +  ' '  +  targetView.text,  PRECEDENCE_STMT ),
				state )


	# Return statement
	def returnStmt(self, state, node, value):
		valueView = viewEval( value )
		return nodeEditor( node,
				ahbox( [ keywordLabel( returnKeyword ),  valueView ] ),
				UnparsedText( returnKeyword  +  ' '  +  valueView.text,  PRECEDENCE_STMT ),
				state )

	# If statement
	def ifStmt(self, state, node, value, suite):
		lineViews = mapViewEval( suite, None, python25ViewState( statementParser, MODE_LINE ) )
		suiteView = listView( VerticalListViewLayout( 0.0, 0.0, 30.0 ), '{', '}', None, lineViews )
		
		valueView = viewEval( value )
		return nodeEditor( node,
				vbox( [ ahbox( [ keywordLabel( ifKeyword ),  valueView,  label( ':', punctuationStyle ) ] ),  suiteView ] ),
				UnparsedText( ifKeyword  +  ' '  +  valueView.text  +  ':',  PRECEDENCE_STMT ),
				state )
	
	
	
	
	
	

