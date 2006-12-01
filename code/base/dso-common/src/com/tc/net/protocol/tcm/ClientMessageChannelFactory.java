/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.net.protocol.tcm;

public interface ClientMessageChannelFactory {

  MessageChannelInternal createNewChannel(String hostname, int port);

}
