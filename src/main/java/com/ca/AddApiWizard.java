package com.ca;

import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

@AForm(name = "Add API from CA API Dev Portal", description = "Imports an API from the CA API Developer Portal")
public interface AddApiWizard {

    @AField(name = "Portal", description = "Portal Address", type = AField.AFieldType.STRING)
    public final static String PORTAL = "Portal";

    @AField(name = "Username", description = "Username", type = AField.AFieldType.STRING)
    public final static String USERNAME = "Username";

    @AField(name = "Password", description = "Password", type = AField.AFieldType.STRING)
    public final static String PASSWORD = "Password";

    @AField(name = "Remember Credentials", description = "Remember credentials", type = AField.AFieldType.BOOLEAN)
    public final static String REMEMBER = "Remember Credentials";

    @AField(name = "Get APIs", description = "Get APIs", type = AField.AFieldType.ACTION)
    public final static String GETAPIS = "Get APIs";

    @AField(name = "APIs", description = "API Definitions", type = AField.AFieldType.MULTILIST)
    public final static String APIS = "APIs";

}
