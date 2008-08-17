##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

"""
Define LiteralCell and its metaclass

Literal cells are restricted such that they can only contain a literal value.
Attempting to set the evaluator to anythin except a literal value will result in a TypeError being raised.
"""


from copy import *

import weakref

from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel.Mutability import *

#from Britefury.FileIO.IOXml import *

from Britefury.Cell.CellEvaluator import CellEvaluator
from Britefury.Cell.CellInterface import CellInterface



class CellEvaluationCycleError (Exception):
	pass






class LiteralCellClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		super( LiteralCellClass, cls ).__init__( clsName, clsBases, clsDict )

		# Ensure that @valueType is present
		if not clsDict.has_key( 'valueClass' ):
			raise AttributeError, 'valueClass not specified'
		else:
			valueClass = clsDict['valueClass']
			if valueClass is not None  and  issubclass( valueClass, CellEvaluator ):
				raise TypeError, 'valueClass of literal cell cannot be a subclass of CellEvaluator; it is \'%s\''  %  ( clsDict['valueClass'], )

		if not clsDict.has_key( 'bAllowNone' ):
			raise AttributeError, 'bAllowNone not specified'

		#ioObjectFactoryRegister( clsName, cls )




class LiteralCell (CellInterface):
	__slots__ = [ '_defaultValue', '_value', 'owner' ]

	#__metaclass__ = LiteralCellClass

	#__ioxml_can_delegate__ = True


	valueClass = None
	bAllowNone = True


	def __init__(self, defaultValue=None):
		super( LiteralCell, self ).__init__()

		self._defaultValue = defaultValue

		if not self._p_checkValueType( defaultValue ):
			raise TypeError, 'default value does not complie to value type restrictions (type \'%s\', none allowed = \'%s\')'  %  ( self.valueClass, self.bAllowNone )

		self._value = self._p_getDefaultValueForUse()

		self.owner = None



	def getEvaluator(self):
		return self._value

	def setEvaluator(self, evaluator):
		if isinstance( evaluator, CellEvaluator ):
			raise TypeError, 'literal cell cannot take CellEvaluator instances as the evaluator; use a literal value'
		self._p_setEvaluator( evaluator )


	def getLiteralValue(self):
		if isinstance( self._value, CellEvaluator ):
			return None
		else:
			return self._value

	def setLiteralValue(self, literal):
		if isinstance( literal, CellEvaluator ):
			raise TypeError, 'literal cell cannot take CellEvaluator instances as the evaluator; use a literal value'

		if not self._p_checkValueType( literal ):
			literal = self._p_getDefaultValueForUse()

		self._p_setEvaluator( literal )

	def isLiteral(self):
		return not isinstance( self._value, CellEvaluator )


	def getValue(self):
		self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED

		# Add @self to the global dependency list if it exists; this ensures that any cell that
		# is recomputing its value will know that the @value of self is required
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = None

		if isValueImmutable( self._value ):
			return self._value
		else:
			return copy( self._value )

	def getImmutableValue(self):
		self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED

		# Add @self to the global dependency list if it exists; this ensures that any cell that
		# is recomputing its value will know that the @value of self is required
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = None

		return self._value



	def isValid(self):
		return True



	def _p_setEvaluator(self, evaluator):
		oldEval = self._value
		self._value = evaluator
		self.evaluatorSignal.emit( oldEval, self._value )
		if self.owner is not None:
			self.owner._f_onCellEvaluator( self, oldEval, self._value )
		self._o_changed()








	def __readxml__(self, xmlNode):
		if xmlNode.isValid():
			evaluator = xmlNode.readObject()
			# Check type
			if not self._p_checkValueType( evaluator ):
				evaluator = self._p_getDefaultValueForUse()
			self._p_setEvaluator( evaluator )



	def __writexml__(self, xmlNode):
		if xmlNode.isValid():
			xmlNode  <<  self._value



	def __copy__(self):
		cell = self.__class__( self._defaultValue )
		cell._p_setEvaluator( copy( self._value ) )
		return cell


	def copyFrom(self, cell):
		self._p_setEvaluator( copy( cell._value ) )


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

	value = property( getValue )
	immutableValue = property( getImmutableValue )





