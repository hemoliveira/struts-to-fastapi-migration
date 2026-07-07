package com.orderco.legacy.action;

import com.orderco.legacy.service.OrderService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ListOrdersAction extends Action {

    private final OrderService orderService = new OrderService();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("orders", orderService.listOrders());
        return mapping.findForward("success");
    }
}
