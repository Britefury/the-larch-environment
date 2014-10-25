##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import sys, imp, inspect, os, binascii



class AbstractModuleSource (object):
	def execute(self, module):
		raise NotImplementedError, 'abstract'


	@staticmethod
	def coerce(x):
		if isinstance(x, str)  or  isinstance(x, unicode):
			return ModuleSourceStr(x)
		elif isinstance(x, AbstractModuleSource):
			return x
		else:
			raise TypeError, 'Could not coerce a {0} into a AbstractModuleSource'.format(type(x))


class ModuleSourceStr (AbstractModuleSource):
	def __init__(self, src):
		self.__source_str = src

	def execute(self, module):
		exec self.__source_str in module.__dict__


ModuleSourceStr.empty = ModuleSourceStr('')



class ModuleFinder (object):
	def __init__(self):
		self.__registered_modules = set()
		self.__path_to_module_source = {}
		self.__path_to_module_loader = {}
		self.__implicit_package_paths = None


	def __sub_paths(self, path):
		"""
		Return a generator that emits sub-paths; sub-sequences of the supplied path, starting from longest, going to shortest
		"""
		return (path[:i]   for i in xrange(len(path) - 1, 0, -1))


	def __refresh_implicit_package_paths(self):
		"""
		Re-build the set of implicit package paths
		"""
		if self.__implicit_package_paths is None:
			self.__implicit_package_paths = set()
			for path in self.__path_to_module_source.keys():
				self.__register_implicit_package_paths(path)

	def __register_implicit_package_paths(self, path_segs):
		for sub_path in self.__sub_paths(path_segs):
			self.__implicit_package_paths.add(sub_path)





	def set_module_source(self, name, source):
		source = AbstractModuleSource.coerce(source)

		path_segs = tuple(name.split('.'))
		self.__registered_modules.add(name)
		self.__path_to_module_source[path_segs] = source
		self.__refresh_implicit_package_paths()
		self.__register_implicit_package_paths(path_segs)


	def remove_module(self, name):
		if name not in self.__registered_modules:
			raise ValueError, 'No module {0} has been registered with this ModuleFinder'.format(name)
		path_segs = tuple(name.split('.'))
		self.__registered_modules.remove(name)
		del self.__path_to_module_source[path_segs]


	def unload_all_modules(self):
		for loader in self.__path_to_module_loader.values():
			if loader is not None:
				loader.unload()
		self.__path_to_module_loader = {}
		self.__implicit_package_paths = None



	def __find_module_source(self, path):
		try:
			return self.__path_to_module_source[path]
		except KeyError:
			self.__refresh_implicit_package_paths()
			if path in self.__implicit_package_paths:
				return ModuleSourceStr.empty
			else:
				return None

	def __find_module_loader(self, path):
		try:
			return self.__path_to_module_loader[path]
		except KeyError:
			source = self.__find_module_source(path)
			if source is not None:
				loader = ModuleLoader(self, source)
			else:
				loader = None
			self.__path_to_module_loader[path] = loader
			return loader


	def find_module(self, fullname, path):
		"""
		Implementation of the find_module method defined in the Python path hooks API
		"""
		path = tuple(fullname.split('.'))
		return self.__find_module_loader(path)


	def install_hooks(self):
		sys.meta_path.append(self)

	def uninstall_hooks(self):
		sys.meta_path.remove(self)




class ModuleLoader (object):
	def __init__(self, finder, source):
		self.__finder = finder
		self.__source = source
		self.__fullname = None
		self.__module = None


	@property
	def source(self):
		return self.__source


	def set_source(self, source):
		self.__source = source


	def new_module(self, fullname):
		mod = imp.new_module(fullname)
		sys.modules[fullname] = mod
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split('.')
		return mod


	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass

		mod = self.new_module(fullname)

		self.__source.execute(mod)

		self.__fullname = fullname
		self.__module = mod

		return mod


	def unload(self):
		if self.__module is not None:
			del sys.modules[self.__fullname]
			self.__fullname = None
			self.__module = None



_module_finder_name = __name__



def loader_module_name():
	return '__loader__{0}'.format(binascii.hexlify(os.urandom(32)))


_install_loader_template = '''
_mod___ = __import__('imp').new_module({0})
__import__('sys').modules[{0}] = _mod___
_mod___.__file__ = {0}
_mod___.__loader__ = None
_mod___.__path__ = {0}.split('.')
exec {1} in _mod___.__dict__
_mod___.finder = _mod___.ModuleFinder()
_mod___.finder.install_hooks()
del _mod___
'''

def install_loader_src(loader_mod_name):
	mod = sys.modules[_module_finder_name]
	mod_source = inspect.getsource(mod)

	return _install_loader_template.format(repr(loader_mod_name), repr(mod_source))


_uninstall_loader_template = '''
_mod___ = __import__({0})
_mod___.finder.uninstall_hooks()
del _mod___.finder
del _mod___
'''
def uninstall_loader_src(loader_mod_name):
	return _uninstall_loader_template.format(repr(loader_mod_name))


_set_module_src_template = '''
_mod___ = __import__({0})
_mod___.finder.set_module_source({1}, {2})
del _mod___
'''
def loader_set_module_source_src(loader_mod_name, mod_name, module_source):
	return _set_module_src_template.format(repr(loader_mod_name), repr(mod_name), repr(module_source))


