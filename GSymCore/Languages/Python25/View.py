##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.Parser import ParserExpression
from BritefuryJ.Parser.ItemStream import ItemStreamBuilder

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym.View import GSymViewContext
from BritefuryJ.GSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout



from GSymCore.Languages.Python25.Parser import Python25Grammar
from GSymCore.Languages.Python25.Styles import *
from GSymCore.Languages.Python25.PythonEditOperations import *
from GSymCore.Languages.Python25.NodeEditor import *
from GSymCore.Languages.Python25.SelectionEditor import *
from GSymCore.Languages.Python25.Keywords import *
from GSymCore.Languages.Python25.Precedence import *
from GSymCore.Languages.Python25 import NodeClasses as Nodes




DEFAULT_LINE_BREAK_PRIORITY = 100



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


def capitalisedKeywordText(ctx, keyword):
	text = keyword[:1].upper()  +  keyword[1:]
	return ctx.textWithContent( capitalisedKeyword_textStyle, text, keyword )



_statementKeyboardListener = StatementKeyboardListener()





def _paren(ctx, x):
	return ctx.span( [ ctx.text( punctuation_textStyle, '(' ), x, ctx.text( punctuation_textStyle, ')' ) ] )

def _precedenceParen(ctx, node, x, xPrecedence, outerPrecedence):
	if node.isInstanceOf( Nodes.Expr )  or  node.isInstanceOf( Nodes.Target ):
		numParens = getNumParens( node )
	else:
		numParens = 0
	if outerPrecedence is not None  and  xPrecedence is not None  and  xPrecedence > outerPrecedence:
		numParens += 1
	for i in xrange( 0, numParens ):
		x = _paren( ctx, x )
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




def expressionNodeEditor(ctx, node, contents, precedence, state):
	outerPrecedence, parser, mode = state

	if mode == MODE_DISPLAYCONTENTS:
		contents = _precedenceParen( ctx, node, contents, precedence, outerPrecedence )
		return contents
	elif mode == MODE_EDITEXPRESSION:
		contents = _precedenceParen( ctx, node, contents, precedence, outerPrecedence )
		return ctx.linearRepresentationListener( contents, ParsedExpressionLinearRepresentationListener.newListener( parser, outerPrecedence ) )
	elif mode == MODE_EDITSTATEMENT:
		return statementNodeEditor( ctx, node, contents, precedence, state )
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def statementNodeEditor(ctx, node, contents, precedence, state):
	outerPrecedence, parser, mode = state

	if mode == MODE_EDITSTATEMENT:
		contents = _precedenceParen( ctx, node, contents, precedence, outerPrecedence )
		segment = ctx.segment( default_textStyle, True, True, contents )
		newLine = ctx.whitespace( '\n' )

		para = ctx.paragraph( python_paragraphStyle, [ segment, newLine ] )
		if node.isInstanceOf( Nodes.UNPARSED ):
			builder = ItemStreamBuilder()
			for x in node['value']:
				if isinstance( x, str )  or  isinstance( x, unicode ):
					builder.appendTextValue( x )
				elif isinstance( x, DMObjectInterface ):
					builder.appendStructuralValue( x )
				else:
					raise TypeError, 'UNPARSED node should only contain strings or objects, not %s'  %  ( type( x ), )
			para.setStructuralValueStream( builder.stream() )
		else:
			para.setStructuralValueObject( node )
		para = ctx.linearRepresentationListener( para, StatementLinearRepresentationListener.newListener( parser ) )
		para = ctx.keyboardListener( para, _statementKeyboardListener )
		return para
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def compoundStatementHeaderEditor(ctx, node, headerContents, precedence, state, headerContainerFn=None):
	outerPrecedence, parser, mode = state

	# THE EDIT OPERATIONS RELY ON THE ELEMENT STRUCTURE USED HERE:
	#	Paragraph - header
	#		Segment - header
	#			header content
	#		NewLine - header

	segment = ctx.segment( default_textStyle, True, True, headerContents )
	newLine = ctx.whitespace( '\n' )

	para = ctx.paragraph( python_paragraphStyle, [ segment, newLine ] )
	para.setStructuralValueObject( node )
	para = ctx.linearRepresentationListener( para, StatementLinearRepresentationListener.newListener( parser ) )
	para = ctx.keyboardListener( para, _statementKeyboardListener )
	if headerContainerFn is not None:
		para = headerContainerFn( para )
	return para


def compoundStatementEditor(ctx, node, precedence, compoundBlocks, state, suiteParser, statementParser):
	outerPrecedence, parser, mode = state

	# THE EDIT OPERATIONS RELY ON THE ELEMENT STRUCTURE USED HERE:
	#	VBox - compound stmt
	#		Paragraph - header
	#			Segment - header
	#				header content
	#			NewLine - header
	#		Indent - suite
	#			suite view

	statementContents = []
	for i, block in enumerate( compoundBlocks ):
		if len( block ) == 3:
			headerNode, headerContents, suite = block
			headerContainerFn = None
		elif len( block ) == 4:
			headerNode, headerContents, suite, headerContainerFn = block
		else:
			raise TypeError, 'Compound block should be of the form (headerContents, suite)  or  (headerContents, suite, headerContainerFn)'
		
		headerSegment = ctx.segment( default_textStyle, True, True, headerContents )
		newLine = ctx.whitespace( '\n' )
		
		headerParagraph = ctx.paragraph( python_paragraphStyle, [ headerSegment, newLine ] )
		headerParagraph.setStructuralValueObject( headerNode )
		headerParagraph = ctx.linearRepresentationListener( headerParagraph, CompoundHeaderLinearRepresentationListener.newListener( statementParser ) )
		headerParagraph = ctx.keyboardListener( headerParagraph, _statementKeyboardListener )
		
		if headerContainerFn is not None:
			headerParagraph = headerContainerFn( headerParagraph )



		if suite is not None:
			suiteElement = indentedSuiteView( ctx, suite, statementParser )
			suiteElement.setStructuralValueObject( Nodes.IndentedBlock( suite=suite ) )
			suiteElement = ctx.linearRepresentationListener( suiteElement, SuiteLinearRepresentationListener( suiteParser, suite ) )
			
			statementContents.extend( [ headerParagraph.alignHExpand(), ctx.indent( 30.0, suiteElement ).alignHExpand() ] )
		else:
			statementContents.append( headerParagraph.alignHExpand() )
			
	statementElement = ctx.vbox( compoundStmt_vboxStyle, statementContents )
	return statementElement



