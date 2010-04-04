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
	private AttributeTable subjectContext;

	
	
	public GSymSubject(Object focus, GSymPerspective perspective, AttributeTable subjectContext)
	{
		this.focus = focus;
		this.perspective = perspective;
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
	
	public AttributeTable getSubjectContext()
	{
		return subjectContext;
	}
	
	
	
	public GSymSubject withFocusAndPerspective(Object focus, GSymPerspective perspective)
	{
		return new GSymSubject( focus, perspective, subjectContext );
	}
}
