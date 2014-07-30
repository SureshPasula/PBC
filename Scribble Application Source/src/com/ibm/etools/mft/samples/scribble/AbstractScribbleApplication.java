/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import org.eclipse.jface.window.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import javax.jms.*;
import java.text.*;
import java.util.*;

/**
 * Superclass of ScribblePublisher and ScribbleSubscriber applications.
 */

public abstract class AbstractScribbleApplication extends ApplicationWindow
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	static ResourceBundle resourceBundle;
	
	/**
	 * We expect to find scribble.properties in the same directory as the code.
	 */
	
	static
	{
		try
		{
			resourceBundle = ResourceBundle.getBundle ("com.ibm.etools.mft.samples.scribble.scribble");
		}
		
		catch (Exception e)
		{
			System.err.println (e);
		}
	}
	
	static String getString (String key)
	{
		try
		{
			return resourceBundle.getString (key);
		}
		
		catch (MissingResourceException mre)
		{
			return key;
		}
		
		catch (NullPointerException npe)
		{
			return key;
		}
	}

	static String getFormattedString (String key, Object arg)
	{
		return MessageFormat.format (getString (key), new Object [] {arg});
	}

	/**
	 * Establishes basic window characteristics shared by scribble applications.
	 * 
	 * @see </tt>org.eclipse.jface.window.ApplicationWindow</tt>
	 */

	protected AbstractScribbleApplication (Shell shell)
	{
		super (shell);
		
		setShellStyle (SWT.DIALOG_TRIM);
		addToolBar (SWT.FLAT);
		addStatusLine ();
	}
	
	/**
	 * The ScribbleConnectionDialog invokes the <tt>init</tt> method with a TopicConnection 
	 * extablished from the connection information supplied in the dialog. The 
	 * scribble application initializes the resources it needs (sessions, publishers, 
	 * subscribers) in this method.
	 */

	public abstract void init (TopicConnection connection) throws JMSException;
	
	/**
	 * The ScribbleConnectionDialog invokes the <tt>disconnect</tt> method prior to 
	 * establishing a connection for the application.
	 */
	
	public abstract void disconnect ();

	/**
	 * Performs a disconnect asynchronously. This is to avoid blocking the GUI 
	 * for up to two minutes (the broker timeout value).
	 */

	class DisconnectOperation extends Thread
	{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
		TopicConnection connection;
		
		public DisconnectOperation (TopicConnection connection)
		{
			this.connection = connection;
		}
		
		public void run ()
		{
			try
			{
				connection.close ();
			}
			
			catch (Exception e)
			{
			}
		}
	}
}
