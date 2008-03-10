##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

import os

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel.DMIO import readSX

from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString
from Britefury.GLisp.GLispEnvironment import getGLispModulePath



#
#
#
# GLisp exceptions
#
#
#

class GLispNameError (Exception):
	pass

class GLispMethodError (Exception):
	pass

class GLispKeywordError (Exception):
	pass

class GLispParameterListError (Exception):
	pass

class GLispItemTypeError (Exception):
	pass






#
#
#
# Module registry
#
#
#


class ModuleRegistry (object):
	def __init__(self):
		self._modules = {}
		self.moduleFactory = GLispModule
			

	def __getitem__(self, name):
		try:
			return self._modules[name]
		except KeyError:
			# Create the module, and register it
			m = self.moduleFactory()
			self._modules[name] = m
			m.name = name

			# Generate the module path
			path = os.path.join( *name.split( '.' ) )  +  '.gsym'
			realpath = os.path.realpath( path )
			
			# Read in the document from the file, and execute it, in the context of the module
			modulePath = getGLispModulePath( path )
			
			doc = readSX( file( modulePath, 'r' ) )
			m.evaluate( doc )
						
			return m
		
		
	def __setitem__(self, name, module):
		self._modules[name] = module
		
		
		




_glispInterpreterMethodNameMap = {
	'+' : intern( '__add__' ),
	'-' : intern( '__sub__' ),
	'*' : intern( '__mul__' ),
	'/' : intern( '__div__' ),
	'%' : intern( '__mod__' ),
	'**' : intern( '__pow__' ),
	'<<' : intern( '__lshift__' ),
	'>>' : intern( '__rshift__' ),
	'&' : intern( '__and__' ),
	'|' : intern( '__or__' ),
	'^' : intern( '__xor__' ),
	'[]' : intern( '__getitem__' ),
	'[]=' : intern( '__setitem__' ),
	'is' : lambda x, y: x is y,
	'new' : lambda x, *args: x( *args ),
	'<' : lambda x, y: x < y,
	'<=' : lambda x,y: x <= y,
	'==' : lambda x, y: x == y,
	'!=' : lambda x, y: x != y,
	'>' : lambda x, y: x > y,
	'>=' : lambda x, y: x >= y,
	'.' : lambda x, y: getattr( x, y )
	}




class specialform (object):
	def __init__(self, f):
		self._f = f
	
	def invoke(self, obj, env, xs):
		return self._f( obj, env, xs )
	






#
#
#
# GLisp interpreter frame
#
#
#

