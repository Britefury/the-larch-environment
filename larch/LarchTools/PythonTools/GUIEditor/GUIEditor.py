##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from BritefuryJ.Pres import Pres

from LarchTools.PythonTools.GUIEditor.Properties import GUIEdProp





class GUIEditor (object):
	def __init__(self, root):
		self._root = root
	
	def __present__(self, fragment, inheritedState):
		
		p = Pres.coerce(self._root)
		p = p.withProperty(GUIEdProp.instance, self)
		return p
