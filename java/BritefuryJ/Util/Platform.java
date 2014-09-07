//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2014.
//##************************
package BritefuryJ.Util;

/**
 * Platform class
 *
 * Identify the current platform
 */
public class Platform {
    public static final Platform WINDOWS = new Platform("Windows");
    public static final Platform LINUX = new Platform("Linux");
    public static final Platform MAC = new Platform("Mac");
    public static final Platform UNKNOWN = new Platform("<unknown>");


    private String name;


    /**
     * Constructor
     *
     * Initialises the platform object with the given name
     * @param name - the platform name
     */
    private Platform(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private static Platform runningOn = null;

    public static Platform getPlatform() {
        if (runningOn == null) {
            // Initialise runningOn
            String osName = System.getProperty("os.name");

            if (osName.startsWith("Windows")) {
                runningOn = Platform.WINDOWS;
            }
            else if (osName.startsWith("Linux")) {
                runningOn = Platform.LINUX;
            }
            else if (osName.startsWith("Mac")) {
                runningOn = Platform.MAC;
            }
            else {
                runningOn = UNKNOWN;
            }
        }
        return runningOn;
    }
}
