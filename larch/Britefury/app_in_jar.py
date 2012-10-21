##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.util.jar import JarInputStream
from java.lang import String
from java.io import ByteArrayInputStream

import collections
import jarray


_jarEntryHandlers = []
_larchJarURL = None


_JarEntryHandler = collections.namedtuple('JarEntryHandler', ['test_fn', 'handle_fn'])


def registerJarEntryHandler(name_test_fn, handle_fn):
	"""
	Register a JAR entry handler

	name_test_fn - function(name) -> boolean; True if we want the hanlder to be invoked

	handle_fn - function(name, reader_fn)      reader_fn - function() -> java.io.ByteArrayInputStream
	"""
	_jarEntryHandlers.append( _JarEntryHandler( name_test_fn, handle_fn ) )



def setLarchJarURL(jarURL):
	global _larchJarURL
	_larchJarURL = jarURL



def getLarchJarURL():
	return _larchJarURL

def startedFromJar():
	return _larchJarURL is not None



def scanLarchJar():
	if _larchJarURL is None:
		raise RuntimeError, 'Larch was not loaded from a JAR file'

	jar = JarInputStream( _larchJarURL.openStream() )

	pluginNames = []
	entry = jar.getNextJarEntry()
	while entry is not None:
		name = entry.getName()

		for handler in _jarEntryHandlers:
			if handler.test_fn(name):
				def _reader():
					size = entry.getSize()
					bytes = jarray.zeros( size, 'b' )
					pos = 0
					while pos < size:
						bytesRead = jar.read( bytes, pos, size - pos )
						if bytesRead == -1:
							break
						pos += bytesRead
					return ByteArrayInputStream( bytes )

				handler.handle_fn(name, _reader)

		entry = jar.getNextJarEntry()

