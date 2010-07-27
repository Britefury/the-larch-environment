//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public class PresentationContext
{
	private GSymFragmentView fragment = null;
	private GSymAbstractPerspective perspective = null;
	private StyleSheetValues style;
	private AttributeTable inheritedState = null;
	
	
	public PresentationContext()
	{
		style = StyleSheetValues.instance;
	}
	
	public PresentationContext(StyleSheetValues style)
	{
		this.style = style;
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
		if ( style == this.style )
		{
			return this;
		}
		else
		{
			return new PresentationContext( fragment, perspective, style, inheritedState );
		}
	}
}