package com.ca;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressMonitor;

import java.util.Map;

public class WadlImporterWorker extends Worker.WorkerAdapter {
    private WsdlProject project;
    private final Object[] apiNames;
    private final Map apiEndpoints;

    public WadlImporterWorker(WsdlProject project, Object[] apiNames, Map apiEndpoints) {
        this.project = project;
        this.apiNames = apiNames;
        this.apiEndpoints = apiEndpoints;
    }

    public Object construct(XProgressMonitor monitor) {

        for (Object name : apiNames) {

            String endpoint = apiEndpoints.get(name).toString();
            monitor.setProgress(0, "Importing [" + name + "]");

            try {
                if (endpoint.toLowerCase().endsWith("wadl")) {
                    RestService service = (RestService) project.addNewInterface(name.toString(), RestServiceFactory.REST_TYPE);
                    WadlImporter importer = new WadlImporter(service);
                    importer.initFromWadl(endpoint);
                } else if (endpoint.toLowerCase().endsWith("wsdl")) {
                    WsdlImporter importer = new WsdlImporter();
                    importer.importWsdl(project, endpoint);
                }

            } catch (Throwable e) {
                UISupport.showErrorMessage(e);
            }
        }

        return project;
    }
}
