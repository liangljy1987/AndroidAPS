package info.nightscout.androidaps.plugins.general.nsclient.acks;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import info.nightscout.androidaps.events.Event;
import info.nightscout.androidaps.logging.L;
import info.nightscout.androidaps.logging.LTag;
import info.nightscout.androidaps.logging.StacktraceLoggerWrapper;
import info.nightscout.androidaps.plugins.bus.RxBus;
import info.nightscout.androidaps.plugins.general.nsclient.events.EventNSClientRestart;
import io.socket.client.Ack;

/**
 * Created by mike on 29.12.2015.
 */
public class NSAddAck extends Event implements Ack {
    private static Logger log = StacktraceLoggerWrapper.getLogger(LTag.NSCLIENT);
    public String _id = null;
    public String nsClientID = null;
    public JSONObject json = null;

    public void call(Object... args) {
        // Regular response
        try {
            JSONArray responsearray = (JSONArray) (args[0]);
            JSONObject response = null;
            if (responsearray.length() > 0) {
                response = responsearray.getJSONObject(0);
                _id = response.getString("_id");
                json = response;
                if (response.has("NSCLIENT_ID")) {
                    nsClientID = response.getString("NSCLIENT_ID");
                }
            }
            RxBus.Companion.getINSTANCE().send(this);
            return;
        } catch (Exception e) {
            log.error("Unhandled exception", e);
        }
        // Check for not authorized
        try {
            JSONObject response = (JSONObject) (args[0]);
            if (response.has("result")) {
                _id = null;
                if (response.getString("result").contains("Not")) {
                    RxBus.Companion.getINSTANCE().send(new EventNSClientRestart());
                    return;
                }
                if (L.isEnabled(LTag.NSCLIENT))
                    log.debug("DBACCESS " + response.getString("result"));
            }
            return;
        } catch (Exception e) {
            log.error("Unhandled exception", e);
        }
    }
}