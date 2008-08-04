##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import *

import weakref
import gc

from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel.Mutability import *

from Britefury.FileIO.IOXml import *

from Britefury.Cell.CellEvaluator import CellEvaluator, CellEvaluationError
from Britefury.Cell.CellEvaluatorFunction import CellEvaluatorFunction
from Britefury.Cell.CellInterface import CellInterface, CellOwner



class CellEvaluationCycleError (Exception):
	pass






class CellClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		super( CellClass, cls ).__init__( clsName, clsBases, clsDict )

		# Ensure that @valueType is present
		if not clsDict.has_key( 'valueClass' ):
			raise AttributeError, 'valueClass not specified'

		if not clsDict.has_key( 'bAllowNone' ):
			raise AttributeError, 'bAllowNone not specified'

		ioObjectFactoryRegister( clsName, cls )


class Cell (CellInterface):
	__slots__ = [ '_defaultValue', '_evaluator', '_valueCache', '_dependencies', '_cycleLock', 'owner' ]

	__metaclass__ = CellClass

	__ioxml_can_delegate__ = True


	valueClass = None
	bAllowNone = True


	def __init__(self, defaultValue=None):
		super( Cell, self ).__init__()

		self._defaultValue = defaultValue

		if not self._p_checkValueType( defaultValue ):
			raise TypeError, 'default value does not comply to value type restrictions (type \'%s\', none allowed = \'%s\')'  %  ( self.valueClass, self.bAllowNone )

		self._evaluator = self._p_getDefaultValueForUse()
		self._valueCache = None

		self._dependencies = None
		self._cycleLock = False

		self.owner = None



	def getEvaluator(self):
		return self._evaluator

	def setEvaluator(self, evaluator):
		self._p_setEvaluator( evaluator )


	def getLiteralValue(self):
		if isinstance( self._evaluator, CellEvaluator ):
			return None
		else:
			return self._evaluator

	def setLiteralValue(self, literal):
		if not self._p_checkValueType( literal ):
			literal = self._p_getDefaultValueForUse()

		self._p_setEvaluator( literal )

	def isLiteral(self):
		return not isinstance( self._evaluator, CellEvaluator )


	def setFunction(self, function):
		self._p_setEvaluator( CellEvaluatorFunction( function ) )


	def getValue(self):
		self._p_refreshValue()
		if isValueImmutable( self._valueCache ):
			return self._valueCache
		else:
			return copy( self._valueCache )

	def getImmutableValue(self):
		self._p_refreshValue()
		return self._valueCache



	def isValid(self):
		return True



	def _p_setEvaluator(self, evaluator):
		oldEval = self._evaluator
		self._evaluator = evaluator
		self.evaluatorSignal.emit( oldEval, self._evaluator )
		if self.owner is not None:
			self.owner._f_onCellEvaluator( self, oldEval, self._evaluator )
		self._o_changed()






	def _p_refreshValue(self):
		if self._cycleLock:
			raise CellEvaluationCycleError, 'cell evaluation: cycle encountered'
		self._cycleLock = True

		try:
			# Add @self to the global dependency list if it exists; this ensures that any cell that
			# is recomputing its value will know that the @value of self is required
			if CellInterface._cellAccessList is not None:
				CellInterface._cellAccessList[self] = None

			if self._refreshState  !=  self.REFRESHSTATE_REFRESH_NOT_REQUIRED:
				# IS THIS CORRECT
				self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED
				if isinstance( self._evaluator, CellEvaluator ):
					# Push a new cell access list
					oldCellAccesses = CellInterface.pushNewAccessList()
					try:
						self._valueCache = self._evaluator.evaluate( self )
					except CellEvaluationError:
						self._valueCache = self._p_getDefaultValueForUse()
					# Restore the existing cell access list
					deps = CellInterface.popAccessList( oldCellAccesses )

					if not self._p_checkValueType( self._valueCache ):
						self._valueCache = self._p_getDefaultValueForUse()

					if self._dependencies is None:
						self._dependencies = deps

						# Process the dependencies that are being added
						for cell in deps:
							cell._dependents[self] = 0
					else:
						oldDeps = self._dependencies

						# Process the dependencies that are being removed
						for cell in oldDeps:
							if cell not in deps:
								cell._dependents.pop( self, None )

						# Process the dependencies that are being added
						for cell in deps:
							if cell not in oldDeps:
								cell._dependents[self] = 0

						self._dependencies = deps
				else:
					self._valueCache = self._evaluator

					# No dependencies
					if self._dependencies is not None:
						for cell in self._dependencies:
							cell._dependents.pop( self, None )

						self._dependencies.clear()


				# IS THIS CORRECT?
				#self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED

		except:
			self._cycleLock = False
			raise

		self._cycleLock = False



	def dependsOn(self, cell):
		if self._dependencies is None:
			return False
		else:
			return cell in self._dependencies


	def getDependencies(self):
		if self._dependencies is None:
			return []
		else:
			return self._dependencies.keys()







	def __readxml__(self, xmlNode):
		if xmlNode.isValid():
			evalNode = xmlNode

			if xmlNode.getTypeProperty() == 'Cell':
				evalNode = xmlNode.getChild( 'evaluator' )

			if evalNode.isValid():
				bCellEvaluatorNotSerialisable = ioXmlReadBoolProp( evalNode.property( 'b_cell_evaluator_not_serialisable' ) )

				if not bCellEvaluatorNotSerialisable:
					bCellEvaluatorProcedural = ioXmlReadBoolProp( evalNode.property( 'b_cell_evaluator_procedural' ) )

					evaluator = evalNode.readObject()
					if not bCellEvaluatorProcedural:
						# Literal; check type
						if not self._p_checkValueType( evaluator ):
							evaluator = self._p_getDefaultValueForUse()
					self._p_setEvaluator( evaluator )



	def __writexml__(self, xmlNode):
		if xmlNode.isValid():
			# Record if the cell value is procedural (not literal), and not serialisable
			if isinstance( self._evaluator, CellEvaluator ):
				ioXmlWriteBoolProp( xmlNode.property( 'b_cell_evaluator_procedural' ), True )

				if not self._evaluator.bSerialisable:
					ioXmlWriteBoolProp( xmlNode.property( 'b_cell_evaluator_not_serialisable' ), True )

			xmlNode  <<  self._evaluator



	def __copy__(self):
		cell = self.__class__( self._defaultValue )
		cell._p_setEvaluator( copy( self._evaluator ) )
		return cell


	def copyFrom(self, cell):
		self._p_setEvaluator( copy( cell._evaluator ) )


	def _p_checkValueType(self, value):
		if self.valueClass is not None:
			return isinstance( value, self.valueClass )  or  ( value is None  and  self.bAllowNone )
		else:
			return True


	def _p_getDefaultValueForUse(self):
		return copy( self._defaultValue )



	bValid = property( isValid, None )

	evaluator = property( getEvaluator, setEvaluator )
	literalValue = property( getLiteralValue, setLiteralValue )
	bLiteral = property( isLiteral )
	function = property( None, setFunction )

	value = property( getValue )
	immutableValue = property( getImmutableValue )

	dependencies = property( getDependencies )






