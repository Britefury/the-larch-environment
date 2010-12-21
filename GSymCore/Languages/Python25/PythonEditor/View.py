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
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler

from BritefuryJ.ModelAccess.DocModel import *



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25 import ExternalExpression


from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.NodeEditor import *
from GSymCore.Languages.Python25.PythonEditor.SREditor import *
from GSymCore.Languages.Python25.PythonEditor.Keywords import *
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditorCombinators import *




EDITMODE_DISPLAYCONTENTS = 0
EDITMODE_EDITEXPRESSION = 1
EDITMODE_EDITSTATEMENT = 2



DEFAULT_LINE_BREAK_PRIORITY = 100



_statementIndentationInteractor = StatementIndentationInteractor()



_pythonPrecedenceHandler = PrecedenceHandler( ClassAttributeReader( parensRequired ), ObjectFieldReader( 'parens' ).stringToInteger( -1 ), openParen, closeParen )



def computeBinOpViewPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1



def _makeSuiteCommitFn(suite):
	def _commit(model, parsed):
		modifySuiteMinimisingChanges( suite, parsed )
	return _commit

def _isValidUnparsedStatementValue(value):
	i = value.indexOf( '\n' )
	return i != -1   and   i == len( value ) - 1

def _commitUnparsedStatment(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	pyReplaceNode( model, unparsed )



def unparsedNodeEditor(grammar, inheritedState, model, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_DISPLAYCONTENTS:
		return contents
	elif mode == EDITMODE_EDITEXPRESSION:
		return EditableSequentialItem( PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Expression', grammar.expression(), pyReplaceNode ),  contents )
	elif mode == EDITMODE_EDITSTATEMENT:
		s = statementLine( contents )
		s = EditableSequentialItem( [ PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Statement', grammar.simpleSingleLineStatementValid(), pyReplaceNode ),
		                              PythonSyntaxRecognizingEditor.instance.partialParsingNodeEditListener( 'Compound header', grammar.compoundStmtHeader() ),
		                              PythonSyntaxRecognizingEditor.instance.unparsedNodeEditListener( 'Statement', _isValidUnparsedStatementValue, _commitUnparsedStatment ) ],  s )
		s = s.withElementInteractor( _statementIndentationInteractor )
		return s
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def expressionNodeEditor(grammar, inheritedState, model, contents):
	mode = inheritedState['editMode']
	precedence = nodePrecedence[model]
	if mode == EDITMODE_DISPLAYCONTENTS:
		contents = _pythonPrecedenceHandler.applyPrecedenceBrackets( model, contents, precedence   if precedence is not None   else -1, inheritedState )
		return contents
	elif mode == EDITMODE_EDITEXPRESSION:
		contents = _pythonPrecedenceHandler.applyPrecedenceBrackets( model, contents, precedence   if precedence is not None   else -1, inheritedState )
		contents = EditableSequentialItem( PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Expression', grammar.expression(), pyReplaceNode ),  contents )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def statementNodeEditor(grammar, inheritedState, model, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_EDITSTATEMENT:
		s = statementLine( contents )

		assert not model.isInstanceOf( Schema.UNPARSED )
		s = EditableStructuralItem( [ PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Statement', grammar.simpleSingleLineStatementValid(), pyReplaceNode ),
		                              PythonSyntaxRecognizingEditor.instance.partialParsingNodeEditListener( 'Compound header', grammar.compoundStmtHeader() ),
		                              PythonSyntaxRecognizingEditor.instance.unparsedNodeEditListener( 'Statement', _isValidUnparsedStatementValue, _commitUnparsedStatment ) ], model, s )
		s = s.withElementInteractor( _statementIndentationInteractor )
		return s
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def compoundStatementHeaderEditor(grammar, inheritedState, model, headerContents, headerContainerFn=None):
	headerStatementLine = statementLine( headerContents )

	headerStatementLine = EditableStructuralItem( [
		                              PythonSyntaxRecognizingEditor.instance.partialParsingNodeEditListener( 'Compound header', grammar.compoundStmtHeader() ),
	                                      PythonSyntaxRecognizingEditor.instance.unparsedNodeEditListener( 'Statement', _isValidUnparsedStatementValue, _commitUnparsedStatment ) ], model, headerStatementLine )
	headerStatementLine = headerStatementLine.withElementInteractor( _statementIndentationInteractor )
	if headerContainerFn is not None:
		headerStatementLine = headerContainerFn( headerStatementLine )
	return headerStatementLine


def compoundStatementEditor(grammar, inheritedState, model, compoundBlocks):
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
		headerStatementLine = BreakableStructuralItem( PythonSyntaxRecognizingEditor.instance, headerNode, headerStatementLine )
		headerStatementLine = headerStatementLine.withElementInteractor( _statementIndentationInteractor )

		if headerContainerFn is not None:
			headerStatementLine = headerContainerFn( headerStatementLine )



		if suite is not None:
			indent = StructuralItem( Schema.Indent(), indentElement() )

			lineViews = InnerFragment.map( suite, _withPythonState( inheritedState, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )

			dedent = StructuralItem( Schema.Dedent(), dedentElement() )

			suiteElement = indentedBlock( indent, lineViews, dedent )
			suiteElement = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', suiteParser, _makeSuiteCommitFn( suite ) ),
			                                       Schema.IndentedBlock( suite=suite ), suiteElement )

			statementContents.extend( [ headerStatementLine.alignHExpand(), suiteElement.alignHExpand() ] )
		else:
			statementContents.append( headerStatementLine.alignHExpand() )

	return compoundStmt( statementContents )



def specialFormExpressionNodeEditor(grammar, inheritedState, model, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_DISPLAYCONTENTS  or  mode == EDITMODE_EDITEXPRESSION:
		contents = StructuralItem( model, contents )
		return contents
	else:
		raise ValueError, 'invalid mode %d'  %  mode


def specialFormStatementNodeEditor(grammar, inheritedState, model, contents):
	mode = inheritedState['editMode']
	if mode == EDITMODE_EDITSTATEMENT:
		contents = StructuralItem( model, contents )
		s = specialFormStatementLine( contents )
		s = EditableStructuralItem( [ PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Statement', grammar.simpleSingleLineStatementValid(), pyReplaceNode ),
		                              PythonSyntaxRecognizingEditor.instance.partialParsingNodeEditListener( 'Compound header', grammar.compoundStmtHeader() ),
		                              PythonSyntaxRecognizingEditor.instance.unparsedNodeEditListener( 'Statement', _isValidUnparsedStatementValue, _commitUnparsedStatment ) ], model, s )
		s = s.withElementInteractor( _statementIndentationInteractor )
		return s
	else:
		raise ValueError, 'invalid mode %d'  %  mode



def spanPrefixOpView(grammar, inheritedState, model, x, op):
	xView = InnerFragment( x, _withPythonState( inheritedState, nodePrecedence[model], EDITMODE_DISPLAYCONTENTS ) )
	view = spanPrefixOp( xView, op )
	return expressionNodeEditor( grammar, inheritedState, model,
	                             view )


def spanBinOpView(grammar, inheritedState, model, x, y, op, bRightAssociative):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( nodePrecedence[model], bRightAssociative )
	xView = InnerFragment( x, _withPythonState( inheritedState, xPrec, EDITMODE_DISPLAYCONTENTS ) )
	yView = InnerFragment( y, _withPythonState( inheritedState, yPrec, EDITMODE_DISPLAYCONTENTS ) )
	view = spanBinOp( xView, yView, op )
	return expressionNodeEditor( grammar, inheritedState, model,
	                             view )


def spanCmpOpView(grammar, inheritedState, model, op, y):
	yView = InnerFragment( y, _withPythonState( inheritedState, nodePrecedence[model], EDITMODE_DISPLAYCONTENTS ) )
	view = spanCmpOp( op, yView )
	return expressionNodeEditor( grammar, inheritedState, model,
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
		pyReplaceNode( model, Schema.Load( name='None' ) )

	deleteItem = MenuItem.menuItemWithLabel( 'Delete inline object', _onDelete )
	menu.add( deleteItem )

	return False


def _inlineObjectStmtContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		pyReplaceNode( model, Schema.BlankLine() )

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



def _withPythonState(inheritedState, precedence, mode=EDITMODE_DISPLAYCONTENTS):
	return inheritedState.withAttrs( outerPrecedence=precedence, editMode=mode )


class Python25View (GSymViewObjectNodeDispatch):
	def __init__(self, parser):
		self._parser = parser


	# OUTER NODES
	@DMObjectNodeDispatchMethod( Schema.PythonModule )
	def PythonModule(self, fragment, inheritedState, model, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = InnerFragment.map( suite, _withPythonState( inheritedState, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )
		s = suiteView( lineViews )
		_inlineObject_dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop_inlineObject )
		s = s.withDropDest( _inlineObject_dropDest )
		s = EditableStructuralItem( [ PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', self._parser.suite(), _makeSuiteCommitFn( suite ) ),
		                              PythonSyntaxRecognizingEditor.instance.topLevelNodeEditListener() ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonSuite )
	def PythonSuite(self, fragment, inheritedState, model, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = InnerFragment.map( suite, _withPythonState( inheritedState, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )
		s = suiteView( lineViews )
		_inlineObject_dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop_inlineObject )
		s = s.withDropDest( _inlineObject_dropDest )
		s = EditableStructuralItem( [ PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', self._parser.suite(), _makeSuiteCommitFn( suite ) ),
		                              PythonSyntaxRecognizingEditor.instance.topLevelNodeEditListener() ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonExpression )
	def PythonExpression(self, fragment, inheritedState, model, expr):
		if expr is None:
			# Empty document - create a single blank line so that there is something to edit
			exprView = blankLine()
			seg = exprView
		else:
			exprView = InnerFragment( expr, _withPythonState( inheritedState, PRECEDENCE_NONE, EDITMODE_DISPLAYCONTENTS ) )
			seg = Segment( exprView )
		e = Paragraph( [ seg ] )
		_inlineObject_dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop_inlineObject )
		e = e.withDropDest( _inlineObject_dropDest )
		e = EditableStructuralItem( [ instanceCache( PythonExpressionEditListener, self._parser.tupleOrExpression() ),
		                              PythonExpressionTopLevelEditListener.instance ],  model,  e )
		e = e.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return e



	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, fragment, inheritedState, model):
		return statementNodeEditor( self._parser, inheritedState, model,
		                            blankLine() )


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, fragment, inheritedState, model, value):
		def _viewItem(x):
			if x is model:
				raise ValueError, 'Python25View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseableText( x )
				return view
			elif isinstance( x, DMObjectInterface ):
				view = InnerFragment( x, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_UNPARSED, EDITMODE_DISPLAYCONTENTS ) )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( x, view )
				return view
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsedNodeEditor( self._parser, inheritedState, model,
		                           unparsedElements( views ) )





	# Comment statement
	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	def CommentStmt(self, fragment, inheritedState, model, comment):
		view = commentStmt( comment )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )





	# String literal
	__strLit_fmtTable = { 'ascii' : None,  'unicode' : 'u',  'ascii-regex' : 'r',  'unicode-regex' : 'ur' }

	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	def StringLiteral(self, fragment, inheritedState, model, format, quotation, value):
		fmt = self.__strLit_fmtTable[format]

		quote = "'"   if quotation == 'single'   else   '"'

		view = stringLiteral( fmt, quote, value )

		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	def IntLiteral(self, fragment, inheritedState, model, format, numType, value):
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

		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# Float literal
	@DMObjectNodeDispatchMethod( Schema.FloatLiteral )
	def FloatLiteral(self, fragment, inheritedState, model, value):
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             floatLiteral( value ) )



	# Imaginary literal
	@DMObjectNodeDispatchMethod( Schema.ImaginaryLiteral )
	def ImaginaryLiteral(self, fragment, inheritedState, model, value):
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             imaginaryLiteral( value ) )



	# Targets
	@DMObjectNodeDispatchMethod( Schema.SingleTarget )
	def SingleTarget(self, fragment, inheritedState, model, name):
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             singleTarget( name ) )


	@DMObjectNodeDispatchMethod( Schema.TupleTarget )
	def TupleTarget(self, fragment, inheritedState, model, targets, trailingSeparator):
		elementViews = InnerFragment.map( targets, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = tupleTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ListTarget )
	def ListTarget(self, fragment, inheritedState, model, targets, trailingSeparator):
		elementViews = InnerFragment.map( targets, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = listTarget( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# Variable reference
	@DMObjectNodeDispatchMethod( Schema.Load )
	def Load(self, fragment, inheritedState, model, name):
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             load( name ) )



	# Tuple literal
	@DMObjectNodeDispatchMethod( Schema.TupleLiteral )
	def TupleLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = tupleLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# List literal
	@DMObjectNodeDispatchMethod( Schema.ListLiteral )
	def ListLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = listLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# List comprehension / generator expression
	@DMObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, fragment, inheritedState, model, target, source):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_COMPREHENSIONFOR) )
		sourceView = InnerFragment( source, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_COMPREHENSIONFOR ) )
		view = comprehensionFor( targetView, sourceView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, fragment, inheritedState, model, condition):
		conditionView = InnerFragment( condition, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_COMPREHENSIONIF ) )
		view = comprehensionIf( conditionView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ListComp )
	def ListComp(self, fragment, inheritedState, model, resultExpr, comprehensionItems):
		exprView = InnerFragment( resultExpr, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		itemViews = InnerFragment.map( comprehensionItems, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = listComp( exprView, itemViews )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )


	@DMObjectNodeDispatchMethod( Schema.GeneratorExpr )
	def GeneratorExpr(self, fragment, inheritedState, model, resultExpr, comprehensionItems):
		exprView = InnerFragment( resultExpr, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		itemViews = InnerFragment.map( comprehensionItems, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = genExpr( exprView, itemViews )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )




	# Dictionary literal
	@DMObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, fragment, inheritedState, model, key, value):
		keyView = InnerFragment( key, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = dictKeyValuePair( keyView, valueView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.DictLiteral )
	def DictLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = dictLiteral( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )


	# Yield expression
	@DMObjectNodeDispatchMethod( Schema.YieldExpr )
	def YieldExpr(self, fragment, inheritedState, model, value):
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_YIELDEXPR ) )
		view = yieldExpr( valueView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# Attribute ref
	@DMObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, fragment, inheritedState, model, target, name):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET ) )
		view = attributeRef( targetView, name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# Subscript
	@DMObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, fragment, inheritedState, model, lower, upper):
		lowerView = InnerFragment( lower, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if lower is not None   else None
		upperView = InnerFragment( upper, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if upper is not None   else None
		view = subscriptSlice( lowerView, upperView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, fragment, inheritedState, model, lower, upper, stride):
		lowerView = InnerFragment( lower, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if lower is not None   else None
		upperView = InnerFragment( upper, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if upper is not None   else None
		strideView = InnerFragment( stride, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )   if stride is not None   else None
		view = subscriptLongSlice( lowerView, upperView, strideView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, fragment, inheritedState, model):
		view = subscriptEllipsis()
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.SubscriptTuple )
	def SubscriptTuple(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = InnerFragment.map( values, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )
		view = subscriptTuple( elementViews, trailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, fragment, inheritedState, model, target, index):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET ) )
		indexView = InnerFragment( index, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX ) )
		view = subscript( targetView, indexView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )




	# Call
	@DMObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, fragment, inheritedState, model, name, value):
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CALLARG ) )
		view = callKWArg( name, valueView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, fragment, inheritedState, model, value):
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CALLARG ) )
		view = callArgList( valueView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, fragment, inheritedState, model, value):
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CALLARG ) )
		view = callKWArgList( valueView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.Call )
	def Call(self, fragment, inheritedState, model, target, args, argsTrailingSeparator):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CALLTARGET ) )
		argViews = InnerFragment.map( args, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CALLARG ) )
		view = call( targetView, argViews, argsTrailingSeparator is not None )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )





	# Operators
	@DMObjectNodeDispatchMethod( Schema.Pow )
	def Pow(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = InnerFragment( x, _withPythonState( inheritedState, xPrec ) )
		yView = InnerFragment( y, _withPythonState( inheritedState, yPrec, EDITMODE_EDITEXPRESSION ) )
		view = exponent( xView, yView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )


	@DMObjectNodeDispatchMethod( Schema.Invert )
	def Invert(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '~' )

	@DMObjectNodeDispatchMethod( Schema.Negate )
	def Negate(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '-' )

	@DMObjectNodeDispatchMethod( Schema.Pos )
	def Pos(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '+' )


	@DMObjectNodeDispatchMethod( Schema.Mul )
	def Mul(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '*', False )

	@DMObjectNodeDispatchMethod( Schema.Div )
	def Div(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = InnerFragment( x, _withPythonState( inheritedState, xPrec, EDITMODE_EDITEXPRESSION ) )
		yView = InnerFragment( y, _withPythonState( inheritedState, yPrec, EDITMODE_EDITEXPRESSION ) )
		#<NO_TREE_EVENT_LISTENER>
		view = div( xView, yView, '/' )
		view = BreakableStructuralItem( PythonSyntaxRecognizingEditor.instance, model, view )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.Mod )
	def Mod(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '%', False )


	@DMObjectNodeDispatchMethod( Schema.Add )
	def Add(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '+', False )

	@DMObjectNodeDispatchMethod( Schema.Sub )
	def Sub(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '-', False )


	@DMObjectNodeDispatchMethod( Schema.LShift )
	def LShift(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '<<', False )

	@DMObjectNodeDispatchMethod( Schema.RShift )
	def RShift(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '>>', False )


	@DMObjectNodeDispatchMethod( Schema.BitAnd )
	def BitAnd(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '&', False )

	@DMObjectNodeDispatchMethod( Schema.BitXor )
	def BitXor(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '^', False )

	@DMObjectNodeDispatchMethod( Schema.BitOr )
	def BitOr(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '|', False )


	@DMObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, fragment, inheritedState, model, x, ops):
		xView = InnerFragment( x, _withPythonState( inheritedState, PRECEDENCE_CMP ) )
		opViews = InnerFragment.map( ops, _withPythonState( inheritedState, PRECEDENCE_CMP ) )
		view = compare( xView, opViews )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.CmpOpLte )
	def CmpOpLte(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '<=', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpLt )
	def CmpOpLt(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '<', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpGte )
	def CmpOpGte(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '>=', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpGt )
	def CmpOpGt(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '>', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpEq )
	def CmpOpEq(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '==', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpNeq )
	def CmpOpNeq(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '!=', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'is not', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpIs )
	def CmpOpIs(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'is', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'not in', y )

	@DMObjectNodeDispatchMethod( Schema.CmpOpIn )
	def CmpOpIn(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'in', y )



	@DMObjectNodeDispatchMethod( Schema.NotTest )
	def NotTest(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, 'not ' )

	@DMObjectNodeDispatchMethod( Schema.AndTest )
	def AndTest(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, 'and', False )

	@DMObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, 'or', False )





	# Parameters
	@DMObjectNodeDispatchMethod( Schema.SimpleParam )
	def SimpleParam(self, fragment, inheritedState, model, name):
		view = simpleParam( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.DefaultValueParam )
	def DefaultValueParam(self, fragment, inheritedState, model, name, defaultValue):
		valueView = InnerFragment( defaultValue, _withPythonState( inheritedState, PRECEDENCE_NONE ) )
		view = defaultValueParam( name, valueView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ParamList )
	def ParamList(self, fragment, inheritedState, model, name):
		view = paramList( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.KWParamList )
	def KWParamList(self, fragment, inheritedState, model, name):
		view = kwParamList( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# Lambda expression
	@DMObjectNodeDispatchMethod( Schema.LambdaExpr )
	def LambdaExpr(self, fragment, inheritedState, model, params, paramsTrailingSeparator, expr):
		exprView = InnerFragment( expr, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_LAMBDAEXPR ) )
		paramViews = InnerFragment.map( params, _withPythonState( inheritedState, PRECEDENCE_NONE ) )

		view = lambdaExpr( paramViews, paramsTrailingSeparator is not None, exprView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )



	# Conditional expression
	@DMObjectNodeDispatchMethod( Schema.ConditionalExpr )
	def ConditionalExpr(self, fragment, inheritedState, model, condition, expr, elseExpr):
		conditionView = InnerFragment( condition, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		exprView = InnerFragment( expr, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		elseExprView = InnerFragment( elseExpr, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_CONDITIONALEXPR ) )
		view = conditionalExpr( conditionView, exprView, elseExprView )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )




	#
	#
	# QUOTE AND UNQUOTE
	#
	#

	# Quote
	@DMObjectNodeDispatchMethod( Schema.Quote )
	def Quote(self, fragment, inheritedState, model, value):
		if isinstance( value, DMObject ):
			if value.isInstanceOf( Schema.PythonExpression ):
				title = 'QUOTE - Expr'
			elif value.isInstanceOf( Schema.PythonSuite ):
				title = 'QUOTE - Suite'
			else:
				raise TypeError, 'Contents of \'quote\' should be a PythonExpression or a PythonSuite'

			valueView = perspective.applyTo( InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_QUOTE ) ) )
		else:
			raise TypeError, 'Value of \'quote\' should be a DMObject'


		view = quote( valueView, title, PythonSyntaxRecognizingEditor.instance )
		return specialFormExpressionNodeEditor( self._parser, inheritedState, model,
		                                        view )



	# Unquote
	@DMObjectNodeDispatchMethod( Schema.Unquote )
	def Unquote(self, fragment, inheritedState, model, value):
		if isinstance( value, DMObject ):
			valueView = perspective.applyTo( InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_QUOTE ) ) )
		else:
			raise TypeError, 'Value of \'unquote\' should be a DMObject'


		view = unquote( valueView, 'UNQUOTE', PythonSyntaxRecognizingEditor.instance )
		return specialFormExpressionNodeEditor( self._parser, inheritedState, model,
		                                        view )



	#
	#
	# EXTERNAL EXPRESSION
	#
	#

	# External expression
	@DMObjectNodeDispatchMethod( Schema.ExternalExpr )
	def ExternalExpr(self, fragment, inheritedState, model, expr):
		if isinstance( expr, DMObject ):
			schema = expr.getDMObjectClass().getSchema()
			presenter, title = ExternalExpression.getExternalExpressionPresenterAndTitle( schema )
			exprView = presenter( expr, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_EXTERNALEXPR ) )
		else:
			exprView = Label( '<expr>' )
			title = 'ext'

		def _onDeleteButton(button, event):
			pyReplaceNode( model, Schema.Load( name='None' ) )


		deleteButton = Button( Image.systemIcon( 'delete_tiny' ), _onDeleteButton )

		view = externalExpr( exprView, title, deleteButton )
		return specialFormExpressionNodeEditor( self._parser, inheritedState, model,
		                                        view )



	#
	#
	# INLINE OBJECT
	#
	#

	# Inline object expression
	@DMObjectNodeDispatchMethod( Schema.InlineObjectExpr )
	def InlineObjectExpr(self, fragment, inheritedState, model, resource):
		value = resource.getValue()
		valueView = ApplyPerspective( None, value )

		try:
			modelFn = value.__py_model__
		except AttributeError:
			# Standard view
			view = inlineObject( valueView )
			view = view.withContextMenuInteractor( _inlineObjectExprContextMenuFactory )
			return specialFormExpressionNodeEditor( self._parser, inheritedState, model,
			                                        view )
		else:
			# Macro view
			def createModelView():
				return Pres.coerce( modelFn() )
			view = inlineObjectMacro( valueView, LazyPres( createModelView ) )
			view = view.withContextMenuInteractor( _inlineObjectExprContextMenuFactory )
			return specialFormExpressionNodeEditor( self._parser, inheritedState, model,
			                                        view )


	# Inline object statement
	@DMObjectNodeDispatchMethod( Schema.InlineObjectStmt )
	def InlineObjectStmt(self, fragment, inheritedState, model, resource):
		value = resource.getValue()
		valueView = ApplyPerspective( None, value )

		try:
			modelFn = value.__py_model__
		except AttributeError:
			# Standard view
			view = inlineObject( valueView )
			view = view.withContextMenuInteractor( _inlineObjectStmtContextMenuFactory )
			return specialFormStatementNodeEditor( self._parser, inheritedState, model,
			                                       view )
		else:
			# Macro view
			def createModelView():
				return Pres.coerce( modelFn() )
			view = inlineObjectMacro( valueView, LazyPres( createModelView ) )
			view = view.withContextMenuInteractor( _inlineObjectStmtContextMenuFactory )
			return specialFormStatementNodeEditor( self._parser, inheritedState, model,
			                                       view )



	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Expression statement
	@DMObjectNodeDispatchMethod( Schema.ExprStmt )
	def ExprStmt(self, fragment, inheritedState, model, expr):
		exprView = InnerFragment( expr, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = exprStmt( exprView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )



	# Assert statement
	@DMObjectNodeDispatchMethod( Schema.AssertStmt )
	def AssertStmt(self, fragment, inheritedState, model, condition, fail):
		conditionView = InnerFragment( condition, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		failView = InnerFragment( fail, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if fail is not None   else None
		view = assertStmt( conditionView, failView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Assignment statement
	@DMObjectNodeDispatchMethod( Schema.AssignStmt )
	def AssignStmt(self, fragment, inheritedState, model, targets, value):
		targetViews = InnerFragment.map( targets, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = assignStmt( targetViews, valueView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Augmented assignment statement
	@DMObjectNodeDispatchMethod( Schema.AugAssignStmt )
	def AugAssignStmt(self, fragment, inheritedState, model, op, target, value):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = augAssignStmt( op, targetView, valueView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Pass statement
	@DMObjectNodeDispatchMethod( Schema.PassStmt )
	def PassStmt(self, fragment, inheritedState, model):
		view = passStmt()
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Del statement
	@DMObjectNodeDispatchMethod( Schema.DelStmt )
	def DelStmt(self, fragment, inheritedState, model, target):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = delStmt( targetView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Return statement
	@DMObjectNodeDispatchMethod( Schema.ReturnStmt )
	def ReturnStmt(self, fragment, inheritedState, model, value):
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = returnStmt( valueView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Yield statement
	@DMObjectNodeDispatchMethod( Schema.YieldStmt )
	def YieldStmt(self, fragment, inheritedState, model, value):
		valueView = InnerFragment( value, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = yieldStmt( valueView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Raise statement
	@DMObjectNodeDispatchMethod( Schema.RaiseStmt )
	def RaiseStmt(self, fragment, inheritedState, model, excType, excValue, traceback):
		excTypeView = InnerFragment( excType, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if excType is not None   else None
		excValueView = InnerFragment( excValue, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if excValue is not None   else None
		tracebackView = InnerFragment( traceback, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if traceback is not None   else None
		view = raiseStmt( excTypeView, excValueView, tracebackView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Break statement
	@DMObjectNodeDispatchMethod( Schema.BreakStmt )
	def BreakStmt(self, fragment, inheritedState, model):
		view = breakStmt()
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Continue statement
	@DMObjectNodeDispatchMethod( Schema.ContinueStmt )
	def ContinueStmt(self, fragment, inheritedState, model):
		view = continueStmt()
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Import statement
	@DMObjectNodeDispatchMethod( Schema.RelativeModule )
	def RelativeModule(self, fragment, inheritedState, model, name):
		view = relativeModule( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleImport )
	def ModuleImport(self, fragment, inheritedState, model, name):
		view = moduleImport( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleImportAs )
	def ModuleImportAs(self, fragment, inheritedState, model, name, asName):
		view = moduleImportAs( name, asName )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleContentImport )
	def ModuleContentImport(self, fragment, inheritedState, model, name):
		view = moduleContentImport( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ModuleContentImportAs )
	def ModuleContentImportAs(self, fragment, inheritedState, model, name, asName):
		view = moduleContentImportAs( name, asName )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.ImportStmt )
	def ImportStmt(self, fragment, inheritedState, model, modules):
		moduleViews = InnerFragment.map( modules, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = importStmt( moduleViews )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )

	@DMObjectNodeDispatchMethod( Schema.FromImportStmt )
	def FromImportStmt(self, fragment, inheritedState, model, module, imports):
		moduleView = InnerFragment( module, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		importViews = InnerFragment.map( imports, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = fromImportStmt( moduleView, importViews )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )

	@DMObjectNodeDispatchMethod( Schema.FromImportAllStmt )
	def FromImportAllStmt(self, fragment, inheritedState, model, module):
		moduleView = InnerFragment( module, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = fromImportAllStmt( moduleView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )


	# Global statement
	@DMObjectNodeDispatchMethod( Schema.GlobalVar )
	def GlobalVar(self, fragment, inheritedState, model, name):
		view = globalVar( name )
		return expressionNodeEditor( self._parser, inheritedState, model,
		                             view )

	@DMObjectNodeDispatchMethod( Schema.GlobalStmt )
	def GlobalStmt(self, fragment, inheritedState, model, vars):
		varViews = InnerFragment.map( vars, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = globalStmt( varViews )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )



	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.ExecStmt )
	def ExecStmt(self, fragment, inheritedState, model, source, globals, locals):
		sourceView = InnerFragment( source, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		globalsView = InnerFragment( globals, _withPythonState( inheritedState, PRECEDENCE_STMT ) )    if globals is not None   else None
		localsView = InnerFragment( locals, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if locals is not None   else None
		view = execStmt( sourceView, globalsView, localsView )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )






	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.PrintStmt )
	def PrintStmt(self, fragment, inheritedState, model, destination, values):
		destView = InnerFragment( destination, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if destination is not None   else None
		valueViews = InnerFragment.map( values, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		view = printStmt( destView, valueViews )
		return statementNodeEditor( self._parser, inheritedState, model,
		                            view )




	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def _ifStmtHeaderElement(self, inheritedState, condition):
		conditionView = InnerFragment( condition, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		return ifStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.IfStmtHeader )
	def IfStmtHeader(self, fragment, inheritedState, model, condition):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._ifStmtHeaderElement( inheritedState, condition ) )


	# Elif statement
	def _elifStmtHeaderElement(self, inheritedState, condition):
		conditionView = InnerFragment( condition, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		return elifStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.ElifStmtHeader )
	def ElifStmtHeader(self, fragment, inheritedState, model, condition):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._elifStmtHeaderElement( inheritedState, condition ) )



	# Else statement
	def _elseStmtHeaderElement(self, inheritedState):
		return elseStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.ElseStmtHeader )
	def ElseStmtHeader(self, fragment, inheritedState, model):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._elseStmtHeaderElement( inheritedState ) )


	# While statement
	def _whileStmtHeaderElement(self, inheritedState, condition):
		conditionView = InnerFragment( condition, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		return whileStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.WhileStmtHeader )
	def WhileStmtHeader(self, fragment, inheritedState, model, condition):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._whileStmtHeaderElement( inheritedState, condition ) )


	# For statement
	def _forStmtHeaderElement(self, inheritedState, target, source):
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		sourceView = InnerFragment( source, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		return forStmtHeader( targetView, sourceView )

	@DMObjectNodeDispatchMethod( Schema.ForStmtHeader )
	def ForStmtHeader(self, fragment, inheritedState, model, target, source):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._forStmtHeaderElement( inheritedState, target, source ) )



	# Try statement
	def _tryStmtHeaderElement(self, inheritedState):
		return tryStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.TryStmtHeader )
	def TryStmtHeader(self, fragment, inheritedState, model):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._tryStmtHeaderElement( inheritedState ) )



	# Except statement
	def _exceptStmtHeaderElement(self, inheritedState, exception, target):
		excView = InnerFragment( exception, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if exception is not None   else None
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if target is not None   else None
		return exceptStmtHeader( excView, targetView )

	@DMObjectNodeDispatchMethod( Schema.ExceptStmtHeader )
	def ExceptStmtHeader(self, fragment, inheritedState, model, exception, target):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._exceptStmtHeaderElement( inheritedState, exception, target ) )



	# Finally statement
	def _finallyStmtHeaderElement(self, inheritedState):
		return finallyStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.FinallyStmtHeader )
	def FinallyStmtHeader(self, fragment, inheritedState, model):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._finallyStmtHeaderElement( inheritedState ) )



	# With statement
	def _withStmtHeaderElement(self, inheritedState, expr, target):
		exprView = InnerFragment( expr, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		targetView = InnerFragment( target, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if target is not None   else None
		return withStmtHeader( exprView, targetView )

	@DMObjectNodeDispatchMethod( Schema.WithStmtHeader )
	def WithStmtHeader(self, fragment, inheritedState, model, expr, target):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._withStmtHeaderElement( inheritedState, expr, target ) )



	# Decorator statement
	def _decoStmtHeaderElement(self, inheritedState, name, args, argsTrailingSeparator):
		argViews = InnerFragment.map( args, _withPythonState( inheritedState, PRECEDENCE_STMT ) )   if args is not None   else None
		return decoStmtHeader( name, argViews, argsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DecoStmtHeader )
	def DecoStmtHeader(self, fragment, inheritedState, model, name, args, argsTrailingSeparator):
		return compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                      self._decoStmtHeaderElement( inheritedState, name, args, argsTrailingSeparator ) )



	# Def statement
	def _defStmtHeaderElement(self, inheritedState, name, params, paramsTrailingSeparator):
		paramViews = InnerFragment.map( params, _withPythonState( inheritedState, PRECEDENCE_STMT ) )
		return defStmtHeader( name, paramViews, paramsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DefStmtHeader )
	def DefStmtHeader(self, fragment, inheritedState, model, name, params, paramsTrailingSeparator):
		editor = compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                        self._defStmtHeaderElement( inheritedState, name, params, paramsTrailingSeparator ),
		                                        lambda header: defStmtHeaderHighlight( header ) )
		return defStmtHighlight( editor )


	# Def statement
	def _classStmtHeaderElement(self, inheritedState, name, bases, basesTrailingSeparator):
		baseViews = InnerFragment.map( bases, _withPythonState( inheritedState, PRECEDENCE_CONTAINER_ELEMENT ) )   if bases is not None   else None
		return classStmtHeader( name, baseViews, basesTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.ClassStmtHeader )
	def ClassStmtHeader(self, fragment, inheritedState, model, name, bases, basesTrailingSeparator):
		editor = compoundStatementHeaderEditor( self._parser, inheritedState, model,
		                                        self._classStmtHeaderElement( inheritedState, name, bases, basesTrailingSeparator ),
		                                        lambda header: classStmtHeaderHighlight( header ) )
		return classStmtHighlight( editor )




	#
	#
	# STRUCTURE STATEMENTS
	#
	#

	# Indented block
	@DMObjectNodeDispatchMethod( Schema.IndentedBlock )
	def IndentedBlock(self, fragment, inheritedState, model, suite):
		indent = StructuralItem( Schema.Indent(), indentElement() )

		lineViews = InnerFragment.map( suite, _withPythonState( inheritedState, PRECEDENCE_NONE, EDITMODE_EDITSTATEMENT ) )

		dedent = StructuralItem( Schema.Dedent(), dedentElement() )

		suiteElement = indentedBlock( indent, lineViews, dedent )
		suiteElement = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', self._parser.compoundSuite(), _makeSuiteCommitFn( suite ) ),
		                                       model, suiteElement )

		return badIndentation( suiteElement )





	#
	#
	# COMPOUND STATEMENTS
	#
	#

	# If statement
	@DMObjectNodeDispatchMethod( Schema.IfStmt )
	def IfStmt(self, fragment, inheritedState, model, condition, suite, elifBlocks, elseSuite):
		compoundBlocks = [ ( Schema.IfStmtHeader( condition=condition ), self._ifStmtHeaderElement( inheritedState, condition ), suite ) ]
		for b in elifBlocks:
			if not b.isInstanceOf( Schema.ElifBlock ):
				raise TypeError, 'IfStmt elifBlocks should only contain ElifBlock instances'
			compoundBlocks.append( ( Schema.ElifStmtHeader( condition=b['condition'] ), self._elifStmtHeaderElement( inheritedState, b['condition'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundStatementEditor( self._parser, inheritedState, model,
		                                compoundBlocks )



	# While statement
	@DMObjectNodeDispatchMethod( Schema.WhileStmt )
	def WhileStmt(self, fragment, inheritedState, model, condition, suite, elseSuite):
		compoundBlocks = [ ( Schema.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( inheritedState, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundStatementEditor( self._parser, inheritedState, model,
		                                compoundBlocks )



	# For statement
	@DMObjectNodeDispatchMethod( Schema.ForStmt )
	def ForStmt(self, fragment, inheritedState, model, target, source, suite, elseSuite):
		compoundBlocks = [ ( Schema.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( inheritedState, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundStatementEditor( self._parser, inheritedState, model,
		                                compoundBlocks )



	# Try statement
	@DMObjectNodeDispatchMethod( Schema.TryStmt )
	def TryStmt(self, fragment, inheritedState, model, suite, exceptBlocks, elseSuite, finallySuite):
		compoundBlocks = [ ( Schema.TryStmtHeader(), self._tryStmtHeaderElement( inheritedState ), suite ) ]
		for b in exceptBlocks:
			if not b.isInstanceOf( Schema.ExceptBlock ):
				raise TypeError, 'TryStmt elifBlocks should only contain ExceptBlock instances'
			compoundBlocks.append( ( Schema.ExceptStmtHeader( exception=b['exception'], target=b['target'] ), self._exceptStmtHeaderElement( inheritedState, b['exception'], b['target'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		if finallySuite is not None:
			compoundBlocks.append( ( Schema.FinallyStmtHeader(), self._finallyStmtHeaderElement( inheritedState ),  finallySuite ) )
		return compoundStatementEditor( self._parser, inheritedState, model,
		                                compoundBlocks )




	# With statement
	@DMObjectNodeDispatchMethod( Schema.WithStmt )
	def WithStmt(self, fragment, inheritedState, model, expr, target, suite):
		compoundBlocks = [ ( Schema.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( inheritedState, expr, target ), suite ) ]
		return compoundStatementEditor( self._parser, inheritedState, model,
		                                compoundBlocks )



	# Def statement
	@DMObjectNodeDispatchMethod( Schema.DefStmt )
	def DefStmt(self, fragment, inheritedState, model, decorators, name, params, paramsTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Schema.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Schema.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
			                         self._decoStmtHeaderElement( inheritedState, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )

		compoundBlocks.append( ( Schema.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ),
		                         self._defStmtHeaderElement( inheritedState, name, params, paramsTrailingSeparator ), suite,
		                         lambda header: defStmtHeaderHighlight( header ) ) )
		editor = compoundStatementEditor( self._parser, inheritedState, model,
		                                  compoundBlocks )
		return defStmtHighlight( editor )


	# Class statement
	@DMObjectNodeDispatchMethod( Schema.ClassStmt )
	def ClassStmt(self, fragment, inheritedState, model, name, bases, basesTrailingSeparator, suite):
		compoundBlocks = [ ( Schema.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ),
		                     self._classStmtHeaderElement( inheritedState, name, bases, basesTrailingSeparator ), suite,
		                     lambda header: classStmtHeaderHighlight( header ) ) ]
		editor = compoundStatementEditor( self._parser, inheritedState, model,
		                                  compoundBlocks )
		return classStmtHighlight( editor )





_parser = Python25Grammar()
perspective = SequentialEditorPerspective( Python25View( _parser ), PythonSyntaxRecognizingEditor.instance )



