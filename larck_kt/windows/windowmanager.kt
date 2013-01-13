package windows

import kernel.World
import BritefuryJ.Command.CommandConsoleFactory
import BritefuryJ.LSpace.PresentationComponent
import BritefuryJ.Browser.Browser
import BritefuryJ.Command.AbstractCommandConsole
import BritefuryJ.Command.CommandConsole
import java.util.HashSet
import BritefuryJ.Projection.Subject

/**
 * Created with IntelliJ IDEA.
 * User: Geoff
 * Date: 12/01/13
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */


class WindowManager (public val world: World) {
    public var closeLastWindowListener: ((windowManager: WindowManager) -> Unit)? = null


    private val createCommandConsole = object : CommandConsoleFactory {
        override fun createCommandConsole(pres: PresentationComponent?, browser: Browser?) : AbstractCommandConsole {
            return CommandConsole(browser, pres)
        }
    }

    private val appState = world.rootSubject.getFocus()

    private val rootWindow = Window(this, createCommandConsole, world.rootSubject);
    {
        rootWindow.closeRequestListener = {window -> onWindowCloseRequest(window)}
    }

    private val openWindows = HashSet<Window>();
    {
        openWindows.add(rootWindow)
    }



    public fun showRootWindow() {
        rootWindow.show()
    }


    public fun closeAllWindow() {
        for (window in openWindows) {
            window.close()
        }
        openWindows.clear()
        windowClosed()
    }


    internal fun createNewWindow(subject: Subject) {
        val newWindow = Window(this, createCommandConsole, subject)
        newWindow.closeRequestListener = { window -> onWindowCloseRequest(window) }
        newWindow.close()
        openWindows.add(newWindow)
    }


    private fun onWindowCloseRequest(window: Window) {
        if (openWindows.size == 1) {
            // Only one window open

            // Invoke the application state's onCloseRequest method to determine if closing is allowed
            if (appState is AppWindowCloseListener)
            {
                appState.onCloseAppWindow(this, window)
            }
        }

        window.close()
        openWindows.remove(window)
        windowClosed()
    }


    private fun windowClosed() {
        if (openWindows.size == 0) {

            //closeLastWindowListener(this)

            //closeLastWindowListener

            val l : (windowManager: WindowManager) -> Unit = closeLastWindowListener ?: {}
            l(this)

            if (appState is AppCloseListener)
            {
                appState.onCloseApp(this)
            }
        }
    }

}



trait AppCloseListener {
    open fun onCloseApp(windowManager: WindowManager): Unit
}


trait AppWindowCloseListener {
    open fun onCloseAppWindow(windowManager: WindowManager, window: Window): Unit
}