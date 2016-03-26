//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
