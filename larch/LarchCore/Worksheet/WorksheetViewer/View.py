##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys

from java.awt import Color

from java.awt.event import KeyEvent

from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import MethodDispatchView

from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.LSpace.Input import Modifier
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Controls import Button, Hyperlink
from BritefuryJ.Pres import Pres, ApplyPerspective
from BritefuryJ.Pres.Primitive import Primitive, Blank, Border, Column, Row, Proxy
from BritefuryJ.Pres.RichText import TitleBar, Heading1, Heading2, Heading3, Heading4, Heading4, Heading5, Heading6, NormalText, RichSpan, Page, Body, LinkHeaderBar, EmphSpan
from BritefuryJ.Pres.ObjectPres import ObjectBorder
from BritefuryJ.Pres.UI import Section, SectionHeading2, ControlsRow
from BritefuryJ.Pres.Help import TipBox

from BritefuryJ.Projection import Perspective, Subject, SubjectPathEntry


from LarchCore.Languages.Python2 import Python2
from LarchCore.Languages.Python2.CodeGenerator import compileForModuleExecution

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet.WorksheetViewer import ViewSchema
from LarchCore.Worksheet.WorksheetEditor.View import WorksheetEditorSubject



_editableStyle = StyleSheet.style( Primitive.editable( True ) )

_worksheetMargin = 10.0

_pythonCodeBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
_pythonCodeEditorBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.5, 4.0, 10.0, 10.0, Color( 0.4, 0.4, 0.5 ), None ) ) )


def _worksheetContextMenuFactory(element, menu):
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsRow( [ refreshButton.alignHPack() ] )
	menu.add( Section( SectionHeading2( 'Worksheet' ), worksheetControls ) )
	return True




class WorksheetViewer (MethodDispatchView):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, fragment, inheritedState, node):
		bodyView = Pres.coerce( node.getBody() )

		try:
			editSubject = fragment.subject.editSubject
		except AttributeError:
			pageContents = []
		else:
			editLink = Hyperlink( 'Switch to developer mode', editSubject )
			linkHeader = LinkHeaderBar( [ editLink ] )
			pageContents = [ linkHeader ]


		tip = TipBox( [ NormalText( [ 'To edit this worksheet or add content, click ', EmphSpan( 'Switch to developer mode' ), ' at the top right' ] ) ],
			      'larchcore.worksheet.view.toedit' )

		w = Page( pageContents + [ bodyView, tip ] )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		return StyleSheet.style( Primitive.editable( False ) ).applyTo( w )


	@ObjectDispatchMethod( ViewSchema.BodyView )
	def Body(self, fragment, inheritedState, node):
		contentViews = Pres.mapCoerce( [ c    for c in node.getContents()   if c.isVisible() ] )
		return Body( contentViews ).padX( _worksheetMargin )
	
	
	@ObjectDispatchMethod( ViewSchema.ParagraphView )
	def Paragraph(self, fragment, inheritedState, node):
		text = node.getText()
		style = node.getStyle()
		if style == 'normal':
			p = NormalText( text )
		elif style == 'h1':
			p = Heading1( text )
		elif style == 'h2':
			p = Heading2( text )
		elif style == 'h3':
			p = Heading3( text )
		elif style == 'h4':
			p = Heading4( text )
		elif style == 'h5':
			p = Heading5( text )
		elif style == 'h6':
			p = Heading6( text )
		elif style == 'title':
			p = TitleBar( text )
		else:
			p = NormalText( text )
		return p


	@ObjectDispatchMethod( ViewSchema.TextSpanView )
	def TextSpan(self, fragment, inheritedState, node):
		text = node.getText()
		styleSheet = node.getStyleSheet()
		return styleSheet.applyTo( RichSpan( text ) )



	@ObjectDispatchMethod( ViewSchema.LinkView )
	def Link(self, fragment, inheritedState, node):
		subject = node.getSubject( fragment.subject.documentSubject )
		return Hyperlink( node.text, subject )


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, fragment, inheritedState, node):
		if node.isVisible():
			if node.isCodeVisible():
				codeView = Python2.python2EditorPerspective.applyTo( Pres.coerce( node.getCode() ) )
				if node.isCodeEditable():
					codeView = StyleSheet.style( Primitive.editable( True ) ).applyTo( codeView )
			else:
				codeView = None
			
			executionResultView = None
			executionResult = node.getResult()
			if executionResult is not None:
				if not node.isResultVisible():
					executionResult = executionResult.suppressStdOut().suppressResult()
				if node.isMinimal():
					executionResultView = executionResult.minimalView()
				else:
					executionResultView = executionResult.view()

			if node.isMinimal():
				return executionResultView.alignHExpand()   if executionResultView is not None   else Blank()
			else:
				boxContents = []
				if node.isCodeVisible():
					boxContents.append( _pythonCodeBorderStyle.applyTo( Border( codeView.alignHExpand() ).alignHExpand() ) )
				if node.isResultVisible()  and  executionResultView is not None:
					boxContents.append( executionResultView.alignHExpand() )
				box = StyleSheet.style( Primitive.columnSpacing( 5.0 ) ).applyTo( Column( boxContents ) )
				
				return _pythonCodeEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )
		else:
			return Blank()



	@ObjectDispatchMethod( ViewSchema.InlinePythonCodeView )
	def InlinePythonCode(self, fragment, inheritedState, node):
		assert isinstance(node, ViewSchema.InlinePythonCodeView)
		if node.isCodeVisible():
			exprView = Python2.python2EditorPerspective.applyTo( Pres.coerce( node.getExpr() ) )
			if node.isCodeEditable():
				exprView = StyleSheet.style( Primitive.editable( True ) ).applyTo( exprView )
		else:
			exprView = None

		executionResultView = None
		executionResult = node.getResult()
		if executionResult is not None:
			if node.isResultMinimal():
				executionResultView = executionResult.minimalView()
			else:
				executionResultView = executionResult.view()

		if node.isCodeVisible():
			boxContents = [ _pythonCodeBorderStyle.applyTo( Border( exprView.alignHExpand() ).alignHExpand() ) ]
			if executionResultView is not None:
				boxContents.append( executionResultView.alignHExpand() )
			box = StyleSheet.style( Primitive.rowSpacing( 5.0 ) ).applyTo( Row( boxContents ) )

			return _pythonCodeEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )
		else:
			return executionResultView.alignHPack()   if executionResultView is not None   else Proxy()


	@ObjectDispatchMethod( ViewSchema.InlineEmbeddedObjectView )
	def InlineEmbeddedObject(self, fragment, inheritedState, node):
		value = node.value
		valueView = _editableStyle.applyTo( ApplyPerspective( None, value ) )
		return valueView



	@ObjectDispatchMethod( ViewSchema.ParagraphEmbeddedObjectView )
	def ParagraphEmbeddedObject(self, fragment, inheritedState, node):
		value = node.value
		valueView = _editableStyle.applyTo( ApplyPerspective( None, value ) )

		hideFrame = getattr( value, '__embed_hide_frame__', False )
		p = ObjectBorder( valueView )   if not hideFrame   else valueView

		return p




