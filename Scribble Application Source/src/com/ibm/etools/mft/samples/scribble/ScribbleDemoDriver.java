/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import java.io.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * @author cmarkes
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class ScribbleDemoDriver implements Runnable
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	final ScribblePublisher publisher;
	final ImageData imageData;
	final Display display;
	
	public ScribbleDemoDriver (ScribblePublisher publisher, String filename) throws IOException
	{
		this.publisher = publisher;
		this.imageData = ImageDescriptor.createFromFile (getClass (), filename).getImageData ();
		this.display = publisher.shell.getDisplay ();
	}
	
	public void run ()
	{
		
		while (publisher.demo == this)
		{
			clearAndPublish ();

			byte [] pixels = new byte [imageData.width];
			
			for (int y = 0; y < imageData.height; y++)
			{
				imageData.getPixels (0, y, pixels.length, pixels, 0);

				int start = -1;

				for (int x = 0; x < pixels.length && publisher.demo == this; x++)
				{
					if (start != -1 && pixels [x] != 0)
					{
						drawAndPublish (start, y, x - 1, y);
						start = -1;	
					}
					else if (start == -1 && pixels [x] == 0)
					{
						start = x;
					}
				}
				
				if (start != -1)
				{
					drawAndPublish (start, y, pixels.length - 1, y);	
				}
			}
			
			try
			{
				Thread.sleep (2500);
			}
			
			catch (InterruptedException ie)
			{
			}
		}
	}
	
	void clearAndPublish ()
	{
		display.syncExec (new Runnable () 
		{
			public void run ()
			{
				try
				{
					publisher.canvas.clear ();
					publisher.publishClear ();
				}
				
				catch (Exception e)
				{
					publisher.stopDemo ();
					MessageDialog.openError (publisher.shell, "Scribble Demo", 
						"The scribble demo has been stopped.\n\nReason: " + e.getMessage ());			
				}
			}
		});
	}
	
	void drawAndPublish (final int x1, final int y1, final int x2, final int y2)
	{
		display.syncExec (new Runnable () 
		{
			public void run ()
			{
				try
				{
					publisher.canvas.draw (x1, y1, x2, y2);
					publisher.publishLine (x1, y1, x2, y2);
				}
				
				catch (Exception e)
				{
					publisher.stopDemo ();
					MessageDialog.openError (publisher.shell, "Scribble Demo", 
						"The scribble demo has been stopped.\n\nReason: " + e.getMessage ());			
				}
			}
		});
	}
}
