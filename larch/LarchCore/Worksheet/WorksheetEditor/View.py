##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color
from java.awt.event import KeyEvent

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Kernel.Document import LinkSubjectDrag
from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.Projection import Perspective, Subject

from BritefuryJ.Live import LiveFunction


from BritefuryJ.Controls import Controls, MenuItem, VPopupMenu, Hyperlink, Button, TextEntry, OptionMenu
from BritefuryJ.Graphics import SolidBorder, BorderWithHeaderBar, FilledOutlinePainter
from BritefuryJ.LSpace.Input import ObjectDndHandler, Modifier
from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace.Marker import Marker
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Pres import Pres, ApplyStyleSheetFromAttribute, ApplyPerspective
from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Spacer, Image, Bin, Border, SpaceBin, Row, Column
from BritefuryJ.Pres.RichText import TitleBar, Heading1, Heading2, Heading3, Heading4, Heading4, Heading5, Heading6, NormalText, RichSpan, Page, Body, LinkHeaderBar, StrongSpan, EmphSpan
from BritefuryJ.Pres.ObjectPres import ObjectBorder
from BritefuryJ.Pres.UI import Form, Section, SectionHeading2, SectionHeading3, ControlsRow
from BritefuryJ.Pres.Help import TipBox

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.IncrementalView import FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.RichText.Attrs import RichTextAttributes

from LarchCore.Languages.Python2 import Python2

from LarchCore.Worksheet.WorksheetCommands import worksheetCommands
from LarchCore.Worksheet.WorksheetEditor import EditorSchema
from LarchCore.Worksheet.WorksheetEditor.RichTextController import WorksheetRichTextController



_editableStyle = StyleSheet.style( Primitive.editable( True ) )

_pythonCodeBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
_pythonCodeBox = BorderWithHeaderBar( SolidBorder( 1.5, 4.0, 10.0, 10.0, Color( 0.4, 0.4, 0.5 ), None ), Color( 0.825, 0.825, 0.875 ) )
_inlinePythonCodeBorder = SolidBorder( 1.5, 4.0, 10.0, 10.0, Color( 0.4, 0.4, 0.5 ), None )
_linkEditorTargetBorder = SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.5, 0.75, 1.0 ), Color( 0.9, 0.95, 1.0 ) )

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
		menu.popupMenuAtMousePosition( marker.getElement() )
	return True


_embeddedObject_dropDest = ObjectDndHandler.DropDest( FragmentData, None, _highlightDrop_embeddedObject, _onDrop_embeddedObject )



def _canDrop_link(element, pos, data, action):
	subject = data.subject
	docSubject = element.fragmentContext.subject.documentSubject
	return subject.path().isWithin( docSubject.path() )


def _onDrop_link(element, pos, data, action):
	marker = Marker.atPointIn( element, pos, True )
	if marker is not None  and  marker.isValid():
		def _makeInline():
			subject = data.subject
			docSubject = element.fragmentContext.subject.documentSubject
			return EditorSchema.LinkEditor.newLink( docSubject, subject.getTitle(), subject )
		WorksheetRichTextController.instance.insertInlineEmbedAtMarker( marker, _makeInline )
	return True


_link_dropDest = ObjectDndHandler.DropDest( LinkSubjectDrag, _canDrop_link, _highlightDrop_embeddedObject, _onDrop_link )



class _LinkEditorModelPropertyKey (object):
	pass

_LinkEditorModelPropertyKey.instance = _LinkEditorModelPropertyKey()


def _onDrop_link_onto_linkEditor(element, pos, data, action):
	prop = element.getProperty( _LinkEditorModelPropertyKey.instance )
	node = prop.value   if prop is not None   else None
	if node is not None:
		subject = data.subject
		docSubject = element.fragmentContext.subject.documentSubject
		node.setSubject( docSubject, subject )
		return True
	else:
		return False

_linkEditor_dropDest = ObjectDndHandler.DropDest( LinkSubjectDrag, _canDrop_link, None, _onDrop_link_onto_linkEditor )




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



