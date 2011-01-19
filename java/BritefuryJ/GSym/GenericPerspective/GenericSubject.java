//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import BritefuryJ.Projection.Subject;

public class GenericSubject extends Subject
{
	private Object focus;
	private String title;
	
	
	public GenericSubject(Object focus, String title)
	{
		this.focus = focus;
		this.title = title;
	}
	
	public GenericSubject(Object focus)
	{
		this.focus = focus;
		this.title = focus.getClass().getName();
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
