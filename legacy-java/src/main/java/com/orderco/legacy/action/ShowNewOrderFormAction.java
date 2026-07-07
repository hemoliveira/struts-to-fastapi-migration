package com.orderco.legacy.action;

import com.orderco.legacy.dao.CustomerDao;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShowNewOrderFormAction extends Action {

    private final CustomerDao customerDao = new CustomerDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("customers", customerDao.findAll());
        return mapping.findForward("success");
    }
}
