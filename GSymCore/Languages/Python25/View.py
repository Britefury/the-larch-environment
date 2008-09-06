##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.View.gSymView import border, indent, text, hiddenText, whitespace, hbox, ahbox, vbox, paragraph, script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, contentListener, \
     viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, backspaceStartMethod, deleteEndMethod, Interactor

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, append, prepend, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter

from Britefury.gSym.View.UnparsedText import UnparsedText


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *


from GSymCore.Languages.Python25 import Parser
from GSymCore.Languages.Python25.Styles import *
from GSymCore.Languages.Python25.Keywords import *




def keywordText(ctx, keyword):
	return text( ctx, keyword_textStyle, keyword )



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


class ParsedNodeContentListener (ElementContentListener):
	def __init__(self, ctx, node, parser):
		#super( ParsedNodeContentListener, self ).__init__()
		self._ctx = ctx
		self._node = node
		self._parser = parser

	def contentModified(self, element):
		value = element.getContent()
		parsed = _parseText( self._parser, value )
		if parsed is not None:
			replace( self._ctx, self._node, parsed )
		else:
			replace( self._ctx, self._node, [ 'UNPARSED', value ] )
		return True


_compoundStmtNames = set( [ 'ifStmt', 'elifStmt', 'elseStmt', 'whileStmt', 'forStmt', 'tryStmt', 'exceptStmt', 'finallyStmt', 'withStmt', 'defStmt', 'classStmt' ] )	


def _isCompoundStmt(node):
	return node[0] in _compoundStmtNames





class ParsedLineContentListener (ElementContentListener):
	def __init__(self, ctx, node, parser):
		self._ctx = ctx
		self._node = node
		self._parser = parser



	def parseLines(self, lineStrings):
		result = []
		# For each line
		for i, line in enumerate( lineStrings ):
			if line.strip() == '':
				# Blank line
				result.append( [ 'blankLine' ] )
			else:
				# Parse
				parsed = _parseText( self._parser, line )
				if parsed is None:
					# Parse failure; unparsed text
					result.append( [ 'UNPARSED', line ] )
				else:
					# Parsed
					if not _isCompoundStmt( parsed ):
						# Normal statement (non-compount)
						result.append( parsed )
					else:
						lineParsed = parsed
						lineParsed[-1] = self.parseLines( lineStrings[i+1:] )
						result.append( lineParsed )
						break
		return result




	def contentModified(self, element):
		# Get the content
		value = element.getContent()
		# Split into lines
		lineStrings = value.split( '\n' )
		# Parse
		parsedLines = self.parseLines( lineStrings )

		if _isCompoundStmt( self._node ):
			originalContents = self._node[-1]
			if _isCompoundStmt( parsedLines[-1] ):
				parsedLines[-1][-1].extend( originalContents )
			else:
				parsedLines.extend( originalContents )

		replaceWithRange( self._ctx, self._node, parsedLines )









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
PRECEDENCE_LITERALVALUE = 0
PRECEDENCE_LISTCOMPREHENSION = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_CONDITIONALEXPRESSION = 0
PRECEDENCE_DICTLITERAL = 0
PRECEDENCE_YIELDEXPR = 0
PRECEDENCE_IMPORTCONTENT=0

PRECEDENCE_SUBSCRIPTSLICE = 0
PRECEDENCE_ARG = 0
PRECEDENCE_PARAM = 0







def _paren(ctx, x):
	return paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '(' ), c, text( ctx, punctuation_textStyle, ')' ) ] )

def _precedenceGT(ctx, x, outerPrecedence):
	if outerPrecedence is not None  and  x.metadata is not None  and  x.metadata > outerPrecedence:
		return _paren( ctx, x )
	else:
		return x

def _precedenceGTE(ctx, x, outerPrecedence):
	if outerPrecedence is not None  and  x.metadata is not None  and  x.metadata >= outerPrecedence:
		return _paren( ctx, x )
	else:
		return x

def paragraphPrefixOpElement(ctx, x, op, precedence):
	x = _precedenceGT( ctx, x, precedence )
	return paragraph( ctx, python_paragraphStyle, [ op, x ] )

def paragraphBinOpElement(ctx, x, y, op, precedence, bRightAssociative=False):
	if bRightAssociative:
		x = _precedenceGTE( ctx, x, precedence )
		y = _precedenceGT( ctx, y, precedence )
	else:
		x = _precedenceGT( ctx, x, precedence )
		y = _precedenceGTE( ctx, y, precedence )
	return paragraph( ctx, python_paragraphStyle, [ x, text( ctx, default_textStyle, ' ' ), op, text( ctx, default_textStyle, ' ' ), y ] )

