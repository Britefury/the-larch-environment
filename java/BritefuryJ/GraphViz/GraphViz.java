//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GraphViz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import BritefuryJ.Pres.Primitive.Image;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class GraphViz
{
	private static ExecutorService _threadPool;
	
	
	
	public static void shutdown()
	{
		if ( _threadPool != null )
		{
			_threadPool.shutdown();
		}
	}
	
	public static void shutdownNow()
	{
		if ( _threadPool != null )
		{
			_threadPool.shutdownNow();
		}
	}
	
	
	public static Future<Image> dot(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getDotPath();
			
			return graphViz( path, src );
		}
		else
		{
			return null;
		}
	}
	
	
	public static Future<Image> neato(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getNeatoPath();
			
			return graphViz( path, src );
		}
		else
		{
			return null;
		}
	}
	
	
	public static Future<Image> smyrna(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getSmyrnaPath();
			
			return graphViz( path, src );
		}
		else
		{
			return null;
		}
	}
	
	
	public static Future<Image> lefty(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getLeftyPath();
			
			return graphViz( path, src );
		}
		else
		{
			return null;
		}
	}
	
	
	public static Future<Image> dotty(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getDottyPath();
			
			return graphViz( path, src );
		}
		else
		{
			return null;
		}
	}
	
	
	
	private static Future<Image> graphViz(final String executablePath, final String src)
	{
		Callable<Image> task = new Callable<Image>()
		{
			public Image call()
			{
				Process proc;
				try
				{
					proc = Runtime.getRuntime().exec( new String[] { executablePath, "-Tsvg" } );
				}
				catch (IOException e)
				{
					proc = null;
				}
				
				if ( proc != null )
				{
					OutputStream outStream = proc.getOutputStream();
					PrintWriter writer = new PrintWriter( outStream );
					writer.print( src );
					writer.flush();
					writer.close();
					
					InputStream inStream = proc.getInputStream();
					
					SVGUniverse universe = SVGCache.getSVGUniverse();
					SVGDiagram diagram = null;
					synchronized ( universe )
					{
						URI diagramURI;
						try
						{
							diagramURI = universe.loadSVG( inStream, "dot_output" );
						}
						catch (IOException e)
						{
							diagramURI = null;
						}
						
						if ( diagramURI != null )
						{
							diagram = universe.getDiagram( diagramURI );
						}
					}
					
					
					return new Image( diagram );
				}
				
				return null;
			}
		};
		
		
		return spawn( task );
	}
	
	
	

	private static <V> Future<V> spawn(Callable<V> task)
	{
		return getThreadPool().submit( task );
	}
	
	private static ExecutorService getThreadPool()
	{
		if ( _threadPool == null )
		{
			_threadPool = Executors.newSingleThreadScheduledExecutor();
		}
		return _threadPool;
	}
}
