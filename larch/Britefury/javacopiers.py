##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
"""
Monkeypatch copiers for various Java objects.
"""

from java.awt import Color



def _immutable__copy__(self):
	"""
	__copy__ method for immutable Java objects
	"""
	return self

def _immutable_not_composite__deepcopy__(self, memo):
	"""
	__deepcopy__ method for immutable, non-composite Java objects (objects which do not contain others - e.g. simple objects such as java.awt.Color
	"""
	return self



def install_java_copiers():
	Color.__copy__ = _immutable__copy__
	Color.__deepcopy__ = _immutable_not_composite__deepcopy__
