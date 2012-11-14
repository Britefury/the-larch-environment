##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Parser.Utils import Tokens
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Controls import RealSpinEntry, TextEntry
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor



class RealEditorSpinEntry (Editor):
	def __init__(self, model=None, value=0.0):
		super( RealEditorSpinEntry, self ).__init__( model, value )
		self._min = -1000000.0
		self._max = 1000000.0
		self._step = 1.0
		self._page = 10.0
		self._incr = IncrementalValueMonitor()


	def __getstate__(self):
		state = super( RealEditorSpinEntry, self ).__getstate__()
		state['min'] = self._min
		state['max'] = self._max
		state['step'] = self._step
		state['page'] = self._page
		return state


	def __setstate__(self, state):
		super( RealEditorSpinEntry, self ).__setstate__( state )
		self._min = state.get( 'min', -1000000.0 )
		self._max = state.get( 'max', 1000000.0 )
		self._step = state.get( 'step', 1.0 )
		self._page = state.get( 'page', 10.0 )
		self._incr = IncrementalValueMonitor()


	def _newModel(self, value):
		if not isinstance( value, float ):
			value = 0.0
		return Model( value )


	def _createSettingsPres(self):
		def _listener(attrName):
			class _Listener (TextEntry.TextEntryListener):
				def onTextChanged(listener, textEntry):
					if textEntry.isDisplayedTextValid():
						setattr( self, attrName, float( textEntry.getDisplayedText() ) )
						self._incr.onChanged()
			return _Listener()
		min = [ Label( 'Min' ), TextEntry( str( self._min ), _listener( '_min' ) ).regexValidated( Tokens.floatingPointPattern, 'Please enter a real value (don\'t forget the decimal point)' ) ]
		max = [ Label( 'Max' ), TextEntry( str( self._max ), _listener( '_max' ) ).regexValidated( Tokens.floatingPointPattern, 'Please enter a real value (don\'t forget the decimal point)' ) ]
		step = [ Label( 'Step' ), TextEntry( str( self._step ), _listener( '_step' ) ).regexValidated( Tokens.floatingPointPattern, 'Please enter a real value (don\'t forget the decimal point)' ) ]
		page = [ Label( 'Page' ), TextEntry( str( self._page ), _listener( '_page' ) ).regexValidated( Tokens.floatingPointPattern, 'Please enter a real value (don\'t forget the decimal point)' ) ]
		return Table( [ min, max, step, page ] )


	def buildEditorPres(self, fragment, inheritedState):
		self._incr.onAccess()
		return RealSpinEntry(self._model.liveValue, self._min, self._max, self._step, self._page)
