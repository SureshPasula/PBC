/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author cmarkes
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class ScribbleSubscriber extends AbstractScribbleApplication
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	final static String 	TOPIC_ROOT = ScribblePublisher.TOPIC_ROOT, 
							TOPIC_DRAW = ScribblePublisher.TOPIC_DRAW, 
							TOPIC_CLEAR = ScribblePublisher.TOPIC_CLEAR;
	
	Shell 					shell;
	ScribbleCanvas 			canvas;
	TopicConnection 		connection;
	Action 					connectAction;
	
	public static void main (String [] args)
	{
		ScribbleSubscriber app = new ScribbleSubscriber (null);

		app.open ();

		Shell shell = app.getShell ();
		Display display = Display.getDefault ();

		while (!shell.isDisposed ())
		{
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();
	}

	public ScribbleSubscriber (Shell shell)
	{
		super (shell);
		
		getToolBarManager ().add (connectAction = new Action (getString ("action.connect"), 
			ImageDescriptor.createFromFile (getClass (), "broker_obj.gif"))
		{
			public void run ()
			{
				connect (null);
			}
		});
	}
	
	protected void configureShell (Shell shell)
	{		
		this.shell = shell;

		GridData 		data;
		Control 		toolbar, 
						statusLine;
		int 			numColumns;
				
		shell.setLayout (new GridLayout (numColumns = 2, false));
				
		toolbar = getToolBarManager ().createControl (shell);
		toolbar.setLayoutData (data = new GridData 
			(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		data.horizontalSpan = numColumns;

		canvas = new ScribbleCanvas (shell, SWT.NONE);
		canvas.setLayoutData (data = new GridData (GridData.FILL_BOTH));
		data.horizontalSpan = 2;
		canvas.setBackground (shell.getDisplay ().getSystemColor (SWT.COLOR_WHITE));
		canvas.setForeground (shell.getDisplay ().getSystemColor (SWT.COLOR_DARK_BLUE));
		canvas.setEnabled (false);

		statusLine = getStatusLineManager ().createControl (shell);
		statusLine.setLayoutData (data = new GridData 
			(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		data.horizontalSpan = numColumns;

		shell.setText (getString ("title.subscriber"));
		shell.addDisposeListener (new DisposeListener () 
		{
			public void widgetDisposed (DisposeEvent event)
			{
				disconnect ();
			}
		});
		shell.pack ();
		
		setStatus (getString ("status.unconnected"));
	}
	
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

	public void connect (ScribbleConnectionInfo info)
	{
		ScribbleConnectionDialog dialog = new ScribbleConnectionDialog (shell, this, info);

		if (dialog.open () == Window.OK)
		{
			setStatus (getString ("status.connected") + dialog.getConnectionInfo ());			
		}
		else
		{
			setStatus (getString ("status.unconnected"));
		}
	}
	
	public void init (TopicConnection connection) throws JMSException
	{
		TopicSession 		session = connection.createTopicSession (false, Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber 	drawSubscriber = session.createSubscriber (session.createTopic (TOPIC_DRAW)), 
							clearSubscriber = session.createSubscriber (session.createTopic (TOPIC_CLEAR));

		drawSubscriber.setMessageListener (new MessageListener () 
		{
			public void onMessage (Message message)
			{
				draw (message);
			}
		});

		clearSubscriber.setMessageListener (new MessageListener () 
		{
			public void onMessage (Message message)
			{
				clear (message);
			}
		});
		
		synchronized (this)
		{
			this.connection = connection;
		}
	}
	
	public synchronized void disconnect ()
	{
		if (connection != null)
		{
			new DisconnectOperation (connection).start ();

			this.connection = null;
		}
	}
	
	void draw (Message message)
	{
		final TextMessage m = (TextMessage) message;

		shell.getDisplay ().syncExec (new Runnable () 
		{
			public void run ()
			{
				try
				{					
					byte[] bytes = m.getText().getBytes("UTF-8");
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new ByteArrayInputStream(bytes));
					String x1 = doc.getElementsByTagName("x1").item(0).getFirstChild().getNodeValue();
					String x2 = doc.getElementsByTagName("x2").item(0).getFirstChild().getNodeValue();
					String y1 = doc.getElementsByTagName("y1").item(0).getFirstChild().getNodeValue();
					String y2 = doc.getElementsByTagName("y2").item(0).getFirstChild().getNodeValue();
					canvas.draw(Integer.parseInt(x1), Integer.parseInt(y1), Integer.parseInt(x2), Integer.parseInt(y2));					
				}							
				catch (JMSException jmse)
				{
					setStatus (jmse.getMessage ());
				} catch (ParserConfigurationException e) {

				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	void clear (Message message)
	{
		shell.getDisplay ().syncExec (new Runnable () 
		{
			public void run ()
			{
				canvas.clear ();
			}
		});
	}
}
