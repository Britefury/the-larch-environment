//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;

public class Checkbox extends Control
{
	public static interface CheckboxListener
	{
		public void onCheckboxToggled(Checkbox checkbox, boolean state);
	}
	
	
	private DPElement element, unchecked, checked;
	private DPBorder check;
	private CheckboxListener listener;
	private boolean state;

	
	protected Checkbox(DPElement element, DPBorder check, DPElement unchecked, DPElement checked, boolean state, CheckboxListener listener)
	{
		this.element = element;
		this.check = check;
		this.unchecked = unchecked;
		this.checked = checked;
		this.listener = listener;
		this.state = state;
	}
	
	
	
	@Override
	public DPElement getElement()
	{
		return element;
	}
	
	
	public boolean getState()
	{
		return state;
	}
	
	public void setState(boolean state)
	{
		if ( state != this.state )
		{
			this.state = state;
			
			check.setChild( state  ?  checked  :  unchecked );
			
			listener.onCheckboxToggled( this, state );
		}
	}

	
	
	public void toggle()
	{
		setState( !state );
	}
}
