##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Parser import ParserExpression

from Britefury.gSym.View.gSymView import GSymView

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout



from GSymCore.Languages.Python25.Parser2 import Python25Grammar
from GSymCore.Languages.Python25.Styles import *
from GSymCore.Languages.Python25.Keywords import *





class _TextFactory (object):
	__slots__ = [ 'ctx' ]
	
	style = default_textStyle
	text = ''
	
	def __init__(self, ctx):
		self.ctx = ctx

	def __call__(self):
		return self.ctx.text( self.style, self.text )



class _TextSeparatorFactory (object):
	__slots__ = [ 'ctx' ]
	
	style = default_textStyle
	text = ''
	
	def __init__(self, ctx):
		self.ctx = ctx

	def __call__(self, index, child):
		return self.ctx.text( self.style, self.text )


class _CommaFactory (_TextSeparatorFactory):
	style = punctuation_textStyle
	text = ','
	
	

class _OpenBracketFactory (_TextFactory):
	style = punctuation_textStyle
	text = '['
	
class _CloseBracketFactory (_TextFactory):
	style = punctuation_textStyle
	text = ']'

	
class _OpenBraceFactory (_TextFactory):
	style = punctuation_textStyle
	text = '{'
	
class _CloseBraceFactory (_TextFactory):
	style = punctuation_textStyle
	text = '}'
	

class _OpenParenFactory (_TextFactory):
	style = punctuation_textStyle
	text = '('
	
class _CloseParenFactory (_TextFactory):
	style = punctuation_textStyle
	text = ')'
	



def keywordText(ctx, keyword):
	return ctx.text( keyword_textStyle, keyword )



def _parseText(parser, text):
	res = parser.parseString( text )
	pos = res.getEnd()
	if res is not None:
		if pos == len( text ):
			return res.getValue()
		else:
			print '<INCOMPLETE>'
			print 'FULL TEXT:', text
			print 'PARSED:', text[:pos]
			return None
	else:
		print 'FULL TEXT:', text
		print '<FAIL>'
		return None


class ParsedExpressionContentListener (ElementContentListener):
	def __init__(self, ctx, node, parser):
		#super( ParsedExpressionContentListener, self ).__init__()
		self._ctx = ctx
		self._node = node
		self._parser = parser

	def contentModified(self, element):
		value = element.getContent()
		if '\n' not in value:
			parsed = _parseText( self._parser, value )
			if parsed is not None:
				#replace( self._ctx, self._node, parsed )
				replaceNodeContents( self._ctx, self._node, parsed )
			else:
				#replace( self._ctx, self._node, [ 'UNPARSED', value ] )
				replaceNodeContents( self._ctx, self._node, [ 'UNPARSED', value ] )
			return True
		else:
			return False


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
				
		if len( parsedLines ) == 1  and  parsedLines[0] == self._node:
			# Same data; ignore
			pass
		else:
			replaceWithRange( self._ctx, self._node, parsedLines )




			
PRECEDENCE_NONE = None

PRECEDENCE_STMT = 200

PRECEDENCE_COMPREHENSIONELEMENT = 175
PRECEDENCE_CONDITIONALELEMENT = 175

PRECEDENCE_TUPLEELEMENT = 150
PRECEDENCE_TUPLE = 150

PRECEDENCE_SEQUENCEELEMENT = 100
PRECEDENCE_YIELDVALUE = 100
PRECEDENCE_SUBSCRIPTINDEX = 100
PRECEDENCE_ELLIPSIS = 100
PRECEDENCE_ARG = 100
PRECEDENCE_PARAM = 100

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
PRECEDENCE_IMPORTCONTENT = 0


PRECEDENCE_TARGET = 0








def _paren(ctx, x):
	return ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '(' ), x, ctx.text( punctuation_textStyle, ')' ) ] )

def _precedenceParen(ctx, x, xPrecedence, outerPrecedence):
	if outerPrecedence is not None  and  xPrecedence is not None  and  xPrecedence > outerPrecedence:
		return _paren( ctx, x )
	else:
		return x

def computeBinOpViewPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1




MODE_DISPLAYCONTENTS = 0
MODE_EDITEXPRESSION = 1
MODE_EDITSTATEMENT = 2



def python25ViewState(outerPrecedence, parser, mode=MODE_DISPLAYCONTENTS):
	assert outerPrecedence is None  or  isinstance( outerPrecedence, int )
	assert isinstance( parser, ParserExpression )
	assert isinstance( mode, int )
	return outerPrecedence, parser, mode



