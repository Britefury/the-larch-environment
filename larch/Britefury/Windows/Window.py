##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.lang import Runnable
from javax.swing import AbstractAction, Action, TransferHandler, KeyStroke, BoxLayout, BorderFactory
from javax.swing import JComponent, JFrame, JMenuItem, JMenu, JMenuBar, JMenuItem, JOptionPane, JFileChooser, JOptionPane, JTextField, JLabel, JPanel
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Dimension, Color, KeyboardFocusManager
from java.awt.event import WindowListener, ActionListener, ActionEvent, KeyEvent
from java.beans import PropertyChangeListener


from BritefuryJ.ChangeHistory import ChangeHistory, ChangeHistoryListener

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMNode

from BritefuryJ.Browser import TabbedBrowser

from BritefuryJ.Pres.Help import AttachTooltip, TipBox

from BritefuryJ.Util import Platform



def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )
	
		
		

		
# Transfer action listener
class _TransferActionListener (ActionListener):
	def __init__(self):
		pass
		
	def actionPerformed(self, e):
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
		focusOwner = manager.getPermanentFocusOwner()
		if focusOwner is not None:
			action = e.getActionCommand()
			a = focusOwner.getActionMap().get( action )
			if a is not None:
				a.actionPerformed( ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, None ) )
				
				

		
