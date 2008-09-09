##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Cell.CellEvaluator import CellEvaluator
from Britefury.Cell.CellInterface import CellInterface




class ProxyCell (CellInterface):
	__slots__ = [ '_targetSourceCell', '_targetCell', 'owner' ]


	def __init__(self, targetSourceCell=None):
		"""targetSourceCell is a higher order cell; its value is a reference to a cell, whose value is the target"""
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
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = 0

		oldCellDeps = CellInterface._cellAccessList
		CellInterface._cellAccessList = None

		self._p_refreshTargetCell()

		self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED

		result = None
		if self._targetCell is not None:
			result = self._targetCell.getValue()

		CellInterface._cellAccessList = oldCellDeps

		return result

	def getImmutableValue(self):
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = 0

		oldCellDeps = CellInterface._cellAccessList
		CellInterface._cellAccessList = None

		self._p_refreshTargetCell()

		self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED

		result = None
		if self._targetCell is not None:
			result = self._targetCell.getImmutableValue()

		CellInterface._cellAccessList = oldCellDeps

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
			target = self._targetSourceCell.getImmutableValue()

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




	targetSourceCell = property( getTargetSourceCell, setTargetSourceCell )







import unittest

from Britefury.Cell.Cell import Cell, IntCell, CellRefCell



