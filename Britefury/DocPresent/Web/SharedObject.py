##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import types


from Britefury.DocPresent.Web.JSScript import JSScriptClass, JSScript





_classRegsitryJS = \
"""
_classNameToClass = {};

function _json_to_shared_object(json)
{
	var className = json[0];
	var content = json[1];
	
	cls = _classNameToClass[className];
	return cls.fromJSonContent( content );
};

function _shared_object_to_json(obj)
{
	var className = obj.className();
	var content = obj.jsonContent();
	return [ className, content ];
};
"""


class InvalidSharedObjectJSon (Exception):
	pass

class InvalidSharedObjectName (Exception):
	pass

class SharedObjectHasNoJavascript (Exception):
	pass



_sharedObjectClassNameToClass = {}
sharedObjectClasses = []


class JSClassMethod (object):
	def __init__(self, jsFunction):
		self.name = None
		self.jsFunction = jsFunction



class JSMethod (object):
	def __init__(self, name, jsFunction):
		self.__name__ = name
		self.jsFunction = jsFunction



class JSClassNamedMethod (JSClassMethod):
	def __init__(self, name, jsFunction):
		super( JSClassNamedMethod, self ).__init__( jsFunction )
		self.name = name



class SharedObjectClass (JSScriptClass):
	def __init__(cls, clsName, clsBases, clsDict):
		super( SharedObjectClass, cls ).__init__( clsName, clsBases, clsDict )
		_sharedObjectClassNameToClass[clsName] = cls
		sharedObjectClasses.append( cls )
		
		for name, value in clsDict.items():
			if isinstance( value, JSClassMethod ):
				if value.name is None:
					value.name = name


		
		

class SharedObject (JSScript):
	__metaclass__ = SharedObjectClass
	
	
	
	
	def json(self):
		return [ self.__class__.__name__, self.jsonContent() ]
		
		
	def jsonContent(self):
		return []
	
	
	
	@classmethod
	def fromJSonContent(cls, content):
		return SharedObject()
	
		
		
	@staticmethod
	def fromJSon(j):
		if isinstance( j, list )   and   len( j ) == 2:
			typeName = j[0]
			if isinstance( typeName, str )  or  isinstance( typeName, unicode ):
				try:
					cls = _sharedObjectClassNameToClass[typeName]
				except KeyError:
					raise InvalidSharedObjectName
				
				return cls.fromJSonContent( j[1] )
			else:
				print type( typeName )
				raise TypeError
		else:
			print j
			raise InvalidSharedObjectJSon
		
		
		
	@classmethod
	def __class_js__(cls):
		initialJS = ''
		if cls is SharedObject:
			initialJS = _classRegsitryJS + '\n\n\n'
		clsName = cls.__name__
		return initialJS  +  generateJSImplementation( cls )  +  '_classNameToClass.%s = %s\n'  %  ( clsName, clsName )  +  '%s.className = "%s"\n'  %  ( clsName, clsName )  +  '\n\n'

	
	
def _generateJSForMethod(cls, method):
	try:
		jsFunction = method.jsFunction
	except AttributeError:
		return None
	else:
		return '%s.prototype.%s = %s'  %  ( cls.__name__, method.__name__, jsFunction )
	
	
def _generateJSForClassMethod(cls, method):
	return '%s.%s = %s'  %  ( cls.__name__, method.name, method.jsFunction )
	

def _generateJSForConstructor(cls):
	bases = [ base   for base in cls.__bases__   if issubclass( base, SharedObject ) ]
	
	inheritanceJS = '%s.prototype = new %s;\n'  %  ( cls.__name__, bases[0].__name__ )      if len( bases ) > 0   else   ''
	
	
	try:
		method = cls.__init__
	except AttributeError:
		pass
	else:
		try:
			jsFunction = method.jsFunction
		except AttributeError:
			pass
		else:
			return '%s = %s'  %  ( cls.__name__, jsFunction )  +  inheritanceJS

	return \
"""function %s()
{
}
"""  %  cls.__name__  +  inheritanceJS


def _generateClassNameMethod(cls):
	return \
"""%s.prototype.className = function ()
{
	return "%s";
}
""" % ( cls.__name__, cls.__name__ )
	



def generateJSImplementation(cls):
	methodsJS = ''
	for name, value in sorted( cls.__dict__.items(), lambda a, b: cmp( a[0], b[0] ) ):
		if name != '__init__':
			methodJS = None
			if isinstance( value, types.FunctionType )  or  isinstance( value, JSMethod ):
				methodJS = _generateJSForMethod( cls, value )
			elif isinstance( value, JSClassMethod ):
				methodJS = _generateJSForClassMethod( cls, value )
			if methodJS is not None:
				methodsJS += methodJS + '\n\n'
				
	constructorJS = _generateJSForConstructor( cls )
	return constructorJS + '\n\n' + methodsJS + _generateClassNameMethod( cls ) + '\n\n'
	
	
	
	
	
import unittest


class TestCase_SharedObject (unittest.TestCase):
	def _makeMySO(self):
		class MySO (SharedObject):
			def __init__(self, x):
				self.x = x
			__init__.jsFunction = \
"""function (x)
{
	this.x = x;
}
"""
				
			myMethod = JSMethod( 'myMethod', \
"""function (a,b)
{
	return a+b;
}
""" )
			
			def jsonContent(self):
				return self.x
			jsonContent.jsFunction = \
"""function ()
{
	return this.x;
}
"""
			
			@classmethod
			def fromJSonContent(self, content):
				return MySO( content )
			fromJSonContentJS = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	return MySO( content );
}
""" )
			
			js = \
"""
function MySO(text)
{
	this.text = text;
}
"""
			
		return MySO
		
		
		
	def test_to_JSon(self):
		MySO = self._makeMySO()
		ev = MySO( 'hi' )
		self.assert_( ev.json()  ==  [ 'MySO', 'hi' ] )

	def test_from_JSon(self):
		MySO = self._makeMySO()
		json = [ 'MySO', 'hi' ]
		ev = SharedObject.fromJSon( json )
		self.assert_( isinstance( ev, MySO ) )
		self.assert_( ev.x == 'hi' )
		
	
	def test_generateJSForMethod(self):
		MySO = self._makeMySO()
		self.assert_( _generateJSForMethod( MySO, MySO.myMethod )  == \
"""MySO.prototype.myMethod = function (a,b)
{
	return a+b;
}
""" )

	
	def test_generateJSForConstructor(self):
		class A (SharedObject):
			pass
		
		class B (SharedObject):
			def __init__(self):
				pass
			
		class C (SharedObject):
			def __init__(self):
				pass
			__init__.jsFunction =\
"""function()
{
	this.x = 0;
}
"""
		self.assert_( _generateJSForConstructor( A ) == \
"""function A()
{
}
""" )

		self.assert_( _generateJSForConstructor( B ) == \
"""function B()
{
}
""" )

		self.assert_( _generateJSForConstructor( C ) == \
"""C = function()
{
	this.x = 0;
}
""" )
		
		
	def test_generateJSImplementation(self):
		MySO = self._makeMySO()
		self.assert_( generateJSImplementation( MySO ) == \
"""MySO = function (x)
{
	this.x = x;
}


MySO.fromJSonContent = function (content)
{
	return MySO( content );
}


MySO.prototype.jsonContent = function ()
{
	return this.x;
}


MySO.prototype.myMethod = function (a,b)
{
	return a+b;
}


MySO.prototype.className = function ()
{
	return "MySO";
}


""" )
