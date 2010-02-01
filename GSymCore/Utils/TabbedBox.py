##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.StyleSheets import *


_tabTextStyle = StaticTextStyleSheet( Font( "SansSerif", Font.BOLD, 16 ), Color.BLACK )
_tabBorder = EmptyBorder( 2.0, 2.0, 2.0, 2.0, Color( 161, 178, 160 ) )
_tabBoxBorder = SolidBorder( 2.0, 2.0, Color( 161, 178, 160 ), None )
_tabVBoxStyle = VBoxStyleSheet()


def tabbedBox(ctx, tabTitle, contents):
	tabLabel = ctx.staticText( _tabTextStyle, tabTitle )
	tab = ctx.border( _tabBorder, tabLabel )
	box = ctx.border( _tabBoxBorder, ctx.vbox( _tabVBoxStyle, [ contents ] ) )
	return ctx.vbox( _tabVBoxStyle, [ tab, box.alignHExpand() ] )
	

