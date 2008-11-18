##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Math.Math import Colour3f

from Britefury.gSym.View.gSymStyleSheet import GSymStyleSheet


nilStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
unparsedStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )
numericLiteralStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.5, 0.5 ), font='Sans 11' )
literalFormatStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.0, 0.5 ), font='Sans 11' )
punctuationStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.0, 1.0 ), font='Sans 11' )
operatorStyle = GSymStyleSheet( colour=Colour3f( 0.0, 0.5, 0.0 ), font='Sans 11' )
keywordStyle = GSymStyleSheet( colour=Colour3f( 0.25, 0.0, 0.5 ), font='Sans 11' )
lineEditorStyle = GSymStyleSheet( highlightBackgroundColour=Colour3f( 0.85, 0.85, 1.0 ) )
commentStyle = GSymStyleSheet( colour=Colour3f( 0.4, 0.4, 0.4 ), font='Sans 11' )