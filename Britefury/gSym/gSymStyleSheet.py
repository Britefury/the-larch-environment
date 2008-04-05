##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBox import DTBox




class GSymStyleSheet (object):
	def __init__(self, settingsPairs):
		self._settings = {}
		for key, value in settingsPairs:
			self._settings[key] = value
		
		
	def applyToWidget(self, widget):
		for key, value in self._settings.items():
			try:
				a = getattr( widget.__class__, key )
			except AttributeError:
				pass
			else:
				setattr( widget, key, value )
				
