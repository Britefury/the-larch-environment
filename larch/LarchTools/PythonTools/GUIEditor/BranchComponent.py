##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank

from BritefuryJ.StyleSheet import StyleSheet

from LarchTools.PythonTools.GUIEditor.Component import GUIComponent



#
#Sequential components:
_emptyStyle = StyleSheet.style(Primitive.fontItalic(True), Primitive.fontSize(10), Primitive.foreground(Color(0.4, 0.4, 0.4)))
emptyLabel = _emptyStyle(Label('<empty>'))



class GUIBranchComponent (GUIComponent):
	pass

