##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.GSym.View.ListView import ListViewLayout, ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Layout import *
from BritefuryJ.DocPresent.ElementTree import *



default_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color.black )

string_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.5, 0.5 ) )
punctuation_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.0, 1.0 ) )
className_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.5, 0.0 ) )
fieldName_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.5, 0.0, 0.5 ) )


lisp_paragraphStyle = ParagraphStyleSheet( VAlignment.BASELINES, 0.0, 0.0, 0.0, 60.0 )


horizontal_listViewLayout = ParagraphListViewLayout( lisp_paragraphStyle, lambda: TextElement( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )

verticalInline_listViewLayout = VerticalInlineListViewLayout( VBoxStyleSheet( HAlignment.LEFT, 0.0, False, 0.0 ), \
						ParagraphStyleSheet(), 30.0, ListViewLayout.TrailingSeparator.NEVER )

vertical_listViewLayout = VerticalListViewLayout( VBoxStyleSheet( HAlignment.LEFT, 0.0, False, 0.0 ), \
						ParagraphStyleSheet(), 30.0, ListViewLayout.TrailingSeparator.NEVER )