class TestCase_ProxyCell (unittest.TestCase):
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

		pCellTarget.setLiteralValue( None )

		self.assert_( not pCell.isValid() )
		self.assert_( not qCell.isValid() )

		pCellTarget.setLiteralValue( cell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )

		qCellTarget.setLiteralValue( None )

		self.assert_( pCell.isValid() )
		self.assert_( not qCell.isValid() )

		qCellTarget.setLiteralValue( cell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )

		qCellTarget.setLiteralValue( pCell )

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

		qCellTarget.setLiteralValue( None )

		self.assert_( pCell.isValid() )
		self.assert_( not qCell.isValid() )

		qCellTarget.setLiteralValue( cell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )

		qCellTarget.setLiteralValue( pCell )

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

		pCellTarget.setLiteralValue( None )

		self.assert_( not pCell.isValid() )
		self.assert_( not qCell.isValid() )
		self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

		pCellTarget.setLiteralValue( cell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )
		self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

		qCellTarget.setLiteralValue( None )

		self.assert_( pCell.isValid() )
		self.assert_( not qCell.isValid() )
		self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

		qCellTarget.setLiteralValue( cell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )
		self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

		qCellTarget.setLiteralValue( pCell )

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

		qCellTarget.setLiteralValue( None )

		self.assert_( pCell.isValid() )
		self.assert_( not qCell.isValid() )
		self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

		qCellTarget.setLiteralValue( cell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )
		self.assert_( self.wasReceivedAndReset( 'validity' ) == True )

		qCellTarget.setLiteralValue( pCell )

		self.assert_( pCell.isValid() )
		self.assert_( qCell.isValid() )
		self.reset( 'validity' )



	def testProxyCellLiteralValue(self):
		cell1 = IntCell( 1 )
		cell2 = IntCell( 2 )
		pCellTarget = CellRefCell( None, cell1 )
		pCell = ProxyCell( pCellTarget )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 2 )
		self.assert_( pCell.getValue() == 1 )

		pCellTarget.setLiteralValue( cell2 )

		self.assert_( pCell.getValue() == 2 )

		pCell.setLiteralValue( 20 )

		self.assert_( pCell.getValue() == 20 )
		self.assert_( cell2.getValue() == 20 )

		pCellTarget.setLiteralValue( cell1 )

		self.assert_( pCell.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )


	def testProxyCellLiteralValue2nd(self):
		cell1 = IntCell( 1 )
		atCell1 = CellRefCell( None, cell1 )
		cell2 = IntCell( 2 )
		atCell2 = CellRefCell( None, cell2 )
		pCellTarget = CellRefCell( None, cell2 )
		pCell = ProxyCell( atCell1 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 2 )
		self.assert_( pCell.getValue() == 1 )

		pCell.targetSourceCell = atCell2

		self.assert_( pCell.getValue() == 2 )

		pCell.setLiteralValue( 20 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 20 )

		pCell.targetSourceCell = atCell1

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 1 )

		pCell.targetSourceCell = pCellTarget

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 20 )

		pCellTarget.setLiteralValue( cell1 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 1 )

		pCell.setLiteralValue( 30 )

		self.assert_( cell1.getValue() == 30 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 30 )



	def testProxyCellChangedSignal(self):
		cell1 = IntCell( 1 )
		cell2 = IntCell( 2 )
		pCellTarget = CellRefCell( None, cell1 )
		pCell = ProxyCell( pCellTarget )

		pCell.changedSignal.connect( self.makeListener( 'changed' ) )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 2 )
		self.assert_( pCell.getValue() == 1 )
		self.assert_( self.received( 'changed' ) == 0 )

		pCellTarget.setLiteralValue( cell2 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 2 )
		self.assert_( pCell.getValue() == 2 )
		self.assert_( self.received( 'changed' ) == 1 )

		cell2.setLiteralValue( 20 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 20 )
		self.assert_( self.received( 'changed' ) == 2 )

		pCellTarget.setLiteralValue( cell1 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 1 )
		self.assert_( self.received( 'changed' ) == 3 )





	def testProxyCellChangedSignal2nd(self):
		cell1 = IntCell( 1 )
		atCell1 = CellRefCell( None, cell1 )
		cell2 = IntCell( 2 )
		atCell2 = CellRefCell( None, cell2 )
		pCellTarget = CellRefCell( None, cell2 )
		pCell = ProxyCell( atCell1 )

		pCell.changedSignal.connect( self.makeListener( 'changed' ) )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 2 )
		self.assert_( pCell.getValue() == 1 )
		self.assert_( self.received( 'changed' ) == 0 )

		pCell.targetSourceCell = atCell2

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 2 )
		self.assert_( pCell.getValue() == 2 )
		self.assert_( self.received( 'changed' ) == 1 )

		cell2.setLiteralValue( 20 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 20 )
		self.assert_( self.received( 'changed' ) == 2 )

		pCell.targetSourceCell = atCell1

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 1 )
		self.assert_( self.received( 'changed' ) == 3 )

		pCell.targetSourceCell = pCellTarget

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 20 )
		self.assert_( self.received( 'changed' ) == 4 )

		pCellTarget.setLiteralValue( cell1 )

		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 1 )
		self.assert_( self.received( 'changed' ) == 5 )

		pCell.setLiteralValue( 30 )

		self.assert_( cell1.getValue() == 30 )
		self.assert_( cell2.getValue() == 20 )
		self.assert_( pCell.getValue() == 30 )
		self.assert_( self.received( 'changed' ) == 6 )



	def testFunctionOfProxyCell(self):
		cell1 = IntCell( 1 )
		cell2 = IntCell( 3 )
		pCellTarget = CellRefCell( None, cell1 )
		pCell = ProxyCell( pCellTarget )
		fCell = IntCell( 0 )


		def f():
			return pCell.getValue() * 5

		fCell.setFunction( f )

		fCell.changedSignal.connect( self.makeListener( 'changed' ) )

		self.assert_( self.received( 'changed' ) == 0 )
		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 3 )
		self.assert_( pCell.getValue() == 1 )
		self.assert_( fCell.getValue() == 5 )

		self.assert_( fCell.getDependencies() == [ pCell ] )

		pCellTarget.setLiteralValue( cell2 )

		self.assert_( self.received( 'changed' ) == 1 )
		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 3 )
		self.assert_( pCell.getValue() == 3 )
		self.assert_( fCell.getValue() == 15 )


		pCell.setLiteralValue( 4 )

		self.assert_( self.received( 'changed' ) == 2 )
		self.assert_( cell1.getValue() == 1 )
		self.assert_( cell2.getValue() == 4 )
		self.assert_( pCell.getValue() == 4 )
		self.assert_( fCell.getValue() == 20 )






if __name__ == '__main__':
	unittest.main()

