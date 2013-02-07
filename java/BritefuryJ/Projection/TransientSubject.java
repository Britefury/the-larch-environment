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
	
	static class FixedSubjectPathEntry extends TransientSubjectPathEntry
	{
		protected Subject subject;
		
		
		public FixedSubjectPathEntry(Subject subject)
		{
		}
		
		@Override
		public Subject follow(Subject outerSubject)
		{
			return subject;
		}
	}
	
	
	public TransientSubject(Subject enclosingSubject)
	{
		super( enclosingSubject, new SubjectPath( new FixedSubjectPathEntry( null ) ) );
		List<AbstractSubjectPathEntry> pathEntries = path().entries;
		((FixedSubjectPathEntry)pathEntries.get( pathEntries.size() - 1 )).subject = this;
	}
}
