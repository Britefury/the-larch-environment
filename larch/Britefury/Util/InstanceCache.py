##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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

	
