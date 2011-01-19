//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Visual;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSpacer;
import BritefuryJ.DocPresent.Interactor.RealiseElementInteractor;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class AnimatedDrawing extends Drawing
{
	public static interface Stepper
	{
		public void step(DPElement element, double time);
	}
	
	
	private static class AbstractStepper implements RealiseElementInteractor
	{
		private DPElement element;
		private Stepper stepper;
		private double startTime;
		protected boolean bRunning;
		
		private AbstractStepper(DPElement element, Stepper stepper)
		{
			this.element = element;
			this.stepper = stepper;
			this.startTime = (double)System.nanoTime() * 1.0e-9;
			bRunning = false;
		}
		
		
		protected void step()
		{
			double time = ( (double)System.nanoTime() * 1.0e-9 ) - startTime;
			this.stepper.step( element, time );
			element.queueFullRedraw();
		}


		@Override
		public void elementRealised(DPElement element)
		{
			bRunning = true;
		}


		@Override
		public void elementUnrealised(DPElement element)
		{
			bRunning = false;
		}
	}
	
	private static class ContinuousStepper extends AbstractStepper implements Runnable
	{
		private ContinuousStepper(DPElement element, Stepper stepper)
		{
			super( element, stepper );
		}
		
		
		@Override
		public void run()
		{
			step();
			if ( bRunning )
			{
				SwingUtilities.invokeLater( this );
			}
		}


		@Override
		public void elementRealised(DPElement element)
		{
			SwingUtilities.invokeLater( this );
		}


		@Override
		public void elementUnrealised(DPElement element)
		{
		}
	}
	
	private static class TimedStepper extends AbstractStepper implements ActionListener
	{
		private Timer timer;
		
		
		private TimedStepper(DPElement element, Stepper stepper, double interval)
		{
			super( element, stepper );
			int delay = (int)( interval * 1000.0 + 0.5 );
			timer = new Timer( delay, this );
			timer.setRepeats( true );
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			step();
		}


		@Override
		public void elementRealised(DPElement element)
		{
			timer.start();
		}


		@Override
		public void elementUnrealised(DPElement element)
		{
			timer.stop();
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
		
		AbstractStepper s;
		if ( interval <= 0.0 )
		{
			s = new ContinuousStepper( element, stepper );
		}
		else
		{
			s = new TimedStepper( element, stepper, interval );
		}
		element.addElementInteractor( s );
		
		return element;
	}
}
