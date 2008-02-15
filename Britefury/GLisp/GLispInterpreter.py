##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.GLispFrame import GLispFrame



class GLispNameError (Exception):
	pass

class GLispMethodError (Exception):
	pass

class GLispKeywordError (Exception):
	pass




_glispInterpreterMethodNameMap = {
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
	






class GLispInterpreterEnv (object):
	__slots__ = [ '_frame' ]

	
	def __init__(self, frame=None):
		if frame is None:
			self._frame = GLispFrame()
		else:
			self._frame = frame
			
			
	def innerScope(self):
		return GLispInterpreterEnv( self._frame.innerScope() )
		
		
	def _p_interpretLiteral(self, xs):
		if xs[0] == '@':
			varName = xs[1:]
			try:
				return self._frame[varName]
			except KeyError:
				raise GLispNameError, '%s not bound'  %  ( varName, )
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
		
		
		
	def evaluate(self, xs):
		if xs is None:
			return None
		elif isinstance( xs, DMListInterface ):
			x0 = xs[0]
			if x0[0] == '$':
				# keyword
				keyword = x0[1:]
				try:
					method = getattr( self, '_keyword_' + keyword )
				except AttributeError:
					raise GLispKeywordError, keyword
				return method( xs )
			else:
				if isinstance( x0, DMListInterface ):
					target = self.evaluate( x0 )
				else:
					target = self._p_interpretLiteral( x0 )
				
				methodName = xs[1]
				methodName = _glispInterpreterMethodNameMap.get( methodName, methodName )
				
				if isinstance( methodName, str ):
					try:
						method = getattr( target, methodName )
					except AttributeError:
						raise GLispMethodError, '%s has no method %s, in %s' % ( target, methodName, xs )
				elif callable( methodName ):
					args = [ self.evaluate( dmarg )   for dmarg in xs[2:] ]
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
					args = [ self.evaluate( dmarg )   for dmarg in xs[2:] ]
					try:
						return method( *args )
					except Exception:
						print '*** Internal error in %s'  %  ( xs, )
						raise
					
		else:
			return self._p_interpretLiteral( xs )


	def execute(self, xs):
		if isinstance( xs, DMListInterface )  or  isinstance( xs, list ):
			if len( xs ) > 0:
				if isinstance( xs[0], DMListInterface ):
					for x in xs:
						res = self.evaluate( x )
					return res
				else:
					return self.evaluate( xs )
		else:
			return self.evaluate( xs )
		
		
	def _keyword_let(self, xs):
		if len( xs ) < 2:
			raise ValueError, '$let must have have at least 1 parameter; the binding list'
	
		bindings = xs[1]
		expressions = xs[2:]
		
		if not isinstance( bindings, DMListInterface ):
			raise ValueError, '$let bindings must be a list of pairs'
		
		newEnv = self.innerScope()
		for binding in bindings:
			if not isinstance( binding, DMListInterface )  or  len( binding ) != 2:
				raise ValueError, '$let binding must be a name value pair'
			
			if binding[0][0] != '@':
				raise ValueError, '$let binding name must start with @'
			
			newEnv._frame[binding[0][1:]] = newEnv.evaluate( binding[1] )
			
		return newEnv.execute( expressions )

	
	def __getitem__(self, key):
		return self._frame[key]
		
	def __setitem__(self, key, value):
		self._frame[key] = value
		
		

	
	
	
import unittest
from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMIO import readSX
import cStringIO
import sys



class TestCase_GLispInterpreter (unittest.TestCase):
	class _OutputWriter (object):
		def __init__(self, stdout=sys.stdout):
			super( TestCase_GLispInterpreter._OutputWriter, self ).__init__()
			self._stdout = stdout
	
		def stdout(self, s):
			self._stdout.write( s )
			
	
	def setUp(self):
		self.stdout = cStringIO.StringIO()
		
	def tearDown(self):
		self.stdout.close()

	def execute(self, programText):
		sys = self._OutputWriter( self.stdout )
		return GLispInterpreterEnv( GLispFrame( sys=sys ) ).execute( readSX( programText ) )
	
	def evaluate(self, programText):
		return GLispInterpreterEnv().evaluate( readSX( programText ) )
	
	
	def testStdout(self):
		self.execute( '(@sys stdout a)' )
		self.assert_( self.stdout.getvalue() == 'a' )
		
	def testLet(self):
		src = """($let ((@a #4) (@b (@a + #5)) (@c test) (@d (@c * @b)))
		(@c split)
		(@sys stdout @d)
		)"""
		self.execute( src )
		self.assert_( self.stdout.getvalue() == 'test' * 9 )

	def testBooleans(self):
		self.assert_( self.evaluate( '#False' )  ==  False )
		self.assert_( self.evaluate( '#True' )  ==  True)
		
	def testNone(self):
		self.assert_( self.evaluate( '#None' )  ==  None )
		self.assert_( self.evaluate( '`null`' )  ==  None)
		
	def testInt(self):
		self.assert_( self.evaluate( '#123' ) == 123 )
		
	def testMath(self):
		self.assert_( self.evaluate( '(#10 + #2)' ) == 12 )
		self.assert_( self.evaluate( '(#10 - #2)' ) == 8 )
		self.assert_( self.evaluate( '(#10 * #2)' ) == 20 )
		self.assert_( self.evaluate( '(#10 / #2)' ) == 5 )
		self.assert_( self.evaluate( '(#10 % #3)' ) == 1 )
		self.assert_( self.evaluate( '(#10 ** #2)' ) == 100 )
		self.assert_( self.evaluate( '(#10 << #2)' ) == 40 )
		self.assert_( self.evaluate( '(#10 >> #2)' ) == 2 )
		self.assert_( self.evaluate( '(#3 & #6)' ) == 2 )
		self.assert_( self.evaluate( '(#3 | #6)' ) == 7 )
		self.assert_( self.evaluate( '(#3 ^ #6)' ) == 5 )
		self.assert_( self.evaluate( '(#3 < #6)' ) == True )
		self.assert_( self.evaluate( '(#3 <= #6)' ) == True )
		self.assert_( self.evaluate( '(#3 == #6)' ) == False )
		self.assert_( self.evaluate( '(#3 != #6)' ) == True )
		self.assert_( self.evaluate( '(#3 > #6)' ) == False )
		self.assert_( self.evaluate( '(#3 >= #6)' ) == False )
		self.assert_( self.evaluate( '(#6 < #3)' ) == False )
		self.assert_( self.evaluate( '(#6 <= #3)' ) == False )
		self.assert_( self.evaluate( '(#6 == #3)' ) == False )
		self.assert_( self.evaluate( '(#6 != #3)' ) == True )
		self.assert_( self.evaluate( '(#6 > #3)' ) == True )
		self.assert_( self.evaluate( '(#6 >= #3)' ) == True )
		self.assert_( self.evaluate( '(#3 < #3)' ) == False )
		self.assert_( self.evaluate( '(#3 <= #3)' ) == True )
		self.assert_( self.evaluate( '(#3 == #3)' ) == True )
		self.assert_( self.evaluate( '(#3 != #3)' ) == False )
		self.assert_( self.evaluate( '(#3 > #3)' ) == False )
		self.assert_( self.evaluate( '(#3 >= #3)' ) == True )
		



if __name__ == '__main__':
	unittest.main()
