##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from java.awt import Color
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
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Controls import TextEntry
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.DocPresent.Combinators.Primitive import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import GSymFragmentView

from GSymCore.Languages.Python25 import Python25

from GSymCore.PythonConsole import ConsoleSchema as Schema
from GSymCore.PythonConsole.ConsoleViewer.ConsoleViewerStyleSheet import ConsoleViewerStyleSheet
from GSymCore.PythonConsole.ConsoleViewer.ConsoleViewerCombinators import ConsoleViewerStyle



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




_labelStyle = StyleSheet2.instance.withAttr( Primitive.fontSize, 10 )

_blockStyle = StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 2.0 ).withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) )

_pythonModuleBorderStyle = StyleSheet2.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) )
_dropPromptStyle = StyleSheet2.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), None ) )

_varAssignVarNameStyle = StyleSheet2.instance.withAttr( Primitive.fontItalic, True ).withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) )
_varAssignTypeNameStyle = StyleSheet2.instance.withAttr( Primitive.foreground, Color( 0.0, 0.125, 0.0 ) )
_varAssignMsgStyle = StyleSheet2.instance.withAttr( Primitive.foreground, Color( 0.0, 0.125, 0.0 ) )

_consoleBlockListStyle = StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 5.0 )
_consoleStyle = StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 8.0 )




class ConsoleView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Schema.Console )
	def Console(self, ctx, styleSheet, state, node):
		blockViews = ctx.mapPresentFragment( node.getBlocks(), styleSheet )
		currentModuleView = ctx.presentFragmentWithPerspectiveAndStyleSheet( node.getCurrentPythonModule(), Python25.python25EditorPerspective, styleSheet['pythonStyle'] )
	
		def _onDrop(element, pos, data, action):
			class _VarNameEntryListener (TextEntry.TextEntryListener):
				def onAccept(self, entry, text):
					node.assignVariable( text, data.getModel() )
					_finish( entry )
				
				def onCancel(self, entry, text):
					_finish( entry )
				
			def _finish(entry):
				caret.moveTo( marker )
				dropPromptInsertionPoint.setChildren( [] )
			
			dropPrompt, textEntry = styleSheet.dropPrompt( _VarNameEntryListener() )
			rootElement = element.getRootElement()
			caret = rootElement.getCaret()
			marker = caret.getMarker().copy()
			dropPromptInsertionPoint.setChildren( [ dropPrompt ] )
			textEntry.grabCaret()
			textEntry.selectAll()
			rootElement.grabFocus()
			
			return True
			
		
		
		dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop )
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
			resultView = ctx.presentFragmentWithGenericPerspective( result[0] )
		else:
			resultView = None
		
		return styleSheet.consoleBlock( moduleView, execResult.getStdOut(), execResult.getStdErr(), caughtExceptionView, resultView )

	def consoleBlock(self, pythonModule, stdout, stderr, caughtException, result):
		executionStyle = self['executionStyle']
		blockStyle = self.blockStyle()
		pythonModuleBorderStyle = self.pythonModuleBorderStyle()
		
		blockContents = []
		blockContents.append( pythonModuleBorderStyle.border( pythonModule.alignHExpand() ).alignHExpand() )
		if stderr is not None:
			blockContents.append( executionStyle.stderr( stderr ) )
		if caughtException is not None:
			blockContents.append( executionStyle.exception( caughtException ) )
		if stdout is not None:
			blockContents.append( executionStyle.stdout( stdout ) )
		if result is not None:
			blockContents.append( executionStyle.result( result ) )
		blockVBox = blockStyle.vbox( blockContents ).alignHExpand()
		return blockStyle.border( blockVBox ).alignHExpand()


	@ObjectDispatchMethod( Schema.ConsoleVarAssignment )
	def ConsoleVarAssignment(self, ctx, styleSheet, state, node):
		varName = node.getVarName()
		valueType = node.getValueType()
		valueTypeName = valueType.__name__
		
		varNameView = _varAssignVarNameStyle.applyTo( StaticText( varName ) )
		typeNameView = _varAssignTypeNameStyle.applyTo( StaticText( valueTypeName ) )
		
		return Paragraph( [ _varAssignMsgStyle.applyTo( StaticText( 'Variable ' ) ), LineBreak(),
		                    varNameView, LineBreak(),
		                    _varAssignMsgStyle.applyTo( StaticText( ' was assigned a ' ) ), LineBreak(),
		                    typeNameView ] )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )

	

perspective = GSymPerspective( ConsoleView(), StyleSheet2.instance, AttributeTable.instance, None, None )
