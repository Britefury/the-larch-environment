//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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