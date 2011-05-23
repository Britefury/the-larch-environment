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

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ErrorBox;
import BritefuryJ.Pres.Primitive.Image;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class GraphVizBlocking
{
	public static Pres dot(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getDotPath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	public static Pres neato(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getNeatoPath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	public static Pres twopi(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getTwopiPath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	public static Pres circo(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getCircoPath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	public static Pres fdp(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getFdpPath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	public static Pres sfdp(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getSfdpPath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	public static Pres osage(final String src)
	{
		if ( Configuration.instance != null )
		{
			final String path = Configuration.instance.getOsagePath();
			
			return graphViz( path, src );
		}
		else
		{
			throw new GraphVizNotConfiguredException();
		}
	}
	
	
	
	private static Pres graphViz(final String executablePath, final String src)
	{
		Process proc;
		try
		{
			proc = Runtime.getRuntime().exec( new String[] { executablePath, "-Tsvg" } );
		}
		catch (IOException e)
		{
			return new ErrorBox( "GraphViz: Error while executing GraphViz tool", e );
		}
		
		
		OutputStream outStream = proc.getOutputStream();
		PrintWriter writer = new PrintWriter( outStream );
		writer.print( src );
		writer.flush();
		writer.close();
		
		InputStream inStream = proc.getInputStream();
		
		SVGUniverse universe = SVGCache.getSVGUniverse();
		URI diagramURI;
		try
		{
			diagramURI = universe.loadSVG( inStream, "dot_output", true );
		}
		catch (IOException e)
		{
			return new ErrorBox( "GraphViz: Error while getting URI for SVG", e );
		}
		
		SVGDiagram diagram = universe.getDiagram( diagramURI );
		
		return new Image( diagram );
	}
}
