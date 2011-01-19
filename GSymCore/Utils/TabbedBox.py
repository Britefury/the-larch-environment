##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color, Paint

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.StyleSheet import *

from Britefury.AttributeTableUtils.DerivedAttributeMethod import DerivedAttributeMethod


class TabbedBoxStyle (object):
	class _Params (object):
		def __init__(self, headerStyle, bodyStyle):
			self.headerStyle = headerStyle
			self.bodyStyle = bodyStyle
	
	tabbedBoxNamespace = AttributeNamespace( 'tabbedBox' )
	
	headerTextStyle = AttributeNonNull( tabbedBoxNamespace, 'headerTextStyle', StyleSheet, StyleSheet.instance.withAttr( Primitive.fontFace, 'SansSerif' ).withAttr( Primitive.fontBold, True )
	                                    .withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.foreground, Color.BLACK ) )
	headerPadding = AttributeNonNull( tabbedBoxNamespace, 'headerPadding', float, 2.0 )
	bodyPadding = AttributeNonNull( tabbedBoxNamespace, 'bodyPadding', float, 2.0 )
	boxColour = AttributeNonNull( tabbedBoxNamespace, 'boxColour', Paint, Color( 161, 178, 160 ) )
	
	@PyDerivedValueTable(tabbedBoxNamespace)
	def _params(self, style):
		hpad = self[headerPadding]
		bpad = self[bodyPadding]
		boxColour = self[boxColour]
		return self._Params( style.withAttrs( self[headerTextStyle] ).withAttr( Primitive.border, Border( FilledBorder( hpad, hpad, hpad, hpad, boxColour ) ) ),
	                                     style.withAttr( Primitive.border, SolidBorder( bpad, bpad, boxColour, None ) ) )

	



class TabbedBox (Pres):
	def __init__(self, tabTitle, contents):
		self._tabTitle = tabTitle
		self._contents = Pres.coerce( contents )
		
		
	def present(self, ctx, style):
		params = TabbedBoxStyle._params.get( style )
		headerStyle = params.headerStyle
		bodyStyle = params.bodyStyle

		contentsElement = contents.present( ctx, style )
		header = Border( StaticText( self._tabTitle ) ).present( ctx, headerStyle )
		body = Border( contentsElement )
		return Column( [ header, body.alignHExpand() ] ).present( ctx, headerStyle )