#//////////////////////////////////////////////////////////////////////////////
#			RefCell - reference cell
#//////////////////////////////////////////////////////////////////////////////

class RefCell (Cell):
	valueClass = None
	bAllowNone = True


	def getValue(self):
		self._p_refreshValue()
		return self._valueCache


	def __copy__(self):
		cell = self.__class__( self._defaultValue )
		if self.bLiteral:
			cell._p_setEvaluator( self._evaluator )
		else:
			cell._p_setEvaluator( copy( self._evaluator ) )
		return cell


	def copyFrom(self, cell):
		if cell.bLiteral:
			self._p_setEvaluator( cell._evaluator )
		else:
			self._p_setEvaluator( copy( cell._evaluator ) )


	def _p_getDefaultValueForUse(self):
		return self._defaultValue



	value = property( getValue )



#//////////////////////////////////////////////////////////////////////////////
#			Primitive cell types
#//////////////////////////////////////////////////////////////////////////////

class BoolCell (Cell):
	valueClass = bool
	bAllowNone = False

class IntCell (Cell):
	valueClass = int
	bAllowNone = False

class FloatCell (Cell):
	valueClass = float
	bAllowNone = False

class StringCell (Cell):
	valueClass = str
	bAllowNone = False



# Cell reference type
class CellRefCell (RefCell):
	valueClass = CellInterface
	bAllowNone = True


	def __init__(self, defaultValue=None, initialValue=None):
		super( CellRefCell, self ).__init__( defaultValue )

		if initialValue is not None:
			self.setEvaluator( initialValue )









