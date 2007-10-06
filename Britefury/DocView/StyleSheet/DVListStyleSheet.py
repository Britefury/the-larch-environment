##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocView.StyleSheet.DVBorderStyleSheet import DVBorderStyleSheet




class DVListStyleSheet (DVBorderStyleSheet):
	@abstractmethod
	def elementsContainer(self):
		pass

	@abstractmethod
	def overallContainer(self, elementsContainer):
		pass


	def deleteChildByIndex(self, docNode, index):
		select = docNode
		if len( docNode ) > 1  and  index < ( len( docNode ) - 1 ):
			select = docNode[index+1]
		del docNode[index]
		return select
