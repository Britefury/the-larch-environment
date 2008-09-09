##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

def abstractmethod(method):
	def abstract(self, *args, **kwargs):
		raise TypeError, 'Method %s.%s is abstract'  %  ( type(self), method.__name__ )
	return abstract
