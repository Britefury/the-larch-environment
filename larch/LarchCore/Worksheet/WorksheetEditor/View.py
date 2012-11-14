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
import java.util.List

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.AttributeTable import *

from BritefuryJ.Projection import Perspective, Subject
from BritefuryJ.Pres import InnerFragment, LocationAsInnerFragment


from BritefuryJ.Controls import *
from BritefuryJ.LSpace import *
from BritefuryJ.Graphics import *
from BritefuryJ.LSpace.Input import ObjectDndHandler, Modifier
from BritefuryJ.Browser import Location
from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace.Marker import Marker
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.ContextMenu import *
from BritefuryJ.Pres.ObjectPres import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.Pres.Help import *

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.IncrementalView import FragmentData

from BritefuryJ.LSpace.Interactor import KeyElementInteractor

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.Item import *

from LarchCore.Languages.Python2 import Python2

from LarchCore.MainApp import AppLocationPath

from LarchCore.Worksheet.WorksheetCommands import worksheetCommands
from LarchCore.Worksheet.WorksheetEditor import EditorSchema
from LarchCore.Worksheet.WorksheetEditor.RichTextController import WorksheetRichTextController



_editableStyle = StyleSheet.style( Primitive.editable( True ) )

_pythonCodeBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
_pythonCodeBox = BorderWithHeaderBar( SolidBorder( 1.5, 4.0, 10.0, 10.0, Color( 0.4, 0.4, 0.5 ), None ), Color( 0.825, 0.825, 0.875 ) )

_worksheetMargin = 10.0



def _highlightDrop_embeddedObject(element, graphics, pos, action):
	marker = Marker.atPointIn( element, pos, True )
	if marker is not None  and  marker.isValid():
		ObjectDndHandler.drawCaretDndHighlight( graphics, element, marker )

def _onDrop_embeddedObject(element, pos, data, action):
	marker = Marker.atPointIn( element, pos, True )
	if marker is not None  and  marker.isValid():
		# Display a context menu
		def _onDropInline(control):
			def _makeInline():
				model = data.getModel()
				return EditorSchema.InlineEmbeddedObjectEditor.newInlineEmbeddedObject( model )
			WorksheetRichTextController.instance.insertInlineEmbedAtMarker( marker, _makeInline )

		def _onDropParagraph(control):
			def _makeParagraph():
				model = data.getModel()
				return EditorSchema.ParagraphEmbeddedObjectEditor.newParagraphEmbeddedObject( model )
			WorksheetRichTextController.instance.insertParagraphAtMarker( marker, _makeParagraph )

		menu = VPopupMenu( [ MenuItem.menuItemWithLabel( 'Inline', _onDropInline ),
		                     MenuItem.menuItemWithLabel( 'As paragraph', _onDropParagraph ) ] )
		menu.popupAtMousePosition( marker.getElement() )
	return True


_embeddedObject_dropDest = ObjectDndHandler.DropDest( FragmentData, None, _highlightDrop_embeddedObject, _onDrop_embeddedObject )




def _applyParagraphShortcuts(p):
	def elementAction(modelAction):
		def _elemAction(element):
			model = element.fragmentContext.model
			return modelAction( model )
		return _elemAction

	def paraStyleAction(style):
		def _paraStyle(model):
			model.setStyle( style )
		return elementAction( _paraStyle )

	def insertCodeAction(element):
		marker = Marker.atEndOf( element, True )
		def _makeParagraph():
			return EditorSchema.PythonCodeEditor.newPythonCodeModel()
		WorksheetRichTextController.instance.insertParagraphAtMarker( marker, _makeParagraph )


	p = p.withShortcut( Shortcut( KeyEvent.VK_N, Modifier.ALT ), paraStyleAction( 'normal' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_1, Modifier.ALT ), paraStyleAction( 'h1' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_2, Modifier.ALT ), paraStyleAction( 'h2' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_3, Modifier.ALT ), paraStyleAction( 'h3' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_4, Modifier.ALT ), paraStyleAction( 'h4' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_5, Modifier.ALT ), paraStyleAction( 'h5' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_6, Modifier.ALT ), paraStyleAction( 'h6' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_T, Modifier.ALT ), paraStyleAction( 'title' ) )
	p = p.withShortcut( Shortcut( KeyEvent.VK_C, Modifier.ALT ), insertCodeAction )
	return p



