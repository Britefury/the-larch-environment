##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTWrappedHBoxWithSeparators import DTWrappedHBoxWithSeparators
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel

from Britefury.DocView.StyleSheet.DVListStyleSheet import DVListStyleSheet




class DVListWrappedHBoxStyleSheet (DVListStyleSheet):
	elementSpacing = 0.0
	elementPadding = 0.0

	overallDirection = DTBox.LEFT_TO_RIGHT
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

	def separator(self):
		return DTLabel()

	def elementsContainer(self):
		return DTWrappedHBoxWithSeparators( separatorFactory=self.separator, spacing=self.elementSpacing, padding=self.elementPadding )

	def overallContainer(self, elementsContainer):
		box = DTBox( direction=self.overallDirection, spacing=self.overallSpacing, bExpand=self.overallExpand, bFill=self.overallFill, bShrink=self.overallShrink, alignment=self.overallMinorDirectionAlignment, padding=self.overallPadding )
		begin = self.beginDelimiter()
		if begin is not None:
			box.append( begin )
		box.append( elementsContainer )
		end = self.endDelimiter()
		if end is not None:
			box.append( end )
		return box
