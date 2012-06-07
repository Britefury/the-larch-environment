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

from java.util.regex import Pattern
import java.util.List

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import MethodDispatchView

from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.AttributeTable import *

from BritefuryJ.LSpace import *
from BritefuryJ.Graphics import *
from BritefuryJ.Browser import Location
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Controls import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.ContextMenu import *
from BritefuryJ.Pres.ObjectPres import *
from BritefuryJ.Pres.UI import *

from BritefuryJ.Projection import Perspective, Subject


from LarchCore.Languages.Python25 import Python25
from LarchCore.Languages.Python25.CodeGenerator import compileForModuleExecution

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet.WorksheetViewer import ViewSchema
from LarchCore.Worksheet.WorksheetEditor.View import WorksheetEditorSubject



_editableStyle = StyleSheet.style( Primitive.editable( True ) )

_pythonCodeBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
_pythonCodeEditorBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) ) )

_quoteLocationHeaderStyle = StyleSheet.style( Primitive.background( FillPainter( Color( 0.75, 0.8, 0.925 ) ) ) )
_quoteLocationBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
_quoteLocationEditorBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) ) )


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
		bodyView = InnerFragment( node.getBody() )
		
		editLocation = fragment.getSubjectContext()['editLocation']

		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		editLink = Hyperlink( 'Switch to developer mode', editLocation )
		linkHeader = SplitLinkHeaderBar( [ editLink ], [ homeLink ] )
		
		w = Page( [ linkHeader, bodyView ] )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		return StyleSheet.style( Primitive.editable( False ) ).applyTo( w )


	@ObjectDispatchMethod( ViewSchema.BodyView )
	def Body(self, fragment, inheritedState, node):
		contentViews = InnerFragment.map( [ c    for c in node.getContents()   if c.isVisible() ] )
		return Body( contentViews )
	
	
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


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, fragment, inheritedState, node):
		if node.isVisible():
			if node.isCodeVisible():
				codeView = Python25.python25EditorPerspective.applyTo( InnerFragment( node.getCode() ) )
				if node.isCodeEditable():
					codeView = StyleSheet.style( Primitive.editable( True ) ).applyTo( codeView )
			else:
				codeView = None
			
			executionResultView = None
			executionResult = node.getResult()
			if executionResult is not None:
				if not node.isResultVisible():
					executionResult = executionResult.suppressStdOut().suppressResult()
				if node.isCodeVisible():
					executionResultView = executionResult.view()
				else:
					executionResultView = executionResult.minimalView()

			if node.isResultMinimal():
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



	@ObjectDispatchMethod( ViewSchema.InlineEmbeddedObjectView )
	def InlineEmbeddedObject(self, fragment, inheritedState, node):
		value = node.getValue()
		valueView = _editableStyle.applyTo( ApplyPerspective( None, value ) )
		return valueView



	@ObjectDispatchMethod( ViewSchema.ParagraphEmbeddedObjectView )
	def ParagraphEmbeddedObject(self, fragment, inheritedState, node):
		value = node.getValue()
		valueView = _editableStyle.applyTo( ApplyPerspective( None, value ) )
		p = ObjectBorder( valueView )
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
		
		sources = []
		
		worksheet = self._model
		body = worksheet['body']
		
		for i, node in enumerate( body['contents'] ):
			if node.isInstanceOf( Schema.PythonCode ):
				code = compileForModuleExecution( mod, node['code'], fullname + '_' + str( i ) )
				exec code in mod.__dict__
		return mod



def _refreshWorksheet(subject, pageController):
	subject._modelView.refreshResults()


_refreshCommand = Command( CommandName( '&Refresh worksheet' ), _refreshWorksheet, Shortcut( KeyEvent.VK_F5, 0 ) )
_worksheetViewerCommands = CommandSet( 'LarchCore.Worksheet.Viewer', [ _refreshCommand ] )


class WorksheetViewerSubject (Subject):
	def __init__(self, document, model, enclosingSubject, location, importName, title):
		super( WorksheetViewerSubject, self ).__init__( enclosingSubject )
		assert isinstance( location, Location )
		self._document = document
		self._model = model
		# Defer the creation of the model view - it involves executing all the code in the worksheet which can take some time
		self._modelView = None
		self._location = location
		self._importName = importName
		self._editLocation = self._location + '.edit'
		self._title = title
		
		self.edit = WorksheetEditorSubject( document, model, self, self._editLocation, self._importName, title )

	
	def _getModelView(self):
		if self._modelView is None:
			self._modelView = ViewSchema.WorksheetView( None, self._model, self._importName )
		return self._modelView
		
	

	def getFocus(self):
		return self._getModelView()
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return self._title + ' [Ws-User]'
	
	def getSubjectContext(self):
		return self.enclosingSubject.getSubjectContext().withAttrs( location=self._location, editLocation=self._editLocation, viewLocation=self._location )
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()

	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _worksheetViewerCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )

	def createModuleLoader(self, document):
		return _WorksheetModuleLoader( self._model, document )
	
	
	def __getattr__(self, name):
		module = self._getModelView().getModule()
		try:
			subjectFactory = module.__subject__
		except AttributeError:
			return getattr( module, name )
		else:
			subject = subjectFactory( self._document, module, self, self._location )
			return getattr( subject, name )

	

	
	