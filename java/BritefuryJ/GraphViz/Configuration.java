//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GraphViz;

import BritefuryJ.Projection.Subject;


public class Configuration
{
	private String dotPath, neatoPath, twopiPath, circoPath, fdpPath, sfdpPath, osagePath;
	
	
	protected static Subject configurationPageSubject = null;

	protected static Configuration instance;
	
	
	
	public Configuration(String dotPath, String neatoPath, String twopiPath, String circoPath, String fdpPath, String sfdpPath, String osagePath)
	{
		this.dotPath = dotPath;
		this.neatoPath = neatoPath;
		this.twopiPath = twopiPath;
		this.circoPath = circoPath;
		this.fdpPath = fdpPath;
		this.sfdpPath = sfdpPath;
		this.osagePath = osagePath;
	}
	
	
	public String getDotPath()
	{
		return dotPath;
	}
	
	public String getNeatoPath()
	{
		return neatoPath;
	}
	
	public String getTwopiPath()
	{
		return twopiPath;
	}
	
	public String getCircoPath()
	{
		return circoPath;
	}
	
	public String getFdpPath()
	{
		return fdpPath;
	}

	public String getSfdpPath()
	{
		return sfdpPath;
	}

	public String getOsagePath()
	{
		return osagePath;
	}




	public static void setInstance(Configuration config)
	{
		instance = config;
	}



	public static void setConfigurationPageSubject(Subject s)
	{
		configurationPageSubject = s;
	}
}
