package com.lac.pucrio.luizpitta.iotrade.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.infopae.model.BuyAnalyticsData;
import com.infopae.model.SendActuatorData;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.MatchmakingData;
import com.lac.pucrio.luizpitta.iotrade.Services.Listeners.ConnectionListener;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppConfig;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;

public class ConnectionService extends Service {
	/** DEBUG */
	private static final String TAG = ConnectionService.class.getSimpleName();

    /** Tag used to route the message */
    public static final String ROUTE_TAG = "CONN";

	/** The context object */
	private Context ac;

	/** SDDL IP address */
	private String ipAddress;

	/** SDDL connection port */
	private Integer port;

    /** The UUID of the device */
    private UUID uuid;

	/** The node connection to the SDDL gateway */
	//private static NodeConnection connection;
	private NodeConnection connection;

	/** The connection listener for the node connection */
	private ConnectionListener listener;

	/** The MrUDP socket connection */
	private SocketAddress socket;

	/** The keep running flag to indicate if the service is running, used internally */
	private volatile Boolean keepRunning;

	/** The is connected flag to indicate if the connection is active, used internally */
	private volatile Boolean isConnected;

    /** The interval time between messages to be sent */
    private Integer sendAllMsgsInterval;

	/** A list of messages to be sent to the gateway */
	private final ConcurrentHashMap<String, Message> lstMsg = new ConcurrentHashMap<>();

	final Object lock = new Object();

	@Override
	public void onCreate() {
		super.onCreate();
		// initialize the flags
		keepRunning = true;
		isConnected = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        AppUtils.logger( 'i', TAG, ">> Started" );
		// get the context
		ac = ConnectionService.this;
        // register to event bus
        EventBus.getDefault().register( this );
		// if it is not connected, create a new thread resetting previous threads
		if( !isConnected ) {
			// call the bootstrap to initialize all the variables
			bootstrap();
			// start thread connection
			startThread();
		}
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent i ) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        AppUtils.logger( 'i', TAG, ">> Destroyed" );
		// not connected
		isConnected = false;
        // unregister from event bus
        EventBus.getDefault().unregister( this );

