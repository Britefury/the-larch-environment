##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection

from Britefury.DocView.StyleSheet.DVListStyleSheet import DVListStyleSheet




class DVListBoxStyleSheet (DVListStyleSheet):
	elementDirection = DTDirection.LEFT_TO_RIGHT
	elementSpacing = 0.0
	elementExpand = False
	elementFill = False
	elementShrink = False
	elementPadding = 0.0
	elementMinorDirectionAlignment = DTBox.ALIGN_CENTRE

	overallDirection = DTDirection.LEFT_TO_RIGHT
	overallSpacing = 0.0
	overallExpand = False
	overallFill = False
	overallShrink = False
	overallPadding = 0.0
	overallMinorDirectionAlignment = DTBox.ALIGN_CENTRE


	def beginDelimiter(self):
		return None

	def endDelimiter(self):
		return None

	def elementsContainer(self):
		return DTBox( direction=self.elementDirection, spacing=self.elementSpacing, bExpand=self.elementExpand, bFill=self.elementFill, bShrink=self.elementShrink, minorDirectionAlignment=self.elementMinorDirectionAlignment, padding=self.elementPadding )

	def overallContainer(self, elementsContainer):
		box = DTBox( direction=self.overallDirection, spacing=self.overallSpacing, bExpand=self.overallExpand, bFill=self.overallFill, bShrink=self.overallShrink, minorDirectionAlignment=self.overallMinorDirectionAlignment, padding=self.overallPadding )
		begin = self.beginDelimiter()
		if begin is not None:
			box.append( begin )
		box.append( elementsContainer )
		end = self.endDelimiter()
		if end is not None:
			box.append( end )
		return box
