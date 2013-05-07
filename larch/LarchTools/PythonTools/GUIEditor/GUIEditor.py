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

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Spacer

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedExpressionAtCaretAction

from LarchTools.PythonTools.GUIEditor.Properties import GUIEdProp, GUICProp
from LarchTools.PythonTools.GUIEditor.ContextMenu import guiEditorContextMenu, componentContextMenu
from LarchTools.PythonTools.GUIEditor.Target import GUITargetInteractor, GUIScrollInteractor
from LarchTools.PythonTools.GUIEditor.ComponentPalette import PaletteComponentDrag
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIUnaryBranchComponent




_guiEditorBorder = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.6, 0.5, 0.7 ), None )



class GUIEditorRootComponent (GUIUnaryBranchComponent):
	isRootGUIEditorComponent = True


	def __init__(self, contents=None):
		super(GUIEditorRootComponent, self).__init__(child=contents)
		self._guiEditor = None


	@property
	def guiEditor(self):
		return self._guiEditor


	def __component_py_evalmodel__(self, codeGen):
		child = self.child
		if child is not None:
			return child.__py_evalmodel__(codeGen)
		else:
			blank = codeGen.embeddedValue( Blank )
			return Py.Call( target=blank, args=[] )



	def __present__(self, fragment, inheritedState):
		p = self._presentChild()
		p = p.withContextMenuInteractor(componentContextMenu)
		p = p.withProperty(GUICProp.instance, self)
		return p


	_emptyPres = Spacer(15.0, 15.0)




class GUIEditor (object):
	def __init__(self, contents=None):
		self.__change_history__ = None
		self.__root = GUIEditorRootComponent(contents)
		self.__root._guiEditor = self


	def __getstate__(self):
		return {'root': self.__root}

	def __setstate__(self, state):
		self.__change_history__ = None
		self.__root = state.get('root')  or  state.get('_GUIEditor__root')
		self.__root._guiEditor = self


	def __get_trackable_contents__(self):
		return [self.__root]


	def __clipboard_copy__(self, memo):
		instance = GUIEditor.__new__(GUIEditor)
		instance.__change_history__ = None
		instance.__root = memo.copy(self.__root)
		instance.__root._guiEditor = self
		return instance


	def __py_evalmodel__(self, codeGen):
		return self.__root.__py_evalmodel__(codeGen)

	__embed_hide_frame__ = True
	
	def __present__(self, fragment, inheritedState):
		p = Pres.coerce(self.__root)
		p = _guiEditorBorder.surround(p)
		p = p.withContextMenuInteractor(guiEditorContextMenu)
		p = p.withProperty(GUIEdProp.instance, self)
		return p




@EmbeddedExpressionAtCaretAction
def _newGUIEditorAtCaret(caret):
	return GUIEditor(None)



_vreCommand = Command( '&G&U&I Editor', _newGUIEditorAtCaret )

pythonCommandSet( 'LarchTools.PythonTools.GUIEditor', [ _vreCommand ] )
