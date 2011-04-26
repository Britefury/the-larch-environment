##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import Throwable

from java.awt.event import KeyEvent

from BritefuryJ.Parser import ParserExpression

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, DMObjectNodeDispatchMethodWrapper

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

from BritefuryJ.Pres import ApplyPerspective
from BritefuryJ.Pres.Primitive import Paragraph, Segment

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment
from BritefuryJ.Editor.SyntaxRecognizing.SRFragmentEditor import EditMode

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




_statementIndentationInteractor = StatementIndentationInteractor()



_pythonPrecedenceHandler = PrecedenceHandler( ClassAttributeReader( parensRequired ), ObjectFieldReader( 'parens' ).stringToInteger( -1 ), ClassAttributeReader( nodePrecedence ), openParen, closeParen )



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
	withoutNewline = value[:-1]
	unparsed = Schema.UnparsedStmt( value=Schema.UNPARSED( value=withoutNewline.getItemValues() ) )
	pyReplaceNode( model, unparsed.deepCopy() )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	pyReplaceNode( model, unparsed.deepCopy() )



def compoundStatementEditor(pythonView, inheritedState, model, compoundBlocks):
	statementContents = []

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

			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )

			dedent = StructuralItem( Schema.Dedent(), dedentElement() )

			suiteElement = indentedBlock( indent, lineViews, dedent )
			suiteElement = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, pythonView._makeCompoundSuiteEditListener( suite ), Schema.IndentedBlock( suite=suite ), suiteElement )

			statementContents.extend( [ headerStatementLine.alignHExpand(), suiteElement.alignHExpand() ] )
		else:
			statementContents.append( headerStatementLine.alignHExpand() )

	return compoundStmt( statementContents )



def spanPrefixOpView(grammar, inheritedState, model, x, op):
	xView = SREInnerFragment( x, nodePrecedence[model], EditMode.DISPLAY )
	return spanPrefixOp( xView, op )


def spanBinOpView(grammar, inheritedState, model, x, y, op):
	xPrec, yPrec = computeBinOpViewPrecedenceValues( nodePrecedence[model], rightAssociative[model] )
	xView = SREInnerFragment( x, xPrec, EditMode.DISPLAY )
	yView = SREInnerFragment( y, yPrec, EditMode.DISPLAY )
	return spanBinOp( xView, yView, op )


def spanCmpOpView(grammar, inheritedState, model, op, y):
	yView = SREInnerFragment( y, nodePrecedence[model], EditMode.DISPLAY )
	return spanCmpOp( op, yView )



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
			embeddedValue = DMNode.embedIsolated( model )
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
					expr = Schema.InlineObjectExpr( embeddedValue=embeddedValue )
					_insertSpecialForm( caret, expr )
				elif modelType is Schema.Stmt:
					stmt = Schema.InlineObjectStmt( embeddedValue=embeddedValue )
					_insertSpecialForm( caret, stmt )
	return True




def _inlineObjectExprContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		pyReplaceNode( model, Schema.Load( name='None' ) )

	menu.add( MenuItem.menuItemWithLabel( 'Delete inline object', _onDelete ) )

	return False