def suiteView(ctx, suite, parser):
	lineViews = ctx.mapViewEvalFn( suite, None, python25ViewState( PRECEDENCE_NONE, parser, MODE_EDITSTATEMENT ) )
	newLineFac = lambda index, child: ctx.whitespace( '\n' )
	return ctx.listView( suite_listViewLayout, None, None, newLineFac, lineViews )



def expressionNodeEditor(ctx, node, contents, precedence, state):
	outerPrecedence, parser, mode = state

	if mode == MODE_DISPLAYCONTENTS:
		contents = _precedenceParen( ctx, contents, precedence, outerPrecedence )
		return contents
	elif mode == MODE_EDITEXPRESSION:
		contents = _precedenceParen( ctx, contents, precedence, outerPrecedence )
		contents = ctx.segment( python_paragraphStyle, python_segmentCaretStopFactory, contents )
		return ctx.contentListener( contents, ParsedExpressionContentListener( ctx, node, parser ) )
	elif mode == MODE_EDITSTATEMENT:
		contents = ctx.segment( python_paragraphStyle, python_segmentCaretStopFactory, contents )
		return ctx.contentListener( contents, ParsedLineContentListener( ctx, node, parser ) )
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def statementNodeEditor(ctx, node, contents, precedence, state):
	outerPrecedence, parser, mode = state

	if mode == MODE_EDITSTATEMENT:
		#contents = addContentLineStops( ctx, contents, True )
		contents = ctx.segment( python_paragraphStyle, python_segmentCaretStopFactory, contents )
		return ctx.contentListener( contents, ParsedLineContentListener( ctx, node, parser ) )
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def compoundStatementEditor(ctx, node, headerContents, precedence, suite, state, statementParser):
	outerPrecedence, parser, mode = state

	headerSegment = ctx.segment( python_paragraphStyle, python_segmentCaretStopFactory, headerContents )
	headerParagraph = ctx.paragraph( python_paragraphStyle, [ headerSegment, ctx.whitespace( '\n' ) ] )
	headerElement = ctx.contentListener( headerParagraph, ParsedLineContentListener( ctx, node, parser ) )
	statementElement = ctx.vbox( compoundStmt_vboxStyle, [ headerElement, ctx.indent( 30.0, suiteView( ctx, suite, statementParser ) ) ] )
	return statementElement



def paragraphBinOpView(ctx, state, node, x, y, op, precedence, bRightAssociative, expressionParser):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( precedence, bRightAssociative )
	xView = ctx.viewEvalFn( x, None, python25ViewState( xPrec, expressionParser ) )
	yView = ctx.viewEvalFn( y, None, python25ViewState( yPrec, expressionParser ) )
	opView = ctx.text( operator_textStyle, op )
	return expressionNodeEditor( ctx, node,
			   ctx.paragraph( python_paragraphStyle, [ xView, ctx.text( default_textStyle, ' ' ), opView, ctx.text( default_textStyle, ' ' ), yView ] ),
			   precedence,
			   state )

def paragraphPrefixOpView(ctx, state, node, x, op, precedence, expressionParser):
	xView = ctx.viewEvalFn( x, None, python25ViewState( precedence, expressionParser ) )
	opView = ctx.text( operator_textStyle, op )
	return expressionNodeEditor( ctx, node,
			   ctx.paragraph( python_paragraphStyle, [ opView, xView ] ),
			   precedence,
			   state )



def tupleView(ctx, state, node, xs, parser=None):
	if parser is not None:
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_TUPLEELEMENT, parser ) )
	else:
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_TUPLEELEMENT ) )
	return expressionNodeEditor( ctx, node,
			   ctx.listView( tuple_listViewLayout, None, None, _CommaFactory( ctx ), xViews ),
			   PRECEDENCE_TUPLE,
			   state )


def printElem(elem, level):
	print '  ' * level, elem, elem.getContent()
	if isinstance( elem, BranchElement ):
		for x in elem.getChildren():
			printElem( x, level + 1 )
	


