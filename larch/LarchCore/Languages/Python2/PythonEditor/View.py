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
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, ObjectDispatchMethod, redecorateDispatchMethod


from BritefuryJ.DocModel import DMObjectClass

from BritefuryJ.Command import Command
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.AttributeTable import *
from BritefuryJ.Controls import *
from BritefuryJ.LSpace.Interactor import KeyElementInteractor
from BritefuryJ.LSpace.Input import ObjectDndHandler, Modifier

from BritefuryJ.Pres import ApplyPerspective
from BritefuryJ.Pres.Primitive import Paragraph, Segment

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment
from BritefuryJ.Editor.SyntaxRecognizing.SyntaxRecognizingEditor import EditMode

from BritefuryJ.Live import LiveFunction

from BritefuryJ.ModelAccess.DocModel import *


from LarchCore.Languages.Python2 import Schema
from LarchCore.Languages.Python2 import PythonCommands


from LarchCore.Languages.Python2.PythonEditor.Parser import Python2Grammar
from LarchCore.Languages.Python2.PythonEditor.PythonEditOperations import *
from LarchCore.Languages.Python2.PythonEditor.SREditor import *
from LarchCore.Languages.Python2.PythonEditor.Keywords import *
from LarchCore.Languages.Python2.PythonEditor.Precedence import *
from LarchCore.Languages.Python2.PythonEditor.PythonEditorCombinators import *

from BritefuryJ.LSpace.Marker import Marker



_indentShortcut = Shortcut( KeyEvent.VK_TAB, 0 )
_dedentShortcut = Shortcut( KeyEvent.VK_TAB, Modifier.SHIFT )


def _onIndent(element):
	fragment = element.getFragmentContext()
	node = fragment.getModel()

	editor = PythonSyntaxRecognizingEditor.getEditorForElement( element )
	editor.indent( element, fragment, node )

def _onDedent(element):
	fragment = element.getFragmentContext()
	node = fragment.getModel()

	editor = PythonSyntaxRecognizingEditor.getEditorForElement( element )
	editor.dedent( element, fragment, node )


def _applyIndentationShortcuts(p):
	return p.withShortcut( _indentShortcut, _onIndent ).withShortcut( _dedentShortcut, _onDedent )



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



def _isValidExprOrTargetOuterUnparsed(value):
	return '\n' not in value


def _commitExprOuterValid(model, parsed):
	expr = model['expr']
	if parsed != expr:
		model['expr'] = parsed

def _commitExprOuterEmpty(model, parsed):
	model['expr'] = Schema.UNPARSED( value=[ '' ] )

def _commitExprOuterUnparsed(model, value):
	values = value.getItemValues()
	if values == []:
		values = [ '' ]
	model['expr'] = Schema.UNPARSED( value=values )


def _commitTargetOuterValid(model, parsed):
	expr = model['target']
	if parsed != expr:
		model['target'] = parsed

def _commitTargetOuterEmpty(model, parsed):
	model['target'] = Schema.UNPARSED( value=[ '' ] )

def _commitTargetOuterUnparsed(model, value):
	values = value.getItemValues()
	if values == []:
		values = [ '' ]
	model['target'] = Schema.UNPARSED( value=values )






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
		headerStatementLine = SoftStructuralItem( PythonSyntaxRecognizingEditor.instance, headerNode, headerStatementLine )
		headerStatementLine = _applyIndentationShortcuts( headerStatementLine )

		if headerContainerFn is not None:
			headerStatementLine = headerContainerFn( headerStatementLine )



		if suite is not None:
			indent = StructuralItem( PythonSyntaxRecognizingEditor.instance, Schema.Indent(), indentElement() )

			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )

			dedent = StructuralItem( PythonSyntaxRecognizingEditor.instance, Schema.Dedent(), dedentElement() )

			suiteElement = indentedBlock( indent, lineViews, dedent )
			suiteElement = SoftStructuralItem( PythonSyntaxRecognizingEditor.instance, pythonView._makeCompoundSuiteEditFilter( suite ), Schema.IndentedBlock( suite=suite ), suiteElement )

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



def _highlightDrop_embeddedObject(element, graphics, pos, action):
	marker = Marker.atPointIn( element, pos, True )
	if marker is not None  and  marker.isValid():
		ObjectDndHandler.drawCaretDndHighlight( graphics, element, marker )

