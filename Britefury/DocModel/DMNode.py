##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************



class DMNode (object):
	__slots__ = []


	def __writesx__(self, stream, nodeToIndex):
		try:
			index = nodeToIndex[self]
			stream.write( '{%d}'  %  ( index, ) )
		except KeyError:
			self.__writecontentsx__( stream, nodeToIndex )
			nodeToIndex[self] = len( nodeToIndex )


	def __writecontentsx__(self, stream, nodeToIndex):
		pass


