//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Projection;

import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Command.BoundCommandSet;

public abstract class Subject
{
	private static class AbsoluteSubjectPathEntry implements SubjectPathEntry
	{
		private Subject subject;
		
		public AbsoluteSubjectPathEntry(Subject subject)
		{
			this.subject = subject;
		}
		
		@Override
		public Subject follow(Subject outerSubject)
		{
			return subject;
		}

		@Override
		public boolean canPersist()
		{
			return false;
		}
	}
	
	
	private Subject enclosingSubject;
	private SubjectPath path;
	
	
	public Subject(Subject enclosingSubject, SubjectPath path)
	{
		this.enclosingSubject = enclosingSubject;
		this.path = path;
	}
	
	public Subject(Subject enclosingSubject)
	{
		this.enclosingSubject = enclosingSubject;
		this.path = new SubjectPath( new AbsoluteSubjectPathEntry( this ) );
	}
	
	
	
	public Subject getEnclosingSubject()
	{
		return enclosingSubject;
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
	
	
	public void buildBoundCommandSetList(List<BoundCommandSet> boundCommandSets)
	{
		if ( enclosingSubject != null )
		{
			enclosingSubject.buildBoundCommandSetList( boundCommandSets );
		}
	}
	
	
	
	public Subject redirect()
	{
		return null;
	}
	
	
	public SubjectPath path()
	{
		return path;
	}
}
