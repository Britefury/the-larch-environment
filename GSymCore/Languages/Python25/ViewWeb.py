##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.View.gSymView import listView, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import FlowListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, backspaceStartMethod, deleteEndMethod, Interactor

from Britefury.gSym.View.EditOperations import replace, append, prepend, insertBefore, insertAfter

from Britefury.gSym.View.UnparsedText import UnparsedText

from Britefury.DocPresent.Web.UnparseAs import unparseAs
from Britefury.DocPresent.Web.Highlight import HighlightHtmlClass, highlight


from GSymCore.Languages.Python25 import Parser
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


_compoundStmtNames = set( [ 'ifStmt', 'elifStmt', 'elseStmt', 'whileStmt', 'forStmt', 'tryStmt', 'exceptStmt', 'finallyStmt', 'withStmt', 'defStmt', 'classStmt' ] )	
	

def _isCompoundStmt(node):
	return node[0] in _compoundStmtNames
	

class ParsedLineInteractor (Interactor):
	@textEventMethod()
	def tokData(self, bUserEvent, bChanged, value, node, parser):
		if bChanged:
			if value.strip() == '':
				node = replace( node, [ 'blankLine' ] )
			else:
				parsed = _parseText( parser, value )
				if parsed is not None:
					if _isCompoundStmt( parsed ):
						print 'Parsed is a compound statement'
						if _isCompoundStmt( node ):
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

PRECEDENCE_VAR = 0
PRECEDENCE_LISTLITERAL = 0
PRECEDENCE_LISTCOMPREHENSION = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_CONDITIONALEXPRESSION = 0
PRECEDENCE_DICTLITERAL = 0
PRECEDENCE_YIELDEXPR = 0
PRECEDENCE_IMPORTCONTENT=0

PRECEDENCE_SUBSCRIPTSLICE = 0
PRECEDENCE_ARG = 0
PRECEDENCE_PARAM = 0


	


def _htmlSytle(x, style):
	return '<span class="%s">%s</span>'  %  ( style, x )
	
def _punctuation(x):
	return _htmlSytle( x, 'punctuation' )

def _keyword(x):
	return _htmlSytle( x, 'keyword' )

def _suiteIndent(x):
	return _htmlStyle( x, 'indentedsuite' )

	

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
MODE_STATEMENT = 1



def python25ViewState(parser, mode=MODE_EXPRESSION):
	return parser, mode



def suiteView(ctx, suite):
	lineViews = mapViewEval( ctx, suite, None, python25ViewState( Parser.statement, MODE_STATEMENT ) )
	#return listView( VerticalListViewLayout( 0.0, 0.0, 0.0 ), None, None, None, lineViews )
	
	return '<span style="margin-left: 2em">' + '<br>\n'.join( [ lineView.reference()   for lineView in lineViews ] ) + '</span>'




#stmtHighlightClass = HighlightHtmlClass( 'stmt' )
#exprHighlightClass = HighlightHtmlClass( 'expr', 'ctrl', 'ctrl' )




exprHighlightClass = HighlightHtmlClass( 'exprHighlight', '', 'expr_highlight', 'ctrl', 'ctrl, shift, alt' )
stmtHighlightClass = HighlightHtmlClass( 'stmtHighlight', '', 'stmt_highlight', '', 'ctrl, shift, alt' )


def nodeEditor(ctx, node, html, text, state):
	if state is None:
		parser = Parser.expression
		mode = MODE_EXPRESSION
	else:
		parser, mode = state

	#assert False
	if mode == MODE_EXPRESSION:
		#return interact( focus( editAsText( ctx, highlight( ctx, contents, exprHighlightClass ), text.getText(), 'ctrl', 'ctrl' ) ),  ParsedNodeInteractor( node, parser ) ),   text
		return highlight( ctx, html, exprHighlightClass ), text
	elif mode == MODE_STATEMENT:
		#return interact( focus( editAsText( ctx, highlight( ctx, contents, stmtHighlightClass ), text.getText() ) ),  ParsedLineInteractor( node, parser ) ),   text
		return highlight( ctx, html, stmtHighlightClass ), text
	else:
		raise ValueError
		


