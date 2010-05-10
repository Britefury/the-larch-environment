##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


class DerivedAttributeMethod (object):
	class _Helper (object):
		__slots__ = [ '_table', '_attr' ]
		
		def __init__(self, table, attr):
			self._table = table
			self._attr = attr
			
		def __call__(self):
			try:
				derivedAttrs = self._table.__derivedAttributes__
			except AttributeError:
				derivedAttrs = {}
				self._table.__derivedAttributes__ = derivedAttrs
				
			try:
				return derivedAttrs[self._attr]
			except KeyError:
				result = self._attr._method.__call__( self._table )
				derivedAttrs[self._attr] = result
				return result
	
	
	def __init__(self, method):
		self._method = method
	
	def __get__(self, instance, owner):
		if instance is not None:
			return self._Helper( instance, self )
		else:
			return self

