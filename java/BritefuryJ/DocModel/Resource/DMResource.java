//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel.Resource;

import java.io.Serializable;

public abstract class DMResource implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected String serialised;
	
	
	protected DMResource()
	{
	}
	
	protected DMResource(String serialised)
	{
		this.serialised = serialised;
	}
}