class GLispFrame (object):
	#__slots__ = [ '_env', '_outerFrame', 'moduleRegistry' ]

	
	def __init__(self, **env):
		self._env = copy( env )
		self._outerFrame = None
		self.moduleRegistry = _moduleRegistry
			
			
	def innerScope(self):
		f = GLispFrame()
		f._outerFrame = self
		return f

	
	def rootScope(self):
		s = self
		while s._outerFrame is not None:
			s = s._outerFrame
		assert isinstance( s, GLispModule ), 'root scope should be a module'
		return s
	
	
	def glispError(self, exceptionClass, src, reason):
		raise exceptionClass, self.rootScope().name + ': ' + reason  +  '   ::   '  +  gLispSrcToString( src, 3 )
		

	def _p_interpretLiteral(self, xs):
		if xs[0] == '@':
			varName = xs[1:]
			try:
				return self[varName]
			except KeyError:
				self.glispError( GLispNameError, xs, '%s not bound'  %  ( varName ) )
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
		elif isGLispList( xs ):
			if len( xs ) == 0:
				return None
	
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
				if len( xs ) == 0:
					# Empty list  ->  None
					return None
				elif len( xs ) == 1:
					# List with 1 element  ->  evaluate the element
					return self.evaluate( x0 )
				else:
					# List with 2 or more elements
					x1 = xs[1]
					
					if not isinstance( x1, str )  and  not isinstance( x1, unicode ):
						# Second element not a string  ->  treat as expression list; evaluate in turn, return result of last expression
						for x in xs:
							res = self.evaluate( x )
						return res
					else:
						# Second element is a string  ->  treat as a method call
						target = self.evaluate( x0 )
						
						# _glispInterpreterMethodNameMap maps a method name to a python method name, -OR- a function that will compute the result.
						# This is for methods such as +, -, ., [], <, etc
						methodName = _glispInterpreterMethodNameMap.get( x1, x1 )
						
						if isinstance( methodName, str )  or  isinstance( methodName, unicode ):
							# method name is string; get the method by looking it up in the target object
							try:
								method = getattr( target, methodName )
							except AttributeError:
								self.glispError( GLispMethodError, xs, '%s has no method %s' % ( target, methodName ) )
						elif callable( methodName ):
							# method name is callable; call it
							args = [ self.evaluate( dmarg )   for dmarg in xs[2:] ]
							try:
								return methodName( target, *args )
							except Exception:
								print '*** Internal error in %s'  %  ( xs, )
								raise
						else:
							self.glispError( TypeError, xs, 'methodName is invalid' )
						
						if isinstance( method, specialform ):
							# Special form; pass the list onto the method, along with the frame
							return method.invoke( target, self, xs )
						else:
							# Call the method
							args = [ self.evaluate( dmarg )   for dmarg in xs[2:] ]
							try:
								return method( *args )
							except Exception:
								print '*** Internal error in %s'  %  ( xs, )
								raise
					
		else:
			return self._p_interpretLiteral( xs )

	
	def _keyword_where(self, xs):
		"""
		($where ((name0 value0) (name1 value1) ... (nameN valueN)) (expressions_to_execute))
		"""
		if len( xs ) < 2:
			self.glispError( ValueError, xs, '$where must have have at least 1 parameter; the binding list' )
	
		bindings = xs[1]
		expressions = xs[2:]
		
		if not isGLispList( bindings ):
			self.glispError( ValueError, xs, '$where bindings must be a list of pairs' )
		
		newEnv = self.innerScope()
		for binding in bindings:
			if not isGLispList( binding )  or  len( binding ) != 2:
				self.glispError( ValueError, xs, '$where binding must be a name value pair' )
			
			if binding[0][0] != '@':
				self.glispError( ValueError, xs, '$where binding name must start with @' )
			
			newEnv._env[binding[0][1:]] = newEnv.evaluate( binding[1] )
			
		return newEnv.evaluate( expressions )

	
	def _keyword_importModule(self, xs):
		"""
		($importModule module (expressions_to_execute))
		OR
		($importModule (module as_name) (expressions_to_execute))
		"""
		if len( xs ) < 2:
			self.glispError( TypeError, xs, '$importModule must have at least 1 parameter; the module to import' )

		moduleName = xs[1]
		expressions = xs[2:]
		
		if isinstance( moduleName, str )  or  isinstance( moduleName, unicode ):
			name = moduleName
			targetName = name
		else:
			if len( moduleName ) != 2:
				self.glispError( ValueError, xs, '$importModule module name must either be a string, or a list of two items; the name and the name to import as' )
			name = moduleName[0]
			targetName = moduleName[1]
			if targetName[0] != '@':
				self.glispError( ValueError, xs, '$importModule \'import as name\' must start with a @' )
			targetName = targetName[1:]
		
		module = self.moduleRegistry[name]
		
		newEnv = self.innerScope()
		newEnv._env[targetName] = module
		
		return newEnv.evaluate( expressions )

	

	def _keyword_importModuleContents(self, xs):
		"""
		($importModule module (item0 item1 ... itemN) (expressions_to_execute))
		
		itemX :=
			(name import_as_name)
		"""
		if len( xs ) < 3:
			self.glispError( TypeError, xs, '$importModuleContents must have at least 2 parameter; the module to import, and the items to import' )

		name = xs[1]
		items = xs[2]
		expressions = xs[3:]
		
		newEnv = self.innerScope()

		module = self.moduleRegistry[name]

		if not isinstance( name , str )  and  not isinstance( name, unicode ):
			self.glispError( ValueError, xs, '$importModuleContents module path should be a string' )
		

		if not isGLispList( items ):
			self.glispError( ValueError, xs, '$importModuleContents items should be a list' )
		
		for item in items:
			if not isGLispList( item ):
				self.glispError( ValueError, xs, '$importModuleContents item should be a list' )
		
			srcName = item[0]
			destName = item[1]
			
			if destName[0] != '@':
				self.glispError( ValueError, xs, '$importModuleContents destination name must start with a @' )
			destName = destName[1:]

			try:
				moduleAttribute = module[srcName]
			except KeyError:
				self.glispError( ValueError, xs, '$importModuleContents: module %s has no attribute %s'  %  ( name, srcName ) )
			
			newEnv[destName] = moduleAttribute

		return newEnv.evaluate( expressions )

	
	
	
	def __getitem__(self, key):
		try:
			return self._env[key]
		except KeyError:
			if self._outerFrame is not None:
				return self._outerFrame[key]
			else:
				raise
		
	def __setitem__(self, key, value):
		self._env[key] = value

		