import unittest


class _TestCellValue (object):
	def __init__(self, x):
		self.x = x


class _TestCell (Cell):
	valueClass = _TestCellValue
	bAllowNone = False


class TestCase_Cell (unittest.TestCase):
	def setUp(self):
		self._sigs = {}

	def tearDown(self):
		self._sigs = {}

	def received(self, name):
		return self._sigs.get( name, 0 )

	def makeListener(self, name):
		def listener(*args, **kwargs):
			self._sigs[name] = self._sigs.get( name, 0 ) + 1
		return listener


	def testValidity(self):
		cell = IntCell( 1 )
		self.assert_( cell.isValid() )


	def testSimpleValue(self):
		cell = IntCell( 1 )

		self.assert_( cell.value == 1 )
		self.assert_( cell.immutableValue == 1 )
		self.assert_( cell._evaluator == 1 )

		cell.literalValue = 20

		self.assert_( cell.value == 20 )
		self.assert_( cell.immutableValue == 20 )
		self.assert_( cell._evaluator == 20 )


	def testMutable(self):
		cell = _TestCell( _TestCellValue( 1 ) )

		self.assert_( cell.value.x == 1 )
		self.assert_( cell.immutableValue.x == 1 )
		self.assert_( cell.value is not cell.immutableValue )

		cell.literalValue = _TestCellValue( 20 )

		self.assert_( cell.value.x == 20 )
		self.assert_( cell.immutableValue.x == 20 )
		self.assert_( cell.value is not cell.immutableValue )

		cell.value.x = 50

		self.assert_( cell.value.x == 20 )
		self.assert_( cell.immutableValue.x == 20 )

		cell.value.x = 50

		self.assert_( cell.value.x == 20 )
		self.assert_( cell.immutableValue.x == 20 )


	def testLiteral(self):
		cell = IntCell( 1 )

		self.assert_( cell._evaluator == 1 )

		cell.literalValue = 20

		self.assert_( cell._evaluator == 20 )


	def testEvaluatorSignal(self):
		self.assert_( self.received( 'evaluator' ) == 0 )

		cell = IntCell( 1 )
		cell.evaluatorSignal.connect( self.makeListener( 'evaluator' ) )
		cell.literalValue = 20
		self.assert_( self.received( 'evaluator' ) == 1 )


	def testChangedSignal(self):
		self.assert_( self.received( 'changed' ) == 0 )

		cell = IntCell( 1 )
		cell.changedSignal.connect( self.makeListener( 'changed' ) )
		self.assert_( cell.value == 1 )
		cell.literalValue = 20
		self.assert_( self.received( 'changed' ) == 1 )


	def testFunction(self):
		def f():
			return 20
		cell = IntCell( 1 )
		cell.function = f

		self.assert_( cell.value == 20 )
		self.assert_( cell._evaluator._function is f )


	def testCache(self):
		callCount = [ 0 ]

		def f():
			callCount[0] += 1
			return cell1.value * 3

		cell1 = IntCell( 1 )
		cell2 = IntCell( 0 )
		cell2.function = f

		self.assert_( callCount[0] == 0 )
		self.assert_( cell1.value == 1 )
		self.assert_( cell2.value == 3 )
		self.assert_( callCount[0] == 1 )
		self.assert_( cell2.value == 3 )
		self.assert_( callCount[0] == 1 )
		cell1.literalValue = 12
		self.assert_( cell2.value == 36 )
		self.assert_( callCount[0] == 2 )
		self.assert_( cell2.value == 36 )
		self.assert_( callCount[0] == 2 )



	def testChangedSignalWithFunctions(self):
		self.assert_( self.received( 'changed' ) == 0 )

		def f():
			return cell1.value * 3

		def g():
			return cell2.value * 2

		cell1 = IntCell( 1 )
		cell2 = IntCell( 0 )
		cell3 = IntCell( 0 )
		cell2.function = f
		cell3.function = g
		cell3.changedSignal.connect( self.makeListener( 'changed' ) )

		self.assert_( cell1.value == 1 )
		self.assert_( cell2.value == 3 )
		self.assert_( cell3.value == 6 )
		self.assert_( self.received( 'changed' ) == 0 )
		cell1.literalValue = 12
		self.assert_( cell1.value == 12 )
		self.assert_( cell2.value == 36 )
		self.assert_( cell3.value == 72 )
		self.assert_( self.received( 'changed' ) == 1 )
		cell3.function = f
		self.assert_( cell1.value == 12 )
		self.assert_( cell2.value == 36 )
		self.assert_( cell3.value == 36 )
		self.assert_( self.received( 'changed' ) == 2 )




	def testDeps(self):
		cell1 = IntCell( 1 )
		cell2 = IntCell( 2 )
		cellB = IntCell( 0 )


		def f():
			return cell1.value * 3

		def g():
			return cell2.value * 4

		cellB.function = f
		self.assert_( cellB._evaluator._function is f )

		self.assert_( cell1.value == 1 )
		self.assert_( cellB.value == 3 )
		self.assert_( cellB.dependencies ==  [ cell1 ] )
		self.assert_( cell1.dependents == [ cellB ] )
		self.assert_( cell2.dependents == [] )

		cell1.literalValue = 7

		self.assert_( cell1.value == 7 )
		self.assert_( cellB.value == 21 )
		self.assert_( cellB.value == 21 )
		self.assert_( cellB.dependencies == [ cell1 ] )
		self.assert_( cell1.dependents == [ cellB ] )
		self.assert_( cell2.dependents == [] )

		cellB.function = g

		self.assert_( cellB.value == 8 )
		cell2.literalValue = 12
		self.assert_( cellB.value == 48 )
		cell1.literalValue = 5
		self.assert_( cellB.value == 48 )
		self.assert_( cellB.dependencies == [ cell2 ] )
		self.assert_( cell1.dependents == [] )
		self.assert_( cell2.dependents == [ cellB ] )



		cell2.function = f

		self.assert_( cell1.value == 5 )
		self.assert_( cell2.value == 15 )
		self.assert_( cellB.value == 60 )
		self.assert_( cellB.dependencies == [ cell2 ] )
		self.assert_( cell2.dependencies == [ cell1 ] )
		self.assert_( cell1.dependents == [ cell2 ] )
		self.assert_( cell2.dependents == [ cellB ] )
		cell1.literalValue = 3
		self.assert_( cell1.value == 3 )
		self.assert_( cell2.value == 9 )
		self.assert_( cellB.value == 36 )
		self.assert_( cellB.dependencies == [ cell2 ] )
		self.assert_( cell2.dependencies == [ cell1 ] )
		self.assert_( cell1.dependents == [ cell2 ] )
		self.assert_( cell2.dependents == [ cellB ] )


	def testXml(self):
		cell1 = IntCell( 20 )

		cell2 = IntCell( 1 )

		docOut = OutputXmlDocument()
		docOut.getContentNode()  <<  cell1
		xml = docOut.writeString()

		docIn = InputXmlDocument()
		docIn.parse( xml )

		docIn.getContentNode()  >>  cell2

		self.assert_( cell2.value == 20 )

		docOut2 = OutputXmlDocument()
		docOut2.getContentNode()  <<  cell2
		xml2 = docOut2.writeString()

		self.assert_( xml == xml2 )


	def testCopy(self):
		cell1 = IntCell( 20 )

		self.assert_( cell1.value == 20 )

		cell2 = copy( cell1 )

		self.assert_( cell2.value == 20 )



	def testCopyFrom(self):
		cell1 = IntCell( 20 )
		cell2 = IntCell( 30 )

		self.assert_( cell1.value == 20 )
		self.assert_( cell2.value == 30 )

		cell2.copyFrom( cell1 )

		self.assert_( cell2.value == 20 )



	def testGC(self):
		cell1 = IntCell( 13 )
		cell2 = Cell( 1 )


		def c2f():
			return cell1.value * 20

		cell2.function = c2f


		self.assert_( cell1.value == 13 )
		self.assert_( cell2.value == 260 )
		self.assert_( cell2.dependencies == [ cell1 ] )
		self.assert_( cell1.dependents == [ cell2 ] )

		del cell2

		gc.collect()

		self.assert_( cell1.dependents == [] )




if __name__ == '__main__':
	unittest.main()


