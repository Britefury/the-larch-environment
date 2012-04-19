##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from java.util.regex import Pattern

from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from BritefuryJ.Controls import Button, EditableLabel
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.StyleSheet import StyleSheet

from LarchTools.EmbeddedData.Model import Model
from LarchTools.EmbeddedData.Editor import Editor


_notSetStyle = StyleSheet.style( Primitive.fontItalic( True ) )


_filenamePattern = Pattern.compile( '.+' )


class FilePathEditorTextEntry (Editor):
	def __init__(self, model=None, value=None):
		super( FilePathEditorTextEntry, self ).__init__( model, value )
		self._description = None
		self._extensions = None


	def _newModel(self, value):
		if not isinstance( value, str )  and  not isinstance( value, unicode) :
			value = None
		return Model( value )


	def buildEditorPres(self, fragment, inheritedState):
		def _onOpen(button, event):
			component = button.getElement().getRootElement().getComponent()
			openDialog = JFileChooser()
			if self._description is not None  and  self._extensions is not None:
				openDialog.setFileFilter( FileNameExtensionFilter( self._description, self._extensions ) )
			response = openDialog.showDialog( component, 'Open' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						self._model.liveValue.setLiteralValue( filename )
		openButton = Button.buttonWithLabel( '...', _onOpen )
		return Row( [ openButton, Spacer( 3.0, 0.0 ), EditableLabel.regexValidated( self._model.liveValue, _notSetStyle( Label( '<none>' ) ), _filenamePattern, 'Please enter a valid filename' ) ] )
