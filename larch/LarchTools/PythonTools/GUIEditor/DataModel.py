##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
import copy

from java.awt import Color

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Live import TrackedLiveValue
from BritefuryJ.ChangeHistory import ChangeHistory

from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Row, Column
from BritefuryJ.Pres.UI import Form

from BritefuryJ.Controls import Button, TextEntry

from BritefuryJ.StyleSheet import StyleSheet

from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr
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

	def __field_getstate__(self):
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


	def _classInit(self, containingClass, name):
		self._name = name
		self._attrName = intern('__gui_field_' + name)


	def _instanceInit(self, object_instance, source_value):
		if self._name is None:
			raise TypeError, 'Field not initialised'
		if self.__field_instance_class__ is None:
			raise NotImplementedError, 'Field class \'{0}\' is abstract; __field_instance_class__ not defined'.format(type(self).__name__)
		if isinstance(source_value, self.__field_instance_class__):
			fieldInstance = source_value
		else:
			fieldInstance = self.__field_instance_class__(self, object_instance, source_value)
		setattr(object_instance, self._attrName, fieldInstance)


	def _getFieldInstance(self, object_instance):
		if self._name is None:
			raise TypeError, 'Field not initialised'
		return getattr(object_instance, self._attrName)


	def _getFieldState(self, object_instance):
		return self._getFieldInstance(object_instance).__field_getstate__()



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
		value = source_value   if source_value is not None   else field._defaultValue

		def on_change(old_value, new_value):
			if self._field._change_listener is not None:
				self._field._change_listener(self._object_instance, self, old_value, new_value)

		self.__live = TrackedLiveValue(value)
		self.__live.changeListener = on_change



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

	def __field_getstate__(self):
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
		self._change_listener = None


	def on_change(self, method):
		self._change_listener = method
		return method




class ValueFieldInstance (_PrimitiveFieldInstance):
	def __py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'Type coercion for converting values to code must be implemented first'


class ValueField (Field):
	__field_instance_class__ = ValueFieldInstance


	def __init__(self, defaultValue=None):
		super(ValueField, self).__init__()
		self._change_listener = None
		self._defaultValue = defaultValue


	def on_change(self, method):
		self._change_listener = method
		return method