def _onDrop_embeddedObject(element, pos, data, action):
	def _displayException(e):
		ApplyPerspective( None, Pres.coerce( e ) ).popupAtMousePosition( element, True, True )

	marker = Marker.atPointIn( element, pos, True )
	if marker is not None  and  marker.isValid():
		def _performLiteralInsertion(model):
			isolated = DMNode.embedIsolated( model, False )
			expr = Schema.EmbeddedObjectLiteral( embeddedValue=isolated )
			insertSpecialFormExpressionAtMarker( marker, expr )

		def _performInsertion(model):
			isolated = DMNode.embedIsolated( model, False )
			try:
				modelType = Schema.getEmbeddedObjectModelType( model )
			except Exception, e:
				_displayException( e )
			else:
				if modelType is Schema.Expr:
					expr = Schema.EmbeddedObjectExpr( embeddedValue=isolated )
					insertSpecialFormExpressionAtMarker( marker, expr )
				elif modelType is Schema.Stmt:
					stmt = Schema.EmbeddedObjectStmt( embeddedValue=isolated )
					insertSpecialFormStatementAtMarker( marker, stmt )


		def _makeOnDrop(performDropFn):
			def _onDrop(control):
				try:
					if marker.isValid():
						performDropFn()
				except Exception, e:
					_displayException( e )
			return _onDrop


		# Display a context menu
		def _performDropByCopy():
			model = data.getModel()
			_performInsertion( deepcopy( model ) )

		def _performDropByRef():
			model = data.getModel()
			_performInsertion( model )

		def _performDropAsLiteralByCopy():
			model = data.getModel()
			_performLiteralInsertion( deepcopy( model ) )

		def _performDropAsLiteralByRef():
			model = data.getModel()
			_performLiteralInsertion( model )

		menu = VPopupMenu( [ MenuItem.menuItemWithLabel( 'Copy', _makeOnDrop( _performDropByCopy ) ),
		                     MenuItem.menuItemWithLabel( 'Reference', _makeOnDrop( _performDropByRef ) ),
				     MenuItem.menuItemWithLabel( 'Copy (as literal)', _makeOnDrop( _performDropAsLiteralByCopy ) ),
				     MenuItem.menuItemWithLabel( 'Reference (as literal)', _makeOnDrop( _performDropAsLiteralByRef ) ) ] )
		menu.popupAtMousePosition( marker.getElement() )
	return True


_embeddedObject_dropDest = ObjectDndHandler.DropDest( FragmentData, None, _highlightDrop_embeddedObject, _onDrop_embeddedObject )



def _displayExceptionAtPointer(e):
	ApplyPerspective( None, Pres.coerce( e ) ).popupAtMousePosition( element, True, True )


def _convertEmbeddedObjectExprToLiteral(model):
	isolated = model['embeddedValue']
	replacement = Schema.EmbeddedObjectLiteral( embeddedValue=isolated )
	pyReplaceNode( model, replacement )


def _convertEmbeddedObjectLiteralToExpression(model):
	isolated = model['embeddedValue']
	value = isolated.getValue()

	try:
		modelType = Schema.getEmbeddedObjectModelType( value )
		if modelType is Schema.Expr:
			replacement = Schema.EmbeddedObjectExpr( embeddedValue=isolated )
		elif modelType is Schema.Stmt:
			raise ValueError, 'Cannot convert embedded object literal expression to statement'
		pyReplaceNode( model, replacement )
	except Exception, e:
		_displayExceptionAtPointer( e )


def _convertEmbeddedObjectStmtToLiteral(model):
	isolated = model['embeddedValue']
	replacement = Schema.ExprStmt( expr=Schema.EmbeddedObjectExpr( embeddedValue=isolated, asLiteral='1' ) )
	pyReplaceNode( model, replacement )


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



