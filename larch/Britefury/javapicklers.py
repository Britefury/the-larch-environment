##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
"""
Monkeypatch picklers for various Java objects.
"""
from java.lang import Enum
from java.awt import Color




# Probably doesn't work : will a reference to a Java class save?

#def _install_getstate_and_setstate(cls, getstate, setstate):
#	def __reduce__(self):
#		return cls, (), self.__getstate__()
#
#	cls.__getstate__ = getstate
#	cls.__setstate__ = setstate
#	cls.__reduce__ = __reduce__



def _install_immutable_getstate_and_factory(cls, getstate, factory):
	def __reduce__(self):
		return factory, getstate(self)

	cls.__reduce__ = __reduce__



def _Enum__getstate__(self):
	return (type(self), str(self))

def _Enum_factory(enumClass, asString):
	return enumClass.valueOf(asString)

def _Color__getstate__(self):
	return self.red, self.green, self.blue

def _Color_factory(r, g, b):
	return Color( r, g, b )


def install_java_picklers():
	_install_immutable_getstate_and_factory( Color, _Color__getstate__, _Color_factory )
	_install_immutable_getstate_and_factory( Enum, _Enum__getstate__, _Enum_factory )
