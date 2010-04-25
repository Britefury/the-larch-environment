##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys
import os

from datetime import datetime

from java.lang import Runnable
from javax.swing import AbstractAction, Action, TransferHandler, KeyStroke, BoxLayout, BorderFactory
from javax.swing import JComponent, JFrame, JMenuItem, JMenu, JMenuBar, JMenuItem, JPopupMenu, JOptionPane, JFileChooser, JOptionPane, JTextField, JLabel, JPanel
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Dimension, Font, Color, KeyboardFocusManager
from java.awt.event import WindowListener, ActionListener, ActionEvent, KeyEvent
from java.beans import PropertyChangeListener


from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from BritefuryJ.AttributeTable import AttributeTable

from BritefuryJ.Cell import CellInterface
from BritefuryJ.Utils.Profile import ProfileTimer

from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMNode

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Browser import *
from BritefuryJ.DocPresent.StyleParams import *

from BritefuryJ.DocView import DocView

from BritefuryJ.GSym import GSymBrowserContext, GSymLocationResolver, GSymSubject
from BritefuryJ.GSym.View import GSymViewContext



from Britefury.Kernel.Abstract import abstractmethod


from Britefury.Event.QueuedEvent import queueEvent


from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymDocument import GSymDocument
from Britefury.gSym.AppControlInterface import AppControlInterface

from GSymCore.GSymApp import GSymApp
from GSymCore.Project import Project



_bProfile = True





def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )
	
		
		

		
# Transfer action listener
class _GSymTransferActionListener (ActionListener):
	def __init__(self):
		pass
		
	def actionPerformed(self, e):
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusOwner = manager.getPermanentFocusOwner()
		if focusOwner is not None:
			action = e.getActionCommand()
			a = focusOwner.getActionMap().get( action )
			if a is not None:
				a.actionPerformed( ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, None ) )
				
				

class _WindowLocationResolver (GSymLocationResolver):
	def __init__(self, window):
		self._window = window
		
	
	def resolveLocationAsPage(self, location, persistentState):
		subject = self.resolveLocationAsSubject( location )
		if subject is not None:
			commandHistory = None
			try:
				doc = subject.getSubjectContext()['document']
			except KeyError:
				doc = None
				
			commandHistory = doc.getCommandHistory()   if doc is not None   else None

			viewContext = GSymViewContext( subject, self._window._browserContext, commandHistory, persistentState )
			return viewContext.getPage()
		
		return None
				
	def resolveLocationAsSubject(self, location):
		appState = self._window._app._appState
		if appState is not None:
			world = self._window._app._world
			iterator = location.iterator()
			iterAfterModel = iterator.consumeLiteral( 'model:' )
			perspective = world.getAppStatePerspective()
			if iterAfterModel is not None:
				enclosingSubject = GSymSubject( appState, perspective, '[model]', AttributeTable.instance.withAttrs( world=world, document=None, location=Location( 'model:' ) ) )
				iterator = iterAfterModel
			else:
				enclosingSubject = GSymSubject( appState, perspective, '', AttributeTable.instance.withAttrs( world=world, document=None, location=Location( '' ) ) )
			subject = perspective.resolveRelativeLocation( enclosingSubject, iterator )
			if subject is None:
				return None
			if iterAfterModel:
				subject = subject.withPerspective( self._window._browserContext.getDefaultPerspective() )
			return subject
		else:
			return None
		

		
class _WindowBrowserContext (GSymBrowserContext):
	def __init__(self, window, bWithSystemPages, resolvers=[]):
		super( _WindowBrowserContext, self ).__init__( bWithSystemPages, resolvers )
		self.window = window
		

