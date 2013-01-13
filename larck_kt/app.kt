/**
 * Created with IntelliJ IDEA.
 * User: Geoff
 * Date: 13/01/13
 * Time: 08:27
 * To change this template use File | Settings | File Templates.
 */


import javax.swing.UIManager
import kernel.World
import BritefuryJ.Projection.TransientSubject
import windows.WindowManager

fun main(args: Array<String>) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val app = LarchKtApp()
    val subject = RootSubject(app)


    var world = World(subject)
    val wm = WindowManager(world)

    wm.closeLastWindowListener = {}

    wm.showRootWindow()
}



class LarchKtApp: Any() {

}



class RootSubject(val fcs: Any) : TransientSubject(null) {
    override fun getFocus(): Any = fcs

    override fun getTitle(): String? = "Kotlin-Larch"
}
