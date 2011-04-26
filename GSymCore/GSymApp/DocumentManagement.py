##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from BritefuryJ.Controls import *


from Britefury.gSym.gSymDocument import GSymDocument



# handleNewDocumentFn(document)
def promptNewDocument(world, element, handleNewDocumentFn):
	def _make_newDocument(newDocFn):
		def newDoc(menuItem):
			doc = newDocFn( world )
			handleNewDocumentFn( doc )
		return newDoc
	items = []
	for newDocumentFactory in world.newDocumentFactories:
		items.append( MenuItem.menuItemWithLabel( newDocumentFactory.menuLabelText, _make_newDocument( newDocumentFactory.newDocumentFn ) ) )
	newDocumentMenu = VPopupMenu( items )
	
	newDocumentMenu.popupToRightOf( element )

	
	
# handleOpenedDocumentFn(fullPath, document)
def promptOpenDocument(world, component, handleOpenedDocumentFn):
	openDialog = JFileChooser()
	openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
	response = openDialog.showDialog( component, 'Open' )
	if response == JFileChooser.APPROVE_OPTION:
		sf = openDialog.getSelectedFile()
		if sf is not None:
			filename = sf.getPath()
			if filename is not None:
				document = GSymDocument.readFile( world, filename )
				if document is not None:
					handleOpenedDocumentFn( filename, document )

				
# handleSaveDocumentAsFn(filename)
def promptSaveDocumentAs(world, component, handleSaveDocumentAsFn):
	filename = None
	bFinished = False
	while not bFinished:
		openDialog = JFileChooser()
		openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
		response = openDialog.showSaveDialog( component )
		if response == JFileChooser.APPROVE_OPTION:
			sf = openDialog.getSelectedFile()
			if sf is not None:
				filenameFromDialog = sf.getPath()
				if filenameFromDialog is not None:
					if os.path.exists( filenameFromDialog ):
						response = JOptionPane.showOptionDialog( component, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
						if response == JFileChooser.APPROVE_OPTION:
							filename = filenameFromDialog
							bFinished = True
						else:
							bFinished = False
					else:
						filename = filenameFromDialog
						bFinished = True
				else:
					bFinished = True
			else:
				bFinished = True
		else:
			bFinished = True

	if filename is not None:
		handleSaveDocumentAsFn( filename )
		return True
	else:
		return False
