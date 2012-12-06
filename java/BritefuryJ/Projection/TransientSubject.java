//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Projection;

import java.util.List;

public abstract class TransientSubject extends Subject
{
	//
	// Transient path entry
	//
	
	static class TransientSubjectPathEntry implements SubjectPathEntry
	{
		protected Subject subject;
		
		
		public TransientSubjectPathEntry(Subject subject)
		{
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
	
	
	public TransientSubject(Subject enclosingSubject)
	{
		super( enclosingSubject, new SubjectPath( new TransientSubjectPathEntry( null ) ) );
		List<SubjectPathEntry> pathEntries = path().entries;
		((TransientSubjectPathEntry)pathEntries.get( pathEntries.size() - 1 )).subject = this;
	}
}
