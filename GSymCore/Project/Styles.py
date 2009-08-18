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
from BritefuryJ.DocPresent import DPVBox
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Layout import *
from BritefuryJ.DocPresent.Border import *

try:
	q = DPVBox.x
except AttributeError:
	pass


prj_projectTitleBoxStyle = ElementStyleSheet.newSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.CENTRE, ySpacing=40.0 )
prj_projectTitleStyle = ElementStyleSheet.newSheet( font=Font( 'Serif', Font.BOLD, 32 ), paint=Color.BLACK )

prj_projectIndexHeaderStyle = ElementStyleSheet.newSheet( font=Font( "SansSerif", Font.PLAIN, 18 ), paint=Color.BLACK )
prj_projectIndexBoxStyle = ElementStyleSheet.newSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND, ySpacing=10.0 )

prj_projectContentBoxStyle = ElementStyleSheet.newSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND, ySpacing=40.0 )

prj_projectPageBoxStyle = ElementStyleSheet.newSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND )





prj_packageNameStyle = ElementStyleSheet.newSheet( font=Font( 'Sans serif', Font.BOLD, 14 ), paint=Color( 0, 0, 128 ) )

prj_packageButtonLabelStyle = ElementStyleSheet.newSheet( font=Font( 'Sans serif', Font.BOLD, 14 ), paint=Color.black )
prj_packageAddButtonStyle = ElementStyleSheet()
prj_packagePageControlsBoxStyle = ElementStyleSheet.newSheet( vAlignment=VAlignment.BASELINES, xSpacing=10.0 )
prj_packageControlsBoxStyle = ElementStyleSheet.newSheet( vAlignment=VAlignment.BASELINES, xSpacing=20.0 )

prj_packageHeaderBoxStyle = ElementStyleSheet.newSheet( vAlignment=VAlignment.BASELINES, xSpacing=20.0 )

prj_packageContentsBoxStyle = ElementStyleSheet.newSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.LEFT )
prj_packageBoxStyle = ElementStyleSheet.newSheet( vTypesetting=VTypesetting.NONE, hAlighment=HAlignment.EXPAND, ySpacing=5.0 )

prj_packageBodyBoxStyle = ElementStyleSheet.newSheet( vAlignment=VAlignment.TOP, xSpacing=5.0 )




prj_pageNameStyle = ElementStyleSheet.newSheet( font=Font( 'Sans serif', Font.PLAIN, 14 ), paint=Color.BLACK )
prj_pageEditLinkStyle = ElementStyleSheet()
prj_pageBoxStyle = ElementStyleSheet.newSheet( vAlignment=VAlignment.BASELINES, xSpacing=15.0 )
