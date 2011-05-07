//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ChangeHistory;

import java.util.List;


public interface Trackable
{
	public ChangeHistory getChangeHistory();
	public void setChangeHistory(ChangeHistory h);
	
	public List<Object> getTrackableContents();			// Can return null.   Temporary - is read once - is not needed afterwards
}
