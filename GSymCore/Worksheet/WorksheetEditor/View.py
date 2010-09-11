##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
from datetime import datetime

from java.awt import Color
from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *
from Britefury.Util.InstanceCache import instanceCache

from BritefuryJ.AttributeTable import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.PresCom import InnerFragment, LocationAsInnerFragment


from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution.ExecutionPresCombinators import executionResultBox, minimalExecutionResultBox

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema

from GSymCore.Worksheet.WorksheetEditor.PythonCode import *
from GSymCore.Worksheet.WorksheetEditor.QuoteLocation import *

from GSymCore.Worksheet.WorksheetEditor.TextNodeEditor import *
from GSymCore.Worksheet.WorksheetEditor.BodyNodeEditor import *
from GSymCore.Worksheet.WorksheetEditor.WorksheetNodeEditor import *

from GSymCore.Worksheet.WorksheetEditor.SelectionEditor import *

from BritefuryJ.Controls import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Combinators import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Combinators.ContextMenu import *



_pythonCodeHeaderStyle = StyleSheet.instance.withAttr( Primitive.background, FillPainter( Color( 0.75, 0.8, 0.925 ) ) )
_pythonCodeBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_pythonCodeEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )

_quoteLocationHeaderStyle = StyleSheet.instance.withAttr( Primitive.background, FillPainter( Color( 0.75, 0.8, 0.925 ) ) )
_quoteLocationBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_quoteLocationEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )

_paragraphStyle = StyleSheet.instance.withAttr( RichText.appendNewlineToParagraphs, True )

		
		
def _worksheetContextMenuFactory(element, menu):
	rootElement = element.getRootElement()

	
	def makeStyleFn(style):
		def _onLink(link, event):
			caret = rootElement.getCaret()
			if caret.isValid():
				caret.getElement().postTreeEvent( PargraphRequest( style ) )
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
		caret = rootElement.getCaret()
		if caret.isValid():
			caret.getElement().postTreeEvent( PythonCodeRequest() )

	newCode = Hyperlink( 'Python code', _onPythonCode )
	codeControls = ControlsRow( [ newCode ] )
	menu.add( SectionColumn( [ SectionTitle( 'Code' ), codeControls ] ).alignHExpand() )
	
	
	def _onQuoteLocation(link, event):
		caret = rootElement.getCaret()
		if caret.isValid():
			caret.getElement().postTreeEvent( QuoteLocationRequest() )

	newQuoteLocation = Hyperlink( 'View of Location', _onQuoteLocation )
	quoteLocationControls = ControlsRow( [ newQuoteLocation ] )
	menu.add( SectionColumn( [ SectionTitle( 'Location' ), quoteLocationControls ] ).alignHExpand() )
	
	
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsRow( [ refreshButton ] )
	menu.add( SectionColumn( [ SectionTitle( 'Worksheet' ), worksheetControls ] ).alignHExpand() )
	return True



