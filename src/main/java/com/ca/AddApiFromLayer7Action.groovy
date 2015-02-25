/**
 *  Copyright 2013 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ca

import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport
import com.eviware.soapui.plugins.ActionConfiguration
import com.eviware.soapui.support.StringUtils
import com.eviware.soapui.support.UISupport
import com.eviware.soapui.support.action.support.AbstractSoapUIAction
import com.eviware.x.dialogs.XProgressDialog
import com.eviware.x.form.XFormDialog
import com.eviware.x.form.XFormOptionsField
import com.eviware.x.form.support.ADialogBuilder
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpGet

import javax.swing.AbstractAction
import java.awt.Dimension
import java.awt.event.ActionEvent

/**
 * Action that opens a dialog for browsing and adding APIs from the Layer7 directory
 */

@ActionConfiguration(actionGroup = "EnabledWsdlProjectActions", afterAction = "AddWadlAction", separatorBefore = true)
class AddApiFromLayer7Action extends AbstractSoapUIAction<WsdlProject> {

    public static final String WADL_LIST_PATH = "external-service/list-wadl.json"
    public static final String ENDPOINT_SETTING = "CA-API-DEVELOPER-PORTAL-ENDPOINT"
    public static final String USERNAME_SETTING = "CA-API-DEVELOPER-PORTAL-USERNAME"
    public static final String PASSWORD_SETTING = "CA-API-DEVELOPER-PORTAL-PASSWORD"
    public static final String REMEMBER_CREDENTIALS_SETTING = "CA-API-DEVELOPER-PORTAL-REMEMBER-CREDENTIALS"
    private XFormDialog dialog = null
    private Map endpoints = [:]

    public AddApiFromLayer7Action() {
        super("Add API from CA API Developer Portal", "Imports an API from the CA API Developer Portal");
    }

    void perform(WsdlProject project, Object o) {
        dialog = ADialogBuilder.buildDialog(AddApiWizard.class)

        def settings = project.workspace.settings

        dialog.setValue(AddApiWizard.PORTAL, settings.getString(ENDPOINT_SETTING, ""))
        dialog.setValue(AddApiWizard.USERNAME, settings.getString(USERNAME_SETTING, ""))
        dialog.setValue(AddApiWizard.PASSWORD, settings.getString(PASSWORD_SETTING, ""))
        dialog.setBooleanValue(AddApiWizard.REMEMBER, settings.getBoolean(REMEMBER_CREDENTIALS_SETTING, false))

        dialog.getFormField(AddApiWizard.GETAPIS).setProperty("action", new GetApisAction())
        ((XFormOptionsField) dialog.getFormField(AddApiWizard.APIS)).setOptions([].toArray())
        dialog.getFormField(AddApiWizard.APIS).setEnabled(false)

        if (dialog.show()) {

            def options = ((XFormOptionsField) dialog.getFormField(AddApiWizard.APIS)).selectedOptions
            if (options.length == 0) {
                UISupport.showErrorMessage("No APIs selected")
                return
            }

            XProgressDialog dlg = UISupport.getDialogs().createProgressDialog("Importing $options.length API" + (options.length > 1 ? "s" : ""), 0, "", false);
            dlg.run(new WadlImporterWorker(project, options, endpoints));

            def remember = dialog.getBooleanValue(AddApiWizard.REMEMBER)

            settings.setString(ENDPOINT_SETTING, remember ? dialog.getValue(AddApiWizard.PORTAL) : "")
            settings.setString(USERNAME_SETTING, remember ? dialog.getValue(AddApiWizard.USERNAME) : "")
            settings.setString(PASSWORD_SETTING, remember ? dialog.getValue(AddApiWizard.PASSWORD) : "")
            settings.setBoolean(REMEMBER_CREDENTIALS_SETTING, remember ? dialog.getBooleanValue(AddApiWizard.REMEMBER) : false)
        }
    }

    private class GetApisAction extends AbstractAction {

        GetApisAction() {
            super("Get APIs");
        }

        @Override
        void actionPerformed(ActionEvent e) {

            def endpoint = dialog.getValue(AddApiWizard.PORTAL)
            if (StringUtils.isNullOrEmpty(endpoint)) {
                UISupport.showErrorMessage("Missing Portal endpoint!");
                return
            }

            if (!endpoint.toLowerCase().endsWith(WADL_LIST_PATH)) {
                if (!endpoint.endsWith("/")) {
                    endpoint += "/"
                }

                endpoint += WADL_LIST_PATH
            }

            if (!endpoint.toLowerCase().startsWith("http")) {
                endpoint = "http://" + endpoint
            }

            URI url = new URI(endpoint);

            HttpGet get = new HttpGet(url);

            def username = dialog.getValue(AddApiWizard.USERNAME)
            def password = dialog.getValue(AddApiWizard.PASSWORD)

            if (!StringUtils.hasContent(username) || !StringUtils.hasContent(password)) {
                UISupport.showErrorMessage("Missing Portal credentials!");
                return
            }

            HttpClientSupport.getHttpClient().getCredentialsProvider().setCredentials(
                    new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(username, password)
            );

            try {
                UISupport.setHourglassCursor()
                HttpResponse response = HttpClientSupport.getHttpClient().execute(get);
                if (response.getStatusLine().getStatusCode() != 200) {
                    UISupport.showExtendedInfo("Error", "Failed to get API listing", response.getStatusLine().toString(),
                            new Dimension(400, 200));
                } else {
                    String content = IOUtils.toString(response.getEntity().getContent());

                    def json = new JsonSlurper().parseText(content)
                    def list = []

                    json.wadllist.each {
                        list.add(it.apiname)
                        endpoints.put(it.apiname, it.download)
                    }

                    ((XFormOptionsField) dialog.getFormField(AddApiWizard.APIS)).setOptions(list.toArray())
                    dialog.getFormField(AddApiWizard.APIS).setEnabled(true)
                }
            }
            catch (Throwable t) {
                UISupport.showErrorMessage(t)
            }
            finally {
                UISupport.resetCursor()
            }
        }
    }
}
