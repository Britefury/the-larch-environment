##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.View.gSymView import border, indent, text, hbox, ahbox, vbox, paragraph, script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, contentListener, viewEval, mapViewEval, GSymView
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

def parargraphPrefixOpView(ctx, x, op, precedence):
	x = _precedenceGT( ctx, x, precedence )
	return paragraph( ctx, python_paragraphStyle, [ op, x ] )

def paragraphBinOpView(ctx, x, y, op, precedence, bRightAssociative=False):
	if bRightAssociative:
		x = _precedenceGTE( ctx, x, precedence )
		y = _precedenceGT( ctx, y, precedence )
	else:
		x = _precedenceGT( ctx, x, precedence )
		y = _precedenceGTE( ctx, y, precedence )
	return paragraph( ctx, python_paragraphStyle, [ x, op, y ] )

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
		
	headerParagraph = paragraph( ctx, python_paragraphStyle, [ headerContents, text( ctx, default_textStyle, '\n' ) ] )

	headerElement = contentListener( ctx, headerParagraph, ParsedLineContentListener( ctx, node, parser ) )
	statementElement = vbox( ctx, compoundStmt_vboxStyle, [ headerElement, indent( ctx, 30.0, suiteView( ctx, suite ) ) ] )
	return statementElement, metadata
		


def binOpView(ctx, state, node, x, y, unparsedOp, widgetFactory, precedence):
	xView = viewEval( ctx, x )
	yView = viewEval( ctx, y )
	return nodeEditor( ctx, node,
			elementFactory( ctx, state, node, x, y, xView, yView ),
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
				nameUnparsed,
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
				paragraph( ctx, listComp_paragraphStyle, [ keywordText( ctx, forKeyword ), text( ctx, default_textStyle, ' ' ), targetView, text( ctx, default_textStyle, ' ' ), keywordText( ctx, inKeyword ), text( ctx, default_textStyle, ' ' ), sourceView ] ),
				PRECEDENCE_LISTCOMPREHENSION,
				state )
	
	def listIf(self, ctx, state, node, condition):
		conditionView = viewEval( ctx, condition, None, python25ViewState( Parser.oldExpression ) )
		return nodeEditor( ctx, node,
				paragraph( ctx, listComp_paragraphStyle, [ keywordText( ctx, ifKeyword ), text( ctx, default_textStyle, ' ' ), conditionView ] ),
				PRECEDENCE_LISTCOMPREHENSION,
				state )
	
	def listComprehension(self, ctx, state, node, expr, *xs):
		exprView = viewEval( ctx, expr )
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.listComprehensionItem ) )
		return nodeEditor( ctx, node,
				paragraph( ctx, listComp_paragraphStyle, [ text( ctx, punctuation_textStyle, '[' ),  exprView ] + xViews + [ text( ctx, punctuation_textStyle, ']' ) ] ),
				PRECEDENCE_LISTCOMPREHENSION,
				state )
	
	
	

	# Generator expression
	def genFor(self, ctx, state, node, target, source):
		targetView = viewEval( ctx, target, None, python25ViewState( Parser.targetList ) )
		sourceView = viewEval( ctx, source, None, python25ViewState( Parser.orTest ) )
		return nodeEditor( ctx, node,
				paragraph( ctx, listComp_paragraphStyle, [ keywordText( ctx, forKeyword ), text( ctx, default_textStyle, ' ' ), targetView, text( ctx, default_textStyle, ' ' ), keywordText( ctx, inKeyword ), text( ctx, default_textStyle, ' ' ), sourceView ] ),
				PRECEDENCE_GENERATOREXPRESSION,
				state )
	
	def genIf(self, ctx, state, node, condition):
		conditionView = viewEval( ctx, condition, None, python25ViewState( Parser.oldExpression ) )
		return nodeEditor( ctx, node,
				paragraph( ctx, listComp_paragraphStyle, [ keywordText( ctx, ifKeyword ), text( ctx, default_textStyle, ' ' ), conditionView ] ),
				PRECEDENCE_GENERATOREXPRESSION,
				state )
	
	def generatorExpression(self, ctx, state, node, expr, *xs):
		exprView = viewEval( ctx, expr )
		xViews = mapViewEval( ctx, xs, None, python25ViewState( Parser.generatorExpressionItem ) )
		return nodeEditor( ctx, node,
				paragraph( ctx, listComp_paragraphStyle, [ text( ctx, punctuation_textStyle, '(' ),  exprView ] + xViews + [ text( ctx, punctuation_textStyle, ')' ) ] ),
				PRECEDENCE_GENERATOREXPRESSION,
				 state )

	
	
	
	# Attribute ref
	def attributeRef(self, ctx, state, node, target, name):
		return nodeEditor( ctx, node,
				paragraph( ctx, python_paragraphStyle, [ viewEval( ctx, target ),  text( ctx, punctuation_textStyle, '.' ),  text( ctx, default_textStyle, name ) ] ),
				PRECEDENCE_ATTR,
				state )


	
	# Return statement
	def returnStmt(self, ctx, state, node, value):
		valueView = viewEval( ctx, value, None, python25ViewState( Parser.tupleOrExpression ) )
		return nodeEditor( ctx, node,
				paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, returnKeyword ),  text( ctx, default_textStyle, ' ' ),  valueView ] ),
				PRECEDENCE_STMT,
				state )

	
	# While statement
	def whileStmt(self, ctx, state, node, condition, suite):
		whileLabel = keywordText( ctx, whileKeyword )
		conditionView = viewEval( ctx, condition )
		return compoundStatementEditor( ctx, node,
				paragraph( ctx, python_paragraphStyle, [ whileLabel,  text( ctx, default_textStyle, ' ' ),  conditionView,  text( ctx, punctuation_textStyle, ':' ) ] ),
				PRECEDENCE_STMT,
				suite,
				state  )
	
	
	# Comment statement
	def commentStmt(self, ctx, state, node, comment):
		return nodeEditor( ctx, node,
				text( ctx, comment_textStyle, '#' + comment ),
				PRECEDENCE_STMT,
				state )
	
