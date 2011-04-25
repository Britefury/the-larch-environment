//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Projection;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Command.BoundCommandSet;

public abstract class Subject
{
	private Subject enclosingSubject;
	
	
	public Subject(Subject enclosingSubject)
	{
		this.enclosingSubject = enclosingSubject;
	}
	
	
	public abstract Object getFocus();
	
	
	public AbstractPerspective getPerspective()
	{
		return null;
	}
	
	public abstract String getTitle();

	
	public SimpleAttributeTable getSubjectContext()
	{
		if ( enclosingSubject != null )
		{
			return enclosingSubject.getSubjectContext();
		}
		else
		{
			return SimpleAttributeTable.instance;
		}
	}
	
	
	public ChangeHistory getChangeHistory()
	{
		if ( enclosingSubject != null )
		{
			return enclosingSubject.getChangeHistory();
		}
		else
		{
			return null;
		}
	}
	
	
	public List<BoundCommandSet> getBoundCommandSets()
	{
		if ( enclosingSubject != null )
		{
			return enclosingSubject.getBoundCommandSets();
		}
		else
		{
			return Arrays.asList( new BoundCommandSet[] {} );
		}
	}
}
