##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************


class StyleSheetDispatcher (object):
	def __init__(self, stringSheet, listSheet, nameToSheet={}):
		super( StyleSheetDispatcher, self ).__init__()
		self._stringSheet = stringSheet
		self._listSheet = listSheet
		self._nameToSheet = nameToSheet


	def getStyleSheetForNode(self, docNodeKey):
		docNode = docNodeKey.docNode
		if isinstance( docNode, str ):
			sheet = self._stringSheet
		else:
			if len( docNode ) > 0:
				name = docNode[0]
				try:
					sheet = self._nameToSheet[name]
				except KeyError:
					sheet = self._listSheet
			else:
				sheet = self._listSheet

		if callable( sheet ):
			return sheet( docNodeKey )
		else:
			return sheet


