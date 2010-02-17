##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.Border import *

from GSymCore.Utils.LinkHeader import LinkHeaderStyleSheet
from GSymCore.Utils.Title import TitleBarStyleSheet
from GSymCore.Utils.TabbedBox import TabbedBoxStyleSheet




def app_linkStyle(styleSheet):
	return styleSheet

def app_openDocumentsControlsStyle(styleSheet):
	return styleSheet.withHBoxSpacing( 20.0 ).withBorder( EmptyBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) ) )

def app_openDocumentsStyle(styleSheet):
	styleSheet = styleSheet.withLineDirection( LineStyleParams.Direction.HORIZONTAL ).withForeground( Color( 32, 87, 147 ) ).withLineThickness( 1.0 ).withLineInset( 15.0 ).withLinePadding( 3.0 )
	return styleSheet.withTableColumnSpacing( 15.0 ).withTableColumnExpand( False ).withTableRowSpacing( 5.0 ).withTableRowExpand( False )

def app_contentBoxStyle(styleSheet):
	return styleSheet

def app_docStyle(styleSheet):
	return styleSheet.withBorder( EmptyBorder( 0.0, 30.0, 0.0, 0.0, None ) )



app_linkHeaderStyle = LinkHeaderStyleSheet.instance
app_titleStyle = TitleBarStyleSheet.instance
app_tabbedBoxStyle = TabbedBoxStyleSheet.instance