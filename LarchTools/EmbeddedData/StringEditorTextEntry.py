##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from BritefuryJ.Controls import TextEntry

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor



class StringEditorTextEntry (Editor):
	def __init__(self, model=None, value=''):
		super( StringEditorTextEntry, self ).__init__( model, value )


	def _newModel(self, value):
		if not isinstance( value, str )  and  not isinstance( value, unicode) :
			value = ''
		return Model( value )


	def buildEditorPres(self, fragment, inheritedState):
		return TextEntry(self._model.liveValue)