class IntFieldInstance (_PrimitiveFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.IntLiteral(format='decimal', numType='int', value=repr(self.value))

class IntField (_PrimitiveField):
	__field_instance_class__ = IntFieldInstance
	__primitive_type__ = int



class FloatFieldInstance (_PrimitiveFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.FloatLiteral(value=repr(self.value))

class FloatField (_PrimitiveField):
	__field_instance_class__ = FloatFieldInstance
	__primitive_type__ = float



class StringFieldInstance (_PrimitiveFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.strToStrLiteral(self.value)

class StringField (_PrimitiveField):
	__field_instance_class__ = StringFieldInstance
	__primitive_type__ = str







class ListFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, source_value):
		super(ListFieldInstance, self).__init__(field, object_instance, source_value)

		def on_change(old_contents, new_contents):
			if self._field._change_listener is not None:
				self._field._change_listener(self._object_instance, self, old_contents, new_contents)

		self.__live = LiveList(source_value)

		self.__live.changeListener = on_change

		if source_value is not None:
			on_change([], source_value)



	@property
	def value(self):
		return self.__live

	@value.setter
	def value(self, xs):
		self.__live[:] = xs


	def _addTrackableContentsTo(self, contents):
		contents.append(self.__live)

	def __field_getstate__(self):
		return self.__live[:]

	def getValueForEditor(self):
		return self.__live[:]

	def __py_evalmodel__(self, codeGen):
		return Py.ListLiteral(values=[n.__py_evalmodel__(codeGen)   for n in self.__live])



class ListField (Field):
	__field_instance_class__ = ListFieldInstance



	def __init__(self):
		super(ListField, self).__init__()
		self._change_listener = None


	def on_change(self, method):
		self._change_listener = method
		return method




#
#
# GUI Node class
#
#


class NodeAlreadyHasParentError (Exception):
	pass


class _GUIObjectClass (type):
	def __init__(cls, name, bases, attrs):
		super(_GUIObjectClass, cls).__init__(name, bases, attrs)
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
				value._classInit(cls, name)
				fields[name] = value

		cls._gui_fields__ = fields



class GUIObject (object):
	__metaclass__ = _GUIObjectClass


	def __init__(self, **values):
		for name in values:
			if name not in self._gui_fields__:
				raise TypeError, 'Class \'{0}\' does not have a field named \'{1}\''.format(type(self).__name__, name)

		self.__change_history__ = None
		for field in self._gui_fields__.values():
			field._instanceInit(self, values.get(field._name))


	def __getstate__(self):
		return {name: field._getFieldState(self)   for name, field in self._gui_fields__.items()}


	def __setstate__(self, state):
		self.__change_history__ = None
		self._parent = None
		for field in self._gui_fields__.values():
			field._instanceInit(self, state.get(field._name))


	def __get_trackable_contents__(self):
		contents = []
		for field in self._gui_fields__.values():
			field._getFieldInstance(self)._addTrackableContentsTo(contents)
		return contents



class GUINode (GUIObject):
	def __init__(self, **values):
		self._parent = None
		super(GUINode, self).__init__(**values)


	@property
	def parent(self):
		return self._parent



	def _registerChild(self, childNode):
		if childNode._parent is not None:
			raise NodeAlreadyHasParentError, 'Node \'{0}\' already has a parent'.format(childNode)
		childNode._parent = self

	def _unregisterChild(self, childNode):
		if childNode._parent is not self:
			raise RuntimeError, 'Cannot unregister component that is not a child of this'
		childNode._parent = None




#
#
# Child fields
#
#

class ChildFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, source_value):
		super(ChildFieldInstance, self).__init__(field, object_instance, source_value)

		def on_change(old_value, new_value):
			if new_value is not old_value:
				if old_value is not None:
					object_instance._unregisterChild(old_value)
				if new_value is not None:
					object_instance._registerChild(new_value)

		self.__live = TrackedLiveValue(source_value)
		self.__live.changeListener = on_change

		if source_value is not None:
			on_change(None, source_value)



	@property
	def node(self):
		return self.__live.getValue()

	@node.setter
	def node(self, x):
		self.__live.setLiteralValue(x)


	def _addTrackableContentsTo(self, contents):
		contents.append(self.__live)

	def __field_getstate__(self):
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

	def _classInit(self, containingClass, name):
		if not issubclass(containingClass, GUINode):
			raise TypeError, 'Only subclasses of GUINode may contain child fields'
		super(ChildField, self)._classInit(containingClass, name)




class ChildListFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, source_value):
		super(ChildListFieldInstance, self).__init__(field, object_instance, source_value)

		def on_change(old_contents, new_contents):
			o = set(old_contents)
			n = set(new_contents)
			removed = o - n
			added = n - o
			for n in removed:
				object_instance._unregisterChild(n)
			for n in added:
				object_instance._registerChild(n)

		self.__live = LiveList(source_value)

		self.__live.changeListener = on_change

		if source_value is not None:
			on_change([], source_value)



	@property
	def nodes(self):
		return self.__live


	def _addTrackableContentsTo(self, contents):
		contents.append(self.__live)

	def __field_getstate__(self):
		return self.__live[:]

	def getValueForEditor(self):
		return self.__live[:]

	def __py_evalmodel__(self, codeGen):
		return Py.ListLiteral(values=[n.__py_evalmodel__(codeGen)   for n in self.__live])


class ChildListField (Field):
	__field_instance_class__ = ChildListFieldInstance

	def _classInit(self, containingClass, name):
		if not issubclass(containingClass, GUINode):
			raise TypeError, 'Only subclasses of GUINode may contain child fields'
		super(ChildListField, self)._classInit(containingClass, name)





exprBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.0, 0.25, 0.75 ), None )




class _EvalFieldState (object):
	def __init__(self, constantValue, expr):
		self.constantValue = constantValue
		self.expr = expr

