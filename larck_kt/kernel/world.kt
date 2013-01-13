package kernel

import BritefuryJ.Projection.Subject
import BritefuryJ.IncrementalView.FragmentView
import BritefuryJ.LSpace.LSElement
import BritefuryJ.LSpace.Event.PointerButtonEvent

/**
 * Created with IntelliJ IDEA.
 * User: Geoff
 * Date: 12/01/13
 * Time: 12:11
 * To change this template use File | Settings | File Templates.
 */


class World (val rootSubj: Subject) {
    public val rootSubject : Subject = rootSubj



    public fun inspectFragment(fragment : FragmentView?, sourceElement : LSElement?, triggerringEvent : PointerButtonEvent?) : Boolean {
        return false;
    }
}