def _inlineObjectStmtContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		pyReplaceNode( model, Schema.BlankLine() )

	menu.add( MenuItem.menuItemWithLabel( 'Delete inline object', _onDelete ) )

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

	menu.add( MenuItem.menuItemWithLabel( 'Quote expression', _onQuoteExpr ) )
	menu.add( MenuItem.menuItemWithLabel( 'Quote suite', _onQuoteSuite ) )
	menu.add( MenuItem.menuItemWithLabel( 'Unquote', _onUnquote ) )

	menu.add( MenuItem.menuItemWithLabel( 'Insert expression', extExprMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
	return True





class UnparsedWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( UnparsedWrapper, self ).call( model, pythonView, args )
		return pythonView._unparsedFragmentEditor.editFragment( v, model, args[1] )

Unparsed = UnparsedWrapper.decorator()



class UnparsedStatementWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( UnparsedStatementWrapper, self ).call( model, pythonView, args )
		return pythonView._unparsedStatementFragmentEditor.editFragment( statementLine( v ), model, args[1] )

UnparsedStatement = UnparsedStatementWrapper.decorator()



class GenericWrapper (DMObjectNodeDispatchMethodWrapper):
	pass

Generic = GenericWrapper.decorator()



class ExpressionWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( ExpressionWrapper, self ).call( model, pythonView, args )
		return pythonView._expressionFragmentEditor.editFragment( v, model, args[1] )

Expression = ExpressionWrapper.decorator()



class StatementWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( StatementWrapper, self ).call( model, pythonView, args )
		return pythonView._statementFragmentEditor.editFragment( statementLine( v ), model, args[1] )

Statement = StatementWrapper.decorator()



class CompoundStatementHeaderWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( CompoundStatementHeaderWrapper, self ).call( model, pythonView, args )
		if isinstance( v, tuple ):
			e = pythonView._compoundStatementHeaderFragmentEditor.editFragment( statementLine( v[0] ), model, args[1] )
			for f in v[1:]:
				e = f( e )
			return e
		else:
			return pythonView._compoundStatementHeaderFragmentEditor.editFragment( statementLine( v ), model, args[1] )

CompoundStatementHeader = CompoundStatementHeaderWrapper.decorator()



class CompoundStatementWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( CompoundStatementWrapper, self ).call( model, pythonView, args )
		if isinstance( v, tuple ):
			f = v[1]
			return f( compoundStatementEditor( pythonView, args[1], model, v[0] ) )
		else:
			return compoundStatementEditor( pythonView, args[1], model, v )

CompoundStatement = CompoundStatementWrapper.decorator()



class SpecialFormExpressionWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( SpecialFormExpressionWrapper, self ).call( model, pythonView, args )
		return StructuralItem( model, v )

SpecialFormExpression = SpecialFormExpressionWrapper.decorator()



class SpecialFormStatementWrapper (DMObjectNodeDispatchMethodWrapper):
	def call(self, model, pythonView, args):
		v = super( SpecialFormStatementWrapper, self ).call( model, pythonView, args )
		v = StructuralItem( model, v )
		v = specialFormStatementLine( v )
		return pythonView._specialFormStatementFragmentEditor.editFragment( v, model, args[1] )

SpecialFormStatement = SpecialFormStatementWrapper.decorator()



class Python25View (GSymViewObjectNodeDispatch):
	def __init__(self, grammar):
		self._parser = grammar
		
		editor = PythonSyntaxRecognizingEditor.instance
		
		self._expr = editor.parsingNodeEditListener( 'Expression', grammar.expression(), pyReplaceNode )
		self._stmt = editor.parsingNodeEditListener( 'Statement', grammar.simpleSingleLineStatementValid(), pyReplaceNode )
		self._compHdr = editor.partialParsingNodeEditListener( 'Compound header', grammar.compoundStmtHeader() )
		self._stmtUnparsed = editor.unparsedNodeEditListener( 'Statement', _isValidUnparsedStatementValue, _commitUnparsedStatment, _commitInnerUnparsed )
		self._topLevel = editor.topLevelNodeEditListener()
		self._exprOuter = PythonExpressionEditListener( grammar.tupleOrExpression() )
		self._exprTopLevel = PythonExpressionTopLevelEditListener()
		
		self._expressionFragmentEditor = editor.fragmentEditor( False, _pythonPrecedenceHandler, [ self._expr ] )
		self._unparsedFragmentEditor = editor.fragmentEditor( False, [ self._expr ] )
		self._statementFragmentEditor = editor.fragmentEditor( True, [ self._stmt, self._compHdr, self._stmtUnparsed ], [ _statementIndentationInteractor ] )
		self._unparsedStatementFragmentEditor = editor.fragmentEditor( False, [ self._stmt, self._compHdr, self._stmtUnparsed ], [ _statementIndentationInteractor ] )
		self._compoundStatementHeaderFragmentEditor = editor.fragmentEditor( True, [ self._compHdr, self._stmtUnparsed ], [ _statementIndentationInteractor ] )
		self._specialFormStatementFragmentEditor = editor.fragmentEditor( True, [ self._stmt, self._compHdr, self._stmtUnparsed ], [ _statementIndentationInteractor ] )

		
	def _makeSuiteEditListener(self, suite):
		return PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', self._parser.suite(), _makeSuiteCommitFn( suite ) )

	def _makeCompoundSuiteEditListener(self, suite):
		return PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', self._parser.compoundSuite(), _makeSuiteCommitFn( suite ) )

		
		
		
	# OUTER NODES
	@DMObjectNodeDispatchMethod( Schema.PythonModule )
	def PythonModule(self, fragment, inheritedState, model, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )
		s = suiteView( lineViews ).alignHPack().alignVRefY()
		_inlineObject_dropDest = ObjectDndHandler.DropDest( FragmentView.FragmentModel, _onDrop_inlineObject )
		s = s.withDropDest( _inlineObject_dropDest )
		s = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._makeSuiteEditListener( suite ), self._topLevel ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonSuite )
	def PythonSuite(self, fragment, inheritedState, model, suite):
		if len( suite ) == 0:
			# Empty document - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )
		s = suiteView( lineViews ).alignHPack().alignVRefY()
		_inlineObject_dropDest = ObjectDndHandler.DropDest( FragmentView.FragmentModel, _onDrop_inlineObject )
		s = s.withDropDest( _inlineObject_dropDest )
		s = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._makeSuiteEditListener( suite ), self._topLevel ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonExpression )
	def PythonExpression(self, fragment, inheritedState, model, expr):
		if expr is None:
			# Empty document - create a single blank line so that there is something to edit
			exprView = blankLine()
			seg = exprView
		else:
			exprView = SREInnerFragment( expr, PRECEDENCE_NONE, EditMode.DISPLAY )
			seg = Segment( exprView )
		e = Paragraph( [ seg ] ).alignHPack().alignVRefY()
		_inlineObject_dropDest = ObjectDndHandler.DropDest( FragmentView.FragmentModel, _onDrop_inlineObject )
		e = e.withDropDest( _inlineObject_dropDest )
		e = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._exprOuter, self._exprTopLevel ],  model,  e )
		e = e.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		return e



	@Statement( Schema.BlankLine )
	def BlankLine(self, fragment, inheritedState, model):
		return blankLine()


	@Unparsed( Schema.UNPARSED )
	def UNPARSED(self, fragment, inheritedState, model, value):
		def _viewItem(x):
			if x is model:
				raise ValueError, 'Python25View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseableText( x )
				return view
			elif isinstance( x, DMObjectInterface ):
				view = SREInnerFragment( x, PRECEDENCE_CONTAINER_UNPARSED, EditMode.DISPLAY )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( x, view )
				return view
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsedElements( views )





	# Comment statement
	@Statement( Schema.CommentStmt )
	def CommentStmt(self, fragment, inheritedState, model, comment):
		return commentStmt( comment )





	# String literal
	__strLit_fmtTable = { 'ascii' : None,  'unicode' : 'u',  'ascii-regex' : 'r',  'unicode-regex' : 'ur' }

	@Expression( Schema.StringLiteral )
	def StringLiteral(self, fragment, inheritedState, model, format, quotation, value):
		fmt = self.__strLit_fmtTable[format]

		quote = "'"   if quotation == 'single'   else   '"'

		return stringLiteral( fmt, quote, value )

	# Integer literal
	@Expression( Schema.IntLiteral )
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

		return intLiteral( fmt, valueString )



	# Float literal
	@Expression( Schema.FloatLiteral )
	def FloatLiteral(self, fragment, inheritedState, model, value):
		return floatLiteral( value )



	# Imaginary literal
	@Expression( Schema.ImaginaryLiteral )
	def ImaginaryLiteral(self, fragment, inheritedState, model, value):
		return imaginaryLiteral( value )



	# Targets
	@Expression( Schema.SingleTarget )
	def SingleTarget(self, fragment, inheritedState, model, name):
		return singleTarget( name )


	@Expression( Schema.TupleTarget )
	def TupleTarget(self, fragment, inheritedState, model, targets, trailingSeparator):
		elementViews = SREInnerFragment.map( targets, PRECEDENCE_CONTAINER_ELEMENT )
		return tupleTarget( elementViews, trailingSeparator is not None )

	@Expression( Schema.ListTarget )
	def ListTarget(self, fragment, inheritedState, model, targets, trailingSeparator):
		elementViews = SREInnerFragment.map( targets, PRECEDENCE_CONTAINER_ELEMENT )
		return listTarget( elementViews, trailingSeparator is not None )



	# Variable reference
	@Expression( Schema.Load )
	def Load(self, fragment, inheritedState, model, name):
		return load( name )



	# Tuple literal
	@Expression( Schema.TupleLiteral )
	def TupleLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return tupleLiteral( elementViews, trailingSeparator is not None )



	# List literal
	@Expression( Schema.ListLiteral )
	def ListLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return listLiteral( elementViews, trailingSeparator is not None )



	# List comprehension / generator expression
	@Generic( Schema.ComprehensionFor )
	def ComprehensionFor(self, fragment, inheritedState, model, target, source):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_COMPREHENSIONFOR)
		sourceView = SREInnerFragment( source, PRECEDENCE_CONTAINER_COMPREHENSIONFOR )
		return comprehensionFor( targetView, sourceView )

	@Generic( Schema.ComprehensionIf )
	def ComprehensionIf(self, fragment, inheritedState, model, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_CONTAINER_COMPREHENSIONIF )
		return comprehensionIf( conditionView )

	@Expression( Schema.ListComp )
	def ListComp(self, fragment, inheritedState, model, resultExpr, comprehensionItems):
		exprView = SREInnerFragment( resultExpr, PRECEDENCE_CONTAINER_ELEMENT )
		itemViews = SREInnerFragment.map( comprehensionItems, PRECEDENCE_CONTAINER_ELEMENT )
		return listComp( exprView, itemViews )


	@Expression( Schema.GeneratorExpr )
	def GeneratorExpr(self, fragment, inheritedState, model, resultExpr, comprehensionItems):
		exprView = SREInnerFragment( resultExpr, PRECEDENCE_CONTAINER_ELEMENT )
		itemViews = SREInnerFragment.map( comprehensionItems, PRECEDENCE_CONTAINER_ELEMENT )
		return genExpr( exprView, itemViews )




	# Dictionary literal
	@Generic( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, fragment, inheritedState, model, key, value):
		keyView = SREInnerFragment( key, PRECEDENCE_CONTAINER_ELEMENT )
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_ELEMENT )
		return dictKeyValuePair( keyView, valueView )

	@Expression( Schema.DictLiteral )
	def DictLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return dictLiteral( elementViews, trailingSeparator is not None )


	# Yield expression
	@Expression( Schema.YieldExpr )
	def YieldExpr(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_YIELDEXPR )
		return yieldExpr( valueView )



	# Attribute ref
	@Expression( Schema.AttributeRef )
	def AttributeRef(self, fragment, inheritedState, model, target, name):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET )
		return attributeRef( targetView, name )



	# Subscript
	@Generic( Schema.SubscriptSlice )
	def SubscriptSlice(self, fragment, inheritedState, model, lower, upper):
		lowerView = SREInnerFragment( lower, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if lower is not None   else None
		upperView = SREInnerFragment( upper, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if upper is not None   else None
		return subscriptSlice( lowerView, upperView )

	@Generic( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, fragment, inheritedState, model, lower, upper, stride):
		lowerView = SREInnerFragment( lower, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if lower is not None   else None
		upperView = SREInnerFragment( upper, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if upper is not None   else None
		strideView = SREInnerFragment( stride, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if stride is not None   else None
		return subscriptLongSlice( lowerView, upperView, strideView )

	@Generic( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, fragment, inheritedState, model):
		return subscriptEllipsis()

	@Expression( Schema.SubscriptTuple )
	def SubscriptTuple(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return subscriptTuple( elementViews, trailingSeparator is not None )

	@Expression( Schema.Subscript )
	def Subscript(self, fragment, inheritedState, model, target, index):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET )
		indexView = SREInnerFragment( index, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )
		return subscript( targetView, indexView )




	# Call
	@Generic( Schema.CallKWArg )
	def CallKWArg(self, fragment, inheritedState, model, name, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_CALLARG )
		return callKWArg( name, valueView )

	@Generic( Schema.CallArgList )
	def CallArgList(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_CALLARG )
		return callArgList( valueView )

	@Generic( Schema.CallKWArgList )
	def CallKWArgList(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_CALLARG )
		return callKWArgList( valueView )

	@Expression( Schema.Call )
	def Call(self, fragment, inheritedState, model, target, args, argsTrailingSeparator):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_CALLTARGET )
		argViews = SREInnerFragment.map( args, PRECEDENCE_CONTAINER_CALLARG )
		return call( targetView, argViews, argsTrailingSeparator is not None )





	# Operators
	@Expression( Schema.Pow )
	def Pow(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = SREInnerFragment( x, xPrec )
		yView = SREInnerFragment( y, yPrec, EditMode.EDIT )
		return exponent( xView, yView )


	@Expression( Schema.Invert )
	def Invert(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '~' )

	@Expression( Schema.Negate )
	def Negate(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '-' )

	@Expression( Schema.Pos )
	def Pos(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '+' )


	@Expression( Schema.Mul )
	def Mul(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '*' )

	@Expression( Schema.Div )
	def Div(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = SREInnerFragment( x, xPrec, EditMode.EDIT )
		yView = SREInnerFragment( y, yPrec, EditMode.EDIT )
		#<NO_TREE_EVENT_LISTENER>
		view = div( xView, yView, '/' )
		return BreakableStructuralItem( PythonSyntaxRecognizingEditor.instance, model, view )

	@Expression( Schema.Mod )
	def Mod(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '%' )


	@Expression( Schema.Add )
	def Add(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '+' )

	@Expression( Schema.Sub )
	def Sub(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '-' )


	@Expression( Schema.LShift )
	def LShift(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '<<' )

	@Expression( Schema.RShift )
	def RShift(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '>>' )


	@Expression( Schema.BitAnd )
	def BitAnd(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '&' )

	@Expression( Schema.BitXor )
	def BitXor(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '^' )

	@Expression( Schema.BitOr )
	def BitOr(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '|' )


	@Expression( Schema.Cmp )
	def Cmp(self, fragment, inheritedState, model, x, ops):
		xView = SREInnerFragment( x, PRECEDENCE_CMP )
		opViews = SREInnerFragment.map( ops, PRECEDENCE_CMP )
		return compare( xView, opViews )

	@Generic( Schema.CmpOpLte )
	def CmpOpLte(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '<=', y )

	@Generic( Schema.CmpOpLt )
	def CmpOpLt(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '<', y )

	@Generic( Schema.CmpOpGte )
	def CmpOpGte(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '>=', y )

	@Generic( Schema.CmpOpGt )
	def CmpOpGt(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '>', y )

	@Generic( Schema.CmpOpEq )
	def CmpOpEq(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '==', y )

	@Generic( Schema.CmpOpNeq )
	def CmpOpNeq(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, '!=', y )

	@Generic( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'is not', y )

	@Generic( Schema.CmpOpIs )
	def CmpOpIs(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'is', y )

	@Generic( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'not in', y )

	@Generic( Schema.CmpOpIn )
	def CmpOpIn(self, fragment, inheritedState, model, y):
		return spanCmpOpView( self._parser, inheritedState, model, 'in', y )



	@Generic( Schema.NotTest )
	def NotTest(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, 'not ' )

	@Generic( Schema.AndTest )
	def AndTest(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, 'and' )

	@Generic( Schema.OrTest )
	def OrTest(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, 'or' )





	# Parameters
	@Generic( Schema.SimpleParam )
	def SimpleParam(self, fragment, inheritedState, model, name):
		return simpleParam( name )

	@Generic( Schema.DefaultValueParam )
	def DefaultValueParam(self, fragment, inheritedState, model, name, defaultValue):
		valueView = SREInnerFragment( defaultValue, PRECEDENCE_NONE )
		return defaultValueParam( name, valueView )

	@Generic( Schema.ParamList )
	def ParamList(self, fragment, inheritedState, model, name):
		return paramList( name )

	@Generic( Schema.KWParamList )
	def KWParamList(self, fragment, inheritedState, model, name):
		return kwParamList( name )



	# Lambda expression
	@Expression( Schema.LambdaExpr )
	def LambdaExpr(self, fragment, inheritedState, model, params, paramsTrailingSeparator, expr):
		exprView = SREInnerFragment( expr, PRECEDENCE_CONTAINER_LAMBDAEXPR )
		paramViews = SREInnerFragment.map( params, PRECEDENCE_NONE )

		return lambdaExpr( paramViews, paramsTrailingSeparator is not None, exprView )



	# Conditional expression
	@Expression( Schema.ConditionalExpr )
	def ConditionalExpr(self, fragment, inheritedState, model, condition, expr, elseExpr):
		conditionView = SREInnerFragment( condition, PRECEDENCE_CONTAINER_CONDITIONALEXPR )
		exprView = SREInnerFragment( expr, PRECEDENCE_CONTAINER_CONDITIONALEXPR )
		elseExprView = SREInnerFragment( elseExpr, PRECEDENCE_CONTAINER_CONDITIONALEXPR )
		return conditionalExpr( conditionView, exprView, elseExprView )




	#
	#
	# QUOTE AND UNQUOTE
	#
	#

	# Quote
	@SpecialFormExpression( Schema.Quote )
	def Quote(self, fragment, inheritedState, model, value):
		if isinstance( value, DMObject ):
			if value.isInstanceOf( Schema.PythonExpression ):
				title = 'QUOTE - Expr'
			elif value.isInstanceOf( Schema.PythonSuite ):
				title = 'QUOTE - Suite'
			else:
				raise TypeError, 'Contents of \'quote\' should be a PythonExpression or a PythonSuite'

			valueView = perspective.applyTo( SREInnerFragment( value, PRECEDENCE_CONTAINER_QUOTE ) )
		else:
			raise TypeError, 'Value of \'quote\' should be a DMObject'


		return quote( valueView, title, PythonSyntaxRecognizingEditor.instance )



	# Unquote
	@SpecialFormExpression( Schema.Unquote )
	def Unquote(self, fragment, inheritedState, model, value):
		if isinstance( value, DMObject ):
			valueView = perspective.applyTo( SREInnerFragment( value, PRECEDENCE_CONTAINER_QUOTE ) )
		else:
			raise TypeError, 'Value of \'unquote\' should be a DMObject'


		return unquote( valueView, 'UNQUOTE', PythonSyntaxRecognizingEditor.instance )



	#
	#
	# EXTERNAL EXPRESSION
	#
	#

	# External expression
	@SpecialFormExpression( Schema.ExternalExpr )
	def ExternalExpr(self, fragment, inheritedState, model, expr):
		if isinstance( expr, DMObject ):
			schema = expr.getDMObjectClass().getSchema()
			presenter, title = ExternalExpression.getExternalExpressionPresenterAndTitle( schema )
			exprView = presenter( expr )
		else:
			exprView = Label( '<expr>' )
			title = 'ext'

		def _onDeleteButton(button, event):
			pyReplaceNode( model, Schema.Load( name='None' ) )


		deleteButton = Button( Image.systemIcon( 'delete_tiny' ), _onDeleteButton )

		return externalExpr( exprView, title, deleteButton )



	#
	#
	# INLINE OBJECT
	#
	#

	# Inline object expression
	@SpecialFormExpression( Schema.InlineObjectExpr )
	def InlineObjectExpr(self, fragment, inheritedState, model, embeddedValue):
		value = embeddedValue.getValue()
		valueView = ApplyPerspective( None, value )

		try:
			modelFn = value.__py_model__
		except AttributeError:
			# Standard view
			view = inlineObject( valueView )
			return view.withContextMenuInteractor( _inlineObjectExprContextMenuFactory )
		else:
			# Macro view
			def createModelView():
				return Pres.coerce( modelFn() )
			view = inlineObjectMacro( valueView, LazyPres( createModelView ) )
			return view.withContextMenuInteractor( _inlineObjectExprContextMenuFactory )


	# Inline object statement
	@SpecialFormStatement( Schema.InlineObjectStmt )
	def InlineObjectStmt(self, fragment, inheritedState, model, embeddedValue):
		value = embeddedValue.getValue()
		valueView = ApplyPerspective( None, value )

		try:
			modelFn = value.__py_model__
		except AttributeError:
			# Standard view
			view = inlineObject( valueView )
			return view.withContextMenuInteractor( _inlineObjectStmtContextMenuFactory )
		else:
			# Macro view
			def createModelView():
				return Pres.coerce( modelFn() )
			view = inlineObjectMacro( valueView, LazyPres( createModelView ) )
			return view.withContextMenuInteractor( _inlineObjectStmtContextMenuFactory )



	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Unparsed statement
	@UnparsedStatement( Schema.UnparsedStmt )
	def UnparsedStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return unparsedStmt( valueView )



	# Expression statement
	@Statement( Schema.ExprStmt )
	def ExprStmt(self, fragment, inheritedState, model, expr):
		exprView = SREInnerFragment( expr, PRECEDENCE_STMT )
		return exprStmt( exprView )



	# Assert statement
	@Statement( Schema.AssertStmt )
	def AssertStmt(self, fragment, inheritedState, model, condition, fail):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		failView = SREInnerFragment( fail, PRECEDENCE_STMT )   if fail is not None   else None
		return assertStmt( conditionView, failView )


	# Assignment statement
	@Statement( Schema.AssignStmt )
	def AssignStmt(self, fragment, inheritedState, model, targets, value):
		targetViews = SREInnerFragment.map( targets, PRECEDENCE_STMT )
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return assignStmt( targetViews, valueView )


	# Augmented assignment statement
	@Statement( Schema.AugAssignStmt )
	def AugAssignStmt(self, fragment, inheritedState, model, op, target, value):
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return augAssignStmt( op, targetView, valueView )


	# Pass statement
	@Statement( Schema.PassStmt )
	def PassStmt(self, fragment, inheritedState, model):
		return passStmt()


	# Del statement
	@Statement( Schema.DelStmt )
	def DelStmt(self, fragment, inheritedState, model, target):
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )
		return delStmt( targetView )


	# Return statement
	@Statement( Schema.ReturnStmt )
	def ReturnStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return returnStmt( valueView )


	# Yield statement
	@Statement( Schema.YieldStmt )
	def YieldStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return yieldStmt( valueView )


	# Raise statement
	@Statement( Schema.RaiseStmt )
	def RaiseStmt(self, fragment, inheritedState, model, excType, excValue, traceback):
		excTypeView = SREInnerFragment( excType, PRECEDENCE_STMT )   if excType is not None   else None
		excValueView = SREInnerFragment( excValue, PRECEDENCE_STMT )   if excValue is not None   else None
		tracebackView = SREInnerFragment( traceback, PRECEDENCE_STMT )   if traceback is not None   else None
		return raiseStmt( excTypeView, excValueView, tracebackView )


	# Break statement
	@Statement( Schema.BreakStmt )
	def BreakStmt(self, fragment, inheritedState, model):
		return breakStmt()


	# Continue statement
	@Statement( Schema.ContinueStmt )
	def ContinueStmt(self, fragment, inheritedState, model):
		return continueStmt()


	# Import statement
	@Generic( Schema.RelativeModule )
	def RelativeModule(self, fragment, inheritedState, model, name):
		return relativeModule( name )

	@Generic( Schema.ModuleImport )
	def ModuleImport(self, fragment, inheritedState, model, name):
		return moduleImport( name )

	@Generic( Schema.ModuleImportAs )
	def ModuleImportAs(self, fragment, inheritedState, model, name, asName):
		return moduleImportAs( name, asName )

	@Generic( Schema.ModuleContentImport )
	def ModuleContentImport(self, fragment, inheritedState, model, name):
		return moduleContentImport( name )

	@Generic( Schema.ModuleContentImportAs )
	def ModuleContentImportAs(self, fragment, inheritedState, model, name, asName):
		return moduleContentImportAs( name, asName )

	@Statement( Schema.ImportStmt )
	def ImportStmt(self, fragment, inheritedState, model, modules):
		moduleViews = SREInnerFragment.map( modules, PRECEDENCE_STMT )
		return importStmt( moduleViews )

	@Statement( Schema.FromImportStmt )
	def FromImportStmt(self, fragment, inheritedState, model, module, imports):
		moduleView = SREInnerFragment( module, PRECEDENCE_STMT )
		importViews = SREInnerFragment.map( imports, PRECEDENCE_STMT )
		return fromImportStmt( moduleView, importViews )

	@Statement( Schema.FromImportAllStmt )
	def FromImportAllStmt(self, fragment, inheritedState, model, module):
		moduleView = SREInnerFragment( module, PRECEDENCE_STMT )
		return fromImportAllStmt( moduleView )


	# Global statement
	@Generic( Schema.GlobalVar )
	def GlobalVar(self, fragment, inheritedState, model, name):
		return globalVar( name )

	@Statement( Schema.GlobalStmt )
	def GlobalStmt(self, fragment, inheritedState, model, vars):
		varViews = SREInnerFragment.map( vars, PRECEDENCE_STMT )
		return globalStmt( varViews )



	# Exec statement
	@Statement( Schema.ExecStmt )
	def ExecStmt(self, fragment, inheritedState, model, source, globals, locals):
		sourceView = SREInnerFragment( source, PRECEDENCE_STMT )
		globalsView = SREInnerFragment( globals, PRECEDENCE_STMT )    if globals is not None   else None
		localsView = SREInnerFragment( locals, PRECEDENCE_STMT )   if locals is not None   else None
		return execStmt( sourceView, globalsView, localsView )






	# Exec statement
	@Statement( Schema.PrintStmt )
	def PrintStmt(self, fragment, inheritedState, model, destination, values):
		destView = SREInnerFragment( destination, PRECEDENCE_STMT )   if destination is not None   else None
		valueViews = SREInnerFragment.map( values, PRECEDENCE_STMT )
		return printStmt( destView, valueViews )




	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def _ifStmtHeaderElement(self, inheritedState, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		return ifStmtHeader( conditionView )

	@CompoundStatementHeader( Schema.IfStmtHeader )
	def IfStmtHeader(self, fragment, inheritedState, model, condition):
		return self._ifStmtHeaderElement( inheritedState, condition )


	# Elif statement
	def _elifStmtHeaderElement(self, inheritedState, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		return elifStmtHeader( conditionView )

	@CompoundStatementHeader( Schema.ElifStmtHeader )
	def ElifStmtHeader(self, fragment, inheritedState, model, condition):
		return self._elifStmtHeaderElement( inheritedState, condition )



	# Else statement
	def _elseStmtHeaderElement(self, inheritedState):
		return elseStmtHeader()

	@CompoundStatementHeader( Schema.ElseStmtHeader )
	def ElseStmtHeader(self, fragment, inheritedState, model):
		return self._elseStmtHeaderElement( inheritedState )


	# While statement
	def _whileStmtHeaderElement(self, inheritedState, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		return whileStmtHeader( conditionView )

	@CompoundStatementHeader( Schema.WhileStmtHeader )
	def WhileStmtHeader(self, fragment, inheritedState, model, condition):
		return self._whileStmtHeaderElement( inheritedState, condition )


	# For statement
	def _forStmtHeaderElement(self, inheritedState, target, source):
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )
		sourceView = SREInnerFragment( source, PRECEDENCE_STMT )
		return forStmtHeader( targetView, sourceView )

	@CompoundStatementHeader( Schema.ForStmtHeader )
	def ForStmtHeader(self, fragment, inheritedState, model, target, source):
		return self._forStmtHeaderElement( inheritedState, target, source )



	# Try statement
	def _tryStmtHeaderElement(self, inheritedState):
		return tryStmtHeader()

	@CompoundStatementHeader( Schema.TryStmtHeader )
	def TryStmtHeader(self, fragment, inheritedState, model):
		return self._tryStmtHeaderElement( inheritedState )



	# Except statement
	def _exceptStmtHeaderElement(self, inheritedState, exception, target):
		excView = SREInnerFragment( exception, PRECEDENCE_STMT )   if exception is not None   else None
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )   if target is not None   else None
		return exceptStmtHeader( excView, targetView )

	@CompoundStatementHeader( Schema.ExceptStmtHeader )
	def ExceptStmtHeader(self, fragment, inheritedState, model, exception, target):
		return self._exceptStmtHeaderElement( inheritedState, exception, target )



	# Finally statement
	def _finallyStmtHeaderElement(self, inheritedState):
		return finallyStmtHeader()

	@CompoundStatementHeader( Schema.FinallyStmtHeader )
	def FinallyStmtHeader(self, fragment, inheritedState, model):
		return self._finallyStmtHeaderElement( inheritedState )



	# With statement
	def _withStmtHeaderElement(self, inheritedState, expr, target):
		exprView = SREInnerFragment( expr, PRECEDENCE_STMT )
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )   if target is not None   else None
		return withStmtHeader( exprView, targetView )

	@CompoundStatementHeader( Schema.WithStmtHeader )
	def WithStmtHeader(self, fragment, inheritedState, model, expr, target):
		return self._withStmtHeaderElement( inheritedState, expr, target )



	# Decorator statement
	def _decoStmtHeaderElement(self, inheritedState, name, args, argsTrailingSeparator):
		argViews = SREInnerFragment.map( args, PRECEDENCE_STMT )   if args is not None   else None
		return decoStmtHeader( name, argViews, argsTrailingSeparator is not None )

	@CompoundStatementHeader( Schema.DecoStmtHeader )
	def DecoStmtHeader(self, fragment, inheritedState, model, name, args, argsTrailingSeparator):
		return self._decoStmtHeaderElement( inheritedState, name, args, argsTrailingSeparator )



	# Def statement
	def _defStmtHeaderElement(self, inheritedState, name, params, paramsTrailingSeparator):
		paramViews = SREInnerFragment.map( params, PRECEDENCE_STMT )
		return defStmtHeader( name, paramViews, paramsTrailingSeparator is not None )

	@CompoundStatementHeader( Schema.DefStmtHeader )
	def DefStmtHeader(self, fragment, inheritedState, model, name, params, paramsTrailingSeparator):
		return self._defStmtHeaderElement( inheritedState, name, params, paramsTrailingSeparator ), defStmtHeaderHighlight, defStmtHighlight


	# Def statement
	def _classStmtHeaderElement(self, inheritedState, name, bases, basesTrailingSeparator):
		baseViews = SREInnerFragment.map( bases, PRECEDENCE_CONTAINER_ELEMENT )   if bases is not None   else None
		return classStmtHeader( name, baseViews, basesTrailingSeparator is not None )

	@CompoundStatementHeader( Schema.ClassStmtHeader )
	def ClassStmtHeader(self, fragment, inheritedState, model, name, bases, basesTrailingSeparator):
		return self._classStmtHeaderElement( inheritedState, name, bases, basesTrailingSeparator ), classStmtHeaderHighlight, classStmtHighlight




	#
	#
	# STRUCTURE STATEMENTS
	#
	#

	# Indented block
	@DMObjectNodeDispatchMethod( Schema.IndentedBlock )
	def IndentedBlock(self, fragment, inheritedState, model, suite):
		indent = StructuralItem( Schema.Indent(), indentElement() )

		lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )

		dedent = StructuralItem( Schema.Dedent(), dedentElement() )

		suiteElement = badIndentedBlock( indent, lineViews, dedent )
		suiteElement = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance,
		                                       PythonSyntaxRecognizingEditor.instance.parsingNodeEditListener( 'Suite', self._parser.compoundSuite(), _makeSuiteCommitFn( suite ) ),
		                                       model, suiteElement )

		return suiteElement





	#
	#
	# COMPOUND STATEMENTS
	#
	#

	# If statement
	@CompoundStatement( Schema.IfStmt )
	def IfStmt(self, fragment, inheritedState, model, condition, suite, elifBlocks, elseSuite):
		compoundBlocks = [ ( Schema.IfStmtHeader( condition=condition ), self._ifStmtHeaderElement( inheritedState, condition ), suite ) ]
		for b in elifBlocks:
			if not b.isInstanceOf( Schema.ElifBlock ):
				raise TypeError, 'IfStmt elifBlocks should only contain ElifBlock instances'
			compoundBlocks.append( ( Schema.ElifStmtHeader( condition=b['condition'] ), self._elifStmtHeaderElement( inheritedState, b['condition'] ),  b['suite'] ) )
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundBlocks



	# While statement
	@CompoundStatement( Schema.WhileStmt )
	def WhileStmt(self, fragment, inheritedState, model, condition, suite, elseSuite):
		compoundBlocks = [ ( Schema.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( inheritedState, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundBlocks



	# For statement
	@CompoundStatement( Schema.ForStmt )
	def ForStmt(self, fragment, inheritedState, model, target, source, suite, elseSuite):
		compoundBlocks = [ ( Schema.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( inheritedState, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundBlocks



	# Try statement
	@CompoundStatement( Schema.TryStmt )
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
		return compoundBlocks




	# With statement
	@CompoundStatement( Schema.WithStmt )
	def WithStmt(self, fragment, inheritedState, model, expr, target, suite):
		return [ ( Schema.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( inheritedState, expr, target ), suite ) ]



	# Def statement
	@CompoundStatement( Schema.DefStmt )
	def DefStmt(self, fragment, inheritedState, model, decorators, name, params, paramsTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Schema.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Schema.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
			                         self._decoStmtHeaderElement( inheritedState, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )

		compoundBlocks.append( ( Schema.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ),
		                         self._defStmtHeaderElement( inheritedState, name, params, paramsTrailingSeparator ), suite,
		                         defStmtHeaderHighlight ) )
		return compoundBlocks, defStmtHighlight


	# Class statement
	@CompoundStatement( Schema.ClassStmt )
	def ClassStmt(self, fragment, inheritedState, model, decorators, name, bases, basesTrailingSeparator, suite):
		compoundBlocks = []
		for d in decorators:
			if not d.isInstanceOf( Schema.Decorator ):
				raise TypeError, 'DefStmt decorators should only contain Decorator instances'
			compoundBlocks.append( ( Schema.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ), 
			                         self._decoStmtHeaderElement( inheritedState, d['name'], d['args'], d['argsTrailingSeparator'] ),  None ) )

		compoundBlocks.append( ( Schema.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ),
		                     self._classStmtHeaderElement( inheritedState, name, bases, basesTrailingSeparator ), suite,
		                     classStmtHeaderHighlight ) )
		return compoundBlocks, classStmtHighlight





_parser = Python25Grammar()
perspective = SequentialEditorPerspective( Python25View( _parser ), PythonSyntaxRecognizingEditor.instance )



