##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import StringBuilder

import sys
import imp
from copy import copy, deepcopy

from java.awt import Color
from java.awt.event import KeyEvent
from java.util.regex import Pattern

from javax.swing import JPopupMenu

from Britefury import LoadBuiltins

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Live import LiveValue

from BritefuryJ.AttributeTable import *

from BritefuryJ.Controls import TextEntry
from BritefuryJ.StyleSheet import *
from BritefuryJ.Graphics import *
from BritefuryJ.LSpace.Browser import Location
from BritefuryJ.LSpace.Input import ObjectDndHandler, Modifier
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Pres import InnerFragment, ApplyPerspective
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Util import TypeUtils


from LarchCore.Languages.Python25 import Python25
from LarchCore.Languages.Python25.Execution.ExecutionPresCombinators import execStdout, execStderr, execException, execResult
from LarchCore.Languages.Python25.Execution import Execution





_executeShortcut = Shortcut( KeyEvent.VK_ENTER, Modifier.CTRL )
_executeNoEvalShortcut = Shortcut( KeyEvent.VK_ENTER, Modifier.CTRL | Modifier.SHIFT )
_historyPreviousShortcut = Shortcut( KeyEvent.VK_UP, Modifier.ALT )
_historyNextShortcut = Shortcut( KeyEvent.VK_DOWN, Modifier.ALT )

_bannerTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontSmallCaps( True ), Primitive.editable( False ) )
_bannerHelpKeyTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontSmallCaps( True ), Primitive.fontItalic( True ), Primitive.foreground( Color( 0.25, 0.25, 0.25 ) ) )
_bannerHelpTextStyle = StyleSheet.style( Primitive.fontFace( 'Serif' ), Primitive.fontItalic( True ), Primitive.foreground( Color( 0.25, 0.25, 0.25 ) ) )
_bannerBorder = SolidBorder( 2.0, 5.0, 8.0, 8.0, Color( 0.3, 0.5, 0.3 ), Color( 0.875, 0.9, 0.875 ) )


_labelStyle = StyleSheet.style( Primitive.fontSize( 10 ) )

_blockStyle = StyleSheet.style( Primitive.columnSpacing( 2.0 ), Primitive.border( SolidBorder( 1.0, 5.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) ) )

_pythonModuleBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) ) )
_dropPromptStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), None ) ) )

_varAssignVarNameStyle = StyleSheet.style( Primitive.fontItalic( True ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_varAssignTypeNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.3, 0.0, 0.3 ) ) )
_varAssignJavaKindStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.4 ) ), Primitive.fontItalic( True ) )
_varAssignPythonKindStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.4, 0.0 ) ), Primitive.fontItalic( True ) )
_varAssignDocModelKindStyle = StyleSheet.style( Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ), Primitive.fontItalic( True ) )
_varAssignMsgStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.125, 0.0 ) ) )

_consoleBlockListStyle = StyleSheet.style( Primitive.columnSpacing( 5.0 ) )
_consoleStyle = StyleSheet.style( Primitive.columnSpacing( 8.0 ) )


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
	textEntry = TextEntry.regexValidated( 'var', varNameTextEntryListener, _varNameRegex, 'Please enter a valid identifier' )
	prompt = Label( 'Place object into a variable named: ' )
	textEntry.grabCaretOnRealise()
	textEntry.selectAllOnRealise()
	return _dropPromptStyle.applyTo( Border( Paragraph( [ prompt.alignVCentre(), textEntry.alignVCentre() ] ).alignHPack() ) )