def _embeddedObjectLiteralContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _asExpression(item):
		_convertEmbeddedObjectLiteralToExpression( model )

	def _onDelete(item):
		_removeEmbeddedObjectExpr( model )

	# Determine if the literal embedded object can be converted to a non-literal - it can't if the result will convert an expression to a statement
	# If it can, add the menu entry
	isolated = model['embeddedValue']
	value = isolated.getValue()

	try:
		modelType = Schema.getEmbeddedObjectModelType( value )
		if modelType is Schema.Expr:
			menu.add( MenuItem.menuItemWithLabel( 'Convert to expression', _asExpression ) )
		else:
			pass
	except:
		pass

	# Add the delete menu entry
	menu.add( MenuItem.menuItemWithLabel( 'Delete embedded object', _onDelete ) )

	return False


def _embeddedObjectExprContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _asLiteral(item):
		_convertEmbeddedObjectExprToLiteral( model )

	def _onDelete(item):
		_removeEmbeddedObjectExpr( model )

	menu.add( MenuItem.menuItemWithLabel( 'Convert to literal', _asLiteral ) )
	menu.add( MenuItem.menuItemWithLabel( 'Delete embedded object', _onDelete ) )

	return False


def _embeddedObjectStmtContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _asLiteral(item):
		_convertEmbeddedObjectStmtToLiteral( model )

	def _onDelete(item):
		_removeEmbeddedObjectStmt( model )

	menu.add( MenuItem.menuItemWithLabel( 'Convert to literal', _asLiteral ) )
	menu.add( MenuItem.menuItemWithLabel( 'Delete embedded object', _onDelete ) )

	return False



def _removeSpecialFormExpr(model):
	pyReplaceNode( model, Schema.Load( name='None' ) )


def _specialFormExprContextMenuFactory(element, menu):
	fragment = element.getFragmentContext()
	model = fragment.getModel()

	def _onDelete(item):
		_removeSpecialFormExpr( model )

	menu.add( MenuItem.menuItemWithLabel( 'Delete', _onDelete ) )

	return False




def _pythonModuleContextMenuFactory(element, menu):
	return True



def _pythonTargetContextMenuFactory(element, menu):
	return True



def Unparsed(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._unparsedEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )
		


def UnparsedStatement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		v = self._unparsedStatementEditRule.applyToFragment( statementLine( v ), model, inheritedState )
		v = _applyIndentationShortcuts( v )
		return v
	return redecorateDispatchMethod( method, _m )



def TargetTopLevel(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._targetTopLevelEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )



def Expression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._expressionEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )



def StructuralExpression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._structuralExpressionEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )



