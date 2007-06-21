##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os
import sys

import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Event.QueuedEvent import queueEvent

from Britefury.UI.ConfirmDialog import *
from Britefury.UI.ConfirmOverwriteFileDialog import confirmOverwriteFileDialog

from Britefury.FileIO.IOXml import *

from Britefury.CommandHistory.CommandHistory import CommandHistory

from Britefury.SheetGraph.SheetGraph import *

from Britefury.VirtualMachine.VMMachine import *

from Britefury.LowLevelCodeTree.LowLevelCodeTree import LowLevelCodeTree

from Britefury.DocView.Toolkit.DTDocument import DTDocument

from Britefury.CodeGraph.CGModule import CGModule

from Britefury.CodeViewTree.CodeViewTree import CodeViewTree

from Britefury.CodeView.CodeView import CodeView

from Britefury.GraphView.SheetGraphView import SheetGraphView
from Britefury.GraphView.SheetGraphViewDisplayTable import SheetGraphViewDisplayTable
from Britefury.GraphView.SheetGraphViewLayout import SheetGraphViewLayout




class MainApp (object):
	def __init__(self, graph, rootNode):
		self._graph = None
		self._graphRoot = None
		self._tree = None
		self._view = None
		self._viewRoot = None
		self._commandHistory = None
		self._bUnsavedData = False

		self._graphView = None
		self._graphViewDisplayTable = None
		self._graphViewLayout = None
		self._graphViewWindow = None


		self._doc = DTDocument()
		self._doc.show()


		self.setGraph( graph, rootNode )


		oneToOneButton = gtk.Button( '1:1' )
		oneToOneButton.show()
		oneToOneButton.connect( 'clicked', self._p_onOneToOne )


		executeButton = gtk.Button( 'Execute' )
		executeButton.show()
		executeButton.connect( 'clicked', self._p_onExecuteCode )


		executeDebugButton = gtk.Button( 'Execute with debug' )
		executeDebugButton.show()
		executeDebugButton.connect( 'clicked', self._p_onExecuteCodeWithDebug )


		buttonBox = gtk.HBox( spacing=20 )
		buttonBox.pack_end( executeDebugButton, False, False, 0 )
		buttonBox.pack_end( executeButton, False, False, 0 )
		buttonBox.pack_end( oneToOneButton, False, False, 0 )
		buttonBox.show()



		newItem = gtk.MenuItem( 'New' )
		newItem.connect( 'activate', self._p_onNew )

		openItem = gtk.MenuItem( 'Open' )
		openItem.connect( 'activate', self._p_onOpen )

		saveItem = gtk.MenuItem( 'Save' )
		saveItem.connect( 'activate', self._p_onSave )

		fileMenu = gtk.Menu()
		fileMenu.append( newItem )
		fileMenu.append( openItem )
		fileMenu.append( saveItem )


		undoItem = gtk.MenuItem( 'Undo' )
		undoItem.connect( 'activate', self._p_onUndo )

		redoItem = gtk.MenuItem( 'Redo' )
		redoItem.connect( 'activate', self._p_onRedo )

		editMenu = gtk.Menu()
		editMenu.append( undoItem )
		editMenu.append( redoItem )


		graphViewItem = gtk.MenuItem( 'Show graph view' )
		graphViewItem.connect( 'activate', self._p_onShowGraphView )

		debugMenu = gtk.Menu()
		debugMenu.append( graphViewItem )


		fileMenuItem = gtk.MenuItem( 'File' )
		fileMenuItem.set_submenu( fileMenu )

		editMenuItem = gtk.MenuItem( 'Edit' )
		editMenuItem.set_submenu( editMenu )

		debugMenuItem = gtk.MenuItem( 'Debug' )
		debugMenuItem.set_submenu( debugMenu )


		menuBar = gtk.MenuBar()
		menuBar.append( fileMenuItem )
		menuBar.append( editMenuItem )
		menuBar.append( debugMenuItem )
		menuBar.show_all()




		box = gtk.VBox()
		box.pack_start( menuBar, False, False )
		box.pack_start( self._doc )
		box.pack_start( gtk.HSeparator(), False, False, 10 )
		box.pack_start( buttonBox, False, False, 10 )
		box.show_all()


		self._window = gtk.Window( gtk.WINDOW_TOPLEVEL );
		self._window.connect( 'delete-event', self._p_onDeleteEvent )
		self._window.connect( 'destroy', self._p_onDestroy )
		self._window.set_border_width( 10 )
		self._window.set_size_request( 640, 480 )
		self._window.add( box )
		self._window.show()










	def setGraph(self, graph, rootNode):
		self._graph = graph
		self._graphRoot = rootNode

		self._commandHistory = CommandHistory()
		self._commandHistory.track( self._graph )
		self._commandHistory.changedSignal.connect( self._p_onCommandHistoryChanged )
		self._bUnsavedData = False

		self._tree = CodeViewTree( self._graph, rootNode )
		treeRoot = self._tree.getRootNode()
		self._view = CodeView( self._tree, self._commandHistory )
		self._viewRoot = self._view.buildView( treeRoot, None )

		self._view.refreshCell.changedSignal.connect( self._p_queueRefresh )

		self._view.refresh()

		self._doc.child = self._viewRoot.widget
		self._view.setDocument( self._doc )




	def _p_refreshCodeView(self):
		self._view.refresh()


	def _p_queueRefresh(self):
		queueEvent( self._p_refreshCodeView )



	def _p_onExecuteCode(self, widget):
		pyCodeBlock = self._graphRoot.generatePyCodeBlock()
		text = pyCodeBlock.asText()
		exec( text )



	def _p_onExecuteCodeWithDebug(self, widget):
		pyCodeBlock = self._graphRoot.generatePyCodeBlock()
		text = pyCodeBlock.asText()
		print text
		exec( text )



	def _p_onOneToOne(self, widget):
		self._doc.oneToOne()


	def _p_onShowGraphView(self, widget):
		if self._graphView is None:
			self._graphViewDisplayTable = SheetGraphViewDisplayTable()

			self._graphView = SheetGraphView( self._p_graphViewCreateLink, self._p_graphViewEraseLink )
			self._graphView.attachGraph( self._graph, self._graphViewDisplayTable )
			self._graphView.show()

			self._graphViewLayout = SheetGraphViewLayout()
			self._graphViewLayout.attachGraph( self._graph, self._graphView, self._graphViewDisplayTable )

			self._graphViewWindow = gtk.Window( gtk.WINDOW_TOPLEVEL );
			self._graphViewWindow.connect( 'delete-event', self._p_onGraphViewDeleteEvent )
			self._graphViewWindow.set_border_width( 10 )
			self._graphViewWindow.set_size_request( 800, 600 )

			self._graphViewWindow.add( self._graphView )

			self._graphViewWindow.show()


	def _p_onGraphViewDeleteEvent(self, widget, event, data=None):
		self._graphViewLayout.detachGraph()
		self._graphView.detachGraph()

		self._graphViewWindow.destroy()

		self._graphViewWindow = None
		self._graphViewLayout = None
		self._graphView = None
		self._graphViewDisplayTable = None

		return True




	def _p_onCommandHistoryChanged(self, commandHistory):
		self._bUnsavedData = True




	def _p_onNew(self, widget):
		bProceed = True
		if self._bUnsavedData:
			bProceed = confirmDialog( _( 'New project' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
		if bProceed:
			graph = SheetGraph()
			mainModule = CGModule()
			graph.nodes.append( mainModule )
			self.setGraph( graph, mainModule )


	def _p_onOpen(self, widget):
		bProceed = True
		if self._bUnsavedData:
			bProceed = confirmDialog( _( 'New project' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
		if bProceed:
			gsymFilter = gtk.FileFilter()
			gsymFilter.set_name( _( 'gSym project (*.gsym)' ) )
			gsymFilter.add_pattern( '*.gsym' )

			openDialog = gtk.FileChooserDialog( _( 'Open' ), self._window, gtk.FILE_CHOOSER_ACTION_OPEN,
											( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
			openDialog.add_filter( gsymFilter )
			openDialog.show()
			response = openDialog.run()
			filename = openDialog.get_filename()
			openDialog.destroy()
			if response == gtk.RESPONSE_OK:
				if filename is not None:
					f = open( filename, 'r' )
					if f is not None:
						doc = InputXmlDocument()
						doc.parseFile( f )
						contentNode = doc.getContentNode()
						if contentNode.isValid():
							graphXmlNode = contentNode.getChild( 'graph' )
							rootXmlNode = contentNode.getChild( 'root' )
							if graphXmlNode.isValid()  and  rootXmlNode.isValid():
								graph = graphXmlNode.readObject()
								root = rootXmlNode.readObject()
								self.setGraph( graph, root )


	def _p_onSave(self, widget):
		filename = None
		bFinished = False
		while not bFinished:
			gsymFilter = gtk.FileFilter()
			gsymFilter.set_name( _( 'gSym project (*.gsym)' ) )
			gsymFilter.add_pattern( '*.gsym' )

			saveAsDialog = gtk.FileChooserDialog( _( 'Save As' ), self._window, gtk.FILE_CHOOSER_ACTION_SAVE,
										( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
			saveAsDialog.add_filter( gsymFilter )
			saveAsDialog.show()
			response = saveAsDialog.run()
			filenameFromDialog = saveAsDialog.get_filename()
			saveAsDialog.destroy()
			if response == gtk.RESPONSE_OK:
				if filenameFromDialog is not None:
					if os.path.exists( filenameFromDialog ):
						if confirmOverwriteFileDialog( filenameFromDialog, self._window ):
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

		if filename is not None:
			self._p_writeFile( filename )
			return True
		else:
			return False


	def _p_writeFile(self, filename):
		doc = OutputXmlDocument()
		doc.getContentNode().addChild( 'graph' ).writeObject( self._graph )
		doc.getContentNode().addChild( 'root' ).writeObject( self._graphRoot )
		f = open( filename, 'w' )
		if f is not None:
			doc.writeFile( f )
			f.close()
			self._bUnsavedData = False


	def _p_onUndo(self, widget):
		self._commandHistory.undo()

	def _p_onRedo(self, widget):
		self._commandHistory.redo()






	def _p_onDeleteEvent(self, widget, event, data=None):
		return False

	def _p_onDestroy(self, widget, data=None):
		gtk.main_quit()




	def _p_graphViewCreateLink(sourcePin, sinkPin):
		pass

	def _p_graphViewEraseLink(source, sink):
		pass

