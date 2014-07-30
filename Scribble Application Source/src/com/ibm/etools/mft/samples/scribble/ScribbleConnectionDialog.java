/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import javax.jms.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Dialog used by scribble publisher and subscriber applications. This dialog 
 * supports one type of JMS connection: 
 * <ul>
 * <li> WebSphere MQ remote queue manager (client connection)
 * </ul>
 * If initialized with an existing <tt>ScribbleConnectionInfo</tt> object, the connection 
 * dialog attempts to connect to it and shows only a progress bar and cancel button.
 */

public class ScribbleConnectionDialog extends org.eclipse.jface.dialogs.Dialog
{	
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	static ScribbleConnectionInfo 	defaultConnectionInfo;
	static boolean 				autoconnect = true;
	
	final AbstractScribbleApplication app;
	
	Shell 							shell;
	Composite 						stackComposite;
	StackLayout 					stackLayout;
	ConnectionPanel [] 				connectionPanels;
	ConnectionPanel 				current;
	int 							labelWidthHint;
	Label 							progressLabel;
	ProgressIndicator 				progressIndicator;
	ConnectionOperation 			operation;
	ScribbleConnectionInfo 			connectionInfo;
	
	/**
	 * Returns connection information for automatically connecting applications. The 
	 * default connection information is captured from the dialog if the auto-connect 
	 * checkbox is checked. Null will be returned if the checkbox was not checked last 
	 * time a connection was made or no connection has yet been made.
	 */
	
	public static ScribbleConnectionInfo getDefaultConnectionInfo ()
	{
		return autoconnect ? defaultConnectionInfo : null;
	}

	/**
	 * Initializes a connection dialog for the specified scribble application. 
	 * When OK is pressed, the dialog will first invoke the application's 
	 * <tt>disconnect</tt> method, the the <tt>init</tt> method with a newly 
	 * established <tt>TopicConnection</tt> object.
	 */

	public ScribbleConnectionDialog (Shell shell, AbstractScribbleApplication app)
	{
		this (shell, app, null);
	}
	
	/**
	 * Initializes a connection dialog for the specified scribble application and 
	 * existing connection info. If the connection info is valid, the dialog shows 
	 * only a progress bar and cancel button while it invokes the application's 
	 * <tt>disconnect</tt> method, creates a <tt>TopicConnection</tt>, 
	 * and then invokes the <tt>init</tt> method.
	 */