class WorksheetEditor (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, ctx, inheritedState, node):
		bodyView = InnerFragment( node.getBody() )
		
		viewLocation = ctx.getSubjectContext()['viewLocation']
		
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		viewLink = Hyperlink( 'View this worksheet', viewLocation )
		linkHeader = SplitLinkHeaderBar( [ viewLink ], [ homeLink ] )

		
		w = Page( [ linkHeader, bodyView ] )
		w = w.withTreeEventListener( WorksheetNodeEventListener.instance )
		w = w.withInteractor( WorksheetNodeInteractor.instance )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		return w
	
	
	@ObjectDispatchMethod( ViewSchema.BodyView )
	def Body(self, ctx, inheritedState, node):
		emptyLine = Paragraph( [ Text( '' ) ] )
		emptyLine = emptyLine.withTreeEventListener( EmptyEventListener.instance )
		contentViews = list( InnerFragment.map( node.getContents() ) )  +  [ emptyLine ]
		
		w = Body( contentViews )
		w = w.withTreeEventListener( BodyNodeEventListener.instance )
		return w
	
	
	@ObjectDispatchMethod( ViewSchema.ParagraphView )
	def Paragraph(self, ctx, inheritedState, node):
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
		p = _paragraphStyle.applyTo( p )
		p = withParagraphStreamValueFn( p, node.partialModel() )
		w = Span( [ p ] )
		w = w.withTreeEventListener( TextNodeEventListener.instance )
		w = w.withInteractor( TextNodeInteractor.instance )
		w = w.withFixedValue( node.getModel() )
		return w


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, ctx, inheritedState, node):
		choiceValues = [
		        ViewSchema.PythonCodeView.STYLE_MINIMAL_RESULT,
		        ViewSchema.PythonCodeView.STYLE_RESULT,
		        ViewSchema.PythonCodeView.STYLE_CODE_AND_RESULT,
		        ViewSchema.PythonCodeView.STYLE_CODE,
		        ViewSchema.PythonCodeView.STYLE_EDITABLE_CODE_AND_RESULT,
		        ViewSchema.PythonCodeView.STYLE_EDITABLE_CODE,
		        ViewSchema.PythonCodeView.STYLE_HIDDEN ]

		
		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			node.setStyle( style )
			
		def _onDeleteButton(button, event):
			button.getElement().postTreeEvent( DeleteNodeOperation( node ) )

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
		        StyleSheet.instance.withAttr( Primitive.rowSpacing, 20.0 ).applyTo( Row( [ StaticText( 'Python code' ).alignHExpand(), styleOptionMenu, deleteButton.alignVCentre() ] ) ).alignHExpand().pad( 2.0, 2.0 ) ) )
		
		boxContents = [ headerBox.alignHExpand() ]
		boxContents.append( _pythonCodeBorderStyle.applyTo( Border( codeView.alignHExpand() ).alignHExpand() ) )
		if executionResultView is not None:
			boxContents.append( executionResultView.alignHExpand() )
		box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( boxContents ) )
		
		p = _pythonCodeEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )

		
		p = p.withFixedValue( node.getModel() )
		p = p.withTreeEventListener( PythonCodeNodeEventListener.instance )
		return p


	
	@ObjectDispatchMethod( ViewSchema.QuoteLocationView )
	def QuoteLocation(self, ctx, inheritedState, node):
		choiceValues = [
		        ViewSchema.QuoteLocationView.STYLE_MINIMAL,
		        ViewSchema.QuoteLocationView.STYLE_NORMAL ]
		
		
		class _LocationEntryListener (TextEntry.TextEntryListener):
			def onAccept(self, entry, location):
				node.setLocation( location )

		
		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			node.setStyle( style )
			
		def _onDeleteButton(button, event):
			button.getElement().postTreeEvent( DeleteNodeOperation( node ) )

		targetView = StyleSheet.instance.withAttr( Primitive.editable, True ).applyTo( LocationAsInnerFragment( Location( node.getLocation() ) ) )
		
		optionTexts = [ 'Minimal', 'Normal' ]
		optionChoices = [ StaticText( text )   for text in optionTexts ]
		styleOptionMenu = OptionMenu( optionChoices, choiceValues.index( node.getStyle() ), _onStyleOptionMenu )
		
		deleteButton = Button( Image.systemIcon( 'delete' ), _onDeleteButton )
		
		locationEditor = TextEntry( node.getLocation(), _LocationEntryListener() )
		
		headerBox = _quoteLocationHeaderStyle.applyTo( Bin(
		        StyleSheet.instance.withAttr( Primitive.rowSpacing, 20.0 ).applyTo( Row( 
		                [ StaticText( 'Location: ' ), locationEditor, HiddenContent( '' ).alignHExpand(), styleOptionMenu, deleteButton.alignVCentre() ] ) ).alignHExpand().pad( 2.0, 2.0 ) ) )
		
		boxContents = [ headerBox.alignHExpand() ]
		boxContents.append( _quoteLocationBorderStyle.applyTo( Border( targetView.alignHExpand() ).alignHExpand() ) )
		box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( boxContents ) )
		
		p = _quoteLocationEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )

		
		p = p.withFixedValue( node.getModel() )
		p = p.withTreeEventListener( QuoteLocationNodeEventListener.instance )
		return p





perspective = GSymPerspective( WorksheetEditor(), WorksheetEditHandler() )


class WorksheetEditorSubject (GSymSubject):
	def __init__(self, document, model, enclosingSubject, location):
		self._document = document
		self._modelView = ViewSchema.WorksheetView( None, model )
		self._enclosingSubject = enclosingSubject
		self._location = location


	def getFocus(self):
		return self._modelView
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Worksheet [view]'
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( location=self._location )
	
	def getCommandHistory(self):
		return self._document.getCommandHistory()

	