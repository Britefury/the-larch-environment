##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************


def abstractmethod(method):
	def abstract(self, *args, **kwargs):
		raise TypeError, 'Method %s.%s is abstract'  %  ( type(self), method.__name__ )
	return abstract



class abstractproperty (object):
	def __get__(self, obj, objtype):
		raise TypeError, 'Cannot get value of abstract property'

	def __set__(self, obj, value):
		raise TypeError, 'Cannot set value of abstract property'

	def __delete__(self, obj):
		raise TypeError, 'Cannot delete value of abstract property'