class GLispModule (GLispFrame):
	def __init__(self, **env):
		super( GLispModule, self ).__init__( **env )
		self._env['__module__'] = self
		self.name = ''

	def get(self, key):
		return self._env[key]
	
	def set(self, key, value):
		self._env[key] = value

		

		
		
_moduleRegistry = ModuleRegistry()


	
	
	
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

	def evaluate(self, programText, moduleRegistry=_moduleRegistry):
		sys = self._OutputWriter( self.stdout )
		frame = GLispFrame( sys=sys, tester=self )
		frame.moduleRegistry = moduleRegistry
		return frame.evaluate( readSX( programText ) )
	
	
	def _makeModuleRegistry(self, **modules):
		moduleRegistry = ModuleRegistry()
		for key, value in modules.items():
			moduleRegistry[key] = value
		return moduleRegistry
	
	def evaluateWithModules(self, programText, **modules):
		sys = self._OutputWriter( self.stdout )
		frame = GLispFrame( sys=sys, tester=self )
		frame.moduleRegistry = self._makeModuleRegistry( **modules )
		return frame.evaluate( readSX( programText ) )
	
	
	
	def testStdout(self):
		self.evaluate( '(@sys stdout a)' )
		self.assert_( self.stdout.getvalue() == 'a' )
		
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
		
	def testGetAttr(self):
		self.assert_( self.evaluate( '(@tester . stdout)' )  is  self.stdout )
		
	def testWhere(self):
		src = """($where ((@a #4) (@b (@a + #5)) (@c test) (@d (@c * @b)))
		(@c split)
		(@sys stdout @d)
		)"""
		self.evaluate( src )
		self.assert_( self.stdout.getvalue() == 'test' * 9 )

	def testImportModule(self):
		module = GLispModule()
		module['a'] = 123
		src = """
		($importModule testModule
		(@testModule [] a)
		)"""
		result = self.evaluateWithModules( src, testModule=module )
		self.assert_( result == 123 )
		
	def testImportModuleAs(self):
		module = GLispModule()
		module['a'] = 123
		src = """
		($importModule (testModule @test)
		(@test [] a)
		)"""
		result = self.evaluateWithModules( src, testModule=module )
		self.assert_( result == 123 )

	def testImportModuleContents(self):
		module = GLispModule()
		module['a'] = 123
		src = """
		($importModuleContents testModule ((a @test))
		@test
		)"""
		result = self.evaluateWithModules( src, testModule=module )
		self.assert_( result == 123 )

		

if __name__ == '__main__':
	unittest.main()
