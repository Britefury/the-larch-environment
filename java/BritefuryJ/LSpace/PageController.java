//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Projection.Subject;


public interface PageController
{
	public enum OpenOperation
	{
		OPEN_IN_CURRENT_TAB,
		OPEN_IN_NEW_TAB,
		OPEN_IN_NEW_WINDOW
	}
	
	public void openSubject(Subject subject, OpenOperation op);
}
