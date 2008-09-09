##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *

from Britefury.Tests.ElementTreeTestApp import ElementTreeTestApp
from Britefury.gSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout, listView

from java.awt import Font, Color


from javax.swing import JLabel

textStyle = TextStyleSheet( Font( 'SansSerif', Font.PLAIN, 12 ), Color( 0.0, 0.0, 0.0 ) )
delimStyle = TextStyleSheet( Font( 'SansSerif', Font.BOLD, 12 ), Color( 0.0, 0.0, 0.5 ) )



beginDelimFactory = lambda: TextElement( delimStyle, '[' )
endDelimFactory = lambda: TextElement( delimStyle, ']' )
separatorFactory = lambda: TextElement( textStyle, ',' )
spacingFactory = lambda: TextElement( textStyle, ' ' )

listContents = [ TextElement( textStyle, x )   for x in [ 'abc', '123', 'xyz', 'paragraph', 'list', 'test' ] ]
paragraphStyle = ParagraphStyleSheet()
paragraph, paragraphCell = listView( [], ParagraphListViewLayout( paragraphStyle, spacingFactory, 0 ), beginDelimFactory, endDelimFactory, separatorFactory, listContents )
paragraphCell.getValue()

listContents = [ TextElement( textStyle, x )   for x in [ 'abc', '123', 'xyz', 'horizontal', 'list', 'test' ] ]
hboxStyle = HBoxStyleSheet()
hbox, hboxCell = listView( [], HorizontalListViewLayout( hboxStyle, spacingFactory ), beginDelimFactory, endDelimFactory, separatorFactory, listContents )
hboxCell.getValue()

listContents = [ TextElement( textStyle, x )   for x in [ 'abc', '123', 'xyz', 'vertical-inline', 'list', 'test' ] ]
vInlineVBoxStyle = VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, False, 0.0 )
vInlineParaStyle = ParagraphStyleSheet()
vboxInline, vboxInlineCell = listView( [], VerticalInlineListViewLayout( vInlineVBoxStyle, vInlineParaStyle, 20.0 ), beginDelimFactory, endDelimFactory, separatorFactory, listContents )
vboxInlineCell.getValue()

listContents = [ TextElement( textStyle, x )   for x in [ 'abc', '123', 'xyz', 'vertical', 'list', 'test' ] ]
vboxStyle = VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, False, 0.0 )
vhboxStyle = ParagraphStyleSheet()
vbox, vboxCell = listView( [], VerticalListViewLayout( vboxStyle, vhboxStyle ), beginDelimFactory, endDelimFactory, separatorFactory, listContents )
vboxCell.getValue()



boxStyle = VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 25.0, False, 0.0 )
box = VBoxElement( boxStyle )


box.setChildren( [ paragraph, hbox, vboxInline, vbox ] )



t = ElementTree()
t.getRoot().setChild( box )

app = ElementTreeTestApp( 'List view test', t )
app.run()

