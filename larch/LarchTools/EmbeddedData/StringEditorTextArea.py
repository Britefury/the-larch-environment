##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
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
