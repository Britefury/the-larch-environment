package windows

import BritefuryJ.Command.CommandConsoleFactory
import BritefuryJ.Projection.Subject
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.KeyboardFocusManager
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.Action
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.TransferHandler
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JMenuBar
import javax.swing.BoxLayout
import BritefuryJ.Browser.TabbedBrowser
import BritefuryJ.Browser.Browser
import BritefuryJ.IncrementalView.FragmentView
import BritefuryJ.LSpace.LSElement
import BritefuryJ.LSpace.Event.PointerButtonEvent
import BritefuryJ.IncrementalView.FragmentInspector
import javax.swing.JPanel
import java.awt.Component
import java.awt.event.WindowListener
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.WindowConstants
import BritefuryJ.ChangeHistory.ChangeHistoryListener
import BritefuryJ.ChangeHistory.ChangeHistory
import BritefuryJ.ChangeHistory.ChangeHistoryController
import BritefuryJ.DefaultPerspective.DefaultPerspective
import BritefuryJ.Pres.Help.AttachTooltip
import BritefuryJ.Pres.Help.TipBox

/**
 * Created with IntelliJ IDEA.
 * User: Geoff
 * Date: 12/01/13
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */


class Window (val windowManager : WindowManager, val commandConsoleFactory : CommandConsoleFactory, val subject: Subject) {
    public var closeRequestListener : ((window: Window) -> Unit)? = null




