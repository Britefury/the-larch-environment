//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.IncrementalView.FragmentData;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.ObjectPres.ObjectBorder;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectDropBox extends ControlPres
{
	public interface ObjectDropBoxListener
	{
		public void onObjectDropBoxDrop(ObjectDropBoxControl control, Object value);
	}
	
	
	private static class DropListener implements ObjectDndHandler.DropFn
	{
		private ObjectDropBoxControl control;
		
		
		public boolean acceptDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
		{
			control.listener.onObjectDropBoxDrop( control, ((FragmentData)data).getModel() );
			return true;
		}
	}

	
	private static class ObjectDropBoxControl extends Control
	{
		private LSElement element;
		private ObjectDropBoxListener listener;
		
		
		public ObjectDropBoxControl(PresentationContext ctx, StyleValues style, LSElement element, ObjectDropBoxListener listener)
		{
			super( ctx, style );
			
			this.element = element;
			this.listener = listener;
		}

		@Override
		public LSElement getElement()
		{
			return element;
		}
	}
	
	
	private static class CommitListener implements ObjectDropBoxListener
	{
		private LiveInterface live;
		
		public CommitListener(LiveInterface live)
		{
			this.live = live;
		}
		
		@Override
		public void onObjectDropBoxDrop(ObjectDropBoxControl control, Object value)
		{
			this.live.setLiteralValue( value );
		}
	}
	
	
	
	private LiveSource valueSource;
	private ObjectDropBoxListener listener;
	
	
	private ObjectDropBox(LiveSource valueSource, ObjectDropBoxListener listener)
	{
		this.valueSource = valueSource;
		this.listener = listener;
	}
	
	
	public ObjectDropBox(Object initialValue, ObjectDropBoxListener listener)
	{
		this( new LiveSourceValue( initialValue ), listener );
	}
	
	public ObjectDropBox(LiveInterface value, ObjectDropBoxListener listener)
	{
		this( new LiveSourceRef( value ), listener );
	}
	
	public ObjectDropBox(LiveInterface value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ) );
	}
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		final LiveInterface value = valueSource.getLive();
		
		LiveFunction.Function fn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				Object val = value.getValue();
				if ( PrimitivePresenter.isPrimitive( val ) )
				{
					return DefaultPerspective.instance.applyTo( val );
				}
				else
				{
					return new Label( val.getClass().getName() ).withStyleSheetFromAttr( Controls.objectDropBoxClassNameStyle );
				}
			}
		};
		
		LiveFunction dropContents = new LiveFunction( fn );
		
		DropListener dropListener = new DropListener();
		
		Pres contents = new Row( new Object[] { new Label( "Drop: " ), dropContents } );
		Pres p = new ObjectBorder( contents ).withDropDest( FragmentData.class, dropListener );
		
		LSElement element = p.present( ctx, style );
		
		ObjectDropBoxControl control = new ObjectDropBoxControl( ctx, style, element, listener );
		
		dropListener.control = control;
		
		return control;
	}
}
