##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.MethodDispatch import redecorateDispatchMethod




class BoundEditRuleDecorator (object):
	def __init__(self, rule):
		self.__rule = rule
		self.applyToFragment = rule.applyToFragment


	@property
	def rule(self):
		return self.__rule


	def __call__(self, method):
		def _m(*args):
			p = method(*args)
			f = args[1]
			return self.applyToFragment(p, f.model, f.inheritedState)
		redecorateDispatchMethod(method, _m)
		return _m





class EditRuleDecorator (object):
	def __init__(self, ruleFn):
		self.__ruleFn = ruleFn


	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			rule = self.__ruleFn( obj )
			return BoundEditRuleDecorator( rule )
