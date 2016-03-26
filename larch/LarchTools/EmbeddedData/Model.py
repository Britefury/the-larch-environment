##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.ObjectPres import ObjectBox
from BritefuryJ.Live import LiveValue

__author__ = 'Geoff'


class Model (object):
	def __init__(self, value=None):
		self._live = LiveValue( value )


	def __getValue(self):
		return self._live.getValue()

	def __setValue(self, value):
		self._live.setLiteralValue(value)

	value = property(__getValue, __setValue)


	@property
	def liveValue(self):
		return self._live



	def __getstate__(self):
		return { 'value' : self._live.getValue() }


	def __setstate__(self, state):
		value = state['value']
		self._live = LiveValue( value )



	def _addListener(self, listener):
		self._live.addListener( listener )

	def _removeListener(self, listener):
		self._live.removeListener( listener )



	def __present__(self, fragment, inheritedState):
		return ObjectBox( 'LarchTools.EmbeddedData.Model', self._live )

