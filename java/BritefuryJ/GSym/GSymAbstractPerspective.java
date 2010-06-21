//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;


public abstract class GSymAbstractPerspective
{
	private class ProjectionPresentable implements Presentable
	{
		private Object x;
		private StyleSheet styleSheet;
		private AttributeTable inheritedState;
		
		
		public ProjectionPresentable(Object x, StyleSheet styleSheet, AttributeTable inheritedState)
		{
			this.x = x;
			this.styleSheet = styleSheet;
			this.inheritedState = inheritedState;
		}

		
		@Override
		public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
		{
			return fragment.presentFragmentWithPerspectiveAndStyleSheet( x, GSymAbstractPerspective.this, this.styleSheet, this.inheritedState );
		}
	}
	
	
	public abstract DPElement present(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable inheritedState);
	public abstract StyleSheet getStyleSheet();
	public abstract AttributeTable getInitialInheritedState();
	public abstract EditHandler getEditHandler();

	public abstract GSymSubject resolveRelativeLocation(GSymSubject enclosingSubject, Location.TokenIterator locationIterator);
	
	
	public Presentable project(Object x)
	{
		return new ProjectionPresentable( x, getStyleSheet(), getInitialInheritedState() );
	}

	public Presentable project(Object x, AttributeTable inheritedState)
	{
		return new ProjectionPresentable( x, getStyleSheet(), inheritedState );
	}

	public Presentable project(Object x, StyleSheet styleSheet)
	{
		return new ProjectionPresentable( x, styleSheet, getInitialInheritedState() );
	}

	public Presentable project(Object x, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		return new ProjectionPresentable( x, styleSheet, inheritedState );
	}
}
