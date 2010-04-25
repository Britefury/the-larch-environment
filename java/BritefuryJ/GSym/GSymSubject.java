//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;

public class GSymSubject
{
	private Object focus;
	private GSymPerspective perspective;
	private String title;
	private AttributeTable subjectContext;

	
	
	public GSymSubject(Object focus, GSymPerspective perspective, String title, AttributeTable subjectContext)
	{
		this.focus = focus;
		this.perspective = perspective;
		this.title = title;
		this.subjectContext = subjectContext;
	}
	
	
	public Object getFocus()
	{
		return focus;
	}
	
	public GSymPerspective getPerspective()
	{
		return perspective;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public AttributeTable getSubjectContext()
	{
		return subjectContext;
	}
	
	
	
	public GSymSubject withTitle(String title)
	{
		return new GSymSubject( focus, perspective, title, subjectContext );
	}
	
	public GSymSubject withPerspective(GSymPerspective perspective)
	{
		return new GSymSubject( focus, perspective, title, subjectContext );
	}
	
	public GSymSubject withPerspectiveAndTitle(GSymPerspective perspective, String title)
	{
		return new GSymSubject( focus, perspective, title, subjectContext );
	}
	
	public GSymSubject withFocusAndPerspective(Object focus, GSymPerspective perspective)
	{
		return new GSymSubject( focus, perspective, title, subjectContext );
	}
	
	public GSymSubject withFocusPerspectiveAndTitle(Object focus, GSymPerspective perspective, String title)
	{
		return new GSymSubject( focus, perspective, title, subjectContext );
	}
	
	
	public String toString()
	{
		return "GSymSubject( focus=" + focus + ", perspective=" + perspective + ", title=" + title + ", subjectContext=" + subjectContext + " )";
	}
}