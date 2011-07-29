##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os
from datetime import datetime

from java.awt import Color
from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Kernel.View.DispatchView import ObjectDispatchView
from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod


from BritefuryJ.AttributeTable import *

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.Pres import InnerFragment, LocationAsInnerFragment


from BritefuryJ.Controls import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.ContextMenu import *

from BritefuryJ.DocPresent.Interactor import KeyElementInteractor

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *

from LarchCore.Languages.Python25 import Python25
from LarchCore.Languages.Python25.Execution.ExecutionPresCombinators import executionResultBox, minimalExecutionResultBox

from LarchCore.Worksheet.WorksheetEditor import EditorSchema
from LarchCore.Worksheet.WorksheetEditor.RichTextEditor import WorksheetRichTextEditor



_pythonCodeHeaderStyle = StyleSheet.instance.withAttr( Primitive.background, FillPainter( Color( 0.75, 0.8, 0.925 ) ) )
_pythonCodeBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_pythonCodeEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )

_quoteLocationHeaderStyle = StyleSheet.instance.withAttr( Primitive.background, FillPainter( Color( 0.75, 0.8, 0.925 ) ) )
_quoteLocationBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_quoteLocationEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )



class WorksheetNodeInteractor (KeyElementInteractor):
	def __init__(self):
		pass


	def keyPressed(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_F5:
			ctx = element.getFragmentContext()
			node = ctx.getModel()
			node.refreshResults()
			return True
		else:
			return False

	def keyReleased(self, element, event):
		return False

	def keyTyped(self, element, event):
		return False

WorksheetNodeInteractor.instance = WorksheetNodeInteractor()




class ParagraphNodeInteractor (KeyElementInteractor):
	def __init__(self):
		pass


	def keyTyped(self, element, event):
		return False


	def keyPressed(self, element, event):
		if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
			ctx = element.getFragmentContext()
			node = ctx.getModel()

			if event.getKeyCode() == KeyEvent.VK_N:
				node.setStyle( 'normal' )
			elif event.getKeyCode() == KeyEvent.VK_1:
				node.setStyle( 'h1' )
			elif event.getKeyCode() == KeyEvent.VK_2:
				node.setStyle( 'h2' )
			elif event.getKeyCode() == KeyEvent.VK_3:
				node.setStyle( 'h3' )
			elif event.getKeyCode() == KeyEvent.VK_4:
				node.setStyle( 'h4' )
			elif event.getKeyCode() == KeyEvent.VK_5:
				node.setStyle( 'h5' )
			elif event.getKeyCode() == KeyEvent.VK_6:
				node.setStyle( 'h6' )
			elif event.getKeyCode() == KeyEvent.VK_T:
				node.setStyle( 'title' )
			elif event.getKeyCode() == KeyEvent.VK_C:
				self._insertPythonCode( ctx, element, node )
				return True
			else:
				return False

			return True

		return False



	def keyReleased(self, element, event):
		return False



	def _insertPythonCode(self, ctx, element, node):
		#return element.postTreeEvent( InsertPythonCodeOperation( node.getModel() ) )
		return True

ParagraphNodeInteractor.instance = ParagraphNodeInteractor()





def _worksheetContextMenuFactory(element, menu):
	rootElement = element.getRootElement()

	
	def makeStyleFn(style):
		def _modifyParagraph(paragraph):
			paragraph.setStyle( style )

		def _onLink(link, event):
			caret = rootElement.getCaret()
			if caret.isValid():
				WorksheetRichTextEditor.instance.modifyParagraphAtMarker( caret.getMarker(), _modifyParagraph )
		return _onLink
	
	normalStyle = Hyperlink( 'Normal', makeStyleFn( 'normal' ) )
	h1Style = Hyperlink( 'H1', makeStyleFn( 'h1' ) )
	h2Style = Hyperlink( 'H2', makeStyleFn( 'h2' ) )
	h3Style = Hyperlink( 'H3', makeStyleFn( 'h3' ) )
	h4Style = Hyperlink( 'H4', makeStyleFn( 'h4' ) )
	h5Style = Hyperlink( 'H5', makeStyleFn( 'h5' ) )
	h6Style = Hyperlink( 'H6', makeStyleFn( 'h6' ) )
	titleStyle = Hyperlink( 'Title', makeStyleFn( 'title' ) )
	styles = ControlsRow( [ normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style, titleStyle ] )
	menu.add( SectionColumn( [ SectionTitle( 'Style' ), styles ] ).alignHExpand() )
	
	
	def _onPythonCode(link, event):
		def _makePythonCode():
			return EditorSchema.PythonCodeEditor.newPythonCode()
		
		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextEditor.instance.insertParagraphAtCaret( caret, _makePythonCode )

	newCode = Hyperlink( 'Python code', _onPythonCode )
	codeControls = ControlsRow( [ newCode ] )
	menu.add( SectionColumn( [ SectionTitle( 'Code' ), codeControls ] ).alignHExpand() )
	
	
	def _onQuoteLocation(link, event):
		def _makeQuoteLocation():
			return EditorSchema.QuoteLocationEditor.newQuoteLocation()

		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextEditor.instance.insertParagraphAtCaret( caret, _makeQuoteLocation )

	newQuoteLocation = Hyperlink( 'View of Location', _onQuoteLocation )
	quoteLocationControls = ControlsRow( [ newQuoteLocation ] )
	menu.add( SectionColumn( [ SectionTitle( 'Location' ), quoteLocationControls ] ).alignHExpand() )
	
	
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsRow( [ refreshButton.alignHPack() ] )
	menu.add( SectionColumn( [ SectionTitle( 'Worksheet' ), worksheetControls ] ).alignHExpand() )
	return True



class WorksheetEditor (ObjectDispatchView):
	@ObjectDispatchMethod( EditorSchema.WorksheetEditor )
	def Worksheet(self, fragment, inheritedState, node):
		bodyView = InnerFragment( node.getBody() )
		
		viewLocation = fragment.getSubjectContext()['viewLocation']
		
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		viewLink = Hyperlink( 'View this worksheet', viewLocation )
		linkHeader = SplitLinkHeaderBar( [ viewLink ], [ homeLink ] )

		
		w = Page( [ linkHeader, bodyView ] )
		w = w.withElementInteractor( WorksheetNodeInteractor.instance )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		w = WorksheetRichTextEditor.instance.region( w )
		return w
	
	
	@ObjectDispatchMethod( EditorSchema.BodyEditor )
	def Body(self, fragment, inheritedState, node):
		contentViews = list( InnerFragment.map( node.getContents() ) )

		b = Body( contentViews )
		b = WorksheetRichTextEditor.instance.editableBlock( node, b )
		return b
	
	
	@ObjectDispatchMethod( EditorSchema.ParagraphEditor )
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
		p = WorksheetRichTextEditor.instance.editableParagraph( node, p )
		p = p.withElementInteractor( ParagraphNodeInteractor.instance )
		return p
	
	
	@ObjectDispatchMethod( EditorSchema.BlankParagraphEditor )
	def BlankParagraph(self, fragment, inheritedState, node):
		style = node.getStyle()
		if style == 'normal':
			p = NormalText( '' )
		elif style == 'h1':
			p = Heading1( '' )
		elif style == 'h2':
			p = Heading2( '' )
		elif style == 'h3':
			p = Heading3( '' )
		elif style == 'h4':
			p = Heading4( '' )
		elif style == 'h5':
			p = Heading5( '' )
		elif style == 'h6':
			p = Heading6( '' )
		elif style == 'title':
			p = TitleBar( '' )
		p = WorksheetRichTextEditor.instance.editableParagraph( node, p )
		p = p.withElementInteractor( ParagraphNodeInteractor.instance )
		return p


	@ObjectDispatchMethod( EditorSchema.TextSpanEditor )
	def TextSpan(self, fragment, inheritedState, node):
		text = node.getText()
		styleAttrs = node.getStyleAttrs()
		p = RichSpan( text )
		p = WorksheetRichTextEditor.instance.editableSpan( node, p )
		return p


	
	@ObjectDispatchMethod( EditorSchema.PythonCodeEditor )
	def PythonCode(self, fragment, inheritedState, node):
		choiceValues = [
		        EditorSchema.PythonCodeEditor.STYLE_MINIMAL_RESULT,
		        EditorSchema.PythonCodeEditor.STYLE_RESULT,
		        EditorSchema.PythonCodeEditor.STYLE_CODE_AND_RESULT,
		        EditorSchema.PythonCodeEditor.STYLE_CODE,
		        EditorSchema.PythonCodeEditor.STYLE_EDITABLE_CODE_AND_RESULT,
		        EditorSchema.PythonCodeEditor.STYLE_EDITABLE_CODE,
		        EditorSchema.PythonCodeEditor.STYLE_HIDDEN ]

		
		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			node.setStyle( style )
			
		def _onDeleteButton(button, event):
			WorksheetRichTextEditor.instance.deleteParagraphContainingElement( button.getElement() )

		codeView = Python25.python25EditorPerspective.applyTo( InnerFragment( node.getCode() ) )
		
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
			executionResultView = executionResultBox( stdout, executionResult.getStdErr(), exc, result, True, True )
			
			
		optionTexts = [ 'Minimal result', 'Result', 'Code with result', 'Code', 'Editable code with result', 'Editable code', 'Hidden' ]
		optionChoices = [ StaticText( text )   for text in optionTexts ]
		styleOptionMenu = OptionMenu( optionChoices, choiceValues.index( node.getStyle() ), _onStyleOptionMenu )
		
		deleteButton = Button( Image.systemIcon( 'delete' ), _onDeleteButton )
		
		headerBox = _pythonCodeHeaderStyle.applyTo( Bin(
		        StyleSheet.instance.withAttr( Primitive.rowSpacing, 20.0 ).applyTo( Row( [
		                Row( [ Label( 'Python code' ) ] ).alignHLeft(),
		                Row( [ styleOptionMenu, deleteButton.alignVCentre() ] ).alignHRight() ] ) ).pad( 2.0, 2.0 ) ) )
		
		boxContents = [ headerBox ]
		boxContents.append( _pythonCodeBorderStyle.applyTo( Border( codeView ) ) )
		if executionResultView is not None:
			boxContents.append( executionResultView.alignHExpand() )
		box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( boxContents ) )
		
		p = _pythonCodeEditorBorderStyle.applyTo( Border( box ).alignHExpand() )

		p = WorksheetRichTextEditor.instance.editableParagraphEmbed( node, p )
		return p


	
	@ObjectDispatchMethod( EditorSchema.QuoteLocationEditor )
	def QuoteLocation(self, fragment, inheritedState, node):
		choiceValues = [
		        EditorSchema.QuoteLocationEditor.STYLE_MINIMAL,
		        EditorSchema.QuoteLocationEditor.STYLE_NORMAL ]
		
		
		class _LocationEntryListener (TextEntry.TextEntryListener):
			def onAccept(self, entry, location):
				node.setLocation( location )

		
		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			node.setStyle( style )
			
		def _onDeleteButton(button, event):
			WorksheetRichTextEditor.instance.deleteParagraphContainingElement( button.getElement() )

		targetView = StyleSheet.instance.withAttr( Primitive.editable, True ).applyTo( LocationAsInnerFragment( Location( node.getLocation() ) ) )
		
		optionTexts = [ 'Minimal', 'Normal' ]
		optionChoices = [ StaticText( text )   for text in optionTexts ]
		styleOptionMenu = OptionMenu( optionChoices, choiceValues.index( node.getStyle() ), _onStyleOptionMenu )
		
		deleteButton = Button( Image.systemIcon( 'delete' ), _onDeleteButton )
		
		locationEditor = TextEntry( node.getLocation(), _LocationEntryListener() )
		
		headerBox = _quoteLocationHeaderStyle.applyTo( Bin(
		        StyleSheet.instance.withAttr( Primitive.rowSpacing, 20.0 ).applyTo( Row( [
		                Row( [ Label( 'Location: ' ) ] ).alignHPack(),
		                locationEditor.alignHExpand(),
		                Row( [ styleOptionMenu, deleteButton.alignVCentre() ] ).alignHRight() ] ) ).pad( 2.0, 2.0 ) ) )
		
		boxContents = [ headerBox ]
		boxContents.append( _quoteLocationBorderStyle.applyTo( Border( targetView ) ) )
		box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( boxContents ) )
		
		p = _quoteLocationEditorBorderStyle.applyTo( Border( box ).alignHExpand() )

		p = WorksheetRichTextEditor.instance.editableParagraphEmbed( node, p )
		return p




_view = WorksheetEditor()
perspective2 = SequentialEditorPerspective( _view.fragmentViewFunction, WorksheetRichTextEditor.instance )


class WorksheetEditorSubject (Subject):
	def __init__(self, document, model, enclosingSubject, location, title):
		super( WorksheetEditorSubject, self ).__init__( enclosingSubject )
		self._document = document
		self._model = model
		# Defer the creation of the model view - it involves executing all the code in the worksheet which can take some time
		self._modelView = None
		self._enclosingSubject = enclosingSubject
		self._location = location
		self._title = title


	def _getModelView(self):
		if self._modelView is None:
			self._modelView = EditorSchema.WorksheetEditor( None, self._model )
		return self._modelView
		
	
	def getFocus(self):
		return self._getModelView()
	
	def getPerspective(self):
		return perspective2
	
	def getTitle(self):
		return self._title + ' [WsEdit2]'
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( location=self._location )
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()

	