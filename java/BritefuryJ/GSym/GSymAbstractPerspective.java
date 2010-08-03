//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;


public abstract class GSymAbstractPerspective
{
	private class ProjectionPresentable implements Presentable
	{
		private Object model;
		private StyleSheet2 styleSheet;
		
		
		public ProjectionPresentable(Object x, StyleSheet2 styleSheet)
		{
			this.model = x;
			this.styleSheet = styleSheet;
		}

		
		@Override
		public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return styleSheet.applyTo( new InnerFragment( model ) );
		}
	}
	
	
	public abstract Pres present(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState);
	
	public StyleSheet2 getStyleSheet()
	{
		return StyleSheet2.instance;
	}
	
	public abstract SimpleAttributeTable getInitialInheritedState();
	public abstract EditHandler getEditHandler();

	public abstract GSymSubject resolveRelativeLocation(GSymSubject enclosingSubject, Location.TokenIterator locationIterator);
	
	
	public Presentable project(Object x)
	{
		return new ProjectionPresentable( x, getStyleSheet() );
	}

	public Presentable project(Object x, StyleSheet2 styleSheet)
	{
		return new ProjectionPresentable( x, styleSheet );
	}
}
