//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class Expander extends ControlPres
{
	public static interface ExpanderListener
	{
		public void onExpander(ExpanderControl expander, boolean bExpanded);
	}

	public static class ExpanderControl extends Control
	{
		private LSBin element;
		private Pres expanded, contracted;
		private boolean currentState;
		private ExpanderListener listener;
		
		
		protected ExpanderControl(PresentationContext ctx, StyleValues style, LSBin element, Pres expanded, Pres contracted, boolean initialState, ExpanderListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.expanded = expanded;
			this.contracted = contracted;
			currentState = initialState;
			this.listener = listener;
		}
		
		
		
		
		@Override
		public LSElement getElement()
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
					element.setChild( expanded.present( ctx, style ).layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) ) );
				}
				else
				{
					element.setChild( contracted.present( ctx, style ).layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) ) );
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