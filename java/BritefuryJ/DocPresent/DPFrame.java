//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class DPFrame extends DPBin
{
	private EditHandler editHandler;

	
	
	
	public DPFrame()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPFrame(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
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
	
	

	
	
	public DPFrame getFrame()
	{
		return this;
	}
}
