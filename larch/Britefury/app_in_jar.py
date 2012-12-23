##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.util.jar import JarInputStream, JarOutputStream, Manifest
from java.util.zip import ZipEntry
from java.lang import String
from java.io import ByteArrayInputStream, ByteArrayOutputStream

import collections
import jarray


_BYTES_BUFFER_SIZE = 16384


_jarEntryHandlers = []
_larchJarURL = None


_JarEntryHandler = collections.namedtuple('JarEntryHandler', ['test_fn', 'handle_fn'])


def registerJarEntryHandler(name_test_fn, handle_fn):
	"""
	Register a JAR entry handler

	name_test_fn - function(name) -> boolean; True if we want the handler to be invoked

	handle_fn - function(name, reader_fn)      reader_fn - function() -> byte[]
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

	bytesBuffer = jarray.zeros( _BYTES_BUFFER_SIZE, 'b' )

	entry = jar.getNextJarEntry()
	while entry is not None:
		name = entry.getName()

		for handler in _jarEntryHandlers:
			if handler.test_fn(name):
				def _reader():
					stream = ByteArrayOutputStream()
					while True:
						bytesRead = jar.read( bytesBuffer, 0, _BYTES_BUFFER_SIZE )
						if bytesRead == -1:
							break
						stream.write( bytesBuffer, 0, bytesRead )
					return stream.toByteArray()

				handler.handle_fn(name, _reader)

		entry = jar.getNextJarEntry()



def buildLarchJar(outputStream, additionalNameBytesPairs, filterFn=None, larchJarURL=None):
	if larchJarURL is None:
		larchJarURL = _larchJarURL

	if larchJarURL is None:
		raise RuntimeError, 'Larch was not loaded from a JAR file and no Larch JAR file was provided'

	jarIn = JarInputStream( larchJarURL.openStream() )

	manifestIn = jarIn.getManifest()
	manifestOut = Manifest( manifestIn )

	jarOut = JarOutputStream( outputStream, manifestOut )

	bytesBuffer = jarray.zeros( _BYTES_BUFFER_SIZE, 'b' )

	entryIn = jarIn.getNextJarEntry()
	while entryIn is not None:
		name = entryIn.getName()

		if filterFn is None  or  filterFn( name ):
			bufferStream = ByteArrayOutputStream()
			while True:
				bytesRead = jarIn.read( bytesBuffer, 0, _BYTES_BUFFER_SIZE )
				if bytesRead == -1:
					break
				bufferStream.write( bytesBuffer, 0, bytesRead )


			entryOut = ZipEntry( name )
			entryOut.setSize( bufferStream.size() )
			jarOut.putNextEntry( entryOut )
			bufferStream.writeTo( jarOut )
			jarOut.closeEntry()

		entryIn = jarIn.getNextJarEntry()

	for name, bytes in additionalNameBytesPairs:
		size = len( bytes )
		entryOut = ZipEntry( name )
		entryOut.setSize( size )
		jarOut.putNextEntry( entryOut )
		jarOut.write( bytes, 0, size )
		jarOut.closeEntry()

	jarOut.finish()
