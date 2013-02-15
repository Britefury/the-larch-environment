//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Projection;

import java.util.ArrayList;
import java.util.List;

import org.python.core.*;

import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Command.BoundCommandSet;

public abstract class Subject
{
	private Subject enclosingSubject;
	private SubjectPath path;
	
	
	
	public Subject(Subject enclosingSubject, SubjectPath path)
	{
		this.enclosingSubject = enclosingSubject;
		this.path = path;
	}
	
	
	// Basics:
	// - enclosing subject
	// - focus (override this)
	// - perspective (override this if perspective other than the default perspective is needed)
	// - title (override this)
	// - change history (override this if change history available)
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
	
	
	
	//
	// Command list - override to add commands. Call super method.
	public void buildBoundCommandSetList(List<BoundCommandSet> boundCommandSets)
	{
		if ( enclosingSubject != null )
		{
			enclosingSubject.buildBoundCommandSetList( boundCommandSets );
		}
	}
	
	
	//
	// Fall back on attributes from enclosing subject if attribute not found
	//
	public PyObject __getattr__(PyString key)
	{
		if ( enclosingSubject != null )
		{
			try
			{
				return __builtin__.getattr( Py.java2py( enclosingSubject ), key );
			}
			catch (PyException e)
			{
				if ( e.match( Py.AttributeError ) )
				{
					throw Py.AttributeError( "Object of class '" + Py.java2py( this ).getType().getName() + "' has no attribute '" + key.asString() + "'" );
				}
				else
				{
					throw e;
				}
			}
		}
		else
		{
			throw Py.AttributeError( "Object of class '" + Py.java2py( this ).getType().getName() + "' has no attribute '" + key.asString() + "'" );
		}
	}
	
	
	//
	// Get the path
	//
	
	public SubjectPath path()
	{
		return path;
	}
	
	
	//
	// Trail - for the browser trail
	//
	
	// Link text
	public String getTrailLinkText()
	{
		return null;
	}
	
	// Return a link
	public SubjectTrailLink getTrailLink()
	{
		String text = getTrailLinkText();
		return text != null  ?  new SubjectTrailLink( text, this )  :  null;
	}
	
	// Get the trail
	public List<SubjectTrailLink> getTrail()
	{
		ArrayList<SubjectTrailLink> trail = new ArrayList<SubjectTrailLink>();
		buildTrail( trail );
		return trail;
	}
	
	private void buildTrail(List<SubjectTrailLink> trail)
	{
		if ( enclosingSubject != null )
		{
			enclosingSubject.buildTrail( trail );
		}
		SubjectTrailLink link = getTrailLink();
		if ( link != null )
		{
			trail.add( link );
		}
	}
}
