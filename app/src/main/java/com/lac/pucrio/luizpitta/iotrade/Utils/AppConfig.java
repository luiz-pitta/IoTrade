package com.lac.pucrio.luizpitta.iotrade.Utils;

/**
 * Configuration class with the default values for the project.
 * @author Luis Talavera
 */
public class AppConfig {
	/* DEBUG flag */ 
	public static final boolean DEBUG = true;

	/* ID of the shared preferences file. */
	public static final String SHARED_PREF_FILE = "MobileHubSharedPref";
	public static final String NAME = "name";
	
	/**
	 * Keys used with the Shared Preferences (SP) and default values.
	 * {{ ======================================================================
	 */
	
	/* IP Address of the SDDL gateway
	 * type -- String
	 */
	public static final String SPREF_GATEWAY_IP_ADDRESS = "SPGatewayIpAddress";
	
	/* Port of the SDDL gateway
	 * type -- String
	 */
	public static final String SPREF_GATEWAY_PORT = "SPGatewayPort";

	/* Port of the Node.js Server
	 * type -- String
	 */
	public static final String SPREF_SERVER_PORT = "SPServerPort";
	
	/* The UUID of the user-device
	 * type -- String
	 */
	public static final String SPREF_USER_UUID = "SPUserUuid";
	
	/* The current interval used by the connection service to send messages.
	 * type -- Integer
	 */
	public static final String SPREF_CURRENT_MESSAGES_INTERVAL = "SPCurrentMessagesInterval";
	
	/* The interval values used by the connection service, this parameters are
	 * set manually from the application configuration.
	 */
	public static final String SPREF_MESSAGES_INTERVAL_HIGH   = "SPMessagesIntervalHigh";
	public static final String SPREF_MESSAGES_INTERVAL_MEDIUM = "SPMessagesIntervalMedium";
	public static final String SPREF_MESSAGES_INTERVAL_LOW    = "SPMessagesIntervalLow";
	
	/* Connection status with the gateway.
	 * type -- Boolean
	 * 
	 * __Values__
	 * true  -- Connected 
	 * false -- Not connected
	 */
	public static final String SPREF_IS_CONNECTED = "SPIsConnected";
	
	/**
	 * }} ======================================================================
	 */
	
	/**
	 * Default values
	 * {{ ======================================================================
	 */
	
	/* Default interval values to send messages (milliseconds), it is used by
	 * the connection service.
	 * 
	 * HIGH   ::  1.5 second
	 * MEDIUM ::  2.5 seconds
	 * LOW    ::  6 seconds
	 */
	public static final int DEFAULT_MESSAGES_INTERVAL_HIGH   = 500 * 3;
	public static final int DEFAULT_MESSAGES_INTERVAL_MEDIUM = 500 * 5;
	public static final int DEFAULT_MESSAGES_INTERVAL_LOW    = 500 * 4 * 3;

	
	/*
	 * Default value for the ip address, the first address that the device
	 * will connect to.
	 * 
	 * Default: onDevelopment --// not set //--
	 */
	public static final String DEFAULT_SDDL_IP_ADDRESS = "192.168.25.7";
	
	/*
	 * Default value for the port used by the SDDL.
	 * 
	 * Default: 5500
	 */
	public static final Integer DEFAULT_SDDL_PORT = 5500;

	/*
	 * Default value for the port used by the Server Node.js.
	 *
	 * Default: 8080
	 */
	public static final Integer DEFAULT_SERVER_PORT = 8080;
	
	/**
	 * }} ======================================================================
	 */
}