class _EvalFieldInstance (FieldInstance):
	"""
	A field that contains a value, which can alternatively have an expression that generates the required value
	"""
	def __init__(self, field, object_instance, source_value):
		super(_EvalFieldInstance, self).__init__(field, object_instance, source_value)

		self.__change_history__ = None

		if source_value is not None:
			if isinstance(source_value, _EvalFieldState):
				constantValue = source_value.constantValue
				expr = source_value.expr
			else:
				constantValue = source_value
				expr = None
		else:
			constantValue = field._defaultValue
			expr = None

		self.__live = TrackedLiveValue(constantValue)
		self.__expr = expr
		self.__incr = IncrementalValueMonitor()



	def isConstant(self):
		return self.__expr is None


	@property
	def constantValue(self):
		return self.__live.getValue()

	@constantValue.setter
	def constantValue(self, x):
		self.__live.setLiteralValue(x)



	@property
	def constantValueLive(self):
		return self.__live


	@property
	def expr(self):
		return self.__expr

	@expr.setter
	def expr(self, exp):
		oldExpr = self.__expr
		self.__expr = exp

		if self.__change_history__ is not None:
			if oldExpr is not None:
				self.__change_history__.stopTracking(oldExpr)
			def setExpression():
				self.expr = exp
			def revertExpression():
				self.expr = oldExpr
			self.__change_history__.addChange(setExpression, revertExpression, 'Set expression')
			if exp is not None:
				self.__change_history__.track(exp)

		self.__incr.onChanged()




	def _addTrackableContentsTo(self, contents):
		contents.append(self)

	def __field_getstate__(self):
		return _EvalFieldState(self.__live.getValue(), self.__expr)


	def getValueForEditor(self):
		return self.__live.getValue()

	def __py_evalmodel__(self, codeGen):
		self.__incr.onAccess()
		if self.__expr is None:
			return self.__fixedvalue_py_evalmodel__(self.__live.getValue(), codeGen)
		else:
			return self.__expr.model


	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		raise NotImplementedError, 'abstract'


	def __get_trackable_contents__(self):
		if self.__expr is not None:
			return [self.__live, self.__expr]
		else:
			return [self.__live]



	def editUI(self):
		self.__incr.onAccess()
		valueControl = self._field.__edit_ui_make_control__(self.__live)

		if self.__expr is None:
			def _onAdd(button, event):
				self.expr = EmbeddedPython2Expr()

			addButton = Button(self._addButtonContents, _onAdd)

			return Row([valueControl, Spacer(10.0, 0.0), addButton])
		else:
			def _onRemove(button, event):
				self.expr = None

			removeButton = Button(self._removeButtonContents, _onRemove)

			return Column([valueControl, Row([removeButton, Spacer(10.0, 0.0), exprBorder.surround(self.__expr)])])


	_addStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.5, 0.0)), Primitive.fontBold(True), Primitive.fontSize(11))
	_removeStyle = StyleSheet.style(Primitive.foreground(Color(0.5, 0.0, 0.0)), Primitive.fontBold(True), Primitive.fontSize(11))
	_fStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.25, 0.5)), Primitive.fontItalic(True), Primitive.fontSize(11))
	_parenStyle = StyleSheet.style(Primitive.foreground(Color(0.3, 0.3, 0.3)), Primitive.fontSize(11))

	_addButtonContents = Row([_addStyle(Label('+ ')), _fStyle(Label('f')), _parenStyle(Label('()'))])
	_removeButtonContents = Row([_removeStyle(Label('- ')), _fStyle(Label('f')), _parenStyle(Label('()'))])










class _EvalField (Field):
	__primitive_type__ = None


	def __init__(self, defaultValue, controlFactoryFn):
		if self.__primitive_type__ is None:
			raise NotImplementedError, 'Value field class \'{0}\' is abstract; __primitive_type__ not defined'.format(type(self).__name__)
		if defaultValue is None:
			defaultValue = self.__primitive_type__()
		else:
			if not isinstance(defaultValue, self.__primitive_type__):
				raise TypeError, 'Default value is not an instance of \'{0}\''.format(self.__primitive_type__.__name__)

		super(_EvalField, self).__init__()
		self._defaultValue = defaultValue
		self.__controlFactoryFn = controlFactoryFn


	def __edit_ui_make_control__(self, live):
		return self.__controlFactoryFn(live)




class IntEvalFieldInstance (_EvalFieldInstance):
	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		return Py.IntLiteral(format='decimal', numType='int', value=repr(value))


class IntEvalField (_EvalField):
	__field_instance_class__ = IntEvalFieldInstance
	__primitive_type__ = int



class FloatEvalFieldInstance (_EvalFieldInstance):
	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		return Py.FloatLiteral(value=repr(value))


