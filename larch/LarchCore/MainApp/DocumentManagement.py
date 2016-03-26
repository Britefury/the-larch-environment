##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import os

from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from BritefuryJ.Controls import MenuItem, VPopupMenu

from BritefuryJ.LSpace import Anchor


from Britefury.Kernel.Document import Document



# handleNewDocumentFn(document)
def promptNewDocument(world, element, handleNewDocumentFn):
	def _make_newDocument(documentFactory):
		def newDoc(menuItem):
			doc = documentFactory.makeDocument( world )
			handleNewDocumentFn( doc, documentFactory.firstPageSubject )
		return newDoc
	items = []
	for newDocumentFactory in world.newDocumentFactories:
		items.append( MenuItem.menuItemWithLabel( newDocumentFactory.menuLabelText, _make_newDocument( newDocumentFactory ) ) )
	newDocumentMenu = VPopupMenu( items )
	
	newDocumentMenu.popupMenu( element, Anchor.TOP_RIGHT, Anchor.TOP_LEFT )

	
	
# handleOpenedDocumentFn(fullPath, document)
def promptOpenDocument(world, component, handleOpenedDocumentFn):
	openDialog = JFileChooser()
	openDialog.setFileFilter( FileNameExtensionFilter( 'Larch project (*.larch)', [ 'larch' ] ) )
	response = openDialog.showDialog( component, 'Open' )
	if response == JFileChooser.APPROVE_OPTION:
		sf = openDialog.getSelectedFile()
		if sf is not None:
			filename = sf.getPath()
			if filename is not None:
				document = Document.readFile( world, filename )
				if document is not None:
					handleOpenedDocumentFn( filename, document )

				
# handleSaveDocumentAsFn(filename)
def promptSaveDocumentAs(world, component, handleSaveDocumentAsFn, existingFilename=None):
	filename = None
	bFinished = False
	while not bFinished:
		saveDialog = JFileChooser()
		saveDialog.setFileFilter( FileNameExtensionFilter( 'Larch project (*.larch)', [ 'larch' ] ) )
		response = saveDialog.showSaveDialog( component )
		if response == JFileChooser.APPROVE_OPTION:
			sf = saveDialog.getSelectedFile()
			if sf is not None:
				if sf.exists():
					response = JOptionPane.showOptionDialog( component, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
					if response == JFileChooser.APPROVE_OPTION:
						filename = sf.getPath()
						bFinished = True
					else:
						bFinished = False
				else:
					filename = sf.getPath()
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