    private val transferActionListener = object : ActionListener {
        public override fun actionPerformed(e : ActionEvent) : Unit {
            var manager : KeyboardFocusManager? = KeyboardFocusManager.getCurrentKeyboardFocusManager()
            var focusOwner : JComponent? = (manager?.getPermanentFocusOwner() as JComponent?)
            if (focusOwner != null)
            {
                var action : String? = e.getActionCommand()
                var a : Action? = focusOwner?.getActionMap()?.get(action ?: "")
                if (a != null)
                {
                    a?.actionPerformed(ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null))
                }

            }

        }
    }


    private val changeHistoryListener = object : ChangeHistoryListener {
        override fun onChangeHistoryChanged(history: ChangeHistoryController?) {
            refreshChangeHistoryControls(history)
        }
    }


    private val editMenu: JMenu = JMenu("Edit")
    private val editUndoItem = JMenuItem("Undo")
    private val editRedoItem = JMenuItem("Redo")
    private val showUndoHistoryItem = JMenuItem("Show undo history")

    private var prevChangeHistory : ChangeHistory? = null

    public val currentBrowser : Browser?
        get() = tabbedBrowser.getCurrentBrowser()

    public val tabbedBrowser: TabbedBrowser
    public val frame : JFrame

    {
        val undoAction = action("undo", {undo()})
        editUndoItem.setActionCommand(undoAction.getValue(Action.NAME) as String?)
        editUndoItem.addActionListener(undoAction)
        editUndoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK))
        editUndoItem.setMnemonic(KeyEvent.VK_U)
        editMenu.add(editUndoItem)

        val redoAction = action("redo", {redo()})
        editRedoItem.setActionCommand(redoAction.getValue(Action.NAME) as String?)
        editRedoItem.addActionListener(redoAction)
        editRedoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK))
        editRedoItem.setMnemonic(KeyEvent.VK_R)
        editMenu.add(editRedoItem)


        val editCutItem = JMenuItem("Cut")
        editCutItem.setActionCommand((TransferHandler.getCutAction().getValue(Action.NAME) as String?))
        editCutItem.addActionListener(transferActionListener)
        editCutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK))
        editCutItem.setMnemonic(KeyEvent.VK_T)
        editMenu.add(editCutItem)

        val editCopyItem = JMenuItem("Copy")
        editCopyItem.setActionCommand((TransferHandler.getCopyAction().getValue(Action.NAME) as String?))
        editCopyItem.addActionListener(transferActionListener)
        editCopyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK))
        editCopyItem.setMnemonic(KeyEvent.VK_C)
        editMenu.add(editCopyItem)

        val editPasteItem = JMenuItem("Paste")
        editPasteItem.setActionCommand((TransferHandler.getPasteAction().getValue(Action.NAME) as String?))
        editPasteItem.addActionListener(transferActionListener)
        editPasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK))
        editPasteItem.setMnemonic(KeyEvent.VK_P)
        editMenu.add(editPasteItem)

        editMenu.addSeparator()

        showUndoHistoryItem.addActionListener(action("Show undo history", {showUndoHistory()}))
        editMenu.add(showUndoHistoryItem)



        val helpMenu = JMenu("Help")
        val toggleTooltipHighlightsItem = JMenuItem("Toggle tooltip highlights")
        val toggleTooltipHighlightsAction = action("Toggle tooltip highlights", {toggleTooltipHighlights()})
        toggleTooltipHighlightsItem.setActionCommand(toggleTooltipHighlightsAction.getValue(Action.NAME) as String?)
        toggleTooltipHighlightsItem.addActionListener(toggleTooltipHighlightsAction)
        toggleTooltipHighlightsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0))
        helpMenu.add(toggleTooltipHighlightsItem)

        helpMenu.add(action("Show all tip boxes", {showAllTipBoxes()}))


        val menuBar = JMenuBar()
        menuBar.add(editMenu)
        menuBar.add(helpMenu)


        // Browser
        val browserListener = object : TabbedBrowser.TabbedBrowserListener {
            public override fun createNewBrowserWindow(subject: Subject?) : Unit {
                if (subject != null) {
                    openNewWindow(subject)
                }
            }

            public override fun onTabbledBrowserChangePage(browser: TabbedBrowser?) : Unit {
                changePage(browser)
            }
        }

        val inspector = object : FragmentInspector {
            public override fun inspectFragment(fragment : FragmentView?, sourceElement : LSElement?, triggerringEvent : PointerButtonEvent?) : Boolean {
                return windowManager.world.inspectFragment(fragment, sourceElement, triggerringEvent)
            }
        }

        tabbedBrowser = TabbedBrowser(windowManager.world.rootSubject, subject, inspector, browserListener, commandConsoleFactory)
        tabbedBrowser.getComponent()?.setPreferredSize(Dimension(800, 600))


        // Main panel
        val windowPanel = JPanel()
        windowPanel.setLayout(BoxLayout(windowPanel, BoxLayout.Y_AXIS))
        val c: Component? = tabbedBrowser.getComponent();
        if (c != null)
        {
            windowPanel.add(c)
        }



        // Window
        val windowListener = object : WindowListener {
            public override fun windowOpened(event: WindowEvent?) : Unit {
            }

            public override fun windowClosing(event: WindowEvent) : Unit {
                val l : ((window: Window) -> Unit) = closeRequestListener ?: {}
                l(this@Window)
            }

            public override fun windowClosed(event: WindowEvent?) : Unit {
            }

            public override fun windowActivated(event: WindowEvent?) : Unit {
            }

            public override fun windowDeactivated(event: WindowEvent?) : Unit {
            }

            public override fun windowIconified(event: WindowEvent?) : Unit {
            }

            public override fun windowDeiconified(event: WindowEvent?) : Unit {
            }
        }

        frame = JFrame("Larch")
        frame.setJMenuBar(menuBar)
        frame.add(windowPanel)
        frame.addWindowListener(windowListener)
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
        frame.pack()

        refreshChangeHistoryControls(null)
    }


    public fun show() : Unit {
        frame.setVisible(true)
    }

    public fun close() : Unit {
        frame.dispose()
    }



    private fun changePage(browser: TabbedBrowser?) : Unit {
        val changeHistory = browser?.getChangeHistory()

        if (changeHistory != prevChangeHistory) {
            prevChangeHistory?.removeChangeHistoryListener(changeHistoryListener)

            changeHistory?.addChangeHistoryListener(changeHistoryListener)

            prevChangeHistory = changeHistory

            refreshChangeHistoryControls(changeHistory)
        }
    }


    private fun refreshChangeHistoryControls(history: ChangeHistoryController?) {
        if (history != null) {
            editUndoItem.setEnabled(history.canUndo())
            editRedoItem.setEnabled(history.canRedo())
            showUndoHistoryItem.setEnabled(true)
        }
        else {
            editUndoItem.setEnabled(false)
            editRedoItem.setEnabled(false)
            showUndoHistoryItem.setEnabled(false)
        }
    }



    private fun openNewWindow(subject: Subject) : Unit {
        windowManager.createNewWindow(subject)
    }



    private fun undo() : Unit {
        val changeHistory = tabbedBrowser.getChangeHistory()
        if (changeHistory != null) {
            if (changeHistory.canUndo()) {
                changeHistory.undo()
            }
        }
    }

    private fun redo() : Unit {
        val changeHistory = tabbedBrowser.getChangeHistory()
        if (changeHistory != null) {
            if (changeHistory.canRedo()) {
                changeHistory.redo()
            }
        }
    }

    private fun showUndoHistory() : Unit {
        val changeHistory = tabbedBrowser.getChangeHistory()
        if (changeHistory != null) {
            val subject = DefaultPerspective.instance.objectSubject(changeHistory)
            tabbedBrowser.openSubjectInNewWindow(subject)
        }
    }


    private fun toggleTooltipHighlights() : Unit {
        AttachTooltip.toggleHighlights()
    }

    private fun showAllTipBoxes() : Unit {
        TipBox.resetTipHiddenStates()
    }




    private fun action(name: String, f: () -> Unit) : AbstractAction {
        val a = object : AbstractAction(name) {
            override fun actionPerformed(event: ActionEvent) : Unit {
                f()
            }
        }
        return a
    }
}