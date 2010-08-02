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


from BritefuryJ.Cell import LiteralCell

from BritefuryJ.AttributeTable import *

from BritefuryJ.Controls import TextEntry
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.DocPresent.Combinators.Primitive import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import GSymFragmentView

from BritefuryJ.GSym.GenericPerspective.PresCom import GenericPerspectiveInnerFragment
from BritefuryJ.GSym.PresCom import InnerFragment, PerspectiveInnerFragment

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution.ExecutionPresCombinators import execStdout, execStdout, execException, execResult

from GSymCore.PythonConsole import ConsoleSchema as Schema




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



def _dropPrompt(varNameTextEntryListener):
	textEntry = TextEntry( 'var', varNameTextEntryListener )
	prompt = StaticText( 'Place node into a variable named: ' )
	def _grab(textEntryControl, ctx, style):
		textEntryControl.grabCaret()
		# TODO - control must be realised before this can be done
		# textEntryControl.selectAll()
	textEntry = textEntry.withCustomControlAction( _grab )
	return _dropPromptStyle.applyTo( Border( Paragraph( [ prompt.alignVCentre(), textEntry.alignVCentre() ] ) ) )
	


class ConsoleView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Schema.Console )
	def Console(self, ctx, state, node):
		blocks = InnerFragment.map( node.getBlocks() )
		currentModule = PerspectiveInnerFragment( Python25.python25EditorPerspective, node.getCurrentPythonModule() )
	
		def _onDrop(element, pos, data, action):
			class _VarNameEntryListener (TextEntry.TextEntryListener):
				def onAccept(self, entry, text):
					node.assignVariable( text, data.getModel() )
					_finish( entry )
				
				def onCancel(self, entry, text):
					_finish( entry )
				
			def _finish(entry):
				caret.moveTo( marker )
				dropPromptCell.setLiteralValue( HiddenContent( '' ) )
			
			dropPrompt = _dropPrompt( _VarNameEntryListener() )
			rootElement = element.getRootElement()
			caret = rootElement.getCaret()
			marker = caret.getMarker().copy()
			dropPromptCell.setLiteralValue( dropPrompt )
			rootElement.grabFocus()
			
			return True
			
		
		
		dropDest = ObjectDndHandler.DropDest( GSymFragmentView.FragmentModel, _onDrop )

		currentModule = Span( [ currentModule ] )
		currentModule = currentModule.withInteractor( CurrentModuleInteractor( node ) )
		
		m = _pythonModuleBorderStyle.applyTo( Border( currentModule.alignHExpand() ) ).alignHExpand()
		m = m.withDropDest( dropDest )
		def _ensureCurrentModuleVisible(element, ctx, style):
			element.ensureVisible()
		m = m.withCustomElementAction( _ensureCurrentModuleVisible )
		
		dropPromptCell = LiteralCell( HiddenContent( '' ) )
		dropPromptView = dropPromptCell.genericPerspectiveValuePresInFragment()
		
		if len( blocks ) > 0:
			blockList = _consoleBlockListStyle.applyTo( VBox( blocks ) ).alignHExpand()
			return _consoleStyle.applyTo( VBox( [ blockList.alignHExpand(), dropPromptView, m.alignHExpand() ] ) ).alignHExpand()
		else:
			return _consoleStyle.applyTo( VBox( [ dropPromptView, m.alignVTop().alignHExpand() ] ) ).alignHExpand()
		

		
	@ObjectDispatchMethod( Schema.ConsoleBlock )
	def ConsoleBlock(self, ctx, state, node):
		pythonModule = node.getPythonModule()

		executionResult = node.getExecResult()
		
		caughtException = executionResult.getCaughtException()
		result = executionResult.getResult()
		stdout = executionResult.getStdOut()
		stderr = executionResult.getStdErr()
		
		moduleView = StyleSheet2.instance.withAttr( Primitive.editable, False ).applyTo( PerspectiveInnerFragment( Python25.python25EditorPerspective, pythonModule ) )
		caughtExceptionView = GenericPerspectiveInnerFragment( caughtException )   if caughtException is not None   else None
		resultView = GenericPerspectiveInnerFragment( result[0] )   if result is not None   else None
			
		blockContents = []
		blockContents.append( _pythonModuleBorderStyle.applyTo( Border( moduleView.alignHExpand() ).alignHExpand() ) )
		if stderr is not None:
			blockContents.append( execStderr( stderr ) )
		if caughtExceptionView is not None:
			blockContents.append( execException( caughtExceptionView ) )
		if stdout is not None:
			blockContents.append( execStdout( stdout ) )
		if resultView is not None:
			blockContents.append( execResult( resultView ) )
		blockVBox = VBox( blockContents ).alignHExpand()
		return _blockStyle.applyTo( Border( blockVBox ).alignHExpand() )


	@ObjectDispatchMethod( Schema.ConsoleVarAssignment )
	def ConsoleVarAssignment(self, ctx, state, node):
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
