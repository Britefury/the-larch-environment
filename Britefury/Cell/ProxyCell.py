##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.CellEvaluator import CellEvaluator
from Britefury.Cell.CellInterface import CellInterface




class ProxyCell (CellInterface):
	__slots__ = [ '_targetSourceCell', '_targetCell', 'owner' ]


	def __init__(self, targetSourceCell=None):
		super( ProxyCell, self ).__init__()

		self._targetSourceCell = None
		self._targetCell = None

		self.owner = None

		self.setTargetSourceCell( targetSourceCell )
		self._p_refreshTargetCell()




	def getEvaluator(self):
		self._p_refreshTargetCell()

		if self._targetCell is not None:
			return self._targetCell.getEvaluator()
		else:
			return None

	def setEvaluator(self, evaluator):
		self._p_refreshTargetCell()

		if self._targetCell is not None:
			self._targetCell.setEvaluator( evaluator )


	def getLiteralValue(self):
		self._p_refreshTargetCell()

		if self._targetCell is not None:
			return self._targetCell.getLiteralValue()
		else:
			return None

	def setLiteralValue(self, literal):
		self._p_refreshTargetCell()

		if self._targetCell is not None:
			self._targetCell.setLiteralValue( literal )

	def isLiteral(self):
		self._p_refreshTargetCell()

		if self._targetCell is not None:
			return self._targetCell.isLiteral()
		else:
			return False


	def getValue(self):
		if CellInterface._cellDependencies is not None:
			CellInterface._cellDependencies[self] = 0

		oldCellDeps = CellInterface._cellDependencies
		CellInterface._cellDependencies = None

		self._p_refreshTargetCell()

		self._bRefreshRequired = False

		result = None
		if self._targetCell is not None:
			result = self._targetCell.getValue()

		CellInterface._cellDependencies = oldCellDeps

		return result

	def getImmutableValue(self):
		if CellInterface._cellDependencies is not None:
			CellInterface._cellDependencies[self] = 0

		oldCellDeps = CellInterface._cellDependencies
		CellInterface._cellDependencies = None

		self._p_refreshTargetCell()

		self._bRefreshRequired = False

		result = None
		if self._targetCell is not None:
			result = self._targetCell.getImmutableValue()

		CellInterface._cellDependencies = oldCellDeps

		return result



	def isValid(self):
		self._p_refreshTargetCell()

		if self._targetCell is None:
			return False
		else:
			return self._targetCell.isValid()



	def getTargetSourceCell(self):
		return self._targetSourceCell

	def setTargetSourceCell(self, targetSourceCell):
		if targetSourceCell is not self._targetSourceCell:
			if self._targetSourceCell is not None:
				self._targetSourceCell._dependents.pop( self, None )
				self._targetSourceCell.changedSignal.chainDisconnect( self.validitySignal )
			self._targetSourceCell = targetSourceCell
			if self._targetSourceCell is not None:
				self._targetSourceCell._dependents[self] = 0
				self._targetSourceCell.changedSignal.chainConnect( self.validitySignal )
			self._o_changed()
			self.validitySignal.emit()


	def _p_refreshTargetCell(self):
		target = None

		if self._targetSourceCell is not None:
			target = self._targetSourceCell.immutableValue

		if not isinstance( target, CellInterface ):
			target = None

		if target is not self._targetCell:
			bWasValid = self._p_isCellValid( self._targetCell )
			oldEval = self._p_getCellEvaluator( self._targetCell )

			if self._targetCell is not None:
				self._targetCell.evaluatorSignal.chainDisconnect( self.evaluatorSignal )
				self._targetCell.evaluatorSignal.disconnect( self._p_onTargetCellEvaluator )
				self._targetCell.validitySignal.chainDisconnect( self.validitySignal )
				self._targetCell._dependents.pop( self, None )
			self._targetCell = target
			if self._targetCell is not None:
				self._targetCell.evaluatorSignal.chainConnect( self.evaluatorSignal )
				self._targetCell.evaluatorSignal.connect( self._p_onTargetCellEvaluator )
				self._targetCell.validitySignal.chainConnect( self.validitySignal )
				self._targetCell._dependents[self] = 0

			bValid = self._p_isCellValid( target )
			evaluator = self._p_getCellEvaluator( target )

			if bValid != bWasValid:
				self.validitySignal.emit()
			if evaluator is not oldEval:
				self.evaluatorSignal.emit( oldEval, evaluator )
				if self.owner is not None:
					self.owner._f_onDelegateCellEvaluator( self, oldEval, evaluator )
			self._o_changed()



	def _p_onTargetCellEvaluator(self, oldEval, newEval):
		if self.owner is not None:
			self.owner._f_onDelegateCellEvaluator( self, oldEval, newEval )


	@staticmethod
	def _p_isCellValid(cell):
		if cell is None:
			return False
		else:
			return cell.isValid()


	@staticmethod
	def _p_getCellEvaluator(cell):
		if cell is None:
			return None
		else:
			return cell.getEvaluator()



	def __copy__(self):
		return self.__class__( self._targetSourceCell )


	def copyFrom(self, cell):
		self._p_setEvaluator( cell._targetSourceCell )




	bValid = property( isValid, None )

	evaluator = property( getEvaluator, setEvaluator )
	literalValue = property( getLiteralValue, setLiteralValue )
	bLiteral = property( isLiteral )

	value = property( getValue )
	immutableValue = property( getImmutableValue )

	targetSourceCell = property( getTargetSourceCell, setTargetSourceCell )







