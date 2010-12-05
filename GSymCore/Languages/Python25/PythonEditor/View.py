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


import sys
import imp

from java.lang import Throwable

from java.awt.event import KeyEvent

from BritefuryJ.Parser import ParserExpression

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch


from Britefury.Util.NodeUtil import *
from Britefury.Util.InstanceCache import instanceCache


from BritefuryJ.DocModel import DMObjectClass

from BritefuryJ.AttributeTable import *
from BritefuryJ.Controls import *
from BritefuryJ.DocPresent import ElementValueFunction, TextEditEvent, DPText
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor
from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.DocPresent.Combinators.Primitive import Paragraph, Segment
from BritefuryJ.GSym.PresCom import InnerFragment, ApplyPerspective

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import GSymFragmentView

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.Language.Precedence import PrecedenceHandler

from BritefuryJ.ModelAccess.DocModel import *



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25 import ExternalExpression


from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.NodeEditor import *
from GSymCore.Languages.Python25.PythonEditor.SequentialEditor import *
from GSymCore.Languages.Python25.PythonEditor.Keywords import *
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditorCombinators import *




DEFAULT_LINE_BREAK_PRIORITY = 100



_statementIndentationInteractor = StatementIndentationInteractor()



_pythonPrecedenceHandler = PrecedenceHandler( ClassAttributeReader( parensRequired ), ObjectFieldReader( 'parens' ).stringToInteger( -1 ), openParen, closeParen )



def computeBinOpViewPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1




