##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import StringBuilder

import sys
from copy import copy, deepcopy

from java.awt import Color
from java.awt.event import KeyEvent
from java.util.regex import Pattern

from javax.swing import JPopupMenu

from Britefury import LoadBuiltins

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Live import LiveValue

from BritefuryJ.AttributeTable import SimpleAttributeTable

from BritefuryJ.Controls import TextEntry
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.LSpace.Input import ObjectDndHandler, Modifier
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Pres import Pres, ApplyPerspective
from BritefuryJ.Pres.Primitive import Primitive, Blank, Label, LineBreak, Border, Paragraph, Row, Column, Span
from BritefuryJ.Pres.RichText import NormalText

from BritefuryJ.Projection import Perspective, TransientSubject
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Util import TypeUtils


from LarchCore.Languages.Python2 import Python2

from LarchCore.Kernel import execution_pres
from LarchCore.Kernel.python import inproc_kernel



_executeShortcut = Shortcut( KeyEvent.VK_ENTER, Modifier.CTRL )
_executeNoEvalShortcut = Shortcut( KeyEvent.VK_ENTER, Modifier.CTRL | Modifier.SHIFT )
_historyPreviousShortcut = Shortcut( KeyEvent.VK_UP, Modifier.ALT )
_historyNextShortcut = Shortcut( KeyEvent.VK_DOWN, Modifier.ALT )

_bannerTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontSmallCaps( True ), Primitive.editable( False ) )
_bannerHelpKeyTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontSmallCaps( True ), Primitive.fontItalic( True ), Primitive.foreground( Color( 0.25, 0.25, 0.25 ) ) )
_bannerHelpTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontItalic( True ), Primitive.foreground( Color( 0.25, 0.25, 0.25 ) ) )
_bannerBorder = SolidBorder( 2.0, 5.0, 8.0, 8.0, Color( 0.3, 0.5, 0.3 ), Color( 0.875, 0.9, 0.875 ) )


_labelStyle = StyleSheet.style( Primitive.fontSize( 10 ) )

#_blockStyle = StyleSheet.style( Primitive.columnSpacing( 2.0 ), Primitive.border( SolidBorder( 1.0, 5.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) ) )
_blockStyle = StyleSheet.style( Primitive.columnSpacing( 3.0 ), Primitive.border( SolidBorder( 1.0, 3.0, 13.0, 13.0, Color( 0.6, 0.6, 0.6 ), Color( 0.9, 0.9, 0.9 ) ) ) )
_blockOutputStyle = StyleSheet.style( Primitive.columnSpacing( 2.0 ) )

_pythonModuleBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.5, 5.0, 10.0, 10.0, Color( 0.65, 0.65, 0.65 ), Color.WHITE ) ) )
_dropPromptStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), None ) ) )

_varAssignVarNameStyle = StyleSheet.style( Primitive.fontItalic( True ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_varAssignTypeNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.3, 0.0, 0.3 ) ) )
_varAssignJavaKindStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.4 ) ), Primitive.fontItalic( True ) )
_varAssignPythonKindStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.4, 0.0 ) ), Primitive.fontItalic( True ) )
_varAssignDocModelKindStyle = StyleSheet.style( Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ), Primitive.fontItalic( True ) )
_varAssignMsgStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.125, 0.0 ) ) )

_consoleBlockListStyle = StyleSheet.style( Primitive.columnSpacing( 5.0 ) )
_consoleStyle = StyleSheet.style( Primitive.columnSpacing( 9.0 ) )



_objectKindJava = _varAssignJavaKindStyle( Label( 'Java object' ) )
_objectKindPython = _varAssignPythonKindStyle( Label( 'Python object' ) )
_objectKindDocModel = _varAssignDocModelKindStyle( Label( 'DocModel object' ) )

_objectKindMap = {
	TypeUtils.ObjectKind.JAVA : _objectKindJava,
	TypeUtils.ObjectKind.PYTHON : _objectKindPython,
	TypeUtils.ObjectKind.DOCMODEL : _objectKindDocModel,
	}




_varNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' )


def _dropPrompt(varNameTextEntryListener):
	textEntry = TextEntry( 'var', varNameTextEntryListener ).regexValidated( _varNameRegex, 'Please enter a valid identifier' )
	prompt = Label( 'Place object into a variable named: ' )
	textEntry.grabCaretOnRealise()
	textEntry.selectAllOnRealise()
	return _dropPromptStyle.applyTo( Border( Paragraph( [ prompt.alignVCentre(), textEntry.alignVCentre() ] ).alignHPack() ) )



