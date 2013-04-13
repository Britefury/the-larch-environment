##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Command import Command

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Spacer

from BritefuryJ.Live import LiveValue

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedExpressionAtCaretAction

from LarchTools.PythonTools.GUIEditor.Properties import GUIEdProp
from LarchTools.PythonTools.GUIEditor.ContextMenu import guiEditorContextMenu
from LarchTools.PythonTools.GUIEditor.ComponentPalette import PaletteComponentDrag




_guiEditorBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.6, 0.5, 0.7 ), None )



class GUIEditor (object):
	def __init__(self, root):
		self._root = LiveValue(root)


	def __py_evalmodel__(self, codeGen):
		root = self._root.getValue()
		if root is not None:
			return root.__py_evalmodel__(codeGen)
		else:
			blank = codeGen.embeddedValue( Blank )
			return Py.Call( target=blank, args=[] )

	__embed_hide_frame__ = True
	
	def __present__(self, fragment, inheritedState):
		def _onDropFromPalette(element, targetPos, data, action):
			assert isinstance(data, PaletteComponentDrag)
			self._root.setLiteralValue(data.getItem())


		root = self._root.getValue()
		if root is not None:
			p = Pres.coerce(root)
			p = _guiEditorBorder.surround( p )
		else:
			p = Spacer(15.0, 15.0)
			p = _guiEditorBorder.surround( p )
			p = p.withDropDest(PaletteComponentDrag, _onDropFromPalette)
		p = p.withContextMenuInteractor(guiEditorContextMenu)
		p = p.withProperty(GUIEdProp.instance, self)
		return p




@EmbeddedExpressionAtCaretAction
def _newGUIEditorAtCaret(caret):
	return GUIEditor(None)



_vreCommand = Command( '&G&U&I Editor', _newGUIEditorAtCaret )

pythonCommandSet( 'LarchTools.PythonTools.GUIEditor', [ _vreCommand ] )
