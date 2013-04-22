##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from BritefuryJ.Live import TrackedLiveValue
from BritefuryJ.ChangeHistory import ChangeHistory

from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.CodeGenerator import Python2CodeGenerator




#
# FieldInstance (abstract)
#

class FieldInstance (object):
	def __init__(self, field, object_instance, source_value):
		self._field = field
		self._object_instance = object_instance


	def _addTrackableContentsTo(self, contents):
		raise NotImplementedError, 'abstract'

	def _getFieldState(self):
		raise NotImplementedError, 'abstract'


	def getValueForEditor(self):
		raise NotImplementedError, 'abstract'

	def __py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'abstract'




#
# Field (abstract)
#

class Field (object):
	__field_instance_class__ = None

	def __init__(self):
		self._name = None
		self._attrName = None


	def _classInit(self, name):
		self._name = name
		self._attrName = intern('__gui_field_' + name)


	def _instanceInit(self, object_instance, source_value):
		if self._name is None:
			raise TypeError, 'Field not initialised'
		if self.__field_instance_class__ is None:
			raise NotImplementedError, 'Field class \'{0}\' is abstract; __field_instance_class__ not defined'.format(type(self).__name__)
		fieldInstance = self.__field_instance_class__(self, object_instance, source_value)
		setattr(object_instance, self._attrName, fieldInstance)


	def _getFieldInstance(self, object_instance):
		if self._name is None:
			raise TypeError, 'Field not initialised'
		return getattr(object_instance, self._attrName)


	def _getFieldState(self, object_instance):
		return self._getFieldInstance(object_instance)._getFieldState()



	def __get__(self, instance, owner):
		if instance is None:
			return self
		else:
			if self._name is None:
				raise TypeError, 'Field not initialised'
			return getattr(instance, self._attrName)

	def __set__(self, instance, value):
		raise TypeError, 'fields cannot be set'


	def __delete__(self, instance):
		raise TypeError, 'fields cannot be deleted'




#
# PRIMITIVE FIELDS
#

class _PrimitiveFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, source_value):
		super(_PrimitiveFieldInstance, self).__init__(field, object_instance, source_value)
		value = field._defaultValue
		if source_value is not None:
			if isinstance(source_value, _PrimitiveFieldInstance):
				value = source_value.__live.getValue()
			else:
				value = source_value
		self.__live = TrackedLiveValue(value)



	@property
	def value(self):
		return self.__live.getValue()

	@value.setter
	def value(self, x):
		self.__live.setLiteralValue(x)


	@property
	def live(self):
		return self.__live



	def _addTrackableContentsTo(self, contents):
		contents.append(self.__live)

	def _getFieldState(self):
		return self.__live.getValue()

	def getValueForEditor(self):
		return self.value



class _PrimitiveField (Field):
	__primitive_type__ = None

	def __init__(self, defaultValue):
		if self.__primitive_type__ is None:
			raise NotImplementedError, 'Field class \'{0}\' is abstract; __primitive_type__ not defined'.format(type(self).__name__)
		if defaultValue is None:
			defaultValue = self.__primitive_type__()
		else:
			if not isinstance(defaultValue, self.__primitive_type__):
				raise TypeError, 'Default value is not an instance of \'{0}\''.format(self.__primitive_type__.__name__)
		super(_PrimitiveField, self).__init__()
		self._defaultValue = defaultValue




