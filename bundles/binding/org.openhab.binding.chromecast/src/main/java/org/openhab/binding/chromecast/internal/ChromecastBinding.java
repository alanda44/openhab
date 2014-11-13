/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal;

import java.util.Dictionary;

import org.openhab.binding.chromecast.ChromecastBindingProvider;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a binding for ChromeCast devices.
 * 
 * @author Till Klocke
 * @since 1.6.0
 */
public class ChromecastBinding extends
		AbstractActiveBinding<ChromecastBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(ChromecastBinding.class);

	/**
	 * the refresh interval which is used to poll values from the Chromecast
	 * server (optional, defaults to 10000ms)
	 */
	private long refreshInterval = 10000;

	ChromecastHandler chromecastHandler;

	public ChromecastBinding() {
	}

	public void activate() {
		logger.info("Activating ChromeCast Binding and discovering devices");
		chromecastHandler = new ChromecastHandler(this.eventPublisher,
				this.providers.iterator().next());

		chromecastHandler.startDiscovery();
	}

	public void deactivate() {
		logger.info("Deactivating ChromeCast Binding and disconnecting from all discovered devices");
		chromecastHandler.stopDiscovery();
		chromecastHandler.disconnectFromAllDevices();
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected String getName() {
		return "Chromecast Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		if (chromecastHandler != null) {
			chromecastHandler.update();
		} else {
			logger.error("execute called, but ChromecastHandler is null");
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		if (chromecastHandler != null) {
			chromecastHandler.handleCommand(itemName, command);
		} else {
			logger.error("internalReceiveCommand called, but ChromecastHandler is null");
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// Ignored
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		if (config != null) {

			// to override the default refresh interval one has to add a
			// parameter to openhab.cfg like
			// <bindingName>:refresh=<intervalInMs>
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}

			String active = (String) config.get("active");
			if ("true".equals(active)) {
				setProperlyConfigured(true);
			} else {
				setProperlyConfigured(false);
			}
		}
	}

}