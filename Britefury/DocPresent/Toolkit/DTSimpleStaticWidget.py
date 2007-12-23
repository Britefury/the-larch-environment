##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTWidget import DTWidget


class DTSimpleStaticWidget (DTWidget):
	def __init__(self):
		super( DTSimpleStaticWidget, self ).__init__()

		self._cursorEntity = DTCursorEntity( self )



	def getFirstCursorEntity(self):
		return self._cursorEntity
	
	def getLastCursorEntity(self):
		return self._cursorEntity