class Console (object):
	def __init__(self, kernel, showBanner=True):
		self._incr = IncrementalValueMonitor( self )

		self._blocks = []
		self._currentPythonModule = Python2.py25NewModuleAsRoot()
		self._before = []
		self._after = []
		self._kernel = kernel
		self._module = self._kernel.get_live_module()
		self._can_assign = isinstance(self._module, inproc_kernel.InProcessLiveModule)
		self._banner_text = ''

		if showBanner:
			def on_result(result):
				self._banner_text = result.result
				self._incr.onChanged()

			self._module.evaluate('__import__("sys").version', on_result)

		self._showBanner = showBanner


	def getBlocks(self):
		self._incr.onAccess()
		return copy( self._blocks )

	def getCurrentPythonModule(self):
		self._incr.onAccess()
		return self._currentPythonModule


	def _commit(self, module, execResult):
		self._blocks.append( ConsoleBlock( module, execResult ) )
		for a in self._after:
			if not Python2.isEmptyTopLevel(a):
				self._before.append( a )
		if not Python2.isEmptyTopLevel(module):
			self._before.append( deepcopy( module ) )
		self._after = []
		self._currentPythonModule = Python2.py25NewModuleAsRoot()
		self._incr.onChanged()

	def backwards(self):
		if len( self._before ) > 0:
			self._after.insert( 0, self._currentPythonModule )
			self._currentPythonModule = self._before.pop()
			self._incr.onChanged()

	def forwards(self):
		if len( self._after ) > 0:
			self._before.append( self._currentPythonModule )
			self._currentPythonModule = self._after[0]
			del self._after[0]
			self._incr.onChanged()


	def assignVariable(self, name, value):
		if self._can_assign:
			self._module.assign_variable(name, value)
			self._blocks.append( ConsoleVarAssignment( name, value ) )
			self._incr.onChanged()



	def execute(self, bEvaluate=True):
		module = self.getCurrentPythonModule()
		if not Python2.isEmptyTopLevel(module):
			def on_result(result):
				self._commit(module, result)
			self._module.execute(module, bEvaluate, on_result)

	def executeModule(self, module, bEvaluate=True):
		if not Python2.isEmptyTopLevel(module):
			def on_result(result):
				self._commit(module, result)
			self._module.execute(module, bEvaluate, on_result)




	def __present__(self, fragment, inheritedState):
		blocks = self.getBlocks()
		currentModule = Python2.python2EditorPerspective.applyTo( self.getCurrentPythonModule() )

		def _onDrop(element, pos, data, action):
			class _VarNameEntryListener (TextEntry.TextEntryListener):
				def onAccept(listenerSelf, entry, text):
					self.assignVariable( text, data.getModel() )
					_finish( entry )

				def onCancel(listenerSelf, entry, text):
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
		if self._showBanner:
			bannerVersionText = [ _bannerTextStyle.applyTo( NormalText( v ) )   for v in self._banner_text.split( '\n' ) ]
			helpText1 = Row( [ _bannerHelpKeyTextStyle.applyTo( Label( 'Ctrl+Enter' ) ),
					   _bannerHelpTextStyle.applyTo( Label( ' - execute and evaluate, ' ) ),
					   _bannerHelpKeyTextStyle.applyTo( Label( 'Ctrl+Shift+Enter' ) ),
					   _bannerHelpTextStyle.applyTo( Label( ' - execute only' ) ) ] )
			helpText2 = Row( [ _bannerHelpKeyTextStyle.applyTo( Label( 'Alt+Up' ) ),
					   _bannerHelpTextStyle.applyTo( Label( ' - previous, ' ) ),
					   _bannerHelpKeyTextStyle.applyTo( Label( 'Alt+Down' ) ),
					   _bannerHelpTextStyle.applyTo( Label( ' - next' ) ) ] )
			bannerText = Column( bannerVersionText + [ helpText1, helpText2 ] ).alignHPack()

			banner = _bannerBorder.surround( bannerText )
		else:
			banner = None


		dropDest = ObjectDndHandler.DropDest( FragmentData, _onDrop )

		def _onExecute(element):
			self.execute( True )

		def _onExecuteNoEval(element):
			self.execute( False )

		def _onHistoryPrev(element):
			self.backwards()

		def _onHistoryNext(element):
			self.forwards()

		currentModule = Span( [ currentModule ] )
		currentModule = currentModule.withShortcut( _executeShortcut, _onExecute )
		currentModule = currentModule.withShortcut( _executeNoEvalShortcut, _onExecuteNoEval )
		currentModule = currentModule.withShortcut( _historyPreviousShortcut, _onHistoryPrev )
		currentModule = currentModule.withShortcut( _historyNextShortcut, _onHistoryNext )

		m = _pythonModuleBorderStyle.applyTo( Border( currentModule.alignHExpand() ) ).alignHExpand()
		if self._can_assign:
			m = m.withDropDest( dropDest )
		def _ensureCurrentModuleVisible(element, ctx, style):
			element.ensureVisible()
		m = m.withCustomElementAction( _ensureCurrentModuleVisible )

		dropPromptLive = LiveValue( Span( [] ) )
		dropPromptView = dropPromptLive

		consoleColumnContents = [ banner.alignVRefY() ]   if self._showBanner   else []
		if len( blocks ) > 0:
			blockList = _consoleBlockListStyle.applyTo( Column( blocks ) ).alignHExpand()
			consoleColumnContents += [ blockList.alignVRefY(), dropPromptView.alignVRefY(), m.alignVRefY() ]
		else:
			consoleColumnContents += [ dropPromptView.alignVRefY(), m.alignVRefY() ]
		return _consoleStyle.applyTo( Column( consoleColumnContents ) ).alignHExpand().alignVTop()




