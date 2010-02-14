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
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.Layout import *
from BritefuryJ.DocPresent.Border import *


prj_linkStyle = LinkStyleParams()


prj_projectIndexHeaderStyle = StaticTextStyleParams( Font( "SansSerif", Font.PLAIN, 18 ), Color.BLACK )
prj_projectIndexBoxStyle = VBoxStyleParams( 10.0 )

prj_projectContentBoxStyle = VBoxStyleParams()


prj_controlsBorder = SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None )
prj_controlsBoxStyle = HBoxStyleParams( 30.0 )


prj_packageNameStyle = TextStyleParams( Font( 'Sans serif', Font.BOLD, 14 ), Color( 0, 0, 128 ) )

prj_packageButtonLabelStyle = StaticTextStyleParams()
prj_packageAddButtonStyle = ButtonStyleParams()
prj_packagePageControlsBoxStyle = HBoxStyleParams( 10.0 )
prj_packageControlsBoxStyle = HBoxStyleParams( 20.0 )

prj_packageHeaderBoxStyle = HBoxStyleParams( 20.0 )

prj_packageContentsBoxStyle = VBoxStyleParams.defaultStyleParams
prj_packageBoxStyle = VBoxStyleParams( 5.0 )




prj_pageNameStyle = TextStyleParams( Font( 'Sans serif', Font.PLAIN, 14 ), Color.BLACK )
prj_pageEditLinkStyle = LinkStyleParams()
prj_pageBoxStyle = HBoxStyleParams( 15.0 )
