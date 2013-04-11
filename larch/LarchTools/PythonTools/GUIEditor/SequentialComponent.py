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

from LarchTools.PythonTools.GUIEditor.Component import GUIComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import emptyLabel



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





class GUISequenceComponent (GUIComponent):
	componentName = 'Sequence'

	def __init__(self, xs=None):
		self.xs = LiveList(xs)


	def __iter__(self):
		return iter(self.xs)

	def __len__(self):
		return len(self.xs)

	def index(self, x):
		return self.xs.index(x)


	def __getitem__(self, index):
		return self.xs[index]

	def __setitem__(self, index, x):
		self.xs[index] = x

	def __delitem__(self, index):
		del self.xs[index]


	def append(self, x):
		self.xs.append(x)

	def insert(self, index, x):
		self.xs.insert(index, x)

	def remove(self, x):
		self.xs.remove(x)



	def _lookFor(self, x):
		for item in self.xs:
			if item.lookFor(x):
				return True
		return False

	def _presentContents(self, fragment, inheritedState):
		contents = [emptyLabel]   if len(self.xs) == 0   else self.xs[:]
		p = self._presentSequenceContents(contents, fragment, inheritedState)
		p = SequentialGUIController.instance.editableList(self, p)
		return p

