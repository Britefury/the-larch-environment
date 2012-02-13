##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from copy import deepcopy, copy
import imp

from java.awt import Color
from java.awt.event import KeyEvent

from BritefuryJ.Command import Command, CommandSet

from BritefuryJ.Controls import *

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Pres.Primitive import *
from BritefuryJ.StyleSheet import *

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeWrapSelectedStatementRangeInEmbeddedObjectAction, makeInsertEmbeddedStatementAtCaretAction, chainActions
from LarchCore.Languages.Python25.Embedded import EmbeddedPython25Suite, removeEmbeddedObjectContainingElement
from LarchCore.Languages.Python25.Execution import Execution



_titleStyle = StyleSheet.style( Primitive.fontSize( 9 ) )
_infoStyle = StyleSheet.style( Primitive.fontSize( 9 ), Primitive.foreground( Color( 0.0, 0.2, 0.4 ) ) )
_blockStyle = StyleSheet.style( Primitive.columnSpacing( 2.0 ) )
_inlineConsoleBorder = SolidBorder( 1.0, 3.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.85, 0.85, 0.85 ) )
_pythonModuleBorder = SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE )




class _InlineConsoleInteractor (KeyElementInteractor):
	def __init__(self, console):
		self._console = console


	def keyTyped(self, element, event):
		return False


	def keyPressed(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_ENTER:
			if event.getModifiers() & KeyEvent.CTRL_MASK  !=  0:
				self._console._refresh()
				return True
			elif event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
				self._console._commit( element )
				return True
		return False

	def keyReleased(self, element, event):
		return False






class InlineConsole (object):
	def __init__(self, suite=None):
		self._suite = EmbeddedPython25Suite( suite )
		self._scopeGlobals = {}
		self._scopeLocals = {}
		self._module = None
		self._execResult = None
		self._incr = IncrementalValueMonitor( self )
		self.__change_history__ = None


	def __getstate__(self):
		return { 'suite' : self._suite }

	def __setstate__(self, state):
		self._suite = state['suite']
		self._scopeGlobals = {}
		self._scopeLocals = {}
		self._module = None
		self._execResult = None
		self._incr = IncrementalValueMonitor( self )
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._suite ]


	def __py_replacement__(self):
		return deepcopy( self._suite.model['suite'] )


	def __py_exec__(self, globalVars, localVars, codeGen):
		self._module = codeGen.module
		self._scopeGlobals = {}
		self._scopeGlobals.update( globalVars )
		self._scopeLocals = {}
		self._scopeLocals.update( localVars )
		self._refresh()
		self._incr.onChanged()



	def _refresh(self):
		if self._module is not None:
			self._execResult = Execution.getResultOfExecutionInScopeWithinModule( self._suite.model, copy( self._scopeGlobals ), copy( self._scopeLocals ), self._module, True )
			self._incr.onChanged()


	def _commit(self, element):
		removeEmbeddedObjectContainingElement( element )



	def __present__(self, fragment, inheritedState):
		self._incr.onAccess()

		title = _titleStyle( Label( 'INLINE CONSOLE - Alt+Enter to commit' ) )
		code = _pythonModuleBorder.surround( self._suite ).alignHExpand()
		code = code.withElementInteractor( _InlineConsoleInteractor( self ) )

		contents = [ title, code ]
		if self._execResult is not None:
			view = self._execResult.view()
			if view is not None:
				contents.append( view )
		if self._module is None:
			contents.append( _infoStyle( Label( 'Please execute the surrounding code' ) ) )

		main = _blockStyle( Column( contents ) )

		return _inlineConsoleBorder.surround( main ).alignHExpand()





def _newInlineConsoleAtCaret(caret):
	return InlineConsole()

def _newInlineConsoleAtStatementRange(statements, selection):
	return InlineConsole( deepcopy( statements ) )


_inlineConsoleAtCaret = makeInsertEmbeddedStatementAtCaretAction( _newInlineConsoleAtCaret )
_inlineConsoleAtSelection = makeWrapSelectedStatementRangeInEmbeddedObjectAction( _newInlineConsoleAtStatementRange )


_icCommand = Command( '&Inline &Console', chainActions( _inlineConsoleAtSelection, _inlineConsoleAtCaret ) )

_icCommands = CommandSet( 'LarchTools.PythonTools.InlineConsole', [ _icCommand ] )

pythonCommands.registerCommandSet( _icCommands )
