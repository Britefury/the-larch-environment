##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



_elementClasses = []


class ElementClass (type):
	def __init__(cls, clsName, clsBases, cldDict):
		super( ElementClass, cls ).__init__( clsName, clsBases, clsDict )
		_elementClasses.append( cls )


class Element (object):
	@classmethod
	def js_classInit(cls):
		return None
	

	def js_instanceInitFunction(self):
		return None
	
	def js_callInstanceInit(self, js_functionExpression):
		pass
	
	
	
	@classmethod
	def js_classInit(
	
	
	
	
	@classmethod
	def js_selectClass(cls):
		return '$(".%s")'  %  cls.__name__
	
	