class Python25View (GSymView):
	def __init__(self):
		self._parser = Python25Grammar()
		
		
	# MISC
	def python25Module(self, ctx, state, node, *content):
		lineViews = ctx.mapViewEvalFn( content, None, python25ViewState( PRECEDENCE_NONE, self._parser.statement(), MODE_EDITSTATEMENT ) )
		newLineFac = lambda index, child: ctx.whitespace( '\n' )
		return ctx.listView( module_listViewLayout, None, None, newLineFac, lineViews )



	def blankLine(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
				   ctx.text( default_textStyle, '' ),
				   None,
				   state )


	def UNPARSED(self, ctx, state, node, value):
		value = value.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( unparsed_textStyle, value ),
				   None,
				   state )


	# String literal
	def stringLiteral(self, ctx, state, node, format, quotation, value):
		boxContents = []
		
		format = format.toString()
		quotation = quotation.toString()
		value = value.toString()

		if format == 'ascii':
			pass
		elif format == 'unicode':
			boxContents.append( ctx.text( literalFormat_textStyle, 'u' ) )
		elif format == 'ascii-regex':
			boxContents.append( ctx.text( literalFormat_textStyle, 'r' ) )
		elif format == 'unicode-regex':
			boxContents.append( ctx.text( literalFormat_textStyle, 'ur' ) )
		else:
			raise ValueError, 'invalid string literal format'

		if quotation == 'single':
			boxContents.append( ctx.text( punctuation_textStyle, "'" ) )
			boxContents.append( None )
			boxContents.append( ctx.text( punctuation_textStyle, "'" ) )
		else:
			boxContents.append( ctx.text( punctuation_textStyle, '"' ) )
			boxContents.append( None )
			boxContents.append( ctx.text( punctuation_textStyle, '"' ) )

		boxContents[-2] = ctx.text( default_textStyle, value )

		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, boxContents ),
				   PRECEDENCE_LITERALVALUE,
				   state )


	# Integer literal
	def intLiteral(self, ctx, state, node, format, numType, value):
		boxContents = []

		format = format.toString()
		numType = numType.toString()
		value = value.toString()

		if numType == 'int':
			if format == 'decimal':
				valueString = '%d'  %  int( value )
			elif format == 'hex':
				valueString = '%x'  %  int( value, 16 )
			boxContents.append( ctx.text( numericLiteral_textStyle, valueString ) )
		elif numType == 'long':
			if format == 'decimal':
				valueString = '%d'  %  long( value )
			elif format == 'hex':
				valueString = '%x'  %  long( value, 16 )
			boxContents.append( ctx.text( numericLiteral_textStyle, valueString ) )
			boxContents.append( ctx.text( literalFormat_textStyle, 'L' ) )

		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, boxContents ),
				   PRECEDENCE_LITERALVALUE,
				   state )



	# Float literal
	def floatLiteral(self, ctx, state, node, value):
		value = value.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( numericLiteral_textStyle, value ),
				   PRECEDENCE_LITERALVALUE,
				   state )



	# Imaginary literal
	def imaginaryLiteral(self, ctx, state, node, value):
		value = value.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( numericLiteral_textStyle, value ),
				   PRECEDENCE_LITERALVALUE,
				   state )



	# Targets
	def singleTarget(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_TARGET,
				   state )


	def tupleTarget(self, ctx, state, node, *xs):
		return tupleView( ctx, state, node, xs, self._parser.targetItem() )

	def listTarget(self, ctx, state, node, *xs):
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_SEQUENCEELEMENT, self._parser.targetItem() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.listView( list_listViewLayout, _OpenBracketFactory( ctx ), _CloseBracketFactory( ctx ), _CommaFactory( ctx ), xViews ),
				   PRECEDENCE_TARGET,
				   state )




	# Variable reference
	def var(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_LOADLOCAL,
				   state )



	# Tuple literal
	def tupleLiteral(self, ctx, state, node, *xs):
		return tupleView( ctx, state, node, xs )



	# List literal
	def listLiteral(self, ctx, state, node, *xs):
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_SEQUENCEELEMENT, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.listView( list_listViewLayout, _OpenBracketFactory( ctx ), _CloseBracketFactory( ctx ), _CommaFactory( ctx ), xViews ),
				   PRECEDENCE_LISTLITERAL,
				   state )



	# List comprehension
	def listFor(self, ctx, state, node, target, source):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.targetList() ) )
		sourceView = ctx.viewEvalFn( source, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.oldTupleOrExpression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, forKeyword ), ctx.text( default_textStyle, ' ' ), targetView, ctx.text( default_textStyle, ' ' ), keywordText( ctx, inKeyword ), ctx.text( default_textStyle, ' ' ), sourceView ] ),
				   PRECEDENCE_LISTCOMPREHENSION,
				   state )

	def listIf(self, ctx, state, node, condition):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.oldExpression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, ifKeyword ), ctx.text( default_textStyle, ' ' ), conditionView ] ),
				   PRECEDENCE_LISTCOMPREHENSION,
				   state )

	def listComprehension(self, ctx, state, node, expr, *xs):
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.expression() ) )
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.listComprehensionItem() ) )
		xViewsSpaced = []
		if len( xViews ) > 0:
			for x in xViews[:-1]:
				xViewsSpaced.append( x )
				xViewsSpaced.append( ctx.whitespace( ' ', 15.0 ) )
			xViewsSpaced.append( xViews[-1] )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '[' ),  exprView,  ctx.whitespace( ' ', 15.0 ) ] + xViewsSpaced + [ ctx.text( punctuation_textStyle, ']' ) ] ),
				   PRECEDENCE_LISTCOMPREHENSION,
				   state )




	# Generator expression
	def genFor(self, ctx, state, node, target, source):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.targetList() ) )
		sourceView = ctx.viewEvalFn( source, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.orTest() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, forKeyword ), ctx.text( default_textStyle, ' ' ), targetView, ctx.text( default_textStyle, ' ' ), keywordText( ctx, inKeyword ), ctx.text( default_textStyle, ' ' ), sourceView ] ),
				   PRECEDENCE_GENERATOREXPRESSION,
				   state )

	def genIf(self, ctx, state, node, condition):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.oldExpression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, ifKeyword ), ctx.text( default_textStyle, ' ' ), conditionView ] ),
				   PRECEDENCE_GENERATOREXPRESSION,
				   state )

	def generatorExpression(self, ctx, state, node, expr, *xs):
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.expression() ) )
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_COMPREHENSIONELEMENT, self._parser.generatorExpressionItem() ) )
		xViewsSpaced = []
		if len( xViews ) > 0:
			for x in xViews[:-1]:
				xViewsSpaced.append( x )
				xViewsSpaced.append( ctx.whitespace( ' ', 15.0 ) )
			xViewsSpaced.append( xViews[-1] )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '(' ),  exprView,  ctx.whitespace( ' ', 15.0 ) ] + xViewsSpaced + [ ctx.text( punctuation_textStyle, ')' ) ] ),
				   PRECEDENCE_GENERATOREXPRESSION,
				   state )




	# Dictionary literal
	def keyValuePair(self, ctx, state, node, key, value):
		keyView = ctx.viewEvalFn( key, None, python25ViewState( PRECEDENCE_SEQUENCEELEMENT, self._parser.expression() ) )
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_SEQUENCEELEMENT, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keyView, ctx.text( punctuation_textStyle, ' : ' ), valueView ] ),
				   PRECEDENCE_NONE,
				   state )

	def dictLiteral(self, ctx, state, node, *xs):
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_SEQUENCEELEMENT, self._parser.keyValuePair() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.listView( dict_listViewLayout, _OpenBraceFactory( ctx ), _CloseBraceFactory( ctx ), _CommaFactory( ctx ), xViews ),
				   PRECEDENCE_DICTLITERAL,
				   state )


	# Yield expression
	def yieldExpr(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_YIELDVALUE, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, yieldKeyword ),  ctx.text( punctuation_textStyle, ' ' ),  valueView ] ),
				   PRECEDENCE_YIELDEXPR,
				   state )

	def yieldAtom(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_YIELDVALUE, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '(' ),  keywordText( ctx, yieldKeyword ),  ctx.text( punctuation_textStyle, ' ' ),  valueView,  ctx.text( punctuation_textStyle, ')' ) ] ),
				   PRECEDENCE_YIELDEXPR,
				   state )



	# Attribute ref
	def attributeRef(self, ctx, state, node, target, name):
		name = name.toString()
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_ATTR, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ targetView,  ctx.text( punctuation_textStyle, '.' ),  ctx.text( default_textStyle, name ) ] ),
				   PRECEDENCE_ATTR,
				   state )



	# Subscript
	def subscriptSlice(self, ctx, state, node, x, y):
		def _sliceIndex(i):
			if isNullNode( i ):
				return []
			else:
				return [ ctx.viewEvalFn( i, None, python25ViewState( PRECEDENCE_SUBSCRIPTINDEX, self._parser.expression() ) ) ]
		xView = _sliceIndex( x )
		yView = _sliceIndex( y )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, xView + [ ctx.text( punctuation_textStyle, ':' ) ] + yView ),
				   PRECEDENCE_SUBSCRIPTINDEX,
				   state )

	def subscriptLongSlice(self, ctx, state, node, x, y, z):
		def _sliceIndex(i):
			if isNullNode( i ):
				return []
			else:
				return [ ctx.viewEvalFn( i, None, python25ViewState( PRECEDENCE_SUBSCRIPTINDEX, self._parser.expression() ) ) ]
		xView = _sliceIndex( x )
		yView = _sliceIndex( y )
		zView = _sliceIndex( z )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, xView + [ ctx.text( punctuation_textStyle, ':' ) ] +  yView + [ ctx.text( punctuation_textStyle, ':' ) ] + zView ),
				   PRECEDENCE_SUBSCRIPTINDEX,
				   state )

	def ellipsis(self, ctx, state, node):
		return expressionNodeEditor( ctx, node,
				   ctx.text( punctuation_textStyle, '...' ),
				   PRECEDENCE_ELLIPSIS,
				   state )

	def subscriptTuple(self, ctx, state, node, *xs):
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_TUPLE, self._parser.subscriptItem() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.listView( tuple_listViewLayout, None, None, _CommaFactory( ctx ), xViews ),
				   PRECEDENCE_TUPLE,
				   state )

	def subscript(self, ctx, state, node, target, index):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_SUBSCRIPT, self._parser.expression() ) )
		indexView = ctx.viewEvalFn( index, None, python25ViewState( PRECEDENCE_SUBSCRIPTINDEX, self._parser.subscriptIndex() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ targetView,  ctx.text( punctuation_textStyle, '[' ),  indexView,  ctx.text( punctuation_textStyle, ']' ) ] ),
				   PRECEDENCE_SUBSCRIPT,
				   state )




	# Call
	def kwArg(self, ctx, state, node, name, value):
		name = name.toString()
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_ARG, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( default_textStyle, name ), ctx.text( punctuation_textStyle, '=' ), valueView ] ),
				   PRECEDENCE_ARG,
				   state )

	def argList(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_ARG, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '*' ),  valueView ] ),
				   PRECEDENCE_ARG,
				   state )

	def kwArgList(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_ARG, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '**' ),  valueView ] ),
				   PRECEDENCE_ARG,
				   state )

	def call(self, ctx, state, node, target, *args):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_CALL, self._parser.expression() ) )
		argViews = ctx.mapViewEvalFn( args, None, python25ViewState( PRECEDENCE_ARG, self._parser.callArg() ) )
		argElements = []
		if len( args ) > 0:
			for a in argViews[:-1]:
				argElements.append( a )
				argElements.append( ctx.text( punctuation_textStyle, ', ' ) )
			argElements.append( argViews[-1] )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ targetView, ctx.text( punctuation_textStyle, '(' ) ]  +  argElements  +  [ ctx.text( punctuation_textStyle, ')' ) ] ),
				   PRECEDENCE_CALL,
				   state )





	# Operators
	def pow(self, ctx, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = ctx.viewEvalFn( x, None, python25ViewState( xPrec, self._parser.expression() ) )
		yView = ctx.viewEvalFn( y, None, python25ViewState( yPrec, self._parser.expression(), MODE_EDITEXPRESSION ) )
		yElement = ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '**' ), ctx.text( default_textStyle, ' ' ), yView ] )
		return expressionNodeEditor( ctx, node,
				   ctx.scriptRSuper( pow_scriptStyle, xView, yElement  ),
				   PRECEDENCE_POW,
				   state )


	def invert(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )

	def negate(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )

	def pos(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )


	def mul(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '*', PRECEDENCE_MULDIVMOD, False, self._parser.expression() )

	def div(self, ctx, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = ctx.viewEvalFn( x, None, python25ViewState( xPrec, self._parser.expression(), MODE_EDITEXPRESSION ) )
		yView = ctx.viewEvalFn( y, None, python25ViewState( yPrec, self._parser.expression(), MODE_EDITEXPRESSION ) )
		return expressionNodeEditor( ctx, node,
				   ctx.fraction( div_fractionStyle, xView, yView ),
				   PRECEDENCE_MULDIVMOD,
				   state )

	def mod(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '%', PRECEDENCE_MULDIVMOD, False, self._parser.expression() )

	
	def add(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '+', PRECEDENCE_ADDSUB, False, self._parser.expression() )

	def sub(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '-', PRECEDENCE_ADDSUB, False, self._parser.expression() )


	def lshift(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '<<', PRECEDENCE_SHIFT, False, self._parser.expression() )

	def rshift(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '>>', PRECEDENCE_SHIFT, False, self._parser.expression() )


	def bitAnd(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '&', PRECEDENCE_BITAND, False, self._parser.expression() )

	def bitXor(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '^', PRECEDENCE_BITXOR, False, self._parser.expression() )

	def bitOr(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '|', PRECEDENCE_BITOR, False, self._parser.expression() )


	def lte(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '<=', PRECEDENCE_CMP, False, self._parser.expression() )

	def lt(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '<', PRECEDENCE_CMP, False, self._parser.expression() )

	def gte(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '>=', PRECEDENCE_CMP, False, self._parser.expression() )

	def gt(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '>', PRECEDENCE_CMP, False, self._parser.expression() )

	def eq(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '==', PRECEDENCE_CMP, False, self._parser.expression() )

	def neq(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, '!=', PRECEDENCE_CMP, False, self._parser.expression() )


	def isNotTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'is not', PRECEDENCE_IS, False, self._parser.expression() )

	def isTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'is', PRECEDENCE_IS, False, self._parser.expression() )

	def notInTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'not in', PRECEDENCE_IN, False, self._parser.expression() )

	def inTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'in', PRECEDENCE_IN, False, self._parser.expression() )


	def notTest(self, ctx, state, node, x):
		return paragraphPrefixOpView( ctx, state, node, x, 'not ', PRECEDENCE_NOT, self._parser.expression() )

	def andTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'and', PRECEDENCE_AND, False, self._parser.expression() )

	def orTest(self, ctx, state, node, x, y):
		return paragraphBinOpView( ctx, state, node, x, y, 'or', PRECEDENCE_OR, False, self._parser.expression() )





	# Parameters
	def simpleParam(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_PARAM,
				   state )

	def defaultValueParam(self, ctx, state, node, name, value):
		name = name.toString()
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_PARAM, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( default_textStyle, name ), ctx.text( punctuation_textStyle, '=' ), valueView ] ),
				   PRECEDENCE_PARAM,
				   state )

	def paramList(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '*' ),  ctx.text( default_textStyle, name ) ] ),
				   PRECEDENCE_PARAM,
				   state )

	def kwParamList(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '**' ),  ctx.text( default_textStyle, name ) ] ),
				   PRECEDENCE_PARAM,
				   state )



	# Lambda expression
	def lambdaExpr(self, ctx, state, node, params, expr):
		# The Python 2.5 grammar has two versions of the lambda expression grammar; one what reckognises the full lambda expression, and one that
		# reckognises a lambda expression that cannot wrap conditional expression.
		# Ensure that we use the correct parser for @expr
		exprParser = self._parser.expression()
		if state is not None:
			outerPrecedence, parser, mode = state
			if parser is self._parser.oldExpression()   or  parser is self._parser.oldTupleOrExpression():
				exprParser = self._parser.oldExpression()

		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_LAMBDAEXPR, exprParser ) )
		paramViews = ctx.mapViewEvalFn( params, None, python25ViewState( PRECEDENCE_PARAM, self._parser.param() ) )
		paramElements = []
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramElements.append( p )
				paramElements.append( ctx.text( punctuation_textStyle, ', ' ) )
			paramElements.append( paramViews[-1] )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, lambdaKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  paramElements  +  [ ctx.text( punctuation_textStyle, ': ' ), exprView ] ),
				   PRECEDENCE_LAMBDAEXPR,
				   state )



	# Conditional expression
	def conditionalExpr(self, ctx, state, node, condition, expr, elseExpr):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_CONDITIONALEXPRESSION, self._parser.orTest() ) )
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_CONDITIONALEXPRESSION, self._parser.orTest() ) )
		elseExprView = ctx.viewEvalFn( elseExpr, None, python25ViewState( PRECEDENCE_CONDITIONALEXPRESSION, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ exprView,   ctx.whitespace( '  ', 15.0 ),
									    keywordText( ctx, ifKeyword ), ctx.text( default_textStyle, ' ' ), conditionView,   ctx.whitespace( '  ', 15.0 ),
									    keywordText( ctx, elseKeyword ), ctx.text( default_textStyle, ' ' ), elseExprView ] ),
				   PRECEDENCE_CONDITIONALEXPRESSION,
				   state )





	# Assert statement
	def assertStmt(self, ctx, state, node, condition, fail):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		elements = [ keywordText( ctx, assertKeyword ), ctx.text( default_textStyle, ' ' ), conditionView ]
		if not isNullNode( fail ):
			failView = ctx.viewEvalFn( fail, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( punctuation_textStyle, ', ' ),  failView ] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, elements ),
				   PRECEDENCE_STMT,
				   state )


	# Assignment statement
	def assignmentStmt(self, ctx, state, node, targets, value):
		targetViews = ctx.mapViewEvalFn( targets, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetList() ) )
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpressionOrYieldExpression() ) )
		targetElements = []
		for t in targetViews:
			targetElements.extend( [ t,  ctx.text( punctuation_textStyle, ' = ' ) ] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, targetElements  +  [ valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Augmented assignment statement
	def augAssignStmt(self, ctx, state, node, op, target, value):
		op = op.toString()
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetItem() ) )
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpressionOrYieldExpression() ) )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ targetView,  ctx.text( punctuation_textStyle, ' ' + op + ' ' ),  valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Pass statement
	def passStmt(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
				   keywordText( ctx, passKeyword ),
				   PRECEDENCE_STMT,
				   state )


	# Del statement
	def delStmt(self, ctx, state, node, target):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetList() ) )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, delKeyword ),  ctx.text( default_textStyle, ' ' ),  targetView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Return statement
	def returnStmt(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpression() ) )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, returnKeyword ),  ctx.text( default_textStyle, ' ' ),  valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Yield statement
	def yieldStmt(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, yieldKeyword ),  ctx.text( default_textStyle, ' ' ),  valueView ] ),
				   PRECEDENCE_STMT,
				   state )


	# Raise statement
	def raiseStmt(self, ctx, state, node, *xs):
		xs = [ x   for x in xs   if not isNullNode( x ) ]
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		xElements = []
		if len( xs ) > 0:
			for x in xViews[:-1]:
				xElements.extend( [ x,  ctx.text( punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, raiseKeyword ),  ctx.text( default_textStyle, ' ' ) ] + xElements ),
				   PRECEDENCE_STMT,
				   state )


	# Break statement
	def breakStmt(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
				   keywordText( ctx, breakKeyword ),
				   PRECEDENCE_STMT,
				   state )


	# Continue statement
	def continueStmt(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
				   keywordText( ctx, continueKeyword ),
				   PRECEDENCE_STMT,
				   state )


	# Import statement
	def relativeModule(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleImport(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleImportAs(self, ctx, state, node, name, asName):
		name = name.toString()
		asName = asName.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( default_textStyle, name ),  ctx.text( default_textStyle, ' ' ),  keywordText( ctx, asKeyword ),
									    ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, asName ) ] ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleContentImport(self, ctx, state, node, name):
		name = name.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def moduleContentImportAs(self, ctx, state, node, name, asName):
		name = name.toString()
		asName = asName.toString()
		return expressionNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( default_textStyle, name ),  ctx.text( default_textStyle, ' ' ),  keywordText( ctx, asKeyword ),
									    ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, asName ) ] ),
				   PRECEDENCE_IMPORTCONTENT,
				   state )
	
	def importStmt(self, ctx, state, node, *xs):
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleImport() ) )
		xElements = []
		if len( xs ) > 0:
			for xv in xViews[:-1]:
				xElements.extend( [ xv,  ctx.text( punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, importKeyword ), ctx.text( default_textStyle, ' ' ) ]  +  xElements ),
				   PRECEDENCE_STMT,
				   state )
	
	def fromImportStmt(self, ctx, state, node, mod, *xs):
		modView = ctx.viewEvalFn( mod, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleContentImport() ) )
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleImport() ) )
		xElements = []
		if len( xs ) > 0:
			for xv in xViews[:-1]:
				xElements.extend( [ xv,  ctx.text( punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, fromKeyword ), ctx.text( default_textStyle, ' ' ), modView, ctx.text( default_textStyle, ' ' ),
									    keywordText( ctx, importKeyword ), ctx.text( default_textStyle, ' ' ) ]  +  xElements ),
				   PRECEDENCE_STMT,
				   state )
	
	def fromImportAllStmt(self, ctx, state, node, mod):
		modView = ctx.viewEvalFn( mod, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleContentImport() ) )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, fromKeyword ), ctx.text( default_textStyle, ' ' ), modView, ctx.text( default_textStyle, ' ' ),
									     keywordText( ctx, importKeyword ), ctx.text( default_textStyle, ' ' ),  ctx.text( punctuation_textStyle, '*' ) ] ),
				   PRECEDENCE_STMT,
				   state )


	# Global statement
	def globalVar(self, ctx, state, node, name):
		name = name.toString()
		return statementNodeEditor( ctx, node,
				   ctx.text( default_textStyle, name ),
				   PRECEDENCE_STMT,
				   state )
	
	def globalStmt(self, ctx, state, node, *xs):
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_STMT, self._parser.globalVar() ) )
		xElements = []
		if len( xs ) > 0:
			for xv in xViews[:-1]:
				xElements.extend( [ xv,  ctx.text( punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, globalKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  xElements ),
				   PRECEDENCE_STMT,
				   state )
	

	
	# Exec statement
	def execStmt(self, ctx, state, node, src, loc, glob):
		srcView = ctx.viewEvalFn( src, None, python25ViewState( PRECEDENCE_STMT, self._parser.orOp() ) )
		elements = [ srcView ]
		if not isNullNode( loc ):
			locView = ctx.viewEvalFn( loc, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ' ' ),  keywordText( ctx, inKeyword ),  ctx.text( default_textStyle, ' ' ),  locView ] )
		if not isNullNode( glob ):
			globView = ctx.viewEvalFn( glob, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ', ' ),  globView ] )
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, execKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  elements ),
				   PRECEDENCE_STMT,
				   state )


	
	
	
	# If statement
	def ifStmt(self, ctx, state, node, condition, suite):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, ifKeyword ),  ctx.text( default_textStyle, ' ' ),  conditionView,  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )
	
	
	
	# Elif statement
	def elifStmt(self, ctx, state, node, condition, suite):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, elifKeyword ),  ctx.text( default_textStyle, ' ' ),  conditionView,  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement())
	
	
	
	# Else statement
	def elseStmt(self, ctx, state, node, suite):
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, elseKeyword ),  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement())
	
	
	# While statement
	def whileStmt(self, ctx, state, node, condition, suite):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, whileKeyword ),  ctx.text( default_textStyle, ' ' ),  conditionView,  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )


	# For statement
	def forStmt(self, ctx, state, node, target, source, suite):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetList() ) )
		sourceView = ctx.viewEvalFn( source, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpression() ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, forKeyword ),  ctx.text( default_textStyle, ' ' ),  targetView,  ctx.text( default_textStyle, ' ' ),
											 keywordText( ctx, inKeyword ),  ctx.text( default_textStyle, ' ' ),  sourceView,  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )
	
	

	# Try statement
	def tryStmt(self, ctx, state, node, suite):
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, tryKeyword ),  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )
	
	
	
	# Except statement
	def exceptStmt(self, ctx, state, node, exc, target, suite):
		elements = []
		if not isNullNode( exc ):
			excView = ctx.viewEvalFn( exc, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ' ' ),  excView ] )
		if not isNullNode( target ):
			targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ', ' ),  targetView ] )
		elements.append( ctx.text( punctuation_textStyle, ':' ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, exceptKeyword ) ]  +  elements ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )

	
	
	# Finally statement
	def finallyStmt(self, ctx, state, node, suite):
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, finallyKeyword ),  ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )
	
	
	
	# With statement
	def withStmt(self, ctx, state, node, expr, target, suite):
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		elements = [ exprView ]
		if not isNullNode( target ):
			targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ' ' ),  keywordText( ctx, asKeyword ),  ctx.text( default_textStyle, ' ' ),  targetView ] )
		elements.append( ctx.text( punctuation_textStyle, ':' ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, withKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  elements ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )

	
	
	# Def statement
	def defStmt(self, ctx, state, node, name, params, suite):
		name = name.toString()
		paramViews = ctx.mapViewEvalFn( params, None, python25ViewState( PRECEDENCE_STMT, self._parser.param() ) )
		paramElements = [ ctx.text( punctuation_textStyle, '(' ) ]
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramElements.extend( [ p,  ctx.text( punctuation_textStyle, ', ' ) ] )
			paramElements.append( paramViews[-1] )
		paramElements.append( ctx.text( punctuation_textStyle, ')' ) )
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, defKeyword ),  ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, name ) ]  +  \
							   paramElements  +  [ ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )

	
	# Decorator statement
	def decoStmt(self, ctx, state, node, name, args):
		name = name.toString()
		if not isNullNode( args ):
			argViews = ctx.mapViewEvalFn( args, None, python25ViewState( PRECEDENCE_STMT, self._parser.callArg() ) )
			argElements = [ ctx.text( punctuation_textStyle, '(' ) ]
			if len( args ) > 0:
				for a in argViews[:-1]:
					argElements.extend( [ a, ctx.text( punctuation_textStyle, ', ' ) ] )
				argElements.append( argViews[-1] )
			argElements.append( ctx.text( punctuation_textStyle, ')' ) )
		else:
			argElements = []
		return statementNodeEditor( ctx, node,
				   ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '@' ),  ctx.text( default_textStyle, name ) ]  +  argElements ),
				   PRECEDENCE_STMT,
				   state )
	
	
	
	# Def statement
	def classStmt(self, ctx, state, node, name, inheritance, suite):
		name = name.toString()
		if not isNullNode( inheritance ):
			inhViews = ctx.mapViewEvalFn( inheritance, None, python25ViewState( PRECEDENCE_TUPLEELEMENT, self._parser.expression() ) )
			inhElements = [ ctx.text( punctuation_textStyle, '(' ),  ctx.listView( tuple_listViewLayout, None, None, _CommaFactory( ctx ), inhViews ),  ctx.text( punctuation_textStyle, ')' ) ]
		else:
			inhElements = []
			
		return compoundStatementEditor( ctx, node,
						ctx.paragraph( python_paragraphStyle, [ keywordText( ctx, classKeyword ),  ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, name ) ]  +  \
							   inhElements  +  [ ctx.text( punctuation_textStyle, ':' ) ] ),
						PRECEDENCE_STMT,
						suite,
						state,
						self._parser.statement() )
	

	
	# Comment statement
	def commentStmt(self, ctx, state, node, comment):
		comment = comment.toString()
		return statementNodeEditor( ctx, node,
				   ctx.text( comment_textStyle, '#' + comment ),
				   PRECEDENCE_STMT,
				   state )

