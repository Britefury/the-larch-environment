##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Live import TrackedLiveValue

from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Row, Column
from BritefuryJ.Pres.UI import Form

from BritefuryJ.Controls import Button, TextEntry

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr
from LarchTools.PythonTools import GUIEditor



exprBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.0, 0.25, 0.75 ), None )




def unaryBranchChildEditUIFormSections(branch):
	child = branch.child
	if child is not None:
		if isinstance(child, GUIEditor.PrimitiveComponents.GUILabel):
			textField = child.text
			if textField.isConstant():
				textLive = textField.constantValueLive
				return [Form.SmallSection('Label text', None, TextEntry.textEntryCommitOnChange(textLive))]
	return []