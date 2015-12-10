package org.rapla.plugin.archiver.server;

import javax.inject.Inject;

import org.rapla.components.util.Command;
import org.rapla.components.util.CommandScheduler;
import org.rapla.components.util.DateTools;
import org.rapla.entities.configuration.RaplaConfiguration;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaException;
import org.rapla.framework.logger.Logger;
import org.rapla.inject.Extension;
import org.rapla.plugin.archiver.ArchiverService;
import org.rapla.server.extensionpoints.ServerExtension;
import org.rapla.storage.ImportExportManager;

@Extension(provides = ServerExtension.class,id="archiver")
public class ArchiverServiceTask  implements ServerExtension
{
    @Inject
	public ArchiverServiceTask(  CommandScheduler timer, final Logger logger, final ClientFacade facade, final ImportExportManager importExportManager)
            throws RaplaException
    {
        final RaplaConfiguration config = facade.getSystemPreferences().getEntry(ArchiverService.CONFIG,new RaplaConfiguration());
        final int days = config.getChild( ArchiverService.REMOVE_OLDER_THAN_ENTRY).getValueAsInteger(-20);
        final boolean export = config.getChild( ArchiverService.EXPORT).getValueAsBoolean(false);
        if ( days != -20 || export)
        {
            Command removeTask = new Command() {
            	public void execute() throws RaplaException {


            		try 
            		{
            			if ( export && ArchiverServiceImpl.isExportEnabled(facade))
            			{
            				importExportManager.doExport();
            			}
            			if ( days != -20 )
                        {
                            ArchiverServiceImpl.delete(days,facade,logger);
                        }
					} 
            		catch (RaplaException e) {
			            logger.error("Could not execute archiver task ", e);
			        }
            	}
            };
            // Call it each hour
            timer.schedule(removeTask, 0, DateTools.MILLISECONDS_PER_HOUR); 
        }
    }

    @Override public void start()
    {

    }
}
