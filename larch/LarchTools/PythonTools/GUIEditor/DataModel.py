##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
import copy

from java.awt import Color
import java.lang.Enum

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
	def __init__(self, field, object_instance, wrapped_source_value):
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


	def _instanceInit(self, object_instance, wrapped_source_value):
		if self._name is None:
			raise TypeError, 'Field not initialised'
		if self.__field_instance_class__ is None:
			raise NotImplementedError, 'Field class \'{0}\' is abstract; __field_instance_class__ not defined'.format(type(self).__name__)
		fieldInstance = self.__field_instance_class__(self, object_instance, wrapped_source_value)
		setattr(object_instance, self._attrName, fieldInstance)


	def _getFieldInstance(self, object_instance):
		if self._name is None:
			raise TypeError, 'Field not initialised'
		return getattr(object_instance, self._attrName)


	def _getFieldState(self, object_instance):
		return self._getFieldInstance(object_instance).__field_getstate__()

	def _convertValueFromState(self, object_instance, stateValue):
		return stateValue




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







def _checkValueType(value, valueType, valueName):
	if isinstance(valueType, type):
		if not isinstance(value, valueType):
			raise TypeError, '{0} is not an instance of \'{1}\''.format(valueName, valueType.__name__)
	else:
		for t in valueType:
			if isinstance(value, t):
				return
		raise TypeError, '{0} is not an instance of \'{1}\''.format(valueName, ', or '.join([t.__name__   for t in valueType]))






#
# PRIMITIVE FIELDS
#

class ValueFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, wrapped_source_value):
		super(ValueFieldInstance, self).__init__(field, object_instance, wrapped_source_value)
		value = wrapped_source_value[0]   if wrapped_source_value is not None   else field._defaultValue

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

	def __py_evalmodel__(self, codeGen):
		return Py.coerceToModel(self.__live.getValue())





class TypedField (Field):
	__field_instance_class__ = ValueFieldInstance

	def __init__(self, valueType, defaultValue):
		_checkValueType(defaultValue, valueType, 'default value')
		super(TypedField, self).__init__()
		self._defaultValue = defaultValue
		self._change_listener = None


	def on_change(self, method):
		self._change_listener = method
		return method




class ValueField (Field):
	__field_instance_class__ = ValueFieldInstance


	def __init__(self, defaultValue=None):
		super(ValueField, self).__init__()
		self._change_listener = None
		self._defaultValue = defaultValue


	def on_change(self, method):
		self._change_listener = method
		return method




