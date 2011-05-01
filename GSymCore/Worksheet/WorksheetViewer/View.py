##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.awt import Color

from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import ObjectDispatchView


from Britefury.Util.NodeUtil import *
from Britefury.Util.InstanceCache import instanceCache

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.Controls import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.ContextMenu import *

from BritefuryJ.Projection import Perspective, Subject


from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.CodeGenerator import compileForModuleExecution
from GSymCore.Languages.Python25.Execution.ExecutionPresCombinators import executionResultBox, minimalExecutionResultBox

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema
from GSymCore.Worksheet.WorksheetEditor.View import perspective as editorPerspective, WorksheetEditorSubject



_pythonCodeBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_pythonCodeEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )

_quoteLocationHeaderStyle = StyleSheet.instance.withAttr( Primitive.background, FillPainter( Color( 0.75, 0.8, 0.925 ) ) )
_quoteLocationBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_quoteLocationEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )


def _worksheetContextMenuFactory(element, menu):
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsRow( [ refreshButton.alignHPack() ] )
	menu.add( SectionColumn( [ SectionTitle( 'Worksheet' ), worksheetControls ] ) )
	return True




class WorksheetViewer (ObjectDispatchView):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, fragment, inheritedState, node):
		bodyView = InnerFragment( node.getBody() )
		
		editLocation = fragment.getSubjectContext()['editLocation']
		
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		editLink = Hyperlink( 'Edit this worksheet', editLocation )
		linkHeader = SplitLinkHeaderBar( [ editLink ], [ homeLink ] )
		
		w = Page( [ linkHeader, bodyView ] )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		return StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( w )


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
		return p


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, fragment, inheritedState, node):
		if node.isVisible():
			if node.isCodeVisible():
				codeView = Python25.python25EditorPerspective.applyTo( InnerFragment( node.getCode() ) )
				if node.isCodeEditable():
					codeView = StyleSheet.instance.withAttr( Primitive.editable, True ).applyTo( codeView )
			else:
				codeView = None
			
			executionResultView = None
			executionResult = node.getResult()
			if executionResult is not None:
				if node.isResultVisible():
					stdout = executionResult.getStdOut()
					result = executionResult.getResult()
				else:
					stdout = None
					result = None
				exc = executionResult.getCaughtException()
				if node.isCodeVisible():
					executionResultView = executionResultBox( stdout, executionResult.getStdErr(), exc, result, True, True )
				else:
					executionResultView = minimalExecutionResultBox( stdout, executionResult.getStdErr(), exc, result, True, True )
			
			if node.isResultMinimal():
				return executionResultView.alignHExpand()   if executionResultView is not None   else HiddenContent( '' )
			else:
				boxContents = []
				if node.isCodeVisible():
					boxContents.append( _pythonCodeBorderStyle.applyTo( Border( codeView.alignHExpand() ).alignHExpand() ) )
				if node.isResultVisible()  and  executionResultView is not None:
					boxContents.append( executionResultView.alignHExpand() )
				box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( boxContents ) )
				
				return _pythonCodeEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )
		else:
			return HiddenContent( '' )


	
	@ObjectDispatchMethod( ViewSchema.QuoteLocationView )
	def QuoteLocation(self, fragment, inheritedState, node):
		targetView = StyleSheet.instance.withAttr( Primitive.editable, True ).applyTo( LocationAsInnerFragment( Location( node.getLocation() ) ) )
		
		if node.isMinimal():
			return targetView.alignHExpand()
		else:
			headerBox = _quoteLocationHeaderStyle.applyTo( Bin(
				StyleSheet.instance.withAttr( Primitive.rowSpacing, 20.0 ).applyTo( Row(
			                [ Label( 'Location: ' ).alignHExpand(), Label( node.getLocation() ) ] ) ).alignHExpand().pad( 2.0, 2.0 ) ) )
			
			boxContents = [ headerBox.alignHExpand() ]
			boxContents.append( _quoteLocationBorderStyle.applyTo( Border( targetView.alignHExpand() ).alignHExpand() ) )
			box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( boxContents ) )
			
			return _quoteLocationEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )





perspective = Perspective( WorksheetViewer(), None )


class _WorksheetModuleLoader (object):
	def __init__(self, model, document):
		self._model = model
		self._document = document
		
	def load_module(self, fullname):
		mod = self._document.newModule( fullname, self )
		
		sources = []
		
		worksheet = self._model
		body = worksheet['body']
		
		for i, node in enumerate( body['contents'] ):
			if node.isInstanceOf( Schema.PythonCode ):
				code = compileForModuleExecution( mod, node['code'], fullname + '_' + str( i ) )
				exec code in mod.__dict__
		return mod



class WorksheetViewerSubject (Subject):
	def __init__(self, document, model, enclosingSubject, location, title):
		super( WorksheetViewerSubject, self ).__init__( enclosingSubject )
		self._document = document
		self._model = model
		# Defer the creation of the model view - it involves executing all the code in the worksheet which can take some time
		self._modelView = None
		self._enclosingSubject = enclosingSubject
		self._location = location
		self._editLocation = self._location + '.edit'
		self._title = title
		
		self.edit = WorksheetEditorSubject( document, model, self, self._editLocation, title )

	
	def _getModelView(self):
		if self._modelView is None:
			self._modelView = ViewSchema.WorksheetView( None, self._model )
		return self._modelView
		
	

	def getFocus(self):
		return self._getModelView()
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return self._title + ' [WsView]'
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( location=self._location, editLocation=Location( self._editLocation ), viewLocation=Location( self._location ) )
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()
	
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

	

	
	