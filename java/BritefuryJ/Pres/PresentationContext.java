//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.PersistentState.PersistentState;
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