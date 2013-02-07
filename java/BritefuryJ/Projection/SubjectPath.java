//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Projection;

import org.python.core.*;

import java.util.ArrayList;

public class SubjectPath
{
	protected ArrayList<AbstractSubjectPathEntry> entries;
	
	
	public SubjectPath()
	{
		entries = new ArrayList<AbstractSubjectPathEntry>();
	}

	public SubjectPath(AbstractSubjectPathEntry entry)
	{
		entries = new ArrayList<AbstractSubjectPathEntry>();
		entries.add( entry );
	}
	
	
	public SubjectPath followedBy(AbstractSubjectPathEntry entry)
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
		
		for (AbstractSubjectPathEntry e: entries)
		{
			subject = e.follow( subject );
		}
		
		return subject;
	}
	
	
	public boolean canPersist()
	{
		for (AbstractSubjectPathEntry e: entries)
		{
			if ( !e.canPersist() )
			{
				return false;
			}
		}
		
		return true;
	}



	public PyObject __getstate__()
	{
		if ( canPersist() )
		{
			PyObject elts[] = new PyObject[entries.size()];
			for (int i = 0; i < elts.length; i++)
			{
				elts[i] = Py.java2py( entries.get( i ) );
			}
			PyList eltsList = new PyList( elts );
			PyDictionary d = new PyDictionary();
			d.__setitem__( Py.newString( "pathEntries" ), eltsList );
			return d;
		}
		else
		{
			throw Py.ValueError( "SubjectPath cannot be persisted; contains one or more un-persistable path entries" );
		}
	}

	public void __setstate__(PyObject state)
	{
		if ( state instanceof PyDictionary )
		{
			PyObject e = state.__getitem__( Py.newString( "pathEntries" ) );
			if ( e instanceof PyList )
			{
				PyList l = (PyList)e;
				entries = new ArrayList<AbstractSubjectPathEntry>();
				entries.addAll( (PyList)e );
			}
			else
			{
				throw Py.TypeError( "Elements list must be a list" );
			}
		}
		else
		{
			throw Py.TypeError( "State must be a dictionary" );
		}
	}

	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( getClass() ), new PyTuple(), __getstate__() );
	}
}
