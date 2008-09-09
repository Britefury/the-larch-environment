##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Cell.CellEvaluator import CellEvaluator



class CellEvaluatorWithExternalDependencies (CellEvaluator):
	__slots__ = [ '_ownerCell' ]


	def __init__(self, ownerCell):
		super( CellEvaluatorWithExternalDependencies, self ).__init__()
		self._ownerCell = ownerCell



	def _o_changed(self):
		if self._ownerCell is not None:
			self._ownerCell._o_changed()