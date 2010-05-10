//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;


import java.awt.Color;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalMonitorListener;



public abstract class CellInterface implements IncrementalOwner, Presentable
{
	public abstract void addListener(IncrementalMonitorListener listener);
	public abstract void removeListener(IncrementalMonitorListener listener);
	
	
	
	public abstract Object getLiteralValue();
	public abstract void setLiteralValue(Object value);
	public abstract boolean isLiteral();
	
	public abstract Object getValue();
	
	
	
	@Override
	public DPElement present(GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement valueView = ctx.presentFragment( getValue(), styleSheet, inheritedState );
		
		return cellStyle.objectBox( getClass().getName(), valueView );
	}
	
	
	private static GenericPerspectiveStyleSheet cellStyle = GenericPerspectiveStyleSheet.instance.withObjectBorderPaint( new Color( 0.5f, 0.0f, 0.5f ) ).withObjectTitlePaint( new Color( 0.5f, 0.0f, 0.5f ) );
}
