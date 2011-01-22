//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Projection.AbstractPerspective;

public class PresentationContext
{
	public static final PresentationContext defaultCtx = new PresentationContext();
	
	
	private FragmentView fragment = null;
	private AbstractPerspective perspective = null;
	private SimpleAttributeTable inheritedState = null;
	
	
	private PresentationContext()
	{
	}
	
	public PresentationContext(FragmentView fragment, AbstractPerspective perspective, SimpleAttributeTable inheritedState)
	{
		this.fragment = fragment;
		this.perspective = perspective;
		this.inheritedState = inheritedState;
	}
	
	
	public FragmentView getFragment()
	{
		return fragment;
	}
	
	public AbstractPerspective getPerspective()
	{
		return perspective;
	}
	
	public SimpleAttributeTable getInheritedState()
	{
		return inheritedState;
	}
	
	public SimpleAttributeTable getSubjectContext()
	{
		return fragment != null  ?  fragment.getSubjectContext()  :  null;
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