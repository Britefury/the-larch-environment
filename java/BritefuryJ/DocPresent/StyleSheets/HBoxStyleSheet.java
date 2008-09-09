//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

import BritefuryJ.DocPresent.DPHBox;

public class HBoxStyleSheet extends AbstractBoxStyleSheet
{
	public static HBoxStyleSheet defaultStyleSheet = new HBoxStyleSheet();
	
	
	protected DPHBox.Alignment alignment;


	public HBoxStyleSheet()
	{
		this( DPHBox.Alignment.CENTRE, 0.0, false, 0.0, null );
	}
	
	public HBoxStyleSheet(Color backgroundColour)
	{
		this( DPHBox.Alignment.CENTRE, 0.0, false, 0.0, backgroundColour );
	}
	
	public HBoxStyleSheet(DPHBox.Alignment alignment, double spacing, boolean bExpand, double padding)
	{
		this( alignment, spacing, bExpand, padding, null );
	}
	
	public HBoxStyleSheet(DPHBox.Alignment alignment, double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, padding, backgroundColour );
		
		this.alignment = alignment;
	}

	
	public DPHBox.Alignment getAlignment()
	{
		return alignment;
	}
}
