##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color, BasicStroke

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location

from Britefury.AttributeTableUtils.DerivedAttributeMethod import DerivedAttributeMethod

from GSymCore.Languages.Python25.Execution.ExecutionStyleSheet import ExecutionStyleSheet

from GSymCore.Worksheet.ViewSchema import PythonCodeView



class WorksheetEditorStyleSheet (StyleSheet):
	def __init__(self):
		super( WorksheetEditorStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'richTextStyle', RichTextStyleSheet.instance )
		self.initAttr( 'editableRichTextStyle', RichTextStyleSheet.instance.withEditable().withAppendNewlineToParagraphs( True ) )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'contextMenuStyle', ContextMenuStyleSheet.instance )
		self.initAttr( 'executionStyle', ExecutionStyleSheet.instance )
		
		self.initAttr( 'pythonCodeHeaderBackground', FillPainter( Color( 0.75, 0.8, 0.925 ) ) )
		self.initAttr( 'pythonCodeBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
		self.initAttr( 'pythonCodeEditorBorderAttrs', AttributeValues( border=SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) ) )

		
		
	def newInstance(self):
		return WorksheetStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withRichTextStyleSheet(self, richTextStyle):
		return self.withAttrs( richTextStyle=richTextStyle, editableRichTextStyle=richTextStyle.withEditable() )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withContextMenuStyleSheet(self, contextMenuStyle):
		return self.withAttrs( contextMenuStyle=contextMenuStyle )
	
	def withExecutionStyleSheet(self, executionStyle):
		return self.withAttrs( executionStyle=executionStyle )
	
	
	def withPythonCodeHeaderBackground(self, pythonCodeHeaderBackground):
		return self.withAttrs( pythonCodeHeaderBackground=pythonCodeHeaderBackground )
	
	def withPythonCodeBorderAttrs(self, pythonCodeBorderAttrs):
		return self.withAttrs( pythonCodeBorderAttrs=pythonCodeBorderAttrs )
	
	def withPythonCodeEditorBorderAttrs(self, pythonCodeEditorBorderAttrs):
		return self.withAttrs( pythonCodeEditorBorderAttrs=pythonCodeEditorBorderAttrs )
	
	
	@DerivedAttributeMethod
	def pythonCodeHeaderStyle(self):
		return self['primitiveStyle'].withAttrs( background=self['pythonCodeHeaderBackground'] )
		
	@DerivedAttributeMethod
	def pythonCodeBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonCodeBorderAttrs'] )
		
	@DerivedAttributeMethod
	def pythonCodeEditorBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonCodeEditorBorderAttrs'] )
		
	
	
	
	def worksheetTitle(self, title):
		editableRichTextStyle = self['editableRichTextStyle']
		
		return editableRichTextStyle.titleBar( title )
	
	
	def worksheet(self, titleView, body):
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
		editableRichTextStyle = self['editableRichTextStyle']
		controlsStyle = self['controlsStyle']

		homeLink = controlsStyle.link( 'HOME PAGE', Location( '' ) ).getElement()
		linkHeader = richTextStyle.linkHeaderBar( [ homeLink ] )
		
		return richTextStyle.page( [ linkHeader, titleView, body ] )
	
	
	def body(self, contents):
		richTextStyle = self['richTextStyle']
		return richTextStyle.body( contents )
	
	
	def paragraph(self, text):
		return self['editableRichTextStyle'].paragraph( text )
	
	def h1(self, text):
		return self['editableRichTextStyle'].h1( text )
	
	def h2(self, text):
		return self['editableRichTextStyle'].h2( text )
	
	def h3(self, text):
		return self['editableRichTextStyle'].h3( text )
	
	def h4(self, text):
		return self['editableRichTextStyle'].h4( text )
	
	def h5(self, text):
		return self['editableRichTextStyle'].h5( text )
	
	def h6(self, text):
		return self['editableRichTextStyle'].h6( text )

	
	def pythonCode(self, codeView, resultView, style, bShowResult, onSetStyle, onDelete):
		def _onStyleOptionMenu(optionMenu, prevChoice, choice):
			style = choiceValues[choice]
			onSetStyle( style )
			
		def _onDeleteButton(button, event):
			onDelete()
		
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		controlsPrimStyle = controlsStyle['primitiveStyle']
		pythonCodeHeaderStyle = self.pythonCodeHeaderStyle()
		pythonCodeBorderStyle = self.pythonCodeBorderStyle()
		pythonCodeEditorBorderStyle = self.pythonCodeEditorBorderStyle()
		
		optionTexts = [ 'Minimal result', 'Result', 'Code with result', 'Code', 'Editable code with result', 'Editable code', 'Hidden' ]
		optionChoices = [ controlsPrimStyle.staticText( text )   for text in optionTexts ]
		menuChoices = [ controlsPrimStyle.staticText( text )   for text in optionTexts ]
		choiceValues = [
		        PythonCodeView.STYLE_MINIMAL_RESULT,
		        PythonCodeView.STYLE_RESULT,
		        PythonCodeView.STYLE_CODE_AND_RESULT,
		        PythonCodeView.STYLE_CODE,
		        PythonCodeView.STYLE_EDITABLE_CODE_AND_RESULT,
		        PythonCodeView.STYLE_EDITABLE_CODE,
		        PythonCodeView.STYLE_HIDDEN ]
		styleOptionMenu = controlsStyle.optionMenu( optionChoices, menuChoices, choiceValues.index( style ), _onStyleOptionMenu )
		
		deleteButton = controlsStyle.button( primitiveStyle.systemIcon( 'delete' ), _onDeleteButton )
		
		headerBox = pythonCodeHeaderStyle.bin(
		        primitiveStyle.withHBoxSpacing( 20.0 ).hbox( [ primitiveStyle.staticText( 'Python code' ).alignHExpand(), styleOptionMenu.getElement(), deleteButton.getElement() ] ).alignHExpand().pad( 2.0, 2.0 ) )
		
		boxContents = [ headerBox.alignHExpand() ]
		boxContents.append( pythonCodeBorderStyle.border( codeView.alignHExpand() ).alignHExpand() )
		if resultView is not None:
			boxContents.append( resultView.alignHExpand() )
		box = primitiveStyle.withVBoxSpacing( 5.0 ).vbox( boxContents )
		
		return pythonCodeEditorBorderStyle.border( box.alignHExpand() ).alignHExpand()
	

WorksheetEditorStyleSheet.instance = WorksheetEditorStyleSheet()
	
	
	