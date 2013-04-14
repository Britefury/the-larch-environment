##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from copy import copy

from java.awt import Color

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Live import TrackedLiveValue

from BritefuryJ.Editor.List import EditableListController

from LarchTools.PythonTools.GUIEditor.Component import GUIComponent
from LarchTools.PythonTools.GUIEditor.ComponentPalette import PaletteComponentDrag



#
#Sequential components:
_emptyStyle = StyleSheet.style(Primitive.fontItalic(True), Primitive.fontSize(10), Primitive.foreground(Color(0.4, 0.4, 0.4)))
emptyLabel = _emptyStyle(Label('<empty>'))



class GUIBranchComponent (GUIComponent):
	def removeChild(self, child):
		raise NotImplementedError, 'abstract'

	def getNextSiblingOf(self, child):
		raise NotImplementedError, 'abstract'



	def _registerChild(self, childComponent):
		childComponent._parent = self

	def _unregisterChild(self, childComponent):
		if childComponent._parent is not self:
			raise RuntimeError, 'Cannot unregister component that is not a child of this'
		childComponent._parent = None




class GUIUnaryBranchComponent (GUIBranchComponent):
	def __init__(self, child=None):
		super(GUIBranchComponent, self).__init__()
		self.__child = TrackedLiveValue(child)
		if child is not None:
			self._registerChild(child)


	def removeChild(self, child):
		assert child is self.child
		self.child = None


	def getNextSiblingOf(self, child):
		return None


	def _presentChild(self):
		def _onDropFromPalette(element, targetPos, data, action):
			assert isinstance(data, PaletteComponentDrag)
			self.child = data.getItem()
			return True

		child = self.child
		if child is None:
			p = self._emptyPres
			return p.withDropDest(PaletteComponentDrag, _onDropFromPalette)
		else:
			return Pres.coerce(self.child)



	@property
	def child(self):
		return self.__child.getValue()

	@child.setter
	def child(self, c):
		prevChild = self.__child.getStaticValue()
		if prevChild is not None:
			self._unregisterChild(prevChild)

		self.__child.setLiteralValue(c)

		if c is not None:
			self._registerChild(c)


	_emptyPres = emptyLabel


	def _presentBranchContents(self, fragment, inheritedState):
		raise NotImplementedError, 'abstract'


	def _presentContents(self, fragment, inheritedState):
		p = self._presentBranchContents(fragment, inheritedState)
		p = SequentialGUIController.instance.item(self, p)
		return p




class SequentialGUIController (EditableListController):
	def canMove(self, item, srcList, index, destList):
		if isinstance(item, GUIComponent):
			if item._lookFor(destList):
				return False
		return True


	def reorder(self, item, destList, index):
		srcIndex = destList.index(item)
		if srcIndex < index:
			index -= 1
		del destList[srcIndex]
		destList.insert(index, item)
		return True

	def move(self, item, srcList, index, destList):
		if srcList is not None:
			srcList.remove(item)
		destList.insert(index, item)
		return True

	def copy(self, item, srcList, index, destList):
		destList.insert(index, copy.deepcopy(item))
		return True



	def canMoveToEnd(self, item, srcList, destList):
		if isinstance(item, GUIComponent):
			if item.lookFor(destList):
				return False
		return True


	def reorderToEnd(self, item, destList):
		destList.remove(item)
		destList.append(item)
		return True

	def moveToEnd(self, item, srcList, destList):
		if srcList is not None:
			srcList.remove(item)
		destList.append(item)
		return True

	def copyToEnd(self, item, srcList, destList):
		destList.append(copy.deepcopy(item))
		return True


SequentialGUIController.instance = SequentialGUIController()







class GUISequenceComponent (GUIBranchComponent):
	componentName = 'Sequence'

	def __init__(self, xs=None):
		super(GUISequenceComponent, self).__init__()
		self._children = LiveList(xs)
		for child in self._children:
			self._registerChild(child)


	def getNextSiblingOf(self, child):
		try:
			index = self._children.index(child) + 1
		except ValueError:
			return None
		if index < len(self._children):
			return self._children[index]
		else:
			return None



	def __iter__(self):
		return iter(self._children)

	def __len__(self):
		return len(self._children)

	def index(self, x):
		return self._children.index(x)


	def __getitem__(self, index):
		return self._children[index]

	def __setitem__(self, index, x):
		self._children[index] = x

	def __delitem__(self, index):
		del self._children[index]


	def append(self, x):
		self._children.append(x)
		self._registerChild(x)

	def insert(self, index, x):
		self._children.insert(index, x)
		self._registerChild(x)

	def remove(self, x):
		self._children.remove(x)
		self._unregisterChild(x)



	def removeChild(self, child):
		self.remove(child)


	def _lookFor(self, x):
		for item in self._children:
			if item.lookFor(x):
				return True
		return False

	def _presentContents(self, fragment, inheritedState):
		contents = [emptyLabel]   if len(self._children) == 0   else self._children[:]
		p = self._presentSequenceContents(contents, fragment, inheritedState)
		p = SequentialGUIController.instance.editableList(self, p)
		return p


	def _py_evalmodel_forChildren(self, codeGen):
		return Py.ListLiteral( values=[ x.__py_evalmodel__( codeGen )   for x in self._children ] )
