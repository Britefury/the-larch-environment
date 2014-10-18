##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
from BritefuryJ.Live import LiveInterface, LiveValue

from LarchCore.Kernel import abstract_kernel




class AbstractPythonLiveModule (abstract_kernel.AbstractLiveModule):
	pass



class _ImportableModule (object):
	def __init__(self, kernel):
		self.__kernel = kernel
		self.__name = None

		self.__name_in_use = None
		self.__update_required = True
		self.__source = None



	@property
	def name(self):
		return self.__name

	@name.setter
	def name(self, n):
		self.__name = n
		self.__update_required = True



	def set_source(self, src):
		def on_source_modified(incremental_monitor):
			self.__update_required = True

		if src is not self.__source:
			if isinstance(self.__source, LiveInterface):
				self.__source.removeListener(on_source_modified)
			self.__source = src
			if isinstance(self.__source, LiveInterface):
				self.__source.addListener(on_source_modified)
			self.__update_required = True


	def __get_source(self):
		if self.__source is None:
			return None
		elif isinstance(self.__source, str)  or  isinstance(self.__source, unicode):
			return self.__source
		elif isinstance(self.__source, LiveInterface):
			return self.__source.value



	def _update(self, modules_to_remove, modules_to_set):
		if self.__update_required:
			source = self.__get_source()
			name_changed = self.__name != self.__name_in_use

			if self.__name_in_use is not None:
				if name_changed:
					modules_to_remove.append(self.__name_in_use)
					self.__update_required = False

			if self.__name is not None:
				if source is not None:
					modules_to_set.append((self.__name, source))
					self.__update_required = False
					self.__name_in_use = self.__name




class AbstractPythonKernel (abstract_kernel.AbstractKernel):
	def __init__(self):
		self._importable_modules = []


	def _shutdown(self):
		pass

	def new_live_module(self, full_name):
		raise NotImplementedError, 'abstract'


	def new_importable_module(self):
		m = _ImportableModule(self)
		self._importable_modules.append(m)
		return m

	def delete_importable_module(self, m):
		self._importable_modules.remove(m)


	def update_importable_modules(self):
		modules_to_remove = []
		modules_to_set = []
		for m in self._importable_modules:
			m._update(modules_to_remove, modules_to_set)
		for fullname in modules_to_remove:
			self.remove_module(fullname)
		for fullname, source in modules_to_set:
			self.set_module_source(fullname, source)


	def set_module_source(self, fullname, source):
		raise NotImplementedError, 'abstract'

	def remove_module(self, fullname):
		raise NotImplementedError, 'abstract'




class AbstractPythonContext (object):
	def start_kernel(self, on_kernel_started):
		raise NotImplementedError, 'abstract'

	def shutdown_kernel(self, kernel):
		raise NotImplementedError, 'abstract'


import unittest



class Test_ImportableModule (unittest.TestCase):
	class TestKernel (AbstractPythonKernel):
		def __init__(self):
			super(Test_ImportableModule.TestKernel, self).__init__()
			self.module_sources = {}
			self.removed_modules = set()

		def set_module_source(self, fullname, source):
			self.module_sources[fullname] = source

		def remove_module(self, fullname):
			self.removed_modules.add(fullname)



	def setUp(self):
		self.kernel = self.TestKernel()

	def tearDown(self):
		del self.kernel


	def assertChangesAndClear(self, expected_set, expected_removed):
		self.assertEqual(expected_set, self.kernel.module_sources)
		self.assertEqual(expected_removed, self.kernel.removed_modules)
		self.kernel.module_sources = {}
		self.kernel.removed_modules = set()


	def test_no_modules(self):
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({}, set())

	def test_single_module(self):
		# Create module, leave unnamed
		m1 = self.kernel.new_importable_module()
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({}, set())

		# Set name
		m1.name = 'm1'
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({}, set())

		# Set source as string
		m1.set_source('x')
		self.kernel.update_importable_modules()
		# Set
		self.assertChangesAndClear({'m1': 'x'}, set())

		# Change source
		m1.set_source('y')
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1': 'y'}, set())

		# Change name
		m1.name = 'm1b'
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1b': 'y'}, {'m1'})

		# Change name twice
		m1.name = 'm1c'
		m1.name = 'm1d'
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1d': 'y'}, {'m1b'})

		# Change source twice
		m1.set_source('w')
		m1.set_source('q')
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1d': 'q'}, set())

		# Change name and source
		m1.name = 'm1'
		m1.set_source('x')
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1': 'x'}, {'m1d'})

		# Use live source
		s_live = LiveValue('y')
		m1.set_source(s_live)
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1': 'y'}, set())

		# Change live source
		s_live.setLiteralValue('z')
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1': 'z'}, set())

		# Change live source again
		s_live.setLiteralValue('x')
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1': 'x'}, set())


	def test_two_modules(self):
		# Create modules
		m1 = self.kernel.new_importable_module()
		m2 = self.kernel.new_importable_module()
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({}, set())

		# Change m1 name, m2 source
		m1.name = 'm1'
		m2.set_source('y')
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({}, set())

		# Change m1 source, m2 name
		m1.set_source('x')
		m2.name = 'm2'
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m1': 'x', 'm2': 'y'}, set())

		# Swap names
		m1.name = 'm2'
		m2.name = 'm1'
		self.kernel.update_importable_modules()
		self.assertChangesAndClear({'m2': 'x', 'm1': 'y'}, {'m1', 'm2'})