_view = WorksheetViewer()
perspective = Perspective( _view.fragmentViewFunction, None )


class _WorksheetModuleLoader (object):
	def __init__(self, model, document):
		self._model = model
		self._document = document
		
	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass

		mod = self._document.newModule( fullname, self )
		
		worksheet = self._model
		body = worksheet['body']
		
		for i, node in enumerate( body['contents'] ):
			if node.isInstanceOf( Schema.PythonCode ):
				code = compileForModuleExecution( mod, node['code'], fullname + '_' + str( i ) )
				exec code in mod.__dict__
		return mod



def _refreshWorksheet(subject, pageController):
	subject._modelView.refreshResults()


_refreshCommand = Command( CommandName( '&Refresh worksheet' ), _refreshWorksheet, Shortcut( KeyEvent.VK_ENTER, Modifier.CTRL ) )
_worksheetViewerCommands = CommandSet( 'LarchCore.Worksheet.Viewer', [ _refreshCommand ] )



class _WorksheetEditorPathEntry (SubjectPathEntry):
	def follow(self, outerSubject):
		return outerSubject.editSubject


	def __getstate__(self):
		return False

	def __setstate__(self):
		pass


_WorksheetEditorPathEntry.instance = _WorksheetEditorPathEntry()


class WorksheetViewerSubject (Subject):
	def __init__(self, document, model, enclosingSubject, path, importName, title):
		super( WorksheetViewerSubject, self ).__init__( enclosingSubject, path )
		self._document = document
		self._model = model
		# Defer the creation of the model view - it involves executing all the code in the worksheet which can take some time
		self.__modelView = None
		self._importName = importName
		self._title = title
		
		self.editSubject = WorksheetEditorSubject( document, model, self, path.followedBy( _WorksheetEditorPathEntry.instance ), self._importName, title )


	@property
	def viewSubject(self):
		return self

	
	@property
	def _modelView(self):
		if self.__modelView is None:
			self.__modelView = ViewSchema.WorksheetView( None, self._model, self._importName )
		return self.__modelView


	def getTrailLinkText(self):
		return self._title + '[Ws]'


	def getFocus(self):
		f = self._modelView
		# This causes execution results to refresh on page view
		f.refreshResults()
		return f
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return self._title + ' [Ws]'
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()

	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _worksheetViewerCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )

	def createModuleLoader(self, document):
		return _WorksheetModuleLoader( self._model, document )
