********************************************************************
* Sample MQSC source defining queues used by the Scribble sample   * 
*                                                                  
* 
* Licensed Materials - Property of IBM                             *                                   *                                                                  *
* 5648-C63                                                         *                                   *                                                                  *
*(c) Copyright IBM Corp. 2000, 2001. All Rights Reserved.          *                             
* US Government Users Restricted Rights - Use, duplication or      * 
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp *    
*                                                                  *
* This file is to be read by the queue manager running the broker  *
* on which this sample is being used.  To run the file, enter      *
* runmqsc queuemanagername <scribbleqdefs.tst                      *
*                                                                  *
********************************************************************
********************************************
*  Scribble publication queue              *
********************************************   
DEFINE QL('SCRIBBLE_PUBLICATION')   REPLACE
********************************************
*  Scribble sample SVRCONN channel         *
******************************************** 
DEFINE CHL('SCRIBBLE_SVRCONN') CHLTYPE(SVRCONN)
*************************************************************************
*  Scribble sample - JMS required queues.  These queues are defined in  * 
* file MQJMS_PSQ.msqc in the product directory for IBM WebSphere MQ     * 
* Support for Java Message Service.  REPLACE has been removed           * 
* from this copy to protect any existing definitions                    * 
************************************************************************* 
********************************************************************
* IBM Websphere MQ Support for Java Message Service                *             
*                                                                  
* 
* Sample MQSC source defining JMS Publish/Subscribe queues.        * 
* Installation Verification Test - Setup script                    *                                   *                                                                  
* 
* Licensed Materials - Property of IBM                             *                                   *                                                                  *
* 5648-C60 5724-B4 5655-F10                                        *                                   *                                                                  *
*(c) Copyright IBM Corp. 1999. All Rights Reserved.                *                                   *                                                                  *
* US Government Users Restricted Rights - Use, duplication or      * 
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp *  ********************************************************************                                   *  JMS Publish/Subscribe Administration Queue                      * 

********************************************************************   
DEFINE QLOCAL('SYSTEM.JMS.ADMIN.QUEUE') + 
DESCR('Websphere MQ - JMS Classes - admin queue') +    
DEFPSIST(YES) + 
NOSHARE


********************************************************************                                   *  JMS Publish/Subscribe Subscriber Status Queue                   *  

********************************************************************                                   
DEFINE QLOCAL('SYSTEM.JMS.PS.STATUS.QUEUE') + 
DESCR('Websphere MQ - JMS Classes - PS status queue') +
DEFPSIST(YES) +  
SHARE DEFSOPT(SHARED)


********************************************************************                                   *  JMS Publish/Subscribe Report Queue                              *  

********************************************************************    
DEFINE QLOCAL('SYSTEM.JMS.REPORT.QUEUE') + 
DESCR('Websphere MQ - JMS Classes - Report queue') +
DEFPSIST(YES) + 
SHARE DEFSOPT(SHARED)


********************************************************************                                   *  JMS Publish/Subscribe Subscribers Model Queue.  This queue is   * 
*  used by subscribers to create a permanent queue for             *
*  subscriptions                                                   *  


********************************************************************  
DEFINE QMODEL('SYSTEM.JMS.MODEL.QUEUE') +
DESCR('Websphere MQ - JMS Classes - Model queue') +
DEFTYPE(PERMDYN) +
SHARE DEFSOPT(SHARED)
********************************************************************                                   *  JMS Publish/Subscribe Default Non-Durable Shared Queue.  This   * 
*  queue is used as the default shared queue by non-durable        *
*  subscribers                                                     *
*
********************************************************************  
DEFINE QLOCAL('SYSTEM.JMS.ND.SUBSCRIBER.QUEUE') +
DESCR('Websphere MQ - JMS Classes - PS ND shared queue') +
DEFPSIST(YES) +
SHARE DEFSOPT(SHARED) +
MAXDEPTH(100000)

********************************************************************                                   *  JMS Publish/Subscribe Default Non-Durable Shared Queue for      * 
*  ConnectionConsumer functionality.  This queue is used as the    *
*  default shared queue by non-durable connection consumers        *
*
******************************************************************* 
  
DEFINE QLOCAL('SYSTEM.JMS.ND.CC.SUBSCRIBER.QUEUE') +
DESCR('Websphere MQ - JMS Classes - PS ND CC shared q') +
DEFPSIST(YES) +
SHARE DEFSOPT(SHARED) +
MAXDEPTH(100000)

********************************************************************                                   *  JMS Publish/Subscribe Default Durable Shared Queue.  This       * 
*  queue is used as the default shared queue by durable            *
*  subscribers                                                     *
*
********************************************************************  
    
DEFINE QLOCAL('SYSTEM.JMS.D.SUBSCRIBER.QUEUE') +
DESCR('Websphere MQ - JMS Classes - PS D shared queue') +
DEFPSIST(YES) +  
SHARE DEFSOPT(SHARED) +  
MAXDEPTH(100000)


********************************************************************                                   *  JMS Publish/Subscribe Default Durable Shared Queue for          * 
*  ConnectionConsumer functionality.  This queue is used as the    *
*  default shared queue by durable connection consumers            *

******************************************************************** 

DEFINE QLOCAL('SYSTEM.JMS.D.CC.SUBSCRIBER.QUEUE') + 
DESCR('Websphere MQ - JMS Classes - PS D CC shared q') + 
DEFPSIST(YES) + 
SHARE DEFSOPT(SHARED) + 
MAXDEPTH(100000)

