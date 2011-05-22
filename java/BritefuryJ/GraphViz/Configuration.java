//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GraphViz;

public class Configuration
{
	private String dotPath, neatoPath, smyrnaPath, leftyPath, dottyPath;
	
	
	protected static Configuration instance;
	
	
	
	public Configuration(String dotPath, String neatoPath, String smyrnaPath, String leftyPath, String dottyPath)
	{
		this.dotPath = dotPath;
		this.neatoPath = neatoPath;
		this.smyrnaPath = smyrnaPath;
		this.leftyPath = leftyPath;
		this.dottyPath = dottyPath;
	}
	
	
	public String getDotPath()
	{
		return dotPath;
	}
	
	public String getNeatoPath()
	{
		return neatoPath;
	}
	
	public String getSmyrnaPath()
	{
		return smyrnaPath;
	}
	
	public String getLeftyPath()
	{
		return leftyPath;
	}
	
	public String getDottyPath()
	{
		return dottyPath;
	}




	public static void setInstance(Configuration config)
	{
		instance = config;
	}
}