def _applyBlankParagraphShortcuts(p):
	def insertCodeAction(element):
		marker = Marker.atEndOf( element, True )
		def _makeParagraph():
			return EditorSchema.PythonCodeEditor.newPythonCodeModel()
		WorksheetRichTextController.instance.insertParagraphAtMarker( marker, _makeParagraph )


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
	paraStyles = ControlsRow( [ normalParaStyle, h1ParaStyle, h2ParaStyle, h3ParaStyle, h4ParaStyle, h5ParaStyle, h6ParaStyle, titleParaStyle ] ).alignHPack()
	menu.add( Section( SectionHeading2( 'Paragraph style' ), paraStyles ).alignHExpand() )
	
	
	def makeToggleStyleFn(attrName):
		def computeStyleValues(listOfSpanAttrs):
			value = bool(listOfSpanAttrs[0].getValue(attrName, 0))
			value = not value
			attrs = RichTextAttributes()
			attrs.putOverride(attrName, '1'   if value   else None)
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


	def _onLink(button, event):
		def _makeLink():
			return EditorSchema.LinkEditor.newDocumentLink( 'Link' )

		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextController.instance.insertInlineEmbedAtCaret( caret, _makeLink )

	insertLink = Button.buttonWithLabel( 'Hyperlink', _onLink )
	insert = ControlsRow( [ insertLink ] ).alignHPack()
	menu.add( Section( SectionHeading2( 'Insert' ), insert ).alignHExpand() )



	def _onPythonBlock(link, event):
		def _makePythonCode():
			return EditorSchema.PythonCodeEditor.newPythonCode()
		
		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextController.instance.insertParagraphAtCaret( caret, _makePythonCode )

	def _onPythonExpression(link, event):
		def _makeInlinePythonCode():
			return EditorSchema.InlinePythonCodeEditor.newInlinePythonCode()

		caret = rootElement.getCaret()
		if caret.isValid():
			WorksheetRichTextController.instance.insertInlineEmbedAtCaret( caret, _makeInlinePythonCode )

	newCode = Button.buttonWithLabel( 'Block', _onPythonBlock )
	newInlineCode = Button.buttonWithLabel( 'Expression', _onPythonExpression )
	codeControls = ControlsRow( [ newCode, newInlineCode ] ).alignHPack()
	menu.add( Section( SectionHeading2( 'Python code' ), codeControls ).alignHExpand() )
	
	
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsRow( [ refreshButton ] ).alignHPack()
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
		try:
			viewSubject = fragment.subject.viewSubject
		except AttributeError:
			pageContents = []
		else:
			viewLink = Hyperlink( 'Switch to user mode', viewSubject )
			linkHeader = LinkHeaderBar( [ viewLink ] )
			pageContents = [ linkHeader ]


		tip = TipBox( [ NormalText( [ StrongSpan( 'Text: ' ), 'Type to add text to the worksheet.\nRight click to access the context menu, from which styles can be applied.' ] ),
			      NormalText( [ StrongSpan( 'Code: ' ), 'Code can be added from the context menu. You can add complete blocks of python code in between paragraphs, or single expressions to be evaluated with paragraph text.' ] ),
			      NormalText( [ 'To re-execute all code within the worksheet, press ', EmphSpan( 'Control-Enter' ) ] ) ],
			      'larchcore.worksheet.edit.howto' )


		w = Page( pageContents + [ node.getBody(), tip ] )
		w = w.withContextMenuInteractor( _worksheetContextMenuFactory )
		w = w.withDropDest( _embeddedObject_dropDest )
		w = w.withDropDest( _link_dropDest )
		w = w.withCommands( worksheetCommands )
		w = WorksheetRichTextController.instance.region( w )
		return w
	
	
	@ObjectDispatchMethod( EditorSchema.BodyEditor )
	def Body(self, fragment, inheritedState, node):
		nodeContents = node.getContents()
		contents = nodeContents   if len( nodeContents ) > 0   else [ EditorSchema.BlankParagraphEditor( node._worksheet, node ) ]
		b = Body( contents ).padX( _worksheetMargin )
		b = WorksheetRichTextController.instance.editableBlock( node, b )
		return b


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
		p = _applyBlankParagraphShortcuts( p )
		return p


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
		docSubject = fragment.subject.documentSubject

		def _linkContextMenuFactory(element, menu):
			def _onRemove(control, event):
				WorksheetRichTextController.instance.deleteInlineEmbedContainingElement( element )


			class _TextListener (TextEntry.TextEntryListener):
				def onAccept(self, textEntry, text):
					node.text = text


			class _TargetListener (TextEntry.TextEntryListener):
				def onAccept(self, textEntry, text):
					raise NotImplementedError


			textEntry = TextEntry( node.text, _TextListener() )

			@LiveFunction
			def targetHyperlink():
				subject = node.getSubject( docSubject )
				p = _linkEditorTargetBorder.surround( Hyperlink( subject.getTitle(), subject ) )
				p = p.withProperty( _LinkEditorModelPropertyKey.instance, node )
				p = p.withDropDest( _linkEditor_dropDest )
				return p

			onRemove = Button.buttonWithLabel( 'Remove', _onRemove )

			textSection = Form.Section( 'Text', 'Edit and press enter to change', textEntry )
			targetSection = Form.Section( 'Target', 'Drag links from a project page and drop to change', targetHyperlink )

			menu.add( Form( 'Hyperlink', [ textSection, targetSection ] ) )
			menu.add( Spacer( 0.0, 20.0 ) )
			menu.add( onRemove )
			return True


		p = _linkStyle.applyTo( ApplyStyleSheetFromAttribute( Controls.hyperlinkAttrs, Label( node.text ) ) )
		p = p.withContextMenuInteractor( _linkContextMenuFactory )
		p = WorksheetRichTextController.instance.editableInlineEmbed( node, p )
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
			EditorSchema.PythonCodeEditor.STYLE_ERRORS,
		        EditorSchema.PythonCodeEditor.STYLE_HIDDEN ]

		
		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			node.setStyle( style )
			
		def _onDeleteButton(button, event):
			WorksheetRichTextController.instance.deleteParagraphContainingElement( button.getElement() )

		codeView = Python2.python2EditorPerspective.applyTo( node.getCode() )
		
		executionResultView = None
		executionResult = node.getResult()
		if executionResult is not None:
			if not node.isResultVisible():
				executionResult = executionResult.suppressStdOut().suppressResult()
			executionResultView = executionResult.view()
			
			
		optionTexts = [ 'Minimal result', 'Result', 'Code with result', 'Code', 'Editable code with result', 'Editable code', 'Errors only', 'Hidden' ]
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


	@ObjectDispatchMethod( EditorSchema.InlinePythonCodeEditor )
	def InlinePythonCode(self, fragment, inheritedState, node):
		assert isinstance( node, EditorSchema.InlinePythonCodeEditor )
		choiceValues = [
			EditorSchema.InlinePythonCodeEditor.STYLE_MINIMAL_RESULT,
			EditorSchema.InlinePythonCodeEditor.STYLE_RESULT,
			EditorSchema.InlinePythonCodeEditor.STYLE_CODE_AND_RESULT,
			EditorSchema.InlinePythonCodeEditor.STYLE_EDITABLE_CODE_AND_RESULT ]


		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			node.setStyle( style )


		def _inlinePythonCodeContextMenuFactory(element, menu):
			def _onDelete(control):
				WorksheetRichTextController.instance.deleteInlineEmbedContainingElement( element )

			optionTexts = [ 'Minimal result', 'Result', 'Code with result', 'Editable code with result' ]
			optionChoices = [ StaticText( text )   for text in optionTexts ]
			styleOptionMenu = OptionMenu( optionChoices, choiceValues.index( node.getStyle() ), _onStyleOptionMenu )

			deleteItem = MenuItem.menuItemWithLabel( 'Delete', _onDelete )

			menu.add( deleteItem )
			menu.add( Row( [ Label( 'Style: ' ), styleOptionMenu ] ) )
			return True


		exprView = Python2.python2EditorPerspective.applyTo( node.getExpr() )

		executionResult = node.getResult()
		executionResultView = executionResult.view()   if executionResult is not None   else None

		boxContents = [ _pythonCodeBorderStyle.applyTo( Border( exprView ) ) ]
		if executionResultView is not None:
			boxContents.append( executionResultView )
		box = StyleSheet.style( Primitive.rowSpacing( 5.0 ) ).applyTo( Row( boxContents ) )

		p = _inlinePythonCodeBorder.surround( box )

		p = p.withContextMenuInteractor( _inlinePythonCodeContextMenuFactory )
		p = WorksheetRichTextController.instance.editableInlineEmbed( node, p )
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
	def __init__(self, document, model, enclosingSubject, path, importName, title):
		super( WorksheetEditorSubject, self ).__init__( enclosingSubject, path )
		self._document = document
		self._model = model
		# Defer the creation of the model view - it involves executing all the code in the worksheet which can take some time
		self.__modelView = None
		self._importName = importName
		self._title = title


	@property
	def _modelView(self):
		if self.__modelView is None:
			try:
				self.__modelView = EditorSchema.WorksheetEditor( None, self._model, self._importName )
			except Exception as e:
				print e
				raise
		return self.__modelView


	def getTrailLinkText(self):
		return 'Edit'


	def getFocus(self):
		f = self._modelView
		# This causes execution results to refresh on page view
		f.refreshResults()
		return f
	
	def getPerspective(self):
		return perspective2
	
	def getTitle(self):
		return self._title
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()

	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _worksheetEditorCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )

	