def _listViewNeedsDelims(x, outerPrecedence):
	return outerPrecedence is not None  and  x.metadata is not None  and  x.metadata > outerPrecedence



MODE_EXPRESSION = 0
MODE_STATEMENT = 1



def python25ViewState(parser, mode=MODE_EXPRESSION):
	return parser, mode



def suiteView(ctx, suite):
	lineViews = mapViewEval( ctx, suite, None, python25ViewState( Parser.statement, MODE_STATEMENT ) )
	return listView( ctx, suite_listViewLayout, None, None, None, lineViews )



def nodeEditor(ctx, node, contents, metadata, state):
	if state is None:
		parser = Parser.expression
		mode = MODE_EXPRESSION
	else:
		parser, mode = state

	if mode == MODE_EXPRESSION:
		return contents, metadata
	elif mode == MODE_STATEMENT:
		return contentListener( ctx, contents, ParsedLineContentListener( ctx, node, parser ) ),  metadata
	else:
		raise ValueError


def compoundStatementEditor(ctx, node, headerContents, metadata, suite, state):
	if state is None:
		parser = Parser.statement
		mode = MODE_STATEMENT
	else:
		parser, mode = state

	headerParagraph = paragraph( ctx, python_paragraphStyle, [ headerContents, whitespace( ctx, '\n' ) ] )

	headerElement = contentListener( ctx, headerParagraph, ParsedLineContentListener( ctx, node, parser ) )
	statementElement = vbox( ctx, compoundStmt_vboxStyle, [ headerElement, indent( ctx, 30.0, suiteView( ctx, suite ) ) ] )
	return statementElement, metadata



def binOpView(ctx, state, node, x, y, precedence, bRightAssociative, elementFactory):
	xView = viewEval( ctx, x )
	yView = viewEval( ctx, y )
	if bRightAssociative:
		xView = _precedenceGTE( ctx, xView, precedence )
		yView = _precedenceGT( ctx, yView, precedence )
	else:
		xView = _precedenceGT( ctx, xView, precedence )
		yView = _precedenceGTE( ctx, yView, precedence )
	return nodeEditor( ctx, node,
			   elementFactory( ctx, state, node, x, y, xView, yView ),
			   precedence,
			   state )


def paragraphBinOpView(ctx, state, node, x, y, op, precedence, bRightAssociative):
	xView = viewEval( ctx, x )
	yView = viewEval( ctx, y )
	return nodeEditor( ctx, node,
			   paragraphBinOpElement( ctx, xView, yView, text( ctx, operator_textStyle, op ), precedence, bRightAssociative ),
			   precedence,
			   state )

def paragraphPrefixOpView(ctx, state, node, x, op, precedence):
	xView = viewEval( ctx, x )
	return nodeEditor( ctx, node,
			   paragraphPrefixOpElement( ctx, xView, text( ctx, operator_textStyle, op ), precedence ),
			   precedence,
			   state )


def tupleView(ctx, state, node, xs, parser=None):
	def tupleElement(x):
		if x.metadata == PRECEDENCE_TUPLE:
			return paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '(' ), x, text( ctx, punctuation_textStyle, ')' ) ] )
		else:
			return x
	if parser is not None:
		xViews = mapViewEval( ctx, xs, None, python25ViewState( parser ) )
	else:
		xViews = mapViewEval( ctx, xs )
	xElements = [ tupleElement( x )   for x in xViews ]
	return nodeEditor( ctx, node,
			   listView( ctx, tuple_listViewLayout, None, None, lambda: text( ctx, punctuation_textStyle, ',' ), xElements ),
			   PRECEDENCE_TUPLE,
			   state )


