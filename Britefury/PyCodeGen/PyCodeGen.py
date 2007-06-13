##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import *


class PyCodeBlock (object):
	def __init__(self, lines=[]):
		self._lines = copy( lines )


	def append(self, line):
		self._lines.append( line )


	def extend(self, block):
		self._lines.extend( block._lines )


	def indent(self):
		self._lines = [ '\t' + line   for line in self._lines ]


	def asText(self):
		return ''.join( [ line + '\n'   for line in self._lines ] )


	def __iadd__(self, block):
		self.extend( block )
		return self

	def __add__(self, block):
		return PyCodeBlock( self._lines + block._lines )