_italicButtonLabelStyle = StyleSheet.style( Primitive.fontFace( 'Monospaced' ), Primitive.fontItalic( True ) )
_boldButtonLabelStyle = StyleSheet.style( Primitive.fontFace( 'Monospaced' ), Primitive.fontBold( True ) )
_underlineButtonLabelStyle = StyleSheet.style( Primitive.fontFace( 'Monospaced' ), Primitive.fontUnderline( True ) )
_strikethroughButtonLabelStyle = StyleSheet.style( Primitive.fontFace( 'Monospaced' ), Primitive.fontStrikethrough( True ) )



def _worksheetContextMenuFactory(element, menu):
	region = element.getRegion()
	rootElement = element.getRootElement()

	
	def makeParaStyleFn(style):
		def _modifyParagraph(paragraph):
			paragraph.setStyle( style )

		def _onLink(link, event):
			caret = rootElement.getCaret()
			if caret.isValid():
				WorksheetRichTextController.instance.modifyParagraphAtMarker( caret.getMarker(), _modifyParagraph )
		return _onLink
	
	normalParaStyle = Hyperlink( 'Normal', makeParaStyleFn( 'normal' ) )
	h1ParaStyle = Hyperlink( 'H1', makeParaStyleFn( 'h1' ) )
	h2ParaStyle = Hyperlink( 'H2', makeParaStyleFn( 'h2' ) )
	h3ParaStyle = Hyperlink( 'H3', makeParaStyleFn( 'h3' ) )
	h4ParaStyle = Hyperlink( 'H4', makeParaStyleFn( 'h4' ) )
	h5ParaStyle = Hyperlink( 'H5', makeParaStyleFn( 'h5' ) )
	h6ParaStyle = Hyperlink( 'H6', makeParaStyleFn( 'h6' ) )
	titleParaStyle = Hyperlink( 'Title', makeParaStyleFn( 'title' ) )
	paraStyles = ControlsRow( [ normalParaStyle, h1ParaStyle, h2ParaStyle, h3ParaStyle, h4ParaStyle, h5ParaStyle, h6ParaStyle, titleParaStyle ] )
	menu.add( Section( SectionHeading2( 'Paragraph style' ), paraStyles ).alignHExpand() )
	
	
	def makeToggleStyleFn(attrName):
		def computeStyleValues(styleAttrDicts):
			value = bool( dict( styleAttrDicts[0] ).get( attrName, None ) )
			value = not value
			attrs = {}
			attrs[attrName] = '1'   if value   else None
			return attrs

		def onButton(button, event):
			selection = rootElement.getSelection()
			if isinstance( selection, TextSelection ):
				if selection.getRegion() == region:
					WorksheetRichTextController.instance.applyStyleToSelection( selection, computeStyleValues )


		return onButton

	
	italicStyle = Button( _italicButtonLabelStyle( SpaceBin( 16.0, 16.0, Label( 'I' ).alignHCentre().alignVCentre() ) ), makeToggleStyleFn( 'italic' ) )
	boldStyle = Button( _boldButtonLabelStyle( SpaceBin( 16.0, 16.0, Label( 'B' ).alignHCentre().alignVCentre() ) ), makeToggleStyleFn( 'bold' ) )
	underlineStyle = Button( _underlineButtonLabelStyle( SpaceBin( 16.0, 16.0, Label( 'U' ).alignHCentre().alignVCentre() ) ), makeToggleStyleFn( 'underline' ) )
	strikethroughStyle = Button( _strikethroughButtonLabelStyle( SpaceBin( 16.0, 16.0, Label( 'abc' ).alignHCentre().alignVCentre() ) ), makeToggleStyleFn( 'strikethrough' ) )
	styles = ControlsRow( [ italicStyle, boldStyle, underlineStyle, strikethroughStyle ] ).alignHPack()
	menu.add( Section( SectionHeading2( 'Text style' ), styles ).alignHExpand() )


	def _onLink(link, event):
		def _makeLink():
			return EditorSchema.LinkEditor.newHomeLink( 'Link' )

		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextController.instance.insertInlineEmbedAtCaret( caret, _makeLink )


	insertLink = Hyperlink( 'Hyperlink', _onLink )
	insert = ControlsRow( [ insertLink ] )
	menu.add( Section( SectionHeading2( 'Insert' ), insert ).alignHExpand() )



	def _onPythonCode(link, event):
		def _makePythonCode():
			return EditorSchema.PythonCodeEditor.newPythonCode()
		
		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextController.instance.insertParagraphAtCaret( caret, _makePythonCode )

	newCode = Hyperlink( 'Python code', _onPythonCode )
	codeControls = ControlsRow( [ newCode ] )
	menu.add( Section( SectionHeading2( 'Code' ), codeControls ).alignHExpand() )
	
	
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsRow( [ refreshButton.alignHPack() ] )
	menu.add( Section( SectionHeading2( 'Worksheet' ), worksheetControls ).alignHExpand() )
	return True



