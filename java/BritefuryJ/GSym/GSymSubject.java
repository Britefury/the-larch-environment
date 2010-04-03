//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.CommandHistory.CommandHistory;

public class GSymSubject
{
	private GSymSubject enclosingSubject;
	private Object focus;
	private GSymPerspective perspective;
	private CommandHistory commandHistory;
	private String relativeLocation, locationSeparator;
	
	
	protected GSymSubject(GSymSubject enclosingSubject, Object focus, GSymPerspective perspective, CommandHistory commandHistory, String relativeLocation, String locationSeparator)
	{
		this.enclosingSubject = enclosingSubject;
		this.focus = focus;
		this.perspective = perspective;
		this.commandHistory = commandHistory;
		this.relativeLocation = relativeLocation;
		this.locationSeparator = locationSeparator;
	}
	
	
	public GSymSubject getEnclosingSubject()
	{
		return enclosingSubject;
	}
	
	public Object getFocus()
	{
		return focus;
	}
	
	public GSymPerspective getPerspective()
	{
		return perspective;
	}
	
	public CommandHistory getCommandHistory()
	{
		return commandHistory;
	}
	
	public String getRelativeLocation()
	{
		return relativeLocation;
	}
	
	public String getLocationSeparator()
	{
		return locationSeparator;
	}
	
	
	public String getLocation()
	{
		String enclosingLocation = enclosingSubject != null  ?  enclosingSubject.getLocationForEnclosedSubject()  :  "";
		return enclosingLocation + relativeLocation;
	}
	
	
	private String getLocationForEnclosedSubject()
	{
		String enclosingLocation = enclosingSubject != null  ?  enclosingSubject.getLocationForEnclosedSubject()  :  "";
		return enclosingLocation + relativeLocation + locationSeparator;
	}
	
	
	
	public GSymSubject withFocusAndPerspective(Object focus, GSymPerspective perspective)
	{
		return new GSymSubject( enclosingSubject, focus, perspective, commandHistory, relativeLocation, locationSeparator );
	}
	
	public GSymSubject withPerspective(GSymPerspective perspective)
	{
		return new GSymSubject( enclosingSubject, focus, perspective, commandHistory, relativeLocation, locationSeparator );
	}
	
	public GSymSubject enclosedSubject(Object focus, GSymPerspective perspective, String relativeLocation, String locationSeparator)
	{
		return new GSymSubject( this, focus, perspective, commandHistory, relativeLocation, locationSeparator );
	}
	
	public GSymSubject enclosedSubjectWithNewCommandHistory(Object focus, GSymPerspective perspective, CommandHistory commandHistory, String relativeLocation, String locationSeparator)
	{
		return new GSymSubject( this, focus, perspective, commandHistory, relativeLocation, locationSeparator );
	}
	
	
	
	public static GSymSubject rootSubject(Object focus, GSymPerspective perspective, CommandHistory commandHistory, String relativeLocation, String locationSeparator)
	{
		return new GSymSubject( null, focus, perspective, commandHistory, relativeLocation, locationSeparator );
	}
	
}
