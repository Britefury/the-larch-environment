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

from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import ChildField, ChildListField
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




class GUIUnaryBranchComponent (GUIBranchComponent):
	child = ChildField()


	def removeChild(self, child):
		assert child is self.child.node
		self.child.node = None


	def getNextSiblingOf(self, child):
		return None


	def _presentChild(self):
		def _onDropFromPalette(element, targetPos, data, action):
			assert isinstance(data, PaletteComponentDrag)
			self.child.node = data.getItem()
			return True

		child = self.child.node
		if child is None:
			p = self._emptyPres
			return p.withDropDest(PaletteComponentDrag, _onDropFromPalette)
		else:
			return Pres.coerce(child)



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


	children = ChildListField()


	def getNextSiblingOf(self, child):
		nodes = self.children.nodes
		try:
			index = nodes.index(child) + 1
		except ValueError:
			return None
		if index < len(nodes):
			return nodes[index]
		else:
			return None



	def __iter__(self):
		return iter(self.children.nodes)

	def __len__(self):
		return len(self.children.nodes)

	def index(self, x):
		return self.children.nodes.index(x)


	def __getitem__(self, index):
		return self.children.nodes[index]

	def __setitem__(self, index, x):
		self.children.nodes[index] = x

	def __delitem__(self, index):
		del self.children.nodes[index]


	def append(self, x):
		self.children.nodes.append(x)

	def insert(self, index, x):
		self.children.nodes.insert(index, x)

	def remove(self, x):
		self.children.nodes.remove(x)



	def removeChild(self, child):
		self.remove(child)


	def _lookFor(self, x):
		for item in self.children.nodes:
			if item.lookFor(x):
				return True
		return False

	def _presentContents(self, fragment, inheritedState):
		children = self.children.nodes
		contents = [emptyLabel]   if len(children) == 0   else children[:]
		p = self._presentSequenceContents(contents, fragment, inheritedState)
		p = SequentialGUIController.instance.editableList(self, p)
		return p


	def _py_evalmodel_forChildren(self, codeGen):
		return Py.ListLiteral( values=[ x.__py_evalmodel__( codeGen )   for x in self.children.nodes ] )
