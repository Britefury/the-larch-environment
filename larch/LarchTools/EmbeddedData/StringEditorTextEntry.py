##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
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
