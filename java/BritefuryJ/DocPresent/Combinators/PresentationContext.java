//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.IncrementalView.FragmentView;

public class PresentationContext
{
	private FragmentView fragment = null;
	private GSymAbstractPerspective perspective = null;
	private SimpleAttributeTable inheritedState = null;
	
	
	public PresentationContext()
	{
	}
	
	public PresentationContext(FragmentView fragment, GSymAbstractPerspective perspective, SimpleAttributeTable inheritedState)
	{
		this.fragment = fragment;
		this.perspective = perspective;
		this.inheritedState = inheritedState;
	}
	
	
	public FragmentView getFragment()
	{
		return fragment;
	}
	
	public GSymAbstractPerspective getPerspective()
	{
		return perspective;
	}
	
	public SimpleAttributeTable getInheritedState()
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
}