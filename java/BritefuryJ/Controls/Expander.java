//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public abstract class Expander extends ControlPres
{
	public static interface ExpanderListener
	{
		public void onExpander(ExpanderControl expander, boolean bExpanded);
	}

	public static class ExpanderControl extends Control
	{
		private DPBin element;
		private Pres expanded, contracted;
		private boolean currentState;
		private ExpanderListener listener;
		
		
		protected ExpanderControl(PresentationContext ctx, StyleValues style, DPBin element, Pres expanded, Pres contracted, boolean initialState, ExpanderListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.expanded = expanded;
			this.contracted = contracted;
			currentState = initialState;
			this.listener = listener;
		}
		
		
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		
		public void toggle()
		{
			setState( !currentState );
		}
		
		
		public boolean getState()
		{
			return currentState;
		}
		
		public void setState(boolean state)
		{
			if ( state != currentState )
			{
				currentState = state;
				if ( currentState )
				{
					element.setChild( expanded.present( ctx, style ).layoutWrap() );
				}
				else
				{
					element.setChild( contracted.present( ctx, style ).layoutWrap() );
				}
				if ( listener != null )
				{
					listener.onExpander( this, currentState );
				}
			}
		}
	}

	
	
	protected boolean initialState;
	protected ExpanderListener listener;

	
	public Expander(boolean initialState, ExpanderListener listener)
	{
		super();

		this.initialState = initialState;
		this.listener = listener;
	}
}