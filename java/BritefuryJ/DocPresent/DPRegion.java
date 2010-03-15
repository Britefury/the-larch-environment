//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPRegion extends DPBox
{
	private EditHandler editHandler;

	
	
	
	public DPRegion()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPRegion(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}
	
	
	

	//
	//
	// EDIT HANDLER
	//
	//
	
	public void setEditHandler(EditHandler handler)
	{
		editHandler = handler;
	}
	
	public EditHandler getEditHandler()
	{
		return editHandler;
	}
	
	

	
	
	public DPRegion getRegion()
	{
		return this;
	}
}
