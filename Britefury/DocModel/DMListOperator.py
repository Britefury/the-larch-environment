##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel.DMList import DMList





class DMListOperator (object):
	__slots__ = [ '_layer' ]

	def __init__(self, layer):
		self._layer = layer


	def _p_dest(self, x):
		if isinstance( x, DMListInterface ):
			try:
				return self._layer.getDestList( x )
			except ValueError:
				pass
		return self._p_srcToDest( x )


	def _p_src(self, x):
		if isinstance( x, DMListInterface ):
			try:
				return self._layer.getSrcList( x )
			except ValueError:
				pass
		return self._p_destToSrc( x )


	def _p_srcToDest(self, x):
		return x

	def _p_destToSrc(self, x):
		return x



	@abstractmethod
	def evaluate(self):
		pass



	@abstractmethod
	def append(self, x):
		pass

	@abstractmethod
	def extend(self, xs):
		pass

	@abstractmethod
	def insert(self, i, x):
		pass

	@abstractmethod
	def insertBefore(self, before, x):
		pass

	@abstractmethod
	def insertAfter(self, after, x):
		pass

	@abstractmethod
	def remove(self, x):
		pass

	@abstractmethod
	def replace(self, a, x):
		pass

	@abstractmethod
	def replaceRange(self, a, b, xs):
		"""Replaces the range (a,b) inclusive with the contents of xs"""
		pass

	@abstractmethod
	def __setitem__(self, i, x):
		pass

	@abstractmethod
	def __delitem__(self, i):
		pass


	def __getitem__(self, i):
		return self.evaluate()[i]


	@abstractmethod
	def __len__(self):
		pass










import unittest



class TestCase_DMListOperator_base (unittest.TestCase):
	def _p_testCaseParam(self, operationFunc, opDescription, makeLayerListFunc, expectedValueFunc, expectedLiteralValueFunc):
		"""
		operationFunc :				f( xs )
		"""
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMList()
		x.extend( range( 0, 10 ) )

		y = makeLayerListFunc( layer2, x )

		expectedError = None
		expectedErrorClass = None
		error = None
		errorClass = None

		testList = y[:]
		try:
			operationFunc( testList )
		except Exception, e:
			expectedError = e
			expectedErrorClass = e.__class__

		try:
			operationFunc( y )
		except Exception, e:
			error = e
			errorClass = e.__class__

		self.assert_( expectedErrorClass == errorClass, ( opDescription, expectedError, error ) )

		if error is None:
			expectedValue = expectedValueFunc( testList )
			expectedLiteralValue = expectedLiteralValueFunc( testList )

			self.assert_( x[:] == expectedLiteralValue, ( opDescription, x[:], y[:], testList, expectedLiteralValue, expectedValue ) )
			self.assert_( y[:] == expectedValue, ( opDescription, x[:], y[:], testList, expectedLiteralValue, expectedValue ) )




	def _p_testCase(self, operationFunc, opDescription):
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerList, self._p_expectedValue, self._p_expectedLiteralValue )





	def _p_testCaseCheckInPlaceParam(self, x, y, operationFunc, opDescription, expectedValueFunc, expectedLiteralValueFunc):
		"""
		operationFunc :				f( xs )
		"""
		expectedError = None
		expectedErrorClass = None
		error = None
		errorClass = None

		testList = y[:]
		try:
			operationFunc( testList )
		except Exception, e:
			expectedError = e
			expectedErrorClass = e.__class__

		try:
			operationFunc( y )
		except Exception, e:
			error = e
			errorClass = e.__class__

		self.assert_( expectedErrorClass == errorClass, ( opDescription, expectedError, error ) )

		if error is None:
			expectedValue = expectedValueFunc( testList )
			expectedLiteralValue = expectedLiteralValueFunc( testList )

			self.assert_( x[:] == expectedLiteralValue, ( opDescription, x[:], y[:], testList, expectedLiteralValue, expectedValue ) )
			self.assert_( y[:] == expectedValue, ( opDescription, x[:], y[:], testList, expectedLiteralValue, expectedValue ) )




	def _p_testCaseCheckInPlace(self, x, y, operationFunc, opDescription):
		self._p_testCaseCheckInPlaceParam( x, y, operationFunc, opDescription, self._p_expectedValue, self._p_expectedLiteralValue )


