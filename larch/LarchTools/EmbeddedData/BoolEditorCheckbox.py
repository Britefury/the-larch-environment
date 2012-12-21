##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.util.regex import Pattern

from BritefuryJ.Parser.Utils import Tokens
from BritefuryJ.Pres.Primitive import Label, Row
from BritefuryJ.Controls import Checkbox, TextEntry
from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor


class BoolEditorCheckbox (Editor):
	def __init__(self, model=None, value=False):
		super( BoolEditorCheckbox, self ).__init__( model, value )
		self._labelText = LiveValue( 'state' )


	def __getstate__(self):
		state = super( BoolEditorCheckbox, self ).__getstate__()
		state['labelText'] = self._labelText.getStaticValue()
		return state


	def __setstate__(self, state):
		super( BoolEditorCheckbox, self ).__setstate__( state )
		self._labelText = LiveValue( state.get( 'labelText', 'state' ) )


	def _newModel(self, value):
		if not isinstance( value, bool ):
			value = False
		return Model( value )


	def _createSettingsPres(self):
		text = [ Label( 'Label text: ' ), TextEntry.textEntryCommitOnChange( self._labelText ) ]
		return Row( text )



	def buildEditorPres(self, fragment, inheritedState):
		return Checkbox.checkboxWithLabel(self._labelText.getValue(), self._model.liveValue)
