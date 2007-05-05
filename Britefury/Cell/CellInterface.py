##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref

from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel.Mutability import *

from Britefury.FileIO.IOXml import *







class CellOwner (object):
	def __init__(self):
		#super( CellOwner, self ).__init__()
		self.cellScope = {}



	def _f_onCellEvaluator(self, cell, oldEval, newEval):
		pass


	def _f_onDelegateCellEvaluator(self, cell, oldEval, newEval):
		pass



class CellInterface (object):
	__slots__ = [ '__weakref__', '_bRefreshRequired', '_dependents' ]

	_cellDependencies = None


	changedSignal = ClassSignal()
	evaluatorSignal = ClassSignal()
	validitySignal = ClassSignal()


	def __init__(self):
		super( CellInterface, self ).__init__()

		# @self._bRefreshRequired has three possible values:
		#	True:		will result in cells refreshing their value before returning it (getValue())
		#	False:		will prevent the changed signal from being sent
		#	None:		will result in cells refreshing their value before returning it, AND changed signals WILL be sent
		self._bRefreshRequired = None
		self._dependents = weakref.WeakKeyDictionary()



	def getEvaluator(self):
		assert False, 'abstract'

	def setEvaluator(self, evaluator):
		assert False, 'abstract'

	def getLiteralValue(self):
		assert False, 'abstract'

	def setLiteralValue(self, literal):
		assert False, 'abstract'

	def isLiteral(self):
		assert False, 'abstract'



	def getValue(self):
		assert False, 'abstract'

	def getValueAsClass(self, asClass):
		value = self.getImmutableValue()
		if isinstance( value, asClass ):
			return self.getValue()
		else:
			return None

	def getImmutableValue(self):
		assert False, 'abstract'

	def getImmutableValueAsClass(self, asClass):
		value = self.getImmutableValue()
		if isinstance( value, asClass ):
			return value
		else:
			return None


	def isValid(self):
		return False




	def _o_changed(self):
		if self._bRefreshRequired != True:
			self._bRefreshRequired = True
			self.changedSignal.emit()
			for dep in self._dependents:
				dep._o_changed()


	bValid = property( isValid, None )

	evaluator = property( getEvaluator, setEvaluator )
	literalValue = property( getLiteralValue, setLiteralValue )
	bLiteral = property( isLiteral )

	value = property( getValue )
	immutableValue = property( getImmutableValue )
