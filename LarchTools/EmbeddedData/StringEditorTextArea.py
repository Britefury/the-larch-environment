##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from BritefuryJ.Controls import TextArea, Checkbox
from BritefuryJ.Live import LiveValue

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor



class StringEditorTextArea (Editor):
	def __init__(self, model=None, value=''):
		super( StringEditorTextArea, self ).__init__( model, value )
		self._commitOnChange = LiveValue( True )


	def __getstate__(self):
		state = super( StringEditorTextArea, self ).__getstate__()
		state['commitOnChange'] = self._commitOnChange.getValue()
		return state


	def __setstate__(self, state):
		super( StringEditorTextArea, self ).__setstate__( state )
		self._commitOnChange = LiveValue( state.get( 'commitOnChange', True ) )


	def _newModel(self, value):
		if not isinstance( value, str )  and  not isinstance( value, unicode ) :
			value = ''
		return Model( value )


	def _createSettingsPres(self):
		commitOnChange = Checkbox.checkboxWithLabel( 'Commit on change', self._commitOnChange )
		return commitOnChange


	def buildEditorPres(self, fragment, inheritedState):
		if self._commitOnChange.getValue():
			return TextArea.textAreaCommitOnChange( self._model.liveValue )
		else:
			return TextArea(self._model.liveValue)
