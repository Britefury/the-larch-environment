##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import ClassSignal

from Britefury.CodeViewTree.CodeViewTree import CodeViewTree

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNode import CGNode




class CVTNodeClass (SheetClass):
	def __init__(cls, clsName, clsBases, clsDict):
		super( CVTNodeClass, cls ).__init__( clsName, clsBases, clsDict )

		try:
			graphNodeClass = clsDict['graphNodeClass']
		except KeyError:
			pass
		else:
			CodeViewTree._nodeClassTable[graphNodeClass] = cls




class CVTNode (Sheet):
	__metaclass__ = CVTNodeClass

	graphNode = SheetRefField( CGNode )


	def __init__(self, graphNode, tree):
		super( CVTNode, self ).__init__()

		self.graphNode = graphNode
		self._tree = tree




	def __eq__(self, node):
		return self.graphNode  is  node.graphNode


