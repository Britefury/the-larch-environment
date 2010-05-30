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
		
		self.initAttr( 'projectControlsAttrs', AttributeValues( border=SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None ), hBoxSpacing=30.0 ) )
		self.initAttr( 'packageNameAttrs', AttributeValues( foreground=Color( 0.0, 0.0, 0.5 ), fontBold=True, fontSize=14 ) )
		self.initAttr( 'itemHoverHighlightAttrs', AttributeValues( hoverBackground=FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )
		
		self.initAttr( 'packageContentsIndentation', 20.0 )
	
		
	def newInstance(self):
		return WorksheetStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withRichTextStyleSheet(self, richTextStyle):
		return self.withAttrs( richTextStyle=richTextStyle )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	
	
	def withProjectControlsAttrs(self, projectControlsAttrs):
		return self.withAttrs( projectControlsAttrs=projectControlsAttrs )
	
	def withPackageNameAttrs(self, packageNameAttrs):
		return self.withAttrs( packageNameAttrs=packageNameAttrs )
	
	def withItemHoverHighlightAttrs(self, itemHoverHighlightAttrs):
		return self.withAttrs( itemHoverHighlightAttrs=itemHoverHighlightAttrs )
	
	def withPackageContentsIndentation(self, packageContentsIndentation):
		return self.withAttrs( packageContentsIndentation=packageContentsIndentation )
	
	
	
	@DerivedAttributeMethod
	def projectControlsStyle(self):
		return self['primitiveStyle'].withAttrValues( self['projectControlsAttrs'] )
	
	@DerivedAttributeMethod
	def packageNameStyle(self):
		return self['primitiveStyle'].withAttrValues( self['packageNameAttrs'] )
	
	@DerivedAttributeMethod
	def itemHoverHighlightStyle(self):
		return self['primitiveStyle'].withAttrValues( self['itemHoverHighlightAttrs'] )
	
	
	
	def worksheet(self, title, contents):
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
		controlsStyle = self['controlsStyle']
		projectControlsStyle = self.projectControlsStyle()

		
		homeLink = controlsStyle.link( 'HOME PAGE', Location( '' ) ).getElement()
		linkHeader = richTextStyle.linkHeaderBar( [ homeLink ] )
		
		title = richTextStyle.titleBarWithSubtitle( title, 'Worksheet' )
		
		return richTextStyle.page( [ linkHeader, title ] + contents )
	
	
	def paragraph(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].paragraph( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	def h1(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h1( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	def h2(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h2( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	def h3(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h3( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	def h4(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h4( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	def h5(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h5( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	def h6(self, text):
		if text != '':
			return self['primitiveStyle'].segment( True, True, self['editableRichTextStyle'].h6( text ) )
		else:
			return self['primitiveStyle'].segment( True, True, self['primitiveStyle'].text( '' ) )
	
	

WorksheetEditorStyleSheet.instance = WorksheetEditorStyleSheet()
	
	
	