class IntFieldInstance (ValueFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.IntLiteral(format='decimal', numType='int', value=repr(self.value))

class IntField (TypedField):
	__field_instance_class__ = IntFieldInstance
	__primitive_type__ = int



class FloatFieldInstance (ValueFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.FloatLiteral(value=repr(self.value))

class FloatField (TypedField):
	__field_instance_class__ = FloatFieldInstance
	__primitive_type__ = float



class StringFieldInstance (ValueFieldInstance):
	def __py_evalmodel__(self, codeGen):
		return Py.strToStrLiteral(self.value)

class StringField (TypedField):
	__field_instance_class__ = StringFieldInstance
	__primitive_type__ = str






class EnumFieldInstance (ValueFieldInstance):
	def __field_getstate__(self):
		return str(self.value)


	def __py_evalmodel__(self, codeGen):
		return codeGen.embeddedValue(self.value)


class EnumField (Field):
	__field_instance_class__ = EnumFieldInstance


	def __init__(self, enumType, defaultValue):
		if not issubclass(enumType, java.lang.Enum):
			raise TypeError, 'enumType must be a sublcass of java.lang.Enum'
		if defaultValue is None:
			values = enumType.values()
			defaultValue = values[0]   if len(values) > 0   else None
		else:
			if not isinstance(defaultValue, enumType):
				raise TypeError, 'Default value is not an instance of \'{0}\''.format(enumType.__name__)
		super(EnumField, self).__init__()
		self._defaultValue = defaultValue
		self.__enumType = enumType
		self._change_listener = None


	def on_change(self, method):
		self._change_listener = method
		return method


	def _convertValueFromState(self, object_instance, stateValue):
		return self.__enumType.valueOf(stateValue)






class ListFieldInstance (FieldInstance):
	def __init__(self, field, object_instance, wrapped_source_value):
		super(ListFieldInstance, self).__init__(field, object_instance, wrapped_source_value)

		def on_change(old_contents, new_contents):
			if self._field._change_listener is not None:
				self._field._change_listener(self._object_instance, self, old_contents, new_contents)

		self.__live = LiveList(wrapped_source_value[0]   if wrapped_source_value is not None   else None)

		self.__live.changeListener = on_change

		if wrapped_source_value is not None:
			on_change([], wrapped_source_value)



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
			value = (values[field._name],)   if field._name in values   else None
			field._instanceInit(self, value)


	def __getstate__(self):
		return {name: field._getFieldState(self)   for name, field in self._gui_fields__.items()}


	def __setstate__(self, state):
		self.__change_history__ = None
		self._parent = None
		for field in self._gui_fields__.values():
			value = (field._convertValueFromState(self, state[field._name]),)   if field._name in state   else None
			field._instanceInit(self, value)


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
	def __init__(self, field, object_instance, wrapped_source_value):
		super(ChildFieldInstance, self).__init__(field, object_instance, wrapped_source_value)

		def on_change(old_value, new_value):
			if new_value is not old_value:
				if old_value is not None:
					object_instance._unregisterChild(old_value)
				if new_value is not None:
					object_instance._registerChild(new_value)

		source_value = wrapped_source_value[0]   if wrapped_source_value is not None   else None
		self.__live = TrackedLiveValue(source_value)
		self.__live.changeListener = on_change

		if wrapped_source_value is not None:
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
	def __init__(self, field, object_instance, wrapped_source_value):
		super(ChildListFieldInstance, self).__init__(field, object_instance, wrapped_source_value)

		def on_change(old_contents, new_contents):
			o = set(old_contents)
			n = set(new_contents)
			removed = o - n
			added = n - o
			for n in removed:
				object_instance._unregisterChild(n)
			for n in added:
				object_instance._registerChild(n)

		source_value = wrapped_source_value[0]   if wrapped_source_value is not None   else None
		self.__live = LiveList(source_value)

		self.__live.changeListener = on_change

		if wrapped_source_value is not None:
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



	def __eq__(self, other):
		if isinstance(other, _EvalFieldState):
			return self.constantValue == other.constantValue  and  self.expr == other.expr
		else:
			return False

	def __ne__(self, other):
		if isinstance(other, _EvalFieldState):
			return self.constantValue != other.constantValue  or  self.expr != other.expr
		else:
			return True


	def __hash__(self):
		return hash((self.constantValue, self.expr))



class EvalFieldInstance (FieldInstance):
	"""
	A field that contains a value, which can alternatively have an expression that generates the required value
	"""
	def __init__(self, field, object_instance, wrapped_source_value):
		super(EvalFieldInstance, self).__init__(field, object_instance, wrapped_source_value)

		self.__change_history__ = None

		if wrapped_source_value is not None:
			source_value = wrapped_source_value[0]
			if isinstance(source_value, _EvalFieldState):
				constantValue = source_value.constantValue
				expr = source_value.expr
			else:
				constantValue = source_value
				expr = None
		else:
			constantValue = field._defaultValue
			expr = None

		self._live = TrackedLiveValue(constantValue)
		self._expr = expr
		self.__incr = IncrementalValueMonitor()



	def isConstant(self):
		return self._expr is None


	@property
	def constantValue(self):
		return self._live.getValue()

	@constantValue.setter
	def constantValue(self, x):
		self._live.setLiteralValue(x)



	@property
	def constantValueLive(self):
		return self._live


	@property
	def expr(self):
		return self._expr

	@expr.setter
	def expr(self, exp):
		oldExpr = self._expr
		self._expr = exp

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
		return _EvalFieldState(self._live.getValue(), self._expr)


	def getValueForEditor(self):
		return self._live.getValue()

	def __py_evalmodel__(self, codeGen):
		self.__incr.onAccess()
		if self._expr is None:
			return self.__fixedvalue_py_evalmodel__(self._live.getValue(), codeGen)
		else:
			return self._expr.model


	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		return Py.coerceToModel(value)


	def __get_trackable_contents__(self):
		if self._expr is not None:
			return [self._live, self._expr]
		else:
			return [self._live]



	def editUI(self):
		self.__incr.onAccess()
		valueControl = self._field.__edit_ui_make_control__(self._live)

		if self._expr is None:
			def _onAdd(button, event):
				self.expr = EmbeddedPython2Expr()

			addButton = Button(self._addButtonContents, _onAdd).alignHPack()

			return Row([valueControl, Spacer(10.0, 0.0), addButton])
		else:
			def _onRemove(button, event):
				self.expr = None

			removeButton = Button(self._removeButtonContents, _onRemove).alignHPack()

			return Column([valueControl, Row([removeButton, Spacer(10.0, 0.0), exprBorder.surround(self._expr)])])


	_addStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.5, 0.0)), Primitive.fontBold(True), Primitive.fontSize(11))
	_removeStyle = StyleSheet.style(Primitive.foreground(Color(0.5, 0.0, 0.0)), Primitive.fontBold(True), Primitive.fontSize(11))
	_fStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.25, 0.5)), Primitive.fontItalic(True), Primitive.fontSize(11))
	_parenStyle = StyleSheet.style(Primitive.foreground(Color(0.3, 0.3, 0.3)), Primitive.fontSize(11))

	_addButtonContents = Row([_addStyle(Label('+ ')), _fStyle(Label('f')), _parenStyle(Label('()'))])
	_removeButtonContents = Row([_removeStyle(Label('- ')), _fStyle(Label('f')), _parenStyle(Label('()'))])










