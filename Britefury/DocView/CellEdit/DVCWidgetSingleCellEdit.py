##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

class DVCWidgetSingleCellEdit (object):
	def __init__(self):
		super( DVCWidgetSingleCellEdit, self ).__init__()
		self._cell = None


	def attachCell(self, cell):
		assert self._cell is None, 'cell already attached'
		self._cell = cell

	def detachCell(self):
		assert self._cell is not None, 'no cell attached'
		self._cell = None


	def _o_isValid(self):
		if self._cell is not None:
			return self._cell.isValid()
		else:
			return False
