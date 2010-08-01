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

#from GSymCore.Languages.Python25.PythonEditor.PythonEditorStyleSheet import PythonEditorStyleSheet
#from GSymCore.Languages.Python25.Execution.ExecutionStyleSheet import ExecutionStyleSheet



class WorksheetViewerStyleSheet (StyleSheet):
	def __init__(self):
		super( WorksheetViewerStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'richTextStyle', RichTextStyleSheet.instance.withNonEditable() )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'contextMenuStyle', ContextMenuStyleSheet.instance )
		self.initAttr( 'executionStyle', ExecutionStyleSheet.instance )
		self.initAttr( 'pythonStyle', PythonEditorStyleSheet.instance )
		self.initAttr( 'staticPythonStyle', PythonEditorStyleSheet.instance.staticStyle() )
		
		self.initAttr( 'pythonCodeBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) ) )
		self.initAttr( 'pythonCodeEditorBorderAttrs', AttributeValues( border=SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) ) )

		
		
	def newInstance(self):
		return WorksheetStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withRichTextStyleSheet(self, richTextStyle):
		return self.withAttrs( richTextStyle=richTextStyle )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withContextMenuStyleSheet(self, contextMenuStyle):
		return self.withAttrs( contextMenuStyle=contextMenuStyle )
	
	def withExecutionStyleSheet(self, executionStyle):
		return self.withAttrs( executionStyle=executionStyle )
	
	def withPythonStyle(self, pythonStyle):
		return self.withAttrs( pythonStyle=pythonStyle )
	
	def withStaticPythonStyle(self, staticPythonStyle):
		return self.withAttrs( staticPythonStyle=staticPythonStyle )
	
	
	def withPythonCodeBorderAttrs(self, pythonCodeBorderAttrs):
		return self.withAttrs( pythonCodeBorderAttrs=pythonCodeBorderAttrs )
	
	def withPythonCodeEditorBorderAttrs(self, pythonCodeEditorBorderAttrs):
		return self.withAttrs( pythonCodeEditorBorderAttrs=pythonCodeEditorBorderAttrs )
	
	
	@DerivedAttributeMethod
	def pythonCodeBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonCodeBorderAttrs'] )
		
	@DerivedAttributeMethod
	def pythonCodeEditorBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonCodeEditorBorderAttrs'] )
		
	
	
	
	def worksheet(self, body, editLocation):
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
		controlsStyle = self['controlsStyle']

		
		homeLink = controlsStyle.link( 'HOME PAGE', Location( '' ) ).getElement()
		editLink = controlsStyle.link( 'Edit this worksheet', editLocation ).getElement()
		linkHeader = richTextStyle.splitLinkHeaderBar( [ editLink ], [ homeLink ] )
		
		return richTextStyle.page( [ linkHeader, body ] )
	
	
	def body(self, contents):
		richTextStyle = self['richTextStyle']
		return richTextStyle.body( contents )
	
	
	def paragraph(self, text):
		return self['richTextStyle'].paragraph( text )
	
	def h1(self, text):
		return self['richTextStyle'].h1( text )
	
	def h2(self, text):
		return self['richTextStyle'].h2( text )
	
	def h3(self, text):
		return self['richTextStyle'].h3( text )
	
	def h4(self, text):
		return self['richTextStyle'].h4( text )
	
	def h5(self, text):
		return self['richTextStyle'].h5( text )
	
	def h6(self, text):
		return self['richTextStyle'].h6( text )

	def title(self, text):
		return self['richTextStyle'].titleBar( text )
	
	
	def pythonCode(self, codeView, resultView, bShowCode, bShowResult):
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		pythonCodeBorderStyle = self.pythonCodeBorderStyle()
		pythonCodeEditorBorderStyle = self.pythonCodeEditorBorderStyle()
		
		boxContents = []
		if bShowCode:
			boxContents.append( pythonCodeBorderStyle.border( codeView.alignHExpand() ).alignHExpand() )
		if bShowResult  and  resultView is not None:
			boxContents.append( resultView.alignHExpand() )
		box = primitiveStyle.withVBoxSpacing( 5.0 ).vbox( boxContents )
		
		return pythonCodeEditorBorderStyle.border( box.alignHExpand() ).alignHExpand()
	
	def minimalPythonCodeResult(self, resultView):
		if resultView is not None:
			return resultView.alignHExpand()
		else:
			return PrimitiveStyleSheet.instance.hiddenContent( '' )
	
	

#WorksheetViewerStyleSheet.instance = WorksheetViewerStyleSheet()
	
	
	