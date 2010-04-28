//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.DefaultPerspective;

import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class DefaultPerspectiveStyleSheet extends StyleSheet
{
	public enum PresentationSize
	{
		FULL,
		ONELINE
	}
	
	
	
	public static final DefaultPerspectiveStyleSheet instance = new DefaultPerspectiveStyleSheet();

	
	
	public DefaultPerspectiveStyleSheet()
	{
		super();
		
		initAttr( "presentationSize", PresentationSize.FULL );
	}
	
	
	protected StyleSheet newInstance()
	{
		return new DefaultPerspectiveStyleSheet();
	}
	
	
	
	public DefaultPerspectiveStyleSheet withPresentationSize(PresentationSize size)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "presentationSize", size );
	}
	
	
	
	public PresentationSize getPresentationSize()
	{
		return getNonNull( "presentationSize", PresentationSize.class, PresentationSize.FULL );
	}
}
