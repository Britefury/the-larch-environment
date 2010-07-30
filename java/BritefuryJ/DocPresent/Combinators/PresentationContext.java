//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public class PresentationContext
{
	private GSymFragmentView fragment = null;
	private GSymAbstractPerspective perspective = null;
	private StyleValues style;
	private AttributeTable inheritedState = null;
	
	
	public PresentationContext()
	{
		style = StyleValues.instance;
	}
	
	public PresentationContext(StyleValues style)
	{
		this.style = style;
	}
	
	public PresentationContext(GSymFragmentView fragment, GSymAbstractPerspective perspective, StyleValues style, AttributeTable inheritedState)
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
	
	public StyleValues getStyle()
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
	
	
	public PresentationContext withStyle(StyleValues style)
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
	
	public PresentationContext withStyleSheet(StyleSheet2 styleSheet)
	{
		return withStyle( getStyle().withAttrs( styleSheet ) );
	}
	
	public PresentationContext withStyleSheetFromAttr(AttributeBase attr)
	{
		StyleValues style = getStyle();
		StyleSheet2 styleSheet = style.get( attr, StyleSheet2.class );
		return withStyle( style.withAttrs( styleSheet ) );
	}
}