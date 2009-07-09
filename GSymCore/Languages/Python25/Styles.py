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


default_textStyle = TextStyleSheet()

unparsed_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.0, 0.0 ), Color( 1.0, 0.0, 0.0 ) )
keyword_textStyle = TextStyleSheet( Font( 'SansSerif', Font.BOLD, 14 ),  Color( 0.25, 0.0, 0.5 ) )
capitalisedKeyword_textStyle = TextStyleSheet( Font( 'SansSerif', Font.BOLD, 14 ),  Color( 0.25, 0.0, 0.5 ), True )
numericLiteral_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.5, 0.5 ) )
literalFormat_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.0, 0.5 ) )
punctuation_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.0, 0.0, 1.0 ) )
operator_textStyle = TextStyleSheet( Font( 'SansSerif', Font.BOLD, 14 ),  Color( 0.0, 0.5, 0.0 ) )
comment_textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 14 ),  Color( 0.4, 0.4, 0.4 ) )

defBackground_border = SolidBorder( 2.0, 2.0, 5.0, 5.0, Color( 0.420, 0.620, 0.522 ), None )
defHeader_border = EmptyBorder( 0.0, 0.0, 0.0, 0.0, 3.0, 3.0, Color( 0.913, 0.953, 0.933 ) )

classBackground_border = SolidBorder( 2.0, 2.0, 5.0, 5.0, Color( 0.522, 0.420, 0.620 ), None )
classHeader_border = EmptyBorder( 0.0, 0.0, 0.0, 0.0, 3.0, 3.0, Color( 0.933, 0.913, 0.953 ) )

indentedBlock_border = SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.red, None )

pow_scriptStyle = ScriptStyleSheet( 0.0, 0.0 )
div_fractionStyle = FractionStyleSheet( Color( 0.0, 0.5, 0.0 ) )

python_paragraphStyle = ParagraphStyleSheet( VAlignment.BASELINES, 0.0, 0.0, 0.0, 30.0 )

compoundStmt_vboxStyle = VBoxStyleSheet( VTypesetting.ALIGN_WITH_TOP, HAlignment.EXPAND, 0.0, False, 0.0 )

tuple_listViewLayout = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )
tuple_listViewLayoutSep = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.ALWAYS )
list_listViewLayout = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )
list_listViewLayoutSep = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.ALWAYS )
dict_listViewLayout = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )
dict_listViewLayoutSep = SpanListViewLayout( lambda: DPText( default_textStyle, ' ' ), 0, ListViewLayout.TrailingSeparator.NEVER )

suite_vboxStyle = VBoxStyleSheet( VTypesetting.ALIGN_WITH_TOP, HAlignment.LEFT, 0.0, False, 0.0 )


#lineEditorStyle = GSymStyleSheet( highlightBackgroundColour=Colour3f( 0.85, 0.85, 1.0 ) )

