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


prj_projectTitleBoxStyle = VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, False, 0.0 )
prj_projectTitleStyle = StaticTextStyleSheet( Font( 'Serif', Font.BOLD, 32 ), Color.BLACK )

prj_projectIndexHeaderStyle = StaticTextStyleSheet( Font( "SansSerif", Font.PLAIN, 18 ), Color.BLACK )
prj_projectIndexBoxStyle = VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 10.0, False, 0.0 )

prj_projectContentBoxStyle = VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 40.0, False, 0.0 )

prj_projectPageBoxStyle = VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, False, 0.0 )





prj_packageNameStyle = TextStyleSheet( Font( 'Sans serif', Font.BOLD, 14 ), Color( 0, 0, 128 ) )

prj_packageButtonLabelStyle = StaticTextStyleSheet()
prj_packageAddButtonStyle = ButtonStyleSheet()
prj_packagePageControlsBoxStyle = HBoxStyleSheet( VAlignment.BASELINES, 10.0, False, 0.0 )
prj_packageControlsBoxStyle = HBoxStyleSheet( VAlignment.BASELINES, 20.0, False, 0.0 )

prj_packageHeaderBoxStyle = HBoxStyleSheet( VAlignment.BASELINES, 20.0, False, 0.0 )

prj_packageContentsBoxStyle = VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, False, 0.0 )
prj_packageBoxStyle = VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 5.0, False, 0.0 )

prj_packageBodyBoxStyle = HBoxStyleSheet( VAlignment.TOP, 5.0, False, 0.0 )




prj_pageNameStyle = TextStyleSheet( Font( 'Sans serif', Font.PLAIN, 14 ), Color.BLACK )
prj_pageEditLinkStyle = LinkStyleSheet()
prj_pageBoxStyle = HBoxStyleSheet( VAlignment.BASELINES, 15.0, False, 0.0 )
