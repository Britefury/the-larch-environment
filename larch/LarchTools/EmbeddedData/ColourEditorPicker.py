##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from BritefuryJ.Controls import ColourPicker

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor



class ColourEditorPicker (Editor):
	def __init__(self, model=None, value=Color.GRAY):
		super( ColourEditorPicker, self ).__init__( model, value )


	def _newModel(self, value):
		if not isinstance( value, Color ):
			value = Color.GRAY
		return Model( value )


	def buildEditorPres(self, fragment, inheritedState):
		return ColourPicker( self._model.liveValue )
