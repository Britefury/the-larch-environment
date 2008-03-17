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




_widgetPropertiesByType = [
	( DTContainer, [ 'backgroundColour' ] ),
	( DTBorder, [ 'leftMargin', 'rightMargin', 'topMargin', 'bottomMargin', 'allMargins' ] ),
	( DTActiveBorder, [ 'borderWidth', 'highlightedBorderWidth', 'borderColour', 'prelitBorderColour', 'highlightedBorderColour', 'highlightedBackgroundColour' ] ),
	( DTLabel, [ 'font', 'colour', 'hAlign', 'vAlign' ] ),
	( DTBox, [ 'direction', 'spacing', 'bExpand', 'bFill', 'bShrink', 'alignment', 'padding' ] ),
	]


class GSymStyleSheet (object):
	def __init__(self, settingsPairs):
		self._settings = {}
		for key, value in settingsPairs:
			self._settings[key] = value
		
		
	def _p_applyProperties(self, widget, properties):
		for property in properties:
			try:
				value = self._settings[property]
			except KeyError:
				pass
			else:
				setattr( widget, property, value )
	

	def applyToWidget(self, widget):
		for widgetClass, properties in _widgetPropertiesByType:
			if isinstance( widget, widgetClass ):
				self._p_applyProperties( widget, properties )
				
