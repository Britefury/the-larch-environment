##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.GSym.View.ListView import ListViewLayout, ParagraphListViewLayout, SpanListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Layout import *
from BritefuryJ.DocPresent.Border import *


prj_linkStyle = LinkStyleSheet()


prj_projectIndexHeaderStyle = StaticTextStyleSheet( Font( "SansSerif", Font.PLAIN, 18 ), Color.BLACK )
prj_projectIndexBoxStyle = VBoxStyleSheet( VTypesetting.NONE, 10.0 )

prj_projectContentBoxStyle = VBoxStyleSheet()


prj_controlsBorder = SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None )
prj_controlsBoxStyle = HBoxStyleSheet( 30.0 )


prj_packageNameStyle = TextStyleSheet( Font( 'Sans serif', Font.BOLD, 14 ), Color( 0, 0, 128 ) )

prj_packageButtonLabelStyle = StaticTextStyleSheet()
prj_packageAddButtonStyle = ButtonStyleSheet()
prj_packagePageControlsBoxStyle = HBoxStyleSheet( 10.0 )
prj_packageControlsBoxStyle = HBoxStyleSheet( 20.0 )

prj_packageHeaderBoxStyle = HBoxStyleSheet( 20.0 )

prj_packageContentsBoxStyle = VBoxStyleSheet.defaultStyleSheet
prj_packageBoxStyle = VBoxStyleSheet( VTypesetting.NONE, 5.0 )




prj_pageNameStyle = TextStyleSheet( Font( 'Sans serif', Font.PLAIN, 14 ), Color.BLACK )
prj_pageEditLinkStyle = LinkStyleSheet()
prj_pageBoxStyle = HBoxStyleSheet( 15.0 )