class FloatEvalField (_EvalField):
	__field_instance_class__ = FloatEvalFieldInstance
	__primitive_type__ = float



class StringEvalFieldInstance (_EvalFieldInstance):
	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		return Py.strToStrLiteral(value)


class StringEvalField (_EvalField):
	__field_instance_class__ = StringEvalFieldInstance
	__primitive_type__ = str







class ExprFieldInstance (FieldInstance):
	"""
	A field that contains an expression that generates the required value
	"""
	def __init__(self, field, object_instance, source_value):
		super(ExprFieldInstance, self).__init__(field, object_instance, source_value)

		if source_value is None:
			source_value = EmbeddedPython2Expr()

		self.__expr = source_value



	@property
	def expr(self):
		return self.__expr




	def _addTrackableContentsTo(self, contents):
		contents.append(self.__expr)

	def __field_getstate__(self):
		return self.__expr


	def getValueForEditor(self):
		return None

	def __py_evalmodel__(self, codeGen):
		return self.__expr.model


	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		raise NotImplementedError, 'abstract'



	def editUI(self):
		return exprBorder.surround(self.__expr)





class ExprField (Field):
	__field_instance_class__ = ExprFieldInstance







#
#
# UNIT TESTS
#
#


import unittest
import sys
import imp
import pickle


