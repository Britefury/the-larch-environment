##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import Throwable

from java.awt.event import KeyEvent

from java.util import List

from BritefuryJ.Parser import ParserExpression

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Kernel.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, ObjectDispatchMethod


from BritefuryJ.DocModel import DMObjectClass

from BritefuryJ.AttributeTable import *
from BritefuryJ.Controls import *
from BritefuryJ.LSpace import ElementValueFunction, TextEditEvent
from BritefuryJ.LSpace.Interactor import KeyElementInteractor
from BritefuryJ.LSpace.StreamValue import StreamValueBuilder
from BritefuryJ.LSpace.Input import ObjectDndHandler
from BritefuryJ.LSpace.Marker import Marker

from BritefuryJ.Pres import ApplyPerspective
from BritefuryJ.Pres.Primitive import Paragraph, Segment

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment
from BritefuryJ.Editor.SyntaxRecognizing.SRFragmentEditor import EditMode

from BritefuryJ.ModelAccess.DocModel import *



from LarchCore.Languages.Python25 import Schema
from LarchCore.Languages.Python25 import PythonCommands


from LarchCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from LarchCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from LarchCore.Languages.Python25.PythonEditor.NodeEditor import *
from LarchCore.Languages.Python25.PythonEditor.SREditor import *
from LarchCore.Languages.Python25.PythonEditor.Keywords import *
from LarchCore.Languages.Python25.PythonEditor.Precedence import *
from LarchCore.Languages.Python25.PythonEditor.PythonEditorCombinators import *




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
	# Unparsed statement is only valid if there is ONE newline, and it is at the end
	i = value.indexOf( '\n' )
	return i != -1   and   i == len( value ) - 1

def _commitUnparsedStatment(model, value):
	withoutNewline = value[:-1]
	unparsed = Schema.UnparsedStmt( value=Schema.UNPARSED( value=withoutNewline.getItemValues() ) )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	pyReplaceNode( model, deepcopy( unparsed ) )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	pyReplaceNode( model, deepcopy( unparsed ) )



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



def _onDrop_embeddedObject(element, pos, data, action):
	def _displayModelException(e):
		ApplyPerspective( None, Pres.coerce( e ) ).popupAtMousePosition( element, True, True )

	marker = Marker.atPointIn( element, pos, True )
	if marker is not None  and  marker.isValid():
		def _performInsertion(model):
			embeddedValue = DMNode.embedIsolated( model, False )
			try:
				modelType = Schema.getEmbeddedObjectModelType( model )
			except Exception, e:
				_displayModelException( e )
			else:
				if modelType is Schema.Expr:
					expr = Schema.EmbeddedObjectExpr( embeddedValue=embeddedValue )
					insertSpecialFormExpressionAtMarker( marker, expr )
				elif modelType is Schema.Stmt:
					stmt = Schema.EmbeddedObjectStmt( embeddedValue=embeddedValue )
					insertSpecialFormStatementAtMarker( marker, stmt )
		
		# Display a context menu
		def _onDropByCopy(control):
			if marker.isValid():
				model = data.getModel()
				_performInsertion( deepcopy( model ) )
		
		def _onDropByRef(control):
			if marker.isValid():
				model = data.getModel()
				_performInsertion( model )

		menu = VPopupMenu( [ MenuItem.menuItemWithLabel( 'Copy', _onDropByCopy ),
		                     MenuItem.menuItemWithLabel( 'Reference', _onDropByRef ) ] )
		menu.popupAtMousePosition( marker.getElement() )
	return True


_embeddedObject_dropDest = ObjectDndHandler.DropDest( FragmentData, _onDrop_embeddedObject )



def _removeEmbeddedObjectExpr(model):
	value = model['embeddedValue'].getValue()

	try:
		replacementFn = value.__py_replacement__
	except AttributeError:
		replacementFn = None

	if replacementFn is not None:
		replacement = replacementFn()
		pyReplaceNode( model, replacement )
	else:
		pyReplaceNode( model, Schema.Load( name='None' ) )


def _removeEmbeddedObjectStmt(model):
	value = model['embeddedValue'].getValue()

	try:
		replacementFn = value.__py_replacement__
	except AttributeError:
		replacementFn = None

	if replacementFn is not None:
		replacement = replacementFn()
		if isinstance( replacement, list )  or  isinstance( replacement, tuple )  or  isinstance( replacement, List ):
			pyReplaceStatementWithStatementRange( model, replacement )
		else:
			pyReplaceNode( model, replacement )
	else:
		pyReplaceNode( model, Schema.BlankLine() )



class _EmbeddedObjectExprTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( RemoveEmbeddedObjectTreeEvent )
	def _removeEmbeddedObjectTreeEvent(self, element, sourceElement, event):
		model = element.getFragmentContext().getModel()
		_removeEmbeddedObjectExpr( model )
		return True

_EmbeddedObjectExprTreeEventListener.instance = _EmbeddedObjectExprTreeEventListener()



class _EmbeddedObjectStmtTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( RemoveEmbeddedObjectTreeEvent )
	def _removeEmbeddedObjectTreeEvent(self, element, sourceElement, event):
		model = element.getFragmentContext().getModel()
		_removeEmbeddedObjectStmt( model )
		return True

_EmbeddedObjectStmtTreeEventListener.instance = _EmbeddedObjectStmtTreeEventListener()



def _embeddedObjectExprContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		_removeEmbeddedObjectExpr( model )

	menu.add( MenuItem.menuItemWithLabel( 'Delete embedded object', _onDelete ) )

	return False


def _embeddedObjectStmtContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		_removeEmbeddedObjectStmt( model )

	menu.add( MenuItem.menuItemWithLabel( 'Delete embedded object', _onDelete ) )

	return False



def _pythonModuleContextMenuFactory(element, menu):
	rootElement = element.getRootElement()


	extExprItems = []

	def _onQuoteExpr(item):
		caret = rootElement.getCaret()
		if caret.isValid():
			pyExpr = Schema.Quote( value=Schema.PythonExpression( expr=Schema.Load( name='None' ) ) )
			insertSpecialFormExpressionAtCaret( caret, pyExpr )

	def _onQuoteSuite(item):
		caret = rootElement.getCaret()
		if caret.isValid():
			pyExpr = Schema.Quote( value=Schema.PythonSuite( suite=[] ) )
			insertSpecialFormExpressionAtCaret( caret, pyExpr )

	def _onUnquote(item):
		caret = rootElement.getCaret()
		if caret.isValid():
			pyExpr = Schema.Unquote( value=Schema.PythonExpression( expr=Schema.Load( name='None' ) ) )
			insertSpecialFormExpressionAtCaret( caret, pyExpr )


	menu.add( MenuItem.menuItemWithLabel( 'Quote expression', _onQuoteExpr ) )
	menu.add( MenuItem.menuItemWithLabel( 'Quote suite', _onQuoteSuite ) )
	menu.add( MenuItem.menuItemWithLabel( 'Unquote', _onUnquote ) )

	return True



def _pythonTargetContextMenuFactory(element, menu):
	return True

	
def _setUnwrappedMethod(method, m):
	m.__dispatch_unwrapped_method__ = method
	m.__name__ = method.__name__
	return m