#//////////////////////////////////////////////////////////////////////////////
#		LiteralRefCell - literal reference cell
#//////////////////////////////////////////////////////////////////////////////

class LiteralRefCell (LiteralCell):
	valueClass = None
	bAllowNone = True


	def getValue(self):
		self._refreshState = self.REFRESHSTATE_REFRESH_NOT_REQUIRED

		# Add @self to the global dependency list if it exists; this ensures that any cell that
		# is recomputing its value will know that the @value of self is required
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = None

		return self._value


	def __copy__(self):
		return self.__class__( self._defaultValue, self._value )


	def copyFrom(self, cell):
		self._p_setEvaluator( cell._evaluator )


	def _p_getDefaultValueForUse(self):
		return self._defaultValue


	value = property( getValue )





#//////////////////////////////////////////////////////////////////////////////
#		Primitive literal cell types
#//////////////////////////////////////////////////////////////////////////////

class LiteralBoolCell (LiteralCell):
	valueClass = bool
	bAllowNone = False

class LiteralIntCell (LiteralCell):
	valueClass = int
	bAllowNone = False

class LiteralFloatCell (LiteralCell):
	valueClass = float
	bAllowNone = False

class LiteralStringCell (LiteralCell):
	valueClass = str
	bAllowNone = False





import unittest


class _CellValue (object):
	def __init__(self, x):
		self.x = x


class _TestLiteralCell (LiteralCell):
	valueClass = _CellValue
	bAllowNone = False


class TestCase_LiteralCell (unittest.TestCase):
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
		cell = LiteralIntCell( 1 )
		self.assert_( cell.isValid() )


	def testSimpleValue(self):
		cell = LiteralIntCell( 1 )

		self.assert_( cell.value == 1 )
		self.assert_( cell.immutableValue == 1 )
		self.assert_( cell._value == 1 )

		cell.literalValue = 20

		self.assert_( cell.value == 20 )
		self.assert_( cell.immutableValue == 20 )
		self.assert_( cell._value == 20 )


	def testMutable(self):
		cell = _TestLiteralCell( _CellValue( 1 ) )

		self.assert_( cell.value.x == 1 )
		self.assert_( cell.immutableValue.x == 1 )
		self.assert_( cell.value is not cell.immutableValue )

		cell.literalValue = _CellValue( 20 )

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
		cell = LiteralIntCell( 1 )

		self.assert_( cell._value == 1 )

		cell.literalValue = 20

		self.assert_( cell._value == 20 )


	def testEvaluatorSignal(self):
		self.assert_( self.received( 'evaluator' ) == 0 )

		cell = LiteralIntCell( 1 )
		cell.evaluatorSignal.connect( self.makeListener( 'evaluator' ) )
		cell.literalValue = 20
		self.assert_( self.received( 'evaluator' ) == 1 )


	def testChangedSignal(self):
		self.assert_( self.received( 'changed' ) == 0 )

		cell = LiteralIntCell( 1 )
		cell.changedSignal.connect( self.makeListener( 'changed' ) )
		self.assert_( cell.value == 1 )
		cell.literalValue = 20
		self.assert_( self.received( 'changed' ) == 1 )


	def testXml(self):
		cell1 = LiteralIntCell( 20 )

		cell2 = LiteralIntCell( 1 )

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
		cell1 = LiteralIntCell( 20 )

		self.assert_( cell1.value == 20 )

		cell2 = copy( cell1 )

		self.assert_( cell2.value == 20 )



	def testCopyFrom(self):
		cell1 = LiteralIntCell( 20 )
		cell2 = LiteralIntCell( 30 )

		self.assert_( cell1.value == 20 )
		self.assert_( cell2.value == 30 )

		cell2.copyFrom( cell1 )

		self.assert_( cell2.value == 20 )




if __name__ == '__main__':
	unittest.main()


