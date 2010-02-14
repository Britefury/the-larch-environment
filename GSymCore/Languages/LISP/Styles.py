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
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.Layout import *



default_textStyle = TextStyleParams( Font( 'SansSerif', Font.PLAIN, 14 ),  Color.black )

string_textStyle = TextStyleParams( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.5, 0.5 ) )
punctuation_textStyle = TextStyleParams( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.0, 1.0 ) )
className_textStyle = TextStyleParams( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.5, 0.0 ) )
fieldName_textStyle = TextStyleParams( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.5, 0.0, 0.5 ) )


lisp_paragraphStyle = ParagraphStyleParams( 0.0, 0.0, 60.0 )


paragraph_listViewLayout = ParagraphListViewLayout( ParagraphStyleParams(), lambda ctx: ctx.text( default_textStyle, ' ' ), True, ListViewLayout.TrailingSeparator.NEVER )

verticalInline_listViewLayout = VerticalInlineListViewLayout( VBoxStyleParams( 0.0 ), \
						ParagraphStyleParams(), 30.0, ListViewLayout.TrailingSeparator.NEVER )

vertical_listViewLayout = VerticalListViewLayout( VBoxStyleParams( 0.0 ), \
						ParagraphStyleParams(), 30.0, ListViewLayout.TrailingSeparator.NEVER )
