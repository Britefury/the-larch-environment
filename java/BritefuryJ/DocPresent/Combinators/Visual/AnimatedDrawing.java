//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Visual;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSpacer;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class AnimatedDrawing extends Drawing
{
	public static interface Stepper
	{
		public void step(DPElement element, double time);
	}
	
	
	private static class AbstractStepper
	{
		private DPElement element;
		private Stepper stepper;
		private double startTime;
		
		private AbstractStepper(DPElement element, Stepper stepper)
		{
			this.element = element;
			this.stepper = stepper;
			this.startTime = (double)System.nanoTime() * 1.0e-9;
		}
		
		
		protected void step()
		{
			double time = ( (double)System.nanoTime() * 1.0e-9 ) - startTime;
			this.stepper.step( element, time );
			element.queueFullRedraw();
		}
	}
	
	private static class ContinuousStepper extends AbstractStepper implements Runnable
	{
		private ContinuousStepper(DPElement element, Stepper stepper)
		{
			super( element, stepper );
			SwingUtilities.invokeLater( this );
		}
		
		
		@Override
		public void run()
		{
			step();
			SwingUtilities.invokeLater( this );
		}
	}
	
	private static class TimedStepper extends AbstractStepper implements ActionListener
	{
		private TimedStepper(DPElement element, Stepper stepper, double interval)
		{
			super( element, stepper );
			int delay = (int)( interval * 1000.0 + 0.5 );
			Timer timer = new Timer( delay, this );
			timer.setRepeats( true );
			timer.start();
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			step();
		}
	}
	

	private Stepper stepper;
	private double interval;
	
	public AnimatedDrawing(double minWidth, double minHeight, Painter painter, Stepper stepper)
	{
		this( minWidth, minHeight, painter, -1.0, stepper );
	}

	public AnimatedDrawing(double minWidth, double minHeight, Painter painter, double interval, Stepper stepper)
	{
		super( minWidth, minHeight, painter );
		this.stepper = stepper;
		this.interval = interval;
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPSpacer element = new DPSpacer( minWidth, minHeight );
		element.addPainter( new DrawingPainter( painter ) );
		
		if ( interval <= 0.0 )
		{
			new ContinuousStepper( element, stepper );
		}
		else
		{
			new TimedStepper( element, stepper, interval );
		}
		
		return element;
	}
}