class AppWindow (object):
	def __init__(self, app, location=Location( '' )):
		self._app = app
		
		self._resolver = _WindowLocationResolver( self )
		self._browserContext = _WindowBrowserContext( self, True, [ self._resolver ] )
		
		
		class _BrowserListener (TabbedBrowser.TabbedBrowserListener):
			def createNewBrowserWindow(_self, location):
				self._createNewWindow( location )
				
				
		self._browser = TabbedBrowser( self._browserContext.getBrowserContext(), _BrowserListener(), location )
		self._browser.getComponent().setPreferredSize( Dimension( 800, 600 ) )

		
		class _CommandHistoryListener (CommandHistoryListener):
			def onCommandHistoryChanged(_self, history):
				self._onCommandHistoryChanged( history )
		
		self._browser.setCommandHistoryListener( _CommandHistoryListener() )
		
		
		
		# NEW MENU
		
		newMenu = JMenu( 'New' )
		newMenu.add( _action( 'New tab', self._onNewTab ) )
		newMenu.add( _action( 'New widnow', self._onNewWindow ) )
		
		
		
		# EDIT MENU
		
		transferActionListener = _GSymTransferActionListener()
		
		editMenu = JMenu( 'Edit' )
		
		self._editUndoItem = JMenuItem( 'Undo' )
		undoAction = _action( 'undo', self._onUndo )
		self._editUndoItem.setActionCommand( undoAction.getValue( Action.NAME ) )
		self._editUndoItem.addActionListener( undoAction )
		self._editUndoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK ) )
		self._editUndoItem.setMnemonic( KeyEvent.VK_U )
		editMenu.add( self._editUndoItem )

		self._editRedoItem = JMenuItem( 'Redo' )
		redoAction = _action( 'redo', self._onRedo )
		self._editRedoItem.setActionCommand( redoAction.getValue( Action.NAME ) )
		self._editRedoItem.addActionListener( redoAction )
		self._editRedoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK ) )
		self._editRedoItem.setMnemonic( KeyEvent.VK_R )
		editMenu.add( self._editRedoItem )

		
		editMenu.addSeparator()
		
		editCutItem = JMenuItem( 'Cut' )
		editCutItem.setActionCommand( TransferHandler.getCutAction().getValue( Action.NAME ) )
		editCutItem.addActionListener( transferActionListener )
		editCutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) )
		editCutItem.setMnemonic( KeyEvent.VK_T )
		editMenu.add( editCutItem )
		
		editCopyItem = JMenuItem( 'Copy' )
		editCopyItem.setActionCommand( TransferHandler.getCopyAction().getValue( Action.NAME ) )
		editCopyItem.addActionListener( transferActionListener )
		editCopyItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) )
		editCopyItem.setMnemonic( KeyEvent.VK_C )
		editMenu.add( editCopyItem )
		
		editPasteItem = JMenuItem( 'Paste' )
		editPasteItem.setActionCommand( TransferHandler.getPasteAction().getValue( Action.NAME ) )
		editPasteItem.addActionListener( transferActionListener )
		editPasteItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) )
		editPasteItem.setMnemonic( KeyEvent.VK_P )
		editMenu.add( editPasteItem )
		

		
		
		# VIEW MENU
		
		viewMenu = JMenu( 'View' )
		viewMenu.add( _action( 'View document model', self._onViewDocModel ) )
		viewMenu.add( _action( 'Show element tree explorer', self._onShowElementTreeExplorer ) )
		viewMenu.add( _action( 'Reset', self._onReset ) )
		viewMenu.add( _action( '1:1', self._onOneToOne ) )
		
		
		
		menuBar = JMenuBar();
		menuBar.add( newMenu )
		menuBar.add( editMenu )
		menuBar.add( viewMenu )

		
		
		# WINDOW
		
		windowPanel = JPanel()
		windowPanel.setLayout( BoxLayout( windowPanel, BoxLayout.Y_AXIS ) )
		windowPanel.add( self._browser.getComponent() )
		
		
		

		self._frame = JFrame( 'gSym' )
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		self._frame.setJMenuBar( menuBar )
		
		self._frame.add( windowPanel )
		
		self._frame.pack()
		
		
		# Cause command history controls to refresh
		self._onCommandHistoryChanged( None )
		
		
		

	
	def show(self):
		self._frame.setVisible( True )

		
		
		
		
	def _onCommandHistoryChanged(self, commandHistory):
		if commandHistory is not None:
			self._editUndoItem.setEnabled( commandHistory.canUndo() )
			self._editRedoItem.setEnabled( commandHistory.canRedo() )
		else:
			self._editUndoItem.setEnabled( False )
			self._editRedoItem.setEnabled( False )
			
		
		
	# handleNewPageFn(unit)
	def populateNewPageMenu(self, menu, handleNewPageFn):
		def _make_newPage(newPageFn):
			def newPage(actionEvent):
				unit = newPageFn()
				handleNewPageFn( unit )
			return newPage
		for newPageFactory in self._app._world.newPageFactories:
			menu.addItem( newPageFactory.menuLabelText, _make_newPage( newPageFactory.newPageFn ) )
		
		
		
	# handleImportedPageFn(name, unit)
	def populateImportPageMenu(self, menu, handleImportedPageFn):
		def _make_importPage(fileType, filePattern, importUnitFn):
			def _import(actionEvent):
				openDialog = JFileChooser()
				openDialog.setFileFilter( FileNameExtensionFilter( fileType, [ filePattern ] ) )
				response = openDialog.showDialog( self._frame, 'Import' )
				if response == JFileChooser.APPROVE_OPTION:
					sf = openDialog.getSelectedFile()
					if sf is not None:
						filename = sf.getPath()
						if filename is not None:
							t1 = datetime.now()
							unit = importUnitFn( filename )
							t2 = datetime.now()
							if unit is not None:
								unitName = os.path.splitext( filename )[0]
								unitName = os.path.split( unitName )[1]
								print 'MainApp: IMPORT TIME = %s'  %  ( t2 - t1, )
								handleImportedPageFn( unitName, unit )
			return _import

		for pageImporter in self._app._world.pageImporters:
			menu.addItem( pageImporter.menuLabelText, _make_importPage( pageImporter.fileType, pageImporter.filePattern, pageImporter.importFn ) )
			
			
		
		
	# handleNewDocumentFn(unit)
	def promptNewDocument(self, handleNewDocumentFn):
		def _make_newDocument(newUnitFn):
			def newDoc():
				unit = newUnitFn()
				handleNewDocumentFn( unit )
			return newDoc
		newDocumentMenu = JPopupMenu( 'New document' )
		for newUnitFactory in self._app._world.newUnitFactories:
			newDocumentMenu.add( _action( newUnitFactory.menuLabelText, _make_newDocument( newUnitFactory.newDocumentFn ) ) )
		pos = self._frame.getMousePosition( True )
		newDocumentMenu.show( self._frame, pos.x, pos.y )
	
		
		
	# handleOpenedDocumentFn(fullPath, document)
	def promptOpenDocument(self, handleOpenedDocumentFn):
		openDialog = JFileChooser()
		openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
		response = openDialog.showDialog( self._frame, 'Open' )
		if response == JFileChooser.APPROVE_OPTION:
			sf = openDialog.getSelectedFile()
			if sf is not None:
				filename = sf.getPath()
				if filename is not None:
					document = GSymDocument.readFile( self._app._world, filename )
					if document is not None:
						handleOpenedDocumentFn( filename, document )
	
	
	
	# handleSaveDocumentAsFn(filename)
	def promptSaveDocumentAs(self, handleSaveDocumentAsFn):
		filename = None
		bFinished = False
		while not bFinished:
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
			response = openDialog.showSaveDialog( self._frame )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filenameFromDialog = sf.getPath()
					if filenameFromDialog is not None:
						if os.path.exists( filenameFromDialog ):
							response = JOptionPane.showOptionDialog( self._frame, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
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
		
		
	
	def getWorld(self):
		return self._app.getWorld()

	
	def getBrowserContext(self):
		return self._browserContext

	
	
	
	def _onNewTab(self):
		self._browser.openLocationInNewTab( '' )
	
	
	def _onNewWindow(self):
		self._createNewWindow( Location( '' ) )
		
		
	def _createNewWindow(self, location):
		newWindow = self._app._createNewWindow( location )
		newWindow.show()
	
	
	
	def _onUndo(self):
		commandHistoryController = self._browser.getCommandHistoryController()
		if commandHistoryController.canUndo():
			commandHistoryController.undo()

	def _onRedo(self):
		commandHistoryController = self._browser.getCommandHistoryController()
		if commandHistoryController.canRedo():
			commandHistoryController.redo()


		

	def _onViewDocModel(self):
		currentLoc = self._browser.getCurrentBrowserLocation().getLocationString()
		if currentLoc.startswith( 'model:' ):
			currentLoc = currentLoc[6:]
		else:
			currentLoc = 'model:' + currentLoc
		self._browser.openLocationInCurrentTab( Location( currentLoc ) )
	
	
	def _onShowElementTreeExplorer(self):
		currentTab = self._browser.getCurrentBrowser()
		element = currentTab.getContentsElement()
		location = self._browserContext.getLocationForObject( element )
		self._browser.openLocationInNewWindow( location )


	def _onReset(self):
		self._browser.viewportReset()

	def _onOneToOne(self):
		self._browser.viewportOneToOne()




	


