##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.util.regex import Pattern

from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from BritefuryJ.Controls import Button, EditableLabel
from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Row
from BritefuryJ.StyleSheet import StyleSheet

from Britefury.Util.Abstract import abstractmethod

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor


_notSetStyle = StyleSheet.style( Primitive.fontItalic( True ) )


_filenamePattern = Pattern.compile( '.+' )


class _PathEditorTextEntry (Editor):
	def _newModel(self, value):
		if not isinstance( value, str )  and  not isinstance( value, unicode) :
			value = None
		return Model( value )


	@abstractmethod
	def _initFileChooser(self, fileChooser):
		pass


	def buildEditorPres(self, fragment, inheritedState):
		def _onOpen(button, event):
			component = button.getElement().getRootElement().getComponent()
			fileChooser = JFileChooser()
			self._initFileChooser( fileChooser )
			response = fileChooser.showDialog( component, 'Open' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = fileChooser.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						self._model.liveValue.setLiteralValue( filename )
		openButton = Button.buttonWithLabel( '...', _onOpen )
		return Row( [ openButton, Spacer( 3.0, 0.0 ), EditableLabel( self._model.liveValue, _notSetStyle( Label( '<none>' ) ) ).regexValidated( _filenamePattern, 'Please enter a valid filename' ) ] )



class FilePathEditorTextEntry (_PathEditorTextEntry):
	def __init__(self, model=None, value=None):
		super( FilePathEditorTextEntry, self ).__init__( model, value )
		self._description = None
		self._extensions = None


	def _initFileChooser(self, fileChooser):
		if self._description is not None  and  self._extensions is not None:
			fileChooser.setFileFilter( FileNameExtensionFilter( self._description, self._extensions ) )



class DirPathEditorTextEntry (_PathEditorTextEntry):
	def _initFileChooser(self, fileChooser):
		fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY )
