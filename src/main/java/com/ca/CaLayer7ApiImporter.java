package com.ca;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.plugins.ApiImporter;
import com.eviware.soapui.plugins.PluginApiImporter;

import java.util.List;

@PluginApiImporter(label = "CA API Developer Portal")
public class CaLayer7ApiImporter implements ApiImporter {

    @Override
    public List<Interface> importApis(Project project) {

        AddApiFromLayer7Action action = new AddApiFromLayer7Action();
        action.perform((WsdlProject) project, null);

        return (List<Interface>) ((WsdlProject) project).getInterfaces().values();
    }
}
