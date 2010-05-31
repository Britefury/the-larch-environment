##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


#
#
#  BUG
#
# enter:
#
# a=x+x**(x/q)**
#
# causes crash
#
#
#
#


from java.awt.event import KeyEvent

from BritefuryJ.Parser import ParserExpression
from BritefuryJ.Parser.ItemStream import ItemStreamBuilder

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction



from GSymCore.Languages.Python25 import Schema

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.NodeEditor import *
from GSymCore.Languages.Python25.PythonEditor.SelectionEditor import *
from GSymCore.Languages.Python25.PythonEditor.Keywords import *
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditorStyleSheet import PythonEditorStyleSheet




DEFAULT_LINE_BREAK_PRIORITY = 100



_statementIndentationInteractor = StatementIndentationInteractor()




def _nodeRequiresParens(node):
	return node.isInstanceOf( Schema.Expr )  or  node.isInstanceOf( Schema.Target )

def computeBinOpViewPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1




def unparsedNodeEditor(grammar, styleSheet, node, precedence, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_DISPLAYCONTENTS:
		if _nodeRequiresParens( node ):
			contents = styleSheet.applyParens( contents, precedence, getNumParens( node ) )
		return contents
	elif mode == PythonEditorStyleSheet.MODE_EDITEXPRESSION:
		outerPrecedence = styleSheet.getOuterPrecedence()
		contents.addTreeEventListener( TextEditEvent, ParsedExpressionTreeEventListener.newListener( grammar.expression(), outerPrecedence ) )
		return contents
	elif mode == PythonEditorStyleSheet.MODE_EDITSTATEMENT:
		statementLine = styleSheet.statementLine( contents )
		
		builder = ItemStreamBuilder()
		for x in node['value']:
			if isinstance( x, str )  or  isinstance( x, unicode ):
				builder.appendTextValue( x )
			elif isinstance( x, DMObjectInterface ):
				builder.appendStructuralValue( x )
			else:
				raise TypeError, 'UNPARSED node should only contain strings or objects, not %s'  %  ( type( x ), )
		statementLine.setStructuralValueStream( builder.stream() )
		statementLine.addTreeEventListener( TextEditEvent, StatementTreeEventListener.newListener( grammar.singleLineStatement() ) )
		return statementLine
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def expressionNodeEditor(grammar, styleSheet, node, precedence, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_DISPLAYCONTENTS:
		if _nodeRequiresParens( node ):
			contents = styleSheet.applyParens( contents, precedence, getNumParens( node ) )
		return contents
	elif mode == PythonEditorStyleSheet.MODE_EDITEXPRESSION:
		outerPrecedence = styleSheet.getOuterPrecedence()
		
		if _nodeRequiresParens( node ):
			contents = styleSheet.applyParens( contents, precedence, getNumParens( node ) )
		contents.addTreeEventListener( TextEditEvent, ParsedExpressionTreeEventListener.newListener( grammar.expression(), outerPrecedence ) )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def structuralExpressionNodeEditor(styleSheet, node, precedence, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_DISPLAYCONTENTS  or  mode == PythonEditorStyleSheet.MODE_EDITEXPRESSION:
		contents = styleSheet.applyParens( contents, _nodeRequiresParens( node ), precedence, getNumParens( node ) )
		contents.addTreeEventListener( TextEditEvent, StructuralExpressionTreeEventListener.newListener() )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def statementNodeEditor(grammar, styleSheet, node, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_EDITSTATEMENT:
		statementLine = styleSheet.statementLine( contents )
		
		if node.isInstanceOf( Schema.UNPARSED ):
			builder = ItemStreamBuilder()
			for x in node['value']:
				if isinstance( x, str )  or  isinstance( x, unicode ):
					builder.appendTextValue( x )
				elif isinstance( x, DMObjectInterface ):
					builder.appendStructuralValue( x )
				else:
					raise TypeError, 'UNPARSED node should only contain strings or objects, not %s'  %  ( type( x ), )
			statementLine.setStructuralValueStream( builder.stream() )
		else:
			statementLine.setStructuralValueObject( node )
		statementLine.addTreeEventListener( TextEditEvent, StatementTreeEventListener.newListener( grammar.singleLineStatement() ) )
		statementLine.addTreeEventListener( PythonSelectionTreeEvent, StatementTreeEventListener.newListener( grammar.singleLineStatement() ) )
		statementLine.addInteractor( _statementIndentationInteractor )
		return statementLine
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def compoundStatementHeaderEditor(grammar, styleSheet, node, headerContents, headerContainerFn=None):
	headerStatementLine = styleSheet.statementLine( headerContents )
	
	headerStatementLine.setStructuralValueObject( node )
	headerStatementLine.addTreeEventListener( TextEditEvent, StatementTreeEventListener.newListener( grammar.singleLineStatement() ) )
	headerStatementLine.addTreeEventListener( PythonSelectionTreeEvent, StatementTreeEventListener.newListener( grammar.singleLineStatement() ) )
	headerStatementLine.addInteractor( _statementIndentationInteractor )
	if headerContainerFn is not None:
		headerStatementLine = headerContainerFn( headerStatementLine )
	return headerStatementLine


def compoundStatementEditor(ctx, grammar, styleSheet, node, precedence, compoundBlocks, state):
	statementContents = []
	
	statementParser = grammar.singleLineStatement()
	suiteParser = grammar.compoundSuite()
	
	for i, block in enumerate( compoundBlocks ):
		if len( block ) == 3:
			headerNode, headerContents, suite = block
			headerContainerFn = None
		elif len( block ) == 4:
			headerNode, headerContents, suite, headerContainerFn = block
		else:
			raise TypeError, 'Compound block should be of the form (headerNode, headerContents, suite)  or  (headerNode, headerContents, suite, headerContainerFn)'
		
		headerStatementLine = styleSheet.statementLine( headerContents )
		headerStatementLine.setStructuralValueObject( headerNode )
		headerStatementLine.addTreeEventListener( TextEditEvent, CompoundHeaderTreeEventListener.newListener( statementParser ) )
		headerStatementLine.addTreeEventListener( PythonSelectionTreeEvent, CompoundHeaderTreeEventListener.newListener( statementParser ) )
		headerStatementLine.addInteractor( _statementIndentationInteractor )
		
		if headerContainerFn is not None:
			headerStatementLine = headerContainerFn( headerStatementLine )



		if suite is not None:
			indent = styleSheet.indentElement()
			indent.setStructuralValueObject( Schema.Indent() )
			
			lineViews = ctx.mapPresentFragment( suite, styleSheet.withPythonState( PRECEDENCE_NONE, PythonEditorStyleSheet.MODE_EDITSTATEMENT ) )
			
			dedent = styleSheet.dedentElement()
			dedent.setStructuralValueObject( Schema.Dedent() )
			
			suiteElement = styleSheet.indentedBlock( indent, lineViews, dedent )
			suiteElement.setStructuralValueObject( Schema.IndentedBlock( suite=suite ) )
			suiteListener = SuiteTreeEventListener( suiteParser, suite )
			suiteElement.addTreeEventListener( TextEditEvent, suiteListener )
			suiteElement.addTreeEventListener( PythonSelectionTreeEvent, suiteListener )
			suiteElement.addTreeEventListener( PythonIndentationTreeEvent, suiteListener )
			
			statementContents.extend( [ headerStatementLine.alignHExpand(), suiteElement.alignHExpand() ] )
		else:
			statementContents.append( headerStatementLine.alignHExpand() )
			
	return styleSheet.compoundStmt( statementContents )



def spanPrefixOpView(ctx, grammar, styleSheet, node, x, op, precedence):
	xView = ctx.presentFragment( x, styleSheet.withPythonState( precedence, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	view = styleSheet.spanPrefixOp( xView, op )
	return expressionNodeEditor( grammar, styleSheet, node, precedence,
	                             view )


def spanBinOpView(ctx, grammar, styleSheet, node, x, y, op, precedence, bRightAssociative):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( precedence, bRightAssociative )
	xView = ctx.presentFragment( x, styleSheet.withPythonState( xPrec, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	yView = ctx.presentFragment( y, styleSheet.withPythonState( yPrec, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	view = styleSheet.spanBinOp( xView, yView, op )
	return expressionNodeEditor( grammar, styleSheet, node, precedence,
	                             view )


def spanCmpOpView(ctx, grammar, styleSheet, node, op, y, precedence):
	yView = ctx.presentFragment( y, styleSheet.withPythonState( precedence, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	view = styleSheet.spanCmpOp( op, yView )
	return expressionNodeEditor( grammar, styleSheet, node, precedence,
	                             view )
	
	
	
	
	
	
def printElem(elem, level):
	print '  ' * level, elem, elem.getTextRepresentation()
	if isinstance( elem, BranchElement ):
		for x in elem.getChildren():
			printElem( x, level + 1 )



class Python25View (GSymViewObjectNodeDispatch):
	def __init__(self, parser):
		self._parser = parser


	# MISC
	@ObjectNodeDispatchMethod( Schema.PythonModule )
	def PythonModule(self, ctx, styleSheet, state, node, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ styleSheet.statementLine( styleSheet.blankLine() ) ]
		else:
			lineViews = ctx.mapPresentFragment( suite, styleSheet.withPythonState( PRECEDENCE_NONE, PythonEditorStyleSheet.MODE_EDITSTATEMENT ) )
		suiteElement = styleSheet.suiteView( lineViews )
		suiteElement.setStructuralValueObject( suite )
		suiteListener = SuiteTreeEventListener( self._parser.suite(), suite )
		suiteElement.addTreeEventListener( TextEditEvent, suiteListener )
		suiteElement.addTreeEventListener( PythonIndentationTreeEvent, suiteListener )
		suiteElement.addTreeEventListener( PythonSelectionTreeEvent, suiteListener )
		return suiteElement



	@ObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, ctx, styleSheet, state, node):
		return statementNodeEditor( self._parser, styleSheet, node,
		                            styleSheet.blankLine() )


	@ObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, ctx, styleSheet, state, node, value):
		def _viewItem(x):
			if x is node:
				raise ValueError, 'Python25View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				return styleSheet.unparseableText( x )
			elif isinstance( x, DMObjectInterface ):
				return ctx.presentFragment( x, styleSheet.withPythonState( PRECEDENCE_CONTAINER_UNPARSED ) )
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsedNodeEditor( self._parser, styleSheet, node, PRECEDENCE_NONE,
		                             styleSheet.unparsedElements( views ) )





	# Comment statement
	@ObjectNodeDispatchMethod( Schema.CommentStmt )
	def CommentStmt(self, ctx, styleSheet, state, node, comment):
		view = styleSheet.commentStmt( comment )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	
	
	
	# String literal
	__strLit_fmtTable = { 'ascii' : None,  'unicode' : 'u',  'ascii-regex' : 'r',  'unicode-regex' : 'ur' }
	
	@ObjectNodeDispatchMethod( Schema.StringLiteral )
	def StringLiteral(self, ctx, styleSheet, state, node, format, quotation, value):
		fmt = self.__strLit_fmtTable[format]
		
		quote = "'"   if quotation == 'single'   else   '"'
		
		view = styleSheet.stringLiteral( fmt, quote, value )

		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_LITERALVALUE,
		                             view )

	# Integer literal
	@ObjectNodeDispatchMethod( Schema.IntLiteral )
	def IntLiteral(self, ctx, styleSheet, state, node, format, numType, value):
		boxContents = []

		if numType == 'int':
			if format == 'decimal':
				valueString = '%d'  %  int( value )
			elif format == 'hex':
				valueString = '%x'  %  int( value, 16 )
			fmt = None
		elif numType == 'long':
			if format == 'decimal':
				valueString = '%d'  %  long( value )
			elif format == 'hex':
				valueString = '%x'  %  long( value, 16 )
			fmt = 'L'
		
		view = styleSheet.intLiteral( fmt, valueString )
		
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_LITERALVALUE,
		                             view )



	# Float literal
	@ObjectNodeDispatchMethod( Schema.FloatLiteral )
	def FloatLiteral(self, ctx, styleSheet, state, node, value):
		return expressionNodeEditor( self._parser, styleSheet, node,
					     PRECEDENCE_LITERALVALUE,
					     styleSheet.floatLiteral( value ) )



	# Imaginary literal
	@ObjectNodeDispatchMethod( Schema.ImaginaryLiteral )
	def ImaginaryLiteral(self, ctx, styleSheet, state, node, value):
		return expressionNodeEditor( self._parser, styleSheet, node,
					     PRECEDENCE_LITERALVALUE,
		                             styleSheet.imaginaryLiteral( value ) )



	# Targets
	@ObjectNodeDispatchMethod( Schema.SingleTarget )
	def SingleTarget(self, ctx, styleSheet, state, node, name):
		return expressionNodeEditor( self._parser, styleSheet, node,
					     PRECEDENCE_SINGLETARGET,
					     styleSheet.singleTarget( name ) )


	@ObjectNodeDispatchMethod( Schema.TupleTarget )
	def TupleTarget(self, ctx, styleSheet, state, node, targets, trailingSeparator):
		elementViews = ctx.mapPresentFragment( targets, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.tupleTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_TUPLE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.ListTarget )
	def ListTarget(self, ctx, styleSheet, state, node, targets, trailingSeparator):
		elementViews = ctx.mapPresentFragment( targets, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.listTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )




	# Variable reference
	@ObjectNodeDispatchMethod( Schema.Load )
	def Load(self, ctx, styleSheet, state, node, name):
		return expressionNodeEditor( self._parser, styleSheet, node,
					     PRECEDENCE_LOAD,
					     styleSheet.load( name ) )



	# Tuple literal
	@ObjectNodeDispatchMethod( Schema.TupleLiteral )
	def TupleLiteral(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapPresentFragment( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.tupleLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_TUPLE,
		                             view )



	# List literal
	@ObjectNodeDispatchMethod( Schema.ListLiteral )
	def ListLiteral(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapPresentFragment( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.listLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )



	# List comprehension / generator expression
	@ObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, ctx, styleSheet, state, node, target, source):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_COMPREHENSIONFOR) )
		sourceView = ctx.presentFragment( source, styleSheet.withPythonState( PRECEDENCE_CONTAINER_COMPREHENSIONFOR ) )
		view = styleSheet.comprehensionFor( targetView, sourceView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, ctx, styleSheet, state, node, condition):
		conditionView = ctx.presentFragment( condition, styleSheet.withPythonState( PRECEDENCE_CONTAINER_COMPREHENSIONIF ) )
		view = styleSheet.comprehensionIf( conditionView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.ListComp )
	def ListComp(self, ctx, styleSheet, state, node, resultExpr, comprehensionItems):
		exprView = ctx.presentFragment( resultExpr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		itemViews = ctx.mapPresentFragment( comprehensionItems, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.listComp( exprView, itemViews )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )


	@ObjectNodeDispatchMethod( Schema.GeneratorExpr )
	def GeneratorExpr(self, ctx, styleSheet, state, node, resultExpr, comprehensionItems):
		exprView = ctx.presentFragment( resultExpr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		itemViews = ctx.mapPresentFragment( comprehensionItems, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.genExpr( exprView, itemViews )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )




	# Dictionary literal
	@ObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, ctx, styleSheet, state, node, key, value):
		keyView = ctx.presentFragment( key, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.dictKeyValuePair( keyView, valueView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.DictLiteral )
	def DictLiteral(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapPresentFragment( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.dictLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_DICTDISPLAY,
		                             view )


	# Yield expression
	@ObjectNodeDispatchMethod( Schema.YieldExpr )
	def YieldExpr(self, ctx, styleSheet, state, node, value):
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_YIELDEXPR ) )
		view = styleSheet.yieldExpr( valueView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_YIELDEXPR,
		                             view )



	# Attribute ref
	@ObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, ctx, styleSheet, state, node, target, name):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET ) )
		view = styleSheet.attributeRef( targetView, name )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_ATTR,
		                             view )



	# Subscript
	@ObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, ctx, styleSheet, state, node, lower, upper):
		lowerView = ctx.presentFragment( lower, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if lower is not None   else None
		upperView = ctx.presentFragment( upper, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if upper is not None   else None
		view = styleSheet.subscriptSlice( lowerView, upperView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, ctx, styleSheet, state, node, lower, upper, stride):
		lowerView = ctx.presentFragment( lower, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if lower is not None   else None
		upperView = ctx.presentFragment( upper, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if upper is not None   else None
		strideView = ctx.presentFragment( stride, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if stride is not None   else None
		view = styleSheet.subscriptLongSlice( lowerView, upperView, strideView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, ctx, styleSheet, state, node):
		view = styleSheet.subscriptEllipsis()
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.SubscriptTuple )
	def SubscriptTuple(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapPresentFragment( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )
		view = styleSheet.subscriptTuple( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_TUPLE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, ctx, styleSheet, state, node, target, index):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTTARGET ) )
		indexView = ctx.presentFragment( index, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )
		view = styleSheet.subscript( targetView, indexView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_SUBSCRIPT,
		                             view )




	# Call
	@ObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, ctx, styleSheet, state, node, name, value):
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG ) )
		view = styleSheet.callKWArg( name, valueView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, ctx, styleSheet, state, node, value):
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG ) )
		view = styleSheet.callArgList( valueView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, ctx, styleSheet, state, node, value):
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG ) )
		view = styleSheet.callKWArgList( valueView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.Call )
	def Call(self, ctx, styleSheet, state, node, target, args, argsTrailingSeparator):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLTARGET ) )
		argViews = ctx.mapPresentFragment( args, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG ) )
		view = styleSheet.call( targetView, argViews, argsTrailingSeparator is not None )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_CALL,
		                             view )





	# Operators
	@ObjectNodeDispatchMethod( Schema.Pow )
	def Pow(self, ctx, styleSheet, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = ctx.presentFragment( x, styleSheet.withPythonState( xPrec ) )
		yView = ctx.presentFragment( y, styleSheet.powExponentStyle().withPythonState( yPrec, PythonEditorStyleSheet.MODE_EDITEXPRESSION ) )
		view = styleSheet.pow( xView, yView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_POW,
		                             view )


	@ObjectNodeDispatchMethod( Schema.Invert )
	def Invert(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, self._parser, styleSheet, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS )

	@ObjectNodeDispatchMethod( Schema.Negate )
	def Negate(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, self._parser, styleSheet, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS )

	@ObjectNodeDispatchMethod( Schema.Pos )
	def Pos(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, self._parser, styleSheet, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS )


	@ObjectNodeDispatchMethod( Schema.Mul )
	def Mul(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '*', PRECEDENCE_MULDIVMOD, False )

	@ObjectNodeDispatchMethod( Schema.Div )
	def Div(self, ctx, styleSheet, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = ctx.presentFragment( x, styleSheet.divNumeratorStyle().withPythonState( xPrec, PythonEditorStyleSheet.MODE_EDITEXPRESSION ) )
		yView = ctx.presentFragment( y, styleSheet.divDenominatorStyle().withPythonState( yPrec, PythonEditorStyleSheet.MODE_EDITEXPRESSION ) )
		view = styleSheet.div( xView, yView, '/' )
		view.setStructuralValueObject( node )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_MULDIVMOD,
		                             view )

	@ObjectNodeDispatchMethod( Schema.Mod )
	def Mod(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '%', PRECEDENCE_MULDIVMOD, False )


	@ObjectNodeDispatchMethod( Schema.Add )
	def Add(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '+', PRECEDENCE_ADDSUB, False )

	@ObjectNodeDispatchMethod( Schema.Sub )
	def Sub(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '-', PRECEDENCE_ADDSUB, False )


	@ObjectNodeDispatchMethod( Schema.LShift )
	def LShift(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '<<', PRECEDENCE_SHIFT, False )

	@ObjectNodeDispatchMethod( Schema.RShift )
	def RShift(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '>>', PRECEDENCE_SHIFT, False )


	@ObjectNodeDispatchMethod( Schema.BitAnd )
	def BitAnd(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '&', PRECEDENCE_BITAND, False )

	@ObjectNodeDispatchMethod( Schema.BitXor )
	def BitXor(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '^', PRECEDENCE_BITXOR, False )

	@ObjectNodeDispatchMethod( Schema.BitOr )
	def BitOr(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, '|', PRECEDENCE_BITOR, False )


	@ObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, ctx, styleSheet, state, node, x, ops):
		xView = ctx.presentFragment( x, styleSheet.withPythonState( PRECEDENCE_CMP ) )
		opViews = ctx.mapPresentFragment( ops, styleSheet.withPythonState( PRECEDENCE_CMP ) )
		view = styleSheet.compare( xView, opViews )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_CMP,
		                             view )

	@ObjectNodeDispatchMethod( Schema.CmpOpLte )
	def CmpOpLte(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, '<=', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpLt )
	def CmpOpLt(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, '<', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpGte )
	def CmpOpGte(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, '>=', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpGt )
	def CmpOpGt(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, '>', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpEq )
	def CmpOpEq(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, '==', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpNeq )
	def CmpOpNeq(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, '!=', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, 'is not', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpIs )
	def CmpOpIs(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, 'is', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, 'not in', y, PRECEDENCE_CMP )

	@ObjectNodeDispatchMethod( Schema.CmpOpIn )
	def CmpOpIn(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, self._parser, styleSheet, node, 'in', y, PRECEDENCE_CMP )



	@ObjectNodeDispatchMethod( Schema.NotTest )
	def NotTest(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, self._parser, styleSheet, node, x, 'not ', PRECEDENCE_NOT )

	@ObjectNodeDispatchMethod( Schema.AndTest )
	def AndTest(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, 'and', PRECEDENCE_AND, False )

	@ObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, self._parser, styleSheet, node, x, y, 'or', PRECEDENCE_OR, False )





	# Parameters
	@ObjectNodeDispatchMethod( Schema.SimpleParam )
	def SimpleParam(self, ctx, styleSheet, state, node, name):
		view = styleSheet.simpleParam( name )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.DefaultValueParam )
	def DefaultValueParam(self, ctx, styleSheet, state, node, name, defaultValue):
		valueView = ctx.presentFragment( defaultValue, styleSheet.withPythonState( PRECEDENCE_NONE ) )
		view = styleSheet.defaultValueParam( name, valueView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.ParamList )
	def ParamList(self, ctx, styleSheet, state, node, name):
		view = styleSheet.paramList( name )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.KWParamList )
	def KWParamList(self, ctx, styleSheet, state, node, name):
		view = styleSheet.kwParamList( name )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )



	# Lambda expression
	@ObjectNodeDispatchMethod( Schema.LambdaExpr )
	def LambdaExpr(self, ctx, styleSheet, state, node, params, paramsTrailingSeparator, expr):
		exprView = ctx.presentFragment( expr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_LAMBDAEXPR ) )
		paramViews = ctx.mapPresentFragment( params, styleSheet.withPythonState( PRECEDENCE_NONE ) )
		
		view = styleSheet.lambdaExpr( paramViews, paramsTrailingSeparator is not None, exprView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_LAMBDAEXPR,
		                             view )



	# Conditional expression
	@ObjectNodeDispatchMethod( Schema.ConditionalExpr )
	def ConditionalExpr(self, ctx, styleSheet, state, node, condition, expr, elseExpr):
		conditionView = ctx.presentFragment( condition, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		exprView = ctx.presentFragment( expr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		elseExprView = ctx.presentFragment( elseExpr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		view = styleSheet.conditionalExpr( conditionView, exprView, elseExprView )
		return expressionNodeEditor( self._parser, styleSheet, node,
			                     PRECEDENCE_CONDITIONAL,
		                             view )




	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Expression statement
	@ObjectNodeDispatchMethod( Schema.ExprStmt )
	def ExprStmt(self, ctx, styleSheet, state, node, expr):
		exprView = ctx.presentFragment( expr, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.exprStmt( exprView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )



	# Assert statement
	@ObjectNodeDispatchMethod( Schema.AssertStmt )
	def AssertStmt(self, ctx, styleSheet, state, node, condition, fail):
		conditionView = ctx.presentFragment( condition, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		failView = ctx.presentFragment( fail, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if fail is not None   else None
		view = styleSheet.assertStmt( conditionView, failView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Assignment statement
	@ObjectNodeDispatchMethod( Schema.AssignStmt )
	def AssignStmt(self, ctx, styleSheet, state, node, targets, value):
		targetViews = ctx.mapPresentFragment( targets, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.assignStmt( targetViews, valueView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Augmented assignment statement
	@ObjectNodeDispatchMethod( Schema.AugAssignStmt )
	def AugAssignStmt(self, ctx, styleSheet, state, node, op, target, value):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.augAssignStmt( op, targetView, valueView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Pass statement
	@ObjectNodeDispatchMethod( Schema.PassStmt )
	def PassStmt(self, ctx, styleSheet, state, node):
		view = styleSheet.passStmt()
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Del statement
	@ObjectNodeDispatchMethod( Schema.DelStmt )
	def DelStmt(self, ctx, styleSheet, state, node, target):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.delStmt( targetView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Return statement
	@ObjectNodeDispatchMethod( Schema.ReturnStmt )
	def ReturnStmt(self, ctx, styleSheet, state, node, value):
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.returnStmt( valueView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Yield statement
	@ObjectNodeDispatchMethod( Schema.YieldStmt )
	def YieldStmt(self, ctx, styleSheet, state, node, value):
		valueView = ctx.presentFragment( value, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.yieldStmt( valueView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Raise statement
	@ObjectNodeDispatchMethod( Schema.RaiseStmt )
	def RaiseStmt(self, ctx, styleSheet, state, node, excType, excValue, traceback):
		excTypeView = ctx.presentFragment( excType, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if excType is not None   else None
		excValueView = ctx.presentFragment( excValue, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if excValue is not None   else None
		tracebackView = ctx.presentFragment( traceback, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if traceback is not None   else None
		view = styleSheet.raiseStmt( excTypeView, excValueView, tracebackView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Break statement
	@ObjectNodeDispatchMethod( Schema.BreakStmt )
	def BreakStmt(self, ctx, styleSheet, state, node):
		view = styleSheet.breakStmt()
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Continue statement
	@ObjectNodeDispatchMethod( Schema.ContinueStmt )
	def ContinueStmt(self, ctx, styleSheet, state, node):
		view = styleSheet.continueStmt()
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Import statement
	@ObjectNodeDispatchMethod( Schema.RelativeModule )
	def RelativeModule(self, ctx, styleSheet, state, node, name):
		view = styleSheet.relativeModule( name )
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Schema.ModuleImport )
	def ModuleImport(self, ctx, styleSheet, state, node, name):
		view = styleSheet.moduleImport( name )
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Schema.ModuleImportAs )
	def ModuleImportAs(self, ctx, styleSheet, state, node, name, asName):
		view = styleSheet.moduleImportAs( name, asName )
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Schema.ModuleContentImport )
	def ModuleContentImport(self, ctx, styleSheet, state, node, name):
		view = styleSheet.moduleContentImport( name )
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Schema.ModuleContentImportAs )
	def ModuleContentImportAs(self, ctx, styleSheet, state, node, name, asName):
		view = styleSheet.moduleContentImportAs( name, asName )
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Schema.ImportStmt )
	def ImportStmt(self, ctx, styleSheet, state, node, modules):
		moduleViews = ctx.mapPresentFragment( modules, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.importStmt( moduleViews )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )

	@ObjectNodeDispatchMethod( Schema.FromImportStmt )
	def FromImportStmt(self, ctx, styleSheet, state, node, module, imports):
		moduleView = ctx.presentFragment( module, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		importViews = ctx.mapPresentFragment( imports, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.fromImportStmt( moduleView, importViews )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )

	@ObjectNodeDispatchMethod( Schema.FromImportAllStmt )
	def FromImportAllStmt(self, ctx, styleSheet, state, node, module):
		moduleView = ctx.presentFragment( module, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.fromImportAllStmt( moduleView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )


	# Global statement
	@ObjectNodeDispatchMethod( Schema.GlobalVar )
	def GlobalVar(self, ctx, styleSheet, state, node, name):
		view = styleSheet.globalVar( name )
		return expressionNodeEditor( self._parser, styleSheet, node, PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Schema.GlobalStmt )
	def GlobalStmt(self, ctx, styleSheet, state, node, vars):
		varViews = ctx.mapPresentFragment( vars, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.globalStmt( varViews )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )



	# Exec statement
	@ObjectNodeDispatchMethod( Schema.ExecStmt )
	def ExecStmt(self, ctx, styleSheet, state, node, source, globals, locals):
		sourceView = ctx.presentFragment( source, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		globalsView = ctx.presentFragment( globals, styleSheet.withPythonState( PRECEDENCE_STMT ) )    if globals is not None   else None
		localsView = ctx.presentFragment( locals, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if locals is not None   else None
		view = styleSheet.execStmt( sourceView, globalsView, localsView )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )






	# Exec statement
	@ObjectNodeDispatchMethod( Schema.PrintStmt )
	def PrintStmt(self, ctx, styleSheet, state, node, destination, values):
		destView = ctx.presentFragment( destination, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if destination is not None   else None
		valueViews = ctx.mapPresentFragment( values, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		view = styleSheet.printStmt( destView, valueViews )
		return statementNodeEditor( self._parser, styleSheet, node,
		                            view )
	
	
	
	
	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def _ifStmtHeaderElement(self, ctx, styleSheet, state, condition):
		conditionView = ctx.presentFragment( condition, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		return styleSheet.ifStmtHeader( conditionView )

	@ObjectNodeDispatchMethod( Schema.IfStmtHeader )
	def IfStmtHeader(self, ctx, styleSheet, state, node, condition):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._ifStmtHeaderElement( ctx, styleSheet, state, condition ) )


	# Elif statement
	def _elifStmtHeaderElement(self, ctx, styleSheet, state, condition):
		conditionView = ctx.presentFragment( condition, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		return styleSheet.elifStmtHeader( conditionView )

	@ObjectNodeDispatchMethod( Schema.ElifStmtHeader )
	def ElifStmtHeader(self, ctx, styleSheet, state, node, condition):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._elifStmtHeaderElement( ctx, styleSheet, state, condition ) )



	# Else statement
	def _elseStmtHeaderElement(self, ctx, styleSheet, state):
		return styleSheet.elseStmtHeader()

	@ObjectNodeDispatchMethod( Schema.ElseStmtHeader )
	def ElseStmtHeader(self, ctx, styleSheet, state, node):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._elseStmtHeaderElement( ctx, styleSheet, state ) )


	# While statement
	def _whileStmtHeaderElement(self, ctx, styleSheet, state, condition):
		conditionView = ctx.presentFragment( condition, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		return styleSheet.whileStmtHeader( conditionView )

	@ObjectNodeDispatchMethod( Schema.WhileStmtHeader )
	def WhileStmtHeader(self, ctx, styleSheet, state, node, condition):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._whileStmtHeaderElement( ctx, styleSheet, state, condition ) )


	# For statement
	def _forStmtHeaderElement(self, ctx, styleSheet, state, target, source):
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		sourceView = ctx.presentFragment( source, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		return styleSheet.forStmtHeader( targetView, sourceView )

	@ObjectNodeDispatchMethod( Schema.ForStmtHeader )
	def ForStmtHeader(self, ctx, styleSheet, state, node, target, source):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
						self._forStmtHeaderElement( ctx, styleSheet, state, target, source ) )



	# Try statement
	def _tryStmtHeaderElement(self, ctx, styleSheet, state):
		return styleSheet.tryStmtHeader()

	@ObjectNodeDispatchMethod( Schema.TryStmtHeader )
	def TryStmtHeader(self, ctx, styleSheet, state, node):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._tryStmtHeaderElement( ctx, styleSheet, state ) )



	# Except statement
	def _exceptStmtHeaderElement(self, ctx, styleSheet, state, exception, target):
		excView = ctx.presentFragment( exception, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if exception is not None   else None
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if target is not None   else None
		return styleSheet.exceptStmtHeader( excView, targetView )

	@ObjectNodeDispatchMethod( Schema.ExceptStmtHeader )
	def ExceptStmtHeader(self, ctx, styleSheet, state, node, exception, target):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._exceptStmtHeaderElement( ctx, styleSheet, state, exception, target ) )



	# Finally statement
	def _finallyStmtHeaderElement(self, ctx, styleSheet, state):
		return styleSheet.finallyStmtHeader()

	@ObjectNodeDispatchMethod( Schema.FinallyStmtHeader )
	def FinallyStmtHeader(self, ctx, styleSheet, state, node):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._finallyStmtHeaderElement( ctx, styleSheet, state ) )



	# With statement
	def _withStmtHeaderElement(self, ctx, styleSheet, state, expr, target):
		exprView = ctx.presentFragment( expr, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		targetView = ctx.presentFragment( target, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if target is not None   else None
		return styleSheet.withStmtHeader( exprView, targetView )

	@ObjectNodeDispatchMethod( Schema.WithStmtHeader )
	def WithStmtHeader(self, ctx, styleSheet, state, node, expr, target):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._withStmtHeaderElement( ctx, styleSheet, state, expr, target ) )



	# Decorator statement
	def _decoStmtHeaderElement(self, ctx, styleSheet, state, name, args, argsTrailingSeparator):
		argViews = ctx.mapPresentFragment( args, styleSheet.withPythonState( PRECEDENCE_STMT ) )   if args is not None   else None
		return styleSheet.decoStmtHeader( name, argViews, argsTrailingSeparator is not None )

	@ObjectNodeDispatchMethod( Schema.DecoStmtHeader )
	def DecoStmtHeader(self, ctx, styleSheet, state, node, name, args, argsTrailingSeparator):
		return compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._decoStmtHeaderElement( ctx, styleSheet, state, name, args, argsTrailingSeparator ) )



	# Def statement
	def _defStmtHeaderElement(self, ctx, styleSheet, state, name, params, paramsTrailingSeparator):
		paramViews = ctx.mapPresentFragment( params, styleSheet.withPythonState( PRECEDENCE_STMT ) )
		return styleSheet.defStmtHeader( name, paramViews, paramsTrailingSeparator is not None )

	@ObjectNodeDispatchMethod( Schema.DefStmtHeader )
	def DefStmtHeader(self, ctx, styleSheet, state, node, name, params, paramsTrailingSeparator):
		editor = compoundStatementHeaderEditor( self._parser, styleSheet, node,
					    self._defStmtHeaderElement( ctx, styleSheet, state, name, params, paramsTrailingSeparator ),
					    lambda header: styleSheet.defStmtHeaderHighlight( header ) )
		return styleSheet.defStmtHighlight( editor )


	# Def statement
	def _classStmtHeaderElement(self, ctx, styleSheet, state, name, bases, basesTrailingSeparator):
		baseViews = ctx.mapPresentFragment( bases, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT ) )   if bases is not None   else None
		return styleSheet.classStmtHeader( name, baseViews, basesTrailingSeparator is not None )

	@ObjectNodeDispatchMethod( Schema.ClassStmtHeader )
	def ClassStmtHeader(self, ctx, styleSheet, state, node, name, bases, basesTrailingSeparator):
		editor = compoundStatementHeaderEditor( self._parser, styleSheet, node,
						  self._classStmtHeaderElement( ctx, styleSheet, state, name, bases, basesTrailingSeparator ),
		                                  lambda header: styleSheet.classStmtHeaderHighlight( header ) )
		return styleSheet.classStmtHighlight( editor )


	

	#
	#
	# STRUCTURE STATEMENTS
	#
	#

	# Indented block
	@ObjectNodeDispatchMethod( Schema.IndentedBlock )
	def IndentedBlock(self, ctx, styleSheet, state, node, suite):
		indent = styleSheet.indentElement()
		indent.setStructuralValueObject( Schema.Indent() )
		
		lineViews = ctx.mapPresentFragment( suite, styleSheet.withPythonState( PRECEDENCE_NONE, PythonEditorStyleSheet.MODE_EDITSTATEMENT ) )
		
		dedent = styleSheet.dedentElement()
		dedent.setStructuralValueObject( Schema.Dedent() )
		
		suiteElement = styleSheet.indentedBlock( indent, lineViews, dedent )
		suiteElement.setStructuralValueObject( node )
		suiteListener = SuiteTreeEventListener( self._parser.compoundSuite(), suite )
		suiteElement.addTreeEventListener( TextEditEvent, suiteListener )
		suiteElement.addTreeEventListener( PythonIndentationTreeEvent, suiteListener )
		suiteElement.addTreeEventListener( PythonSelectionTreeEvent, suiteListener )
		
		return styleSheet.badIndentation( suiteElement )





	#
	#
	# COMPOUND STATEMENTS
	#
	#

	# If statement
	@ObjectNodeDispatchMethod( Schema.IfStmt )
	def IfStmt(self, ctx, styleSheet, state, node, condition, suite, elifBlocks, elseSuite):
		compoundBlocks = [ ( Schema.IfStmtHeader( condition=condition ), self._ifStmtHeaderElement( ctx, styleSheet, state, condition ), suite ) ]
		for b in elifBlocks:
			if not b.isInstanceOf( Schema.ElifBlock ):
				raise TypeError, 'IfStmt elifBlocks should only contain ElifBlock instances'
			compoundBlocks.append( ( Schema.ElifStmtHeader( condition=b['condition'] ), self._elifStmtHeaderElement( ctx, styleSheet, state, b['condition'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )



	# While statement
	@ObjectNodeDispatchMethod( Schema.WhileStmt )
	def WhileStmt(self, ctx, styleSheet, state, node, condition, suite, elseSuite):
		compoundBlocks = [ ( Schema.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( ctx, styleSheet, state, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )

	

	# For statement
	@ObjectNodeDispatchMethod( Schema.ForStmt )
	def ForStmt(self, ctx, styleSheet, state, node, target, source, suite, elseSuite):
		compoundBlocks = [ ( Schema.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( ctx, styleSheet, state, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )



	# Try statement
	@ObjectNodeDispatchMethod( Schema.TryStmt )
	def TryStmt(self, ctx, styleSheet, state, node, suite, exceptBlocks, elseSuite, finallySuite):
		compoundBlocks = [ ( Schema.TryStmtHeader(), self._tryStmtHeaderElement( ctx, styleSheet, state ), suite ) ]
		for b in exceptBlocks:
			if not b.isInstanceOf( Schema.ExceptBlock ):
				raise TypeError, 'TryStmt elifBlocks should only contain ExceptBlock instances'
			compoundBlocks.append( ( Schema.ExceptStmtHeader( exception=b['exception'], target=b['target'] ), self._exceptStmtHeaderElement( ctx, styleSheet, state, b['exception'], b['target'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		if finallySuite is not None:
			compoundBlocks.append( ( Schema.FinallyStmtHeader(), self._finallyStmtHeaderElement( ctx, styleSheet, state ),  finallySuite ) )
		return compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )




	# With statement
	@ObjectNodeDispatchMethod( Schema.WithStmt )
	def WithStmt(self, ctx, styleSheet, state, node, expr, target, suite):
		compoundBlocks = [ ( Schema.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( ctx, styleSheet, state, expr, target ), suite ) ]
		return compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )



	# Def statement
	@ObjectNodeDispatchMethod( Schema.DefStmt )
	def DefStmt(self, ctx, styleSheet, state, node, decorators, name, params, paramsTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Schema.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Schema.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
						 self._decoStmtHeaderElement( ctx, styleSheet, state, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )
			
		compoundBlocks.append( ( Schema.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ),
					 self._defStmtHeaderElement( ctx, styleSheet, state, name, params, paramsTrailingSeparator ), suite,
		                         lambda header: styleSheet.defStmtHeaderHighlight( header ) ) )
		editor = compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )
		return styleSheet.defStmtHighlight( editor )


	# Class statement
	@ObjectNodeDispatchMethod( Schema.ClassStmt )
	def ClassStmt(self, ctx, styleSheet, state, node, name, bases, basesTrailingSeparator, suite):
		compoundBlocks = [ ( Schema.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ),
				     self._classStmtHeaderElement( ctx, styleSheet, state, name, bases, basesTrailingSeparator ), suite,
		                     lambda header: styleSheet.classStmtHeaderHighlight( header ) ) ]
		editor = compoundStatementEditor( ctx, self._parser, styleSheet, node, PRECEDENCE_STMT,
						compoundBlocks,
						state )
		return styleSheet.classStmtHighlight( editor )




class Python25EditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject.withTitle( 'Py2.5: ' + enclosingSubject.getTitle() )
		else:
			return None
	

	
_parser = Python25Grammar()
_viewFn = PyGSymViewFragmentFunction( Python25View( _parser ) )
perspective = GSymPerspective( _viewFn, PythonEditorStyleSheet.instance, AttributeTable.instance, Python25EditHandler(), Python25EditorRelativeLocationResolver() )
