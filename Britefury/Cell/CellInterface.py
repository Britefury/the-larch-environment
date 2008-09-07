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

#from Britefury.FileIO.IOXml import *







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

	
	
	
	_cellAccessList = None
	
	@staticmethod
	def pushNewAccessList():
		"""
		Starts tracking cell accesses;
		Creates a new global cell access list that records all cells that are accessed through getValue() / getImmutableValue()
		Returns the current access list
		Call popAccessList() with the value returned here, to finish tracking cell accesses
		"""
		# Save the existing/old global access list
		oldAccesses = CellInterface._cellAccessList
		# Create a new access list
		CellInterface._cellAccessList = weakref.WeakKeyDictionary()
		# Return the existing list
		return oldAccesses
	
	@staticmethod
	def popAccessList(oldAccesses):
		"""
		Stops tracking cell accesses;
		Restores the old cell access list (which was returned by pushNewAccessList())
		Returns a WeakKeyDictionary where the keys are the cells that were access between pushNewAccessList() and popAccessList()
		"""
		# Get the current access list
		accesses = CellInterface._cellAccessList
		# Restore the existing/old global access list
		CellInterface._cellAccessList = oldAccesses
		# Return the current list
		return accesses
		


	@staticmethod
	def blockAccessTracking():
		"""
		Blocks tracking of cell accesses
		Returns the current access list; this MUST be passed to unblockAccessTracking()
		"""
		# Save the existing/old global access list
		oldAccesses = CellInterface._cellAccessList
		# Clear access list
		CellInterface._cellAccessList = None
		# Return the existing list
		return oldAccesses
	
	@staticmethod
	def unblockAccessTracking(oldAccesses):
		"""
		Unblocks cell access tracking
		Pass the object returned by blockAccessTracking()
		"""
		# Restore the existing/old global access list
		CellInterface._cellAccessList = oldAccesses
		



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



	def getValue(self, bBlockDependencies=False):
		"""Get the value of the cell"""
		assert False, 'abstract'

	def getImmutableValue(self, bBlockDependencies=False):
		"""Get the immutable value of the cell. This value is only used where you DO NOT intend to modify it. This will not return a copy of the value"""
		assert False, 'abstract'


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



