##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from java.awt.event import KeyEvent
from java.util.regex import Pattern

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectDispatch
from Britefury.gSym.gSymDocument import GSymDocument

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Input import ObjectDndHandler

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import GSymFragmentView, PyGSymViewFragmentFunction

from GSymCore.Languages.Python25 import Python25

from GSymCore.PythonConsole import ConsoleSchema as Schema
from GSymCore.PythonConsole.ConsoleViewer.ConsoleViewerStyleSheet import ConsoleViewerStyleSheet



class CurrentModuleInteractor (ElementInteractor):
	def __init__(self, console):
		self._console = console
		
		
	def onKeyTyped(self, element, event):
		return False
		
		
	def onKeyPress(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_ENTER:
			if event.getModifiers() & KeyEvent.CTRL_MASK  !=  0:
				bEvaluate = event.getModifiers() & KeyEvent.SHIFT_MASK  ==  0
				self._console.execute( bEvaluate )
				return True
		elif event.getKeyCode() == KeyEvent.VK_UP:
			if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
				self._console.backwards()
				return True
		elif event.getKeyCode() == KeyEvent.VK_DOWN:
			if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
				self._console.forwards()
				return True
		return False
	
	def onKeyRelease(self, element, event):
		return False




class ConsoleView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Schema.Console )
	def Console(self, ctx, styleSheet, state, node):
		blockViews = ctx.mapPresentFragment( node.getBlocks(), styleSheet )
		currentModuleView = ctx.presentFragmentWithPerspectiveAndStyleSheet( node.getCurrentPythonModule(), Python25.python25EditorPerspective, styleSheet['pythonStyle'] )
	
		def _onDrop(element, pos, data):
			def _onAccept(entry, text):
				node.setGlobalVar( text, data.getDocNode() )
				_finish( entry )
			
			def _onCancel(entry, text):
				_finish( entry )
				
			def _finish(entry):
				caret.moveTo( marker )
				dropPromptInsertionPoint.setChildren( [] )
			
			dropPrompt, textEntry = styleSheet.dropPrompt( _onAccept, _onCancel )
			rootElement = element.getRootElement()
			caret = rootElement.getCaret()
			marker = caret.getMarker().copy()
			dropPromptInsertionPoint.setChildren( [ dropPrompt ] )
			textEntry.grabCaret()
			textEntry.selectAll()
			rootElement.grabFocus()
			
			return True
			
		
		
		dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentDocNode, _onDrop )
		consoleView, dropPromptInsertionPoint = styleSheet.console( blockViews, currentModuleView, CurrentModuleInteractor( node ), dropDest )
		return consoleView



	@ObjectDispatchMethod( Schema.ConsoleBlock )
	def ConsoleBlock(self, ctx, styleSheet, state, node):
		pythonModule = node.getPythonModule()
		execResult = node.getExecResult()
		caughtException = execResult.getCaughtException()
		result = execResult.getResult()
		
		moduleView = ctx.presentFragmentWithPerspectiveAndStyleSheet( pythonModule, Python25.python25EditorPerspective, styleSheet.staticPythonStyle() )
		caughtExceptionView = ctx.presentFragmentWithGenericPerspective( caughtException )   if caughtException is not None   else None
		if result is not None:
			resultView = ctx.presentFragmentWithGenericPerspective( result[0]   if result[0] is not None   else   str( None ) )
		else:
			resultView = None
		
		return styleSheet.consoleBlock( moduleView, execResult.getStdOut(), execResult.getStdErr(), caughtExceptionView, resultView )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )

	

perspective = GSymPerspective( PyGSymViewFragmentFunction( ConsoleView() ), ConsoleViewerStyleSheet.instance, AttributeTable.instance, None, None )
