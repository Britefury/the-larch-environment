##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.DocModel.DMListInterface import DMListInterface




class DMInterpreterEnv (object):
	def __init__(self, env={}):
		super( DMInterpreterEnv, self ).__init__()

		self._env = copy( env )


	def update(self, env):
		e2 = DMInterpreterEnv( self._env )
		e2._env.update( env )
		return e2

	def funcs(self, funcs):
		e2 = DMInterpreterEnv( self._env )
		for f in funcs:
			name = f.__name__
			if name.startswith( '_' ):
				name = name[1:]
			e2._env[name] = f
		return e2


	def dmEval(self, xs):
		assert isinstance( xs, DMListInterface )
		assert len( xs ) > 0, 'empty list'

		funcName = xs[0]

		try:
			func = self._env[funcName]
		except KeyError:
			raise KeyError, 'no function called %s' % ( funcName, )
		return func( self, *xs[1:] )





import unittest
from Britefury.DocModel.DMList import DMList



class TestCase_DMInterpreter (unittest.TestCase):
	def testInterp(self):
		env = DMInterpreterEnv()

		def _string(env, value):
			return value

		def _concat(env, *strings):
			res = ''
			for x in strings:
				res += env.dmEval( x )
			return res

		env = env.funcs( [ _string, _concat ] )

		program = DMList()
		program.append( 'concat' )
		program.append( DMList( [ 'string', 'a' ] ) )
		program.append( DMList( [ 'string', 'b' ] ) )
		program.append( DMList( [ 'string', 'c' ] ) )

		res = env.dmEval( program )

		self.assert_( res == 'abc' )



if __name__ == '__main__':
	unittest.main()
