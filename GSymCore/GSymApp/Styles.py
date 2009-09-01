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


app_linkStyle = LinkStyleSheet()

app_openDocumentsControlsBoxStyle = HBoxStyleSheet( 20.0 )
app_openDocumentsControlsBorder = EmptyBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) )

app_openDocumentsBoxStyle = VBoxStyleSheet()
app_openDocumentsLineStyle = LineStyleSheet( LineStyleSheet.Direction.HORIZONTAL, Color( 32, 87, 147 ), 1.0, 15.0, 3.0 )
app_openDocumentsGridStyle = TableStyleSheet( 15.0, False, 5.0, False )

app_contentBoxStyle = VBoxStyleSheet()




app_docLinkStyle = LinkStyleSheet()
app_docLinkBorder = EmptyBorder( 0.0, 30.0, 0.0, 0.0, None )
app_docGridRowStyle = GridRowStyleSheet()