class Python25View (GSymView):
	# MISC
	def python25Module(self, ctx, state, node, *content):
		lineViews = mapViewEval( ctx, content, None, python25ViewState( Parser.statement, MODE_STATEMENT ) )
		return listView( ctx, module_listViewLayout, None, None, None, lineViews ), ''



	def blankLine(self, ctx, state, node):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, ' ' ),
				   None,
				   state )


	def UNPARSED(self, ctx, state, node, value):
		return nodeEditor( ctx, node,
				   text( ctx, unparsed_textStyle, value ),
				   None,
				   state )


	# String literal
	def stringLiteral(self, ctx, state, node, format, quotation, value):
		boxContents = []

		if format == 'ascii':
			pass
		elif format == 'unicode':
			boxContents.append( text( ctx, literalFormat_textStyle, 'u' ) )
		elif format == 'ascii-regex':
			boxContents.append( text( ctx, literalFormat_textStyle, 'r' ) )
		elif format == 'unicode-regex':
			boxContents.append( text( ctx, literalFormat_textStyle, 'ur' ) )
		else:
			raise ValueError, 'invalid string literal format'

		if quotation == 'single':
			boxContents.append( text( ctx, punctuation_textStyle, "'" ) )
			boxContents.append( None )
			boxContents.append( text( ctx, punctuation_textStyle, "'" ) )
		else:
			boxContents.append( text( ctx, punctuation_textStyle, '"' ) )
			boxContents.append( None )
			boxContents.append( text( ctx, punctuation_textStyle, '"' ) )

		boxContents[-2] = text( ctx, default_textStyle, value )

		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, boxContents ),
				   PRECEDENCE_LITERALVALUE,
				   state )


	# Integer literal
	def intLiteral(self, ctx, state, node, format, numType, value):
		boxContents = []

		if numType == 'int':
			if format == 'decimal':
				valueString = '%d'  %  int( value )
			elif format == 'hex':
				valueString = '%x'  %  int( value, 16 )
			boxContents.append( text( ctx, numericLiteral_textStyle, valueString ) )
		elif numType == 'long':
			if format == 'decimal':
				valueString = '%d'  %  long( value )
			elif format == 'hex':
				valueString = '%x'  %  long( value, 16 )
			boxContents.append( text( ctx, numericLiteral_textStyle, valueString ) )
			boxContents.append( text( ctx, literalFormat_textStyle, 'L' ) )

		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, boxContents ),
				   PRECEDENCE_LITERALVALUE,
				   state )



	# Float literal
	def floatLiteral(self, ctx, state, node, value):
		return nodeEditor( ctx, node,
				   text( ctx, numericLiteral_textStyle, value ),
				   PRECEDENCE_LITERALVALUE,
				   state )



	# Imaginary literal
	def imaginaryLiteral(self, sctx, tate, node, value):
		return nodeEditor( ctx, node,
				   text( ctx, numericLiteral_textStyle, value ),
				   PRECEDENCE_LITERALVALUE,
				   state )



	# Targets
	def singleTarget(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   None,
				   state )


	def tupleTarget(self, ctx, state, node, *xs):
		return tupleView( ctx, state, node, xs, Parser.targetItem )

	def listTarget(self, ctx, state, node, *xs):
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.targetItem ) )
		return nodeEditor( ctx, node,
				   listView( ctx, list_listViewLayout, lambda: text( ctx, punctuation_textStyle, '[' ), lambda: text( ctx, punctuation_textStyle, ']' ), lambda: text( ctx, punctuation_textStyle, ',' ), xViews ),
				   None,
				   state )




	# Variable reference
	def var(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   None,
				   state )



	# Tuple literal
	def tupleLiteral(self, ctx, state, node, *xs):
		return tupleView( ctx, state, node, xs )



	# List literal
	def listLiteral(self, ctx, state, node, *xs):
		xViews = mapViewEval( ctx, xs )
		return nodeEditor( ctx, node,
				   listView( ctx, list_listViewLayout, lambda: text( ctx, punctuation_textStyle, '[' ), lambda: text( ctx, punctuation_textStyle, ']' ), lambda: text( ctx, punctuation_textStyle, ',' ), xViews ),
				   PRECEDENCE_LISTLITERAL,
				   state )



	# List comprehension
	def listFor(self, ctx, state, node, target, source):
		targetView = viewEval( ctx, target, None, python25ViewState( Parser.targetList ) )
		sourceView = viewEval( ctx, source, None, python25ViewState( Parser.oldTupleOrExpression ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, forKeyword ), text( ctx, default_textStyle, ' ' ), targetView, text( ctx, default_textStyle, ' ' ), keywordText( ctx, inKeyword ), text( ctx, default_textStyle, ' ' ), sourceView ] ),
				   PRECEDENCE_LISTCOMPREHENSION,
				   state )

	def listIf(self, ctx, state, node, condition):
		conditionView = viewEval( ctx, condition, None, python25ViewState( Parser.oldExpression ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, ifKeyword ), text( ctx, default_textStyle, ' ' ), conditionView ] ),
				   PRECEDENCE_LISTCOMPREHENSION,
				   state )

	def listComprehension(self, ctx, state, node, expr, *xs):
		exprView = viewEval( ctx, expr )
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.listComprehensionItem ) )
		xViewsSpaced = []
		if len( xViews ) > 0:
			for x in xViews[:-1]:
				xViewsSpaced.append( x )
				xViewsSpaced.append( whitespace( ctx, ' ', 15.0 ) )
			xViewsSpaced.append( xViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '[' ),  exprView,  whitespace( ctx, ' ', 15.0 ) ] + xViewsSpaced + [ text( ctx, punctuation_textStyle, ']' ) ] ),
				   PRECEDENCE_LISTCOMPREHENSION,
				   state )




	# Generator expression
	def genFor(self, ctx, state, node, target, source):
		targetView = viewEval( ctx, target, None, python25ViewState( Parser.targetList ) )
		sourceView = viewEval( ctx, source, None, python25ViewState( Parser.orTest ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, forKeyword ), text( ctx, default_textStyle, ' ' ), targetView, text( ctx, default_textStyle, ' ' ), keywordText( ctx, inKeyword ), text( ctx, default_textStyle, ' ' ), sourceView ] ),
				   PRECEDENCE_GENERATOREXPRESSION,
				   state )

	def genIf(self, ctx, state, node, condition):
		conditionView = viewEval( ctx, condition, None, python25ViewState( Parser.oldExpression ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, ifKeyword ), text( ctx, default_textStyle, ' ' ), conditionView ] ),
				   PRECEDENCE_GENERATOREXPRESSION,
				   state )

	def generatorExpression(self, ctx, state, node, expr, *xs):
		exprView = viewEval( ctx, expr )
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.generatorExpressionItem ) )
		xViewsSpaced = []
		if len( xViews ) > 0:
			for x in xViews[:-1]:
				xViewsSpaced.append( x )
				xViewsSpaced.append( whitespace( ctx, ' ', 15.0 ) )
			xViewsSpaced.append( xViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '(' ),  exprView,  whitespace( ctx, ' ', 15.0 ) ] + xViewsSpaced + [ text( ctx, punctuation_textStyle, ')' ) ] ),
				   PRECEDENCE_GENERATOREXPRESSION,
				   state )




	# Dictionary literal
	def keyValuePair(self, ctx, state, node, key, value):
		keyView = viewEval( ctx, key )
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keyView, text( ctx, punctuation_textStyle, ' : ' ), valueView ] ),
				   None,
				   state )

	def dictLiteral(self, ctx, state, node, *xs):
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.keyValuePair ) )
		return nodeEditor( ctx, node,
				   listView( ctx, dict_listViewLayout, lambda: text( ctx, punctuation_textStyle, '{' ), lambda: text( ctx, punctuation_textStyle, '}' ), lambda: text( ctx, punctuation_textStyle, ',' ), xViews ),
				   PRECEDENCE_DICTLITERAL,
				   state )


	# Yield expression
	def yieldExpr(self, ctx, state, node, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, yieldKeyword ),  text( ctx, punctuation_textStyle, ' ' ),  valueView ] ),
				   PRECEDENCE_YIELDEXPR,
				   state )

	def yieldAtom(self, ctx, state, node, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '(' ),  keywordText( ctx, yieldKeyword ),  text( ctx, punctuation_textStyle, ' ' ),  valueView,  text( ctx, punctuation_textStyle, ')' ) ] ),
				   PRECEDENCE_YIELDEXPR,
				   state )



	# Attribute ref
	def attributeRef(self, ctx, state, node, target, name):
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ viewEval( ctx, target ),  text( ctx, punctuation_textStyle, '.' ),  text( ctx, default_textStyle, name ) ] ),
				   PRECEDENCE_ATTR,
				   state )



	# Subscript
	def subscriptSlice(self, ctx, state, node, x, y):
		xView = viewEval( ctx, x )
		yView = viewEval( ctx, y )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ xView, text( ctx, punctuation_textStyle, ':' ), yView ] ),
				   PRECEDENCE_SUBSCRIPTSLICE,
				   state )

	def subscriptLongSlice(self, ctx, state, node, x, y, z):
		xView = viewEval( ctx, x )
		yView = viewEval( ctx, y )
		zView = viewEval( ctx, z )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ xView, text( ctx, punctuation_textStyle, ':' ), yView, text( ctx, punctuation_textStyle, ':' ), zView ] ),
				   PRECEDENCE_SUBSCRIPTSLICE,
				   state )

	def ellipsis(self, ctx, state, node):
		return nodeEditor( ctx, node,
				   text( ctx, punctuation_textStyle, '...' ),
				   PRECEDENCE_SUBSCRIPTSLICE,
				   state )

	def subscriptTuple(self, ctx, state, node, *xs):
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.subscriptItem ) )
		return nodeEditor( ctx, node,
				   listView( ctx, tuple_listViewLayout, None, None, lambda: text( ctx, punctuation_textStyle, ',' ), xViews ),
				   PRECEDENCE_TUPLE,
				   state )

	def subscript(self, ctx, state, node, target, index):
		targetView = viewEval( ctx, target )
		indexView = viewEval( ctx, index, None, python25ViewState( Parser.subscriptIndex ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ targetView,  text( ctx, punctuation_textStyle, '[' ),  indexView,  text( ctx, punctuation_textStyle, ']' ) ] ),
				   PRECEDENCE_SUBSCRIPT,
				   state )




	# Call
	def kwArg(self, ctx, state, node, name, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, default_textStyle, name ), text( ctx, punctuation_textStyle, '=' ), valueView ] ),
				   PRECEDENCE_ARG,
				   state )

	def argList(self, ctx, state, node, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '*' ),  valueView ] ),
				   PRECEDENCE_ARG,
				   state )

	def kwArgList(self, ctx, state, node, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '**' ),  valueView ] ),
				   PRECEDENCE_ARG,
				   state )

	def call(self, ctx, state, node, target, *args):
		targetView = viewEval( ctx, target )
		argViews = mapViewEval( ctx, args, None, python25ViewState( Parser.callArg ) )
		argElements = []
		if len( args ) > 0:
			for a in argViews[:-1]:
				argElements.append( a )
				argElements.append( text( ctx, punctuation_textStyle, ', ' ) )
			argElements.append( argViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ targetView, text( ctx, punctuation_textStyle, '(' ) ]  +  argElements  +  [ text( ctx, punctuation_textStyle, ')' ) ] ),
				   PRECEDENCE_CALL,
				   state )





	# Operators
	def pow(self, ctx, state, node, x, y):
		def _elementFactory(ctx, state, node, x, y, xView, yView):
			return scriptRSuper( ctx, pow_scriptStyle, xView, paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '**' ), text( ctx, default_textStyle, ' ' ), yView ] ) )
		return binOpView( ctx, state, node, x, y, PRECEDENCE_POW, True, _elementFactory )


	def invert(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS )

	def negate(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS )

	def pos(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS )


	def mul(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '*', PRECEDENCE_MULDIVMOD, False )


	def div(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '%', PRECEDENCE_MULDIVMOD, False )
		#return binOpView( ctx, state, node, x, y, '/',
					#lambda state, node, x, y, xView, yView: \
					#vbox( [
							#vbox( [ xView ], alignment='centre' ),
							#hline( operatorStyle ),
							#vbox( [ yView ], alignment='centre' ) ],
						#alignment='expand' ),
					#PRECEDENCE_MULDIVMOD )

	def mod(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '%', PRECEDENCE_MULDIVMOD, False )

	def add(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '+', PRECEDENCE_ADDSUB, False )

	def sub(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '-', PRECEDENCE_ADDSUB, False )


	def lshift(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '<<', PRECEDENCE_SHIFT, False )

	def rshift(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '>>', PRECEDENCE_SHIFT, False )


	def bitAnd(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '&', PRECEDENCE_BITAND, False )

	def bitXor(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '^', PRECEDENCE_BITXOR, False )

	def bitOr(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '|', PRECEDENCE_BITOR, False )


	def lte(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '<=', PRECEDENCE_CMP, False )

	def lt(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '<', PRECEDENCE_CMP, False )

	def gte(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '>=', PRECEDENCE_CMP, False )

	def gt(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '>', PRECEDENCE_CMP, False )

	def eq(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '==', PRECEDENCE_CMP, False )

	def neq(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '!=', PRECEDENCE_CMP, False )


	def isNotTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'is not', PRECEDENCE_IS, False )

	def isTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'is', PRECEDENCE_IS, False )

	def notInTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'not in', PRECEDENCE_IN, False )

	def inTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'in', PRECEDENCE_IN, False )


	def notTest(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, 'not ', PRECEDENCE_NOT )

	def andTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'and', PRECEDENCE_AND, False )

	def orTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'or', PRECEDENCE_OR, False )





	# Parameters
	def simpleParam(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   PRECEDENCE_PARAM,
				   state )

	def defaultValueParam(self, ctx, state, node, name, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, default_textStyle, name ), text( ctx, punctuation_textStyle, '=' ), valueView ] ),
				   PRECEDENCE_PARAM,
				   state )

	def paramList(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '*' ),  text( ctx, default_textStyle, name ) ] ),
				   PRECEDENCE_PARAM,
				   state )

	def kwParamList(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '**' ),  text( ctx, default_textStyle, name ) ] ),
				   PRECEDENCE_PARAM,
				   state )



	# Lambda expression
	def lambdaExpr(self, ctx, state, node, params, expr):
		# The Python 2.5 grammar has two versions of the lambda expression grammar; one what reckognises the full lambda expression, and one that
		# reckognises a lambda expression that cannot wrap conditional expression.
		# Ensure that we use the correct parser for @expr
		exprParser = Parser.expression
		if state is not None:
			parser, mode = state
			if parser is Parser.oldExpression   or  parser is Parser.oldLambdaExpr  or  parser is Parser.oldTupleOrExpression:
				exprParser = Parser.oldExpression

		exprView = viewEval( ctx, expr, None, python25ViewState( exprParser ) )
		paramViews = mapViewEval( ctx, params, None, python25ViewState( Parser.param ) )
		paramElements = []
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramElements.append( p )
				paramElements.append( text( ctx, punctuation_textStyle, ', ' ) )
			paramElements.append( paramViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, lambdaKeyword ),  text( ctx, default_textStyle, ' ' ) ]  +  paramElements  +  [ text( ctx, punctuation_textStyle, ': ' ), exprView ] ),
				   PRECEDENCE_LAMBDAEXPR,
				   state )



	# Conditional expression
	def conditionalExpr(self, ctx, state, node, condition, expr, elseExpr):
		conditionView = viewEval( ctx, condition, None, python25ViewState( Parser.orTest ) )
		exprView = viewEval( ctx, expr, None, python25ViewState( Parser.orTest ) )
		elseExprView = viewEval( ctx, elseExpr, None, python25ViewState( Parser.expression ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ exprView,   whitespace( ctx, '  ', 15.0 ),
									    keywordText( ctx, ifKeyword ), text( ctx, default_textStyle, ' ' ), conditionView,   whitespace( ctx, '  ', 15.0 ),
									    keywordText( ctx, elseKeyword ), text( ctx, default_textStyle, ' ' ), elseExprView ] ),
				   PRECEDENCE_CONDITIONALEXPRESSION,
				   state )





	# Assert statement
	def assertStmt(self, ctx, state, node, condition, fail):
		conditionView = viewEval( ctx, condition )
		elements = [ keywordText( ctx, assertKeyword ), text( ctx, default_textStyle, ' ' ), conditionView ]
		if fail != '<nil>':
			failView = viewEval( ctx, fail )
			elements.extend( [ text( ctx, punctuation_textStyle, ', ' ),  failView ] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, elements ),
				   PRECEDENCE_STMT,
				   state )


	# Assignment statement
	def assignmentStmt(self, ctx, state, node, targets, value):
		targetViews = mapViewEval( ctx, targets, None, python25ViewState( Parser.targetList ) )
		valueView = viewEval( ctx, value, None, python25ViewState( Parser.tupleOrExpressionOrYieldExpression ) )
		targetElements = []
		for t in targetViews:
			targetElements.extend( [ t,  text( ctx, punctuation_textStyle, ' = ' ) ] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, targetElements  +  [ valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Augmented assignment statement
	def augAssignStmt(self, ctx, state, node, op, target, value):
		targetView = viewEval( ctx, target, None, python25ViewState( Parser.targetItem ) )
		valueView = viewEval( ctx, value, None, python25ViewState( Parser.tupleOrExpressionOrYieldExpression ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ targetView,  text( ctx, punctuation_textStyle, ' ' + op + ' ' ),  valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Pass statement
	def passStmt(self, ctx, state, node):
		return nodeEditor( ctx, node,
				   keywordText( ctx, passKeyword ),
				   PRECEDENCE_STMT,
				   state )


	# Del statement
	def delStmt(self, ctx, state, node, target):
		targetView = viewEval( ctx, target, None, python25ViewState( Parser.targetList ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, delKeyword ),  text( ctx, default_textStyle, ' ' ),  targetView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Return statement
	def returnStmt(self, ctx, state, node, value):
		valueView = viewEval( ctx, value, None, python25ViewState( Parser.tupleOrExpression ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, returnKeyword ),  text( ctx, default_textStyle, ' ' ),  valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Yield statement
	def yieldStmt(self, ctx, state, node, value):
		valueView = viewEval( ctx, value )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, yieldKeyword ),  text( ctx, default_textStyle, ' ' ),  valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Raise statement
	def raiseStmt(self, ctx, state, node, *xs):
		xs = [ x   for x in xs   if x != '<nil>' ]
		xViews = mapViewEval( ctx, xs )
		xElements = []
		if len( xs ) > 0:
			for x in xViews[:-1]:
				xElements.extend( [ x,  text( ctx, punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, raiseKeyword ),  text( ctx, default_textStyle, ' ' ) ] + xElements ),
				   PRECEDENCE_STMT,
				   state )


	# Break statement
	def breakStmt(self, ctx, state, node):
		return nodeEditor( ctx, node,
				   keywordText( ctx, breakKeyword ),
				   PRECEDENCE_STMT,
				   state )


	# Continue statement
	def continueStmt(self, ctx, state, node):
		return nodeEditor( ctx, node,
				   keywordText( ctx, continueKeyword ),
				   PRECEDENCE_STMT,
				   state )


	# Import statement
	def relativeModule(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleImport(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleImportAs(self, ctx, state, node, name, asName):
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, default_textStyle, name ),  text( ctx, default_textStyle, ' ' ),  keywordText( ctx, asKeyword ),
									    text( ctx, default_textStyle, ' ' ),  text( ctx, default_textStyle, asName ) ] ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleContentImport(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleContentImportAs(self, ctx, state, node, name, asName):
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, default_textStyle, name ),  text( ctx, default_textStyle, ' ' ),  keywordText( ctx, asKeyword ),
									    text( ctx, default_textStyle, ' ' ),  text( ctx, default_textStyle, asName ) ] ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def importStmt(self, ctx, state, node, *xs):
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.moduleImport ) )
		xElements = []
		if len( xs ) > 0:
			for xv in xViews[:-1]:
				xElements.extend( [ xv,  text( ctx, punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, importKeyword ), text( ctx, default_textStyle, ' ' ) ]  +  xElements ),
				   PRECEDENCE_STMT,
				   state )
	
	def fromImportStmt(self, ctx, state, node, moduleName, *xs):
		moduleNameView = viewEval( ctx, moduleName, None, python25ViewState( Parser.moduleContentImport ) )
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.moduleImport ) )
		xElements = []
		if len( xs ) > 0:
			for xv in xViews[:-1]:
				xElements.extend( [ xv,  text( ctx, punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, fromKeyword ), text( ctx, default_textStyle, ' ' ), moduleNameView, text( ctx, default_textStyle, ' ' ),
									    keywordText( ctx, importKeyword ), text( ctx, default_textStyle, ' ' ) ]  +  xElements ),
				   PRECEDENCE_STMT,
				   state )
	
	def fromImportAllStmt(self, ctx, state, node, moduleName):
		moduleNameView = viewEval( ctx, moduleName, None, python25ViewState( Parser.moduleContentImport ) )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, fromKeyword ), text( ctx, default_textStyle, ' ' ), moduleNameView, text( ctx, default_textStyle, ' ' ),
									     keywordText( ctx, importKeyword ), text( ctx, default_textStyle, ' ' ),  text( ctx, punctuation_textStyle, '*' ) ] ),
				   PRECEDENCE_STMT,
				   state )


	# Global statement
	def globalVar(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				   text( ctx, default_textStyle, name ),
				   PRECEDENCE_STMT,
				   state )
	
	def globalStmt(self, ctx, state, node, *xs):
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.globalVar ) )
		xElements = []
		if len( xs ) > 0:
			for xv in xViews[:-1]:
				xElements.extend( [ xv,  text( ctx, punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, globalKeyword ),  text( ctx, default_textStyle, ' ' ) ]  +  xElements ),
				   PRECEDENCE_STMT,
				   state )
	

	
	# Exec statement
	def execStmt(self, ctx, state, node, src, loc, glob):
		srcView = viewEval( ctx, src, None, python25ViewState( Parser.orOp ) )
		elements = [ srcView ]
		if loc != '<nil>':
			locView = viewEval( ctx, loc )
			elements.extend( [ text( ctx, default_textStyle, ' ' ),  keywordText( ctx, inKeyword ),  text( ctx, default_textStyle, ' ' ),  locView ] )
		if glob != '<nil>':
			globView = viewEval( ctx, glob )
			elements.extend( [ text( ctx, default_textStyle, ', ' ),  globView ] )
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, execKeyword ),  text( ctx, default_textStyle, ' ' ) ]  +  elements ),
				   PRECEDENCE_STMT,
				   state )


	
	
	
	# If statement
	def ifStmt(self, ctx, state, node, condition, suite):
		conditionView = viewEval( ctx, condition )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, ifKeyword ),  text( ctx, default_textStyle, ' ' ),  conditionView,  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )
	
	
	
	# Elif statement
	def elifStmt(self, ctx, state, node, condition, suite):
		conditionView = viewEval( ctx, condition )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, elifKeyword ),  text( ctx, default_textStyle, ' ' ),  conditionView,  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )
	
	
	
	# Else statement
	def elseStmt(self, ctx, state, node, suite):
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, elseKeyword ),  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )
	
	
	# While statement
	def whileStmt(self, ctx, state, node, condition, suite):
		conditionView = viewEval( ctx, condition )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, whileKeyword ),  text( ctx, default_textStyle, ' ' ),  conditionView,  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state  )


	# For statement
	def forStmt(self, ctx, state, node, target, source, suite):
		targetView = viewEval( ctx, target, None, python25ViewState( Parser.targetList ) )
		sourceView = viewEval( ctx, source, None, python25ViewState( Parser.tupleOrExpression ) )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, forKeyword ),  text( ctx, default_textStyle, ' ' ),  targetView,  text( ctx, default_textStyle, ' ' ),
											 keywordText( ctx, inKeyword ),  text( ctx, default_textStyle, ' ' ),  sourceView,  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state  )
	
	

	# Try statement
	def tryStmt(self, ctx, state, node, suite):
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, tryKeyword ),  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )
	
	
	
	# Except statement
	def exceptStmt(self, ctx, state, node, exc, target, suite):
		elements = []
		if exc != '<nil>':
			excView = viewEval( ctx, exc )
			elements.extend( [ text( ctx, default_textStyle, ' ' ),  excView ] )
		if target != '<nil>':
			targetView = viewEval( ctx, target )
			elements.extend( [ text( ctx, default_textStyle, ', ' ),  targetView ] )
		elements.append( text( ctx, punctuation_textStyle, ':' ) )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, exceptKeyword ) ]  +  elements ),
						PRECEDENCE_STMT,
						suite,
						state )

	
	
	# Finally statement
	def finallyStmt(self, ctx, state, node, suite):
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, finallyKeyword ),  text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )
	
	
	
	# With statement
	def withStmt(self, ctx, state, node, expr, target, suite):
		exprView = viewEval( ctx, expr )
		elements = [ exprView ]
		if target != '<nil>':
			targetView = viewEval( ctx, target )
			elements.extend( [ text( ctx, default_textStyle, ' ' ),  keywordText( ctx, asKeyword ),  text( ctx, default_textStyle, ' ' ),  targetView ] )
		elements.append( text( ctx, punctuation_textStyle, ':' ) )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, withKeyword ),  text( ctx, default_textStyle, ' ' ) ]  +  elements ),
						PRECEDENCE_STMT,
						suite,
						state )

	
	
	# Def statement
	def defStmt(self, ctx, state, node, name, params, suite):
		paramViews = mapViewEval( ctx, params, None, python25ViewState( Parser.param ) )
		paramElements = [ text( ctx, punctuation_textStyle, '(' ) ]
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramElements.extend( [ p,  text( ctx, punctuation_textStyle, ', ' ) ] )
			paramElements.append( paramViews[-1] )
		paramElements.append( text( ctx, punctuation_textStyle, ')' ) )
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, defKeyword ),  text( ctx, default_textStyle, ' ' ),  text( ctx, default_textStyle, name ) ]  +  \
							   paramElements  +  [ text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )

	
	# Decorator statement
	def decoStmt(self, ctx, state, node, name, args):
		if args != '<nil>':
			argViews = mapViewEval( ctx, args, None, python25ViewState( Parser.callArg ) )
			argElements = [ text( ctx, punctuation_textStyle, '(' ) ]
			if len( args ) > 0:
				for a in argViews[:-1]:
					argElements.extend( [ a, text( ctx, punctuation_textStyle, ', ' ) ] )
				argElements.append( argViews[-1] )
			argElements.append( text( ctx, punctuation_textStyle, ')' ) )
		else:
			argElements = []
		return nodeEditor( ctx, node,
				   paragraph( ctx, python_paragraphStyle, [ text( ctx, punctuation_textStyle, '@' ),  text( ctx, default_textStyle, name ) ]  +  argElements ),
				   PRECEDENCE_STMT,
				   state )
	
	
	
	# Def statement
	def classStmt(self, ctx, state, node, name, inheritance, suite):
		if inheritance != '<nil>':
			inheritanceView = viewEval( ctx, inheritance, None, python25ViewState( Parser.tupleOrExpression ) )
			inhElements = [ text( ctx, punctuation_textStyle, '(' ),  inheritanceView,  text( ctx, punctuation_textStyle, ')' ) ]
		else:
			inhElements = []
			
		return compoundStatementEditor( ctx, node,
						paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, classKeyword ),  text( ctx, default_textStyle, ' ' ),  text( ctx, default_textStyle, name ) ]  +  \
							   inhElements  +  [ text( ctx, punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state )
	
	
	
	# Comment statement
	def commentStmt(self, ctx, state, node, comment):
		return nodeEditor( ctx, node,
				   text( ctx, comment_textStyle, '#' + comment ),
				   PRECEDENCE_STMT,
				   state )