class Window (object):
	def __init__(self, windowManager, commandConsoleFactory, subject, windowTitle):
		self._windowManager = windowManager





		self.onCloseRequestListener = None



		# EDIT MENU

		transferActionListener = _TransferActionListener()

		editMenu = JMenu( 'Edit' )

		if Platform.getPlatform() is Platform.MAC:
			command_key_mask = ActionEvent.META_MASK
		else:
			command_key_mask = ActionEvent.CTRL_MASK;

		self.__editUndoItem = JMenuItem( 'Undo' )
		undoAction = _action( 'undo', self.__onUndo )
		self.__editUndoItem.setActionCommand( undoAction.getValue( Action.NAME ) )
		self.__editUndoItem.addActionListener( undoAction )
		self.__editUndoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, command_key_mask ) )
		self.__editUndoItem.setMnemonic( KeyEvent.VK_U )
		editMenu.add( self.__editUndoItem )

		self.__editRedoItem = JMenuItem( 'Redo' )
		redoAction = _action( 'redo', self.__onRedo )
		self.__editRedoItem.setActionCommand( redoAction.getValue( Action.NAME ) )
		self.__editRedoItem.addActionListener( redoAction )
		self.__editRedoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y, command_key_mask ) )
		self.__editRedoItem.setMnemonic( KeyEvent.VK_R )
		editMenu.add( self.__editRedoItem )

		editMenu.addSeparator()

		editCutItem = JMenuItem( 'Cut' )
		editCutItem.setActionCommand( TransferHandler.getCutAction().getValue( Action.NAME ) )
		editCutItem.addActionListener( transferActionListener )
		editCutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, command_key_mask ) )
		editCutItem.setMnemonic( KeyEvent.VK_T )
		editMenu.add( editCutItem )

		editCopyItem = JMenuItem( 'Copy' )
		editCopyItem.setActionCommand( TransferHandler.getCopyAction().getValue( Action.NAME ) )
		editCopyItem.addActionListener( transferActionListener )
		editCopyItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, command_key_mask ) )
		editCopyItem.setMnemonic( KeyEvent.VK_C )
		editMenu.add( editCopyItem )

		editPasteItem = JMenuItem( 'Paste' )
		editPasteItem.setActionCommand( TransferHandler.getPasteAction().getValue( Action.NAME ) )
		editPasteItem.addActionListener( transferActionListener )
		editPasteItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, command_key_mask ) )
		editPasteItem.setMnemonic( KeyEvent.VK_P )
		editMenu.add( editPasteItem )

		editMenu.addSeparator()

		self.__showUndoHistoryItem = JMenuItem( 'Show undo history' )
		self.__showUndoHistoryItem.addActionListener( _action( 'Show undo history', self.__onShowUndoHistory ) )
		editMenu.add( self.__showUndoHistoryItem )




		# HELP MENU

		helpMenu = JMenu( 'Help' )

		helpToggleTooltipHighlightsItem = JMenuItem( 'Toggle tooltip highlights' )
		toggleTooltipHighlightsAction = _action( 'Toggle tooltip highlights', self.__onToggleTooltipHighlights )
		helpToggleTooltipHighlightsItem.setActionCommand( toggleTooltipHighlightsAction.getValue( Action.NAME ) )
		helpToggleTooltipHighlightsItem.addActionListener( toggleTooltipHighlightsAction )
		helpToggleTooltipHighlightsItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F2, 0 ) )
		helpMenu.add( helpToggleTooltipHighlightsItem )

		helpMenu.add( _action( 'Show all tip boxes', self.__onShowAllTipBoxes ) )


		# MENU BAR

		menuBar = JMenuBar()
		menuBar.add( editMenu )
		menuBar.add( helpMenu )




		# BROWSER

		# Initialise here, as the browser listener may invoke methods upon the browser's creation
		class _BrowserListener (TabbedBrowser.TabbedBrowserListener):
			def createNewBrowserWindow(_self, subject):
				self._onOpenNewWindow( subject )

			def onTabbledBrowserChangePage(_self, browser):
				pass


		def inspectFragment(fragment, sourceElement, triggeringEvent):
			return self._windowManager.world.inspectFragment( fragment, sourceElement, triggeringEvent )



		def onChangeHistoryChanged(history):
			self.__refreshChangeHistoryControls( history )

		self._browser = TabbedBrowser( self._windowManager.world.rootSubject, subject, inspectFragment, _BrowserListener(), commandConsoleFactory )
		self._browser.getComponent().setPreferredSize( Dimension( 800, 600 ) )
		changeHistory = self._browser.getChangeHistory()
		self._browser.getChangeHistory().addChangeHistoryListener(onChangeHistoryChanged)





		# MAIN PANEL

		windowPanel = JPanel()
		windowPanel.setLayout( BoxLayout( windowPanel, BoxLayout.Y_AXIS ) )
		windowPanel.add( self._browser.getComponent() )




		# WINDOW

		class _WindowLister (WindowListener):
			def windowActivated(listenerSelf, event):
				pass

			def windowClosed(listenerSelf, event):
				pass

			def windowClosing(listenerSelf, event):
				if self.onCloseRequestListener is not None:
					self.onCloseRequestListener( self )

			def windowDeactivated(listenerSelf, event):
				pass

			def windowDeiconified(listenerSelf, event):
				pass

			def windowIconified(listenerSelf, event):
				pass

			def windowOpened(listenerSelf, event):
				pass


		self.__frame = JFrame( windowTitle )

		self.__frame.setJMenuBar( menuBar )

		self.__frame.add( windowPanel )
		self.__frame.addWindowListener( _WindowLister() )
		self.__frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE )

		self.__frame.pack()


		# Cause command history controls to refresh
		self.__refreshChangeHistoryControls( None )





	@property
	def frame(self):
		return self.__frame


	@property
	def tabbedBrowser(self):
		return self._browser

	@property
	def currentBrowser(self):
		return self._browser.getCurrentBrowser()



	def show(self):
		self.__frame.setVisible( True )

	def close(self):
		self.__frame.dispose()




	def __refreshChangeHistoryControls(self, changeHistory):
		if changeHistory is not None:
			self.__editUndoItem.setEnabled( changeHistory.canUndo() )
			self.__editRedoItem.setEnabled( changeHistory.canRedo() )
			self.__showUndoHistoryItem.setEnabled( True )
		else:
			self.__editUndoItem.setEnabled( False )
			self.__editRedoItem.setEnabled( False )
			self.__showUndoHistoryItem.setEnabled( False )
			
		
		
			

	
	def _onOpenNewWindow(self, subject):
		self._windowManager._createNewWindow( subject )
	
	
	
	def __onUndo(self):
		changeHistory = self._browser.getChangeHistory()
		if changeHistory.canUndo():
			changeHistory.concreteChangeHistory().undo()

	def __onRedo(self):
		changeHistory = self._browser.getChangeHistory()
		if changeHistory.canRedo():
			changeHistory.concreteChangeHistory().redo()


		

	def __onShowUndoHistory(self):
		changeHistory = self._browser.getChangeHistory().concreteChangeHistory()
		if changeHistory is not None:
			subject = DefaultPerspective.instance.objectSubject( changeHistory )
			self._browser.openSubjectInNewWindow( subject )




	def __onToggleTooltipHighlights(self):
		AttachTooltip.toggleHighlights()


	def __onShowAllTipBoxes(self):
		TipBox.resetTipHiddenStates()




