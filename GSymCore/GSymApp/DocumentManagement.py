##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter


from Britefury.gSym.gSymDocument import GSymDocument



def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )


# handleNewDocumentFn(unit)
def promptNewDocument(world, component, handleNewDocumentFn):
	def _make_newDocument(newUnitFn):
		def newDoc():
			unit = newUnitFn()
			handleNewDocumentFn( unit )
		return newDoc
	newDocumentMenu = JPopupMenu( 'New document' )
	for newUnitFactory in world.newUnitFactories:
		newDocumentMenu.add( _action( newUnitFactory.menuLabelText, _make_newDocument( newUnitFactory.newDocumentFn ) ) )
	
	pos = component.getMousePosition( True )
	newDocumentMenu.show( component, pos.x, pos.y )

	
	
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
						response = JOptionPane.showOptionDialog( frame, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
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