def spanBinOpView(ctx, state, node, x, y, op, precedence, bRightAssociative, expressionParser):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( precedence, bRightAssociative )
	xView = ctx.viewEvalFn( x, None, python25ViewState( xPrec, expressionParser ) )
	yView = ctx.viewEvalFn( y, None, python25ViewState( yPrec, expressionParser ) )
	opView = ctx.text( operator_textStyle, op )
	return expressionNodeEditor( ctx, node,
				     ctx.span( [ xView, ctx.text( default_textStyle, ' ' ), opView, ctx.lineBreak( precedence, ctx.text( default_textStyle, ' ' ) ), yView ] ),
				     precedence,
				     state )

def spanCmpOpView(ctx, state, node, op, y, precedence, expressionParser):
	opView = ctx.text( operator_textStyle, op )
	yView = ctx.viewEvalFn( y, None, python25ViewState( precedence, expressionParser ) )
	return expressionNodeEditor( ctx, node,
				     ctx.span( [ ctx.text( default_textStyle, ' ' ), opView, ctx.lineBreak( precedence, ctx.text( default_textStyle, ' ' ) ), yView ] ),
				     precedence,
				     state )

def spanPrefixOpView(ctx, state, node, x, op, precedence, expressionParser):
	xView = ctx.viewEvalFn( x, None, python25ViewState( precedence, expressionParser ) )
	opView = ctx.text( operator_textStyle, op )
	return expressionNodeEditor( ctx, node,
				     ctx.span( [ opView, xView ] ),
				     precedence,
				     state )



def tupleView(ctx, state, node, xs, trailingSeparator, parser):
	xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, parser ) )
	layout = tuple_listViewLayout   if trailingSeparator is None   else tuple_listViewLayoutSep
	return expressionNodeEditor( ctx, node,
				     ctx.listView( layout, None, None, _CommaFactory( ctx ), xViews ),
				     PRECEDENCE_TUPLE,
				     state )


def suiteView(ctx, suite, parser):
	# THE EDIT OPERATIONS RELY ON THE ELEMENT STRUCTURE USED HERE:
	#	VBox - suite
	#		children*

	lineViews = ctx.mapViewEvalFn( suite, None, python25ViewState( PRECEDENCE_NONE, parser, MODE_EDITSTATEMENT ) )
	return ctx.vbox( suite_vboxStyle, lineViews )


def indentedSuiteView(ctx, suite, parser):
	# THE EDIT OPERATIONS RELY ON THE ELEMENT STRUCTURE USED HERE:
	#	VBox - suite
	#		children*

	indent = ctx.hiddenStructuralObject( Nodes.Indent() )
	dedent = ctx.hiddenStructuralObject( Nodes.Dedent() )
	lineViews = [ indent ]  + ctx.mapViewEvalFn( suite, None, python25ViewState( PRECEDENCE_NONE, parser, MODE_EDITSTATEMENT ) )  +  [ dedent ]
	return ctx.vbox( suite_vboxStyle, lineViews )


def printElem(elem, level):
	print '  ' * level, elem, elem.getTextRepresentation()
	if isinstance( elem, BranchElement ):
		for x in elem.getChildren():
			printElem( x, level + 1 )



