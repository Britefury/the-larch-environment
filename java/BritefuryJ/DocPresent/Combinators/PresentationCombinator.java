//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class PresentationCombinator
{
	public static class PresentationContext
	{
		private GSymFragmentView fragment = null;
		private GSymAbstractPerspective perspective = null;
		private StyleSheetValues style;
		private AttributeTable inheritedState = null;
		
		
		public PresentationContext()
		{
			style = StyleSheetValues.instance;
		}
		
		public PresentationContext(GSymFragmentView fragment, GSymAbstractPerspective perspective, StyleSheetValues style, AttributeTable inheritedState)
		{
			this.fragment = fragment;
			this.perspective = perspective;
			this.style = style;
			this.inheritedState = inheritedState;
		}
		
		
		public GSymFragmentView getFragment()
		{
			return fragment;
		}
		
		public GSymAbstractPerspective getPerspective()
		{
			return perspective;
		}
		
		public StyleSheetValues getStyle()
		{
			return style;
		}
		
		public AttributeTable getInheritedState()
		{
			return inheritedState;
		}
		
		
		public PersistentState persistentState(Object key)
		{
			if ( fragment != null )
			{
				return fragment.persistentState( key );
			}
			else
			{
				return null;
			}
		}
		
		
		public PresentationContext withStyle(StyleSheetValues style)
		{
			return new PresentationContext( fragment, perspective, style, inheritedState );
		}
	}
	
	
	
	public DPElement present()
	{
		return present( new PresentationContext() );
	}

	public abstract DPElement present(PresentationContext ctx);
	
	
	
	protected static DPElement[] mapPresent(PresentationContext ctx, PresentationCombinator children[])
	{
		DPElement result[] = new DPElement[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = children[i].present( ctx );
		}
		return result;
	}
	
	
	
	protected static PresentationCombinator coerce(Object x)
	{
		if ( x == null )
		{
			return null;
		}
		else if ( x instanceof PresentationCombinator )
		{
			return (PresentationCombinator)x;
		}
		else if ( x instanceof DPElement )
		{
			return new PresentElement( (DPElement)x );
		}
		else
		{
			return new PresentObject( x );
		}
	}

	protected static PresentationCombinator[] mapCoerce(Object children[])
	{
		PresentationCombinator result[] = new PresentationCombinator[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = coerce( children[i] );
		}
		return result;
	}
	

	protected static PresentationCombinator[] mapCoerce(List<Object> children)
	{
		PresentationCombinator result[] = new PresentationCombinator[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			result[i] = coerce( children.get( i ) );
		}
		return result;
	}
}
