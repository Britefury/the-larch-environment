##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
import copy

from BritefuryJ.Editor.List import EditableListController

from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.Component import GUIComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import emptyLabel, GUIBranchComponent



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
		self._children = LiveList(xs)


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

	def insert(self, index, x):
		self._children.insert(index, x)

	def remove(self, x):
		self._children.remove(x)



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