def Unparsed(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._unparsedFragmentEditor.editFragment( v, model, inheritedState )
	return _setUnwrappedMethod( method, _m )
		


def UnparsedStatement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._unparsedStatementFragmentEditor.editFragment( statementLine( v ), model, inheritedState )
	return _setUnwrappedMethod( method, _m )
		


def Expression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._expressionFragmentEditor.editFragment( v, model, inheritedState )
	return _setUnwrappedMethod( method, _m )



def Statement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._statementFragmentEditor.editFragment( statementLine( v ), model, inheritedState )
	return _setUnwrappedMethod( method, _m )


	
def CompoundStatementHeader(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		if isinstance( v, tuple ):
			e = self._compoundStatementHeaderFragmentEditor.editFragment( statementLine( v[0] ), model, inheritedState )
			for f in v[1:]:
				e = f( e )
			return e
		else:
			return self._compoundStatementHeaderFragmentEditor.editFragment( statementLine( v ), model, inheritedState )
	return _setUnwrappedMethod( method, _m )



def CompoundStatement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		if isinstance( v, tuple ):
			f = v[1]
			return f( compoundStatementEditor( self, inheritedState, model, v[0] ) )
		else:
			return compoundStatementEditor( self, inheritedState, model, v )
	return _setUnwrappedMethod( method, _m )



def SpecialFormExpression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return StructuralItem( model, v )
	return _setUnwrappedMethod( method, _m )



def SpecialFormStatement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		v = StructuralItem( model, v )
		v = specialFormStatementLine( v )
		return self._specialFormStatementFragmentEditor.editFragment( v, model, inheritedState )
	return _setUnwrappedMethod( method, _m )




class Python25View (MethodDispatchView):
	def __init__(self, grammar):
		super( Python25View, self ).__init__()
		self._parser = grammar
		
		editor = PythonSyntaxRecognizingEditor.instance
		
		self._expr = editor.parsingNodeEditListener( 'Expression', grammar.expression(), pyReplaceNode )
		self._stmt = editor.parsingNodeEditListener( 'Statement', grammar.simpleSingleLineStatementValid(), pyReplaceNode )
		self._compHdr = editor.partialParsingNodeEditListener( 'Compound header', grammar.compoundStmtHeader() )
		self._stmtUnparsed = editor.unparsedNodeEditListener( 'Unparsed statement', _isValidUnparsedStatementValue, _commitUnparsedStatment, _commitInnerUnparsed )
		self._topLevel = editor.topLevelNodeEditListener()
		self._exprOuter = PythonExpressionEditListener( grammar.tupleOrExpression() )
		self._exprTopLevel = PythonExpressionTopLevelEditListener()
		self._targetOuter = PythonTargetEditListener( grammar.targetListOrTargetItem() )
		self._targetTopLevel = PythonTargetTopLevelEditListener()
		
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
			# Empty module - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )
		s = suiteView( lineViews ).alignHPack().alignVRefY()
		s = s.withDropDest( _embeddedObject_dropDest )
		s = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._makeSuiteEditListener( suite ), self._topLevel ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		s = s.withCommands( PythonCommands.pythonTargetCommands )
		s = s.withCommands( PythonCommands.pythonCommands )
		return ApplyStyleSheetFromAttribute( PythonEditorStyle.paragraphIndentationStyle, s )



	@DMObjectNodeDispatchMethod( Schema.PythonSuite )
	def PythonSuite(self, fragment, inheritedState, model, suite):
		if len( suite ) == 0:
			# Empty suite - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )
		s = suiteView( lineViews ).alignHPack().alignVRefY()
		s = s.withDropDest( _embeddedObject_dropDest )
		s = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._makeSuiteEditListener( suite ), self._topLevel ], suite, s )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		s = s.withCommands( PythonCommands.pythonTargetCommands )
		s = s.withCommands( PythonCommands.pythonCommands )
		return ApplyStyleSheetFromAttribute( PythonEditorStyle.paragraphIndentationStyle, s )



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
		e = e.withDropDest( _embeddedObject_dropDest )
		e = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._exprOuter, self._exprTopLevel ],  model,  e )
		e = e.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		e = e.withCommands( PythonCommands.pythonTargetCommands )
		e = e.withCommands( PythonCommands.pythonCommands )
		return e



	@DMObjectNodeDispatchMethod( Schema.PythonTarget )
	def PythonTarget(self, fragment, inheritedState, model, target):
		if target is None:
			# Empty document - create a single blank line so that there is something to edit
			targetView = blankLine()
			seg = targetView
		else:
			targetView = SREInnerFragment( target, PRECEDENCE_NONE, EditMode.DISPLAY )
			seg = Segment( targetView )
		t = Paragraph( [ seg ] ).alignHPack().alignVRefY()
		t = EditableStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._targetOuter, self._targetTopLevel ],  model,  t )
		t = t.withContextMenuInteractor( _pythonTargetContextMenuFactory )
		t = t.withCommands( PythonCommands.pythonTargetCommands )
		return t



	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	@Statement
	def BlankLine(self, fragment, inheritedState, model):
		return blankLine()


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	@Unparsed
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
	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	@Statement
	def CommentStmt(self, fragment, inheritedState, model, comment):
		return commentStmt( comment )





	# String literal
	__strLit_fmtTable = { 'ascii' : None,  'unicode' : 'u',  'ascii-regex' : 'r',  'unicode-regex' : 'ur' }

	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	@Expression
	def StringLiteral(self, fragment, inheritedState, model, format, quotation, value):
		fmt = self.__strLit_fmtTable[format]

		quote = "'"   if quotation == 'single'   else   '"'

		return stringLiteral( fmt, quote, value )

	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	@Expression
	def IntLiteral(self, fragment, inheritedState, model, format, numType, value):
		boxContents = []

		if numType == 'long':
			if format == 'hex':
				valueString = '0x%x'  %  long( value, 16 )
			else:
				valueString = '%d'  %  long( value )
			fmt = 'L'
		else:
			if format == 'hex':
				valueString = '0x%x'  %  int( value, 16 )
			else:
				valueString = '%d'  %  int( value )
			fmt = None

		return intLiteral( fmt, valueString )



	# Float literal
	@DMObjectNodeDispatchMethod( Schema.FloatLiteral )
	@Expression
	def FloatLiteral(self, fragment, inheritedState, model, value):
		return floatLiteral( value )



	# Imaginary literal
	@DMObjectNodeDispatchMethod( Schema.ImaginaryLiteral )
	@Expression
	def ImaginaryLiteral(self, fragment, inheritedState, model, value):
		return imaginaryLiteral( value )



	# Targets
	@DMObjectNodeDispatchMethod( Schema.SingleTarget )
	@Expression
	def SingleTarget(self, fragment, inheritedState, model, name):
		return singleTarget( name )


	@DMObjectNodeDispatchMethod( Schema.TupleTarget )
	@Expression
	def TupleTarget(self, fragment, inheritedState, model, targets, trailingSeparator):
		elementViews = SREInnerFragment.map( targets, PRECEDENCE_CONTAINER_ELEMENT )
		return tupleTarget( elementViews, trailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.ListTarget )
	@Expression
	def ListTarget(self, fragment, inheritedState, model, targets, trailingSeparator):
		elementViews = SREInnerFragment.map( targets, PRECEDENCE_CONTAINER_ELEMENT )
		return listTarget( elementViews, trailingSeparator is not None )



	# Variable reference
	@DMObjectNodeDispatchMethod( Schema.Load )
	@Expression
	def Load(self, fragment, inheritedState, model, name):
		return load( name )



	# Tuple literal
	@DMObjectNodeDispatchMethod( Schema.TupleLiteral )
	@Expression
	def TupleLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return tupleLiteral( elementViews, trailingSeparator is not None )



	# List literal
	@DMObjectNodeDispatchMethod( Schema.ListLiteral )
	@Expression
	def ListLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return listLiteral( elementViews, trailingSeparator is not None )



	# List comprehension / generator expression
	@DMObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, fragment, inheritedState, model, target, source):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_COMPREHENSIONFOR)
		sourceView = SREInnerFragment( source, PRECEDENCE_CONTAINER_COMPREHENSIONFOR )
		return comprehensionFor( targetView, sourceView )

	@DMObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, fragment, inheritedState, model, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_CONTAINER_COMPREHENSIONIF )
		return comprehensionIf( conditionView )

	@DMObjectNodeDispatchMethod( Schema.ListComp )
	@Expression
	def ListComp(self, fragment, inheritedState, model, resultExpr, comprehensionItems):
		exprView = SREInnerFragment( resultExpr, PRECEDENCE_CONTAINER_ELEMENT )
		itemViews = SREInnerFragment.map( comprehensionItems, PRECEDENCE_CONTAINER_ELEMENT )
		return listComp( exprView, itemViews )


	@DMObjectNodeDispatchMethod( Schema.GeneratorExpr )
	@Expression
	def GeneratorExpr(self, fragment, inheritedState, model, resultExpr, comprehensionItems):
		exprView = SREInnerFragment( resultExpr, PRECEDENCE_CONTAINER_ELEMENT )
		itemViews = SREInnerFragment.map( comprehensionItems, PRECEDENCE_CONTAINER_ELEMENT )
		return genExpr( exprView, itemViews )




	# Dictionary literal
	@DMObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, fragment, inheritedState, model, key, value):
		keyView = SREInnerFragment( key, PRECEDENCE_CONTAINER_ELEMENT )
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_ELEMENT )
		return dictKeyValuePair( keyView, valueView )

	@DMObjectNodeDispatchMethod( Schema.DictLiteral )
	@Expression
	def DictLiteral(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return dictLiteral( elementViews, trailingSeparator is not None )


	# Yield expression
	@DMObjectNodeDispatchMethod( Schema.YieldExpr )
	@Expression
	def YieldExpr(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_YIELDEXPR )   if value is not None   else None
		return yieldExpr( valueView )



	# Attribute ref
	@DMObjectNodeDispatchMethod( Schema.AttributeRef )
	@Expression
	def AttributeRef(self, fragment, inheritedState, model, target, name):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET )
		return attributeRef( targetView, name )



	# Subscript
	@DMObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, fragment, inheritedState, model, lower, upper):
		lowerView = SREInnerFragment( lower, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if lower is not None   else None
		upperView = SREInnerFragment( upper, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if upper is not None   else None
		return subscriptSlice( lowerView, upperView )

	@DMObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, fragment, inheritedState, model, lower, upper, stride):
		lowerView = SREInnerFragment( lower, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if lower is not None   else None
		upperView = SREInnerFragment( upper, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if upper is not None   else None
		strideView = SREInnerFragment( stride, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if stride is not None   else None
		return subscriptLongSlice( lowerView, upperView, strideView )

	@DMObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, fragment, inheritedState, model):
		return subscriptEllipsis()

	@DMObjectNodeDispatchMethod( Schema.SubscriptTuple )
	@Expression
	def SubscriptTuple(self, fragment, inheritedState, model, values, trailingSeparator):
		elementViews = SREInnerFragment.map( values, PRECEDENCE_CONTAINER_ELEMENT )
		return subscriptTuple( elementViews, trailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.Subscript )
	@Expression
	def Subscript(self, fragment, inheritedState, model, target, index):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET )
		indexView = SREInnerFragment( index, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )
		return subscript( targetView, indexView )




	# Call
	@DMObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, fragment, inheritedState, model, name, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_CALLARG )
		return callKWArg( name, valueView )

	@DMObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_CALLARG )
		return callArgList( valueView )

	@DMObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_CONTAINER_CALLARG )
		return callKWArgList( valueView )

	@DMObjectNodeDispatchMethod( Schema.Call )
	@Expression
	def Call(self, fragment, inheritedState, model, target, args, argsTrailingSeparator):
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_CALLTARGET )
		argViews = SREInnerFragment.map( args, PRECEDENCE_CONTAINER_CALLARG )
		return call( targetView, argViews, argsTrailingSeparator is not None )





	# Operators
	@DMObjectNodeDispatchMethod( Schema.Pow )
	@Expression
	def Pow(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_POW, True )
		xView = SREInnerFragment( x, xPrec )
		yView = SREInnerFragment( y, yPrec, EditMode.EDIT )
		return exponent( xView, yView )


	@DMObjectNodeDispatchMethod( Schema.Invert )
	@Expression
	def Invert(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '~' )

	@DMObjectNodeDispatchMethod( Schema.Negate )
	@Expression
	def Negate(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '-' )

	@DMObjectNodeDispatchMethod( Schema.Pos )
	@Expression
	def Pos(self, fragment, inheritedState, model, x):
		return spanPrefixOpView( self._parser, inheritedState, model, x, '+' )


	@DMObjectNodeDispatchMethod( Schema.Mul )
	@Expression
	def Mul(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '*' )

	@DMObjectNodeDispatchMethod( Schema.Div )
	@Expression
	def Div(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = SREInnerFragment( x, xPrec, EditMode.EDIT )
		yView = SREInnerFragment( y, yPrec, EditMode.EDIT )
		#<NO_TREE_EVENT_LISTENER>
		view = div( xView, yView, '/' )
		return BreakableStructuralItem( PythonSyntaxRecognizingEditor.instance, model, view )

	@DMObjectNodeDispatchMethod( Schema.Mod )
	@Expression
	def Mod(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '%' )


	@DMObjectNodeDispatchMethod( Schema.Add )
	@Expression
	def Add(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '+' )

	@DMObjectNodeDispatchMethod( Schema.Sub )
	@Expression
	def Sub(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '-' )


	@DMObjectNodeDispatchMethod( Schema.LShift )
	@Expression
	def LShift(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '<<' )

	@DMObjectNodeDispatchMethod( Schema.RShift )
	@Expression
	def RShift(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '>>' )


	@DMObjectNodeDispatchMethod( Schema.BitAnd )
	@Expression
	def BitAnd(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '&' )

	@DMObjectNodeDispatchMethod( Schema.BitXor )
	@Expression
	def BitXor(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '^' )

	@DMObjectNodeDispatchMethod( Schema.BitOr )
	@Expression
	def BitOr(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, '|' )


	@DMObjectNodeDispatchMethod( Schema.Cmp )
	@Expression
	def Cmp(self, fragment, inheritedState, model, x, ops):
		xView = SREInnerFragment( x, PRECEDENCE_CMP )
		opViews = SREInnerFragment.map( ops, PRECEDENCE_CMP )
		return compare( xView, opViews )

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
		return spanBinOpView( self._parser, inheritedState, model, x, y, 'and' )

	@DMObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, fragment, inheritedState, model, x, y):
		return spanBinOpView( self._parser, inheritedState, model, x, y, 'or' )





	# Parameters
	@DMObjectNodeDispatchMethod( Schema.SimpleParam )
	def SimpleParam(self, fragment, inheritedState, model, name):
		return simpleParam( name )

	@DMObjectNodeDispatchMethod( Schema.TupleParam )
	def TupleParam(self, fragment, inheritedState, model, params, paramsTrailingSeparator):
		paramViews = SREInnerFragment.map( params, PRECEDENCE_NONE )
		return tupleParam( paramViews, paramsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DefaultValueParam )
	def DefaultValueParam(self, fragment, inheritedState, model, param, defaultValue):
		paramView = SREInnerFragment( param, PRECEDENCE_NONE )
		valueView = SREInnerFragment( defaultValue, PRECEDENCE_NONE )
		return defaultValueParam( paramView, valueView )

	@DMObjectNodeDispatchMethod( Schema.ParamList )
	def ParamList(self, fragment, inheritedState, model, name):
		return paramList( name )

	@DMObjectNodeDispatchMethod( Schema.KWParamList )
	def KWParamList(self, fragment, inheritedState, model, name):
		return kwParamList( name )



	# Lambda expression
	@DMObjectNodeDispatchMethod( Schema.LambdaExpr )
	@Expression
	def LambdaExpr(self, fragment, inheritedState, model, params, paramsTrailingSeparator, expr):
		exprView = SREInnerFragment( expr, PRECEDENCE_CONTAINER_LAMBDAEXPR )
		paramViews = SREInnerFragment.map( params, PRECEDENCE_NONE )

		return lambdaExpr( paramViews, paramsTrailingSeparator is not None, exprView )



	# Conditional expression
	@DMObjectNodeDispatchMethod( Schema.ConditionalExpr )
	@Expression
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
	@DMObjectNodeDispatchMethod( Schema.Quote )
	@SpecialFormExpression
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
	@DMObjectNodeDispatchMethod( Schema.Unquote )
	@SpecialFormExpression
	def Unquote(self, fragment, inheritedState, model, value):
		if isinstance( value, DMObject ):
			valueView = perspective.applyTo( SREInnerFragment( value, PRECEDENCE_CONTAINER_QUOTE ) )
		else:
			raise TypeError, 'Value of \'unquote\' should be a DMObject'


		return unquote( valueView, 'UNQUOTE', PythonSyntaxRecognizingEditor.instance )




	#
	#
	# EMBEDDED OBJECT
	#
	#
	

	@staticmethod
	def _getExpansionFn(value):
		try:
			modelFn = value.__py_model__
		except AttributeError:
			return None
		
		try:
			hideExpansion = value.__py_hide_expansion__
		except AttributeError:
			return modelFn
		
		if hideExpansion:
			return None
		else:
			return modelFn
	
	
	# Embedded object expression
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectExpr )
	@SpecialFormExpression
	def EmbeddedObjectExpr(self, fragment, inheritedState, model, embeddedValue):
		value = embeddedValue.getValue()
		valueView = ApplyPerspective( EditPerspective.instance, value )
		expansionFn = self._getExpansionFn( value )
		
		if expansionFn is None:
			# Standard view
			view = embeddedObject( valueView )
			return view.withContextMenuInteractor( _embeddedObjectExprContextMenuFactory ).withTreeEventListener( _EmbeddedObjectExprTreeEventListener.instance )
		else:
			# Macro view
			def createExpansionView():
				return Pres.coerce( expansionFn() )
			view = embeddedObjectMacro( valueView, LazyPres( createExpansionView ) )
			return view.withContextMenuInteractor( _embeddedObjectExprContextMenuFactory ).withTreeEventListener( _EmbeddedObjectExprTreeEventListener.instance )


	# Embedded object statement
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectStmt )
	@SpecialFormStatement
	def EmbeddedObjectStmt(self, fragment, inheritedState, model, embeddedValue):
		value = embeddedValue.getValue()
		valueView = ApplyPerspective( EditPerspective.instance, value )
		expansionFn = self._getExpansionFn( value )

		if expansionFn is None:
			# Standard view
			view = embeddedObject( valueView )
			return view.withContextMenuInteractor( _embeddedObjectStmtContextMenuFactory ).withTreeEventListener( _EmbeddedObjectStmtTreeEventListener.instance )
		else:
			# Macro view
			def createExpansionView():
				return Pres.coerce( expansionFn() )
			view = embeddedObjectMacro( valueView, LazyPres( createExpansionView ) )
			return view.withContextMenuInteractor( _embeddedObjectStmtContextMenuFactory ).withTreeEventListener( _EmbeddedObjectStmtTreeEventListener.instance )



	#
	#
	# SIMPLE STATEMENTS
	#
	#

	# Unparsed statement
	@DMObjectNodeDispatchMethod( Schema.UnparsedStmt )
	@UnparsedStatement
	def UnparsedStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return unparsedStmt( valueView )



	# Expression statement
	@DMObjectNodeDispatchMethod( Schema.ExprStmt )
	@Statement
	def ExprStmt(self, fragment, inheritedState, model, expr):
		exprView = SREInnerFragment( expr, PRECEDENCE_STMT )
		return exprStmt( exprView )



	# Assert statement
	@DMObjectNodeDispatchMethod( Schema.AssertStmt )
	@Statement
	def AssertStmt(self, fragment, inheritedState, model, condition, fail):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		failView = SREInnerFragment( fail, PRECEDENCE_STMT )   if fail is not None   else None
		return assertStmt( conditionView, failView )


	# Assignment statement
	@DMObjectNodeDispatchMethod( Schema.AssignStmt )
	@Statement
	def AssignStmt(self, fragment, inheritedState, model, targets, value):
		targetViews = SREInnerFragment.map( targets, PRECEDENCE_STMT )
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return assignStmt( targetViews, valueView )


	# Augmented assignment statement
	@DMObjectNodeDispatchMethod( Schema.AugAssignStmt )
	@Statement
	def AugAssignStmt(self, fragment, inheritedState, model, op, target, value):
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )
		return augAssignStmt( op, targetView, valueView )


	# Pass statement
	@DMObjectNodeDispatchMethod( Schema.PassStmt )
	@Statement
	def PassStmt(self, fragment, inheritedState, model):
		return passStmt()


	# Del statement
	@DMObjectNodeDispatchMethod( Schema.DelStmt )
	@Statement
	def DelStmt(self, fragment, inheritedState, model, target):
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )
		return delStmt( targetView )


	# Return statement
	@DMObjectNodeDispatchMethod( Schema.ReturnStmt )
	@Statement
	def ReturnStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )   if value is not None   else None
		return returnStmt( valueView )


	# Yield statement
	@DMObjectNodeDispatchMethod( Schema.YieldStmt )
	@Statement
	def YieldStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, PRECEDENCE_STMT )   if value is not None   else None
		return yieldStmt( valueView )


	# Raise statement
	@DMObjectNodeDispatchMethod( Schema.RaiseStmt )
	@Statement
	def RaiseStmt(self, fragment, inheritedState, model, excType, excValue, traceback):
		excTypeView = SREInnerFragment( excType, PRECEDENCE_STMT )   if excType is not None   else None
		excValueView = SREInnerFragment( excValue, PRECEDENCE_STMT )   if excValue is not None   else None
		tracebackView = SREInnerFragment( traceback, PRECEDENCE_STMT )   if traceback is not None   else None
		return raiseStmt( excTypeView, excValueView, tracebackView )


	# Break statement
	@DMObjectNodeDispatchMethod( Schema.BreakStmt )
	@Statement
	def BreakStmt(self, fragment, inheritedState, model):
		return breakStmt()


	# Continue statement
	@DMObjectNodeDispatchMethod( Schema.ContinueStmt )
	@Statement
	def ContinueStmt(self, fragment, inheritedState, model):
		return continueStmt()


	# Import statement
	@DMObjectNodeDispatchMethod( Schema.RelativeModule )
	def RelativeModule(self, fragment, inheritedState, model, name):
		return relativeModule( name )

	@DMObjectNodeDispatchMethod( Schema.ModuleImport )
	def ModuleImport(self, fragment, inheritedState, model, name):
		return moduleImport( name )

	@DMObjectNodeDispatchMethod( Schema.ModuleImportAs )
	def ModuleImportAs(self, fragment, inheritedState, model, name, asName):
		return moduleImportAs( name, asName )

	@DMObjectNodeDispatchMethod( Schema.ModuleContentImport )
	def ModuleContentImport(self, fragment, inheritedState, model, name):
		return moduleContentImport( name )

	@DMObjectNodeDispatchMethod( Schema.ModuleContentImportAs )
	def ModuleContentImportAs(self, fragment, inheritedState, model, name, asName):
		return moduleContentImportAs( name, asName )

	@DMObjectNodeDispatchMethod( Schema.ImportStmt )
	@Statement
	def ImportStmt(self, fragment, inheritedState, model, modules):
		moduleViews = SREInnerFragment.map( modules, PRECEDENCE_STMT )
		return importStmt( moduleViews )

	@DMObjectNodeDispatchMethod( Schema.FromImportStmt )
	@Statement
	def FromImportStmt(self, fragment, inheritedState, model, module, imports):
		moduleView = SREInnerFragment( module, PRECEDENCE_STMT )
		importViews = SREInnerFragment.map( imports, PRECEDENCE_STMT )
		return fromImportStmt( moduleView, importViews )

	@DMObjectNodeDispatchMethod( Schema.FromImportAllStmt )
	@Statement
	def FromImportAllStmt(self, fragment, inheritedState, model, module):
		moduleView = SREInnerFragment( module, PRECEDENCE_STMT )
		return fromImportAllStmt( moduleView )


	# Global statement
	@DMObjectNodeDispatchMethod( Schema.GlobalVar )
	def GlobalVar(self, fragment, inheritedState, model, name):
		return globalVar( name )

	@DMObjectNodeDispatchMethod( Schema.GlobalStmt )
	@Statement
	def GlobalStmt(self, fragment, inheritedState, model, vars):
		varViews = SREInnerFragment.map( vars, PRECEDENCE_STMT )
		return globalStmt( varViews )



	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.ExecStmt )
	@Statement
	def ExecStmt(self, fragment, inheritedState, model, source, globals, locals):
		sourceView = SREInnerFragment( source, PRECEDENCE_STMT )
		globalsView = SREInnerFragment( globals, PRECEDENCE_STMT )    if globals is not None   else None
		localsView = SREInnerFragment( locals, PRECEDENCE_STMT )   if locals is not None   else None
		return execStmt( sourceView, globalsView, localsView )






	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.PrintStmt )
	@Statement
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

	@DMObjectNodeDispatchMethod( Schema.IfStmtHeader )
	@CompoundStatementHeader
	def IfStmtHeader(self, fragment, inheritedState, model, condition):
		return self._ifStmtHeaderElement( inheritedState, condition )


	# Elif statement
	def _elifStmtHeaderElement(self, inheritedState, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		return elifStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.ElifStmtHeader )
	@CompoundStatementHeader
	def ElifStmtHeader(self, fragment, inheritedState, model, condition):
		return self._elifStmtHeaderElement( inheritedState, condition )



	# Else statement
	def _elseStmtHeaderElement(self, inheritedState):
		return elseStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.ElseStmtHeader )
	@CompoundStatementHeader
	def ElseStmtHeader(self, fragment, inheritedState, model):
		return self._elseStmtHeaderElement( inheritedState )


	# While statement
	def _whileStmtHeaderElement(self, inheritedState, condition):
		conditionView = SREInnerFragment( condition, PRECEDENCE_STMT )
		return whileStmtHeader( conditionView )

	@DMObjectNodeDispatchMethod( Schema.WhileStmtHeader )
	@CompoundStatementHeader
	def WhileStmtHeader(self, fragment, inheritedState, model, condition):
		return self._whileStmtHeaderElement( inheritedState, condition )


	# For statement
	def _forStmtHeaderElement(self, inheritedState, target, source):
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )
		sourceView = SREInnerFragment( source, PRECEDENCE_STMT )
		return forStmtHeader( targetView, sourceView )

	@DMObjectNodeDispatchMethod( Schema.ForStmtHeader )
	@CompoundStatementHeader
	def ForStmtHeader(self, fragment, inheritedState, model, target, source):
		return self._forStmtHeaderElement( inheritedState, target, source )



	# Try statement
	def _tryStmtHeaderElement(self, inheritedState):
		return tryStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.TryStmtHeader )
	@CompoundStatementHeader
	def TryStmtHeader(self, fragment, inheritedState, model):
		return self._tryStmtHeaderElement( inheritedState )



	# Except statement
	def _exceptStmtHeaderElement(self, inheritedState, exception, target):
		excView = SREInnerFragment( exception, PRECEDENCE_STMT )   if exception is not None   else None
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )   if target is not None   else None
		return exceptStmtHeader( excView, targetView )

	@DMObjectNodeDispatchMethod( Schema.ExceptStmtHeader )
	@CompoundStatementHeader
	def ExceptStmtHeader(self, fragment, inheritedState, model, exception, target):
		return self._exceptStmtHeaderElement( inheritedState, exception, target )



	# Finally statement
	def _finallyStmtHeaderElement(self, inheritedState):
		return finallyStmtHeader()

	@DMObjectNodeDispatchMethod( Schema.FinallyStmtHeader )
	@CompoundStatementHeader
	def FinallyStmtHeader(self, fragment, inheritedState, model):
		return self._finallyStmtHeaderElement( inheritedState )



	# With statement
	def _withStmtHeaderElement(self, inheritedState, expr, target):
		exprView = SREInnerFragment( expr, PRECEDENCE_STMT )
		targetView = SREInnerFragment( target, PRECEDENCE_STMT )   if target is not None   else None
		return withStmtHeader( exprView, targetView )

	@DMObjectNodeDispatchMethod( Schema.WithStmtHeader )
	@CompoundStatementHeader
	def WithStmtHeader(self, fragment, inheritedState, model, expr, target):
		return self._withStmtHeaderElement( inheritedState, expr, target )



	# Decorator statement
	def _decoStmtHeaderElement(self, inheritedState, name, args, argsTrailingSeparator):
		argViews = SREInnerFragment.map( args, PRECEDENCE_STMT )   if args is not None   else None
		return decoStmtHeader( name, argViews, argsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DecoStmtHeader )
	@CompoundStatementHeader
	def DecoStmtHeader(self, fragment, inheritedState, model, name, args, argsTrailingSeparator):
		return self._decoStmtHeaderElement( inheritedState, name, args, argsTrailingSeparator )



	# Def statement
	def _defStmtHeaderElement(self, inheritedState, name, params, paramsTrailingSeparator):
		paramViews = SREInnerFragment.map( params, PRECEDENCE_STMT )
		return defStmtHeader( name, paramViews, paramsTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.DefStmtHeader )
	@CompoundStatementHeader
	def DefStmtHeader(self, fragment, inheritedState, model, name, params, paramsTrailingSeparator):
		return self._defStmtHeaderElement( inheritedState, name, params, paramsTrailingSeparator ), defStmtHeaderHighlight, defStmtHighlight


	# Def statement
	def _classStmtHeaderElement(self, inheritedState, name, bases, basesTrailingSeparator):
		baseViews = SREInnerFragment.map( bases, PRECEDENCE_CONTAINER_ELEMENT )   if bases is not None   else None
		return classStmtHeader( name, baseViews, basesTrailingSeparator is not None )

	@DMObjectNodeDispatchMethod( Schema.ClassStmtHeader )
	@CompoundStatementHeader
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
	@DMObjectNodeDispatchMethod( Schema.IfStmt )
	@CompoundStatement
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
	@DMObjectNodeDispatchMethod( Schema.WhileStmt )
	@CompoundStatement
	def WhileStmt(self, fragment, inheritedState, model, condition, suite, elseSuite):
		compoundBlocks = [ ( Schema.WhileStmtHeader( condition=condition ), self._whileStmtHeaderElement( inheritedState, condition ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundBlocks



	# For statement
	@DMObjectNodeDispatchMethod( Schema.ForStmt )
	@CompoundStatement
	def ForStmt(self, fragment, inheritedState, model, target, source, suite, elseSuite):
		compoundBlocks = [ ( Schema.ForStmtHeader( target=target, source=source ), self._forStmtHeaderElement( inheritedState, target, source ), suite ) ]
		if elseSuite is not None:
			compoundBlocks.append( ( Schema.ElseStmtHeader(), self._elseStmtHeaderElement( inheritedState ),  elseSuite ) )
		return compoundBlocks



	# Try statement
	@DMObjectNodeDispatchMethod( Schema.TryStmt )
	@CompoundStatement
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
	@DMObjectNodeDispatchMethod( Schema.WithStmt )
	@CompoundStatement
	def WithStmt(self, fragment, inheritedState, model, expr, target, suite):
		return [ ( Schema.WithStmtHeader( expr=expr, target=target ), self._withStmtHeaderElement( inheritedState, expr, target ), suite ) ]



	# Def statement
	@DMObjectNodeDispatchMethod( Schema.DefStmt )
	@CompoundStatement
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
	@DMObjectNodeDispatchMethod( Schema.ClassStmt )
	@CompoundStatement
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
_view = Python25View( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, PythonSyntaxRecognizingEditor.instance )