class TypedEvalField (Field):
	__field_instance_class__ = EvalFieldInstance
	__primitive_type__ = None


	def __init__(self, valueType, defaultValue, controlFactoryFn):
		_checkValueType(defaultValue, valueType, 'default value')
		super(TypedEvalField, self).__init__()
		self._defaultValue = defaultValue
		self.__controlFactoryFn = controlFactoryFn


	def __edit_ui_make_control__(self, live):
		return self.__controlFactoryFn(live)








class EnumEvalFieldInstance (EvalFieldInstance):
	def __field_getstate__(self):
		return _EvalFieldState(str(self._live.getValue()), self._expr)

	def __fixedvalue_py_evalmodel__(self, value, codeGen):
		return codeGen.embeddedValue(value)


class EnumEvalField (Field):
	__field_instance_class__ = EnumEvalFieldInstance


	def __init__(self, enumType, defaultValue, controlFactoryFn):
		if not issubclass(enumType, java.lang.Enum):
			raise TypeError, 'enumType must be a sublcass of java.lang.Enum'
		if defaultValue is None:
			values = enumType.values()
			defaultValue = values[0]   if len(values) > 0   else None
		else:
			if not isinstance(defaultValue, enumType):
				raise TypeError, 'Default value is not an instance of \'{0}\''.format(enumType.__name__)

		super(EnumEvalField, self).__init__()
		self._defaultValue = defaultValue
		self.__enumType = enumType
		self.__controlFactoryFn = controlFactoryFn


	def __edit_ui_make_control__(self, live):
		return self.__controlFactoryFn(live)





