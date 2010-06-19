##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Worksheet import Schema, ViewSchema



class TextStyleOperation (object):
	def __init__(self, style):
		self._style = style
		
	def applyToTextNode(self, node):
		node.setStyle( self._style )
	
	def createTextModel(self, text):
		return Schema.Paragraph( text=text, style=self._style )

	
	
	
