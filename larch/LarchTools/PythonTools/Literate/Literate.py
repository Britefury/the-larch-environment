##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from copy import deepcopy

from BritefuryJ.Command import Command, CommandSet

from BritefuryJ.LSpace.Input import ObjectDndHandler

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.IncrementalView import FragmentData
from BritefuryJ.Live import LiveValue

from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Row
from BritefuryJ.Pres.ObjectPres import ObjectBorder

from BritefuryJ.Controls import Controls, DropDownExpander, MenuItem, TextEntry, Button

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedExpressionAtCaretAction, WrapSelectionInEmbeddedExpressionAction,	\
	WrapSelectedStatementRangeInEmbeddedObjectAction, EmbeddedStatementAtCaretAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr, EmbeddedPython2Suite




def _dragSourceCreateSourceData(element, aspect):
	l = element.getFragmentContext().getModel()
	return FragmentData( l._new_reference(), element )


_dragSource = ObjectDndHandler.DragSource( FragmentData, _dragSourceCreateSourceData )




class LiterateExpression (object):
	def __init__(self, expr=None):
		if expr is None:
			expr = EmbeddedPython2Expr()
		self._expr = expr

		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __getstate__(self):
		return { 'expr' : self._expr }

	def __setstate__(self, state):
		self._expr = state['expr']
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._expr ]


	def __py_evalmodel__(self, codeGen):
		return self._expr.model

	def __py_replacement__(self):
		return deepcopy( self._expr.model['expr'] )


	def _new_reference(self):
		return LiterateExpression( self._expr )



	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()
		exprPres = self._expr

		header = Row( [ self._angleQuoteStyle( Label( u'\u00ab' ) ),
					self._angleQuoteStyle( Label( u'\u00bb' ) ),
					Spacer( 9.0, 0.0 ) ] ).withDragSource( _dragSource )

		return ObjectBorder( Row( [ header.alignVCentre(), exprPres ] ) )


	_angleQuoteStyle = StyleSheet.style( Primitive.foreground( Color( 0.15, 0.15, 0.45 ) ), Primitive.fontBold( True ), Primitive.fontSize( 12 ) )



	

class LiterateSuiteDefinition (object):
	def __init__(self, name='suite', suite=None):
		self._name = name

		if suite is None:
			suite = EmbeddedPython2Suite()
		self._suite = suite

		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __getstate__(self):
		return { 'name' : self._name, 'suite' : self._suite }

	def __setstate__(self, state):
		self._name = state['name']
		self._suite = state['suite']
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def getName(self):
		return self._name

	def setName(self, name):
		oldName = self._name
		self._name = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setName( name ), lambda: self.setName( oldName ), 'Literate suite definition set name' )



			
class LiterateSuite (object):
	def __init__(self, definition=None, expanded=True):
		if definition is None:
			definition = LiterateSuiteDefinition()
		self._definition = definition
		
		self._expanded = expanded

		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __getstate__(self):
		return { 'definition' : self._definition, 'expanded' : self._expanded }

	def __setstate__(self, state):
		self._definition = state.get( 'definition' )
		self._expanded = state.get( 'expanded', True )
		if self._definition is None:
			self._definition = LiterateSuiteDefinition()
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._definition ]


	def __py_execmodel__(self, codeGen):
		return self._definition._suite.model

	def __py_replacement__(self):
		return deepcopy( self._definition._suite.model['suite'] )


	def _new_reference(self):
		return LiterateSuite( self._definition )



	def getName(self):
		return self._definition.getName()

	def setName(self, name):
		self._definition.setName( name )


	def isExpanded(self):
		return self._expanded

	def setExpanded(self, expanded):
		oldExpanded = self._expanded
		self._expanded = expanded
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setExpanded( expanded ), lambda: self.setExpanded( oldExpanded ), 'Literate suite set expanded' )


	def __present__(self, fragment, inheritedState):
		def _literateExpressionMenu(element, menu):
			def _onToggleExpanded(item):
				self.setExpanded( not self._expanded )

			menuItemName = 'Start as contracted'   if self._expanded   else 'Start as expanded'
			menu.add( MenuItem.menuItemWithLabel( menuItemName, _onToggleExpanded ) )

			return False



		self._incr.onAccess()
		self._definition._incr.onAccess()
		suitePres = self._definition._suite

		nameLabel =  self._nameStyle( Label( self._definition._name ) )
		liveName = LiveValue( nameLabel )

		
		class _NameEntryListener (TextEntry.TextEntryListener):
			def onAccept(listener, textEntry, text):
				self.setName( text )

			def onCancel(listener, textEntry, orignalText):
				liveName.setLiteralValue( nameLabel )


		def _onNameButton(button, event):
			nameEntry = TextEntry( self._definition._name, _NameEntryListener() )
			nameEntry.grabCaretOnRealise()
			nameEntry = self._nameStyle( nameEntry )
			liveName.setLiteralValue( nameEntry )

		renameButton = self._nameButtonStyle( Button.buttonWithLabel( '...', _onNameButton ) )

		header = Row( [ self._angleQuoteStyle( Label( u'\u00ab' ) ),
		                        liveName,
					self._angleQuoteStyle( Label( u'\u00bb' ) ),
					Spacer( 10.0, 0.0 ),
					renameButton ] ).withDragSource( _dragSource )
	

		dropDown = self._dropDownStyle( DropDownExpander( header, suitePres, self._expanded, None ) )

		return ObjectBorder( dropDown ).withContextMenuInteractor( _literateExpressionMenu )

	_nameStyle = StyleSheet.style( Primitive.foreground( Color( 0.15, 0.15, 0.45 ) ), Primitive.fontSize( 12 ) )
	_nameButtonStyle = StyleSheet.style( Primitive.fontSize( 10 ) )
	_angleQuoteStyle = StyleSheet.style( Primitive.foreground( Color( 0.15, 0.15, 0.45 ) ), Primitive.fontBold( True ), Primitive.fontSize( 12 ) )
	_dropDownStyle = StyleSheet.style( Controls.dropDownExpanderHeaderArrowSize( 10.0 ), Controls.dropDownExpanderPadding( 12.0 ) )





@EmbeddedExpressionAtCaretAction
def _newLiterateExpressionAtCaret(caret):
	return LiterateExpression()

@WrapSelectionInEmbeddedExpressionAction
def _newLiterateExpressionAtSelection(expr, selection):
	d = LiterateExpression()
	d._expr.model['expr'] = deepcopy( expr )
	return d


@EmbeddedStatementAtCaretAction
def _newLiterateSuiteAtCaret(caret):
	return LiterateSuite()

@WrapSelectedStatementRangeInEmbeddedObjectAction
def _newLiterateSuiteAtStatementRange(statements, selection):
	d = LiterateSuite()
	d._definition._suite.model['suite'][:] = deepcopy( statements )
	return d



_lxCommand = Command( '&Literate E&xpression', chainActions( _newLiterateExpressionAtSelection, _newLiterateExpressionAtCaret ) )
_lsCommand = Command( '&Literate &Suite', chainActions( _newLiterateSuiteAtStatementRange, _newLiterateSuiteAtCaret ) )

pythonCommandSet( 'LarchTools.PythonTools.Literate', [ _lxCommand, _lsCommand ] )
