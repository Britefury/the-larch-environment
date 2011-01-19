##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
import sys

from java.awt import Color
from java.awt.event import KeyEvent
from java.util.regex import Pattern

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectDispatch
from Britefury.gSym.gSymDocument import GSymDocument


from Britefury.Util.NodeUtil import *


from BritefuryJ.Cell import LiteralCell

from BritefuryJ.AttributeTable import *

from BritefuryJ.Controls import TextEntry
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.IncrementalView import FragmentView

from BritefuryJ.GSym.PresCom import InnerFragment, ApplyPerspective

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution.ExecutionPresCombinators import execStdout, execStderr, execException, execResult

from GSymCore.PythonConsole import ConsoleSchema as Schema




class CurrentModuleInteractor (KeyElementInteractor):
	def __init__(self, console):
		self._console = console
		
		
	def keyTyped(self, element, event):
		return False
		
		
	def keyPressed(self, element, event):
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
	
	def keyReleasedw(self, element, event):
		return False




_bannerTextStyle = StyleSheet.instance.withAttr( Primitive.fontFace, 'Serif' ).withAttr( Primitive.fontSmallCaps, True ).withAttr( Primitive.editable, False )
_bannerBorder = SolidBorder( 2.0, 5.0, 8.0, 8.0, Color( 0.3, 0.5, 0.3 ), Color( 0.875, 0.9, 0.875 ) )


_labelStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 10 )

_blockStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 2.0 ).withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) )

_pythonModuleBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) )
_dropPromptStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), None ) )

_varAssignVarNameStyle = StyleSheet.instance.withAttr( Primitive.fontItalic, True ).withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) )
_varAssignTypeNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.125, 0.0 ) )
_varAssignMsgStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.125, 0.0 ) )

_consoleBlockListStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 )
_consoleStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 8.0 )



def _dropPrompt(varNameTextEntryListener):
	textEntry = TextEntry( 'var', varNameTextEntryListener )
	prompt = StaticText( 'Place node into a variable named: ' )
	textEntry.grabCaretOnRealise()
	textEntry.selectAllOnRealise()
	return _dropPromptStyle.applyTo( Border( Paragraph( [ prompt.alignVCentre(), textEntry.alignVCentre() ] ) ) )
	


class ConsoleView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Schema.Console )
	def Console(self, ctx, state, node):
		blocks = InnerFragment.map( node.getBlocks() )
		currentModule = Python25.python25EditorPerspective.applyTo( InnerFragment( node.getCurrentPythonModule() ) )
	
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
			
		
		
		# Header
		bannerText = _bannerTextStyle.applyTo( Column( [ NormalText( v )   for v in sys.version.split( '\n' ) ] ) )
		
		banner = _bannerBorder.surround( bannerText )
		
		
		dropDest = ObjectDndHandler.DropDest( FragmentView.FragmentModel, _onDrop )

		currentModule = Span( [ currentModule ] )
		currentModule = currentModule.withElementInteractor( CurrentModuleInteractor( node ) )
		
		m = _pythonModuleBorderStyle.applyTo( Border( currentModule.alignHExpand() ) ).alignHExpand()
		m = m.withDropDest( dropDest )
		def _ensureCurrentModuleVisible(element, ctx, style):
			element.ensureVisible()
		m = m.withCustomElementAction( _ensureCurrentModuleVisible )
		
		dropPromptCell = LiteralCell( HiddenContent( '' ) )
		dropPromptView = dropPromptCell.genericPerspectiveValuePresInFragment()
		
		if len( blocks ) > 0:
			blockList = _consoleBlockListStyle.applyTo( Column( blocks ) ).alignHExpand()
			return _consoleStyle.applyTo( Column( [ banner.alignHExpand(), blockList.alignHExpand(), dropPromptView, m.alignHExpand() ] ) ).alignHExpand()
		else:
			return _consoleStyle.applyTo( Column( [ banner.alignHExpand(), dropPromptView, m.alignVTop().alignHExpand() ] ) ).alignHExpand()
		

		
	@ObjectDispatchMethod( Schema.ConsoleBlock )
	def ConsoleBlock(self, ctx, state, node):
		pythonModule = node.getPythonModule()

		executionResult = node.getExecResult()
		
		caughtException = executionResult.getCaughtException()
		result = executionResult.getResult()
		stdout = executionResult.getStdOut()
		stderr = executionResult.getStdErr()
		
		moduleView = StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( Python25.python25EditorPerspective.applyTo( InnerFragment( pythonModule ) ) )
		caughtExceptionView = ApplyPerspective.generic( InnerFragment( caughtException ) )   if caughtException is not None   else None
		resultView = ApplyPerspective.generic( InnerFragment( result[0] ) )   if result is not None   else None
			
		blockContents = []
		blockContents.append( _pythonModuleBorderStyle.applyTo( Border( moduleView.alignHExpand() ).alignHExpand() ) )
		if stderr is not None:
			blockContents.append( execStderr( stderr, True ) )
		if caughtExceptionView is not None:
			blockContents.append( execException( caughtExceptionView ) )
		if stdout is not None:
			blockContents.append( execStdout( stdout, True ) )
		if resultView is not None:
			blockContents.append( execResult( resultView ) )
		blockColumn = Column( blockContents ).alignHExpand()
		return _blockStyle.applyTo( Border( blockColumn ).alignHExpand() )


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



	
	
perspective = GSymPerspective( ConsoleView(), None )


class ConsoleSubject (GSymSubject):
	def __init__(self, console, enclosingSubject):
		self._console = console
		self._enclosingSubject = enclosingSubject

		
	def getFocus(self):
		return self._console
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Python console'
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext()
	
	def getCommandHistory(self):
		return None
	