_remove_module_template = '''
_mod___ = __import__({0})
_mod___.finder.remove_module({1})
del _mod___
'''
def loader_remove_module_src(loader_mod_name, mod_name):
	return _remove_module_template.format(repr(loader_mod_name), repr(mod_name))


_unload_all_modules_template = '''
_mod___ = __import__({0})
_mod___.finder.unload_all_modules()
del _mod___
'''
def loader_unload_all_modules_src(loader_mod_name):
	return _unload_all_modules_template.format(repr(loader_mod_name))





import unittest


class TestCase_module_finder (unittest.TestCase):
	def setUp(self):
		self.finder = ModuleFinder()
		self.finder.install_hooks()

	def tearDown(self):
		self.finder.uninstall_hooks()
		self.finder.unload_all_modules()


	def test_simple_modules(self):
		self.finder.set_module_source('a1', 'x = 1\n')
		self.finder.set_module_source('a2', 'x = 2\n')

		import a1, a2
		self.assertEqual(1, a1.x)
		self.assertEqual(2, a2.x)


	def test_modify_module(self):
		self.finder.set_module_source('a1', 'x = 1\n')
		self.finder.set_module_source('a2', 'x = 2\n')

		import a1, a2
		self.assertEqual(1, a1.x)
		self.assertEqual(2, a2.x)

		self.finder.set_module_source('a1', 'x = 10\n')
		self.finder.set_module_source('a2', 'x = 20\n')

		del a1, a2

		import a1, a2
		self.assertEqual(1, a1.x)
		self.assertEqual(2, a2.x)

		self.finder.unload_all_modules()

		del a1, a2

		import a1, a2
		self.assertEqual(10, a1.x)
		self.assertEqual(20, a2.x)


	def test_modify_nested_module(self):
		self.finder.set_module_source('a.b.c.d1', 'x=1\n')
		self.finder.set_module_source('a.b', 'x=2\n')

		from a import b
		from a.b.c import d1
		self.assertEqual(1, d1.x)
		self.assertEqual(2, b.x)

		self.finder.set_module_source('a.b', 'x=3')

		del b
		del d1

		self.finder.unload_all_modules()

		from a import b
		from a.b.c import d1
		self.assertEqual(1, d1.x)
		self.assertEqual(3, b.x)



	def test_remove_module(self):
		self.finder.set_module_source('a.b1.c1.d1', 'x=1\n')
		self.finder.set_module_source('a.b1.c2.d2', 'x=2\n')
		self.finder.set_module_source('a.b2.c3.d3', 'x=3\n')

		from a.b1.c1 import d1
		from a.b1.c2 import d2
		from a.b2.c3 import d3
		self.assertEqual(1, d1.x)
		self.assertEqual(2, d2.x)
		self.assertEqual(3, d3.x)
		self.assertIsNotNone(__import__('a.b1.c2.d2'))
		self.assertIsNotNone(__import__('a.b1.c2'))
		self.assertIsNotNone(__import__('a.b1'))

		self.finder.remove_module('a.b1.c2.d2')
		self.assertIsNotNone(__import__('a.b1.c2.d2'))
		self.assertIsNotNone(__import__('a.b1.c2'))
		self.assertIsNotNone(__import__('a.b1'))
		self.assertIsNotNone(__import__('a.b2'))
		self.finder.unload_all_modules()
		self.assertRaises(ImportError, lambda: __import__('a.b1.c2.d2'))
		self.assertRaises(ImportError, lambda: __import__('a.b1.c2'))
		self.assertIsNotNone(__import__('a.b1'))
		self.assertIsNotNone(__import__('a.b2'))

		self.finder.remove_module('a.b1.c1.d1')
		self.finder.unload_all_modules()
		self.assertRaises(ImportError, lambda: __import__('a.b1.c1.d1'))
		self.assertRaises(ImportError, lambda: __import__('a.b1.c1'))
		self.assertRaises(ImportError, lambda: __import__('a.b1'))
		self.assertIsNotNone(__import__('a'))

		self.finder.remove_module('a.b2.c3.d3')
		self.finder.unload_all_modules()
		self.assertRaises(ImportError, lambda: __import__('a.b2.c3.d3'))
		self.assertRaises(ImportError, lambda: __import__('a.b2.c3'))
		self.assertRaises(ImportError, lambda: __import__('a.b2'))
		self.assertRaises(ImportError, lambda: __import__('a'))

		self.assertRaises(ValueError, lambda: self.finder.remove_module('nothing_to_find_here'))


	def __import_fn(self, mod_name):
		# Helper function for test_remove_exec_loader, as exec won't work
		# in a function that defines a closure with free variables
		return lambda: __import__(mod_name)

	def test_remote_exec_loader(self):
		exec install_loader_src('_loader___')

		import _loader___
		self.assertIn('ModuleFinder', _loader___.__dict__)
		self.assertIn('ModuleLoader', _loader___.__dict__)

		exec loader_set_module_source_src('_loader___', 'a.b.c', 'x = 1\n')

		from a.b import c

		self.assertEqual(1, c.x)

		exec loader_remove_module_src('_loader___', 'a.b.c')

		self.assertRaises(ImportError, self.__import_fn('a'b'c'))

		exec loader_unload_all_modules_src('_loader___')
		exec uninstall_loader_src('_loader___')

		del sys.modules['_loader___']

