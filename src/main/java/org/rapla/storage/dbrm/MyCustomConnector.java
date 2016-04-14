package org.rapla.storage.dbrm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.rapla.ConnectInfo;
import org.rapla.RaplaResources;
import org.rapla.entities.configuration.internal.RaplaMapImpl;
import org.rapla.framework.RaplaException;
import org.rapla.inject.DefaultImplementation;
import org.rapla.inject.InjectionContext;
import org.rapla.rest.client.SerializableExceptionInformation;
import org.rapla.rest.client.gwt.MockProxy;
import org.rapla.rest.client.swing.BasicRaplaHTTPConnector;
import org.rapla.scheduler.CommandScheduler;

@DefaultImplementation(of=BasicRaplaHTTPConnector.CustomConnector.class,context = InjectionContext.swing)
public class MyCustomConnector implements BasicRaplaHTTPConnector.CustomConnector
{
    private final RemoteConnectionInfo remoteConnectionInfo;

    public RemoteAuthentificationService getAuthentificationService()
    {
        return authentificationService;
    }

    public void setAuthentificationService(RemoteAuthentificationService authentificationService)
    {
        this.authentificationService = authentificationService;
    }

    private RemoteAuthentificationService authentificationService;
    private final String errorString;
    private final CommandScheduler commandQueue;

    @Inject public MyCustomConnector(RemoteConnectionInfo remoteConnectionInfo,  RaplaResources i18n,
            CommandScheduler commandQueue)
    {
        this.remoteConnectionInfo = remoteConnectionInfo;
        String server = remoteConnectionInfo.getServerURL();
        this.errorString = i18n.format("error.connect", server) + " ";
        this.commandQueue = commandQueue;
    }

    @Override public String reauth(BasicRaplaHTTPConnector proxy) throws Exception
    {
        final boolean isAuthentificationService = proxy.getClass().getCanonicalName().contains(RemoteAuthentificationService.class.getCanonicalName());
        // We dont reauth for authentification services
        if (isAuthentificationService || authentificationService == null)
        {
            return null;
        }
        final ConnectInfo connectInfo = remoteConnectionInfo.connectInfo;
        final String username = connectInfo.getUsername();
        final String password = new String(connectInfo.getPassword());
        final String connectAs = connectInfo.getConnectAs();
        final LoginTokens loginTokens = authentificationService.login(username, password, connectAs);
        final String accessToken = loginTokens.getAccessToken();
        return accessToken;
    }

    @Override public Exception deserializeException(SerializableExceptionInformation exe)
    {
        final String message = exe.getMessage();
        if (message.indexOf(RemoteStorage.USER_WAS_NOT_AUTHENTIFIED) >= 0 && remoteConnectionInfo != null)
        {
            return new BasicRaplaHTTPConnector.AuthenticationException(message);
        }
        RaplaException ex = new RaplaExceptionDeserializer().deserializeException(exe);
        return ex;
    }

    @Override public Class[] getNonPrimitiveClasses()
    {
        return new Class[] { RaplaMapImpl.class };
    }

    @Override public Exception getConnectError(IOException ex)
    {
        return new RaplaConnectException(errorString + ex.getMessage());
    }

    @Override public String getAccessToken()
    {
        return remoteConnectionInfo.getAccessToken();
    }

    @Override public MockProxy getMockProxy()
    {
        return remoteConnectionInfo.getMockProxy();
    }
}
