//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Live;


import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementValueFunction;
import BritefuryJ.LSpace.StreamValue.StreamValueBuilder;
import BritefuryJ.Pres.ApplyPerspective;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.ObjectPres.ErrorBox;
import BritefuryJ.StyleSheet.StyleValues;



public abstract class LiveInterface extends CompositePres
{
	protected static class ElementValueFn implements ElementValueFunction
	{
		private LiveInterface unit;
		
		public ElementValueFn(LiveInterface unit)
		{
			this.unit = unit;
		}
		
		public Object computeElementValue(LSElement element)
		{
			return unit.getStaticValue();
		}

		@Override
		public void addStreamValuePrefixToStream(StreamValueBuilder builder, LSElement element)
		{
		}

		@Override
		public void addStreamValueSuffixToStream(StreamValueBuilder builder, LSElement element)
		{
		}
	}
	
	public static class ValuePres extends Pres
	{
		private LiveInterface unit;
		
		
		private ValuePres(LiveInterface unit)
		{
			this.unit = unit;
		}
		

		@Override
		public LSElement present(PresentationContext ctx, StyleValues style)
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
	
	
	public Object __call__()
	{
		return getValue();
	}
	
	
	
	@Override
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		return new InnerFragment( new ValuePres( this ) );
	}
	
	
	
	public Pres valuePresInFragment()
	{
		System.err.println( "DEPRACATION WARNING: LiveInterface.valuePresInFragment method is DEPRACATED" );
		return new InnerFragment( new ValuePres( this ) );
	}
}
