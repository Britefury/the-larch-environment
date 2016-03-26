//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
