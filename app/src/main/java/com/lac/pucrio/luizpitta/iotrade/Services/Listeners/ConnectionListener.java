package com.lac.pucrio.luizpitta.iotrade.Services.Listeners;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lac.pucrio.luizpitta.iotrade.Managers.LocalRouteManager;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectionData;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.EventData;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import com.infopae.model.SendSensorData;

/**
 * Receives the messages from the cloud, is the listener for
 * the connection service
 */
public class ConnectionListener implements NodeConnectionListener {
	/** DEBUG */
	private static final String TAG = ConnectionListener.class.getSimpleName();

    /** The UUID for this device */
    private final UUID uuid;

    /** The Command Manager */
    private final LocalRouteManager cm;

    private static ConnectionListener instance = null;

	public ConnectionListener(Context ac ) {
        // get UUID
        uuid = AppUtils.getUuid( ac );
        // get the command manager
        cm = LocalRouteManager.getInstance();
	}

    public static ConnectionListener getInstance( Context ac ) {
        if( instance == null )
            instance = new ConnectionListener( ac );
        return instance;
    }

	@Override
	public void connected( NodeConnection nc ) {
		AppUtils.logger( 'i', TAG, "Connected and Identified..." );
		publishState( ConnectionData.CONNECTED );
        sendACKMessage( nc );
	}

	@Override
	public void reconnected(NodeConnection nc, SocketAddress s, boolean b1, boolean b2 ) {
		AppUtils.logger( 'i', TAG, "Reconnected..." );
		publishState( ConnectionData.CONNECTED );
		sendACKMessage( nc );
	}

	@Override
	public void disconnected( NodeConnection nc ) {
		AppUtils.logger( 'i', TAG, "Disconnected..." );
		publishState( ConnectionData.DISCONNECTED );
	}

	@Override
	public void internalException( NodeConnection nc, Exception e ) {
		AppUtils.logger( 'i', TAG, "InternalException... " + e.getMessage() );
	}

	@Override
	public void newMessageReceived( NodeConnection nc, Message m ) {
		AppUtils.logger( 'i', TAG, "NewMessageReceived..." );
		JsonParser parser = new JsonParser();
		Gson gson = new Gson();

		if( m.getContentObject() instanceof String) {
            String string = (String) m.getContentObject();
            if(string.equals("c") || string.equals("a")){
				EventBus.getDefault().post(string);
            }else {
                String content = new String(m.getContent());
                try {
                    JsonElement object = parser.parse(content);
                    EventData eventData = gson.fromJson(object, EventData.class);
                    EventBus.getDefault().post(eventData);
                } catch (Exception ex) {
                }
            }
		}else if(m.getContentObject() instanceof SendSensorData) {
			SendSensorData sendSensorData = (SendSensorData)m.getContentObject();
			EventBus.getDefault().post( sendSensorData );
		}
	}

	@Override
	public void unsentMessages( NodeConnection nc, List<Message> mList ) {
		AppUtils.logger( 'd', TAG, "UnsetMessages..." );
	}

    /**
     * Sends an ACK message to the cloud and the local services
     * to let them know that the connection (connection or reconnection)
     * is established
     * @param nc The connection
     */
    private void sendACKMessage( NodeConnection nc ) {
        // Send a message once we are reconnected
        ApplicationMessage am = new ApplicationMessage();
        am.setContentObject("ack");
        am.setTagList( new ArrayList<String>() );
        am.setSenderID( uuid );

        try {
            nc.sendMessage( am );
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Publish the connection state to the subscribers
	 * @param state The state connected or disconnected
	 */
	private void publishState( String state ) {
		ConnectionData data = new ConnectionData();
		data.setState( state );
		// Post the Connection object for subscribers
		EventBus.getDefault().postSticky( data );
	}
}
