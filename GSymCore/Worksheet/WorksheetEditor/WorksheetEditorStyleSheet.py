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



class WorksheetEditorStyleSheet (StyleSheet):
	def __init__(self):
		super( WorksheetEditorStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'richTextStyle', RichTextStyleSheet.instance )
		self.initAttr( 'editableRichTextStyle', RichTextStyleSheet.instance.withEditable() )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		
		self.initAttr( 'pythonCodeBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) ) )

		
		
	def newInstance(self):
		return WorksheetStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withRichTextStyleSheet(self, richTextStyle):
		return self.withAttrs( richTextStyle=richTextStyle, editableRichTextStyle=richTextStyle.withEditable() )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withPythonCodeBorderAttrs(self, pythonCodeBorderAttrs):
		return self.withAttrs( pythonCodeBorderAttrs=pythonCodeBorderAttrs )
	
	
	@DerivedAttributeMethod
	def pythonCodeBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonCodeBorderAttrs'] )
		
	
	
	
	def worksheet(self, title, contents):
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
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

	
	def pythonCode(self, codeView):
		pythonCodeBorderStyle = self.pythonCodeBorderStyle()
		
		return pythonCodeBorderStyle.border( codeView.alignHExpand() ).alignHExpand()
	
	

WorksheetEditorStyleSheet.instance = WorksheetEditorStyleSheet()
	
	
	