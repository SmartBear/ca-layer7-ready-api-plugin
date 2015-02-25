package com.ca;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

@PluginConfiguration(groupId = "com.ca.plugins", name = "CA API Developer Portal Ready! API Plugin", version = "1.0",
        autoDetect = true, description = "Imports APIs from CA API Developer Portal into Ready! API for testing and virtualization",
        infoUrl = "")
public class PluginConfig extends PluginAdapter {
}
