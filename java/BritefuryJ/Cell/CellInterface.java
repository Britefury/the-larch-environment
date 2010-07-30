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
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.Incremental.IncrementalOwner;



public abstract class CellInterface implements IncrementalOwner, Presentable
{
	public abstract void addListener(IncrementalMonitorListener listener);
	public abstract void removeListener(IncrementalMonitorListener listener);
	
	
	
	public abstract Object getLiteralValue();
	public abstract void setLiteralValue(Object value);
	public abstract boolean isLiteral();
	
	public abstract Object getValue();
	
	
	
	@Override
	public Pres present(GSymFragmentView fragment, AttributeTable inheritedState)
	{
		DPElement valueView = fragment.presentFragment( getValue(), StyleSheet.instance, inheritedState );
		
		return cellStyle.applyTo( new ObjectBox( getClass().getName(), valueView ) );
	}
	
	
	private static StyleSheet2 cellStyle = StyleSheet2.instance.withAttr( GenericStyle.objectBorderPaint, new Color( 0.5f, 0.0f, 0.5f ) ).withAttr( GenericStyle.objectTitlePaint, new Color( 0.5f, 0.0f, 0.5f ) );
}
