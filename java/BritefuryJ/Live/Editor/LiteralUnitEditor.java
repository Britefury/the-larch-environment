//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Live.Editor;

import java.awt.Color;
import java.util.WeakHashMap;

import javax.swing.SwingUtilities;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class LiteralUnitEditor implements Presentable, IncrementalMonitorListener
{
	private static final StyleSheet errorStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.8f, 0.0f, 0.0f ) ) );
	
	protected abstract class Editor
	{
		private boolean bSettingCellValue = false;
		private LiveValue presCell = new LiveValue();
		private Pres pres = DefaultPerspective.instance.applyTo( presCell );

		
		
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
			setPres( errorStyle.applyTo( new Label( "<" + message + ">" ) ) );
		}
		
		
		protected void onUnitChanged()
		{
			if ( !bSettingCellValue )
			{
				Runnable run = new Runnable()
				{
					public void run()
					{
						refreshEditor();
					}
				};
				
				SwingUtilities.invokeLater( run );
			}
		}
		
		protected void setUnitValue(Object value)
		{
			bSettingCellValue = true;
			cell.setLiteralValue( value );
			bSettingCellValue = false;
		}
	}


	protected LiveValue cell;
	protected WeakHashMap<Editor, Object> editors = new WeakHashMap<Editor, Object>();
	
	
	public LiteralUnitEditor(LiveValue cell)
	{
		this.cell = cell;
		this.cell.addListener( this );
	}
	
	
	protected abstract Editor createEditor();
	

	protected <V> V getUnitValue(Class<V> valueClass)
	{
		Object v = cell.getStaticValue();
		
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
	
	protected <V> V getUnitValueNonNull(Class<V> valueClass, V defaultValue)
	{
		Object v = cell.getStaticValue();
		
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
	

	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return createEditor().getPres();
	}


	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		for (Editor editor: editors.keySet())
		{
			editor.onUnitChanged();
		}
	}
}
