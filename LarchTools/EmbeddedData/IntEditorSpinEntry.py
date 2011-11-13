##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from BritefuryJ.Controls import IntSpinEntry

import LarchCore.Languages.Python25.Builder as PyBuilder

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor



class IntEditorSpinEntry (Editor):
	def __init__(self, model=None, value=None):
		super( IntEditorSpinEntry, self ).__init__( model, value )
		self._min = -2048576
		self._max = 2048576
		self._step = 1
		self._page = 10


	def _newModel(self, value):
		if not isinstance( value, int ):
			value = 0
		return Model( value )


	def __py_evalmodel__(self):
		return PyBuilder.expr( self._model.value ).build()


	def __present__(self, fragment, inheritedState):
		return IntSpinEntry(self._model.liveValue, self._min, self._max, self._step, self._page)
