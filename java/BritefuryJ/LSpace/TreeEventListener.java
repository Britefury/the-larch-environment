//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
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
