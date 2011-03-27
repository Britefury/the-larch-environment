//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CommandHistory;

public interface Trackable extends HasCommandHistory
{
	//public void getCommandHistory();					// Defined in HasCommandHistory
	public void setCommandHistory(CommandHistory h);
	
	public void trackContents(CommandHistory history);
	public void stopTrackingContents(CommandHistory history);
}
