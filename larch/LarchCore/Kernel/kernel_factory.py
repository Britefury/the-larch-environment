##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
from java.awt import Color
from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Border, Row, Column, Table
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Graphics import SolidBorder

VM_INPROCESS = 'InProcess'
VM_CPYTHON = 'CPython'
VM_JYTHON = 'Jython'
VM_IRONPYTHON = 'IronPython'
VM_PYPY = 'PyPy'

VARIANT_NONE = None
VARIANT_SCIENTIFIC = 'Scientific'

DISTRIBUTION_NONE = None
DISTRIBUTION_ANACONDA = 'Anaconda'
DISTRIBUTION_CANOPY = 'Canopy'



def extract_variant_and_distribution_from_sys_version(sys_version):
	if 'Anaconda' in sys_version:
		return VARIANT_SCIENTIFIC, DISTRIBUTION_ANACONDA
	else:
		return VARIANT_NONE, DISTRIBUTION_NONE



class KernelNotChosenError (Exception):
	pass


class KernelDescription (object):
	"""Kernel description

	Used to identify a desired kernel that needs to be created.

	Implemented as a list of name-value pairs in order of decreasing importance.

	When constructing a kernel, a list of kernel factories is compared against the kernel description
	to find the best one. The factories are scored and the factory with the highest score is chosen.
	This is done by going through each attribute in the description in order and computing a score
	for the value provided by the factory. A value of None is used if the factory does not define a
	value for that attribute. The scored are put into a tuple, with each element corresponding to
	an attribute. The tuple with the highest score is chosen.

	Particular named attributes can be scored using a custom function by defining a method named
	_score_<attribute_name>
	"""

	def __init__(self, human_description, interp_name, attributes_as_tuples):
		"""
		Constructor

		:param human_description: human readable description of the kernel described
		:param attributes_as_tuples: desired kernel attributes as name-value
				pair tuples in a list in order of decreasing importance
		"""
		self.__human_description = human_description
		self.__interp_name = interp_name
		self.__attributes_as_tuples = attributes_as_tuples
		self.__attributes_as_dict = dict(attributes_as_tuples)


	def __eq__(self, other):
		if isinstance(other, KernelDescription):
			return self.__interp_name == other.__interp_name  and  self.__attributes_as_tuples == other.__attributes_as_tuples
		else:
			return NotImplemented

	def __ne__(self, other):
		return not (self == other)


	@property
	def interpreter_name(self):
		return self.__interp_name

	@property
	def human_description(self):
		return self.__human_description



	def get_best_factory(self, factories):
		"""
		Find the most closely matching kernel factory from a list of kernel factories.

		:param factories: list of kernel factories to compare
		:return: the best factory
		"""
		if len(factories) == 0:
			return None
		else:
			# Filter factories by interpreter name
			factories = [factory   for factory in factories   if factory.description.interpreter_name == self.__interp_name]

			# Score and choose
			scores_and_factories = [(self.__score_factory(factory), factory)   for factory in factories]
			scores_and_factories = sorted(scores_and_factories, key=lambda x: x[0])
			factory = scores_and_factories[-1][1]
			return factory


	def __score_factory(self, factory):
		"""
		Score a kernel factory

		:param factory: a kernel factory
		:return: the score as a tuple of ints or None for no scores
		"""
		factory_description = factory.description
		scores = []
		for name, desired_value in self.__attributes_as_tuples:
			try:
				actual_value = factory_description.__attributes_as_dict[name]
			except KeyError:
				score = None
			else:
				try:
					scoring_function = getattr(self, '_score_{0}'.format(name))
				except AttributeError:
					score = 1   if actual_value == desired_value  else 0
				else:
					score = scoring_function(desired_value, actual_value)
			scores.append(score)
		return tuple(scores)




	def _score_language_version(self, desired_value, actual_value):
		"""
		Score language version.
		If version is a.b.c  (e.g. v2.7.4: a=2, b=7, c=4)
		4 points for matching a, 2 if b match as well,1 more if c matches
		"""
		values = [4, 2, 1]
		score = 0
		for i in xrange(min(len(desired_value), len(actual_value), len(values))):
			if desired_value[i] == actual_value[i]:
				score += values[i]
		return score


	@staticmethod
	def standard_format(interp_name, vm, language_version, variant, distribution):
		human_description = '{0}, v{1}'.format(vm, '.'.join(language_version))
		if variant is not None:
			human_description += ', ' + variant
		if distribution is not None:
			human_description += ', ' + distribution

		return KernelDescription(human_description, interp_name, [
			('vm', vm),
			('language_version', language_version),
			('variant', variant),
			('distribution', distribution),
		])

	@staticmethod
	def from_kernel_information(interp_name, kernel_information):
		implementation, version_tuple, sys_version = kernel_information
		variant, distribution = extract_variant_and_distribution_from_sys_version(sys_version)
		return KernelDescription.standard_format(interp_name, implementation, list(version_tuple), variant, distribution)


	def __present__(self, fragment, inh):
		kname = self._interpreter_type_style.applyTo(Label(self.__interp_name))
		descr = self._interpreter_descr_style.applyTo(Label(self.__human_description))
		return self._interpreter_border.surround(Column([kname, descr.padX(7.0, 0.0)]))



	_interpreter_type_style = StyleSheet.style(Primitive.fontSize(10), Primitive.fontSmallCaps(True), Primitive.foreground(Color(0.4, 0.4, 0.4)))
	_interpreter_descr_style = StyleSheet.style(Primitive.foreground(Color(0.2, 0.2, 0.6)))
	_interpreter_border = SolidBorder(1.0, 3.0, 5.0, 5.0, Color(0.3, 0.3, 0.3), None)



