##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.GSym.View.ListView import ListViewLayout, SpanListViewLayout, ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Layout import *



default_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color.black )

string_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.5, 0.5 ) )
punctuation_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.0, 1.0 ) )
className_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.5, 0.0 ) )
fieldName_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.5, 0.0, 0.5 ) )


lisp_paragraphStyle = ElementStyleSheet( vAlignment=VAlignment.BASELINES, paragraphIndentation=60.0 )


paragraph_listViewLayout = ParagraphListViewLayout( ElementStyleSheet(), lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )

verticalInline_listViewLayout = VerticalInlineListViewLayout( ElementStyleSheet( vTypesetting=VTypesetting.ALIGN_WITH_TOP, hAlighment=HAlignment.LEFT, ySpacing=0.0, childPack_yExpand=False, chidlPack_yPadding=0.0 ), \
						ElementStyleSheet(), 30.0, ListViewLayout.TrailingSeparator.NEVER )

vertical_listViewLayout = VerticalListViewLayout( ElementStyleSheet( vTypesetting=VTypesetting.ALIGN_WITH_TOP, hAlighment=HAlignment.LEFT, ySpacing=0.0, childPack_yExpand=False, chidlPack_yPadding=0.0 ), \
						ElementStyleSheet(), 30.0, ListViewLayout.TrailingSeparator.NEVER )