class ConsoleBlock (object):
	def __init__(self, pythonModule, execResult):
		self._incr = IncrementalValueMonitor( self )

		self._pythonModule = pythonModule
		self._execResult = execResult



	def getPythonModule(self):
		self._incr.onAccess()
		return self._pythonModule

	def getExecResult(self):
		self._incr.onAccess()
		return self._execResult



	def __present__(self, fragment, inheritedState):
		pythonModule = self.getPythonModule()

		executionResult = self.getExecResult()

		caughtException = executionResult.caught_exception
		result = executionResult.result
		streams = executionResult.streams

		moduleView = StyleSheet.style( Primitive.editable( False ) ).applyTo( Python2.python2EditorPerspective.applyTo( pythonModule ) )
		caughtExceptionView = ApplyPerspective.defaultPerspective( caughtException )   if caughtException is not None   else None
		resultView = ApplyPerspective.defaultPerspective( result )   if executionResult.has_result()   else None

		code = _pythonModuleBorderStyle.applyTo( Border( moduleView.alignHExpand() ).alignHExpand() )
		outputContents = []
		for stream in streams:
			outputContents.append( execution_pres.stream_pres( stream.rich_string, stream.name ) )
		if caughtExceptionView is not None:
			outputContents.append( execution_pres.exec_exception( caughtExceptionView ) )
		if resultView is not None:
			outputContents.append( execution_pres.exec_result( resultView ) )
		outputColumn = _blockOutputStyle.applyTo( Column( outputContents ).alignHExpand() )
		return _blockStyle.applyTo( Border( Column( [ code, outputColumn ] ) ) ).alignHExpand()





class ConsoleVarAssignment (object):
	def __init__(self, varName, value):
		self._varName = varName
		self._value = value


	def getVarName(self):
		return self._varName

	def getValue(self):
		return self._value



	def __present__(self, fragment, inheritedState):
		varName = self.getVarName()
		valueTypeName = TypeUtils.nameOfTypeOf( self.getValue() )
		valueKind = TypeUtils.getKindOfObject( self.getValue() )

		varNameView = _varAssignVarNameStyle.applyTo( Label( varName ) )
		typeNameView = _varAssignTypeNameStyle.applyTo( Label( valueTypeName ) )
		typeKindView = _objectKindMap[valueKind]

		return Paragraph( [ _varAssignMsgStyle.applyTo( Label( 'Variable ' ) ), LineBreak(),
				    varNameView, LineBreak(),
				    _varAssignMsgStyle.applyTo( Label( ' was assigned a ' ) ), LineBreak(),
				    typeNameView, LineBreak(),
				    _varAssignMsgStyle.applyTo( Label( ' (a ' ) ), LineBreak(),
				    typeKindView, LineBreak(),
				    _varAssignMsgStyle.applyTo( Label( ')' ) ) ] ).alignHPack()







class ConsoleSubject (TransientSubject):
	def __init__(self, console, enclosingSubject):
		super( ConsoleSubject, self ).__init__( enclosingSubject )
		self._console = console


	def getFocus(self):
		return self._console

	def getPerspective(self):
		return DefaultPerspective.instance

	def getTitle(self):
		return 'Python console'

	def getChangeHistory(self):
		return None