def ExpressionTopLevel(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return self._expressionTopLevelEditRule.applyToFragment( v, model, inheritedState )
	return redecorateDispatchMethod( method, _m )



def Statement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		v = self._statementEditRule.applyToFragment( statementLine( v ), model, inheritedState )
		v = _applyIndentationShortcuts( v )
		return v
	return redecorateDispatchMethod( method, _m )


	
def CompoundStatementHeader(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		if isinstance( v, tuple ):
			e = self._compoundStatementHeaderEditRule.applyToFragment( statementLine( v[0] ), model, inheritedState )
			e = _applyIndentationShortcuts( e )
			for f in v[1:]:
				e = f( e )
			return e
		else:
			v = self._compoundStatementHeaderEditRule.applyToFragment( statementLine( v ), model, inheritedState )
			v = _applyIndentationShortcuts( v )
			return v
	return redecorateDispatchMethod( method, _m )



def CompoundStatement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		if isinstance( v, tuple ):
			f = v[1]
			return f( compoundStatementEditor( self, inheritedState, model, v[0] ) )
		else:
			return compoundStatementEditor( self, inheritedState, model, v )
	return redecorateDispatchMethod( method, _m )



def SpecialFormExpression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		v = v.withContextMenuInteractor( _specialFormExprContextMenuFactory )
		return StructuralItem( PythonSyntaxRecognizingEditor.instance, model, v )
	return redecorateDispatchMethod( method, _m )



def SpecialFormStatement(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		v = StructuralItem( PythonSyntaxRecognizingEditor.instance, model, v )
		v = specialFormStatementLine( v )
		v = self._specialFormStatementEditRule.applyToFragment( v, model, inheritedState )
		v = _applyIndentationShortcuts( v )
		return v
	return redecorateDispatchMethod( method, _m )



def EmbeddedObjectExpression(method):
	def _m(self, fragment, inheritedState, model, *args):
		v = method(self, fragment, inheritedState, model, *args )
		return StructuralItem( PythonSyntaxRecognizingEditor.instance, model, v )
	return redecorateDispatchMethod( method, _m )



class Python2View (MethodDispatchView):
	def __init__(self, grammar):
		super( Python2View, self ).__init__()
		self._parser = grammar
		
		editor = PythonSyntaxRecognizingEditor.instance
		
		self._expr = editor.parsingEditFilter( 'Expression', grammar.expression(), pyReplaceNode )
		self._stmt = editor.parsingEditFilter( 'Statement', grammar.simpleSingleLineStatementValid(), pyReplaceNode )
		self._compHdr = editor.partialParsingEditFilter( 'Compound header', grammar.compoundStmtHeader() )
		self._stmtUnparsed = editor.unparsedEditFilter( 'Unparsed statement', _isValidUnparsedStatementValue, _commitUnparsedStatment, _commitInnerUnparsed )
		self._topLevel = editor.topLevelEditFilter()
		self._exprOuterValid = editor.parsingEditFilter( 'Expression-outer-valid', grammar.tupleOrExpression(), _commitExprOuterValid, _commitExprOuterEmpty )
		self._exprOuterInvalid = editor.unparsedEditFilter( 'Expression-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitExprOuterUnparsed )
		self._targetOuterValid = editor.parsingEditFilter( 'Target-outer-valid', grammar.targetListOrTargetItem(), _commitTargetOuterValid, _commitTargetOuterEmpty )
		self._targetOuterInvalid = editor.unparsedEditFilter( 'Target-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitTargetOuterUnparsed )

		self._expressionEditRule = editor.editRule( _pythonPrecedenceHandler, [ self._expr ] )
		self._structuralExpressionEditRule = editor.softStructuralEditRule( _pythonPrecedenceHandler, [ self._expr ] )
		self._unparsedEditRule = editor.editRule( [ self._expr ] )
		self._statementEditRule = editor.softStructuralEditRule( [ self._stmt, self._compHdr, self._stmtUnparsed ] )
		self._unparsedStatementEditRule = editor.editRule( [ self._stmt, self._compHdr, self._stmtUnparsed ] )
		self._compoundStatementHeaderEditRule = editor.softStructuralEditRule( [ self._compHdr, self._stmtUnparsed ] )
		self._specialFormStatementEditRule = editor.softStructuralEditRule( [ self._stmt, self._compHdr, self._stmtUnparsed ] )
		self._targetTopLevelEditRule = editor.softStructuralEditRule( _pythonPrecedenceHandler, [ self._targetOuterValid, self._targetOuterInvalid, self._topLevel ] )
		self._expressionTopLevelEditRule = editor.softStructuralEditRule( _pythonPrecedenceHandler, [ self._exprOuterValid, self._exprOuterInvalid, self._topLevel ] )

		
	def _makeSuiteEditFilter(self, suite):
		return PythonSyntaxRecognizingEditor.instance.parsingEditFilter( 'Suite', self._parser.suite(), _makeSuiteCommitFn( suite ) )

	def _makeCompoundSuiteEditFilter(self, suite):
		return PythonSyntaxRecognizingEditor.instance.parsingEditFilter( 'Suite', self._parser.compoundSuite(), _makeSuiteCommitFn( suite ) )

		
		
		
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
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		s = s.withCommands( PythonCommands.pythonTargetCommands )
		s = s.withCommands( PythonCommands.pythonCommands )
		s = ApplyStyleSheetFromAttribute( PythonEditorStyle.paragraphIndentationStyle, s )
		s = SoftStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._makeSuiteEditFilter( suite ), self._topLevel ], suite, s )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonSuite )
	def PythonSuite(self, fragment, inheritedState, model, suite):
		if len( suite ) == 0:
			# Empty suite - create a single blank line so that there is something to edit
			lineViews = [ statementLine( blankLine() ) ]
		else:
			lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )
		s = suiteView( lineViews ).alignHPack().alignVRefY()
		s = s.withDropDest( _embeddedObject_dropDest )
		s = s.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		s = s.withCommands( PythonCommands.pythonTargetCommands )
		s = s.withCommands( PythonCommands.pythonCommands )
		s = ApplyStyleSheetFromAttribute( PythonEditorStyle.paragraphIndentationStyle, s )
		s = SoftStructuralItem( PythonSyntaxRecognizingEditor.instance, [ self._makeSuiteEditFilter( suite ), self._topLevel ], suite, s )
		return s



	@DMObjectNodeDispatchMethod( Schema.PythonExpression )
	@ExpressionTopLevel
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
		e = e.withContextMenuInteractor( _pythonModuleContextMenuFactory )
		e = e.withCommands( PythonCommands.pythonTargetCommands )
		e = e.withCommands( PythonCommands.pythonCommands )
		return e



	@DMObjectNodeDispatchMethod( Schema.PythonTarget )
	@TargetTopLevel
	def PythonTarget(self, fragment, inheritedState, model, target):
		if target is None:
			# Empty document - create a single blank line so that there is something to edit
			targetView = blankLine()
			seg = targetView
		else:
			targetView = SREInnerFragment( target, PRECEDENCE_NONE, EditMode.DISPLAY )
			seg = Segment( targetView )
		t = Paragraph( [ seg ] ).alignHPack().alignVRefY()
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
				raise ValueError, 'Python2View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseableText( x )
				return view
			elif isinstance( x, DMObjectInterface ):
				view = SREInnerFragment( x, PRECEDENCE_CONTAINER_UNPARSED, EditMode.DISPLAY )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( PythonSyntaxRecognizingEditor.instance, x, view )
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
	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	@Expression
	def StringLiteral(self, fragment, inheritedState, model, format, quotation, value):
		fmt = Schema.stringFormatToPrefix( format )

		quote = "'"   if quotation == 'single'   else   '"'

		return stringLiteral( fmt, quote, value, format.endswith( 'regex' ) )



	@DMObjectNodeDispatchMethod( Schema.MultilineStringLiteral )
	@SpecialFormExpression
	def MultilineStringLiteral(self, fragment, inheritedState, model, format):
		fragment.disableAutoRefresh()

		@LiveFunction
		def _val():
			# Check if @model is indeed a MultilineStringLiteral;
			# the pyReplaceNode function uses the doc model node 'become' method, which changes the node class
			# and contents in-place, which can result the multi-line string becoming something else
			if model.isInstanceOf( Schema.MultilineStringLiteral ):
				return model['value']
			else:
				# A become operation has changed the type of @model - this fragment needs to be refreshed
				fragment.queueRefresh()
				return ''

		@LiveFunction
		def _format():
			f = model['format']
			if f.startswith( 'unicode' ):
				return 'unicode'
			elif f.startswith( 'bytes' ):
				return 'bytes'
			else:
				return 'ascii'

		@LiveFunction
		def _isRaw():
			return model['format'].endswith( 'regex' )

		def _onEdit(text):
			model['value'] = text
		return multilineStringLiteral( _val, _format, _isRaw, _onEdit )


	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	@Expression
	def IntLiteral(self, fragment, inheritedState, model, format, numType, value):
		boxContents = []

		if numType == 'long':
			if format == 'hex':
				valueString = '0x{0:x}L'.format( long( value, 16 ) )
			elif format == 'bin':
				valueString = '0b{0:b}L'.format( long( value, 2 ) )
			elif format == 'oct':
				valueString = '0o{0:o}L'.format( long( value, 8 ) )
			else:
				valueString = '{0}L'.format( long( value ) )

			fmt = 'L'
		else:
			if format == 'hex':
				valueString = '0x{0:x}'.format( int( value, 16 ) )
			elif format == 'bin':
				valueString = '0b{0:b}'.format( int( value, 2 ) )
			elif format == 'oct':
				valueString = '0o{0:o}'.format( int( value, 8 ) )
			else:
				valueString = '{0}'.format( value )

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
	@StructuralExpression
	def Div(self, fragment, inheritedState, model, x, y):
		xPrec, yPrec = computeBinOpViewPrecedenceValues( PRECEDENCE_MULDIVMOD, False )
		xView = SREInnerFragment( x, xPrec, EditMode.EDIT )
		yView = SREInnerFragment( y, yPrec, EditMode.EDIT )
		return div( xView, yView, '/' )

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


	# Embedded object literal
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectLiteral )
	@EmbeddedObjectExpression
	def EmbeddedObjectLiteral(self, fragment, inheritedState, model, embeddedValue):
		value = embeddedValue.getValue()
		valueView = ApplyPerspective( EditPerspective.instance, value )

		view = embeddedObjectLiteral( valueView )
		return view.withContextMenuInteractor( _embeddedObjectLiteralContextMenuFactory ).withTreeEventListener( _EmbeddedObjectExprTreeEventListener.instance )



	# Embedded object expression
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectExpr )
	@EmbeddedObjectExpression
	def EmbeddedObjectExpr(self, fragment, inheritedState, model, embeddedValue):
		value = embeddedValue.getValue()
		valueView = ApplyPerspective( EditPerspective.instance, value )

		expansionFn = self._getExpansionFn( value )

		hideFrame = getattr( value, '__embed_hide_frame__', False )

		if expansionFn is None:
			# Standard view
			view = embeddedObjectExpr( valueView, hideFrame )
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

		hideFrame = getattr( value, '__embed_hide_frame__', False )

		if expansionFn is None:
			# Standard view
			view = embeddedObjectStmt( valueView, hideFrame )
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
	@DMObjectNodeDispatchMethod( Schema.WithContext )
	def WithContext(self, fragment, inheritedState, model, expr, target):
		exprView = SREInnerFragment( expr, PRECEDENCE_CONTAINER_ELEMENT )
		targetView = SREInnerFragment( target, PRECEDENCE_CONTAINER_ELEMENT )   if target is not None   else None
		return withContext( exprView, targetView )

	def _withStmtHeaderElement(self, inheritedState, contexts):
		contextViews = SREInnerFragment.map( contexts, PRECEDENCE_STMT )
		return withStmtHeader( contextViews )

	@DMObjectNodeDispatchMethod( Schema.WithStmtHeader )
	@CompoundStatementHeader
	def WithStmtHeader(self, fragment, inheritedState, model, contexts):
		return self._withStmtHeaderElement( inheritedState, contexts )



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
		indent = StructuralItem( PythonSyntaxRecognizingEditor.instance, Schema.Indent(), indentElement() )

		lineViews = SREInnerFragment.map( suite, PRECEDENCE_NONE, EditMode.EDIT )

		dedent = StructuralItem( PythonSyntaxRecognizingEditor.instance, Schema.Dedent(), dedentElement() )

		suiteElement = badIndentedBlock( indent, lineViews, dedent )
		suiteElement = SoftStructuralItem( PythonSyntaxRecognizingEditor.instance,
		                                       PythonSyntaxRecognizingEditor.instance.parsingEditFilter( 'Suite', self._parser.compoundSuite(), _makeSuiteCommitFn( suite ) ),
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
	def WithStmt(self, fragment, inheritedState, model, contexts, suite):
		return [ ( Schema.WithStmtHeader( contexts=contexts ), self._withStmtHeaderElement( inheritedState, contexts ), suite ) ]



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



_parser = Python2Grammar()
_view = Python2View( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, PythonSyntaxRecognizingEditor.instance )





@PythonCommands.SpecialFormExprAtCaretAction
def _newMultilineString(caret):
	return Schema.MultilineStringLiteral( value='', format='ascii' )

_multilineStringCommand = Command( '&Mult-line &string', _newMultilineString )


@PythonCommands.SpecialFormExprAtCaretAction
def _newQuoteExpr(caret):
	return Schema.Quote( value=Schema.PythonExpression( expr=Schema.Load( name='None' ) ) )

_quoteExprCommand = Command( '&Quote e&xpression', _newQuoteExpr )


@PythonCommands.SpecialFormExprAtCaretAction
def _newQuoteSuite(caret):
	return Schema.Quote( value=Schema.PythonSuite( suite=[] ) )

_quoteStmtCommand = Command( '&Quote &suite', _newQuoteSuite )


@PythonCommands.SpecialFormExprAtCaretAction
def _newUnquote(caret):
	return Schema.Unquote( value=Schema.PythonExpression( expr=Schema.Load( name='None' ) ) )

_unquoteCommand = Command( '&Un&quote', _newUnquote )


PythonCommands.pythonCommandSet( 'LarchCore.Languages.Python2', [ _multilineStringCommand, _quoteExprCommand, _quoteStmtCommand, _unquoteCommand ] )



