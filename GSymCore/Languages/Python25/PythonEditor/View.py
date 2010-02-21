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

from Britefury.gSym.gSymResolveContext import GSymResolveContext
from Britefury.gSym.gSymResolveResult import GSymResolveResult
from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch, GSymViewPage

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym.View import GSymViewContext



from GSymCore.Languages.Python25 import NodeClasses as Nodes

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Styles import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.NodeEditor import *
from GSymCore.Languages.Python25.PythonEditor.SelectionEditor import *
from GSymCore.Languages.Python25.PythonEditor.Keywords import *
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditorStyleSheet import PythonEditorStyleSheet




DEFAULT_LINE_BREAK_PRIORITY = 100



_statementKeyboardListener = StatementKeyboardListener()




def _nodeRequiresParens(node):
	return node.isInstanceOf( Nodes.Expr )  or  node.isInstanceOf( Nodes.Target )

def computeBinOpViewPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1




def expressionNodeEditor(styleSheet, node, precedence, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_DISPLAYCONTENTS:
		if _nodeRequiresParens( node ):
			contents = styleSheet.applyParens( contents, precedence, getNumParens( node ) )
		return contents
	elif mode == PythonEditorStyleSheet.MODE_EDITEXPRESSION:
		parser = styleSheet['parser']
		outerPrecedence = styleSheet.getOuterPrecedence()
		
		if _nodeRequiresParens( node ):
			contents = styleSheet.applyParens( contents, precedence, getNumParens( node ) )
		contents.setLinearRepresentationListener( ParsedExpressionLinearRepresentationListener.newListener( parser, outerPrecedence ) )
		return contents
	elif mode == PythonEditorStyleSheet.MODE_EDITSTATEMENT:
		return statementNodeEditor( styleSheet, node, contents )
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def structuralExpressionNodeEditor(styleSheet, node, precedence, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_DISPLAYCONTENTS  or  mode == PythonEditorStyleSheet.MODE_EDITEXPRESSION:
		contents = styleSheet.applyParens( contents, _nodeRequiresParens( node ), precedence, getNumParens( node ) )
		contents.setLinearRepresentationListener( StructuralExpressionLinearRepresentationListener.newListener() )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def statementNodeEditor(styleSheet, node, contents):
	mode = styleSheet['editMode']
	if mode == PythonEditorStyleSheet.MODE_EDITSTATEMENT:
		parser = styleSheet['parser']
		
		statementLine = styleSheet.statementLine( contents )
		
		if node.isInstanceOf( Nodes.UNPARSED ):
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
		statementLine.setLinearRepresentationListener( StatementLinearRepresentationListener.newListener( parser ) )
		statementLine.setKeyboardListener( _statementKeyboardListener )
		return statementLine
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def compoundStatementHeaderEditor(styleSheet, node, headerContents, headerContainerFn=None):
	parser = styleSheet['parser']
	
	headerStatementLine = styleSheet.statementLine( headerContents )
	
	headerStatementLine.setStructuralValueObject( node )
	headerStatementLine.setLinearRepresentationListener( StatementLinearRepresentationListener.newListener( parser ) )
	headerStatementLine.setKeyboardListener( _statementKeyboardListener )
	if headerContainerFn is not None:
		headerStatementLine = headerContainerFn( headerStatementLine )
	return headerStatementLine


def compoundStatementEditor(ctx, node, precedence, compoundBlocks, state, suiteParser, statementParser):
	outerPrecedence, parser, mode = state

	statementContents = []
	for i, block in enumerate( compoundBlocks ):
		if len( block ) == 3:
			headerNode, headerContents, suite = block
			headerContainerFn = None
		elif len( block ) == 4:
			headerNode, headerContents, suite, headerContainerFn = block
		else:
			raise TypeError, 'Compound block should be of the form (headerContents, suite)  or  (headerContents, suite, headerContainerFn)'
		
		statementLine = styleSheet.statementLine( headerContents )
		
		statementLine.setStructuralValueObject( headerNode )
		statementLine.setLinearRepresentationListener( CompoundHeaderLinearRepresentationListener.newListener( statementParser ) )
		statementLine.setKeyboardListener( _statementKeyboardListener )
		
		if headerContainerFn is not None:
			statementLine = headerContainerFn( statementLine )



		if suite is not None:
			suiteElement = indentedSuiteView( ctx, suite, statementParser )
			suiteElement.setStructuralValueObject( Nodes.IndentedBlock( suite=suite ) )
			suiteElement = ctx.linearRepresentationListener( suiteElement, SuiteLinearRepresentationListener( suiteParser, suite ) )
			
			statementContents.extend( [ headerParagraph.alignHExpand(), ctx.indent( 30.0, suiteElement ).alignHExpand() ] )
		else:
			statementContents.append( headerParagraph.alignHExpand() )
			
	statementElement = ctx.vbox( compoundStmt_vboxStyle, statementContents )
	return statementElement


def compoundStatementEditor(styleSheet, node, precedence, compoundBlocks, state, suiteParser, statementParser):
	statementContents = []
	for i, block in enumerate( compoundBlocks ):
		if len( block ) == 3:
			headerNode, headerContents, suite = block
			headerContainerFn = None
		elif len( block ) == 4:
			headerNode, headerContents, suite, headerContainerFn = block
		else:
			raise TypeError, 'Compound block should be of the form (headerContents, suite)  or  (headerContents, suite, headerContainerFn)'
		
		headerStatementLine = styleSheet.statementLine( True, True, headerContents )
		headerStatementLine.setStructuralValueObject( headerNode )
		headerStatementLine.setLinearRepresentationListener( CompoundHeaderLinearRepresentationListener.newListener( statementParser ) )
		headerStatementLine.setKeyboardListener( _statementKeyboardListener )
		
		if headerContainerFn is not None:
			headerStatementLine = headerContainerFn( headerStatementLine )



		if suite is not None:
			indent = styleSheet.indentElement()
			indent.setStructuralValueObject( Nodes.Indent() )
			
			lineViews = ctx.mapViewEval( suite, styleSheet.withPythonState( PRECEDENCE_NONE, statementParser, PythonEditorStyleSheet.MODE_EDITSTATEMENT ) )
			
			dedent = styleSheet.dedentElement()
			dedent.setStructuralValueObject( Nodes.Dedent() )
			
			suiteElement = styleSheet.indentedBlock( indent, lineViews, dedent )
			suiteElement.setStructuralValueObject( Nodes.IndentedBlock( suite=suite ) )
			suiteElement.setLinearRepresentationListener( SuiteLinearRepresentationListener( suiteParser, suite ) )
			
			statementContents.extend( [ headerStatementLine.alignHExpand(), suiteElement.alignHExpand() ] )
		else:
			statementContents.append( headerStatementLine.alignHExpand() )
			
	return styleSheet.compoundStmt( statementContents )



def spanPrefixOpView(ctx, styleSheet, node, x, op, precedence, parser):
	xView = ctc.viewEval( x, styleSheet.withPythonState( precedence, parser, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	view = styleSheet.spanPrefixOp( xView, op )
	return expressionNodeEditor( styleSheet, node, precedence,
	                             view )


def spanBinOpView(ctx, styleSheet, node, x, y, op, precedence, bRightAssociative, parser):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( precedence, bRightAssociative )
	xView = ctx.viewEval( x, styleSheet.withPythonState( xPrec, parser, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	yView = ctx.viewEval( y, styleSheet.withPythonState( yPrec, parser, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	view = styleSheet.spanBinOp( xView, yView, op )
	return expressionNodeEditor( styleSheet, node, precedence,
	                             view )


def spanCmpOpView(ctx, styleSheet, node, op, y, precedence, parser):
	yView = ctx.viewEval( y, styleSheet.withPythonState( yPrec, parser, PythonEditorStyleSheet.MODE_DISPLAYCONTENTS ) )
	view = styleSheet.spanBinOp( op, yView )
	return expressionNodeEditor( styleSheet, node, precedence,
	                             view )
	
	
	
	
	
	
def printElem(elem, level):
	print '  ' * level, elem, elem.getTextRepresentation()
	if isinstance( elem, BranchElement ):
		for x in elem.getChildren():
			printElem( x, level + 1 )



class Python25View (GSymViewObjectNodeDispatch):
	def __init__(self):
		self._parser = Python25Grammar()


	# MISC
	@ObjectNodeDispatchMethod( Nodes.PythonModule )
	def PythonModule(self, ctx, styleSheet, state, node, suite):
		lineViews = ctx.mapViewEval( suite, styleSheet.withPythonState( PRECEDENCE_NONE, self._parser.singleLineStatement(), PythonEditorStyleSheet.MODE_EDITSTATEMENT ) )
		suiteElement = styleSheet.suiteView( lineViews )
		suiteElement.setStructuralValueObject( suite )
		suiteElement.setLinearRepresentationListener( SuiteLinearRepresentationListener( self._parser.suite(), suite ) )
		return suiteElement



	@ObjectNodeDispatchMethod( Nodes.BlankLine )
	def BlankLine(self, ctx, styleSheet, state, node):
		return statementNodeEditor( styleSheet, node,
		                            styleSheet.blankLine() )


	@ObjectNodeDispatchMethod( Nodes.UNPARSED )
	def UNPARSED(self, ctx, styleSheet, state, node, value):
		def _viewItem(x):
			if x is node:
				raise ValueError, 'Python25View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				return styleSheet.unparseableText( x )
			elif isinstance( x, DMObjectInterface ):
				return ctx.viewEval( x, styleSheet.withPythonState( PRECEDENCE_CONTAINER_UNPARSED, self._parser.expression() ) )
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_NONE,
		                             styleSheet.unparsedElements( views ) )





	# Comment statement
	@ObjectNodeDispatchMethod( Nodes.CommentStmt )
	def CommentStmt(self, ctx, styleSheet, state, node, comment):
		view = styleSheet.commentStmt( comment )
		return statementNodeEditor( styleSheet, node,
		                            view )


	
	
	
	# String literal
	__strLit_fmtTable = { 'ascii' : None,  'unicode' : 'u',  'ascii-regex' : 'r',  'unicode-regex' : 'ur' }
	
	@ObjectNodeDispatchMethod( Nodes.StringLiteral )
	def StringLiteral(self, ctx, styleSheet, state, node, format, quotation, value):
		fmt = __strLit_fmtTable[format]
		
		quote = "'"   if quotation == 'single'   else   '"'
		
		view = styleSheet.stringLiteral( fmt, quote, value )

		return expressionNodeEditor( styleSheet, node, PRECEDENCE_LITERALVALUE,
		                             view )

	# Integer literal
	@ObjectNodeDispatchMethod( Nodes.IntLiteral )
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
		
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_LITERALVALUE,
		                             view )



	# Float literal
	@ObjectNodeDispatchMethod( Nodes.FloatLiteral )
	def FloatLiteral(self, ctx, styleSheet, state, node, value):
		return expressionNodeEditor( styleSheet, node,
					     PRECEDENCE_LITERALVALUE,
					     styleSheet.floatLiteral( value ) )



	# Imaginary literal
	@ObjectNodeDispatchMethod( Nodes.ImaginaryLiteral )
	def ImaginaryLiteral(self, ctx, styleSheet, state, node, value):
		return expressionNodeEditor( styleSheet, node,
					     PRECEDENCE_LITERALVALUE,
		                             styleSheet.imaginaryLiteral( value ) )



	# Targets
	@ObjectNodeDispatchMethod( Nodes.SingleTarget )
	def SingleTarget(self, ctx, styleSheet, state, node, name):
		return expressionNodeEditor( styleSheet, node,
					     PRECEDENCE_TARGET,
					     styleSheet.singleTarget( name ) )


	@ObjectNodeDispatchMethod( Nodes.TupleTarget )
	def TupleTarget(self, ctx, styleSheet, state, node, targets, trailingSeparator):
		elementViews = ctx.mapViewEval( targets, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.targetItem() ) )
		view = styleSheet.tupleTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_TARGET,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.ListTarget )
	def ListTarget(self, ctx, styleSheet, state, node, targets, trailingSeparator):
		elementViews = ctx.mapViewEval( targets, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.targetItem() ) )
		view = styleSheet.listTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_TARGET,
		                             view )




	# Variable reference
	@ObjectNodeDispatchMethod( Nodes.Load )
	def Load(self, ctx, styleSheet, state, node, name):
		return expressionNodeEditor( styleSheet, node,
					     PRECEDENCE_LOAD,
					     styleSheet.load( name ) )



	# Tuple literal
	@ObjectNodeDispatchMethod( Nodes.TupleLiteral )
	def TupleLiteral(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapViewEval( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		view = styleSheet.tupleLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_TUPLE,
		                             view )



	# List literal
	@ObjectNodeDispatchMethod( Nodes.ListLiteral )
	def ListLiteral(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapViewEval( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		view = styleSheet.listLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )



	# List comprehension / generator expression
	@ObjectNodeDispatchMethod( Nodes.ComprehensionFor )
	def ComprehensionFor(self, ctx, styleSheet, state, node, target, source):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_COMPREHENSIONFOR, self._parser.targetListOrTargetItem() ) )
		sourceView = ctx.viewEval( source, styleSheet.withPythonState( PRECEDENCE_CONTAINER_COMPREHENSIONFOR, self._parser.oldTupleOrExpression() ) )
		view = styleSheet.comprehensionFor( targetView, sourceView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.ComprehensionIf )
	def ComprehensionIf(self, ctx, styleSheet, state, node, condition):
		conditionView = ctx.viewEval( condition, styleSheet.withPythonState( PRECEDENCE_CONTAINER_COMPREHENSIONIF, self._parser.oldExpression() ) )
		view = styleSheet.comprehensionIf( conditionView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.ListComp )
	def ListComp(self, ctx, styleSheet, state, node, resultExpr, comprehensionItems):
		exprView = ctx.viewEval( resultExpr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		itemViews = ctx.mapViewEval( comprehensionItems, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.listCompItem() ) )
		view = styleSheet.listComp( exprView, itemViews )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )


	@ObjectNodeDispatchMethod( Nodes.GeneratorExpr )
	def GeneratorExpr(self, ctx, styleSheet, state, node, resultExpr, comprehensionItems):
		exprView = ctx.viewEval( resultExpr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		itemViews = ctx.mapViewEval( comprehensionItems, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.listCompItem() ) )
		view = styleSheet.genExpr( exprView, itemViews )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_LISTDISPLAY,
		                             view )




	# Dictionary literal
	@ObjectNodeDispatchMethod( Nodes.DictKeyValuePair )
	def DictKeyValuePair(self, ctx, styleSheet, state, node, key, value):
		keyView = ctx.viewEval( key, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		return expressionNodeEditor( ctx, node,
					     ctx.span( [ keyView, ctx.text( punctuation_textStyle, ' : ' ), valueView ] ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod( Nodes.DictLiteral )
	def DictLiteral(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapViewEval( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		view = styleSheet.dictLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_DICTDISPLAY,
		                             view )


	# Yield expression
	@ObjectNodeDispatchMethod( Nodes.YieldExpr )
	def YieldExpr(self, ctx, styleSheet, state, node, value):
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_YIELDEXPR, self._parser.expression() ) )
		view = styleSheet.yieldExpr( valueView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_YIELDEXPR,
		                             view )



	# Attribute ref
	@ObjectNodeDispatchMethod( Nodes.AttributeRef )
	def AttributeRef(self, ctx, styleSheet, state, node, target, name):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET, self._parser.expression() ) )
		view = styleSheet.attributeRef( targetView, name )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_ATTR,
		                             view )



	# Subscript
	@ObjectNodeDispatchMethod( Nodes.SubscriptSlice )
	def SubscriptSlice(self, ctx, styleSheet, state, node, lower, upper):
		lowerView = ctx.viewEval( lower, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) )   if lower is not None   else None
		upperView = ctx.viewEval( upper, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) )   if upper is not None   else None
		view = styleSheet.subscriptSlice( lower, upper )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.SubscriptLongSlice )
	def SubscriptLongSlice(self, ctx, styleSheet, state, node, lower, upper, stride):
		lowerView = ctx.viewEval( lower, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) )   if lower is not None   else None
		upperView = ctx.viewEval( upper, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) )   if upper is not None   else None
		strideView = ctx.viewEval( stride, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.expression() ) )   if stride is not None   else None
		view = styleSheet.subscriptLongSlice( lower, upper, stride )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.SubscriptEllipsis )
	def SubscriptEllipsis(self, ctx, styleSheet, state, node):
		return expressionNodeEditor( ctx, node,
					     ctx.text( punctuation_textStyle, '...' ),
					     PRECEDENCE_NONE,
					     state )

	@ObjectNodeDispatchMethod( Nodes.SubscriptTuple )
	def SubscriptTuple(self, ctx, styleSheet, state, node, values, trailingSeparator):
		elementViews = ctx.mapViewEval( values, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )
		view = styleSheet.subscriptTuple( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_TUPLE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.Subscript )
	def Subscript(self, ctx, styleSheet, state, node, target, index):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTTARGET, self._parser.expression() ) )
		indexView = ctx.viewEval( index, styleSheet.withPythonState( PRECEDENCE_CONTAINER_SUBSCRIPTINDEX, self._parser.subscriptIndex() ) )
		view = styleSheet.subscript( targetView, indexView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_SUBSCRIPT,
		                             view )




	# Call
	@ObjectNodeDispatchMethod( Nodes.CallKWArg )
	def CallKWArg(self, ctx, styleSheet, state, node, name, value):
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG, self._parser.expression() ) )
		view = styleSheet.callKWArg( name, valueView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.CallArgList )
	def CallArgList(self, ctx, styleSheet, state, node, value):
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG, self._parser.expression() ) )
		view = styleSheet.callArgList( valueView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.CallKWArgList )
	def CallKWArgList(self, ctx, styleSheet, state, node, value):
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG, self._parser.expression() ) )
		view = styleSheet.callKWArgList( valueView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.Call )
	def Call(self, ctx, styleSheet, state, node, target, args, argsTrailingSeparator):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLTARGET, self._parser.expression() ) )
		argViews = ctx.mapViewEval( args, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CALLARG, self._parser.callArg() ) )
		view = styleSheet.call( targetView, argViews, argsTrailingSeparator is not None )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_CALL,
		                             view )





	# Operators
	@ObjectNodeDispatchMethod( Nodes.Pow )
	def Pow(self, ctx, styleSheet, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = ctx.viewEval( x, styleSheet.withPythonState( xPrec, self._parser.expression() ) )
		yView = ctx.viewEval( y, styleSheet.withPythonState( yPrec, self._parser.expression(), PythonEditorStyleSheet.MODE_EDITEXPRESSION ) )
		view = styleSheet.pow( xView, yView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_POW,
		                             view )


	@ObjectNodeDispatchMethod( Nodes.Invert )
	def Invert(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, styleSheet, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.Negate )
	def Negate(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, styleSheet, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.Pos )
	def Pos(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, styleSheet, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS, self._parser.expression() )


	@ObjectNodeDispatchMethod( Nodes.Mul )
	def Mul(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '*', PRECEDENCE_MULDIVMOD, False, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.Div )
	def Div(self, ctx, styleSheet, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = ctx.viewEval( x, styleSheet.withPythonState( xPrec, self._parser.expression(), PythonEditorStyleSheet.MODE_EDITEXPRESSION ) )
		yView = ctx.viewEval( y, styleSheet.withPythonState( yPrec, self._parser.expression(), PythonEditorStyleSheet.MODE_EDITEXPRESSION ) )
		view = styleSheet.div( xView, yView, '/' )
		view.setStructuralValueObject( node )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_MULDIVMOD,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.Mod )
	def Mod(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '%', PRECEDENCE_MULDIVMOD, False, self._parser.expression() )


	@ObjectNodeDispatchMethod( Nodes.Add )
	def Add(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '+', PRECEDENCE_ADDSUB, False, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.Sub )
	def Sub(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '-', PRECEDENCE_ADDSUB, False, self._parser.expression() )


	@ObjectNodeDispatchMethod( Nodes.LShift )
	def LShift(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '<<', PRECEDENCE_SHIFT, False, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.RShift )
	def RShift(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '>>', PRECEDENCE_SHIFT, False, self._parser.expression() )


	@ObjectNodeDispatchMethod( Nodes.BitAnd )
	def BitAnd(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '&', PRECEDENCE_BITAND, False, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.BitXor )
	def BitXor(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '^', PRECEDENCE_BITXOR, False, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.BitOr )
	def BitOr(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, '|', PRECEDENCE_BITOR, False, self._parser.expression() )


	@ObjectNodeDispatchMethod( Nodes.Cmp )
	def Cmp(self, ctx, styleSheet, state, node, x, ops):
		xView = ctx.viewEval( x, styleSheet.withPythonState( PRECEDENCE_CMP, self._parser.expression() ) )
		opViews = ctx.mapViewEval( ops, styleSheet.withPythonState( PRECEDENCE_CMP, self._parser.expression() ) )
		view = styleSheet.compare( xView, opViews )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_CMP,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.CmpOpLte )
	def CmpOpLte(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, '<=', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpLt )
	def CmpOpLt(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, '<', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpGte )
	def CmpOpGte(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, '>=', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpGt )
	def CmpOpGt(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, '>', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpEq )
	def CmpOpEq(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, '==', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpNeq )
	def CmpOpNeq(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, '!=', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpIsNot )
	def CmpOpIsNot(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, 'is not', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpIs )
	def CmpOpIs(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, 'is', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpNotIn )
	def CmpOpNotIn(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, 'not in', y, PRECEDENCE_CMP, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.CmpOpIn )
	def CmpOpIn(self, ctx, styleSheet, state, node, y):
		return spanCmpOpView( ctx, styleSheet, node, 'in', y, PRECEDENCE_CMP, self._parser.expression() )



	@ObjectNodeDispatchMethod( Nodes.NotTest )
	def NotTest(self, ctx, styleSheet, state, node, x):
		return spanPrefixOpView( ctx, styleSheet, node, x, 'not ', PRECEDENCE_NOT, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.AndTest )
	def AndTest(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, 'and', PRECEDENCE_AND, False, self._parser.expression() )

	@ObjectNodeDispatchMethod( Nodes.OrTest )
	def OrTest(self, ctx, styleSheet, state, node, x, y):
		return spanBinOpView( ctx, styleSheet, node, x, y, 'or', PRECEDENCE_OR, False, self._parser.expression() )





	# Parameters
	@ObjectNodeDispatchMethod( Nodes.SimpleParam )
	def SimpleParam(self, ctx, styleSheet, state, node, name):
		view = styleSheet.simpleParam( name )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.DefaultValueParam )
	def DefaultValueParam(self, ctx, styleSheet, state, node, name, defaultValue):
		valueView = ctx.viewEval( defaultValue, styleSheet.withPythonState( PRECEDENCE_NONE, self._parser.expression() ) )
		view = styleSheet.defaultValueParam( name, valueView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.ParamList )
	def ParamList(self, ctx, styleSheet, state, node, name):
		view = styleSheet.paramList( name )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.KWParamList )
	def KWParamList(self, ctx, styleSheet, state, node, name):
		view = styleSheet.kwParamList( name )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_NONE,
		                             view )



	# Lambda expression
	@ObjectNodeDispatchMethod( Nodes.LambdaExpr )
	def LambdaExpr(self, ctx, styleSheet, state, node, params, paramsTrailingSeparator, expr):
		# The Python 2.5 grammar has two versions of the lambda expression grammar; one what recognises the full lambda expression, and one that
		# reckognises a lambda expression that cannot wrap conditional expression.
		# Ensure that we use the correct parser for @expr
		parser = styleSheet['parser']
		if parser is self._parser.oldExpression()   or  parser is self._parser.oldTupleOrExpression():
			exprParser = self._parser.oldExpression()
		else:
			exprParser = self._parser.expression()

		exprView = ctx.viewEval( expr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_LAMBDAEXPR, exprParser ) )
		paramViews = ctx.mapViewEval( params, styleSheet.withPythonState( PRECEDENCE_NONE, self._parser.param() ) )
		
		view = styleSheet.lambdaExpr( paramViews, paramsTrailingSeparator is not None, exprView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_LAMBDAEXPR,
		                             view )



	# Conditional expression
	@ObjectNodeDispatchMethod( Nodes.ConditionalExpr )
	def ConditionalExpr(self, ctx, styleSheet, state, node, condition, expr, elseExpr):
		conditionView = ctx.viewEval( condition, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CONDITIONALEXPR, self._parser.orTest() ) )
		exprView = ctx.viewEval( expr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CONDITIONALEXPR, self._parser.orTest() ) )
		elseExprView = ctx.viewEval( elseExpr, styleSheet.withPythonState( PRECEDENCE_CONTAINER_CONDITIONALEXPR, self._parser.expression() ) )
		view = styleSheet.conditionalExpr( conditionView, exprView, elseExprView )
		return expressionNodeEditor( styleSheet, node,
			                     PRECEDENCE_CONDITIONAL,
		                             view )




	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Expression statement
	@ObjectNodeDispatchMethod( Nodes.ExprStmt )
	def ExprStmt(self, ctx, styleSheet, state, node, expr):
		exprView = ctx.viewEval( expr, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		view = styleSheet.exprStmt( exprView )
		return statementNodeEditor( styleSheet, node,
		                            view )



	# Assert statement
	@ObjectNodeDispatchMethod( Nodes.AssertStmt )
	def AssertStmt(self, ctx, styleSheet, state, node, condition, fail):
		conditionView = ctx.viewEval( condition, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		failView = ctx.viewEval( fail, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if fail is not None   else None
		view = styleSheet.assertStmt( conditionView, failView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Assignment statement
	@ObjectNodeDispatchMethod( Nodes.AssignStmt )
	def AssignStmt(self, ctx, styleSheet, state, node, targets, value):
		targetViews = ctx.mapViewEval( targets, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.targetListOrTargetItem() ) )
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.tupleOrExpressionOrYieldExpression() ) )
		view = styleSheet.assignStmt( targetViews, valueView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Augmented assignment statement
	@ObjectNodeDispatchMethod( Nodes.AugAssignStmt )
	def AugAssignStmt(self, ctx, styleSheet, state, node, op, target, value):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.targetItem() ) )
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.tupleOrExpressionOrYieldExpression() ) )
		view = styleSheet.augAssignStmt( op, targetView, valueView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Pass statement
	@ObjectNodeDispatchMethod( Nodes.PassStmt )
	def PassStmt(self, ctx, styleSheet, state, node):
		view = styleSheet.passStmt()
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Del statement
	@ObjectNodeDispatchMethod( Nodes.DelStmt )
	def DelStmt(self, ctx, styleSheet, state, node, target):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.targetListOrTargetItem() ) )
		view = styleSheet.delStmt( targetView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Return statement
	@ObjectNodeDispatchMethod( Nodes.ReturnStmt )
	def ReturnStmt(self, ctx, styleSheet, state, node, value):
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.tupleOrExpression() ) )
		view = styleSheet.returnStmt( valueView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Yield statement
	@ObjectNodeDispatchMethod( Nodes.YieldStmt )
	def YieldStmt(self, ctx, styleSheet, state, node, value):
		valueView = ctx.viewEval( value, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		view = styleSheet.yieldStmt( valueView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Raise statement
	@ObjectNodeDispatchMethod( Nodes.RaiseStmt )
	def RaiseStmt(self, ctx, styleSheet, state, node, excType, excValue, traceback):
		excTypeView = ctx.viewEval( excType, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if excType is not None   else None
		excValueView = ctx.viewEval( excValue, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if excValue is not None   else None
		tracebackView = ctx.viewEval( traceback, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if traceback is not None   else None
		view = styleSheet.raiseStmt( excTypeView, excValueView, tracebackView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Break statement
	@ObjectNodeDispatchMethod( Nodes.BreakStmt )
	def BreakStmt(self, ctx, styleSheet, state, node):
		view = styleSheet.breakStmt()
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Continue statement
	@ObjectNodeDispatchMethod( Nodes.ContinueStmt )
	def ContinueStmt(self, ctx, styleSheet, state, node):
		view = styleSheet.continueStmt()
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Import statement
	@ObjectNodeDispatchMethod( Nodes.RelativeModule )
	def RelativeModule(self, ctx, styleSheet, state, node, name):
		view = styleSheet.relativeModule( name )
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.ModuleImport )
	def ModuleImport(self, ctx, styleSheet, state, node, name):
		view = styleSheet.moduleImport( name )
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.ModuleImportAs )
	def ModuleImportAs(self, ctx, styleSheet, state, node, name, asName):
		view = styleSheet.moduleImportAs( name, asName )
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.ModuleContentImport )
	def ModuleContentImport(self, ctx, styleSheet, state, node, name):
		view = styleSheet.moduleContentImport( name )
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.ModuleContentImportAs )
	def ModuleContentImportAs(self, ctx, styleSheet, state, node, name, asName):
		view = styleSheet.moduleContentImportAs( name, asName )
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_IMPORTCONTENT,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.ImportStmt )
	def ImportStmt(self, ctx, styleSheet, state, node, modules):
		moduleViews = ctx.mapViewEval( modules, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.moduleImport() ) )
		view = styleSheet.importStmt( moduleViews )
		return statementNodeEditor( styleSheet, node,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.FromImportStmt )
	def FromImportStmt(self, ctx, styleSheet, state, node, module, imports):
		moduleView = ctx.viewEval( module, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.moduleContentImport() ) )
		importViews = ctx.mapViewEval( imports, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.moduleImport() ) )
		view = styleSheet.fromImportStmt( moduleView, importViews )
		return statementNodeEditor( styleSheet, node,
		                            view )

	@ObjectNodeDispatchMethod( Nodes.FromImportAllStmt )
	def FromImportAllStmt(self, ctx, styleSheet, state, node, module):
		moduleView = ctx.viewEval( module, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.moduleContentImport() ) )
		view = styleSheet.fromImportAllStmt( moduleView )
		return statementNodeEditor( styleSheet, node,
		                            view )


	# Global statement
	@ObjectNodeDispatchMethod( Nodes.GlobalVar )
	def GlobalVar(self, ctx, styleSheet, state, node, name):
		view = styleSheet.globalVar( name )
		return expressionNodeEditor( styleSheet, node, PRECEDENCE_NONE,
		                             view )

	@ObjectNodeDispatchMethod( Nodes.GlobalStmt )
	def GlobalStmt(self, ctx, styleSheet, state, node, vars):
		varViews = ctx.mapViewEval( vars, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.globalVar() ) )
		view = styleSheet.globalStmt( varViews )
		return statementNodeEditor( styleSheet, node,
		                            view )



	# Exec statement
	@ObjectNodeDispatchMethod( Nodes.ExecStmt )
	def ExecStmt(self, ctx, styleSheet, state, node, source, locals, globals):
		sourceView = ctx.viewEval( source, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.orOp() ) )
		localsView = ctx.viewEval( locals, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if locals is not None   else None
		globalsView = ctx.viewEval( globals, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )    if globals is not none   else None
		view = styleSheet.execStmt( sourceView, localsView, globalsView )
		return statementNodeEditor( styleSheet, node,
		                            view )






	# Exec statement
	@ObjectNodeDispatchMethod( Nodes.PrintStmt )
	def PrintStmt(self, ctx, styleSheet, state, node, destination, values):
		destView = ctx.viewEval( destination, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.orOp() ) )   if destination is not None   else None
		valueViews = ctx.mapViewEval( values, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		view = styleSheet.printStmt( destView, valueViews )
		return statementNodeEditor( styleSheet, node,
		                            view )
	
	
	
	
	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def _ifStmtHeaderElement(self, ctx, styleSheet, state, condition):
		conditionView = ctx.viewEval( condition, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		return styleSheet.ifStmtHeader( conditionView )

	@ObjectNodeDispatchMethod( Nodes.IfStmtHeader )
	def IfStmtHeader(self, ctx, styleSheet, state, node, condition):
		return compoundStatementHeaderEditor( ctx, node,
					    self._ifStmtHeaderElement( ctx, styleSheet, state, condition ),
					    PRECEDENCE_STMT,
					    state )


	# Elif statement
	def _elifStmtHeaderElement(self, ctx, styleSheet, state, condition):
		conditionView = ctx.viewEval( condition, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		return styleSheet.elifStmtHeader( conditionView )

	@ObjectNodeDispatchMethod( Nodes.ElifStmtHeader )
	def ElifStmtHeader(self, ctx, styleSheet, state, node, condition):
		return compoundStatementHeaderEditor( ctx, node,
					    self._elifStmtHeaderElement( ctx, styleSheet, state, condition ),
					    PRECEDENCE_STMT,
					    state )



	# Else statement
	def _elseStmtHeaderElement(self, ctx, styleSheet, state):
		return styleSheet.elseStmtHeader()

	@ObjectNodeDispatchMethod( Nodes.ElseStmtHeader )
	def ElseStmtHeader(self, ctx, styleSheet, state, node):
		return compoundStatementHeaderEditor( ctx, node,
					    self._elseStmtHeaderElement( ctx, styleSheet, state ),
					    PRECEDENCE_STMT,
					    state )


	# While statement
	def _whileStmtHeaderElement(self, ctx, styleSheet, state, condition):
		conditionView = ctx.viewEval( condition, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		return styleSheet.withStmtHeader( conditionView )

	@ObjectNodeDispatchMethod( Nodes.WhileStmtHeader )
	def WhileStmtHeader(self, ctx, styleSheet, state, node, condition):
		return compoundStatementHeaderEditor( ctx, node,
					    self._whileStmtHeaderElement( ctx, styleSheet, state, condition ),
					    PRECEDENCE_STMT,
					    state )


	# For statement
	def _forStmtHeaderElement(self, ctx, styleSheet, state, target, source):
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.targetListOrTargetItem() ) )
		sourceView = ctx.viewEval( source, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.tupleOrExpression() ) )
		return styleSheet.forStmtHeader( targetView, sourceView )

	@ObjectNodeDispatchMethod( Nodes.ForStmtHeader )
	def ForStmtHeader(self, ctx, styleSheet, state, node, target, source):
		return compoundStatementHeaderEditor( ctx, node,
						self._forStmtHeaderElement( ctx, styleSheet, state, target, source ),
						PRECEDENCE_STMT,
						state )



	# Try statement
	def _tryStmtHeaderElement(self, ctx, styleSheet, state):
		return styleSheet.tryStmtHeader()

	@ObjectNodeDispatchMethod( Nodes.TryStmtHeader )
	def TryStmtHeader(self, ctx, styleSheet, state, node):
		return compoundStatementHeaderEditor( ctx, node,
					    self._tryStmtHeaderElement( ctx, styleSheet, state ),
					    PRECEDENCE_STMT,
					    state )



	# Except statement
	def _exceptStmtHeaderElement(self, ctx, styleSheet, state, exception, target):
		excView = ctx.viewEval( exception, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if exception is not None   else None
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if target is not None   else None
		return styleSheet.exceptStmtHeader( excView, targetView )

	@ObjectNodeDispatchMethod( Nodes.ExceptStmtHeader )
	def ExceptStmtHeader(self, ctx, styleSheet, state, node, exception, target):
		return compoundStatementHeaderEditor( ctx, node,
					    self._exceptStmtHeaderElement( ctx, styleSheet, state, exception, target ),
					    PRECEDENCE_STMT,
					    state )



	# Finally statement
	def _finallyStmtHeaderElement(self, ctx, styleSheet, state):
		return styleSheet.finallyStmtHeader()

	@ObjectNodeDispatchMethod( Nodes.FinallyStmtHeader )
	def FinallyStmtHeader(self, ctx, styleSheet, state, node):
		return compoundStatementHeaderEditor( ctx, node,
					    self._finallyStmtHeaderElement( ctx, styleSheet, state ),
					    PRECEDENCE_STMT,
					    state )



	# With statement
	def _withStmtHeaderElement(self, ctx, styleSheet, state, expr, target):
		exprView = ctx.viewEval( expr, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )
		targetView = ctx.viewEval( target, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.expression() ) )   if target is not None   else None
		return styleSheet.withStmtHeader( exprView, targetView )

	@ObjectNodeDispatchMethod( Nodes.WithStmtHeader )
	def WithStmtHeader(self, ctx, styleSheet, state, node, expr, target):
		return compoundStatementHeaderEditor( ctx, node,
					    self._withStmtHeaderElement( ctx, styleSheet, state, expr, target ),
					    PRECEDENCE_STMT,
					    state )



	# Decorator statement
	def _decoStmtHeaderElement(self, ctx, styleSheet, state, name, args, argsTrailingSeparator):
		argViews = ctx.mapViewEval( args, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.callArg() ) )   if args is not None   else None
		return styleSheet.decoStmtHeader( name, argViews, argsTrailingSeparator is not None )

	@ObjectNodeDispatchMethod( Nodes.DecoStmtHeader )
	def DecoStmtHeader(self, ctx, styleSheet, state, node, name, args, argsTrailingSeparator):
		return compoundStatementHeaderEditor( ctx, node,
					    self._decoStmtHeaderElement( ctx, styleSheet, state, name, args, argsTrailingSeparator ),
					    PRECEDENCE_STMT,
					    state )



	# Def statement
	def _defStmtHeaderElement(self, ctx, styleSheet, state, name, params, paramsTrailingSeparator):
		paramViews = ctx.mapViewEval( params, styleSheet.withPythonState( PRECEDENCE_STMT, self._parser.param() ) )
		return styleSheet.defStmtHeader( name, paramViews, paramsTrailingSeparator is not None )

	@ObjectNodeDispatchMethod( Nodes.DefStmtHeader )
	def DefStmtHeader(self, ctx, styleSheet, state, node, name, params, paramsTrailingSeparator):
		editor = compoundStatementHeaderEditor( ctx, node,
					    self._defStmtHeaderElement( ctx, styleSheet, state, name, params, paramsTrailingSeparator ),
					    PRECEDENCE_STMT,
					    state,
					    lambda header: ctx.border( defHeader_border, ContainerStyleParams.defaultStyleParams, header ) )
		#return ctx.border( defBackground_border, ContainerStyleParams.defaultStyleParams, editor )
		return editor


	# Def statement
	def _classStmtHeaderElement(self, ctx, styleSheet, state, name, bases, basesTrailingSeparator):
		baseViews = ctx.mapViewEval( bases, styleSheet.withPythonState( PRECEDENCE_CONTAINER_ELEMENT, self._parser.expression() ) )   if bases is not None   else None
		return styleSheet.classStmtHeader( name, baseViews, basesTrailingSeparator is not None )

	@ObjectNodeDispatchMethod( Nodes.ClassStmtHeader )
	def ClassStmtHeader(self, ctx, styleSheet, state, node, name, bases, basesTrailingSeparator):
		editor = compoundStatementHeaderEditor( ctx, node,
						  self._classStmtHeaderElement( ctx, styleSheet, state, name, bases, basesTrailingSeparator ),
						  PRECEDENCE_STMT,
						  state,
						  lambda header: ctx.border( classHeader_border, ContainerStyleParams.defaultStyleParams, header ) )
		#return ctx.border( classBackground_border, ContainerStyleParams.defaultStyleParams, editor )
		return editor


	

	#
	#
	# STRUCTURE STATEMENTS
	#
	#

	# Indented block
	@ObjectNodeDispatchMethod( Nodes.IndentedBlock )
	def IndentedBlock(self, ctx, styleSheet, state, node, suite):
		indent = styleSheet.indentElement()
		indent.setStructuralValueObject( Nodes.Indent() )
		
		lineViews = ctx.mapViewEval( suite, styleSheet.withPythonState( PRECEDENCE_NONE, self._parser.singleLineStatement(), PythonEditorStyleSheet.MODE_EDITSTATEMENT ) )
		
		dedent = styleSheet.dedentElement()
		dedent.setStructuralValueObject( Nodes.Dedent() )
		
		suiteElement = styleSheet.indentedBlock( indent, lineViews, dedent )
		suiteElement.setStructuralValueObject( node )
		suiteElement.setLinearRepresentationListener( SuiteLinearRepresentationListener( self._parser.compoundSuite(), suite ) )
		
		return styleSheet.badIndentation( styleSheet )





	#
	#
	# COMPOUND STATEMENTS
	#
	#

	# If statement
	@ObjectNodeDispatchMethod( Nodes.IfStmt )
	def IfStmt(self, ctx, styleSheet, state, node, condition, suite, elifBlocks, elseSuite):
		compoundBlocks = [ ( Nodes.IfStmtHeader( condition=condition ), self._ifStmtHeaderElement( ctx, styleSheet, state, condition ), suite ) ]
		for b in elifBlocks:
			if not b.isInstanceOf( Nodes.ElifBlock ):
				raise TypeError, 'IfStmt elifBlocks should only contain ElifBlock instances'
			compoundBlocks.append( ( Nodes.ElifStmtHeader( condition=b['condition'] ), self._elifStmtHeaderElement( ctx, styleSheet, state, b['condition'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Nodes.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )



	# While statement
	@ObjectNodeDispatchMethod( Nodes.WhileStmt )
	def WhileStmt(self, ctx, styleSheet, state, node, condition, suite, elseSuite):
		compoundBlocks = [ ( Nodes.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( ctx, styleSheet, state, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Nodes.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )

	

	# For statement
	@ObjectNodeDispatchMethod( Nodes.ForStmt )
	def ForStmt(self, ctx, styleSheet, state, node, target, source, suite, elseSuite):
		compoundBlocks = [ ( Nodes.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( ctx, styleSheet, state, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )



	# Try statement
	@ObjectNodeDispatchMethod( Nodes.TryStmt )
	def TryStmt(self, ctx, styleSheet, state, node, suite, exceptBlocks, elseSuite, finallySuite):
		compoundBlocks = [ ( Nodes.TryStmtHeader(), self._tryStmtHeaderElement( ctx, styleSheet, state ), suite ) ]
		for b in exceptBlocks:
			if not b.isInstanceOf( Nodes.ExceptBlock ):
				raise TypeError, 'TryStmt elifBlocks should only contain ExceptBlock instances'
			compoundBlocks.append( ( Nodes.ExceptStmtHeader( exception=b['exception'], target=b['target'] ), self._exceptStmtHeaderElement( ctx, styleSheet, state, b['exception'], b['target'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Nodes.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, styleSheet, state ),  elseSuite ) )
		if finallySuite is not None:
			compoundBlocks.append( ( Nodes.FinallyStmtHeader(), self._finallyStmtHeaderElement( ctx, styleSheet, state ),  finallySuite ) )
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )




	# With statement
	@ObjectNodeDispatchMethod( Nodes.WithStmt )
	def WithStmt(self, ctx, styleSheet, state, node, expr, target, suite):
		compoundBlocks = [ ( Nodes.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( ctx, styleSheet, state, expr, target ), suite ) ]
		return compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )



	# Def statement
	@ObjectNodeDispatchMethod( Nodes.DefStmt )
	def DefStmt(self, ctx, styleSheet, state, node, decorators, name, params, paramsTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Nodes.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Nodes.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
						 self._decoStmtHeaderElement( ctx, styleSheet, state, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )
			
		compoundBlocks.append( ( Nodes.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ),
					 self._defStmtHeaderElement( ctx, styleSheet, state, name, params, paramsTrailingSeparator ), suite, lambda header: ctx.border( defHeader_border, ContainerStyleParams.defaultStyleParams, header ) ) )
		editor = compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )
		return ctx.border( defBackground_border, ContainerStyleParams.defaultStyleParams, editor )


	# Class statement
	@ObjectNodeDispatchMethod( Nodes.ClassStmt )
	def ClassStmt(self, ctx, styleSheet, state, node, name, bases, basesTrailingSeparator, suite):
		compoundBlocks = [ ( Nodes.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ),
				     self._classStmtHeaderElement( ctx, styleSheet, state, name, bases, basesTrailingSeparator ), suite, lambda header: ctx.border( classHeader_border, ContainerStyleParams.defaultStyleParams, header ) ) ]
		editor = compoundStatementEditor( ctx, node, PRECEDENCE_STMT,
						compoundBlocks,
						state,
						self._parser.compoundSuite(), self._parser.singleLineStatement() )
		return ctx.border( classBackground_border, ContainerStyleParams.defaultStyleParams, editor )




def viewPython25DocNodeAsElement(document, docRootNode, resolveContext, location, commandHistory, app):
	viewContext = GSymViewContext( docRootNode, Python25View(), PythonEditorStyleSheet.instance, commandHistory )
	editHandler = Python25EditHandler( viewContext )
	frame = viewContext.getFrame()
	frame.setEditHandler( editHandler )
	return frame


def viewPython25DocNodeAsPage(document, docRootNode, resolveContext, location, commandHistory, app):
	return GSymViewPage( 'Python: ' + resolveContext.getTitle(), viewPython25DocNodeAsElement( document, docRootNode, resolveContext, location, commandHistory, app ), commandHistory )


def resolvePython25Location(currentUnitClass, document, docRootNode, resolveContext, location, app):
	return GSymResolveResult( document, docRootNode, currentUnitClass, resolveContext, location )