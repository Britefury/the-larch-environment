##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys
import types
from copy import copy

from Britefury.DocModel.DMListInterface import DMListInterface


class DMINameError (Exception):
	pass

class DMIMethodError (Exception):
	pass




_dmiMethodNameMap = {
	'+' : '__add__',
	'-' : '__sub__',
	'*' : '__mul__',
	'/' : '__div__',
	'%' : '__mod__',
	'**' : '__pow__',
	'<<' : '__lshift__',
	'>>' : '__rshift__',
	'&' : '__and__',
	'|' : '__or__',
	'^' : '__xor__',
	'[]' : '__getitem__',
	'[]=' : '__setitem__',
	'new' : lambda x, args: x( *args ),
	'<' : lambda x, args: x < args[0],
	'<=' : lambda x,args: x <= args[0],
	'==' : lambda x, args: x ==args[0],
	'!=' : lambda x, args: x !=args[0],
	'>' : lambda x, args: x > args[0],
	'>=' : lambda x, args: x >= args[0],
	}




class specialform (object):
	def __init__(self, f):
		self._f = f
	
	def invoke(self, obj, env, xs):
		return self._f( obj, env, xs )
	






class DMInterpreterEnv (object):
	def __init__(self, **env):
		self._env = copy( env )
		
		
	def _p_interpretLiteral(self, xs):
		if xs[0] == '@':
			varName = xs[1:]
			try:
				return self._env[varName]
			except KeyError:
				raise DMINameError, '%s not bound'  %  ( varName, )
		elif xs[0] == '#':
			value = xs[1:]
			if value == 'False':
				return False
			elif value == 'True':
				return True
			elif value == 'None':
				return None
			elif value.isdigit():
				return int( value )
			else:
				return None
		else:
			return xs
		
		
		
	def dmEval(self, xs):
		if xs is None:
			return None
		elif isinstance( xs, DMListInterface ):
			x0 = xs[0]
			if x0 == '=':
				# Assignment
				target = xs[1]
				value = xs[2]
				if target[0] != '@':
					raise DMINameError, 'var name %s must start with @ in %s'  %  ( target, xs )
				target = target[1:]
				if isinstance( value, str ):
					self._env[target] = value
				else:
					self._env[target] = DMInterpreterEnv( **self._env ).dmEval( value )
				return None
			else:
				if isinstance( x0, DMListInterface ):
					target = DMInterpreterEnv( **self._env ).dmEval( x0 )
				else:
					target = self._p_interpretLiteral( x0 )
				
				methodName = xs[1]
				methodName = _dmiMethodNameMap.get( methodName, methodName )
				
				if isinstance( methodName, str ):
					try:
						method = getattr( target, methodName )
					except AttributeError:
						raise DMIMethodError, '%s has no method %s, in %s' % ( target, methodName, xs )
				elif isinstance( methodName, types.LambdaType ):
					args = [ DMInterpreterEnv( **self._env ).dmEval( dmarg )   for dmarg in xs[2:] ]
					try:
						return methodName( target, args )
					except Exception:
						print '*** Internal error in %s'  %  ( xs, )
						raise
				else:
					raise TypeError, 'methodName is invalid in %s'  %  ( xs, )
				
				if isinstance( method, specialform ):
					return method.invoke( target, self, xs )
				else:
					args = [ DMInterpreterEnv( **self._env ).dmEval( dmarg )   for dmarg in xs[2:] ]
					try:
						return method( *args )
					except Exception:
						print '*** Internal error in %s'  %  ( xs, )
						raise
					
		else:
			return self._p_interpretLiteral( xs )


	def dmExec(self, xs):
		if isinstance( xs, DMListInterface ):
			if len( xs ) > 0:
				if isinstance( xs[0], DMListInterface ):
					for x in xs:
						res = self.dmEval( x )
					return res
				else:
					return self.dmEval( xs )
		else:
			return self.dmEval( xs )
		
		
	def __getitem__(self, key):
		return self._env[key]
		
		


