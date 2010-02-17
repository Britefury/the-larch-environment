##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.ListView import *
from BritefuryJ.DocPresent.Layout import *



defaultStyle = PrimitiveStyleSheet.instance.withFont( Font( 'SansSerif', Font.PLAIN, 14 ) ).withForeground( Color.black ).withParagraphIndentation( 60.0 )

stringStyle = defaultStyle.withFont( Font( 'SansSerif', Font.PLAIN, 14 ) ).withForeground( Color( 0.0, 0.5, 0.5 ) )

punctuationStyle = defaultStyle.withForeground( Color( 0.0, 0.0, 1.0 ) )

classNameStyle = defaultStyle.withFont( Font( 'SansSerif', Font.PLAIN, 14 ) ).withForeground( Color( 0.0, 0.5, 0.0 ) )

fieldNameStyle = defaultStyle.withFont( Font( 'SansSerif', Font.PLAIN, 14 ) ).withForeground( Color( 0.5, 0.0, 0.5 ) )




paragraph_listViewLayout = ParagraphListViewLayoutStyleSheet.instance.withAddParagraphIndentMarkers( True )
verticalInline_listViewLayout = VerticalInlineListViewLayoutStyleSheet.instance.withIndentation( 30.0 )
vertical_listViewLayout = VerticalListViewLayoutStyleSheet.instance.withIndentation( 30.0 )

_listviewStyle = ListViewStyleSheet.instance.withSeparatorFactory( lambda styleSheet: styleSheet.text( ' ' ) ).withBeginDelimFactory( lambda styleSheet: punctuationStyle.text( '[' ) )
_listviewStyle = _listviewStyle.withEndDelimFactory( lambda styleSheet: punctuationStyle.text( ']' ) )

_objectviewStyle = ListViewStyleSheet.instance.withSeparatorFactory( lambda styleSheet: styleSheet.text( ' ' ) ).withBeginDelimFactory( lambda styleSheet: punctuationStyle.text( '(' ) )
_objectviewStyle = _listviewStyle.withEndDelimFactory( lambda styleSheet: punctuationStyle.text( ')' ) )

paragraph_listViewStyle = _listviewStyle.withListLayout( paragraph_listViewLayout )
paragraph_objectViewStyle = _objectviewStyle.withListLayout( paragraph_listViewLayout )

verticalInline_listViewStyle = _listviewStyle.withListLayout( verticalInline_listViewLayout )
verticalInline_objectViewStyle = _objectviewStyle.withListLayout( verticalInline_listViewLayout )

vertical_listViewStyle = _listviewStyle.withListLayout( vertical_listViewLayout )
vertical_objectViewStyle = _objectviewStyle.withListLayout( vertical_listViewLayout )