def _inlineEmbeddedObjectContextMenuFactory(element, menu):
	def _onDelete(control):
		WorksheetRichTextController.instance.deleteInlineEmbedContainingElement( element )

	deleteItem = MenuItem.menuItemWithLabel( 'Delete embedded object', _onDelete )

	menu.add( deleteItem )
	return True


def _paragraphEmbeddedObjectContextMenuFactory(element, menu):
	def _onDelete(control):
		WorksheetRichTextController.instance.deleteParagraphContainingElement( element )

	deleteItem = MenuItem.menuItemWithLabel( 'Delete embedded object paragraph', _onDelete )

	menu.add( deleteItem )
	return True



	

class WorksheetEditor (MethodDispatchView):
	@ObjectDispatchMethod( EditorSchema.WorksheetEditor )
	def Worksheet(self, fragment, inheritedState, node):
		bodyView = InnerFragment( node.getBody() )
		
		viewLocation = fragment.getSubjectContext()['viewLocation']
		
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		viewLink = Hyperlink( 'Switch to user mode', viewLocation )
		linkHeader = AppLocationPath.appLinkheaderBar( fragment.getSubjectContext(), [ viewLink ] )


		tip = TipBox( 'Type to add text to the worksheet.\nRight click to access the context menu, from which styles can be applied.\n' + \
			      'Code can also be added from the context menu. To re-execute all code within the worksheet, press Control-Enter',
			      'larchcore.worksheet.edit.howto' )


		w = Page( [ linkHeader, bodyView, tip ] )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		w = w.withDropDest( _embeddedObject_dropDest )
		w = w.withCommands( worksheetCommands )
		w = WorksheetRichTextController.instance.region( w )
		return w
	
	
	@ObjectDispatchMethod( EditorSchema.BodyEditor )
	def Body(self, fragment, inheritedState, node):
		contentViews = list( InnerFragment.map( node.getContents() ) )

		b = Body( contentViews ).padX( _worksheetMargin )
		b = WorksheetRichTextController.instance.editableBlock( node, b )
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
		else:
			p = NormalText( text )
		p = WorksheetRichTextController.instance.editableParagraph( node, p )
		p = _applyParagraphShortcuts( p )
		return p
	
	
	@ObjectDispatchMethod( EditorSchema.TextSpanEditor )
	def TextSpan(self, fragment, inheritedState, node):
		text = node.getText()
		styleSheet = node.getStyleSheet()
		p = styleSheet.applyTo( RichSpan( text ) )
		p = WorksheetRichTextController.instance.editableSpan( node, p )
		return p


	@ObjectDispatchMethod( EditorSchema.LinkEditor )
	def Link(self, fragment, inheritedState, node):
		docLocation = fragment.getSubjectContext()['docLocation']

		def _linkContextMenuFactory(element, menu):
			def _onRemove(control, event):
				WorksheetRichTextController.instance.deleteInlineEmbedContainingElement( element )


			class _TextListener (TextEntry.TextEntryListener):
				def onAccept(self, textEntry, text):
					node.text = text


			class _LocationListener (TextEntry.TextEntryListener):
				def onAccept(self, textEntry, text):
					node.setAbsoluteLocation( docLocation, Location( text ) )


			textEntry = TextEntry( node.text, _TextListener() )

			absLoc = node.getAbsoluteLocation( docLocation )

			locationEntry = TextEntry( absLoc.getLocationString(), _LocationListener() )

			link = Hyperlink( 'Go to location...', absLoc )

			onRemove = Button.buttonWithLabel( 'Remove', _onRemove )

			menu.add( Section( SectionHeading3( 'Text:' ), textEntry ) )
			menu.add( Section( SectionHeading3( 'Location:' ), Column( [ locationEntry, link ] ) ) )
			menu.add( Spacer( 0.0, 20.0 ) )
			menu.add( onRemove )
			return True


		p = _linkStyle.applyTo( ApplyStyleSheetFromAttribute( Controls.hyperlinkAttrs, Label( node.text ) ) )
		p = p.withContextMenuInteractor( _linkContextMenuFactory )
		p = WorksheetRichTextController.instance.editableInlineEmbed( node, p )
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
		else:
			p = NormalText( '' )
		p = WorksheetRichTextController.instance.editableParagraph( node, p )
		p = _applyParagraphShortcuts( p )
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
			WorksheetRichTextController.instance.deleteParagraphContainingElement( button.getElement() )

		codeView = Python2.python2EditorPerspective.applyTo( InnerFragment( node.getCode() ) )
		
		executionResultView = None
		executionResult = node.getResult()
		if executionResult is not None:
			if not node.isResultVisible():
				executionResult = executionResult.suppressStdOut().suppressResult()
			executionResultView = executionResult.view()
			
			
		optionTexts = [ 'Minimal result', 'Result', 'Code with result', 'Code', 'Editable code with result', 'Editable code', 'Hidden' ]
		optionChoices = [ StaticText( text )   for text in optionTexts ]
		styleOptionMenu = OptionMenu( optionChoices, choiceValues.index( node.getStyle() ), _onStyleOptionMenu )
		
		deleteButton = Button( Image.systemIcon( 'delete' ), _onDeleteButton )
		
		headerBox = Bin(
			StyleSheet.style( Primitive.rowSpacing( 20.0 ) ).applyTo( Row( [
				Row( [ Label( 'Python code' ) ] ).alignHLeft(),
				Row( [ styleOptionMenu, deleteButton.alignVCentre() ] ).alignHRight() ] ) ) )


		boxContents = [ _pythonCodeBorderStyle.applyTo( Border( codeView ) ) ]
		if executionResultView is not None:
			boxContents.append( executionResultView.alignHExpand() )
		box = StyleSheet.style( Primitive.columnSpacing( 5.0 ) ).applyTo( Column( boxContents ) )

		p = _pythonCodeBox.surround( headerBox.padY( 0.0, 3.0 ), box.padY( 5.0, 0.0 ) )

		p = WorksheetRichTextController.instance.editableParagraphEmbed( node, p )
		return p.alignHExpand()


	@ObjectDispatchMethod( EditorSchema.InlineEmbeddedObjectEditor )
	def InlineEmbeddedObject(self, fragment, inheritedState, node):
		value = node.value
		valueView = _editableStyle.applyTo( ApplyPerspective( EditPerspective.instance, value ) )

		hideFrame = getattr( value, '__embed_hide_frame__', False )
		p = ObjectBorder( valueView )   if not hideFrame   else valueView

		p = p.withContextMenuInteractor( _inlineEmbeddedObjectContextMenuFactory )
		p = WorksheetRichTextController.instance.editableInlineEmbed( node, p )
		return p



	@ObjectDispatchMethod( EditorSchema.ParagraphEmbeddedObjectEditor )
	def ParagraphEmbeddedObject(self, fragment, inheritedState, node):
		value = node.value
		valueView = _editableStyle.applyTo( ApplyPerspective( EditPerspective.instance, value ) )

		hideFrame = getattr( value, '__embed_hide_frame__', False )
		p = ObjectBorder( valueView )   if not hideFrame   else valueView

		p = p.withContextMenuInteractor( _paragraphEmbeddedObjectContextMenuFactory )
		p = WorksheetRichTextController.instance.editableParagraphEmbed( node, p )
		return p



