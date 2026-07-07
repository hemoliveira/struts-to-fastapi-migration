package com.orderco.legacy.form;

import org.apache.struts.action.ActionForm;

public class NewOrderForm extends ActionForm {

    private long customerId;

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }
}
