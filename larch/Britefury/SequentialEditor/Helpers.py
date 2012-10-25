##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.MethodDispatch import redecorateDispatchMethod



def _cache(controller, key, valueFn):
	try:
		tbl = controller.___seq_editor_cache___
	except AttributeError:
		tbl = {}
		controller.___seq_editor_cache___ = tbl
	try:
		return tbl[key]
	except KeyError:
		value = valueFn()
		tbl[key] = value
		return value



class _SeqEditorDescriptor (object):
	def __set__(self, obj, value):
		raise TypeError

	def __delete__(self, obj):
		raise TypeError



class RichStringFilterDeclaration (_SeqEditorDescriptor):
	def __init__(self):
		self.__method = None


	def commitMethod(self, method):
		self.__method = method


	def _filterForInstance(self, controller):
		return _cache( controller, self, lambda: controller.richStringCommitFilter( lambda model, value: self.__method( controller, model, value ) ) )

	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			if self.__method is not None:
				raise ValueError, 'Commit method of RichStringFilterDeclaration \'{0}\' not set'.format( type( obj ).__name__ )
			return self._filterForInstance( obj )



class _AbstractEditRuleDeclaration (_SeqEditorDescriptor):
	def __init__(self, filterDeclarations):
		self.__filterDeclarations = filterDeclarations

	def _filters(self, controller):
		return  [ f._filterForInstance( controller )   for f in self.__filterDeclarations ]


class EditRuleDeclaration (_AbstractEditRuleDeclaration):
	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			return _cache( obj, self, lambda: _WrappedEditRule( obj.editRule( self._filters( obj ) ) ) )


class SoftStructuralEditRuleDeclaration (_AbstractEditRuleDeclaration):
	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			return _cache( obj, self, lambda: _WrappedEditRule( obj.softStructuralEditRule( self._filters( obj ) ) ) )



class _WrappedEditRule (object):
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


