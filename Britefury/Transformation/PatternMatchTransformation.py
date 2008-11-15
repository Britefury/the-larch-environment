##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.Dispatch import DispatchError


class PatternMatchTransformation (object):
	def __init__(self, matchExpression):
		self._matchExpression = matchExpression
		
	def __call__(self, node, xform):
		res = self._matchExpression.parseNode( node, xform )
		if res.isValid():
			return res.getValue()
		else:
			raise DispatchError
	