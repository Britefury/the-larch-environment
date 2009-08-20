//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;

public class VBoxStyleSheet extends AbstractBoxStyleSheet
{
	public static VBoxStyleSheet defaultStyleSheet = new VBoxStyleSheet();
	
	
	protected VTypesetting typesetting;
	protected HAlignment alignment;


	public VBoxStyleSheet()
	{
		this( VTypesetting.NONE, HAlignment.EXPAND, 0.0, false, 0.0 );
	}
	
	public VBoxStyleSheet(VTypesetting typesetting, HAlignment alignment, double spacing, boolean bExpand, double padding)
	{
		super( spacing, bExpand, padding );
		
		this.typesetting = typesetting;
		this.alignment = alignment;
	}

	
	
	public VTypesetting getTypesetting()
	{
		return typesetting;
	}

	public HAlignment getAlignment()
	{
		return alignment;
	}
}