class Python25View (GSymViewObjectNodeDispatch):
	__dispatch_module__ = Nodes.module


	def __init__(self):
		self._parser = Python25Grammar()


	# MISC
	@ObjectNodeDispatchMethod
	def PythonModule(self, ctx, state, node, suite):
		suiteElement = suiteView( ctx, suite, self._parser.singleLineStatement() )
		suiteElement.setStructuralValueObject( suite )
		suiteElement = ctx.linearRepresentationListener( suiteElement, SuiteLinearRepresentationListener( self._parser.suite(), suite ) )
		return suiteElement



	@ObjectNodeDispatchMethod
	def BlankLine(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
					    ctx.text( default_textStyle, '' ),
					    None,
					    state )


	@ObjectNodeDispatchMethod
	def UNPARSED(self, ctx, state, node, value):
		def _viewItem(x):
			if isinstance( x, str )  or  isinstance( x, unicode ):
				return ctx.text( unparsed_textStyle, x )
			elif isinstance( x, DMObjectInterface ):
				return ctx.viewEvalFn( x, None, python25ViewState( PRECEDENCE_CONTAINER_UNPARSED, self._parser.expression() ) )
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return expressionNodeEditor( ctx, node,
					     ctx.span( views ),
					     None,
					     state )


	# String literal
	@ObjectNodeDispatchMethod
	def StringLiteral(self, ctx, state, node, format, quotation, value):
		boxContents = []

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
					     ctx.span( boxContents ),
					     PRECEDENCE_LITERALVALUE,
					     state )


	# Integer literal
	@ObjectNodeDispatchMethod
	def IntLiteral(self, ctx, state, node, format, numType, value):
		boxContents = []

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
					     ctx.span( boxContents ),
					     PRECEDENCE_LITERALVALUE,
					     state )



	# Float literal
	@ObjectNodeDispatchMethod
	def FloatLiteral(self, ctx, state, node, value):
		return expressionNodeEditor( ctx, node,
					     ctx.text( numericLiteral_textStyle, value ),
					     PRECEDENCE_LITERALVALUE,
					     state )



	# Imaginary literal
	@ObjectNodeDispatchMethod
	def ImaginaryLiteral(self, ctx, state, node, value):
		return expressionNodeEditor( ctx, node,
					     ctx.text( numericLiteral_textStyle, value ),
					     PRECEDENCE_LITERALVALUE,
					     state )



	# Targets
	@ObjectNodeDispatchMethod
	def SingleTarget(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.text( default_textStyle, name ),
					     PRECEDENCE_TARGET,
					     state )


	@ObjectNodeDispatchMethod
	def TupleTarget(self, ctx, state, node, targets, trailingSeparator):
		return tupleView( ctx, state, node, targets, trailingSeparator, self._parser.targetItem() )

	@ObjectNodeDispatchMethod
	def ListTarget(self, ctx, state, node, targets, trailingSeparator):
		targetViews = ctx.mapViewEvalFn( targets, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.targetItem() ) )
		layout = list_listViewLayout   if trailingSeparator is None   else list_listViewLayoutSep		
		return expressionNodeEditor( ctx, node,
					     ctx.listView( layout, _OpenBracketFactory( ctx ), _CloseBracketFactory( ctx ), _CommaFactory( ctx ), targetViews ),
					     PRECEDENCE_TARGET,
					     state )




	# Variable reference
	@ObjectNodeDispatchMethod
	def Load(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.text( default_textStyle, name ),
					     PRECEDENCE_LOAD,
					     state )



	# Tuple literal
	@ObjectNodeDispatchMethod
	def TupleLiteral(self, ctx, state, node, values, trailingSeparator):
		return tupleView( ctx, state, node, values, trailingSeparator, self._parser.expression() )



	# List literal
	@ObjectNodeDispatchMethod
	def ListLiteral(self, ctx, state, node, values, trailingSeparator):
		valueViews = ctx.mapViewEvalFn( values, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		layout = list_listViewLayout   if trailingSeparator is None   else list_listViewLayoutSep		
		return expressionNodeEditor( ctx, node,
					     ctx.listView( layout, _OpenBracketFactory( ctx ), _CloseBracketFactory( ctx ), _CommaFactory( ctx ), valueViews ),
					     PRECEDENCE_LISTDISPLAY,
					     state )



	# List comprehension / generator expression
	@ObjectNodeDispatchMethod
	def ComprehensionFor(self, ctx, state, node, target, source):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_CONTAINER_COMPREHENSIONFOR, self._parser.targetListOrTargetItem() ) )
		sourceView = ctx.viewEvalFn( source, None, python25ViewState( PRECEDENCE_CONTAINER_COMPREHENSIONFOR, self._parser.oldTupleOrExpression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ capitalisedKeywordText( ctx, forKeyword ), ctx.text( default_textStyle, ' ' ), targetView, ctx.text( default_textStyle, ' ' ), capitalisedKeywordText( ctx, inKeyword ), ctx.text( default_textStyle, ' ' ), sourceView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def ComprehensionIf(self, ctx, state, node, condition):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_CONTAINER_COMPREHENSIONIF, self._parser.oldExpression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ capitalisedKeywordText( ctx, ifKeyword ), ctx.text( default_textStyle, ' ' ), conditionView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def ListComp(self, ctx, state, node, resultExpr, comprehensionItems):
		exprView = ctx.viewEvalFn( resultExpr, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		itemViews = ctx.mapViewEvalFn( comprehensionItems, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.listCompItem() ) )
		itemViewsSpaced = []
		if len( itemViews ) > 0:
			for x in itemViews[:-1]:
				itemViewsSpaced.append( x )
				itemViewsSpaced.append( ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.whitespace( ' ', 15.0 ) ) )
			itemViewsSpaced.append( itemViews[-1] )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( punctuation_textStyle, '[' ),  exprView,  ctx.whitespace( ' ', 15.0 ) ] + itemViewsSpaced + [ ctx.text( punctuation_textStyle, ']' ) ] ),
					     PRECEDENCE_LISTDISPLAY,
					     state )


	@ObjectNodeDispatchMethod
	def GeneratorExpr(self, ctx, state, node, resultExpr, comprehensionItems):
		exprView = ctx.viewEvalFn( resultExpr, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		itemViews = ctx.mapViewEvalFn( comprehensionItems, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.genExpItem() ) )
		itemViewsSpaced = []
		if len( itemViews ) > 0:
			for x in itemViews[:-1]:
				itemViewsSpaced.append( x )
				itemViewsSpaced.append( ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.whitespace( ' ', 15.0 ) ) )
			itemViewsSpaced.append( itemViews[-1] )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( punctuation_textStyle, '(' ),  exprView,  ctx.whitespace( ' ', 15.0 ) ] + itemViewsSpaced + [ ctx.text( punctuation_textStyle, ')' ) ] ),
					     PRECEDENCE_GENERATOREXPRESSION,
					     state )




	# Dictionary literal
	@ObjectNodeDispatchMethod
	def DictKeyValuePair(self, ctx, state, node, key, value):
		keyView = ctx.viewEvalFn( key, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ keyView, ctx.text( punctuation_textStyle, ' : ' ), valueView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def DictLiteral(self, ctx, state, node, values, trailingSeparator):
		valueViews = ctx.mapViewEvalFn( values, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.keyValuePair() ) )
		layout = dict_listViewLayout   if trailingSeparator is None   else dict_listViewLayoutSep		
		return expressionNodeEditor( ctx, node,
					     ctx.listView( layout, _OpenBraceFactory( ctx ), _CloseBraceFactory( ctx ), _CommaFactory( ctx ), valueViews ),
					     PRECEDENCE_DICTDISPLAY,
					     state )


	# Yield expression
	@ObjectNodeDispatchMethod
	def YieldExpr(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_CONTAINER_YIELDEXPR, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ capitalisedKeywordText( ctx, yieldKeyword ),  ctx.text( punctuation_textStyle, ' ' ),  valueView ] ),
					     PRECEDENCE_YIELDEXPR,
					     state )



	# Attribute ref
	@ObjectNodeDispatchMethod
	def AttributeRef(self, ctx, state, node, target, name):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ targetView,  ctx.text( punctuation_textStyle, '.' ),  ctx.text( default_textStyle, name ) ] ),
					     PRECEDENCE_ATTR,
					     state )



	# Subscript
	@ObjectNodeDispatchMethod
	def SubscriptSlice(self, ctx, state, node, lower, upper):
		def _sliceIndex(i):
			if i is None:
				return []
			else:
				return [ ctx.viewEvalFn( i, None, python25ViewState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) ) ]
		lowerView = _sliceIndex( lower )
		upperView = _sliceIndex( upper )
		return expressionNodeEditor( ctx, node,
					     ctx.span( lowerView + [ ctx.text( punctuation_textStyle, ':' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY ) ] + upperView ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def SubscriptLongSlice(self, ctx, state, node, lower, upper, stride):
		def _sliceIndex(i):
			if i is None:
				return []
			else:
				return [ ctx.viewEvalFn( i, None, python25ViewState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) ) ]
		lowerView = _sliceIndex( lower )
		upperView = _sliceIndex( upper )
		strideView = _sliceIndex( stride )
		return expressionNodeEditor( ctx, node,
					     ctx.span( lowerView + [ ctx.text( punctuation_textStyle, ':' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY ) ] +  upperView + \
						       [ ctx.text( punctuation_textStyle, ':' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY ) ] + strideView ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def SubscriptEllipsis(self, ctx, state, node):
		return expressionNodeEditor( ctx, node,
					     ctx.text( punctuation_textStyle, '...' ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def SubscriptTuple(self, ctx, state, node, values, trailingSeparator):
		valueViews = ctx.mapViewEvalFn( values, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.subscriptItem() ) )
		layout = tuple_listViewLayout   if trailingSeparator is None   else tuple_listViewLayoutSep
		return expressionNodeEditor( ctx, node,
					     ctx.listView( layout, None, None, _CommaFactory( ctx ), valueViews ),
					     PRECEDENCE_TUPLE,
					     state )

	@ObjectNodeDispatchMethod
	def Subscript(self, ctx, state, node, target, index):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_CONTAINER_SUBSCRIPTTARGET, self._parser.expression() ) )
		indexView = ctx.viewEvalFn( index, None, python25ViewState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.subscriptIndex() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ targetView,  ctx.text( punctuation_textStyle, '[' ),  indexView,  ctx.text( punctuation_textStyle, ']' ) ] ),
					     PRECEDENCE_SUBSCRIPT,
					     state )




	# Call
	@ObjectNodeDispatchMethod
	def CallKWArg(self, ctx, state, node, name, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_CONTAINER_CALLARG, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( default_textStyle, name ), ctx.text( punctuation_textStyle, '=' ), valueView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def CallArgList(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_CONTAINER_CALLARG, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( punctuation_textStyle, '*' ),  valueView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def CallKWArgList(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_CONTAINER_CALLARG, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( punctuation_textStyle, '**' ),  valueView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def Call(self, ctx, state, node, target, args, argsTrailingSeparator):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_CONTAINER_CALLTARGET, self._parser.expression() ) )
		argViews = ctx.mapViewEvalFn( args, None, python25ViewState( PRECEDENCE_CONTAINER_CALLARG, self._parser.callArg() ) )
		argElements = []
		if len( args ) > 0:
			argElements.append( ctx.text( default_textStyle, ' ' ) )
			for a in argViews[:-1]:
				argElements.append( a )
				argElements.append( ctx.text( punctuation_textStyle, ',' ) )
				argElements.append( ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) )
			argElements.append( argViews[-1] )
			if argsTrailingSeparator is not None:
				argElements.append( ctx.text( punctuation_textStyle, ',' ) )
				argElements.append( ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) )
			argElements.append( ctx.text( default_textStyle, ' ' ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ targetView, ctx.text( punctuation_textStyle, '(' ) ]  +  argElements  +  [ ctx.text( punctuation_textStyle, ')' ) ] ),
					     PRECEDENCE_CALL,
					     state )





	# Operators
	@ObjectNodeDispatchMethod
	def Pow(self, ctx, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = ctx.viewEvalFn( x, None, python25ViewState( xPrec, self._parser.expression() ) )
		xElement = ctx.paragraph( python_paragraphStyle, [ xView ] )
		yView = ctx.viewEvalFn( y, None, python25ViewState( yPrec, self._parser.expression(), MODE_EDITEXPRESSION ) )
		yElement = ctx.paragraph( python_paragraphStyle, [ ctx.text( punctuation_textStyle, '**' ), ctx.text( default_textStyle, ' ' ), yView ] )
		return expressionNodeEditor( ctx, node,
					     ctx.scriptRSuper( pow_scriptStyle, xElement, yElement ),
					     PRECEDENCE_POW,
					     state )


	@ObjectNodeDispatchMethod
	def Invert(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, state, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def Negate(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, state, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def Pos(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, state, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )


	@ObjectNodeDispatchMethod
	def Mul(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '*', PRECEDENCE_MULDIVMOD, False, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def Div(self, ctx, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = ctx.viewEvalFn( x, None, python25ViewState( xPrec, self._parser.expression(), MODE_EDITEXPRESSION ) )
		yView = ctx.viewEvalFn( y, None, python25ViewState( yPrec, self._parser.expression(), MODE_EDITEXPRESSION ) )
		element = ctx.fraction( div_fractionStyle, xView, yView, '/' )
		element.setStructuralValueObject( node )
		return expressionNodeEditor( ctx, node,
					     element,
					     PRECEDENCE_MULDIVMOD,
					     state )

	@ObjectNodeDispatchMethod
	def Mod(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '%', PRECEDENCE_MULDIVMOD, False, self._parser.expression() )


	@ObjectNodeDispatchMethod
	def Add(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '+', PRECEDENCE_ADDSUB, False, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def Sub(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '-', PRECEDENCE_ADDSUB, False, self._parser.expression() )


	@ObjectNodeDispatchMethod
	def LShift(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '<<', PRECEDENCE_SHIFT, False, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def RShift(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '>>', PRECEDENCE_SHIFT, False, self._parser.expression() )


	@ObjectNodeDispatchMethod
	def BitAnd(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '&', PRECEDENCE_BITAND, False, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def BitXor(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '^', PRECEDENCE_BITXOR, False, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def BitOr(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, '|', PRECEDENCE_BITOR, False, self._parser.expression() )


	@ObjectNodeDispatchMethod
	def Cmp(self, ctx, state, node, x, ops):
		xView = ctx.viewEvalFn( x, None, python25ViewState( PRECEDENCE_CMP, self._parser.expression() ) )
		opViews = ctx.mapViewEvalFn( ops, None, python25ViewState( PRECEDENCE_CMP, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ xView ] + opViews ),
					     PRECEDENCE_CMP,
					     state )

	@ObjectNodeDispatchMethod
	def CmpOpLte(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, '<=', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpLt(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, '<', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpGte(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, '>=', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpGt(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, '>', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpEq(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, '==', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpNeq(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, '!=', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpIsNot(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, 'is not', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpIs(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, 'is', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpNotIn(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, 'not in', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def CmpOpIn(self, ctx, state, node, y):
		return spanCmpOpView( ctx, state, node, 'in', y, PRECEDENCE_CMP, self._parser.expression() )



	@ObjectNodeDispatchMethod
	def NotTest(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, state, node, x, 'not ', PRECEDENCE_NOT, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def AndTest(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, 'and', PRECEDENCE_AND, False, self._parser.expression() )

	@ObjectNodeDispatchMethod
	def OrTest(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, state, node, x, y, 'or', PRECEDENCE_OR, False, self._parser.expression() )





	# Parameters
	@ObjectNodeDispatchMethod
	def SimpleParam(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.text( default_textStyle, name ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def DefaultValueParam(self, ctx, state, node, name, defaultValue):
		valueView = ctx.viewEvalFn( defaultValue, None, python25ViewState( PRECEDENCE_NONE, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( default_textStyle, name ), ctx.text( punctuation_textStyle, '=' ), valueView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def ParamList(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( punctuation_textStyle, '*' ),  ctx.text( default_textStyle, name ) ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod
	def KWParamList(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( punctuation_textStyle, '**' ),  ctx.text( default_textStyle, name ) ] ),
					     PRECEDENCE_NONE,
					     state )



	# Lambda expression
	@ObjectNodeDispatchMethod
	def LambdaExpr(self, ctx, state, node, params, paramsTrailingSeparator, expr):
		# The Python 2.5 grammar has two versions of the lambda expression grammar; one what reckognises the full lambda expression, and one that
		# reckognises a lambda expression that cannot wrap conditional expression.
		# Ensure that we use the correct parser for @expr
		exprParser = self._parser.expression()
		if state is not None:
			outerPrecedence, parser, mode = state
			if parser is self._parser.oldExpression()   or  parser is self._parser.oldTupleOrExpression():
				exprParser = self._parser.oldExpression()

		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_CONTAINER_LAMBDAEXPR, exprParser ) )
		paramViews = ctx.mapViewEvalFn( params, None, python25ViewState( PRECEDENCE_NONE, self._parser.param() ) )
		paramElements = []
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramElements.append( p )
				paramElements.append( ctx.text( punctuation_textStyle, ',' ) )
				paramElements.append( ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) )
			paramElements.append( paramViews[-1] )
			if paramsTrailingSeparator is not None:
				paramElements.append( ctx.text( punctuation_textStyle, ',' ) )
				paramElements.append( ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) )

		return expressionNodeEditor( ctx, node,
					     ctx.span( [ capitalisedKeywordText( ctx, lambdaKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  paramElements  +  \
						       [ ctx.text( punctuation_textStyle, ':' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ), exprView ] ),
					     PRECEDENCE_LAMBDAEXPR,
					     state )



	# Conditional expression
	@ObjectNodeDispatchMethod
	def ConditionalExpr(self, ctx, state, node, condition, expr, elseExpr):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_CONTAINER_CONDITIONALEXPR, self._parser.orTest() ) )
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_CONTAINER_CONDITIONALEXPR, self._parser.orTest() ) )
		elseExprView = ctx.viewEvalFn( elseExpr, None, python25ViewState( PRECEDENCE_CONTAINER_CONDITIONALEXPR, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ exprView,   ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.whitespace( '  ', 15.0 ) ),
							 capitalisedKeywordText( ctx, ifKeyword ), ctx.text( default_textStyle, ' ' ), conditionView,   ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.whitespace( '  ', 15.0 ) ),
							 capitalisedKeywordText( ctx, elseKeyword ), ctx.text( default_textStyle, ' ' ), elseExprView ] ),
					     PRECEDENCE_CONDITIONAL,
					     state )




	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Expression statement
	@ObjectNodeDispatchMethod
	def ExprStmt(self, ctx, state, node, expr):
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return statementNodeEditor( ctx, node, exprView, PRECEDENCE_STMT, state )



	# Assert statement
	@ObjectNodeDispatchMethod
	def AssertStmt(self, ctx, state, node, condition, fail):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		elements = [ capitalisedKeywordText( ctx, assertKeyword ), ctx.text( default_textStyle, ' ' ), conditionView ]
		if fail is not None:
			failView = ctx.viewEvalFn( fail, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( punctuation_textStyle, ',' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ), failView ] )
		return statementNodeEditor( ctx, node,
					    ctx.span( elements ),
					    PRECEDENCE_STMT,
					    state )


	# Assignment statement
	@ObjectNodeDispatchMethod
	def AssignStmt(self, ctx, state, node, targets, value):
		targetViews = ctx.mapViewEvalFn( targets, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetListOrTargetItem() ) )
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpressionOrYieldExpression() ) )
		targetElements = []
		for t in targetViews:
			targetElements.extend( [ t,  ctx.text( punctuation_textStyle, ' =' ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) ] )
		return statementNodeEditor( ctx, node,
					    ctx.span( targetElements  +  [ valueView ] ),
					    PRECEDENCE_STMT,
					    state )


	# Augmented assignment statement
	@ObjectNodeDispatchMethod
	def AugAssignStmt(self, ctx, state, node, op, target, value):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetItem() ) )
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpressionOrYieldExpression() ) )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ targetView,  ctx.text( punctuation_textStyle, ' ' + op + ' ' ),  valueView ] ),
					    PRECEDENCE_STMT,
					    state )


	# Pass statement
	@ObjectNodeDispatchMethod
	def PassStmt(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
					    capitalisedKeywordText( ctx, passKeyword ),
					    PRECEDENCE_STMT,
					    state )


	# Del statement
	@ObjectNodeDispatchMethod
	def DelStmt(self, ctx, state, node, target):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetListOrTargetItem() ) )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, delKeyword ),  ctx.text( default_textStyle, ' ' ),  targetView ] ),
					    PRECEDENCE_STMT,
					    state )


	# Return statement
	@ObjectNodeDispatchMethod
	def ReturnStmt(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpression() ) )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, returnKeyword ),  ctx.text( default_textStyle, ' ' ),  valueView ] ),
					    PRECEDENCE_STMT,
					    state )


	# Yield statement
	@ObjectNodeDispatchMethod
	def YieldStmt(self, ctx, state, node, value):
		valueView = ctx.viewEvalFn( value, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, yieldKeyword ),  ctx.text( default_textStyle, ' ' ),  valueView ] ),
					    PRECEDENCE_STMT,
					    state )


	# Raise statement
	@ObjectNodeDispatchMethod
	def RaiseStmt(self, ctx, state, node, excType, excValue, traceback):
		xs = [ x   for x in excType, excValue, traceback  if x is not None ]
		xViews = ctx.mapViewEvalFn( xs, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		xElements = []
		if len( xs ) > 0:
			for x in xViews[:-1]:
				xElements.extend( [ x,  ctx.text( punctuation_textStyle, ', ' ) ] )
			xElements.append( xViews[-1] )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, raiseKeyword ),  ctx.text( default_textStyle, ' ' ) ] + xElements ),
					    PRECEDENCE_STMT,
					    state )


	# Break statement
	@ObjectNodeDispatchMethod
	def BreakStmt(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
					    capitalisedKeywordText( ctx, breakKeyword ),
					    PRECEDENCE_STMT,
					    state )


	# Continue statement
	@ObjectNodeDispatchMethod
	def ContinueStmt(self, ctx, state, node):
		return statementNodeEditor( ctx, node,
					    capitalisedKeywordText( ctx, continueKeyword ),
					    PRECEDENCE_STMT,
					    state )


	# Import statement
	@ObjectNodeDispatchMethod
	def RelativeModule(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.text( default_textStyle, name ),
					     PRECEDENCE_IMPORTCONTENT,
					     state )

	@ObjectNodeDispatchMethod
	def ModuleImport(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.text( default_textStyle, name ),
					     PRECEDENCE_IMPORTCONTENT,
					     state )

	@ObjectNodeDispatchMethod
	def ModuleImportAs(self, ctx, state, node, name, asName):
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( default_textStyle, name ),  ctx.text( default_textStyle, ' ' ),  capitalisedKeywordText( ctx, asKeyword ),
							 ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, asName ) ] ),
					     PRECEDENCE_IMPORTCONTENT,
					     state )

	@ObjectNodeDispatchMethod
	def ModuleContentImport(self, ctx, state, node, name):
		return expressionNodeEditor( ctx, node,
					     ctx.text( default_textStyle, name ),
					     PRECEDENCE_IMPORTCONTENT,
					     state )

	@ObjectNodeDispatchMethod
	def ModuleContentImportAs(self, ctx, state, node, name, asName):
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ ctx.text( default_textStyle, name ),  ctx.text( default_textStyle, ' ' ),  capitalisedKeywordText( ctx, asKeyword ),
							 ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, asName ) ] ),
					     PRECEDENCE_IMPORTCONTENT,
					     state )

	@ObjectNodeDispatchMethod
	def ImportStmt(self, ctx, state, node, modules):
		moduleViews = ctx.mapViewEvalFn( modules, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleImport() ) )
		moduleElements = []
		if len( modules ) > 0:
			for mv in moduleViews[:-1]:
				moduleElements.extend( [ mv,  ctx.text( punctuation_textStyle, ',' ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) ] )
			moduleElements.append( moduleViews[-1] )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, importKeyword ), ctx.text( default_textStyle, ' ' ) ]  +  moduleElements ),
					    PRECEDENCE_STMT,
					    state )

	@ObjectNodeDispatchMethod
	def FromImportStmt(self, ctx, state, node, module, imports):
		moduleView = ctx.viewEvalFn( module, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleContentImport() ) )
		importViews = ctx.mapViewEvalFn( imports, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleImport() ) )
		importElements = []
		if len( imports ) > 0:
			for iv in importViews[:-1]:
				importElements.extend( [ iv,  ctx.text( punctuation_textStyle, ',' ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) ] )
			importElements.append( importViews[-1] )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, fromKeyword ), ctx.text( default_textStyle, ' ' ), moduleView, ctx.text( default_textStyle, ' ' ),
							capitalisedKeywordText( ctx, importKeyword ), ctx.text( default_textStyle, ' ' ) ]  +  importElements ),
					    PRECEDENCE_STMT,
					    state )

	@ObjectNodeDispatchMethod
	def FromImportAllStmt(self, ctx, state, node, module):
		moduleView = ctx.viewEvalFn( module, None, python25ViewState( PRECEDENCE_STMT, self._parser.moduleContentImport() ) )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, fromKeyword ), ctx.text( default_textStyle, ' ' ), moduleView, ctx.text( default_textStyle, ' ' ),
							capitalisedKeywordText( ctx, importKeyword ), ctx.text( default_textStyle, ' ' ),  ctx.text( punctuation_textStyle, '*' ) ] ),
					    PRECEDENCE_STMT,
					    state )


	# Global statement
	@ObjectNodeDispatchMethod
	def GlobalVar(self, ctx, state, node, name):
		return statementNodeEditor( ctx, node,
					    ctx.text( default_textStyle, name ),
					    PRECEDENCE_STMT,
					    state )

	@ObjectNodeDispatchMethod
	def GlobalStmt(self, ctx, state, node, vars):
		varViews = ctx.mapViewEvalFn( vars, None, python25ViewState( PRECEDENCE_STMT, self._parser.globalVar() ) )
		varElements = []
		if len( vars ) > 0:
			for vv in varViews[:-1]:
				varElements.extend( [ vv,  ctx.text( punctuation_textStyle, ',' ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) ] )
			varElements.append( varViews[-1] )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, globalKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  varElements ),
					    PRECEDENCE_STMT,
					    state )



	# Exec statement
	@ObjectNodeDispatchMethod
	def ExecStmt(self, ctx, state, node, source, locals, globals):
		sourceView = ctx.viewEvalFn( source, None, python25ViewState( PRECEDENCE_STMT, self._parser.orOp() ) )
		elements = [ sourceView ]
		if locals is not None:
			localsView = ctx.viewEvalFn( locals, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ' ' ),  capitalisedKeywordText( ctx, inKeyword ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ),  localsView ] )
		if globals is not None:
			globalsView = ctx.viewEvalFn( globals, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ',' ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ),  globalsView ] )
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, execKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  elements ),
					    PRECEDENCE_STMT,
					    state )






	# Exec statement
	@ObjectNodeDispatchMethod
	def PrintStmt(self, ctx, state, node, destination, values):
		elements = []
		if destination is not None  or  len( values ) > 0:
			elements.append( ctx.text( default_textStyle, ' ' ) )
		if destination is not None:
			destView = ctx.viewEvalFn( destination, None, python25ViewState( PRECEDENCE_STMT, self._parser.orOp() ) )
			elements.extend( [ ctx.text( punctuation_textStyle, '>>' ), ctx.text( default_textStyle, ' ' ), destView ] )
			if len( values ) > 0:
				elements.extend( [ ctx.text( punctuation_textStyle, ',' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) ] )
		bFirst = True
		valueViews = ctx.mapViewEvalFn( values, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		for v in valueViews:
			if not bFirst:
				elements.extend( [ ctx.text( punctuation_textStyle, ',' ), ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ) ] )
			elements.append( v )
			bFirst = False
		return statementNodeEditor( ctx, node,
					    ctx.span( [ capitalisedKeywordText( ctx, printKeyword ) ]  +  elements ),
					    PRECEDENCE_STMT,
					    state )
	
	
	
	
	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def _ifStmtHeaderElement(self, ctx, state, condition):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return ctx.span( [ capitalisedKeywordText( ctx, ifKeyword ),  ctx.text( default_textStyle, ' ' ),  conditionView,  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def IfStmtHeader(self, ctx, state, node, condition):
		return compoundStatementHeaderEditor( ctx, node,
					    self._ifStmtHeaderElement( ctx, state, condition ),
					    PRECEDENCE_STMT,
					    state )


	# Elif statement
	def _elifStmtHeaderElement(self, ctx, state, condition):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return ctx.span( [ capitalisedKeywordText( ctx, elifKeyword ),  ctx.text( default_textStyle, ' ' ),  conditionView,  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def ElifStmtHeader(self, ctx, state, node, condition):
		return compoundStatementHeaderEditor( ctx, node,
					    self._elifStmtHeaderElement( ctx, state, condition ),
					    PRECEDENCE_STMT,
					    state )



	# Else statement
	def _elseStmtHeaderElement(self, ctx, state):
		return ctx.span( [ capitalisedKeywordText( ctx, elseKeyword ),  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def ElseStmtHeader(self, ctx, state, node):
		return compoundStatementHeaderEditor( ctx, node,
					    self._elseStmtHeaderElement( ctx, state ),
					    PRECEDENCE_STMT,
					    state )


	# While statement
	def _whileStmtHeaderElement(self, ctx, state, condition):
		conditionView = ctx.viewEvalFn( condition, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		return ctx.span( [ capitalisedKeywordText( ctx, whileKeyword ),  ctx.text( default_textStyle, ' ' ),  conditionView,  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def WhileStmtHeader(self, ctx, state, node, condition):
		return compoundStatementHeaderEditor( ctx, node,
					    self._whileStmtHeaderElement( ctx, state, condition ),
					    PRECEDENCE_STMT,
					    state )


	# For statement
	def _forStmtHeaderElement(self, ctx, state, target, source):
		targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.targetListOrTargetItem() ) )
		sourceView = ctx.viewEvalFn( source, None, python25ViewState( PRECEDENCE_STMT, self._parser.tupleOrExpression() ) )
		return ctx.span( [ capitalisedKeywordText( ctx, forKeyword ),  ctx.text( default_textStyle, ' ' ),  targetView,  ctx.text( default_textStyle, ' ' ),
							    capitalisedKeywordText( ctx, inKeyword ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ),
							    sourceView,  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def ForStmtHeader(self, ctx, state, node, target, source):
		return compoundStatementHeaderEditor( ctx, node,
						self._forStmtHeaderElement( ctx, state, target, source ),
						PRECEDENCE_STMT,
						state )



	# Try statement
	def _tryStmtHeaderElement(self, ctx, state):
		return ctx.span( [ capitalisedKeywordText( ctx, tryKeyword ),  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def TryStmtHeader(self, ctx, state, node):
		return compoundStatementHeaderEditor( ctx, node,
					    self._tryStmtHeaderElement( ctx, state ),
					    PRECEDENCE_STMT,
					    state )



	# Except statement
	def _exceptStmtHeaderElement(self, ctx, state, exception, target):
		elements = []
		if exception is not None:
			excView = ctx.viewEvalFn( exception, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ' ' ),  excView ] )
		if target is not None:
			targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ',' ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ),  targetView ] )
		elements.append( ctx.text( punctuation_textStyle, ':' ) )
		return ctx.span( [ capitalisedKeywordText( ctx, exceptKeyword ) ]  +  elements )

	@ObjectNodeDispatchMethod
	def ExceptStmtHeader(self, ctx, state, node, exception, target):
		return compoundStatementHeaderEditor( ctx, node,
					    self._exceptStmtHeaderElement( ctx, state, exception, target ),
					    PRECEDENCE_STMT,
					    state )



	# Finally statement
	def _finallyStmtHeaderElement(self, ctx, state):
		return ctx.span( [ capitalisedKeywordText( ctx, finallyKeyword ),  ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def FinallyStmtHeader(self, ctx, state, node):
		return compoundStatementHeaderEditor( ctx, node,
					    self._finallyStmtHeaderElement( ctx, state ),
					    PRECEDENCE_STMT,
					    state )



	# With statement
	def _withStmtHeaderElement(self, ctx, state, expr, target):
		exprView = ctx.viewEvalFn( expr, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
		elements = [ exprView ]
		if target is not None:
			targetView = ctx.viewEvalFn( target, None, python25ViewState( PRECEDENCE_STMT, self._parser.expression() ) )
			elements.extend( [ ctx.text( default_textStyle, ' ' ),  capitalisedKeywordText( ctx, asKeyword ),  ctx.lineBreak( DEFAULT_LINE_BREAK_PRIORITY, ctx.text( punctuation_textStyle, ' ' ) ),  targetView ] )
		elements.append( ctx.text( punctuation_textStyle, ':' ) )
		return ctx.span( [ capitalisedKeywordText( ctx, withKeyword ),  ctx.text( default_textStyle, ' ' ) ]  +  elements )

	@ObjectNodeDispatchMethod
	def WithStmtHeader(self, ctx, state, node, expr, target):
		return compoundStatementHeaderEditor( ctx, node,
					    self._withStmtHeaderElement( ctx, state, expr, target ),
					    PRECEDENCE_STMT,
					    state )



	# Decorator statement
	def _decoStmtHeaderElement(self, ctx, state, name, args, argsTrailingSeparator):
		if args is not None:
			argViews = ctx.mapViewEvalFn( args, None, python25ViewState( PRECEDENCE_STMT, self._parser.callArg() ) )
			argElements = [ ctx.text( punctuation_textStyle, '(' ) ]
			if len( args ) > 0:
				for a in argViews[:-1]:
					argElements.extend( [ a, ctx.text( punctuation_textStyle, ', ' ) ] )
				argElements.append( argViews[-1] )
				if argsTrailingSeparator is not None:
					argElements.append( ctx.text( punctuation_textStyle, ', ' ) )
			argElements.append( ctx.text( punctuation_textStyle, ')' ) )
		else:
			argElements = []
		return ctx.span( [ ctx.text( punctuation_textStyle, '@' ),  ctx.text( default_textStyle, name ) ]  +  argElements )

	@ObjectNodeDispatchMethod
	def DecoStmtHeader(self, ctx, state, node, name, args, argsTrailingSeparator):
		return compoundStatementHeaderEditor( ctx, node,
					    self._decoStmtHeaderElement( ctx, state, name, args, argsTrailingSeparator ),
					    PRECEDENCE_STMT,
					    state )



	# Def statement
	def _defStmtHeaderElement(self, ctx, state, name, params, paramsTrailingSeparator):
		paramViews = ctx.mapViewEvalFn( params, None, python25ViewState( PRECEDENCE_STMT, self._parser.param() ) )
		paramElements = [ ctx.text( punctuation_textStyle, '(' ) ]
		if len( params ) > 0:
			for p in paramViews[:-1]:
				paramElements.extend( [ p,  ctx.text( punctuation_textStyle, ', ' ) ] )
			paramElements.append( paramViews[-1] )
			if paramsTrailingSeparator is not None:
				paramElements.append( ctx.text( punctuation_textStyle, ', ' ) )

		paramElements.append( ctx.text( punctuation_textStyle, ')' ) )
		return ctx.span( [ capitalisedKeywordText( ctx, defKeyword ),  ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, name ) ]  +  \
							    paramElements  +  [ ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def DefStmtHeader(self, ctx, state, node, name, params, paramsTrailingSeparator):
		editor = compoundStatementHeaderEditor( ctx, node,
					    self._defStmtHeaderElement( ctx, state, name, params, paramsTrailingSeparator ),
					    PRECEDENCE_STMT,
					    state,
					    lambda header: ctx.border( defHeader_border, ContainerStyleSheet.defaultStyleSheet, header ) )
		#return ctx.border( defBackground_border, ContainerStyleSheet.defaultStyleSheet, editor )
		return editor


	# Def statement
	def _classStmtHeaderElement(self, ctx, state, name, bases, basesTrailingSeparator):
		if bases is not None:
			baseViews = ctx.mapViewEvalFn( bases, None, python25ViewState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
			layout = tuple_listViewLayout   if basesTrailingSeparator is None   else tuple_listViewLayoutSep
			baseElements = [ ctx.text( punctuation_textStyle, '(' ),  ctx.listView( layout, None, None, _CommaFactory( ctx ), baseViews ),  ctx.text( punctuation_textStyle, ')' ) ]
		else:
			baseElements = []
		return ctx.span( [ capitalisedKeywordText( ctx, classKeyword ),  ctx.text( default_textStyle, ' ' ),  ctx.text( default_textStyle, name ) ]  +  \
							    baseElements  +  [ ctx.text( punctuation_textStyle, ':' ) ] )

	@ObjectNodeDispatchMethod
	def ClassStmtHeader(self, ctx, state, node, name, bases, basesTrailingSeparator):
		editor = compoundStatementHeaderEditor( ctx, node,
						  self._classStmtHeaderElement( ctx, state, name, bases, basesTrailingSeparator ),
						  PRECEDENCE_STMT,
						  state,
						  lambda header: ctx.border( classHeader_border, ContainerStyleSheet.defaultStyleSheet, header ) )
		#return ctx.border( classBackground_border, ContainerStyleSheet.defaultStyleSheet, editor )
		return editor


	

	#
	#
	# STRUCTURE STATEMENTS
	#
	#

	# Indented block
	@ObjectNodeDispatchMethod
	def IndentedBlock(self, ctx, state, node, suite):
		suiteElement = ctx.indent( 30.0, indentedSuiteView( ctx, suite, self._parser.singleLineStatement() ) )
		suiteElement.setStructuralValueObject( node )
		suiteElement = ctx.linearRepresentationListener( suiteElement, SuiteLinearRepresentationListener( self._parser.compoundSuite(), suite ) )
		return ctx.border( indentedBlock_border, ContainerStyleSheet.defaultStyleSheet, suiteElement )





	#
	#
	# COMPOUND STATEMENTS
	#
	#

	# If statement
	@ObjectNodeDispatchMethod
	def IfStmt(self, ctx, state, node, condition, suite, elifBlocks, elseSuite):
		compoundBlocks = [ ( Nodes.IfStmtHeader( condition=condition ), self._ifStmtHeaderElement( ctx, state, condition ), suite ) ]
		for b in elifBlocks:
			if not b.isInstanceOf( Nodes.ElifBlock ):
				raise TypeError, 'IfStmt elifBlocks should only contain ElifBlock instances'
			compoundBlocks.append( ( Nodes.ElifStmtHeader( condition=b['condition'] ), self._elifStmtHeaderElement( ctx, state, b['condition'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Nodes.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )



	# While statement
	@ObjectNodeDispatchMethod
	def WhileStmt(self, ctx, state, node, condition, suite, elseSuite):
		compoundBlocks = [ ( Nodes.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( ctx, state, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Nodes.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )

	

	# For statement
	@ObjectNodeDispatchMethod
	def ForStmt(self, ctx, state, node, target, source, suite, elseSuite):
		compoundBlocks = [ ( Nodes.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( ctx, state, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )



	# Try statement
	@ObjectNodeDispatchMethod
	def TryStmt(self, ctx, state, node, suite, exceptBlocks, elseSuite, finallySuite):
		compoundBlocks = [ ( Nodes.TryStmtHeader(), self._tryStmtHeaderElement( ctx, state ), suite ) ]
		for b in exceptBlocks:
			if not b.isInstanceOf( Nodes.ExceptBlock ):
				raise TypeError, 'TryStmt elifBlocks should only contain ExceptBlock instances'
			compoundBlocks.append( ( Nodes.ExceptStmtHeader( exception=b['exception'], target=b['target'] ), self._exceptStmtHeaderElement( ctx, state, b['exception'], b['target'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Nodes.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		if finallySuite is not None:
			compoundBlocks.append( ( Nodes.FinallyStmtHeader(), self._finallyStmtHeaderElement( ctx, state ),  finallySuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )




	# With statement
	@ObjectNodeDispatchMethod
	def WithStmt(self, ctx, state, node, expr, target, suite):
		compoundBlocks = [ ( Nodes.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( ctx, state, expr, target ), suite ) ]
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )



	# Def statement
	@ObjectNodeDispatchMethod
	def DefStmt(self, ctx, state, node, decorators, name, params, paramsTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Nodes.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Nodes.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
						 self._decoStmtHeaderElement( ctx, state, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )
			
		compoundBlocks.append( ( Nodes.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ),
					 self._defStmtHeaderElement( ctx, state, name, params, paramsTrailingSeparator ), suite, lambda header: ctx.border( defHeader_border, ContainerStyleSheet.defaultStyleSheet, header ) ) )
		editor = compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )
		return ctx.border( defBackground_border, ContainerStyleSheet.defaultStyleSheet, editor )


	# Class statement
	@ObjectNodeDispatchMethod
	def ClassStmt(self, ctx, state, node, name, bases, basesTrailingSeparator, suite):
		compoundBlocks = [ ( Nodes.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ),
				     self._classStmtHeaderElement( ctx, state, name, bases, basesTrailingSeparator ), suite, lambda header: ctx.border( classHeader_border, ContainerStyleSheet.defaultStyleSheet, header ) ) ]
		editor = compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )
		return ctx.border( classBackground_border, ContainerStyleSheet.defaultStyleSheet, editor )



	# Comment statement
	@ObjectNodeDispatchMethod
	def CommentStmt(self, ctx, state, node, comment):
		return statementNodeEditor( ctx, node,
					    ctx.text( comment_textStyle, '#' + comment ),
					    PRECEDENCE_STMT,
					    state )




def viewPython25LocationAsElement(document, docRootNode, locationPrefix, location, commandHistory, app):
	viewContext = GSymViewContext( docRootNode, Python25View(), commandHistory )
	editHandler = Python25EditHandler( viewContext )
	viewContext.getFrame().setEditHandler( editHandler )
	return viewContext.getFrame()


def getDocNodeForPython25Location(document, docRootNode, locationPrefix, location, app):
	return docRootNode
