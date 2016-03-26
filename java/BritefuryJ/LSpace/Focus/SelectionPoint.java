//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Focus;


public abstract class SelectionPoint
{
	public abstract boolean isValid();
	
	public abstract Selection createSelectionTo(SelectionPoint point);
}
