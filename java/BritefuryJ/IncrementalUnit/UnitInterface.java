//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.IncrementalUnit;


import java.awt.Color;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementValueFunction;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.ApplyPerspective;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.ObjectPres.ErrorBox;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.ObjectPres.ObjectPresStyle;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;



public abstract class UnitInterface implements Presentable
{
	protected static class ElementValueFn implements ElementValueFunction
	{
		private UnitInterface unit;
		
		public ElementValueFn(UnitInterface unit)
		{
			this.unit = unit;
		}
		
		public Object computeElementValue(DPElement element)
		{
			return unit.getStaticValue();
		}

		@Override
		public void addStreamValuePrefixToStream(StreamValueBuilder builder, DPElement element)
		{
		}

		@Override
		public void addStreamValueSuffixToStream(StreamValueBuilder builder, DPElement element)
		{
		}
	}
	
	public static class ValuePres extends Pres
	{
		private UnitInterface unit;
		
		
		private ValuePres(UnitInterface unit)
		{
			this.unit = unit;
		}
		

		@Override
		public DPElement present(PresentationContext ctx, StyleValues style)
		{
			Object value = null;
			try
			{
				value = unit.getValue();
			}
			catch (Throwable t)
			{
				Pres exceptionView = new ApplyPerspective( null, t );
				return new ErrorBox( "UnitInterface.ValuePres presentation error - exception during unit evaluation", exceptionView ).present( ctx, style );
			}

			if ( value != null )
			{
				return Pres.coerce( value ).present( ctx, style );
			}
			else
			{
				return PrimitivePresenter.presentNull().present( ctx, style );
			}
		}
	}
	
	
	public abstract void addListener(IncrementalMonitorListener listener);
	public abstract void removeListener(IncrementalMonitorListener listener);
	
	
	
	public abstract void setLiteralValue(Object value);
	
	public abstract Object getValue();
	public abstract Object getStaticValue();
	
	
	
	public ElementValueFunction elementValueFunction()
	{
		return new ElementValueFn( this );
	}
	
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return unitStyle.applyTo( new ObjectBox( getClass().getName(), new InnerFragment( getValue() ) ) );
	}
	
	
	
	public Pres valuePresInFragment()
	{
		return new InnerFragment( new ValuePres( this ) );
	}
	
	public Pres defaultPerspectiveValuePresInFragment()
	{
		return ApplyPerspective.defaultPerspective( new InnerFragment( new ValuePres( this ) ) );
	}


	private static StyleSheet unitStyle = StyleSheet.style( ObjectPresStyle.objectBorderPaint.as( new Color( 0.5f, 0.0f, 0.5f ) ), ObjectPresStyle.objectTitlePaint.as( new Color( 0.5f, 0.0f, 0.5f ) ) );
}
