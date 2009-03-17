//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;

public class DMObjectTrackerFactory implements CommandTrackerFactory
{
	public static DMObjectTrackerFactory factory = new DMObjectTrackerFactory();
	
	public CommandTracker createTracker(CommandHistory history)
	{
		return new DMObjectCommandTracker( history );
	}
}
