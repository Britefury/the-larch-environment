##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os
import sys

from java.awt import Color
from java.awt.event import KeyEvent
from java.util.regex import Pattern

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import ObjectDispatchView
from Britefury.Kernel.Document import Document


from BritefuryJ.Live import LiveValue

from BritefuryJ.AttributeTable import *

from BritefuryJ.Controls import TextEntry
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor
from BritefuryJ.StyleSheet import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Input import ObjectDndHandler

from BritefuryJ.Pres import InnerFragment, ApplyPerspective
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView, FragmentData


from LarchCore.Languages.Python25 import Python25
from LarchCore.Languages.Python25.Execution.ExecutionPresCombinators import execStdout, execStderr, execException, execResult

from LarchCore.PythonConsole import ConsoleSchema as Schema




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




_bannerTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontSmallCaps( True ), Primitive.editable( False ) )
_bannerHelpKeyTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontSmallCaps( True ), Primitive.fontItalic( True ), Primitive.foreground( Color( 0.25, 0.25, 0.25 ) ) )
_bannerHelpTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontItalic( True ), Primitive.foreground( Color( 0.25, 0.25, 0.25 ) ) )
_bannerBorder = SolidBorder( 2.0, 5.0, 8.0, 8.0, Color( 0.3, 0.5, 0.3 ), Color( 0.875, 0.9, 0.875 ) )


_labelStyle = StyleSheet.style( Primitive.fontSize( 10 ) )

_blockStyle = StyleSheet.style( Primitive.columnSpacing( 2.0 ), Primitive.border( SolidBorder( 1.0, 5.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) ) )

_pythonModuleBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) ) )
_dropPromptStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), None ) ) )

_varAssignVarNameStyle = StyleSheet.style( Primitive.fontItalic( True ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_varAssignTypeNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.125, 0.0 ) ) )
_varAssignMsgStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.125, 0.0 ) ) )

_consoleBlockListStyle = StyleSheet.style( Primitive.columnSpacing( 5.0 ) )
_consoleStyle = StyleSheet.style( Primitive.columnSpacing( 8.0 ) )


_varNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' )


def _dropPrompt(varNameTextEntryListener):
	textEntry = TextEntry.regexValidated( 'var', varNameTextEntryListener, _varNameRegex, 'Please enter a valid identifier' )
	prompt = Label( 'Place node into a variable named: ' )
	textEntry.grabCaretOnRealise()
	textEntry.selectAllOnRealise()
	return _dropPromptStyle.applyTo( Border( Paragraph( [ prompt.alignVCentre(), textEntry.alignVCentre() ] ).alignHPack() ) )
	


class ConsoleView (ObjectDispatchView):
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
				dropPromptLive.setLiteralValue( Blank() )
			
			dropPrompt = _dropPrompt( _VarNameEntryListener() )
			rootElement = element.getRootElement()
			caret = rootElement.getCaret()
			marker = caret.getMarker().copy()
			dropPromptLive.setLiteralValue( dropPrompt )
			rootElement.grabFocus()
			
			return True
			
		
		
		# Header
		bannerVersionText = [ _bannerTextStyle.applyTo( NormalText( v ) )   for v in sys.version.split( '\n' ) ]
		helpText = Row( [ _bannerHelpKeyTextStyle.applyTo( Label( 'Ctrl+Enter' ) ),
		                  _bannerHelpTextStyle.applyTo( Label( ' - execute and evaluate, ' ) ),
		                  _bannerHelpKeyTextStyle.applyTo( Label( 'Ctrl+Shift+Enter' ) ),
		                  _bannerHelpTextStyle.applyTo( Label( ' - execute only' ) ) ] )
		bannerText = Column( bannerVersionText + [ helpText ] ).alignHPack()
		
		banner = _bannerBorder.surround( bannerText )
		
		
		dropDest = ObjectDndHandler.DropDest( FragmentData, _onDrop )

		currentModule = Span( [ currentModule ] )
		currentModule = currentModule.withElementInteractor( CurrentModuleInteractor( node ) )
		
		m = _pythonModuleBorderStyle.applyTo( Border( currentModule.alignHExpand() ) ).alignHExpand()
		m = m.withDropDest( dropDest )
		def _ensureCurrentModuleVisible(element, ctx, style):
			element.ensureVisible()
		m = m.withCustomElementAction( _ensureCurrentModuleVisible )
		
		dropPromptLive = LiveValue( Blank() )
		dropPromptView = dropPromptLive.defaultPerspectiveValuePresInFragment()
		
		if len( blocks ) > 0:
			blockList = _consoleBlockListStyle.applyTo( Column( blocks ) ).alignHExpand()
			return _consoleStyle.applyTo( Column( [ banner, blockList, dropPromptView, m ] ) ).alignHExpand().alignVRefY()
		else:
			return _consoleStyle.applyTo( Column( [ banner, dropPromptView, m.alignVTop() ] ) ).alignHExpand().alignVRefY()
		

		
	@ObjectDispatchMethod( Schema.ConsoleBlock )
	def ConsoleBlock(self, ctx, state, node):
		pythonModule = node.getPythonModule()

		executionResult = node.getExecResult()
		
		caughtException = executionResult.getCaughtException()
		result = executionResult.getResult()
		stdout = executionResult.getStdOut()
		stderr = executionResult.getStdErr()
		
		moduleView = StyleSheet.style( Primitive.editable( False ) ).applyTo( Python25.python25EditorPerspective.applyTo( InnerFragment( pythonModule ) ) )
		caughtExceptionView = ApplyPerspective.defaultPerspective( InnerFragment( caughtException ) )   if caughtException is not None   else None
		resultView = ApplyPerspective.defaultPerspective( InnerFragment( result[0] ) )   if result is not None   else None
			
		blockContents = [ _pythonModuleBorderStyle.applyTo( Border( moduleView.alignHExpand() ).alignHExpand() ) ]
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
		                    typeNameView ] ).alignHPack()



	
_view = ConsoleView()
perspective = Perspective( _view.fragmentViewFunction, None )


class ConsoleSubject (Subject):
	def __init__(self, console, enclosingSubject):
		super( ConsoleSubject, self ).__init__( enclosingSubject )
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
	
	def getChangeHistory(self):
		return None
	