_linkStyle = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.9, 0.9, 0.9 ), Color( 0.5, 0.5, 0.5 ) ) ), Primitive.hoverForeground( None ), Primitive.cursor( None ) )



def _refreshWorksheet(subject, pageController):
	subject._modelView.refreshResults()


_refreshCommand = Command( CommandName( '&Refresh worksheet' ), _refreshWorksheet, Shortcut( KeyEvent.VK_ENTER, Modifier.CTRL ) )
_worksheetEditorCommands = CommandSet( 'LarchCore.Worksheet.Editor', [ _refreshCommand ] )


_view = WorksheetEditor()
perspective2 = SequentialEditorPerspective( _view.fragmentViewFunction, WorksheetRichTextController.instance )


class WorksheetEditorSubject (Subject):
	def __init__(self, document, model, enclosingSubject, location, importName, title):
		super( WorksheetEditorSubject, self ).__init__( enclosingSubject )
		assert isinstance( location, Location )
		self._document = document
		self._model = model
		# Defer the creation of the model view - it involves executing all the code in the worksheet which can take some time
		self._modelView = None
		self._location = location
		self._importName = importName
		self._title = title


	def _getModelView(self):
		if self._modelView is None:
			self._modelView = EditorSchema.WorksheetEditor( None, self._model, self._importName )
		return self._modelView
		
	
	def getFocus(self):
		return self._getModelView()
	
	def getPerspective(self):
		return perspective2
	
	def getTitle(self):
		return self._title + ' [Ws-Devel]'
	
	def getSubjectContext(self):
		return self.enclosingSubject.getSubjectContext().withAttrs( location=self._location )
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()

	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _worksheetEditorCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )

	