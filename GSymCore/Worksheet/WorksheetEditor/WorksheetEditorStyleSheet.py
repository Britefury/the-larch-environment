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



class WorksheetEditorStyleSheet (StyleSheet):
	def __init__(self):
		super( WorksheetEditorStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'richTextStyle', RichTextStyleSheet.instance )
		self.initAttr( 'editableRichTextStyle', RichTextStyleSheet.instance.withEditable() )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
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
		
	
	
	
	def worksheet(self, title, contents):
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
		editableRichTextStyle = self['editableRichTextStyle']
		controlsStyle = self['controlsStyle']

		
		homeLink = controlsStyle.link( 'HOME PAGE', Location( '' ) ).getElement()
		linkHeader = richTextStyle.linkHeaderBar( [ homeLink ] )
		
		title = richTextStyle.titleBarWithSubtitle( title, 'Worksheet' )
		
		return richTextStyle.page( [ linkHeader, title ] + contents )
	
	
	def paragraph(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].paragraph( text ) )
	
	def h1(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h1( text ) )
	
	def h2(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h2( text ) )
	
	def h3(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h3( text ) )
	
	def h4(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h4( text ) )
	
	def h5(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h5( text ) )
	
	def h6(self, text):
		return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h6( text ) )

	
	def pythonCode(self, codeView, resultView, bShowCode, bCodeEditable, bShowResult, onShowCode, onCodeEditable, onShowResult):
		def _onShowCode(button, event):
			return onShowCode()
		
		def _onCodeEditable(button, event):
			return onCodeEditable()
		
		def _onShowResult(button, event):
			return onShowResult()
		
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		pythonCodeHeaderStyle = self.pythonCodeHeaderStyle()
		pythonCodeBorderStyle = self.pythonCodeBorderStyle()
		pythonCodeEditorBorderStyle = self.pythonCodeEditorBorderStyle()
		
		showCodeButton = controlsStyle.buttonWithLabel( 'Hide code'   if bShowCode   else   'Show code',   _onShowCode )
		codeEditableButton = controlsStyle.buttonWithLabel( 'Non-editable'   if bCodeEditable   else   'Editable',   _onCodeEditable )
		showResultButton = controlsStyle.buttonWithLabel( 'Hide result'   if bShowResult   else   'Show result',   _onShowResult )
		
		buttonsBox = primitiveStyle.withHBoxSpacing( 10.0 ).hbox( [ showCodeButton.getElement(), codeEditableButton.getElement(), showResultButton.getElement() ] )
		headerBox = pythonCodeHeaderStyle.bin( primitiveStyle.withHBoxSpacing( 20.0 ).hbox( [ primitiveStyle.instance.staticText( 'Python code' ).alignHExpand(), buttonsBox ] ).alignHExpand().pad( 2.0, 2.0 ) )
		
		boxContents = [ headerBox.alignHExpand() ]
		if bShowCode:
			boxContents.append( pythonCodeBorderStyle.border( codeView.alignHExpand() ).alignHExpand() )
		if bShowResult  and  resultView is not None:
			boxContents.append( resultView.alignHExpand() )
		box = primitiveStyle.withVBoxSpacing( 5.0 ).vbox( boxContents )
		
		return pythonCodeEditorBorderStyle.border( box.alignHExpand() ).alignHExpand()
	
	

WorksheetEditorStyleSheet.instance = WorksheetEditorStyleSheet()
	
	
	