//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalView;

import BritefuryJ.LSpace.LSElement;

public class FragmentData
{
	private Object model;
	private LSElement element;
	
	
	public FragmentData(Object model, LSElement element)
	{
		this.model = model;
		this.element = element;
	}
	
	
	public Object getModel()
	{
		return model;
	}
	
	public LSElement getElement()
	{
		return element;
	}
}