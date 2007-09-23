##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f

from Britefury.DocView.StyleSheet.DVStyleSheet import DVStyleSheet




class DVBorderStyleSheet (DVStyleSheet):
	leftMargin=3.0
	rightMargin=3.0
	topMargin=3.0
	bottomMargin=3.0

	borderWidth=1.0
	highlightedBorderWidth=1.0

	borderColour=None
	prelitBorderColour=Colour3f( 0.6, 0.6, 0.6 )
	highlightedBorderColour=Colour3f( 0.0, 0.0, 0.0 )

	backgroundColour=None
	highlightedBackgroundColour=None
