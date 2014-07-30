/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import java.io.IOException;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * An example publisher application. The scribble publisher publishes coordinate 
 * information corresponding with lines drawn in its user interface.
 */

public class ScribblePublisher extends AbstractScribbleApplication
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	final static String 	TOPIC_ROOT = "scribble", 
							TOPIC_DRAW = "scribble/coords", 
							TOPIC_CLEAR = "scribble/clear";
	
	Shell 					shell;	
	Action 					connectAction, 
							runDemoAction, 
							stopDemoAction, 
							clearAction;
	ScribbleCanvas 			canvas;
	Point 					point;
	boolean 				pressed;
	
	TopicConnection 		connection;
	TopicSession 			session;
	TopicPublisher 			drawPublisher, 
							clearPublisher;
	ScribbleDemoDriver 		demo;
	
	public static void main (String [] args)
	{
		ScribblePublisher app = new ScribblePublisher (null);

		app.open ();

		Shell shell = app.getShell ();
		Display display = Display.getDefault ();

		while (!shell.isDisposed ())
		{
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();
	}

	public ScribblePublisher (Shell shell)
	{
		super (shell);
		
		ToolBarManager manager = getToolBarManager ();
		
		manager.add (connectAction = new Action (getString ("action.connect"), 
			ImageDescriptor.createFromFile (getClass (), "broker_obj.gif"))
		{
			public void run ()
			{
				connect (null);
			}
		});
		connectAction.setToolTipText (connectAction.getText ());

		manager.add (clearAction = new Action (getString ("action.clear"), 
			ImageDescriptor.createFromFile (getClass (), "clear_co.gif"))
		{
			public void run ()
			{
				stopDemo ();
				clearAndPublish ();
			}
		});
		clearAction.setToolTipText (clearAction.getText ());

		manager.add (new Separator ("demo"));
		manager.appendToGroup ("demo", runDemoAction = new Action (getString ("action.demo"), 
			ImageDescriptor.createFromFile (getClass (), "resume_co.gif"))
		{
			public void run ()
			{
				runDemo ("torolab-house.bmp");
			}
		});
		runDemoAction.setToolTipText (runDemoAction.getText ());
		manager.appendToGroup ("demo", stopDemoAction = new Action (getString ("action.stop"), 
			ImageDescriptor.createFromFile (getClass (), "terminate_co.gif"))
		{
			public void run ()
			{
				stopDemo ();
			}
		});
		stopDemoAction.setToolTipText (stopDemoAction.getText ());
		stopDemoAction.setEnabled (false);
	}
	
	/**
	 * @see org.eclipse.jface.window.ApplicationWindow
	 */
	
	protected void configureShell (Shell shell)
	{
		this.shell = shell;

		GridData 		data;
		Control 		toolbar, 
						statusLine;
		int 			numColumns;

		shell.setLayout (new GridLayout (numColumns = 3, false));

		toolbar = getToolBarManager ().createControl (shell);
		toolbar.setLayoutData (data = new GridData 
			(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		data.horizontalSpan = numColumns;

		canvas = new ScribbleCanvas (shell, SWT.NONE);
		canvas.setLayoutData (data = new GridData (GridData.GRAB_VERTICAL | 
			GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		data.horizontalSpan = numColumns;
		canvas.setBackground (shell.getDisplay ().getSystemColor (SWT.COLOR_WHITE));
		canvas.setForeground (shell.getDisplay ().getSystemColor (SWT.COLOR_DARK_BLUE));
		canvas.addMouseListener (new MouseAdapter () 
		{
			public void mouseDown (MouseEvent event) 
			{
				point = new Point (event.x, event.y);
				pressed = true;
			}

			public void mouseUp (MouseEvent event)
			{
				pressed = false;
			}
		});
		canvas.addMouseMoveListener (new MouseMoveListener () 
		{
			public void mouseMove (MouseEvent event)
			{
				if (pressed)
				{
					drawAndPublish (point.x, point.y, event.x, event.y);
					point.x = event.x;
					point.y = event.y;
				}
			}
		});
		
		statusLine = getStatusLineManager ().createControl (shell);
		statusLine.setLayoutData (data = new GridData 
			(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		data.horizontalSpan = numColumns;

		shell.setText (getString ("title.publisher"));
		shell.addDisposeListener (new DisposeListener () 
		{
			public void widgetDisposed (DisposeEvent event)
			{
				demo = null;
				disconnect ();
			}
		});
		shell.pack ();

		setEnabled (false);
		setStatus (getString ("status.unconnected"));
	}
	
	/**
	 * Opens the application window and initiates connection to a broker if there is 
	 * default connection information available.
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow
	 */
	
	public int open ()
	{
		int rc = super.open ();
		
		ScribbleConnectionInfo info = ScribbleConnectionDialog.getDefaultConnectionInfo ();

		if (info != null)
		{
			connect (info);
		}
		
		return rc;
	}
	
	/**
	 * Draws a line on the canvas and publishes the coordinates.
	 */
	
	public void drawAndPublish (int x1, int y1, int x2, int y2)
	{
		try
		{
			canvas.draw (x1, y1, x2, y2);
			publishLine (x1, y1, x2, y2);
		}
		
		catch (JMSException jmse)
		{
			pressed = false;
			
			MessageDialog.openError (shell, getString ("title.scribble"), 
				getString ("message.publishfailed") + jmse.getMessage ());			
		}
	}
	
	/**
	 * Clears the canvas an publishes a clear message.
	 */
	
	public void clearAndPublish ()
	{
		try
		{
			canvas.clear ();
			publishClear ();
		}
		
		catch (JMSException jmse)
		{
			MessageDialog.openError (shell, getString ("title.clear"), 
				getString ("message.publishfailed") + jmse.getMessage ());			
		}
	}
	
	public void connect (ScribbleConnectionInfo info)
	{
		ScribbleConnectionDialog dialog = new ScribbleConnectionDialog (shell, this, info);

		if (dialog.open () == Window.OK)
		{
			setStatus (getString ("status.connected") + dialog.getConnectionInfo ());
			setEnabled (true);
			canvas.setFocus ();
		}
		else
		{
			setStatus (getString ("status.unconnected"));
			setEnabled (false);
		}
	}
	
	public synchronized void init (TopicConnection connection) throws JMSException
	{
		session = connection.createTopicSession (false, Session.AUTO_ACKNOWLEDGE);
		
		drawPublisher = session.createPublisher (session.createTopic (TOPIC_DRAW));
		drawPublisher.setDeliveryMode (DeliveryMode.NON_PERSISTENT);
		
		clearPublisher = session.createPublisher (session.createTopic (TOPIC_CLEAR));
		clearPublisher.setDeliveryMode (DeliveryMode.NON_PERSISTENT);
		
		this.connection = connection;
	}
	
	public synchronized void disconnect ()
	{
		if (connection != null)
		{
			demo = null;

			new DisconnectOperation (connection).start ();
					
			connection = null;
		}
	}
	
	public void publishLine (int x1, int y1, int x2, int y2) throws JMSException
	{
		String xml =
			"<coords>" +
				"<x1>" + x1 + "</x1>" +
				"<x2>" + x2 + "</x2>" +
				"<y1>" + y1 + "</y1>" +
				"<y2>" + y2 + "</y2>" +
			"</coords>"	;
		TextMessage message = session.createTextMessage();
		message.setText(xml);
		drawPublisher.publish (message);
	}
	
	public void publishClear () throws JMSException
	{
		TextMessage message = session.createTextMessage ();
		
		message.setText ("<clear></clear>");
		
		clearPublisher.publish (message);
	}
	
	void setEnabled (boolean enabled)
	{
		canvas.setEnabled (enabled);
		runDemoAction.setEnabled (enabled);
		clearAction.setEnabled (enabled);
	}
	
	public void runDemo (String filename)
	{
		try
		{
			new Thread (demo = new ScribbleDemoDriver (this, filename)).start ();
			stopDemoAction.setEnabled (true);
		}
		
		catch (IOException ioe)
		{
			MessageDialog.openError (shell, getString ("title.scribble"), 
				getString ("message.demofailed") + ioe.getMessage ());
		}
	}
	
	public void stopDemo ()
	{
		stopDemoAction.setEnabled (false);
		demo = null;
	}
}
