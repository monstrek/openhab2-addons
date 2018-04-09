package org.openhab.binding.jablotron.handler;

import com.google.gson.Gson;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.jablotron.config.DeviceConfig;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.model.JablotronControlResponse;
import org.openhab.binding.jablotron.model.JablotronLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

public abstract class JablotronAlarmHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronAlarmHandler.class);

    protected Gson gson = new Gson();

    protected DeviceConfig thingConfig;
    protected String session = "";

    protected boolean inService = true;
    protected int lastHours = Utils.getHoursOfDay();

    ScheduledFuture<?> future = null;
    public JablotronAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        super.dispose();
        logout();
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public void initialize() {
        thingConfig = getConfigAs(DeviceConfig.class);
        scheduler.schedule(() -> {
            doInit();
        }, 0, TimeUnit.SECONDS);
    }

    protected synchronized JablotronControlResponse sendUserCode(String section, String status, String code, String serviceUrl) {
        String url;

        try {
            url = JABLOTRON_URL + "app/" + thing.getThingTypeUID().getId() + "/ajax/ovladani.php";
            String urlParameters = "section=" + section + "&status=" + status + "&code=" + code;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            JablotronControlResponse response;

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Referer", serviceUrl);
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            setConnectionDefaults(connection);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }
            String line = Utils.readResponse(connection);
            logger.info("Control response: {}", line);
            response = gson.fromJson(line, JablotronControlResponse.class);
            logger.debug("sendUserCode result: {}", response.getVysledek());
            return response;
        } catch (Exception ex) {
            logger.error("sendUserCode exception", ex);
        }
        return null;
    }

    protected abstract boolean updateAlarmStatus();

    protected abstract void logout(boolean setOffline);

    protected void logout() {
        logout(true);
    }

    protected void relogin() {
        logger.debug("Doing relogin");
        logout(false);
        login();
        initializeService(false);
    }


    protected void handleHttpRequestStatus(int status) throws InterruptedException {
        switch (status) {
            case 0:
                logout();
                break;
            case 201:
                logout();
                break;
            case 300:
                logger.error("Redirect not supported");
                break;
            case 800:
                login();
                initializeService();
                break;
            case 200:
                scheduler.schedule((Runnable) this::updateAlarmStatus, 1, TimeUnit.SECONDS);
                scheduler.schedule((Runnable) this::updateAlarmStatus, 15, TimeUnit.SECONDS);
                break;
            default:
                logger.error("Unknown status code received: {}", status);
        }
    }


    protected synchronized void login() {
        String url = null;

        try {
            //login

            JablotronBridgeHandler bridge = (JablotronBridgeHandler) this.getBridge().getHandler();
            if (bridge == null) {
                logger.error("Bridge handler is null!");
                return;
            }
            url = JABLOTRON_URL + "ajax/login.php";
            String urlParameters = "login=" + bridge.bridgeConfig.getLogin() + "&heslo=" + bridge.bridgeConfig.getPassword() + "&aStatus=200&loginType=Login";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Referer", JABLOTRON_URL);
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            setConnectionDefaults(connection);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            String line = Utils.readResponse(connection);
            JablotronLoginResponse response = gson.fromJson(line, JablotronLoginResponse.class);

            if (!response.isOKStatus())
                return;

            //get cookie
            session = Utils.getSessionCookie(connection);
            if (!session.equals("")) {
                logger.debug("Successfully logged to Jablonet cloud!");
            } else {
                logger.error("Cannot log in to Jablonet cloud!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot login to Jablonet cloud");
            }

        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed", url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        } catch (Exception e) {
            logger.error("Cannot get Jablotron login cookie", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        }
    }


    protected void setConnectionDefaults(HttpsURLConnection connection) {
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", AGENT);
        connection.setRequestProperty("Accept-Language", "cs-CZ");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setUseCaches(false);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
    }

    protected void doInit() {
        login();
        initializeService();

        future = scheduler.scheduleWithFixedDelay(() -> {
            updateAlarmStatus();
        }, 1, thingConfig.getRefresh(), TimeUnit.SECONDS);
    }

    protected void initializeService() {
        initializeService(true);
    }

    protected void initializeService(boolean verbose) {
        String url = thingConfig.getUrl();
        String serviceId = thingConfig.getServiceId();
        try {
            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", JABLOTRON_URL);
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            setConnectionDefaults(connection);

            if (connection.getResponseCode() == 200) {
                if (verbose) {
                    logger.info("Jablotron {} service: {} successfully initialized", thing.getThingTypeUID().getId(), serviceId);
                } else {
                    logger.debug("Jablotron {} service: {} successfully initialized", thing.getThingTypeUID().getId(), serviceId);
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.error("Cannot initialize Jablotron service: {}", serviceId);
                logger.error("Got response code: {}", connection.getResponseCode());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize OASIS service");
            }
        } catch (Exception ex) {
            logger.error("Cannot initialize Jablotron service: {}", serviceId, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize OASIS service");
        }
    }
}