class IntFieldInstance (_PrimitiveFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.IntLiteral(format='decimal', numType='int', value=repr(self.value))

class IntField (_PrimitiveField):
	__field_instance_class__ = IntFieldInstance
	__primitive_type__ = int





#
#
# GUI Node class
#
#


class NodeAlreadyHasParentError (Exception):
	pass


class _NodeClass (type):
	def __init__(cls, name, bases, attrs):
		super(_NodeClass, cls).__init__(name, bases, attrs)
		fields = {}

		for base in bases:
			try:
				baseFields = base._gui_fields__
			except AttributeError:
				pass
			else:
				fields.update(baseFields)

		for name, value in attrs.items():
			if isinstance(value, Field):
				value._classInit(name)
				fields[name] = value

		cls._gui_fields__ = fields



class GUINode (object):
	__metaclass__ = _NodeClass


	def __init__(self, **values):
		self.__change_history__ = None
		for name in values:
			if name not in self._gui_fields__:
				raise TypeError, 'Class \'{0}\' does not have a field named \'{1}\''.format(type(self).__name__, name)
		for field in self._gui_fields__.values():
			field._instanceInit(self, values.get(field._name))
		self._parent = None


	@property
	def parent(self):
		return self._parent


	def __getstate__(self):
		return {name: field._getFieldState(self)   for name, field in self._gui_fields__.items()}


	def __setstate__(self, state):
		for field in self._gui_fields__.values():
			field._instanceInit(self, state.get(field._name))


	def __get_trackable_contents__(self):
		contents = []
		for field in self._gui_fields__.values():
			field._getFieldInstance(self)._addTrackableContentsTo(contents)
		return contents




#
#
# Child fields
#
#

class ChildFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, source_value):
		super(ChildFieldInstance, self).__init__(field, object_instance, source_value)
		value = None
		if source_value is not None:
			if isinstance(source_value, ChildFieldInstance):
				value = source_value.__live.getValue()
			else:
				value = source_value

		def on_change(old_value, new_value):
			if new_value is not old_value:
				if old_value is not None:
					old_value._parent = None
				if new_value is not None:
					if new_value._parent is not None:
						raise NodeAlreadyHasParentError, 'Node \'{0}\' already has a parent'.format(new_value)
					new_value._parent = self

		self.__live = TrackedLiveValue(value)
		self.__live.changeListener = on_change

		if value is not None:
			on_change(None, value)



	@property
	def node(self):
		return self.__live.getValue()

	@node.setter
	def node(self, x):
		self.__live.setLiteralValue(x)


	def _addTrackableContentsTo(self, contents):
		contents.append(self.__live)

	def _getFieldState(self):
		return self.__live.getValue()

	def getValueForEditor(self):
		return self.__live.getValue()

	def __py_evalmodel__(self, codeGen):
		value = self.__live.getValue()
		if value is None:
			return Py.Load(name='None')
		else:
			return value.__py_evalmodel__(codeGen)


class ChildField (Field):
	__field_instance_class__ = ChildFieldInstance




class ChildListFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, source_value):
		super(ChildListFieldInstance, self).__init__(field, object_instance, source_value)
		value = None
		if source_value is not None:
			if isinstance(source_value, ChildFieldInstance):
				value = source_value.__live.getValue()
			else:
				value = source_value

		def on_change(old_contents, new_contents):
			o = set(old_contents)
			n = set(new_contents)
			removed = o - n
			added = n - o
			for n in removed:
				n._parent = None
			for n in added:
				if n._parent is not None:
					raise NodeAlreadyHasParentError, 'Node \'{0}\' already has a parent'.format(n)
				n._parent = self

		self.__live = LiveList(value)

		if value is not None:
			on_change([], value)



	@property
	def nodes(self):
		return self.__live


	def _addTrackableContentsTo(self, contents):
		contents.append(self.__live)

	def _getFieldState(self):
		return self.__live[:]

	def getValueForEditor(self):
		return self.__live[:]

	def __py_evalmodel__(self, codeGen):
		return Py.ListLiteral(values=[n.__py_evalmodel__(codeGen)   for n in self.__live])


class ChildListField (Field):
	__field_instance_class__ = ChildListFieldInstance





#
#
# UNIT TESTS
#
#


import unittest