class ExprFieldInstance (FieldInstance):
	"""
	A field that contains an expression that generates the required value
	"""
	def __init__(self, field, object_instance, wrapped_source_value):
		super(ExprFieldInstance, self).__init__(field, object_instance, wrapped_source_value)

		if wrapped_source_value is None:
			source_value = EmbeddedPython2Expr()
		else:
			source_value = wrapped_source_value[0]

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
		class TypedFieldTest (GUINode):
			x = TypedField(int, 0)
			y = TypedField(int, 1)
			z = TypedField([int, type(None)], None)

			@x.on_change
			def x_changed(self, field, old_value, new_value):
				self.x_history.append(old_value)

			def __init__(self, **values):
				self.x_history = []
				super(TypedFieldTest, self).__init__(**values)

			def __py_evalmodel__(self, codeGen):
				x = self.x.__py_evalmodel__(codeGen)
				y = self.y.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='A'), args=[x, y])


		from BritefuryJ.LSpace.Layout import HAlignment


		class EnumFieldTest (GUIObject):
			x = EnumField(HAlignment, HAlignment.PACK)


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
			x = TypedEvalField(float, 0.0, lambda live: Label('x'))
			y = TypedEvalField(float, 1.0, lambda live: Label('y'))

			def __py_evalmodel__(self, codeGen):
				x = self.x.__py_evalmodel__(codeGen)
				y = self.y.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='C'), args=[x, y])

		class EnumEvalFieldTest (GUIObject):
			x = EnumEvalField(HAlignment, HAlignment.PACK, lambda live: Label('x'))


		class ExprFieldTest (GUIObject):
			x = ExprField()

			def __py_evalmodel__(self, codeGen):
				x = self.x.__py_evalmodel__(codeGen)
				return Py.Call(target=Py.Load(name='D'), args=[x])

		cls.TypedFieldTest = TypedFieldTest
		cls.EnumFieldTest = EnumFieldTest
		cls.ListFieldTest = ListFieldTest
		cls.ChildFieldTest = ChildFieldTest
		cls.EvalFieldTest = EvalFieldTest
		cls.EnumEvalFieldTest = EnumEvalFieldTest
		cls.ExprFieldTest = ExprFieldTest

		test_module = imp.new_module('GUIEditor_DataModel_Tests')
		sys.modules[test_module.__name__] = test_module
		cls.mod = test_module

		def moveClassToModule(mod, x):
			setattr(mod, x.__name__, x)
			x.__module__ = mod.__name__

		moveClassToModule(test_module, TypedFieldTest)
		moveClassToModule(test_module, EnumFieldTest)
		moveClassToModule(test_module, ListFieldTest)
		moveClassToModule(test_module, ChildFieldTest)
		moveClassToModule(test_module, EvalFieldTest)
		moveClassToModule(test_module, EnumEvalFieldTest)
		moveClassToModule(test_module, ExprFieldTest)


	@classmethod
	def tearDownClass(cls):
		cls.TypedFieldTest = None
		cls.EnumFieldTest = None
		cls.ListFieldTest = None
		cls.ChildFieldTest = None
		cls.EvalFieldTest = None
		cls.EnumEvalFieldTest = None
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



	def test_TypedField_constructor(self):
		a1 = self.TypedFieldTest()

		self.assertEqual(0, a1.x.value)
		self.assertEqual(1, a1.y.value)
		self.assertEqual(None, a1.z.value)

		a2 = self.TypedFieldTest(x=10, y=20)

		self.assertEqual(10, a2.x.value)
		self.assertEqual(20, a2.y.value)

		self.assertRaises(TypeError, lambda: self.TypedFieldTest(a=1, b=2))



	def test_TypedField_change_listener(self):
		a = self.TypedFieldTest()

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



	def test_TypedField_changeHistory(self):
		a = self.TypedFieldTest()

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



	def test_TypedField_serialisation(self):
		a = self.TypedFieldTest(x=10, y=20)

		a_io = pickle.loads(pickle.dumps(a))

		self.assertIsNot(a, a_io)

		self.assertEqual(10, a_io.x.value)
		self.assertEqual(20, a_io.y.value)



	def test_TypedField_editor(self):
		a = self.TypedFieldTest(x=10, y=20)

		codeGen = Python2CodeGenerator('test')

		self.assertEqual(10, a.x.getValueForEditor())
		self.assertEqual(20, a.y.getValueForEditor())

		self.assertEqual(Py.IntLiteral(format='decimal', numType='int', value='10'), a.x.__py_evalmodel__(codeGen))
		self.assertEqual(Py.IntLiteral(format='decimal', numType='int', value='20'), a.y.__py_evalmodel__(codeGen))

		self.assertEqual(Py.Call(target=Py.Load(name='A'), args=[
			Py.IntLiteral(format='decimal', numType='int', value='10'),
			Py.IntLiteral(format='decimal', numType='int', value='20')
		]), a.__py_evalmodel__(codeGen))




	def test_EnumField_constructor(self):
		from BritefuryJ.LSpace.Layout import HAlignment

		a1 = self.EnumFieldTest()
		a2 = self.EnumFieldTest(x=HAlignment.RIGHT)

		self.assertEqual(HAlignment.PACK, a1.x.value)
		self.assertEqual(HAlignment.RIGHT, a2.x.value)

		self.assertEqual({'x': 'PACK'}, a1.__getstate__())
		self.assertEqual({'x': 'RIGHT'}, a2.__getstate__())





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
		a1 = self.TypedFieldTest()
		a2 = self.TypedFieldTest()

		self.assertIs(None, b1.p.node)
		self.assertEqual([], b1.q.nodes)

		b2 = self.ChildFieldTest(p=a1, q=[a2])

		self.assertIs(a1, b2.p.node)
		self.assertEqual([a2], b2.q.nodes)

		self.assertRaises(NodeAlreadyHasParentError, lambda: self.ChildFieldTest(p=a1, q=[a2]))



	def test_ChildField_parentage(self):
		a1 = self.TypedFieldTest(x=10, y=20)
		a2 = self.TypedFieldTest(x=3, y=4)
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
		a1 = self.TypedFieldTest()
		a2 = self.TypedFieldTest()
		a3 = self.TypedFieldTest()

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
		a1 = self.TypedFieldTest(x=10, y=20)
		a2 = self.TypedFieldTest(x=3, y=4)
		b = self.ChildFieldTest(p=a1, q=[a2])

		b_io = pickle.loads(pickle.dumps(b))

		self.assertIsNot(b, b_io)

		self.assertIsInstance(b_io.p.node, self.TypedFieldTest)
		self.assertIsInstance(b_io.q.nodes[0], self.TypedFieldTest)

		self.assertEqual(10, b_io.p.node.x.value)
		self.assertEqual(3, b_io.q.nodes[0].x.value)



	def test_ChildField_editor(self):
		a1 = self.TypedFieldTest(x=10, y=20)
		a2 = self.TypedFieldTest(x=3, y=4)
		b = self.ChildFieldTest(p=a1, q=[a2])

		codeGen = Python2CodeGenerator('test')

		self.assertEqual(a1, b.p.getValueForEditor())
		self.assertEqual([a2], b.q.getValueForEditor())

		self.assertEqual(Py.Call(target=Py.Load(name='B'), args=[
			Py.Call(target=Py.Load(name='A'), args=[Py.IntLiteral(format='decimal', numType='int', value='10'), Py.IntLiteral(format='decimal', numType='int', value='20')]),
			Py.ListLiteral(values=[Py.Call(target=Py.Load(name='A'), args=[Py.IntLiteral(format='decimal', numType='int', value='3'), Py.IntLiteral(format='decimal', numType='int', value='4')])])
		]),  b.__py_evalmodel__(codeGen))





	def test_EvalField_constructor(self):
		c1 = self.EvalFieldTest()

		self.assertEqual(0.0, c1.x.constantValue)
		self.assertEqual(1.0, c1.y.constantValue)

		c2 = self.EvalFieldTest(x=10.0, y=20.0)

		self.assertEqual(10.0, c2.x.constantValue)
		self.assertEqual(20.0, c2.y.constantValue)



	def test_EvalField_changeHistory(self):
		c = self.EvalFieldTest()

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
		c = self.EvalFieldTest()

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
		c = self.EvalFieldTest()

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




	def test_EnumEvalField_constructor(self):
		from BritefuryJ.LSpace.Layout import HAlignment

		c1 = self.EnumEvalFieldTest()
		c2 = self.EnumEvalFieldTest(x=HAlignment.RIGHT)

		self.assertEqual(HAlignment.PACK, c1.x.constantValue)
		self.assertEqual(HAlignment.RIGHT, c2.x.constantValue)

		self.assertEqual({'x': _EvalFieldState('PACK', None)}, c1.__getstate__())
		self.assertEqual({'x': _EvalFieldState('RIGHT', None)}, c2.__getstate__())



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