import unittest
from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMIO import readSX
import cStringIO



class TestCase_DMInterpreter (unittest.TestCase):
	class _OutputWriter (object):
		def __init__(self, stdout=sys.stdout):
			super( DMSys, self ).__init__()
			self._stdout = stdout
	
		def stdout(self, s):
			self._stdout.write( s )
			
	
	def setUp(self):
		self.stdout = cStringIO.StringIO()
		
	def tearDown(self):
		self.stdout.close()

	def dmExec(self, programText):
		sys = self._OutputWriter( self.stdout )
		return DMInterpreterEnv( sys=sys ).dmExec( readSX( programText ) )
	
	def dmEval(self, programText):
		return DMInterpreterEnv().dmEval( readSX( programText ) )
	
	
	def testStdout(self):
		self.dmExec( '(@sys stdout a)' )
		self.assert_( self.stdout.getvalue() == 'a' )
		
	def testVars(self):
		src = """
		((= @a test)
		(@sys stdout @a))"""
		self.dmExec( src )
		self.assert_( self.stdout.getvalue() == 'test' )
		
	def testBooleans(self):
		self.assert_( self.dmEval( '#False' )  ==  False )
		self.assert_( self.dmEval( '#True' )  ==  True)
		
	def testNone(self):
		self.assert_( self.dmEval( '#None' )  ==  None )
		self.assert_( self.dmEval( '`null`' )  ==  None)
		
	def testInt(self):
		self.assert_( self.dmEval( '#123' ) == 123 )
		
	def testMath(self):
		self.assert_( self.dmEval( '(#10 + #2)' ) == 12 )
		self.assert_( self.dmEval( '(#10 - #2)' ) == 8 )
		self.assert_( self.dmEval( '(#10 * #2)' ) == 20 )
		self.assert_( self.dmEval( '(#10 / #2)' ) == 5 )
		self.assert_( self.dmEval( '(#10 % #3)' ) == 1 )
		self.assert_( self.dmEval( '(#10 ** #2)' ) == 100 )
		self.assert_( self.dmEval( '(#10 << #2)' ) == 40 )
		self.assert_( self.dmEval( '(#10 >> #2)' ) == 2 )
		self.assert_( self.dmEval( '(#3 & #6)' ) == 2 )
		self.assert_( self.dmEval( '(#3 | #6)' ) == 7 )
		self.assert_( self.dmEval( '(#3 ^ #6)' ) == 5 )
		self.assert_( self.dmEval( '(#3 < #6)' ) == True )
		self.assert_( self.dmEval( '(#3 <= #6)' ) == True )
		self.assert_( self.dmEval( '(#3 == #6)' ) == False )
		self.assert_( self.dmEval( '(#3 != #6)' ) == True )
		self.assert_( self.dmEval( '(#3 > #6)' ) == False )
		self.assert_( self.dmEval( '(#3 >= #6)' ) == False )
		self.assert_( self.dmEval( '(#6 < #3)' ) == False )
		self.assert_( self.dmEval( '(#6 <= #3)' ) == False )
		self.assert_( self.dmEval( '(#6 == #3)' ) == False )
		self.assert_( self.dmEval( '(#6 != #3)' ) == True )
		self.assert_( self.dmEval( '(#6 > #3)' ) == True )
		self.assert_( self.dmEval( '(#6 >= #3)' ) == True )
		self.assert_( self.dmEval( '(#3 < #3)' ) == False )
		self.assert_( self.dmEval( '(#3 <= #3)' ) == True )
		self.assert_( self.dmEval( '(#3 == #3)' ) == True )
		self.assert_( self.dmEval( '(#3 != #3)' ) == False )
		self.assert_( self.dmEval( '(#3 > #3)' ) == False )
		self.assert_( self.dmEval( '(#3 >= #3)' ) == True )
		



if __name__ == '__main__':
	unittest.main()
