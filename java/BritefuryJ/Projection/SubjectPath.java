//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Projection;

import java.util.ArrayList;

public class SubjectPath
{
	protected ArrayList<SubjectPathEntry> entries;
	
	
	public SubjectPath()
	{
		entries = new ArrayList<SubjectPathEntry>();
	}

	public SubjectPath(SubjectPathEntry entry)
	{
		entries = new ArrayList<SubjectPathEntry>();
		entries.add( entry );
	}
	
	
	public SubjectPath followedBy(SubjectPathEntry entry)
	{
		SubjectPath joinedPath = new SubjectPath();
		joinedPath.entries.addAll( entries );
		joinedPath.entries.add( entry );
		return joinedPath;
	}
	
	public SubjectPath followedBy(SubjectPath path)
	{
		SubjectPath joinedPath = new SubjectPath();
		joinedPath.entries.addAll( entries );
		joinedPath.entries.addAll( path.entries );
		return joinedPath;
	}
	
	
	public SubjectPath __add__(SubjectPath path)
	{
		return followedBy( path );
	}
	
	
	
	public boolean isWithin(SubjectPath absPath)
	{
		if ( absPath.entries.size() > entries.size() )
		{
			return false;
		}
		
		for (int i = 0; i < absPath.entries.size(); i++)
		{
			if ( !absPath.entries.get( i ).equals( entries.get( i ) ) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public SubjectPath relativeTo(SubjectPath absPath)
	{
		if ( !isWithin( absPath ) )
		{
			throw new RuntimeException( "@this is not within absPath" );
		}
		
		SubjectPath relPath = new SubjectPath();
		relPath.entries.addAll( entries.subList( absPath.entries.size(), entries.size() ) );
		
		return relPath;
	}
	
	
	public Subject followFrom(Subject rootSubject)
	{
		Subject subject = rootSubject;
		
		for (SubjectPathEntry e: entries)
		{
			subject = e.follow( subject );
		}
		
		return subject;
	}
	
	
	public boolean canPersist()
	{
		for (SubjectPathEntry e: entries)
		{
			if ( !e.canPersist() )
			{
				return false;
			}
		}
		
		return true;
	}
}
