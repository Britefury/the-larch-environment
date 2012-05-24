##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from java.util.regex import Pattern

from BritefuryJ.Parser.Utils import Tokens
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Controls import IntSpinEntry, TextEntry
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor


class IntEditorSpinEntry (Editor):
	def __init__(self, model=None, value=0):
		super( IntEditorSpinEntry, self ).__init__( model, value )
		self._min = -1000000
		self._max = 1000000
		self._step = 1
		self._page = 10
		self._incr = IncrementalValueMonitor()


	def __getstate__(self):
		state = super( IntEditorSpinEntry, self ).__getstate__()
		state['min'] = self._min
		state['max'] = self._max
		state['step'] = self._step
		state['page'] = self._page
		return state


	def __setstate__(self, state):
		super( IntEditorSpinEntry, self ).__setstate__( state )
		self._min = state.get( 'min', -1000000 )
		self._max = state.get( 'max', 1000000 )
		self._step = state.get( 'step', 1 )
		self._page = state.get( 'page', 10 )
		self._incr = IncrementalValueMonitor()


	def _newModel(self, value):
		if not isinstance( value, int ):
			value = 0
		return Model( value )


	def _createSettingsPres(self):
		def _listener(attrName):
			class _Listener (TextEntry.TextEntryListener):
				def onTextChanged(listener, textEntry):
					if textEntry.isDisplayedTextValid():
						setattr( self, attrName, int( textEntry.getDisplayedText() ) )
						self._incr.onChanged()
			return _Listener()
		min = [ Label( 'Min' ), TextEntry.regexValidated( str( self._min ), _listener( '_min' ), Tokens.decimalIntegerPattern, 'Please enter an integer value' ) ]
		max = [ Label( 'Max' ), TextEntry.regexValidated( str( self._max ), _listener( '_max' ), Tokens.decimalIntegerPattern, 'Please enter an integer value' ) ]
		step = [ Label( 'Step' ), TextEntry.regexValidated( str( self._step ), _listener( '_step' ), Tokens.decimalIntegerPattern, 'Please enter an integer value' ) ]
		page = [ Label( 'Page' ), TextEntry.regexValidated( str( self._page ), _listener( '_page' ), Tokens.decimalIntegerPattern, 'Please enter an integer value' ) ]
		return Table( [ min, max, step, page ] )



	def buildEditorPres(self, fragment, inheritedState):
		self._incr.onAccess()
		return IntSpinEntry(self._model.liveValue, self._min, self._max, self._step, self._page)
