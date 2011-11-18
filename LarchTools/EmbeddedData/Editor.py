##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'


class Editor (object):
	def __init__(self, model=None, value=None):
		if model is None:
			model = self._newModel( value )
		self._model = model


	def _newModel(self, value):
		raise NotImplementedError


	def __py_eval__(self, globals, locals, codeGen):
		return self._model.value


	model = property( lambda self: self._model )