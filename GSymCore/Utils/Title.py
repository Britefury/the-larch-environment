##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.StyleParams import *

_headerStyle = StaticTextStyleParams( Font( 'Sans serif', Font.PLAIN, 14 ), Color( 0.0, 0.5, 0.0 ) )
_titleStyle = StaticTextStyleParams( Font( 'Serif', Font.BOLD, 32 ), Color.BLACK )
_titleVBoxStyle = VBoxStyleParams()
_titleBackgroundBorder = EmptyBorder( 5.0, 5.0, 5.0, 5.0, Color( 240, 240, 240 ) )


def titleBar(ctx, text):
	title = ctx.staticText( _titleStyle, text )
	titleBackground = ctx.border( _titleBackgroundBorder, title.alignHCentre() )
	return titleBackground.alignHExpand().pad( 5.0, 5.0 ).alignHExpand()


def titleBarWithHeader(ctx, headerText, text):
	header = ctx.staticText( _headerStyle, headerText )
	title = ctx.staticText( _titleStyle, text )
	titleVBox = ctx.vbox( _titleVBoxStyle, [ header.alignHCentre(), title.alignHCentre() ] )
	titleBackground = ctx.border( _titleBackgroundBorder, titleVBox.alignHCentre() )
	return titleBackground.alignHExpand().pad( 5.0, 5.0 ).alignHExpand()
