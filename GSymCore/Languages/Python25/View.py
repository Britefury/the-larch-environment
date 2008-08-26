##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.View.gSymView import border, indent, text, hbox, ahbox, vbox, paragraph, script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, backspaceStartMethod, deleteEndMethod, Interactor

from Britefury.gSym.View.EditOperations import replace, append, prepend, insertBefore, insertAfter

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


#class ParsedNodeInteractor (Interactor):
	#@textEventMethod()
	#def tokData(self, bUserEvent, bChanged, value, node, parser):
		#if bChanged:
			#parsed = _parseText( parser, value )
			#if parsed is not None:
				#replace( node, parsed )
			#else:
				#replace( node, [ 'UNPARSED', value ] )
	
	#eventMethods = [ tokData ]


_compoundStmtNames = set( [ 'ifStmt', 'elifStmt', 'elseStmt', 'whileStmt', 'forStmt', 'tryStmt', 'exceptStmt', 'finallyStmt', 'withStmt', 'defStmt', 'classStmt' ] )	
	

def _isCompoundStmt(node):
	return node[0] in _compoundStmtNames
	

#class ParsedLineInteractor (Interactor):
	#@textEventMethod()
	#def tokData(self, bUserEvent, bChanged, value, node, parser):
		#if bChanged:
			#if value.strip() == '':
				#node = replace( node, [ 'blankLine' ] )
			#else:
				#parsed = _parseText( parser, value )
				#if parsed is not None:
					#if _isCompoundStmt( parsed ):
						#print 'Parsed is a compound statement'
						#if _isCompoundStmt( node ):
							#print 'Original is a suite statement'
							#parsed[-1] = node[-1]
						#node = replace( node, parsed )
						#if bUserEvent:
							#print 'Inserting blankline into suite'
							#return prepend( node[-1], [ 'blankLine' ] )
						#else:
							#return node
					#else:
						#node = replace( node, parsed )
				#else:
					#node = replace( node, [ 'UNPARSED', value ] )
		#if bUserEvent:
			#print 'Inserting...'
			#return insertAfter( node, [ 'blankLine' ] )
		
		
		
	#@backspaceStartMethod()
	#def backspaceStart(self, node, parser):
		#print 'Backspace start'
	

	#@deleteEndMethod()
	#def deleteEnd(self, node, parser):
		#print 'Delete end'

	#eventMethods = [ tokData, backspaceStart, deleteEnd ]


	

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
		#return interact( focus( customEntry( highlight( contents, 'ctrl', 'ctrl' ), text.getText(), 'ctrl', 'ctrl' ) ),  ParsedNodeInteractor( node, parser ) ),   text
		return contents, metadata
	elif mode == MODE_STATEMENT:
		#return interact( focus( customEntry( highlight( contents, style=lineEditorStyle ), text.getText() ) ),  ParsedLineInteractor( node, parser ) ),   text
		return contents, metadata
	else:
		raise ValueError
		

def compoundStatementEditor(ctx, node, headerContents, metadata, suite, state):
	if state is None:
		parser = Parser.statement
		mode = MODE_STATEMENT
	else:
		parser, mode = state

	#headerWidget = interact( focus( customEntry( highlight( headerContents, style=lineEditorStyle ), headerText.getText() ) ),  ParsedLineInteractor( node, parser ) )
	headerElement = headerContents
	#statementWidget = vbox( [ headerWidget, indent( suiteView( suite ), 30.0 ) ] )
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
	return nodeEditor( node,
			   listView( ctx, tuple_listViewLayout, None, None, ',', xElements ),
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
				text( ctx, unparsed_textStyle, '<' + value + '>', unparsedStyle ),
				None,
				state )
	
	
	# Variable reference
	def var(self, ctx, state, node, name):
		return nodeEditor( ctx, node,
				text( ctx, default_textStyle, name ),
				None,
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
				paragraph( ctx, python_paragraphStyle, [ keywordText( ctx, returnKeyword ),  valueView ] ),
				PRECEDENCE_STMT,
				state )

	
	# While statement
	def whileStmt(self, ctx, state, node, condition, suite):
		whileLabel = keywordText( ctx, whileKeyword )
		conditionView = viewEval( ctx, condition )
		return compoundStatementEditor( ctx, node,
				paragraph( ctx, python_paragraphStyle, [ whileLabel,  conditionView,  text( ctx, punctuation_textStyle, ':' ) ] ),
				PRECEDENCE_STMT,
				suite,
				state  )
	
	
	
