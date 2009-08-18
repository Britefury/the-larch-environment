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


default_textStyle = ElementStyleSheet( font=Font( 'Sans serif', Font.BOLD, 14 ), paint=Color.black )

unparsed_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.0, 0.0 ), squiggleUndelinePaint=Color( 1.0, 0.0, 0.0, 0.5 ) )
keyword_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.BOLD, 14 ),  paint=Color( 0.25, 0.0, 0.5 ) )
capitalisedKeyword_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.BOLD, 14 ),  paint=Color( 0.25, 0.0, 0.5 ), bMixedSizeCaps=True )
numericLiteral_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.5, 0.5 ) )
literalFormat_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.0, 0.5 ) )
punctuation_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.0, 0.0, 1.0 ) )
operator_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.BOLD, 14 ),  paint=Color( 0.0, 0.5, 0.0 ) )
comment_textStyle = ElementStyleSheet( font=Font( 'SansSerif', Font.PLAIN, 14 ),  paint=Color( 0.4, 0.4, 0.4 ) )

defBackground_border = SolidBorder( 2.0, 2.0, 5.0, 5.0, Color( 0.420, 0.620, 0.522 ), None )
defHeader_border = EmptyBorder( 0.0, 0.0, 0.0, 0.0, 3.0, 3.0, Color( 0.913, 0.953, 0.933 ) )

classBackground_border = SolidBorder( 2.0, 2.0, 5.0, 5.0, Color( 0.522, 0.420, 0.620 ), None )
classHeader_border = EmptyBorder( 0.0, 0.0, 0.0, 0.0, 3.0, 3.0, Color( 0.933, 0.913, 0.953 ) )

indentedBlock_border = SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.red, None )

pow_scriptStyle = ElementStyleSheet()
div_fractionStyle = ElementStyleSheet( fractionBarPaint=Color( 0.0, 0.5, 0.0 ) )

python_paragraphStyle = ElementStyleSheet( vAlignment=VAlignment.BASELINES, paragraphIndentation=30.0 )

compoundStmt_vboxStyle = ElementStyleSheet( vTypesetting=VTypesetting.ALIGN_WITH_TOP, hAlighment=HAlignment.EXPAND )

tuple_listViewLayout = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )
tuple_listViewLayoutSep = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.ALWAYS )
list_listViewLayout = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )
list_listViewLayoutSep = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.ALWAYS )
dict_listViewLayout = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )
dict_listViewLayoutSep = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )

suite_vboxStyle = ElementStyleSheet( vTypesetting=VTypesetting.ALIGN_WITH_TOP, hAlighment=HAlignment.LEFT )


#lineEditorStyle = GSymStyleSheet( highlightBackgroundColour=Colour3f( 0.85, 0.85, 1.0 ) )

