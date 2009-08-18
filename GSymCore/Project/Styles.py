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


prj_projectTitleBoxStyle = ElementStyleSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.CENTRE, ySpacing=40.0 )
prj_projectTitleStyle = ElementStyleSheet( font=Font( 'Serif', Font.BOLD, 32 ), paint=Color.BLACK )

prj_projectIndexHeaderStyle = ElementStyleSheet( font=Font( "SansSerif", Font.PLAIN, 18 ), paint=Color.BLACK )
prj_projectIndexBoxStyle = ElementStyleSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND, ySpacing=10.0 )

prj_projectContentBoxStyle = ElementStyleSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND, ySpacing=40.0 )

prj_projectPageBoxStyle = ElementStyleSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND )





prj_packageNameStyle = ElementStyleSheet( font=Font( 'Sans serif', Font.BOLD, 14 ), paint=Color( 0, 0, 128 ) )

prj_packageButtonLabelStyle = ElementStyleSheet( font=Font( 'Sans serif', Font.BOLD, 14 ), paint=Color.black )
prj_packageAddButtonStyle = ElementStyleSheet()
prj_packagePageControlsBoxStyle = ElementStyleSheet( vAlignment=VAlignment.BASELINES, xSpacing=10.0 )
prj_packageControlsBoxStyle = ElementStyleSheet( vAlignment=VAlignment.BASELINES, xSpacing=20.0 )

prj_packageHeaderBoxStyle = ElementStyleSheet( vAlignment=VAlignment.BASELINES, xSpacing=20.0 )

prj_packageContentsBoxStyle = ElementStyleSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.LEFT )
prj_packageBoxStyle = ElementStyleSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND, ySpacing=5.0 )

prj_packageBodyBoxStyle = ElementStyleSheet( vAlignment=VAlignment.TOP, xSpacing=5.0 )




prj_pageNameStyle = ElementStyleSheet.newSheet( font=Font( 'Sans serif', Font.PLAIN, 14 ), paint=Color.BLACK )
prj_pageEditLinkStyle = ElementStyleSheet()
prj_pageBoxStyle = ElementStyleSheet.newSheet( vAlignment=VAlignment.BASELINES, xSpacing=15.0 )
