##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from java.awt import Font, Color

from Britefury.gSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.ElementTree import *



default_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 12 ),  Color.black )

string_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 12 ),  Color( 0.0, 0.5, 0.5 ) )
punctuation_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 12 ),  Color( 0.0, 0.0, 1.0 ) )


lisp_paragraphStyle = ParagraphStyleSheet( DPParagraph.Alignment.BASELINES, 0.0, 0.0, 60.0 )


horizontal_listViewLayout = ParagraphListViewLayout( lisp_paragraphStyle, lambda: TextElement( default_textStyle, ' ' ), 0 )

verticalInline_listViewLayout = VerticalInlineListViewLayout( VBoxStyleSheet( DPVBox.Typesetting.ALIGN_WITH_TOP, DPVBox.Alignment.LEFT, 0.0, False, 0.0 ), \
						ParagraphStyleSheet(), 30.0 )

vertical_listViewLayout = VerticalListViewLayout( VBoxStyleSheet( DPVBox.Typesetting.ALIGN_WITH_TOP, DPVBox.Alignment.LEFT, 0.0, False, 0.0 ), \
						ParagraphStyleSheet(), 30.0 )
