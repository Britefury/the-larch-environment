//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalView;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;

public class IncrementalViewInComponent
{
	private PresentationComponent component;
	private IncrementalView view;

	
	public IncrementalViewInComponent(Subject subject, ProjectiveBrowserContext browserContext, PersistentStateStore persistentState, PageController pageController)
	{
		component = new PresentationComponent();
		view = new IncrementalView( subject, browserContext, persistentState );

		LSElement column = new Column( new Object[] { view.getViewPres() } ).alignHExpand().alignVExpand().present();
		component.getRootElement().setChild( column );
		
		if ( pageController != null )
		{
			component.setPageController( pageController );
		}
	}
	
	public IncrementalViewInComponent(Subject subject, ProjectiveBrowserContext browserContext, PersistentStateStore persistentState)
	{
		this( subject, browserContext, persistentState, null );
	}
	
	public PresentationComponent getComponent()
	{
		return component;
	}

	public IncrementalView getView()
	{
		return view;
	}
}
