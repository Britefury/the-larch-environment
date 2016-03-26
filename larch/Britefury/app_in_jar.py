##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.util.jar import JarInputStream, JarOutputStream, Manifest
from java.util.zip import ZipEntry
from java.lang import String
from java.io import ByteArrayInputStream, ByteArrayOutputStream

from org.python.core.util import FileUtil

import collections
import jarray


_BYTES_BUFFER_SIZE = 8192


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
	"""
	Set the global JAR url from which Larch was started

	:param jarURL: the URL
	:return: None
	"""
	global _larchJarURL
	_larchJarURL = jarURL



def getLarchJarURL():
	"""
	Get the global JAR url from which Larch was started

	:return: the URL or None if Larch was not started from a JAR
	"""
	return _larchJarURL

def startedFromJar():
	"""
	Determine if Larch was started from a JAR

	:return: True if started from a JAR, False if started from the filesystem
	"""
	return _larchJarURL is not None



def scanLarchJar():
	if _larchJarURL is None:
		raise RuntimeError, 'Larch was not loaded from a JAR file'

	jar = JarInputStream( _larchJarURL.openStream() )

	entry = jar.getNextJarEntry()
	while entry is not None:
		name = entry.getName()

		for handler in _jarEntryHandlers:
			if handler.test_fn(name):
				def _reader():
					return FileUtil.readBytes(jar)

				handler.handle_fn(name, _reader)

		entry = jar.getNextJarEntry()



def buildLarchJar(outputStream, additionalFilesAsNameBytesPairs, filterFn=None, larchJarURL=None):
	"""
	Build a JAR from an existing Larch JAR, along with additional files to make a packaged program

	:param outputStream: The output stream to which the JAR is to be written
	:param additionalFilesAsNameBytesPairs: Additional files in the form of a sequence of tuples consisting of (path, bytes)
	:param filterFn: (optional) A filter function that can be used to exclude files from the existing Larch JAR; takes the form of function(name) -> boolean, should return True if file should be included
	:param larchJarURL: (optional) A URL at which the existing Larch JAR can be obtained. If None, it will use the JAR from which Larch was started. Raises an error if no URL provided and Larch was not started from a JAR
	"""
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

	for name, bytes in additionalFilesAsNameBytesPairs:
		size = len( bytes )
		entryOut = ZipEntry( name )
		entryOut.setSize( size )
		jarOut.putNextEntry( entryOut )
		jarOut.write( bytes, 0, size )
		jarOut.closeEntry()

	jarOut.finish()
