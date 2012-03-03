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

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpacer;
import BritefuryJ.LSpace.Interactor.RealiseElementInteractor;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class AnimatedDrawing extends Drawing
{
	public static interface Stepper
	{
		public void step(LSElement element, double time, double deltaTime);
	}
	
	
	private static class AbstractStepper implements RealiseElementInteractor
	{
		private LSElement element;
		private Stepper stepper;
		private double startTime, lastTime;
		protected boolean bRunning;
		
		private AbstractStepper(LSElement element, Stepper stepper)
		{
			this.element = element;
			this.stepper = stepper;
			this.startTime = (double)System.nanoTime() * 1.0e-9;
			this.lastTime = startTime;
			bRunning = false;
		}
		
		
		protected void step()
		{
			double time = ( (double)System.nanoTime() * 1.0e-9 ) - startTime;
			double deltaTime = time - lastTime;
			this.stepper.step( element, time, deltaTime );
			lastTime = time;
			element.queueFullRedraw();
		}


		@Override
		public void elementRealised(LSElement element)
		{
			bRunning = true;
		}


		@Override
		public void elementUnrealised(LSElement element)
		{
			bRunning = false;
		}
	}
	
	private static class ContinuousStepper extends AbstractStepper implements Runnable
	{
		private ContinuousStepper(LSElement element, Stepper stepper)
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
		public void elementRealised(LSElement element)
		{
			SwingUtilities.invokeLater( this );
		}


		@Override
		public void elementUnrealised(LSElement element)
		{
		}
	}
	
	private static class TimedStepper extends AbstractStepper implements ActionListener
	{
		private Timer timer;
		
		
		private TimedStepper(LSElement element, Stepper stepper, double interval)
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
		public void elementRealised(LSElement element)
		{
			timer.start();
		}


		@Override
		public void elementUnrealised(LSElement element)
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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSSpacer element = new LSSpacer( minWidth, minHeight );
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