class Console (object):
	class Output (object):
		def __init__(self):
			self._builder = None

		def write(self, text):
			if not ( isinstance( text, str )  or  isinstance( text, unicode ) ):
				raise TypeError, 'argument 1 must be string, not %s' % type( text )
			if self._builder is None:
				self._builder = StringBuilder()
			self._builder.append( text )

		def getText(self):
			if self._builder is not None:
				return self._builder.toString()
			else:
				return None



	def __init__(self, name, showBanner=True):
		self._incr = IncrementalValueMonitor( self )

		self._blocks = []
		self._currentPythonModule = Python25.py25NewModuleAsRoot()
		self._before = []
		self._after = []
		self._module = imp.new_module( name )
		self._showBanner = showBanner
		LoadBuiltins.loadBuiltins( self._module )


	def getBlocks(self):
		self._incr.onAccess()
		return copy( self._blocks )

	def getCurrentPythonModule(self):
		self._incr.onAccess()
		return self._currentPythonModule


	def _commit(self, module, execResult):
		self._blocks.append( ConsoleBlock( module, execResult ) )
		for a in self._after:
			if not Python25.isEmptyTopLevel(a):
				self._before.append( a )
		if not Python25.isEmptyTopLevel(module):
			self._before.append( deepcopy( module ) )
		self._after = []
		self._currentPythonModule = Python25.py25NewModuleAsRoot()
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
		setattr( self._module, name, value )
		self._blocks.append( ConsoleVarAssignment( name, value ) )
		self._incr.onChanged()



	def execute(self, bEvaluate=True):
		module = self.getCurrentPythonModule()
		if not Python25.isEmptyTopLevel(module):
			execResult = Execution.getResultOfExecutionWithinModule( module, self._module, bEvaluate )
			self._commit( module, execResult )

	def executeModule(self, module, bEvaluate=True):
		if not Python25.isEmptyTopLevel(module):
			execResult = Execution.getResultOfExecutionWithinModule( module, self._module, bEvaluate )
			self._commit( module, execResult )




	def __present__(self, fragment, inheritedState):
		blocks = InnerFragment.map( self.getBlocks() )
		currentModule = Python25.python25EditorPerspective.applyTo( InnerFragment( self.getCurrentPythonModule() ) )

		def _onDrop(element, pos, data, action):
			class _VarNameEntryListener (TextEntry.TextEntryListener):
				def onAccept(self, entry, text):
					self.assignVariable( text, data.getModel() )
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
		if self._showBanner:
			bannerVersionText = [ _bannerTextStyle.applyTo( NormalText( v ) )   for v in sys.version.split( '\n' ) ]
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
		m = m.withDropDest( dropDest )
		def _ensureCurrentModuleVisible(element, ctx, style):
			element.ensureVisible()
		m = m.withCustomElementAction( _ensureCurrentModuleVisible )

		dropPromptLive = LiveValue( Blank() )
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

		caughtException = executionResult.getCaughtException()
		result = executionResult.getResult()
		stdoutStream = executionResult.getStdOutStream()
		stderrStream = executionResult.getStdErrStream()

		moduleView = StyleSheet.style( Primitive.editable( False ) ).applyTo( Python25.python25EditorPerspective.applyTo( InnerFragment( pythonModule ) ) )
		caughtExceptionView = ApplyPerspective.defaultPerspective( InnerFragment( caughtException ) )   if caughtException is not None   else None
		resultView = ApplyPerspective.defaultPerspective( InnerFragment( result[0] ) )   if result is not None   else None

		blockContents = [ _pythonModuleBorderStyle.applyTo( Border( moduleView.alignHExpand() ).alignHExpand() ) ]
		if stderrStream is not None:
			blockContents.append( execStderr( stderrStream, True ) )
		if stdoutStream is not None:
			blockContents.append( execStdout( stdoutStream, True ) )
		if caughtExceptionView is not None:
			blockContents.append( execException( caughtExceptionView ) )
		if resultView is not None:
			blockContents.append( execResult( resultView ) )
		blockColumn = Column( blockContents ).alignHExpand()
		return _blockStyle.applyTo( Border( blockColumn ).alignHExpand() )





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







class ConsoleSubject (Subject):
	def __init__(self, console, enclosingSubject):
		super( ConsoleSubject, self ).__init__( enclosingSubject )
		self._console = console


	def getFocus(self):
		return self._console

	def getPerspective(self):
		return DefaultPerspective.instance

	def getTitle(self):
		return 'Python console'

	def getSubjectContext(self):
		return self.enclosingSubject.getSubjectContext()

	def getChangeHistory(self):
		return None




