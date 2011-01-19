//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Presenters;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.Timer;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Pres.ErrorBox;
import BritefuryJ.DefaultPerspective.Pres.ObjectBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentersConcurrency extends ObjectPresenterRegistry
{
	private static final int WATCH_TIMER_DELAY = 500;
	private static final int WATCH_MAX_SCAN_SIZE = 512;
	private static final int WATCH_MAX_NUM_REFRESHED = 128;
	
	
	
	private static Timer watchTimer;
	private static HashSet<FragmentView> watches = new HashSet<FragmentView>();
	private static LinkedList<FragmentView> watchList = new LinkedList<FragmentView>();
	
	
	public PresentersConcurrency()
	{
		registerJavaObjectPresenter( Future.class,  presenter_Future );
	}

	
	
	private static void initWatchTimer()
	{
		if ( watchTimer == null )
		{
			ActionListener watchAction = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scanWatchList();
				}
			};
			
			watchTimer = new Timer( WATCH_TIMER_DELAY, watchAction );
			watchTimer.setInitialDelay( WATCH_TIMER_DELAY );
		}
	}
	
	private static void scanWatchList()
	{
		if ( watchList.isEmpty() )
		{
			stopWatchTimer();
		}
		else
		{
			int numScans = 0, numRefreshed = 0;
			
			LinkedList<FragmentView> unchanged = new LinkedList<FragmentView>();
			
			while (!watchList.isEmpty())
			{
				if ( numScans >= WATCH_MAX_SCAN_SIZE  ||  numRefreshed > WATCH_MAX_NUM_REFRESHED )
				{
					break;
				}
				
				
				FragmentView fragment = watchList.remove();
	
				@SuppressWarnings("unchecked")
				Future<Object> future = (Future<Object>)fragment.getModel();
				if ( future.isDone() )
				{
					// The future is now ready - queue a refresh, and remove from the 'watches' set
					fragment.queueRefresh();
					watches.remove( fragment );
					numRefreshed++;
				}
				else
				{
					// The future is still not ready - add to the 'unchanged' list, so that it gets added back again to the queu
					unchanged.add( fragment );
				}
				numScans++;
			}
			
			watchList.addAll( unchanged );
		}
	}
	
	private static void startWatchTimer()
	{
		initWatchTimer();
		watchTimer.start();
	}
	
	private static void stopWatchTimer()
	{
		watchTimer.stop();
	}
	
	private static void watchFutureFragment(FragmentView fragment)
	{
		if ( watchList.isEmpty() )
		{
			watchList.add( fragment );
			startWatchTimer();
		}
		else
		{
			if ( !watches.contains( fragment ) )
			{
				watches.add( fragment );
				watchList.add( fragment );
			}
		}
	}


	public static final ObjectPresenter presenter_Future = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			@SuppressWarnings("unchecked")
			Future<Object> f = (Future<Object>)x;
			
			if ( f.isCancelled() )
			{
				return new ObjectBorder( cancelledStyle.applyTo( new Label( "<Cancelled>" ) ) );
			}
			else if ( !f.isDone() )
			{
				watchFutureFragment( fragment );
				return new ObjectBorder( waitingStyle.applyTo( new Label( "<Awaiting result...>" ) ) );
			}
			else
			{
				try
				{
					return new InnerFragment( f.get() );
				}
				catch (InterruptedException e)
				{
					return new ErrorBox( "Future: InterruptedException", new InnerFragment( e ) );
				}
				catch (ExecutionException e)
				{
					return new ErrorBox( "Future: ExecutionException", new InnerFragment( e ) );
				}
			}
		}
	};
	
	
	private static final StyleSheet cancelledStyle = StyleSheet.instance.withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) );
	private static final StyleSheet waitingStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.3f, 0.3f, 0.3f ) );
}
