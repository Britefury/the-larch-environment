##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

"""
Define CellInterface and CellOwner
"""

import weakref

from Britefury.Util.SignalSlot import ClassSignal, Signal

from Britefury.Kernel.Mutability import *

from Britefury.FileIO.IOXml import *







class CellOwner (object):
	"""
	Cell owner interface

	Subclasses of CellOwner receive events from a cell that is owned by @self
	"""
	def __init__(self):
		#super( CellOwner, self ).__init__()
		self.cellScope = {}



	def _f_onCellEvaluator(self, cell, oldEval, newEval):
		"""On Cell Evaluator: invoked when a cell's evaluator is changed
		@cell - the cell
		@oldEval - the evaluator before the change
		@newEval - the evaluator after the cahnge"""
		pass


	def _f_onDelegateCellEvaluator(self, cell, oldEval, newEval):
		"""On Delegate Cell Evaluator: invoked when a the evaluator of a member cell of a composite, is changed; see the Sheet module
		@cell - the cell
		@oldEval - the evaluator before the change
		@newEval - the evaluator after the cahnge"""
		pass



class CellInterface (object):
	"""Basic Cell Interface
	CellInterface defines the interface of a cell.

	The evaluator of a cell can be either:
		A literal value
		or
		An instance of a subclass of CellEvaluator, whose evaluate() method can be called to compute the value of the cell.


	Signals:
	changedSignal() - emitted when the value becomes out of date
	evaluatorSignal( oldEval, newEval ) - emitted when the evaluator is changed
	validitySignal() - emitted when the cell becomes valid or invalid
	"""
	
	"""
	The cell state:
	Three possible values:
	  Uninitialised: the value is not up to date; it needs to be recomputed, but the 'changed' signal should *not* be blocked
	  Refresh required: the value is not up to date; it needs to be recomputed; but listeners do not need to be informed of value changes
	  Refresh NOT required: the value is up to date; listeners should be informed of value changes
	"""
	REFRESHSTATE_UNINITIALISED = 0
	REFRESHSTATE_REFRESH_REQUIRED = 1
	REFRESHSTATE_REFRESH_NOT_REQUIRED = 2
	
	
	__slots__ = [ '__weakref__', '_refreshState', '_dependents', 'changedSignal', 'evaluatorSignal', 'validitySignal' ]

	_cellDependencies = None


	#changedSignal = ClassSignal()
	#evaluatorSignal = ClassSignal()
	#validitySignal = ClassSignal()


	def __init__(self):
		super( CellInterface, self ).__init__()

		self._refreshState = self.REFRESHSTATE_UNINITIALISED
		self._dependents = weakref.WeakKeyDictionary()
		self.changedSignal = Signal()
		self.evaluatorSignal = Signal()
		self.validitySignal = Signal()



	def getEvaluator(self):
		"""Get the evaluator"""
		assert False, 'abstract'

	def setEvaluator(self, evaluator):
		"""Set the evaluator"""
		assert False, 'abstract'

	def getLiteralValue(self):
		"""Get the value if the cell value is literal"""
		assert False, 'abstract'

	def setLiteralValue(self, literal):
		"""Set the value of the cell, and make the value literal"""
		assert False, 'abstract'

	def isLiteral(self):
		"""Returns True if the cell has a literal value"""
		assert False, 'abstract'



	def getValue(self):
		"""Get the value of the cell"""
		assert False, 'abstract'

	def getValueAsClass(self, asClass):
		"""Get the value of the cell as class @asClass. If the value is an instance of @asClass, it is returned, else None"""
		value = self.getImmutableValue()
		if isinstance( value, asClass ):
			return self.getValue()
		else:
			return None

	def getImmutableValue(self):
		"""Get the immutable value of the cell. This value is only used where you DO NOT intend to modify it. This will not return a copy of the value"""
		assert False, 'abstract'

	def getImmutableValueAsClass(self, asClass):
		"""See getValueAsClass(), same thing but for immutable value"""
		value = self.getImmutableValue()
		if isinstance( value, asClass ):
			return value
		else:
			return None


	def isValid(self):
		"""Returns True if the cell is 'valid' (can be used)"""
		return False




	def _o_changed(self):
		if self._refreshState  !=  self.REFRESHSTATE_REFRESH_REQUIRED:
			self._refreshState = self.REFRESHSTATE_REFRESH_REQUIRED
			self.changedSignal.emit()
			for dep in self._dependents:
				dep._o_changed()



	def getDependents(self):
		return self._dependents.keys()



	bValid = property( isValid, None )

	evaluator = property( getEvaluator, setEvaluator )
	literalValue = property( getLiteralValue, setLiteralValue )
	bLiteral = property( isLiteral )

	value = property( getValue )
	immutableValue = property( getImmutableValue )

	dependents = property( getDependents )
