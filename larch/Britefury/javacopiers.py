##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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
