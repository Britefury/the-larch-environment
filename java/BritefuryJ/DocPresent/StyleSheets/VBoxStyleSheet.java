//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.DPVBox;

public class VBoxStyleSheet extends AbstractBoxStyleSheet
{
	public static VBoxStyleSheet defaultStyleSheet = new VBoxStyleSheet();
	
	
	protected DPVBox.Typesetting typesetting;
	protected DPVBox.Alignment alignment;


	public VBoxStyleSheet()
	{
		this( DPVBox.Typesetting.NONE, DPVBox.Alignment.CENTRE, 0.0, false, 0.0 );
	}
	
	public VBoxStyleSheet(DPVBox.Typesetting typesetting, DPVBox.Alignment alignment, double spacing, boolean bExpand, double padding)
	{
		super( spacing, bExpand, padding );
		
		this.typesetting = typesetting;
		this.alignment = alignment;
	}

	
	
	public DPVBox.Typesetting getTypesetting()
	{
		return typesetting;
	}

	public DPVBox.Alignment getAlignment()
	{
		return alignment;
	}
}
