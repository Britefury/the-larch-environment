##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from BritefuryJ.Controls import RealSpinEntry

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor



class RealEditorSpinEntry (Editor):
	def __init__(self, model=None, value=0.0):
		super( RealEditorSpinEntry, self ).__init__( model, value )
		self._min = -2048576.0
		self._max = 2048576.0
		self._step = 1.0
		self._page = 10.0


	def _newModel(self, value):
		if not isinstance( value, float ):
			value = 0.0
		return Model( value )


	def __present__(self, fragment, inheritedState):
		return RealSpinEntry(self._model.liveValue, self._min, self._max, self._step, self._page)