	public ScribbleConnectionDialog (Shell shell, AbstractScribbleApplication app, ScribbleConnectionInfo info)
	{
		super (shell);
		
		this.app = app;
		this.connectionInfo = info;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog
	 */
	
	protected Control createDialogArea (Composite parent)
	{
		Composite 		c = new Composite (parent, SWT.NONE);
		GridLayout 		layout = new GridLayout ();
		GridData 		data;

		(this.shell = getShell ()).setText (getString ("title.connect"));
		
		c.setLayoutData (new GridData (GridData.FILL_BOTH));
		c.setLayout (layout);
		layout.numColumns = 2;
		layout.marginWidth *= 2;

		// Only present the connection information entry fields if we have no 
		// existing connection info (i.e. we are not doing autoconnect).

		if (connectionInfo == null)
		{		
			Label 			label;
			Combo 			combo;
			Button 			checkbox;

			label = new Label (c, SWT.NONE);
			label.setText (getString ("connect.type"));
			label.setLayoutData (data = new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			labelWidthHint = label.computeSize (SWT.DEFAULT, SWT.DEFAULT).x;
			
			combo = new Combo (c, SWT.READ_ONLY);
			combo.setLayoutData (data = new GridData (GridData.GRAB_HORIZONTAL | 
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
	
			stackComposite = new Composite (c, SWT.NONE);
			stackComposite.setLayout (stackLayout = new StackLayout ());
			stackComposite.setLayoutData (data = new GridData (GridData.FILL_BOTH));
			data.horizontalSpan = layout.numColumns;
	
			checkbox = new Button (c, SWT.CHECK);
			checkbox.setLayoutData (data = new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			data.horizontalSpan = layout.numColumns;
			checkbox.setText (getString ("connect.autoconnect"));
			checkbox.setSelection (autoconnect);
			checkbox.addSelectionListener (new SelectionAdapter () 
			{
				public void widgetSelected (SelectionEvent event)
				{
					autoconnect = ((Button) event.getSource ()).getSelection ();
				}
			});
	
			label = new Label (c, SWT.SEPARATOR | SWT.HORIZONTAL);
			label.setLayoutData (data = new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			data.horizontalSpan = layout.numColumns;
			label.setVisible (false);
	
			connectionPanels = new ConnectionPanel [] 
			{				
				new RemoteConnectionPanel (this, stackComposite, SWT.NONE)
			};
			
			for (int i = 0; i < connectionPanels.length; i++)
			{
				combo.add (connectionPanels [i].getTitle ());
			}
			
			combo.addSelectionListener (new SelectionAdapter () 
			{
				public void widgetSelected (SelectionEvent event)
				{
					setConnectionPanel (((Combo) event.getSource ()).getSelectionIndex ());
				}
			});
	
			combo.select (0);
			setConnectionPanel (0);
			parent.setFocus ();
			
			shell.addShellListener (new ShellAdapter () 
			{
				public void shellActivated (ShellEvent event)
				{
					current.validate ();
				}
			});
		}
		
		// The progess bar and its label are always present.
		
		progressLabel = new Label (c, SWT.NONE);
		progressLabel.setLayoutData (data = new GridData 
			(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		data.horizontalSpan = layout.numColumns;
		
		progressIndicator = new ProgressIndicator (c);
		progressIndicator.setLayoutData (data = new GridData 
			(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		data.horizontalSpan = layout.numColumns;

		// If we are doing autoconnect, start the progress bar and the connection 
		// operation.

		if (connectionInfo != null)
		{
			data.grabExcessHorizontalSpace = true;
			progressLabel.setText (getString ("status.connecting") + connectionInfo);
			progressIndicator.beginAnimatedTask ();
			(operation = new ConnectionOperation (connectionInfo)).start ();
		}

		return c;
	}
	
	/**
	 * If initialized with a ScribbleConnectionInfo, show only a Cancel button. 
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog
	 */
	
	protected void createButtonsForButtonBar (Composite parent)
	{
		if (connectionInfo == null)
		{
			super.createButtonsForButtonBar (parent);
		}
		else
		{
			createButton (parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
		}
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog
	 */
	
	protected void okPressed ()
	{
		getButton (IDialogConstants.OK_ID).setEnabled (false);
		getDialogArea ().setEnabled (false);
		connectionInfo = current.getConnectionInfo ();
		progressLabel.setText (getString ("status.connecting") + connectionInfo);
		progressIndicator.beginAnimatedTask ();
		
		(operation = new ConnectionOperation (connectionInfo)).start ();
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog
	 */

	protected void cancelPressed ()
	{
		operation = null;
		
		super.cancelPressed ();
	}
	
	/**
	 * Returns the connection info from the dialog contents when OK was pressed, 
	 * or null.
	 */
	
	public ScribbleConnectionInfo getConnectionInfo ()
	{
		return connectionInfo;
	}
	
	/**
	 * Brings the specified connection panel to the top.
	 */
	
	private void setConnectionPanel (int index)
	{
		stackLayout.topControl = current = connectionPanels [index];
		stackComposite.layout (false);
		
		if (shell.getVisible ())
		{
			current.validate ();
		}
	}

	boolean isValidMQSeriesName (String name)
	{
		char [] chars = name == null ? new char [0] : name.toCharArray ();
		boolean valid = true;
		
		for (int i = 0; i < chars.length && valid; i++)
		{
			char c = chars [i];
			
			valid = Character.isLetterOrDigit (c) || 
				c == '.' || c == '_' || c == '/' || c == '%';
		}
		
		return valid;
	}

	/**
	 * A connection operation runs in its own thread. If the user presses Cancel, 
	 * we simply throw away the reference to any current connection operation (which 
	 * may take up to two minutes to complete, if the broker is not responding). 
	 * The connection operation checks the <tt>operation</tt> member of the enclosing 
	 * <tt>ScribbleConnectionDialog</tt> instance to determine if it is still wanted, 
	 * when the <tt>init</tt> method returns. 
	 * <p>
	 * Even when the application's <tt>init</tt> method has completed, no messages 
	 * will be delivered (in the subscriber case) if the <tt>start</tt> method of the 
	 * <tt>TopicConnection</tt> is not invoked.
	 * <p>
	 * Methods on SWT widgets must be invoked from the thread that constructed them, 
	 * so we use <tt>syncExec</tt>.
	 */

	class ConnectionOperation extends Thread
	{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
		ScribbleConnectionInfo info;
		
		public ConnectionOperation (ScribbleConnectionInfo info)
		{
			this.info = info;
		}
		
		public void run ()
		{
			app.disconnect ();
			
			try
			{
				TopicConnection connection = info.openConnection ();
				
				app.init (connection);
				
				if (operation == this && !shell.isDisposed ())
				{
					connection.start ();

					shell.getDisplay ().syncExec (new Runnable () 
					{
						public void run ()
						{
							defaultConnectionInfo = info;
							ScribbleConnectionDialog.super.okPressed ();
						}
					});
				}
				else
				{
					connection.close ();
				}
			}
			
			catch (final JMSException jmse)
			{
				if (operation == this && !shell.isDisposed ())
				{
					shell.getDisplay ().syncExec (new Runnable () 
					{
						public void run ()
						{
							progressIndicator.done ();
							progressLabel.setText ("");
							MessageDialog.openError (shell, getString ("title.connect"), 
								getString ("message.connectfailed") + jmse.getMessage ());
							getDialogArea ().setEnabled (true);
						}
					});
				}
			}
		}
	}	
	
	/**
	 * Connection panel to collect connection information for a remote queue 
	 * manager connection.
	 */
	
	class RemoteConnectionPanel extends ConnectionPanel
	{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
		public final String title = getString ("connect.remote.title");
	
		ScribbleConnectionDialog dialog;
		String			host = "localhost";
		int 			port = 2414;
		String			qmgr = "IB9QMGR";
	
		public RemoteConnectionPanel (ScribbleConnectionDialog dialog, Composite parent, int style)
		{
			super (parent, style);
			
			this.dialog = dialog;
			
			GridLayout layout;
			GridData data;
			Label label;
			Text text;
			
			setLayout (layout = new GridLayout ());
			layout.numColumns = 2;
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			
			label = new Label (this, SWT.NONE);
			label.setLayoutData (data = new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			data.widthHint = labelWidthHint;
			label.setText (getString ("connect.remote.hostname"));
			
			text = new Text (this, SWT.BORDER);
			text.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | 
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			text.setText (host);
			text.addModifyListener (new ModifyListener () 
			{
				public void modifyText (ModifyEvent event)
				{
					host = ((Text) event.getSource ()).getText ();
					validate ();
				}
			});
	
			label = new Label (this, SWT.NONE);
			label.setLayoutData (new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			label.setText (getString ("connect.remote.port"));
			
			text = new Text (this, SWT.BORDER);
			text.setLayoutData (new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			text.setText (String.valueOf (port));
			text.addModifyListener (new ModifyListener () 
			{
				public void modifyText (ModifyEvent event)
				{
					try
					{
						port = Integer.parseInt (((Text) event.getSource ()).getText ());
					}
					
					catch (NumberFormatException nfe)
					{
						port = (((Text) event.getSource ()).getText ().length () == 0 ? 0 : -1);
					}
					
					validate ();
				}
			});
	
			label = new Label (this, SWT.NONE);
			label.setLayoutData (new GridData 
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			label.setText (getString ("connect.remote.qmgr"));
			
			text = new Text (this, SWT.BORDER);
			text.setLayoutData (new GridData  
				(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
			text.setText (qmgr);
			text.addModifyListener (new ModifyListener () 
			{
				public void modifyText (ModifyEvent event)
				{
					qmgr = ((Text) event.getSource ()).getText ();
					validate ();
				}
			});
		}
		
		public ScribbleConnectionInfo getConnectionInfo ()
		{
			return new ScribbleConnectionInfo ()
			{
				final String host = RemoteConnectionPanel.this.host;
				final int port = RemoteConnectionPanel.this.port;
				final String qmgr = RemoteConnectionPanel.this.qmgr;
				
				public TopicConnection openConnection () throws JMSException
				{
					return ScribbleConnectionFactory.createConnection (qmgr.length () > 0 ? qmgr : "*", 
						host, port, "SCRIBBLE_SVRCONN");
				}
				
				public String toString ()
				{
					return getString ("connect.remote.info") + host + ':' + port;
				}
			};
		}
		
		/**
		 * Disable the OK button if any of the entry fields contain unacceptable 
		 * entries.
		 */
		
		public void validate ()
		{
			if (host.length () == 0 || port == 0)
			{
				getButton (IDialogConstants.OK_ID).setEnabled (false);
			}
			else if (port < 1 || port > 65535)
			{
				getButton (IDialogConstants.OK_ID).setEnabled (false);
			}
			else if (qmgr.length () == 0 || !isValidMQSeriesName (qmgr))
			{
				getButton (IDialogConstants.OK_ID).setEnabled (false);
			}
			else
			{
				getButton (IDialogConstants.OK_ID).setEnabled (true);
			}
		}
		
		public String getTitle ()
		{
			return title;
		}
	}
	
	/**
	 * Abstract superclass of all connection panels. 
	 */

	abstract class ConnectionPanel extends Composite
	{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
		protected ConnectionPanel (Composite parent, int style)
		{
			super (parent, style);
		}
		
		public abstract String getTitle ();
	
		public abstract void validate ();
	
		public abstract ScribbleConnectionInfo getConnectionInfo ();
	}
	
	static String getString (String key)
	{
		return AbstractScribbleApplication.getString (key);
	}

	static String getFormattedString (String key, Object arg)
	{
		return AbstractScribbleApplication.getFormattedString (key, arg);
	}
}