def compoundStatementEditor(ctx, node, headerHtml, headerText, suite, state):
	if state is None:
		parser = Parser.statement
		mode = MODE_STATEMENT
	else:
		parser, mode = state
		
	headerHtml = highlight( ctx, headerHtml, stmtHighlightClass )

	#assert False
	#headerWidget = interact( focus( customEntry( highlight( headerContents, style=lineEditorStyle ), headerText.getText() ) ),  ParsedLineInteractor( node, parser ) )
	#statementWidget = vbox( [ headerWidget, indent( suiteView( suite ), 30.0 ) ] )
	#return statementWidget, headerText
	statementHtml = headerHtml + '<br>\n' + suiteView( ctx, suite )
	return statementHtml, headerText




class Python25View (GSymView):
	__css_styles__ = \
		"""
		.expr_highlight { background-color:#e0ffe0; }
		.stmt_highlight { background-color:#e0e0ff; }
		"""
	
	__js_onLoad__ = exprHighlightClass.onLoadJS() + stmtHighlightClass.onLoadJS()
	
	
	# MISC
	def python25Module(self, ctx, state, node, *content):
		lineViews = mapViewEval( ctx, content, None, python25ViewState( Parser.statement, MODE_STATEMENT ) )
		#return listView( ctx, VerticalListViewLayout( 0.0, 0.0, 0.0 ), None, None, None, lineViews ), ''
		return '<br>\n'.join( [ lineView.reference()   for lineView in lineViews ] ), ''
	

	
	def blankLine(self, ctx, state, node):
		return nodeEditor( ctx, node,
				'',
				UnparsedText( '' ),
				state )
	
	
	def UNPARSED(self, ctx, state, node, value):
		html = _htmlSytle( '&lt;' + value + '&gt;', 'unparsed' )
		return nodeEditor( ctx, node,
				html,
				UnparsedText( value ),
				#unparseAs( ctx, html, value ),
				state )
	
	
	# Variable reference
	def var(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				name,
				UnparsedText( name, PRECEDENCE_VAR ),
				#unparseAs( ctx, name, name, PRECEDENCE_VAR ),
				state )
	

	
	# Attribute ref
	def attributeRef(self, ctx, state, node, target, name):
		targetView = viewEval( ctx, target )
		html = targetView.reference() + _punctuation( '.' ) + name
		return nodeEditor( ctx, node,
				html,
				UnparsedText( targetView.text + '.' + name, PRECEDENCE_ATTR ),
				#unparseAs( ctx, html, targetView.text + '.' + unparseAs( name, name ), PRECEDENCE_ATTR ),
				state )


	
	# Return statement
	def returnStmt(self, ctx, state, node, value):
		valueView = viewEval( ctx, value, None, python25ViewState( Parser.tupleOrExpression ) )
		html = _keyword( returnKeyword ) + ' ' + valueView.reference()
		return nodeEditor( ctx, node,
				html,
				UnparsedText( returnKeyword  +  ' '  +  valueView.text, PRECEDENCE_STMT ),
				#unparseAs( ctx, html, returnKeyword + ' ' + valueView.text, PRECEDENCE_STMT ),
				state )

	
	
	# While statement
	def whileStmt(self, ctx, state, node, condition, suite):
		conditionView = viewEval( ctx, condition )
		headerHtml = _keyword( whileKeyword ) + ' ' + conditionView.reference() + _punctuation( ':' )
		return compoundStatementEditor( ctx, node,
				headerHtml,
				UnparsedText( whileKeyword + ' ' + conditionView.text + ':' ),
				#unparseAs( headerHtml, unparseAs( whileKeyword + ' ' + conditionView.text + ':' ) ),
				suite,
				state  )