class TestCase_DataModel(unittest.TestCase):
	@classmethod
	def setUpClass(cls):
		class PrimitiveFieldTest (GUINode):
			x = IntField(0)
			y = IntField(1)

			@x.on_change
			def x_changed(self, field, old_value, new_value):
				self.x_history.append(old_value)

			def __init__(self, **values):
				self.x_history = []
				super(PrimitiveFieldTest, self).__init__(**values)

			def __py_evalmodel__(self, codeGen):
				x = self.x.__py_evalmodel__(codeGen)
				y = self.y.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='A'), args=[x, y])


		class ListFieldTest (GUIObject):
			x = ListField()

			@x.on_change
			def x_changed(self, field, old_contents, new_contents):
				self.history.append(old_contents)


			def __init__(self, **values):
				self.history = []
				super(ListFieldTest, self).__init__(**values)

			def __setstate__(self, state):
				self.history = []
				super(ListFieldTest, self).__setstate__(state)




		class ChildFieldTest (GUINode):
			p = ChildField()
			q = ChildListField()

			def __py_evalmodel__(self, codeGen):
				p = self.p.__py_evalmodel__(codeGen)
				q = self.q.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='B'), args=[p, q])

		class EvalFieldTest (GUIObject):
			x = FloatEvalField(0.0, lambda live: Label('x'))
			y = FloatEvalField(1.0, lambda live: Label('y'))

			def __py_evalmodel__(self, codeGen):
				x = self.x.__py_evalmodel__(codeGen)
				y = self.y.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='C'), args=[x, y])

		class ExprFieldTest (GUIObject):
			x = ExprField()

			def __py_evalmodel__(self, codeGen):
				x = self.x.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='D'), args=[x])

		cls.PrimitiveFieldTest = PrimitiveFieldTest
		cls.ListFieldTest = ListFieldTest
		cls.ChildFieldTest = ChildFieldTest
		cls.EvalFieldTEst = EvalFieldTest
		cls.ExprFieldTest = ExprFieldTest

		test_module = imp.new_module('GUIEditor_DataModel_Tests')
		sys.modules[test_module.__name__] = test_module
		cls.mod = test_module

		def moveClassToModule(mod, x):
			setattr(mod, x.__name__, x)
			x.__module__ = mod.__name__

		moveClassToModule(test_module, PrimitiveFieldTest)
		moveClassToModule(test_module, ListFieldTest)
		moveClassToModule(test_module, ChildFieldTest)
		moveClassToModule(test_module, EvalFieldTest)
		moveClassToModule(test_module, ExprFieldTest)


	@classmethod
	def tearDownClass(cls):
		cls.PrimitiveFieldTest = None
		cls.ListFieldTest = None
		cls.ChildFieldTest = None
		cls.EvalFieldTest = None
		cls.ExprFieldTest = None
		del sys.modules[cls.mod.__name__]
		cls.mod = None



	def setUp(self):
		self.ch = ChangeHistory()

	def tearDown(self):
		self.ch = None


	@staticmethod
	def buildFromState(cls, state):
		instance = cls.__new__(cls)
		instance.__setstate__(state)
		return instance



	def test_PrimitiveField_constructor(self):
		a1 = self.PrimitiveFieldTest()

		self.assertEqual(0, a1.x.value)
		self.assertEqual(1, a1.y.value)

		a2 = self.PrimitiveFieldTest(x=10, y=20)

		self.assertEqual(10, a2.x.value)
		self.assertEqual(20, a2.y.value)

		self.assertRaises(TypeError, lambda: self.PrimitiveFieldTest(a=1, b=2))



	def test_PrimitiveField_change_listener(self):
		a = self.PrimitiveFieldTest()

		self.assertEqual(0, a.x.value)
		self.assertEqual(1, a.y.value)
		self.assertEqual([], a.x_history)

		a.x.value = 10
		a.y.value = 20

		self.assertEqual(10, a.x.value)
		self.assertEqual(20, a.y.value)
		self.assertEqual([0], a.x_history)

		a.x.value = 5
		a.y.value = 6

		self.assertEqual(5, a.x.value)
		self.assertEqual(6, a.y.value)
		self.assertEqual([0, 10], a.x_history)



	def test_PrimitiveField_changeHistory(self):
		a = self.PrimitiveFieldTest()

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
		a = self.PrimitiveFieldTest(x=10, y=20)

		a_io = pickle.loads(pickle.dumps(a))

		self.assertIsNot(a, a_io)

		self.assertEqual(10, a_io.x.value)
		self.assertEqual(20, a_io.y.value)



	def test_PrimitiveField_editor(self):
		a = self.PrimitiveFieldTest(x=10, y=20)

		codeGen = Python2CodeGenerator('test')

		self.assertEqual(10, a.x.getValueForEditor())
		self.assertEqual(20, a.y.getValueForEditor())

		self.assertEqual(Py.IntLiteral(format='decimal', numType='int', value='10'), a.x.__py_evalmodel__(codeGen))
		self.assertEqual(Py.IntLiteral(format='decimal', numType='int', value='20'), a.y.__py_evalmodel__(codeGen))

		self.assertEqual(Py.Call(target=Py.Load(name='A'), args=[
			Py.IntLiteral(format='decimal', numType='int', value='10'),
			Py.IntLiteral(format='decimal', numType='int', value='20')
		]), a.__py_evalmodel__(codeGen))




	def test_ListField_constructor(self):
		a1 = self.ListFieldTest()
		a2 = self.ListFieldTest(x=range(5))

		self.assertEqual([], a1.x.value)
		self.assertEqual(range(5), a2.x.value)



	def test_ListField_change_listener(self):
		a1 = self.ListFieldTest()

		self.assertEqual([], a1.x.value)
		self.assertEqual([], a1.history)

		a1.x.value.append(10)

		self.assertEqual([10], a1.x.value)
		self.assertEqual([[]], a1.history)

		a1.x.value.append(20)

		self.assertEqual([10, 20], a1.x.value)
		self.assertEqual([[], [10]], a1.history)



	def test_ListField_changeHistory(self):
		a = self.ListFieldTest()

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		self.ch.track(a)

		self.assertIs(ChangeHistory.getChangeHistoryFor(a), self.ch)

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		a.x.value.append(10)
		a.x.value.append(20)

		self.assertEqual([10, 20], a.x.value)
		self.assertEqual(2, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual([10], a.x.value)
		self.assertEqual(1, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual([], a.x.value)
		self.assertEqual(0, self.ch.getNumUndoChanges())



	def test_ListField_serialisation(self):
		a = self.ListFieldTest(x=range(5))

		a_io = pickle.loads(pickle.dumps(a))

		self.assertIsNot(a, a_io)

		self.assertEqual(range(5), a_io.x.value)



	def test_ListField_editor(self):
		a = self.ListFieldTest(x=[1,2])

		codeGen = Python2CodeGenerator('test')

		self.assertEqual([1, 2], a.x.getValueForEditor())

		self.fail('Test imcomplete; need to implement type-coercion based converter to transform Python objects into AST nodes that construct their literal values')




	def test_ChildField_constructor(self):
		b1 = self.ChildFieldTest()
		a1 = self.PrimitiveFieldTest()
		a2 = self.PrimitiveFieldTest()

		self.assertIs(None, b1.p.node)
		self.assertEqual([], b1.q.nodes)

		b2 = self.ChildFieldTest(p=a1, q=[a2])

		self.assertIs(a1, b2.p.node)
		self.assertEqual([a2], b2.q.nodes)

		self.assertRaises(NodeAlreadyHasParentError, lambda: self.ChildFieldTest(p=a1, q=[a2]))



	def test_ChildField_parentage(self):
		a1 = self.PrimitiveFieldTest(x=10, y=20)
		a2 = self.PrimitiveFieldTest(x=3, y=4)
		b = self.ChildFieldTest()

		self.assertIs(None, a1.parent)
		self.assertIs(None, a2.parent)

		b.p.node = a1

		self.assertIs(b, a1.parent)
		self.assertIs(None, a2.parent)

		b.q.nodes.append(a2)

		self.assertIs(b, a1.parent)
		self.assertIs(b, a2.parent)



	def test_ChildField_changeHistory(self):
		b = self.ChildFieldTest()
		a1 = self.PrimitiveFieldTest()
		a2 = self.PrimitiveFieldTest()
		a3 = self.PrimitiveFieldTest()

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
		a1 = self.PrimitiveFieldTest(x=10, y=20)
		a2 = self.PrimitiveFieldTest(x=3, y=4)
		b = self.ChildFieldTest(p=a1, q=[a2])

		b_io = pickle.loads(pickle.dumps(b))

		self.assertIsNot(b, b_io)

		self.assertIsInstance(b_io.p.node, self.PrimitiveFieldTest)
		self.assertIsInstance(b_io.q.nodes[0], self.PrimitiveFieldTest)

		self.assertEqual(10, b_io.p.node.x.value)
		self.assertEqual(3, b_io.q.nodes[0].x.value)



	def test_ChildField_editor(self):
		a1 = self.PrimitiveFieldTest(x=10, y=20)
		a2 = self.PrimitiveFieldTest(x=3, y=4)
		b = self.ChildFieldTest(p=a1, q=[a2])

		codeGen = Python2CodeGenerator('test')

		self.assertEqual(a1, b.p.getValueForEditor())
		self.assertEqual([a2], b.q.getValueForEditor())

		self.assertEqual(Py.Call(target=Py.Load(name='B'), args=[
			Py.Call(target=Py.Load(name='A'), args=[Py.IntLiteral(format='decimal', numType='int', value='10'), Py.IntLiteral(format='decimal', numType='int', value='20')]),
			Py.ListLiteral(values=[Py.Call(target=Py.Load(name='A'), args=[Py.IntLiteral(format='decimal', numType='int', value='3'), Py.IntLiteral(format='decimal', numType='int', value='4')])])
		]),  b.__py_evalmodel__(codeGen))





	def test_EvalField_constructor(self):
		c1 = self.EvalFieldTEst()

		self.assertEqual(0.0, c1.x.constantValue)
		self.assertEqual(1.0, c1.y.constantValue)

		c2 = self.EvalFieldTEst(x=10.0, y=20.0)

		self.assertEqual(10.0, c2.x.constantValue)
		self.assertEqual(20.0, c2.y.constantValue)



	def test_EvalField_changeHistory(self):
		c = self.EvalFieldTEst()

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		self.ch.track(c)

		self.assertIs(ChangeHistory.getChangeHistoryFor(c), self.ch)

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		c.x.constantValue = 10.0
		c.y.constantValue = 20.0

		self.assertEqual(10.0, c.x.constantValue)
		self.assertEqual(20.0, c.y.constantValue)
		self.assertIsNone(c.x.expr)
		self.assertIsNone(c.y.expr)
		self.assertEqual(2, self.ch.getNumUndoChanges())

		x_expr = EmbeddedPython2Expr.fromText('a+b')
		y_expr = EmbeddedPython2Expr.fromText('c+d')

		self.assertIs(ChangeHistory.getChangeHistoryFor(x_expr), None)
		self.assertIs(ChangeHistory.getChangeHistoryFor(y_expr), None)

		c.x.expr = x_expr
		c.y.expr = y_expr

		self.assertIs(ChangeHistory.getChangeHistoryFor(x_expr), self.ch)
		self.assertIs(ChangeHistory.getChangeHistoryFor(y_expr), self.ch)

		self.assertEqual(10.0, c.x.constantValue)
		self.assertEqual(20.0, c.y.constantValue)
		self.assertIs(x_expr, c.x.expr)
		self.assertIs(y_expr, c.y.expr)
		self.assertEqual(4, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual(10.0, c.x.constantValue)
		self.assertEqual(20.0, c.y.constantValue)
		self.assertIs(x_expr, c.x.expr)
		self.assertIsNone(c.y.expr)
		self.assertEqual(3, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual(10.0, c.x.constantValue)
		self.assertEqual(20.0, c.y.constantValue)
		self.assertIsNone(c.x.expr)
		self.assertIsNone(c.y.expr)
		self.assertEqual(2, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual(10.0, c.x.constantValue)
		self.assertEqual(1.0, c.y.constantValue)
		self.assertIsNone(c.x.expr)
		self.assertIsNone(c.y.expr)
		self.assertEqual(1, self.ch.getNumUndoChanges())

		self.ch.undo()

		self.assertEqual(0.0, c.x.constantValue)
		self.assertEqual(1.0, c.y.constantValue)
		self.assertIsNone(c.x.expr)
		self.assertIsNone(c.y.expr)
		self.assertEqual(0, self.ch.getNumUndoChanges())



	def test_EvalField_serialisation(self):
		c = self.EvalFieldTEst()

		c.x.constantValue = 10.0
		c.y.constantValue = 20.0
		x_expr = EmbeddedPython2Expr.fromText('a+b')
		y_expr = EmbeddedPython2Expr.fromText('c+d')
		c.x.expr = x_expr
		c.y.expr = y_expr

		c_io = pickle.loads(pickle.dumps(c))

		self.assertEqual(10.0, c_io.x.constantValue)
		self.assertEqual(20.0, c_io.y.constantValue)
		self.assertEqual(x_expr, c_io.x.expr)
		self.assertEqual(y_expr, c_io.y.expr)



	def test_EvalField_editor(self):
		c = self.EvalFieldTEst()

		codeGen = Python2CodeGenerator('test')

		c.x.constantValue = 10.0
		c.y.constantValue = 20.0

		self.assertEqual(10.0, c.x.getValueForEditor())
		self.assertEqual(20.0, c.y.getValueForEditor())

		self.assertEqual(Py.Call(target=Py.Load(name='C'), args=[Py.FloatLiteral(value='10.0'), Py.FloatLiteral(value='20.0')]), c.__py_evalmodel__(codeGen))

		x_expr = EmbeddedPython2Expr.fromText('a+b')
		y_expr = EmbeddedPython2Expr.fromText('c+d')
		c.x.expr = x_expr
		c.y.expr = y_expr

		self.assertEqual(10.0, c.x.getValueForEditor())
		self.assertEqual(20.0, c.y.getValueForEditor())

		self.assertEqual(Py.Call(target=Py.Load(name='C'), args=[x_expr.model, y_expr.model]), c.__py_evalmodel__(codeGen))




	def test_ExprField_changeHistory(self):
		d = self.ExprFieldTest()

		self.assertEqual(0, self.ch.getNumUndoChanges())
		self.assertEqual(0, self.ch.getNumRedoChanges())

		self.ch.track(d)

		self.assertIs(ChangeHistory.getChangeHistoryFor(d), self.ch)
		self.assertIs(ChangeHistory.getChangeHistoryFor(d.x.expr), self.ch)



	def test_ExprField_serialisation(self):
		x_expr =EmbeddedPython2Expr.fromText('a+b')
		d = self.ExprFieldTest(x=x_expr)

		d_io = pickle.loads(pickle.dumps(d))

		self.assertEqual(x_expr, d_io.x.expr)



	def test_ExprField_py_evalmodel(self):
		x_expr =EmbeddedPython2Expr.fromText('a+b')
		d = self.ExprFieldTest(x=x_expr)

		codeGen = Python2CodeGenerator('test')

		self.assertEqual(Py.Call(target=Py.Load(name='D'), args=[x_expr.model]), d.__py_evalmodel__(codeGen))



