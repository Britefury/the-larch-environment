//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.CommandHistory.CommandHistory;

public class GSymSubject
{
	private Object focus;
	private GSymAbstractPerspective perspective;
	private String title;
	private AttributeTable subjectContext;
	private CommandHistory commandHistory;

	
	
	public GSymSubject(Object focus, GSymAbstractPerspective perspective, String title, AttributeTable subjectContext, CommandHistory commandHistory)
	{
		this.focus = focus;
		this.perspective = perspective;
		this.title = title;
		this.subjectContext = subjectContext;
		this.commandHistory = commandHistory;
	}
	
	
	public Object getFocus()
	{
		return focus;
	}
	
	public GSymAbstractPerspective getPerspective()
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
	
	public CommandHistory getCommandHistory()
	{
		return commandHistory;
	}
	
	
	
	public GSymSubject withFocus(Object focus)
	{
		return new GSymSubject( focus, perspective, title, subjectContext, commandHistory );
	}
	
	public GSymSubject withPerspective(GSymAbstractPerspective perspective)
	{
		return new GSymSubject( focus, perspective, title, subjectContext, commandHistory );
	}
	
	public GSymSubject withTitle(String title)
	{
		return new GSymSubject( focus, perspective, title, subjectContext, commandHistory );
	}
	
	public GSymSubject withSubjectContext(AttributeTable subjectContext)
	{
		return new GSymSubject( focus, perspective, title, subjectContext, commandHistory );
	}
	
	public GSymSubject withCommandHistory(CommandHistory commandHistory)
	{
		return new GSymSubject( focus, perspective, title, subjectContext, commandHistory );
	}
	
	
	public String toString()
	{
		return "GSymSubject( focus=" + focus + ", perspective=" + perspective + ", title=" + title + ", subjectContext=" + subjectContext + ", commandHistory=" + commandHistory + " )";
	}
}