class KernelFactory (object):
	def __init__(self, description, creation_fn):
		"""
		Constructor

		:param description: kernel description
		:param creation_fn: kernel creation function of the form fn(on_kernel_created) -> None,
			where on_kernel_created is a callback of the form callback(kernel)
		"""
		self.__description = description
		self.__creation_fn = creation_fn


	@property
	def description(self):
		return self.__description


	def create_kernel(self, on_kernel_created):
		self.__creation_fn(on_kernel_created)




import unittest

class TestCase_kernel_factory (unittest.TestCase):
	def assertBestFactory(self, expected_factory, factories, description):
		self.assertIs(expected_factory, description.get_best_factory(factories))

	def test_eq(self):
		ipy_dist = KernelDescription('IronPython',
					     [('vm', 'IronPython'), ('language_version', [2, 7, 1]), ('variant', '.net'), ('distribution', 'Microsoft')])
		ana_dist1 = KernelDescription('Anaconda 1',
					     [('vm', 'CPython'), ('language_version', [3, 4, 2]), ('variant', 'scientific'), ('distribution', 'Anaconda 1')])
		ana_dist2a = KernelDescription('Anaconda 2',
					     [('vm', 'CPython'), ('language_version', [3, 4, 2]), ('variant', 'scientific'), ('distribution', 'Anaconda 2')])
		ana_dist2b = KernelDescription('Anaconda 2',
					     [('vm', 'CPython'), ('language_version', [3, 4, 2]), ('variant', 'scientific'), ('distribution', 'Anaconda 2')])

		self.assertNotEqual(ipy_dist, ana_dist1)
		self.assertNotEqual(ana_dist1, ana_dist2a)
		self.assertEqual(ana_dist2a, ana_dist2b)


	def test_get_best_factory(self):
		ipy_dist = KernelDescription('IronPython',
			[('vm', 'IronPython'), ('language_version', [2, 7, 1]), ('variant', '.net'), ('distribution', 'Microsoft')])
		ana_dist = KernelDescription('Anaconda 2',
			[('vm', 'CPython'), ('language_version', [3, 4, 2]), ('variant', 'scientific'), ('distribution', 'Anaconda')])

		factory0 = KernelFactory(ipy_dist, None)
		factory1 = KernelFactory(ana_dist, None)

		self.assertBestFactory(factory0, [factory0, factory1], KernelDescription('0',
			[('vm', 'IronPython')]))
		self.assertBestFactory(factory1, [factory0, factory1], KernelDescription('0',
			[('vm', 'CPython')]))

		self.assertBestFactory(factory0, [factory0, factory1], KernelDescription('0',
			[('language_version', [2, 7, 1])]))
		self.assertBestFactory(factory0, [factory0, factory1], KernelDescription('0',
			[('language_version', [2, 7, 6])]))
		self.assertBestFactory(factory0, [factory0, factory1], KernelDescription('0',
			[('language_version', [2, 6, 1])]))
		self.assertBestFactory(factory0, [factory0, factory1], KernelDescription('0',
			[('language_version', [2, 6, 2])]))
		self.assertBestFactory(factory1, [factory0, factory1], KernelDescription('0',
			[('language_version', [3, 4, 2])]))
		self.assertBestFactory(factory1, [factory0, factory1], KernelDescription('0',
			[('language_version', [3, 4, 1])]))

		self.assertBestFactory(factory0, [factory0, factory1], KernelDescription('0',
			[('vm', 'IronPython'), ('language_version', [3, 4, 2])]))
		self.assertBestFactory(factory1, [factory0, factory1], KernelDescription('0',
			[('vm', 'CPython'), ('language_version', [2, 7, 1])]))

