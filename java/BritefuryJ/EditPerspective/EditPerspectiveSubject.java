//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.EditPerspective;

import BritefuryJ.Projection.Subject;

public class EditPerspectiveSubject extends Subject
{
	private Object focus;
	private String title;
	
	
	public EditPerspectiveSubject(Object focus, String title)
	{
		this.focus = focus;
		this.title = title;
	}
	
	public EditPerspectiveSubject(Object focus)
	{
		this.focus = focus;
		this.title = focus != null  ?  focus.getClass().getName()  :  "<null>";
	}
	

	@Override
	public Object getFocus()
	{
		return focus;
	}

	@Override
	public String getTitle()
	{
		return title;
	}
}
