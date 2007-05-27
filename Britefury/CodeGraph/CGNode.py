##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *


class CGNode (SheetGraphNode):
	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		return None


	def destroy(self):
		self.destroyChildren()
		self._graph.nodes.remove( self )


	def destroyChildren(self):
		pass


