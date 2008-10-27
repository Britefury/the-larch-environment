##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from Britefury.Dispatch.MethodDispatch import methodDispatch


class TransformationInterface (object):
	def __call__(self, node, xform):
		return methodDispatch( self, node, xform )
	
	def __apply__(self, node):
		return methodDispatch( self, node, self.__apply__ )
