//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

public interface TreeEventListener
{
    /**
     * Handle a tree event
     *
     * @param element - the element to which the tree event handler is attached
     * @param sourceElement - the element at which the event originally occurred before bubbling up to `element`
     * @param event - the event
     * @return - a boolean indicating if the event was consumed
     */
	boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event);
}
