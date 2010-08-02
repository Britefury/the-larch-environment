//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CellEditor;

import java.awt.Color;
import java.util.WeakHashMap;

import javax.swing.SwingUtilities;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;

public abstract class LiteralCellEditor implements Presentable, IncrementalMonitorListener
{
	private static final StyleSheet2 errorStyle = StyleSheet2.instance.withAttr( Primitive.foreground, new Color( 0.8f, 0.0f, 0.0f ) );
	
	protected abstract class Editor
	{
		private boolean bSettingCellValue = false;
		private LiteralCell presCell = new LiteralCell();
		private Pres pres = presCell.genericPerspectiveValuePresInFragment();

		
		
		protected abstract void refreshEditor();
		
		
		protected Pres getPres()
		{
			return pres;
		}
		
		
		protected void setPres(Pres p)
		{
			presCell.setLiteralValue( p );
		}
		
		protected void error(String message)
		{
			setPres( errorStyle.applyTo( new StaticText( "<" + message + ">" ) ) );
		}
		
		
		protected void onCellChanged()
		{
			if ( !bSettingCellValue )
			{
				Runnable run = new Runnable()
				{
					@Override
					public void run()
					{
						refreshEditor();
					}
				};
				
				SwingUtilities.invokeLater( run );
			}
		}
		
		protected void setCellValue(Object value)
		{
			bSettingCellValue = true;
			cell.setLiteralValue( value );
			bSettingCellValue = false;
		}
	};
	
	
	protected LiteralCell cell;
	protected WeakHashMap<Editor, Object> editors = new WeakHashMap<Editor, Object>();
	
	
	public LiteralCellEditor(LiteralCell cell)
	{
		this.cell = cell;
		this.cell.addListener( this );
	}
	
	
	protected abstract Editor createEditor();
	

	protected <V extends Object> V getCellValue(Class<V> valueClass)
	{
		Object v = cell.getLiteralValue();
		
		if ( v == null )
		{
			return null;
		}
		
		V typedV = null;
		try
		{
			typedV = valueClass.cast( v );
		}
		catch (ClassCastException e)
		{
			return null;
		}
		
		return typedV;
	}
	
	protected <V extends Object> V getCellValueNonNull(Class<V> valueClass, V defaultValue)
	{
		Object v = cell.getLiteralValue();
		
		if ( v == null )
		{
			return defaultValue;
		}
		
		V typedV = null;
		try
		{
			typedV = valueClass.cast( v );
		}
		catch (ClassCastException e)
		{
			return defaultValue;
		}
		
		return typedV;
	}
	

	
	@Override
	public Pres present(GSymFragmentView fragment, AttributeTable inheritedState)
	{
		return createEditor().getPres();
	}


	@Override
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		for (Editor editor: editors.keySet())
		{
			editor.onCellChanged();
		}
	}
}
