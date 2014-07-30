/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import javax.jms.*;
import com.ibm.mq.jms.*;

public class ScribbleConnectionFactory
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	static String PUBLICATION_QUEUE = AbstractScribbleApplication.getString ("publication.queue");
	static int PUB_ACK_INTERVAL = 50;

	@SuppressWarnings("deprecation")
	public static TopicConnection createConnection (String qmgr, String hostname, int port, String channel) throws JMSException
	{
		MQTopicConnectionFactory factory = new MQTopicConnectionFactory ();

		factory.setTransportType (JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);     
	    factory.setQueueManager (qmgr);
		factory.setHostName (hostname);
		factory.setPort (port);
		factory.setChannel (channel);
		factory.setBrokerVersion (JMSC.MQJMS_BROKER_V2);
		factory.setSubscriptionStore (JMSC.MQJMS_SUBSTORE_BROKER);
		factory.setProviderVersion("6");
		factory.setBrokerPubQueue (PUBLICATION_QUEUE);
		factory.setPubAckInterval (PUB_ACK_INTERVAL);

		return factory.createTopicConnection ();
	}
}