def unparsedNodeEditor(grammar, inheritedState, node, precedence, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_DISPLAYCONTENTS:
		contents = _pythonPrecedenceHandler.applyPrecedenceBrackets( node, contents, precedence   if precedence is not None   else -1, inheritedState )
		return contents
	elif mode == EDITMODE_EDITEXPRESSION:
		contents = EditableSequentialItem( instanceCache( ParsedExpressionEditListener, grammar.expression() ),  contents )
		return contents
	elif mode == EDITMODE_EDITSTATEMENT:
		s = statementLine( contents )
		s = EditableSequentialItem( [ instanceCache( StatementEditListener, grammar.singleLineStatementValid() ),
		                              instanceCache( StatementUnparsedEditListener, grammar.unparsed() ) ],  s )
		s = s.withElementInteractor( _statementIndentationInteractor )
		return s
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def expressionNodeEditor(grammar, inheritedState, node, precedence, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_DISPLAYCONTENTS:
		contents = _pythonPrecedenceHandler.applyPrecedenceBrackets( node, contents, precedence   if precedence is not None   else -1, inheritedState )
		return contents
	elif mode == EDITMODE_EDITEXPRESSION:
		contents = _pythonPrecedenceHandler.applyPrecedenceBrackets( node, contents, precedence   if precedence is not None   else -1, inheritedState )
		contents = EditableSequentialItem( instanceCache( ParsedExpressionEditListener, grammar.expression() ),  contents )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def specialFormExpressionNodeEditor(grammar, inheritedState, node, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_DISPLAYCONTENTS  or  mode == EDITMODE_EDITEXPRESSION:
		contents = StructuralItem( node, contents )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def statementNodeEditor(grammar, inheritedState, node, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_EDITSTATEMENT:
		s = statementLine( contents )

		assert not node.isInstanceOf( Schema.UNPARSED )
		s = EditableStructuralItem( [ instanceCache( StatementEditListener, grammar.singleLineStatementValid() ),
		                              instanceCache( StatementUnparsedEditListener, grammar.unparsed() ) ], node, s )
		s = s.withElementInteractor( _statementIndentationInteractor )
		return s
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def specialFormStatementNodeEditor(grammar, inheritedState, node, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_EDITSTATEMENT:
		contents = StructuralItem( node, contents )
		s = specialFormStatementLine( contents )
		s = EditableStructuralItem( [ instanceCache( StatementEditListener, grammar.singleLineStatementValid() ),
		                              instanceCache( StatementUnparsedEditListener, grammar.unparsed() ) ], node, s )
		s = s.withElementInteractor( _statementIndentationInteractor )
		return s
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def compoundStatementHeaderEditor(grammar, inheritedState, node, headerContents, headerContainerFn=None):
	headerStatementLine = statementLine( headerContents )

	headerStatementLine = EditableStructuralItem( [ instanceCache( StatementEditListener, grammar.singleLineStatementValid() ),
	                                                instanceCache( StatementUnparsedEditListener, grammar.unparsed() ) ],  node,  headerStatementLine )
	headerStatementLine = headerStatementLine.withElementInteractor( _statementIndentationInteractor )
	if headerContainerFn is not None:
		headerStatementLine = headerContainerFn( headerStatementLine )
	return headerStatementLine


def compoundStatementEditor(ctx, grammar, inheritedState, node, precedence, compoundBlocks):
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

		headerStatementLine = statementLine( headerContents )
		headerStatementLine = EditableStructuralItem( instanceCache( CompoundHeaderEditListener, statementParser ), headerNode, headerStatementLine )
		headerStatementLine = headerStatementLine.withElementInteractor( _statementIndentationInteractor )

		if headerContainerFn is not None:
			headerStatementLine = headerContainerFn( headerStatementLine )



		if suite is not None:
			indent = StructuralItem( Schema.Indent(), indentElement() )

			lineViews = InnerFragment.map( suite, _withPythonState( inheritedState, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )

			dedent = StructuralItem( Schema.Dedent(), dedentElement() )

			suiteElement = indentedBlock( indent, lineViews, dedent )
			suiteElement = EditableStructuralItem( SuiteEditListener( suiteParser, suite ), Schema.IndentedBlock( suite=suite ), suiteElement )

			statementContents.extend( [ headerStatementLine.alignHExpand(), suiteElement.alignHExpand() ] )
		else:
			statementContents.append( headerStatementLine.alignHExpand() )

	return compoundStmt( statementContents )



def spanPrefixOpView(ctx, grammar, inheritedState, node, x, op, precedence):
	xView = InnerFragment( x, _withPythonState( inheritedState, precedence, EDITMODE_DISPLAYCONTENTS ) )
	view = spanPrefixOp( xView, op )
	return expressionNodeEditor( grammar, inheritedState, node, precedence,
	                             view )


def spanBinOpView(ctx, grammar, inheritedState, node, x, y, op, precedence, bRightAssociative):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( precedence, bRightAssociative )
	xView = InnerFragment( x, _withPythonState( inheritedState, xPrec, EDITMODE_DISPLAYCONTENTS ) )
	yView = InnerFragment( y, _withPythonState( inheritedState, yPrec, EDITMODE_DISPLAYCONTENTS ) )
	view = spanBinOp( xView, yView, op )
	return expressionNodeEditor( grammar, inheritedState, node, precedence,
	                             view )


def spanCmpOpView(ctx, grammar, inheritedState, node, op, y, precedence):
	yView = InnerFragment( y, _withPythonState( inheritedState, precedence, EDITMODE_DISPLAYCONTENTS ) )
	view = spanCmpOp( op, yView )
	return expressionNodeEditor( grammar, inheritedState, node, precedence,
	                             view )



class _InsertSpecialFormTreeEvent (TextEditEvent):
	def __init__(self, leaf):
		super( _InsertSpecialFormTreeEvent, self ).__init__( leaf )


def _insertSpecialForm(caret, specialForm):
	element = caret.getElement()
	index = caret.getIndex()
	assert isinstance( element, DPText )
	
	value = element.getStreamValue()
	builder = StreamValueBuilder()
	builder.append( value[:index] )
	builder.appendStructuralValue( specialForm )
	builder.append( value[index:] )
	modifiedValue = builder.stream()
	
	event = _InsertSpecialFormTreeEvent( element )
	visitor = event.getStreamValueVisitor()
	visitor.setElementFixedValue( element, modifiedValue )

	element.postTreeEvent( event )




def _onDrop_inlineObject(element, pos, data, action):
	def _displayResourceException(e):
		ApplyPerspective( None, Pres.coerce( e ) ).popupAtMousePosition( element, True, True )
	def _displayModelException(e):
		ApplyPerspective( None, Pres.coerce( e ) ).popupAtMousePosition( element, True, True )

	rootElement = element.getRootElement()
	caret = rootElement.getCaret()
	if caret.isValid():
		model = data.getModel()
		try:
			resource = DMNode.resource( model )
		except Exception, e:
			_displayResourceException( e )
		except Throwable, t:
			_displayResourceException( t )
		else:
			try:
				modelType = Schema.getInlineObjectModelType( model )
			except Exception, e:
				_displayModelException( e )
			else:
				if modelType is Schema.Expr:
					expr = Schema.InlineObjectExpr( resource=resource )
					_insertSpecialForm( caret, expr )
				elif modelType is Schema.Stmt:
					stmt = Schema.InlineObjectStmt( resource=resource )
					_insertSpecialForm( caret, stmt )
	return True




def _inlineObjectExprContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		pyReplaceExpression( fragment, model, Schema.Load( name='None' ) )

	deleteItem = MenuItem.menuItemWithLabel( 'Delete inline object', _onDelete )
	menu.add( deleteItem )

	return False


def _inlineObjectStmtContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		pyReplaceStmt( fragment, model, Schema.BlankLine() )

	deleteItem = MenuItem.menuItemWithLabel( 'Delete inline object', _onDelete )
	menu.add( deleteItem )

	return False



def _pythonModuleContextMenuFactory(element, menu):
	rootElement = element.getRootElement()


	extExprItems = []

	def _makeExtExprFn(factory):
		def _onMenuItem(item):
			caret = rootElement.getCaret()
			if caret.isValid():
				expr = factory()
				pyExpr = Schema.ExternalExpr( expr=expr )
				_insertSpecialForm( caret, pyExpr )
		return _onMenuItem

	def _onQuoteExpr(item):
		caret = rootElement.getCaret()
		if caret.isValid():
			pyExpr = Schema.Quote( value=Schema.PythonExpression( expr=Schema.Load( name='None' ) ) )
			_insertSpecialForm( caret, pyExpr )

	def _onQuoteSuite(item):
		caret = rootElement.getCaret()
		if caret.isValid():
			pyExpr = Schema.Quote( value=Schema.PythonSuite( suite=[] ) )
			_insertSpecialForm( caret, pyExpr )

	def _onUnquote(item):
		caret = rootElement.getCaret()
		if caret.isValid():
			pyExpr = Schema.Unquote( value=Schema.PythonExpression( expr=Schema.Load( name='None' ) ) )
			_insertSpecialForm( caret, pyExpr )


	extExprItems = [ MenuItem.menuItemWithLabel( labelText, _makeExtExprFn( factory ) )   for labelText, factory in ExternalExpression.getExternalExpressionFactories() ]
	extExprMenu = VPopupMenu( extExprItems )

	quoteExprMenuItem = MenuItem.menuItemWithLabel( 'Quote expression', _onQuoteExpr )
	menu.add( quoteExprMenuItem )
	quoteSuiteMenuItem = MenuItem.menuItemWithLabel( 'Quote suite', _onQuoteSuite )
	menu.add( quoteSuiteMenuItem )
	unquoteMenuItem = MenuItem.menuItemWithLabel( 'Unquote', _onUnquote )
	menu.add( unquoteMenuItem )

	insertExprMenuItem = MenuItem.menuItemWithLabel( 'Insert expression', extExprMenu, MenuItem.SubmenuPopupDirection.RIGHT )
	menu.add( insertExprMenuItem )
	return True



def _withPythonState(state, precedence, mode=EDITMODE_DISPLAYCONTENTS):
	return state.withAttrs( outerPrecedence=precedence, editMode=mode )


class Python25View (GSymViewObjectNodeDispatch):
	def __init__(self, parser):
		self._parser = parser


	# OUTER NODES
	@DMObjectNodeDispatchMethod( Schema.PythonModule )
	def PythonModule(self, ctx, state, node, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = InnerFragment.map( suite, _withPythonState( state, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )
		s = suiteView( lineViews )
		_inlineObject_dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop_inlineObject )
		s = s.withDropDest( _inlineObject_dropDest )
		s = EditableStructuralItem( [ SuiteEditListener( self._parser.suite(), suite ),
		                              PythonModuleTopLevelEditListener.instance ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonSuite )
	def PythonSuite(self, ctx, state, node, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = InnerFragment.map( suite, _withPythonState( state, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )
		s = suiteView( lineViews )
		_inlineObject_dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop_inlineObject )
		s = s.withDropDest( _inlineObject_dropDest )
		s = EditableStructuralItem( [ SuiteEditListener( self._parser.suite(), suite ),
		                              PythonSuiteTopLevelEditListener.instance ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonExpression )
	def PythonExpression(self, ctx, state, node, expr):
		if expr is None:
			# Empty document - create a single blank line so that there is something to edit
			exprView = blankLine()
			seg = exprView
		else:
			exprView = InnerFragment( expr, _withPythonState( state, PRECEDENCE_NONE, EDITMODE_DISPLAYCONTENTS ) )
			seg = Segment( exprView )
		e = Paragraph( [ seg ] )
		_inlineObject_dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop_inlineObject )
		e = e.withDropDest( _inlineObject_dropDest )
		e = EditableStructuralItem( [ instanceCache( PythonExpressionEditListener, self._parser.expression() ),
		                              PythonExpressionTopLevelEditListener.instance ],  node,  e )
		e = e.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return e



	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, ctx, state, node):
		return statementNodeEditor( self._parser, state, node,
		                            blankLine() )


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, ctx, state, node, value):
		def _viewItem(x):
			if x is node:
				raise ValueError, 'Python25View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseableText( x )
				return view
			elif isinstance( x, DMObjectInterface ):
				view = InnerFragment( x, _withPythonState( state, PRECEDENCE_CONTAINER_UNPARSED, EDITMODE_DISPLAYCONTENTS ) )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( x, view )
				return view
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsedNodeEditor( self._parser, state, node, PRECEDENCE_NONE,
		                           unparsedElements( views ) )





	# Comment statement
	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	def CommentStmt(self, ctx, state, node, comment):
		view = commentStmt( comment )
		return statementNodeEditor( self._parser, state, node,
		                            view )





	# String literal
	__strLit_fmtTable = { 'ascii' : None,  'unicode' : 'u',  'ascii-regex' : 'r',  'unicode-regex' : 'ur' }

	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	def StringLiteral(self, ctx, state, node, format, quotation, value):
		fmt = self.__strLit_fmtTable[format]

		quote = "'"   if quotation == 'single'   else   '"'

		view = stringLiteral( fmt, quote, value )

		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_LITERALVALUE,
		                             view )

	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	def IntLiteral(self, ctx, state, node, format, numType, value):
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

		view = intLiteral( fmt, valueString )

		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_LITERALVALUE,
		                             view )



	# Float literal
	@DMObjectNodeDispatchMethod( Schema.FloatLiteral )
	def FloatLiteral(self, ctx, state, node, value):
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LITERALVALUE,
		                             floatLiteral( value ) )



	# Imaginary literal
	@DMObjectNodeDispatchMethod( Schema.ImaginaryLiteral )
	def ImaginaryLiteral(self, ctx, state, node, value):
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LITERALVALUE,
		                             imaginaryLiteral( value ) )



	# Targets
	@DMObjectNodeDispatchMethod( Schema.SingleTarget )
	def SingleTarget(self, ctx, state, node, name):
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_SINGLETARGET,
		                             singleTarget( name ) )


	@DMObjectNodeDispatchMethod( Schema.TupleTarget )
	def TupleTarget(self, ctx, state, node, targets, trailingSeparator):
		elementViews = InnerFragment.map( targets, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = tupleTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_TUPLE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ListTarget )
	def ListTarget(self, ctx, state, node, targets, trailingSeparator):
		elementViews = InnerFragment.map( targets, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = listTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LISTDISPLAY,
		                             view )




	# Variable reference
	@DMObjectNodeDispatchMethod( Schema.Load )
	def Load(self, ctx, state, node, name):
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LOAD,
		                             load( name ) )



	# Tuple literal
	@DMObjectNodeDispatchMethod( Schema.TupleLiteral )
	def TupleLiteral(self, ctx, state, node, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = tupleLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_TUPLE,
		                             view )



	# List literal
	@DMObjectNodeDispatchMethod( Schema.ListLiteral )
	def ListLiteral(self, ctx, state, node, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = listLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LISTDISPLAY,
		                             view )



	# List comprehension / generator expression
	@DMObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, ctx, state, node, target, source):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_CONTAINER_COMPREHENSIONFOR) )
		sourceView = InnerFragment( source, _withPythonState( state, PRECEDENCE_CONTAINER_COMPREHENSIONFOR ) )
		view = comprehensionFor( targetView, sourceView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, ctx, state, node, condition):
		conditionView = InnerFragment( condition, _withPythonState( state, PRECEDENCE_CONTAINER_COMPREHENSIONIF ) )
		view = comprehensionIf( conditionView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ListComp )
	def ListComp(self, ctx, state, node, resultExpr, comprehensionItems):
		exprView = InnerFragment( resultExpr, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		itemViews = InnerFragment.map( comprehensionItems, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = listComp( exprView, itemViews )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LISTDISPLAY,
		                             view )


	@DMObjectNodeDispatchMethod( Schema.GeneratorExpr )
	def GeneratorExpr(self, ctx, state, node, resultExpr, comprehensionItems):
		exprView = InnerFragment( resultExpr, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		itemViews = InnerFragment.map( comprehensionItems, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = genExpr( exprView, itemViews )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LISTDISPLAY,
		                             view )




	# Dictionary literal
	@DMObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, ctx, state, node, key, value):
		keyView = InnerFragment( key, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = dictKeyValuePair( keyView, valueView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.DictLiteral )
	def DictLiteral(self, ctx, state, node, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = dictLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_DICTDISPLAY,
		                             view )


	# Yield expression
	@DMObjectNodeDispatchMethod( Schema.YieldExpr )
	def YieldExpr(self, ctx, state, node, value):
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_YIELDEXPR ) )
		view = yieldExpr( valueView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_YIELDEXPR,
		                             view )



	# Attribute ref
	@DMObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, ctx, state, node, target, name):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET ) )
		view = attributeRef( targetView, name )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_ATTR,
		                             view )



	# Subscript
	@DMObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, ctx, state, node, lower, upper):
		lowerView = InnerFragment( lower, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if lower is not None   else None
		upperView = InnerFragment( upper, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if upper is not None   else None
		view = subscriptSlice( lowerView, upperView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, ctx, state, node, lower, upper, stride):
		lowerView = InnerFragment( lower, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if lower is not None   else None
		upperView = InnerFragment( upper, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if upper is not None   else None
		strideView = InnerFragment( stride, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if stride is not None   else None
		view = subscriptLongSlice( lowerView, upperView, strideView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, ctx, state, node):
		view = subscriptEllipsis()
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.SubscriptTuple )
	def SubscriptTuple(self, ctx, state, node, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = subscriptTuple( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_TUPLE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, ctx, state, node, target, index):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET ) )
		indexView = InnerFragment( index, _withPythonState( state, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )
		view = subscript( targetView, indexView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_SUBSCRIPT,
		                             view )




	# Call
	@DMObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, ctx, state, node, name, value):
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_CALLARG ) )
		view = callKWArg( name, valueView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, ctx, state, node, value):
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_CALLARG ) )
		view = callArgList( valueView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, ctx, state, node, value):
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_CALLARG ) )
		view = callKWArgList( valueView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.Call )
	def Call(self, ctx, state, node, target, args, argsTrailingSeparator):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_CONTAINER_CALLTARGET ) )
		argViews = InnerFragment.map( args, _withPythonState( state, PRECEDENCE_CONTAINER_CALLARG ) )
		view = call( targetView, argViews, argsTrailingSeparator is not None )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_CALL,
		                             view )





	# Operators
	@DMObjectNodeDispatchMethod( Schema.Pow )
	def Pow(self, ctx, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = InnerFragment( x, _withPythonState( state, xPrec ) )
		yView = InnerFragment( y, _withPythonState( state, yPrec, EDITMODE_EDITEXPRESSION ) )
		view = exponent( xView, yView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_POW,
		                             view )


	@DMObjectNodeDispatchMethod( Schema.Invert )
	def Invert(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, self._parser, state, node, x, '~', PRECEDENCE_INVERT_NEGATE_POS )

	@DMObjectNodeDispatchMethod( Schema.Negate )
	def Negate(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, self._parser, state, node, x, '-', PRECEDENCE_INVERT_NEGATE_POS )

	@DMObjectNodeDispatchMethod( Schema.Pos )
	def Pos(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, self._parser, state, node, x, '+', PRECEDENCE_INVERT_NEGATE_POS )


	@DMObjectNodeDispatchMethod( Schema.Mul )
	def Mul(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '*', PRECEDENCE_MULDIVMOD, False )

	@DMObjectNodeDispatchMethod( Schema.Div )
	def Div(self, ctx, state, node, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = InnerFragment( x, _withPythonState( state, xPrec, EDITMODE_EDITEXPRESSION ) )
		yView = InnerFragment( y, _withPythonState( state, yPrec, EDITMODE_EDITEXPRESSION ) )
		#<NO_TREE_EVENT_LISTENER>
		view = div( xView, yView, '/' )
		view = BreakableStructuralItem( PythonSequentialEditor.instance, node, view )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_MULDIVMOD,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.Mod )
	def Mod(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '%', PRECEDENCE_MULDIVMOD, False )


	@DMObjectNodeDispatchMethod( Schema.Add )
	def Add(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '+', PRECEDENCE_ADDSUB, False )

	@DMObjectNodeDispatchMethod( Schema.Sub )
	def Sub(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '-', PRECEDENCE_ADDSUB, False )


	@DMObjectNodeDispatchMethod( Schema.LShift )
	def LShift(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '<<', PRECEDENCE_SHIFT, False )

	@DMObjectNodeDispatchMethod( Schema.RShift )
	def RShift(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '>>', PRECEDENCE_SHIFT, False )


	@DMObjectNodeDispatchMethod( Schema.BitAnd )
	def BitAnd(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '&', PRECEDENCE_BITAND, False )

	@DMObjectNodeDispatchMethod( Schema.BitXor )
	def BitXor(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '^', PRECEDENCE_BITXOR, False )

	@DMObjectNodeDispatchMethod( Schema.BitOr )
	def BitOr(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, '|', PRECEDENCE_BITOR, False )


	@DMObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, ctx, state, node, x, ops):
		xView = InnerFragment( x, _withPythonState( state, PRECEDENCE_CMP ) )
		opViews = InnerFragment.map( ops, _withPythonState( state, PRECEDENCE_CMP ) )
		view = compare( xView, opViews )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_CMP,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.CmpOpLte )
	def CmpOpLte(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, '<=', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpLt )
	def CmpOpLt(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, '<', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpGte )
	def CmpOpGte(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, '>=', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpGt )
	def CmpOpGt(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, '>', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpEq )
	def CmpOpEq(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, '==', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpNeq )
	def CmpOpNeq(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, '!=', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, 'is not', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpIs )
	def CmpOpIs(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, 'is', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, 'not in', y, PRECEDENCE_CMP )

	@DMObjectNodeDispatchMethod( Schema.CmpOpIn )
	def CmpOpIn(self, ctx, state, node, y):
		return spanCmpOpView( ctx, self._parser, state, node, 'in', y, PRECEDENCE_CMP )



	@DMObjectNodeDispatchMethod( Schema.NotTest )
	def NotTest(self, ctx, state, node, x):
		return spanPrefixOpView( ctx, self._parser, state, node, x, 'not ', PRECEDENCE_NOT )

	@DMObjectNodeDispatchMethod( Schema.AndTest )
	def AndTest(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, 'and', PRECEDENCE_AND, False )

	@DMObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, ctx, state, node, x, y):
		return spanBinOpView( ctx, self._parser, state, node, x, y, 'or', PRECEDENCE_OR, False )





	# Parameters
	@DMObjectNodeDispatchMethod( Schema.SimpleParam )
	def SimpleParam(self, ctx, state, node, name):
		view = simpleParam( name )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.DefaultValueParam )
	def DefaultValueParam(self, ctx, state, node, name, defaultValue):
		valueView = InnerFragment( defaultValue, _withPythonState( state, PRECEDENCE_NONE ) )
		view = defaultValueParam( name, valueView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ParamList )
	def ParamList(self, ctx, state, node, name):
		view = paramList( name )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.KWParamList )
	def KWParamList(self, ctx, state, node, name):
		view = kwParamList( name )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_NONE,
		                             view )



	# Lambda expression
	@DMObjectNodeDispatchMethod( Schema.LambdaExpr )
	def LambdaExpr(self, ctx, state, node, params, paramsTrailingSeparator, expr):
		exprView = InnerFragment( expr, _withPythonState( state, PRECEDENCE_CONTAINER_LAMBDAEXPR ) )
		paramViews = InnerFragment.map( params, _withPythonState( state, PRECEDENCE_NONE ) )

		view = lambdaExpr( paramViews, paramsTrailingSeparator is not None, exprView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_LAMBDAEXPR,
		                             view )



	# Conditional expression
	@DMObjectNodeDispatchMethod( Schema.ConditionalExpr )
	def ConditionalExpr(self, ctx, state, node, condition, expr, elseExpr):
		conditionView = InnerFragment( condition, _withPythonState( state, PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		exprView = InnerFragment( expr, _withPythonState( state, PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		elseExprView = InnerFragment( elseExpr, _withPythonState( state, PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		view = conditionalExpr( conditionView, exprView, elseExprView )
		return expressionNodeEditor( self._parser, state, node,
		                             PRECEDENCE_CONDITIONAL,
		                             view )




	#
	#
	# QUOTE AND UNQUOTE
	#
	#

	# Quote
	@DMObjectNodeDispatchMethod( Schema.Quote )
	def Quote(self, ctx, state, node, value):
		if isinstance( value, DMObject ):
			if value.isInstanceOf( Schema.PythonExpression ):
				title = 'QUOTE - Expr'
			elif value.isInstanceOf( Schema.PythonSuite ):
				title = 'QUOTE - Suite'
			else:
				raise TypeError, 'Contents of \'quote\' should be a PythonExpression or a PythonSuite'

			valueView = perspective.applyTo( InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_QUOTE ) ) )
		else:
			raise TypeError, 'Value of \'quote\' should be a DMObject'


		view = quote( valueView, title, PythonSequentialEditor.instance )
		return specialFormExpressionNodeEditor( self._parser, state, node,
		                                        view )



	# Unquote
	@DMObjectNodeDispatchMethod( Schema.Unquote )
	def Unquote(self, ctx, state, node, value):
		if isinstance( value, DMObject ):
			valueView = perspective.applyTo( InnerFragment( value, _withPythonState( state, PRECEDENCE_CONTAINER_QUOTE ) ) )
		else:
			raise TypeError, 'Value of \'unquote\' should be a DMObject'


		view = unquote( valueView, 'UNQUOTE', PythonSequentialEditor.instance )
		return specialFormExpressionNodeEditor( self._parser, state, node,
		                                        view )



	#
	#
	# EXTERNAL EXPRESSION
	#
	#

	# External expression
	@DMObjectNodeDispatchMethod( Schema.ExternalExpr )
	def ExternalExpr(self, ctx, state, node, expr):
		if isinstance( expr, DMObject ):
			schema = expr.getDMObjectClass().getSchema()
			presenter, title = ExternalExpression.getExternalExpressionPresenterAndTitle( schema )
			exprView = presenter( expr, _withPythonState( state, PRECEDENCE_CONTAINER_EXTERNALEXPR ) )
		else:
			exprView = Label( '<expr>' )
			title = 'ext'

		def _onDeleteButton(button, event):
			pyReplaceExpression( ctx, node, Schema.Load( name='None' ) )


		deleteButton = Button( Image.systemIcon( 'delete_tiny' ), _onDeleteButton )

		view = externalExpr( exprView, title, deleteButton )
		return specialFormExpressionNodeEditor( self._parser, state, node,
		                                        view )



	#
	#
	# INLINE OBJECT
	#
	#

	# Inline object expression
	@DMObjectNodeDispatchMethod( Schema.InlineObjectExpr )
	def InlineObjectExpr(self, ctx, state, node, resource):
		value = resource.getValue()
		valueView = ApplyPerspective( None, value )

		try:
			modelFn = value.__py_model__
		except AttributeError:
			# Standard view
			view = inlineObject( valueView )
			view = view.withContextMenuInteractor( _inlineObjectExprContextMenuFactory )
			return specialFormExpressionNodeEditor( self._parser, state, node,
			                                        view )
		else:
			# Macro view
			def createModelView():
				return Pres.coerce( modelFn() )
			view = inlineObjectMacro( valueView, LazyPres( createModelView ) )
			view = view.withContextMenuInteractor( _inlineObjectExprContextMenuFactory )
			return specialFormExpressionNodeEditor( self._parser, state, node,
			                                        view )


	# Inline object statement
	@DMObjectNodeDispatchMethod( Schema.InlineObjectStmt )
	def InlineObjectStmt(self, ctx, state, node, resource):
		value = resource.getValue()
		valueView = ApplyPerspective( None, value )

		try:
			modelFn = value.__py_model__
		except AttributeError:
			# Standard view
			view = inlineObject( valueView )
			view = view.withContextMenuInteractor( _inlineObjectStmtContextMenuFactory )
			return specialFormStatementNodeEditor( self._parser, state, node,
			                                       view )
		else:
			# Macro view
			def createModelView():
				return Pres.coerce( modelFn() )
			view = inlineObjectMacro( valueView, LazyPres( createModelView ) )
			view = view.withContextMenuInteractor( _inlineObjectStmtContextMenuFactory )
			return specialFormStatementNodeEditor( self._parser, state, node,
			                                       view )



	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Expression statement
	@DMObjectNodeDispatchMethod( Schema.ExprStmt )
	def ExprStmt(self, ctx, state, node, expr):
		exprView = InnerFragment( expr, _withPythonState( state, PRECEDENCE_STMT ) )
		view = exprStmt( exprView )
		return statementNodeEditor( self._parser, state, node,
		                            view )



	# Assert statement
	@DMObjectNodeDispatchMethod( Schema.AssertStmt )
	def AssertStmt(self, ctx, state, node, condition, fail):
		conditionView = InnerFragment( condition, _withPythonState( state, PRECEDENCE_STMT ) )
		failView = InnerFragment( fail, _withPythonState( state, PRECEDENCE_STMT ) )   if fail is not None   else None
		view = assertStmt( conditionView, failView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Assignment statement
	@DMObjectNodeDispatchMethod( Schema.AssignStmt )
	def AssignStmt(self, ctx, state, node, targets, value):
		targetViews = InnerFragment.map( targets, _withPythonState( state, PRECEDENCE_STMT ) )
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_STMT ) )
		view = assignStmt( targetViews, valueView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Augmented assignment statement
	@DMObjectNodeDispatchMethod( Schema.AugAssignStmt )
	def AugAssignStmt(self, ctx, state, node, op, target, value):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_STMT ) )
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_STMT ) )
		view = augAssignStmt( op, targetView, valueView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Pass statement
	@DMObjectNodeDispatchMethod( Schema.PassStmt )
	def PassStmt(self, ctx, state, node):
		view = passStmt()
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Del statement
	@DMObjectNodeDispatchMethod( Schema.DelStmt )
	def DelStmt(self, ctx, state, node, target):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_STMT ) )
		view = delStmt( targetView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Return statement
	@DMObjectNodeDispatchMethod( Schema.ReturnStmt )
	def ReturnStmt(self, ctx, state, node, value):
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_STMT ) )
		view = returnStmt( valueView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Yield statement
	@DMObjectNodeDispatchMethod( Schema.YieldStmt )
	def YieldStmt(self, ctx, state, node, value):
		valueView = InnerFragment( value, _withPythonState( state, PRECEDENCE_STMT ) )
		view = yieldStmt( valueView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Raise statement
	@DMObjectNodeDispatchMethod( Schema.RaiseStmt )
	def RaiseStmt(self, ctx, state, node, excType, excValue, traceback):
		excTypeView = InnerFragment( excType, _withPythonState( state, PRECEDENCE_STMT ) )   if excType is not None   else None
		excValueView = InnerFragment( excValue, _withPythonState( state, PRECEDENCE_STMT ) )   if excValue is not None   else None
		tracebackView = InnerFragment( traceback, _withPythonState( state, PRECEDENCE_STMT ) )   if traceback is not None   else None
		view = raiseStmt( excTypeView, excValueView, tracebackView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Break statement
	@DMObjectNodeDispatchMethod( Schema.BreakStmt )
	def BreakStmt(self, ctx, state, node):
		view = breakStmt()
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Continue statement
	@DMObjectNodeDispatchMethod( Schema.ContinueStmt )
	def ContinueStmt(self, ctx, state, node):
		view = continueStmt()
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Import statement
	@DMObjectNodeDispatchMethod( Schema.RelativeModule )
	def RelativeModule(self, ctx, state, node, name):
		view = relativeModule( name )
		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_IMPORTCONTENT,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleImport )
	def ModuleImport(self, ctx, state, node, name):
		view = moduleImport( name )
		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_IMPORTCONTENT,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleImportAs )
	def ModuleImportAs(self, ctx, state, node, name, asName):
		view = moduleImportAs( name, asName )
		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_IMPORTCONTENT,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleContentImport )
	def ModuleContentImport(self, ctx, state, node, name):
		view = moduleContentImport( name )
		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_IMPORTCONTENT,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleContentImportAs )
	def ModuleContentImportAs(self, ctx, state, node, name, asName):
		view = moduleContentImportAs( name, asName )
		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_IMPORTCONTENT,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ImportStmt )
	def ImportStmt(self, ctx, state, node, modules):
		moduleViews = InnerFragment.map( modules, _withPythonState( state, PRECEDENCE_STMT ) )
		view = importStmt( moduleViews )
		return statementNodeEditor( self._parser, state, node,
		                            view )

	@DMObjectNodeDispatchMethod( Schema.FromImportStmt )
	def FromImportStmt(self, ctx, state, node, module, imports):
		moduleView = InnerFragment( module, _withPythonState( state, PRECEDENCE_STMT ) )
		importViews = InnerFragment.map( imports, _withPythonState( state, PRECEDENCE_STMT ) )
		view = fromImportStmt( moduleView, importViews )
		return statementNodeEditor( self._parser, state, node,
		                            view )

	@DMObjectNodeDispatchMethod( Schema.FromImportAllStmt )
	def FromImportAllStmt(self, ctx, state, node, module):
		moduleView = InnerFragment( module, _withPythonState( state, PRECEDENCE_STMT ) )
		view = fromImportAllStmt( moduleView )
		return statementNodeEditor( self._parser, state, node,
		                            view )


	# Global statement
	@DMObjectNodeDispatchMethod( Schema.GlobalVar )
	def GlobalVar(self, ctx, state, node, name):
		view = globalVar( name )
		return expressionNodeEditor( self._parser, state, node, PRECEDENCE_NONE,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.GlobalStmt )
	def GlobalStmt(self, ctx, state, node, vars):
		varViews = InnerFragment.map( vars, _withPythonState( state, PRECEDENCE_STMT ) )
		view = globalStmt( varViews )
		return statementNodeEditor( self._parser, state, node,
		                            view )



	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.ExecStmt )
	def ExecStmt(self, ctx, state, node, source, globals, locals):
		sourceView = InnerFragment( source, _withPythonState( state, PRECEDENCE_STMT ) )
		globalsView = InnerFragment( globals, _withPythonState( state, PRECEDENCE_STMT ) )    if globals is not None   else None
		localsView = InnerFragment( locals, _withPythonState( state, PRECEDENCE_STMT ) )   if locals is not None   else None
		view = execStmt( sourceView, globalsView, localsView )
		return statementNodeEditor( self._parser, state, node,
		                            view )






	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.PrintStmt )
	def PrintStmt(self, ctx, state, node, destination, values):
		destView = InnerFragment( destination, _withPythonState( state, PRECEDENCE_STMT ) )   if destination is not None   else None
		valueViews = InnerFragment.map( values, _withPythonState( state, PRECEDENCE_STMT ) )
		view = printStmt( destView, valueViews )
		return statementNodeEditor( self._parser, state, node,
		                            view )




	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def _ifStmtHeaderElement(self, ctx, state, condition):
		conditionView = InnerFragment( condition, _withPythonState( state, PRECEDENCE_STMT ) )
		return ifStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.IfStmtHeader )
	def IfStmtHeader(self, ctx, state, node, condition):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._ifStmtHeaderElement( ctx, state, condition ) )


	# Elif statement
	def _elifStmtHeaderElement(self, ctx, state, condition):
		conditionView = InnerFragment( condition, _withPythonState( state, PRECEDENCE_STMT ) )
		return elifStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.ElifStmtHeader )
	def ElifStmtHeader(self, ctx, state, node, condition):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._elifStmtHeaderElement( ctx, state, condition ) )



	# Else statement
	def _elseStmtHeaderElement(self, ctx, state):
		return elseStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.ElseStmtHeader )
	def ElseStmtHeader(self, ctx, state, node):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._elseStmtHeaderElement( ctx, state ) )


	# While statement
	def _whileStmtHeaderElement(self, ctx, state, condition):
		conditionView = InnerFragment( condition, _withPythonState( state, PRECEDENCE_STMT ) )
		return whileStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.WhileStmtHeader )
	def WhileStmtHeader(self, ctx, state, node, condition):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._whileStmtHeaderElement( ctx, state, condition ) )


	# For statement
	def _forStmtHeaderElement(self, ctx, state, target, source):
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_STMT ) )
		sourceView = InnerFragment( source, _withPythonState( state, PRECEDENCE_STMT ) )
		return forStmtHeader( targetView, sourceView )

	@DMObjectNodeDispatchMethod( Schema.ForStmtHeader )
	def ForStmtHeader(self, ctx, state, node, target, source):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._forStmtHeaderElement( ctx, state, target, source ) )



	# Try statement
	def _tryStmtHeaderElement(self, ctx, state):
		return tryStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.TryStmtHeader )
	def TryStmtHeader(self, ctx, state, node):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._tryStmtHeaderElement( ctx, state ) )



	# Except statement
	def _exceptStmtHeaderElement(self, ctx, state, exception, target):
		excView = InnerFragment( exception, _withPythonState( state, PRECEDENCE_STMT ) )   if exception is not None   else None
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_STMT ) )   if target is not None   else None
		return exceptStmtHeader( excView, targetView )

	@DMObjectNodeDispatchMethod( Schema.ExceptStmtHeader )
	def ExceptStmtHeader(self, ctx, state, node, exception, target):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._exceptStmtHeaderElement( ctx, state, exception, target ) )



	# Finally statement
	def _finallyStmtHeaderElement(self, ctx, state):
		return finallyStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.FinallyStmtHeader )
	def FinallyStmtHeader(self, ctx, state, node):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._finallyStmtHeaderElement( ctx, state ) )



	# With statement
	def _withStmtHeaderElement(self, ctx, state, expr, target):
		exprView = InnerFragment( expr, _withPythonState( state, PRECEDENCE_STMT ) )
		targetView = InnerFragment( target, _withPythonState( state, PRECEDENCE_STMT ) )   if target is not None   else None
		return withStmtHeader( exprView, targetView )

	@DMObjectNodeDispatchMethod( Schema.WithStmtHeader )
	def WithStmtHeader(self, ctx, state, node, expr, target):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._withStmtHeaderElement( ctx, state, expr, target ) )



	# Decorator statement
	def _decoStmtHeaderElement(self, ctx, state, name, args, argsTrailingSeparator):
		argViews = InnerFragment.map( args, _withPythonState( state, PRECEDENCE_STMT ) )   if args is not None   else None
		return decoStmtHeader( name, argViews, argsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DecoStmtHeader )
	def DecoStmtHeader(self, ctx, state, node, name, args, argsTrailingSeparator):
		return compoundStatementHeaderEditor( self._parser, state, node,
		                                      self._decoStmtHeaderElement( ctx, state, name, args, argsTrailingSeparator ) )



	# Def statement
	def _defStmtHeaderElement(self, ctx, state, name, params, paramsTrailingSeparator):
		paramViews = InnerFragment.map( params, _withPythonState( state, PRECEDENCE_STMT ) )
		return defStmtHeader( name, paramViews, paramsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DefStmtHeader )
	def DefStmtHeader(self, ctx, state, node, name, params, paramsTrailingSeparator):
		editor = compoundStatementHeaderEditor( self._parser, state, node,
		                                        self._defStmtHeaderElement( ctx, state, name, params, paramsTrailingSeparator ),
		                                        lambda header: defStmtHeaderHighlight( header ) )
		return defStmtHighlight( editor )


	# Def statement
	def _classStmtHeaderElement(self, ctx, state, name, bases, basesTrailingSeparator):
		baseViews = InnerFragment.map( bases, _withPythonState( state, PRECEDENCE_CONTAINER_ELEMENT ) )   if bases is not None   else None
		return classStmtHeader( name, baseViews, basesTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.ClassStmtHeader )
	def ClassStmtHeader(self, ctx, state, node, name, bases, basesTrailingSeparator):
		editor = compoundStatementHeaderEditor( self._parser, state, node,
		                                        self._classStmtHeaderElement( ctx, state, name, bases, basesTrailingSeparator ),
		                                        lambda header: classStmtHeaderHighlight( header ) )
		return classStmtHighlight( editor )




	#
	#
	# STRUCTURE STATEMENTS
	#
	#

	# Indented block
	@DMObjectNodeDispatchMethod( Schema.IndentedBlock )
	def IndentedBlock(self, ctx, state, node, suite):
		indent = StructuralItem( Schema.Indent(), indentElement() )

		lineViews = InnerFragment.map( suite, _withPythonState( state, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )

		dedent = StructuralItem( Schema.Dedent(), dedentElement() )

		suiteElement = indentedBlock( indent, lineViews, dedent )
		suiteElement = EditableStructuralItem( SuiteEditListener( self._parser.compoundSuite(), suite ), node, suiteElement )

		return badIndentation( suiteElement )





	#
	#
	# COMPOUND STATEMENTS
	#
	#

	# If statement
	@DMObjectNodeDispatchMethod( Schema.IfStmt )
	def IfStmt(self, ctx, state, node, condition, suite, elifBlocks, elseSuite):
		compoundBlocks = [ ( Schema.IfStmtHeader( condition=condition ), self._ifStmtHeaderElement( ctx, state, condition ), suite ) ]
		for b in elifBlocks:
			if not b.isInstanceOf( Schema.ElifBlock ):
				raise TypeError, 'IfStmt elifBlocks should only contain ElifBlock instances'
			compoundBlocks.append( ( Schema.ElifStmtHeader( condition=b['condition'] ), self._elifStmtHeaderElement( ctx, state, b['condition'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                compoundBlocks )



	# While statement
	@DMObjectNodeDispatchMethod( Schema.WhileStmt )
	def WhileStmt(self, ctx, state, node, condition, suite, elseSuite):
		compoundBlocks = [ ( Schema.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( ctx, state, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                compoundBlocks )



	# For statement
	@DMObjectNodeDispatchMethod( Schema.ForStmt )
	def ForStmt(self, ctx, state, node, target, source, suite, elseSuite):
		compoundBlocks = [ ( Schema.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( ctx, state, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		return compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                compoundBlocks )



	# Try statement
	@DMObjectNodeDispatchMethod( Schema.TryStmt )
	def TryStmt(self, ctx, state, node, suite, exceptBlocks, elseSuite, finallySuite):
		compoundBlocks = [ ( Schema.TryStmtHeader(), self._tryStmtHeaderElement( ctx, state ), suite ) ]
		for b in exceptBlocks:
			if not b.isInstanceOf( Schema.ExceptBlock ):
				raise TypeError, 'TryStmt elifBlocks should only contain ExceptBlock instances'
			compoundBlocks.append( ( Schema.ExceptStmtHeader( exception=b['exception'], target=b['target'] ), self._exceptStmtHeaderElement( ctx, state, b['exception'], b['target'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( ctx, state ),  elseSuite ) )
		if finallySuite is not None:
			compoundBlocks.append( ( Schema.FinallyStmtHeader(), self._finallyStmtHeaderElement( ctx, state ),  finallySuite ) )
		return compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                compoundBlocks )




	# With statement
	@DMObjectNodeDispatchMethod( Schema.WithStmt )
	def WithStmt(self, ctx, state, node, expr, target, suite):
		compoundBlocks = [ ( Schema.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( ctx, state, expr, target ), suite ) ]
		return compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                compoundBlocks )



	# Def statement
	@DMObjectNodeDispatchMethod( Schema.DefStmt )
	def DefStmt(self, ctx, state, node, decorators, name, params, paramsTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Schema.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Schema.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
			                         self._decoStmtHeaderElement( ctx, state, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )

		compoundBlocks.append( ( Schema.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ),
		                         self._defStmtHeaderElement( ctx, state, name, params, paramsTrailingSeparator ), suite,
		                         lambda header: defStmtHeaderHighlight( header ) ) )
		editor = compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                  compoundBlocks )
		return defStmtHighlight( editor )


	# Class statement
	@DMObjectNodeDispatchMethod( Schema.ClassStmt )
	def ClassStmt(self, ctx, state, node, name, bases, basesTrailingSeparator, suite):
		compoundBlocks = [ ( Schema.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ),
		                     self._classStmtHeaderElement( ctx, state, name, bases, basesTrailingSeparator ), suite,
		                     lambda header: classStmtHeaderHighlight( header ) ) ]
		editor = compoundStatementEditor( ctx, self._parser, state, node, PRECEDENCE_STMT,
		                                  compoundBlocks )
		return classStmtHighlight( editor )





_parser = Python25Grammar()
perspective = SequentialEditorPerspective( Python25View( _parser ), PythonSequentialEditor.instance )



