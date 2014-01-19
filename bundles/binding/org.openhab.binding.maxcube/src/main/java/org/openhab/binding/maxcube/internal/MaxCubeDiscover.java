/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class MaxCubeDiscover {

	/**
	* Automatic UDP discovery of a MAX!Cube Lan Gateway on the local network. 
	* 
	* @author Marcel Verpaalen (marcel@verpaalen.com),  
	* based on UDP client code of Michiel De Mey 
	* @since 1.4.0
	*/
	public final static String DiscoverIP () {
	
		String MaxCubeIP = null;
		String MaxCubeName = null;
		String rfAddress = null;
		
		Logger logger = LoggerFactory.getLogger(MaxCubeDiscover.class);
		
		//Find the MaxCube using UDP broadcast
		try {
			DatagramSocket i = new DatagramSocket();
			i.setBroadcast(true);

			byte[] sendData = "eQ3Max*\0**********I".getBytes();

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					// Send the broadcast package!
					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 23272);
						i.send(sendPacket);
					} catch (Exception e) {
						logger.debug(e.getMessage());
						logger.debug(Utils.getStackTrace(e));
				}

				logger.trace( "Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
			}
		}

		logger.trace( "Done looping over all network interfaces. Now waiting for a reply!");
		i.close();

		DatagramSocket c = new DatagramSocket(23272);
		c.setReuseAddress(true);

		//Wait for a response
		byte[] recvBuf = new byte[15000];
		DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
		c.receive(receivePacket);

		//We have a response
		logger.trace( "Broadcast response from server: " + receivePacket.getAddress());

		//Check if the message is correct
		String message = new String(receivePacket.getData()).trim();

		if (message.startsWith("eQ3Max")) {
			
			MaxCubeIP=receivePacket.getAddress().getHostAddress();
			MaxCubeName=message.substring(0, 8);
			rfAddress=message.substring(8, 18);
			logger.debug("Found at: {}", MaxCubeIP);
			logger.debug("Name    : {}", MaxCubeName);
			logger.debug("Serial  : {}", rfAddress);
			logger.trace("Message : {}", message);	
		} else {
			logger.info("No Max!Cube gateway found on network");
		}

		//Close the port!
		c.close();

		} catch (IOException ex) {
			logger.debug(ex.toString());
		}
		return MaxCubeIP;
	}
}