		if( sendAllMsgsInterval <= 0 ) {
			synchronized( lock ) {
				lock.notify();
			}
		}
	}

	/**
	 * The bootstrap for this service, it will start and get all the default
	 * values from the SharedPreferences to start the service without any
	 * problem.
	 */
	private void bootstrap() {
		Boolean saved;
		// create the UUID for this device if there is not one
		if( AppUtils.getUuid( ac ) == null ) {
			saved = AppUtils.createSaveUuid( ac );
			if( !saved )
				AppUtils.logger( 'e', TAG, ">> UUID not saved to SharedPrefs" );
		}
		uuid = AppUtils.getUuid( ac );

		// set ip address
		ipAddress = AppUtils.getIpAddress( ac );
		if( ipAddress == null )
			ipAddress = AppConfig.DEFAULT_SDDL_IP_ADDRESS;
		// save the ip address to SPREF
		AppUtils.saveIpAddress( ac, ipAddress );

		// set port
		port = AppUtils.getGatewayPort( ac );
		if( port == null )
			port = AppConfig.DEFAULT_SDDL_PORT;
		// save port to SPREF
		AppUtils.saveGatewayPort( ac, port );

		// set the interval time between messages
		sendAllMsgsInterval = AppUtils.getCurrentSendMessagesInterval( ac );
		if( sendAllMsgsInterval == null )
			sendAllMsgsInterval = AppConfig.DEFAULT_MESSAGES_INTERVAL_HIGH;
		AppUtils.saveCurrentSendMessagesInterval( ac, sendAllMsgsInterval );

		// start the listener here to be on another Thread
		listener = ConnectionListener.getInstance( ac );
		//listener = new ConnectionListener( ac );

		// set all the default values for the options HIGH, MEDIUM and LOW on SPREF
		if( AppUtils.getSendSignalsInterval( ac, AppConfig.SPREF_MESSAGES_INTERVAL_HIGH ) == null )
			AppUtils.saveSendSignalsInterval( ac,
					AppConfig.DEFAULT_MESSAGES_INTERVAL_HIGH,
					AppConfig.SPREF_MESSAGES_INTERVAL_HIGH );

		if( AppUtils.getSendSignalsInterval( ac, AppConfig.SPREF_MESSAGES_INTERVAL_MEDIUM ) == null )
			AppUtils.saveSendSignalsInterval( ac,
					AppConfig.DEFAULT_MESSAGES_INTERVAL_MEDIUM,
					AppConfig.SPREF_MESSAGES_INTERVAL_MEDIUM );

		if( AppUtils.getSendSignalsInterval( ac, AppConfig.SPREF_MESSAGES_INTERVAL_LOW ) == null )
			AppUtils.saveSendSignalsInterval( ac,
					AppConfig.DEFAULT_MESSAGES_INTERVAL_LOW,
					AppConfig.SPREF_MESSAGES_INTERVAL_LOW );
	}

	/**
	 * It starts the connection thread, it creates the connection and everything
	 * related to the connection to the gateway.
	 */
	private void startThread() {
		Thread t = new Thread(new Runnable() {
			public void run () {
				try {
					AppUtils.logger( 'i', TAG, "Thread created!! -- " + ipAddress + ":" + port );

                    //connection = getConnection();
					connection = new MrUdpNodeConnection();
					connection.addNodeConnectionListener( listener );
					socket = new InetSocketAddress( ipAddress, port );
					connection.connect( socket );
					isConnected = true;
					// set the service is running flag and is connected
                    Boolean saved = AppUtils.saveIsConnected( ac, true );
					if( !saved )
						AppUtils.logger( 'e', TAG, ">> isConnected flag not saved" );

					// loop forever while the service is running
					while( keepRunning ) {
						// kill connection
						if( !isConnected ) {
							keepRunning = false;
							connection.disconnect();
							stopThread();
						}

						// send messages if we have connection with the device
						if( isConnected && sendAllMsgsInterval > 0 ) {
                            AppUtils.logger( 'i', TAG, ">> Sending Messages(" + lstMsg.size() + ")" );
							Iterator<Map.Entry<String, Message>> it = lstMsg.entrySet().iterator();
							//Iterator<Message> it = lstMsg.iterator();

							synchronized( lstMsg ) {
								while( it.hasNext() ) {
									Map.Entry<String, Message> currentMessage = it.next();
									//Message currentMessage = it.next();
									connection.sendMessage( currentMessage.getValue() );
									//connection.sendMessage( currentMessage );
									it.remove();

								}
							}
							// This has to be changed. The disconnection will wait until the thread is wake up
							// Use handlers instead (Pendant)
							synchronized( this ) {
								Thread.sleep( sendAllMsgsInterval );
							}
						} else if( isConnected ) {
							synchronized( lock ) {
								lock.wait();
							}
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	/**
	 * It stops the connection thread.
	 */
	private synchronized void stopThread() {
		Boolean saved = AppUtils.saveIsConnected( ac, false );
		if( !saved )
			AppUtils.logger( 'e', TAG, ">> isConnected flag not saved" );
	}

	/**
	 * Creates an application message to send to the cloud
	 * It will send the message immediately
	 * @param s The Mobile Hub Message structure
	 * @param sender The UUID of the Mobile Hub
	 */
	private void createAndSendMsg(Serializable s, UUID sender) {

		try {
			ApplicationMessage am = new ApplicationMessage();
			am.setContentObject( s );
			am.setTagList( new ArrayList<String>() );
			am.setSenderID( sender );

			connection.sendMessage( am );
		} catch (Exception e) {
			AppUtils.logger( 'i', TAG, "Error sending..." );
		}
	}

	@Subscribe() @SuppressWarnings("unused") // it's actually used to receive activity information and send to contextnet
	public void onEvent( MatchmakingData matchmakingData ) {
		if( matchmakingData != null && AppUtils.isInRoute( ROUTE_TAG, matchmakingData.getRoute() ) ) {
			createAndSendMsg( matchmakingData, uuid );
		}
	}

	@Subscribe() @SuppressWarnings("unused") // it's actually used to receive from ActuatorActivity and send to contextnet
	public void onEvent( SendActuatorData sendActuatorData ) {
		if( sendActuatorData != null ) {
			createAndSendMsg( sendActuatorData, uuid );
		}
	}

	@Subscribe() @SuppressWarnings("unused")	// it's actually used to receive from AnalyticsActivity and send to contextnet
	public void onEvent( BuyAnalyticsData buyAnalyticsData ) {
		if( buyAnalyticsData != null ) {
			createAndSendMsg( buyAnalyticsData, uuid );
		}
	}
}
