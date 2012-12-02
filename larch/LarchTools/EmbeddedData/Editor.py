##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2012.
##-*************************
from Britefury.Util.Abstract import abstractmethod

from BritefuryJ.LSpace import Anchor
from BritefuryJ.LSpace.Input import Modifier
from BritefuryJ.LSpace.Interactor import ContextMenuElementInteractor

from BritefuryJ.Pres.UI import Section, SectionHeading2


class Editor (object):
	class _EditorInteractor (ContextMenuElementInteractor):
		def __init__(self, editor):
			self._editor = editor

		def contextMenu(self, element, popupMenu):
			return self._editor._showEditorPopup( element, popupMenu )

		def buttonRelease(self, element, event):
			pass


	def __init__(self, model=None, value=None):
		if model is None:
			model = self._newModel( value )
		self._model = model



	def __getstate__(self):
		return { '_model' : self._model }


	def __setstate__(self, state):
		self._model = state.get( '_model', None )


	def _newModel(self, value):
		raise NotImplementedError


	def __py_eval__(self, globals, locals, codeGen):
		return self._model.value


	def _showEditorPopup(self, element, popupMenu):
		settingsPres = self._createSettingsPres()
		newEditorPres = self._createNewEditorPres()
		if settingsPres is not None  or  newEditorPres is not None:
			if settingsPres is not None:
				popupMenu.add( Section( SectionHeading2( 'Settings' ), settingsPres ) )
			if newEditorPres is not None:
				popupMenu.add( Section( SectionHeading2( 'New editor' ), newEditorPres ) )
			return True
		else:
			return False


	def _createSettingsPres(self):
		return None

	def _createNewEditorPres(self):
		return None


	@abstractmethod
	def buildEditorPres(self, fragment, inheritedState):
		pass


	def __present__(self, fragment, inheritedState):
		e = self.buildEditorPres( fragment, inheritedState )
		return e.withContextMenuInteractor( self._EditorInteractor( self ) )


	model = property( lambda self: self._model )