if __name__ == '__main__':
	import unittest

	from Britefury.Cell.Cell import Cell, IntCell, CellRefCell



	class ProxyCellTest (unittest.TestCase):
		def setUp(self):
			self._sigs = {}

		def tearDown(self):
			self._sigs = {}

		def received(self, name):
			return self._sigs.get( name, 0 )

		def receivedAndReset(self, name):
			return self._sigs.pop( name, 0 )

		def wasReceivedAndReset(self, name):
			return self._sigs.pop( name, 0 ) != 0

		def reset(self, name):
			self._sigs.pop( name, 0 )

		def makeListener(self, name):
			def listener(*args, **kwargs):
				self._sigs[name] = self._sigs.get( name, 0 ) + 1
			return listener




		def testCellValidity(self):
			cell = IntCell( 1 )
			self.assert_( cell.isValid() )




		def testProxyCellValidity(self):
			cell = IntCell( 1 )
			pCellTarget = CellRefCell( None, cell )
			pCell = ProxyCell( pCellTarget )
			qCellTarget = CellRefCell( None, pCell )
			qCell = ProxyCell( qCellTarget )

			self.assert_( cell.isValid() )
			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			pCellTarget.literalValue = None

			self.assert_( not pCell.isValid() )
			self.assert_( not qCell.isValid() )

			pCellTarget.literalValue = cell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCellTarget.literalValue = None

			self.assert_( pCell.isValid() )
			self.assert_( not qCell.isValid() )

			qCellTarget.literalValue = cell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCellTarget.literalValue = pCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )




		def testProxyCellValidity2nd(self):
			cell = IntCell( 1 )
			atCell = CellRefCell( None, cell )
			pCell = ProxyCell( atCell )
			atPCell = CellRefCell( None, pCell )
			qCellTarget = CellRefCell( None, pCell )
			qCell = ProxyCell( atPCell )


			self.assert_( cell.isValid() )
			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			pCell.targetSourceCell = None

			self.assert_( not pCell.isValid() )
			self.assert_( not qCell.isValid() )

			pCell.targetSourceCell = atCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCell.targetSourceCell = None

			self.assert_( pCell.isValid() )
			self.assert_( not qCell.isValid() )

			qCell.targetSourceCell = atCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCell.targetSourceCell = atPCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCell.targetSourceCell = qCellTarget

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCellTarget.literalValue = None

			self.assert_( pCell.isValid() )
			self.assert_( not qCell.isValid() )

			qCellTarget.literalValue = cell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )

			qCellTarget.literalValue = pCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )




		def testProxyCellValiditySignal(self):
			cell = IntCell( 1 )
			pCellTarget = CellRefCell( None, cell )
			pCell = ProxyCell( pCellTarget )
			qCellTarget = CellRefCell( None, pCell )
			qCell = ProxyCell( qCellTarget )

			qCell.validitySignal.connect( self.makeListener( 'validity' ) )

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == False )

			pCellTarget.literalValue = None

			self.assert_( not pCell.isValid() )
			self.assert_( not qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			pCellTarget.literalValue = cell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCellTarget.literalValue = None

			self.assert_( pCell.isValid() )
			self.assert_( not qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCellTarget.literalValue = cell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCellTarget.literalValue = pCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.reset( 'validity' )




		def testProxyCellValiditySignal2nd(self):
			cell = IntCell( 1 )
			atCell = CellRefCell( None, cell )
			pCell = ProxyCell( atCell )
			atPCell = CellRefCell( None, pCell )
			qCellTarget = CellRefCell( None, pCell )
			qCell = ProxyCell( atPCell )

			qCell.validitySignal.connect( self.makeListener( 'validity' ) )

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == False )

			pCell.targetSourceCell = None

			self.assert_( not pCell.isValid() )
			self.assert_( not qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			pCell.targetSourceCell = atCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCell.targetSourceCell = None

			self.assert_( pCell.isValid() )
			self.assert_( not qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCell.targetSourceCell = atCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCell.targetSourceCell = atPCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.reset( 'validity' )

			qCell.targetSourceCell = qCellTarget

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.reset( 'validity' )

			qCellTarget.literalValue = None

			self.assert_( pCell.isValid() )
			self.assert_( not qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCellTarget.literalValue = cell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

			qCellTarget.literalValue = pCell

			self.assert_( pCell.isValid() )
			self.assert_( qCell.isValid() )
			self.reset( 'validity' )



		def testProxyCellLiteralValue(self):
			cell1 = IntCell( 1 )
			cell2 = IntCell( 2 )
			pCellTarget = CellRefCell( None, cell1 )
			pCell = ProxyCell( pCellTarget )

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 2 )
			self.assert_( pCell.value == 1 )

			pCellTarget.literalValue = cell2

			self.assert_( pCell.value == 2 )

			pCell.literalValue = 20

			self.assert_( pCell.value == 20 )
			self.assert_( cell2.value == 20 )

			pCellTarget.literalValue = cell1

			self.assert_( pCell.value == 1 )
			self.assert_( cell2.value == 20 )


		def testProxyCellLiteralValue2nd(self):
			cell1 = IntCell( 1 )
			atCell1 = CellRefCell( None, cell1 )
			cell2 = IntCell( 2 )
			atCell2 = CellRefCell( None, cell2 )
			pCellTarget = CellRefCell( None, cell2 )
			pCell = ProxyCell( atCell1 )

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 2 )
			self.assert_( pCell.value == 1 )

			pCell.targetSourceCell = atCell2

			self.assert_( pCell.value == 2 )

			pCell.literalValue = 20

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 20 )

			pCell.targetSourceCell = atCell1

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 1 )

			pCell.targetSourceCell = pCellTarget

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 20 )

			pCellTarget.literalValue = cell1

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 1 )

			pCell.literalValue = 30

			self.assert_( cell1.value == 30 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 30 )



		def testProxyCellChangedSignal(self):
			cell1 = IntCell( 1 )
			cell2 = IntCell( 2 )
			pCellTarget = CellRefCell( None, cell1 )
			pCell = ProxyCell( pCellTarget )

			pCell.changedSignal.connect( self.makeListener( 'changed' ) )

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 2 )
			self.assert_( pCell.value == 1 )
			self.assert_( self.received( 'changed' ) == 0 )

			pCellTarget.literalValue = cell2

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 2 )
			self.assert_( pCell.value == 2 )
			self.assert_( self.received( 'changed' ) == 1 )

			cell2.literalValue = 20

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 20 )
			self.assert_( self.received( 'changed' ) == 2 )

			pCellTarget.literalValue = cell1

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 1 )
			self.assert_( self.received( 'changed' ) == 3 )





		def testProxyCellChangedSignal2nd(self):
			cell1 = IntCell( 1 )
			atCell1 = CellRefCell( None, cell1 )
			cell2 = IntCell( 2 )
			atCell2 = CellRefCell( None, cell2 )
			pCellTarget = CellRefCell( None, cell2 )
			pCell = ProxyCell( atCell1 )

			pCell.changedSignal.connect( self.makeListener( 'changed' ) )

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 2 )
			self.assert_( pCell.value == 1 )
			self.assert_( self.received( 'changed' ) == 0 )

			pCell.targetSourceCell = atCell2

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 2 )
			self.assert_( pCell.value == 2 )
			self.assert_( self.received( 'changed' ) == 1 )

			cell2.literalValue = 20

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 20 )
			self.assert_( self.received( 'changed' ) == 2 )

			pCell.targetSourceCell = atCell1

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 1 )
			self.assert_( self.received( 'changed' ) == 3 )

			pCell.targetSourceCell = pCellTarget

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 20 )
			self.assert_( self.received( 'changed' ) == 4 )

			pCellTarget.literalValue = cell1

			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 1 )
			self.assert_( self.received( 'changed' ) == 5 )

			pCell.literalValue = 30

			self.assert_( cell1.value == 30 )
			self.assert_( cell2.value == 20 )
			self.assert_( pCell.value == 30 )
			self.assert_( self.received( 'changed' ) == 6 )



		def testFunctionOfProxyCell(self):
			cell1 = IntCell( 1 )
			cell2 = IntCell( 3 )
			pCellTarget = CellRefCell( None, cell1 )
			pCell = ProxyCell( pCellTarget )
			fCell = IntCell( 0 )


			def f():
				return pCell.value * 5

			fCell.function = f

			fCell.changedSignal.connect( self.makeListener( 'changed' ) )

			self.assert_( self.received( 'changed' ) == 0 )
			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 3 )
			self.assert_( pCell.value == 1 )
			self.assert_( fCell.value == 5 )

			self.assert_( fCell.dependencies == [ pCell ] )

			pCellTarget.literalValue = cell2

			self.assert_( self.received( 'changed' ) == 1 )
			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 3 )
			self.assert_( pCell.value == 3 )
			self.assert_( fCell.value == 15 )


			pCell.literalValue = 4

			self.assert_( self.received( 'changed' ) == 2 )
			self.assert_( cell1.value == 1 )
			self.assert_( cell2.value == 4 )
			self.assert_( pCell.value == 4 )
			self.assert_( fCell.value == 20 )


	unittest.main()