class TestCase_DataModel(unittest.TestCase):
	@classmethod
	def setUpClass(cls):
		class A (GUINode):
			x = IntField(0)
			y = IntField(1)

		class B (GUINode):
			p = ChildField()
			q = ChildListField()

		cls.A = A
		cls.B = B


	@classmethod
	def tearDownClass(cls):
		cls.A = None
		cls.B = None


	def setUp(self):
		self.ch = ChangeHistory()

	def tearDown(self):
		self.ch = None


	@staticmethod
	def buildFromState(cls, state):
		instance = cls.__new__(cls)
		instance.__setstate__(state)
		return instance



	def test_constructor(self):
		a1 = self.A()

		self.assertEqual(0, a1.x.value)
		self.assertEqual(1, a1.y.value)

		a2 = self.A(x=10, y=20)

		self.assertEqual(10, a2.x.value)
		self.assertEqual(20, a2.y.value)

		self.assertRaises(TypeError, lambda: self.A(a=1, b=2))


		b1 = self.B()
		a2 = self.A()

		self.assertIs(None, b1.p.node)
		self.assertEqual([], b1.q.nodes)

		b2 = self.B(p=a1, q=[a2])

		self.assertIs(a1, b2.p.node)
		self.assertEqual([a2], b2.q.nodes)

		self.assertRaises(NodeAlreadyHasParentError, lambda: self.B(p=a1, q=[a2]))





	def test_PrimitiveField_changeHistory(self):
		a = self.A()

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		self.ch.track(a)

		self.assertIs(ChangeHistory.getChangeHistoryFor(a), self.ch)

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		a.x.value = 10
		a.y.value = 20

		self.assertEqual(10, a.x.value)
		self.assertEqual(20, a.y.value)
		self.assertEqual(2, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual(10, a.x.value)
		self.assertEqual(1, a.y.value)
		self.assertEqual(1, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual(0, a.x.value)
		self.assertEqual(1, a.y.value)
		self.assertEqual(0, self.ch.getNumUndoChanges())



	def test_PrimitiveField_serialisation(self):
		a = self.A(x=10, y=20)

		state = a.__getstate__()
		self.assertEqual({'x': 10, 'y': 20}, state)
		a_io = self.buildFromState(self.A, state)

		self.assertIsNot(a, a_io)

		self.assertEqual(10, a_io.x.value)
		self.assertEqual(20, a_io.y.value)



	def test_PrimitiveField_editor(self):
		a = self.A(x=10, y=20)

		codeGen = Python2CodeGenerator('test')

		self.assertEqual(10, a.x.getValueForEditor())
		self.assertEqual(20, a.y.getValueForEditor())

		self.assertEqual(Py.IntLiteral(format='decimal', numType='int', value='10'), a.x.__py_evalmodel__(codeGen))
		self.assertEqual(Py.IntLiteral(format='decimal', numType='int', value='20'), a.y.__py_evalmodel__(codeGen))






	def test_ChildField_changeHistory(self):
		b = self.B()
		a1 = self.A()
		a2 = self.A()
		a3 = self.A()

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		self.ch.track(b)

		self.assertIs(ChangeHistory.getChangeHistoryFor(b), self.ch)
		self.assertIs(ChangeHistory.getChangeHistoryFor(a1), None)
		self.assertIs(ChangeHistory.getChangeHistoryFor(a2), None)
		self.assertIs(ChangeHistory.getChangeHistoryFor(a3), None)

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		b.p.node = a1
		b.q.nodes.append(a2)

		self.assertIs(ChangeHistory.getChangeHistoryFor(b), self.ch)
		self.assertIs(ChangeHistory.getChangeHistoryFor(a1), self.ch)
		self.assertIs(ChangeHistory.getChangeHistoryFor(a2), self.ch)
		self.assertIs(ChangeHistory.getChangeHistoryFor(a3), None)

		self.assertIs(a1, b.p.node)
		self.assertEqual([a2], b.q.nodes[:])
		self.assertEqual(2, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertIs(a1, b.p.node)
		self.assertEqual([], b.q.nodes[:])
		self.assertEqual(1, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertIs(None, b.p.node)
		self.assertEqual([], b.q.nodes[:])
		self.assertEqual(0, self.ch.getNumUndoChanges())



	def test_ChildField_serialisation(self):
		a1 = self.A(x=10, y=20)
		a2 = self.A(x=3, y=4)
		b = self.B(p=a1, q=[a2])

		state = b.__getstate__()
		self.assertEqual({'p': a1, 'q': [a2]}, state)

		self.fail('Incomplete test')


	def test_ChildField_editor(self):
		self.fail('Incomplete test')








