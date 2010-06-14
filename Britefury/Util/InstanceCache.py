##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakValueDictionary


def instanceCache(cls, *constructorArgs):
	key = constructorArgs
	
	try:
		cache = cls.__instance_cache__
	except AttributeError:
		cache = WeakValueDictionary()
		cls.__instance_cache__ = cache
	
	try:
		return cache[key]
	except KeyError:
		instance = cls( *constructorArgs )
		cache[key] = instance
		return instance

	
