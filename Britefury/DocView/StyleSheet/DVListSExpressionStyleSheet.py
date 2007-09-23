##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f

from Britefury.DocPresent.Toolkit.DTLabel import DTLabel

from Britefury.DocView.StyleSheet.DVListWrappedLineStyleSheet import DVListWrappedLineStyleSheet




class DVListSExpressionStyleSheet (DVListWrappedLineStyleSheet):
	elementSpacing = 10.0


	def beginDelimiter(self):
		return DTLabel( '(', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )

	def endDelimiter(self):
		return DTLabel( ')', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )
