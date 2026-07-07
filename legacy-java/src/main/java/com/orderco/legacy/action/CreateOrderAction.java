package com.orderco.legacy.action;

import com.orderco.legacy.form.NewOrderForm;
import com.orderco.legacy.service.OrderService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateOrderAction extends Action {

    private final OrderService orderService = new OrderService();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        NewOrderForm orderForm = (NewOrderForm) form;
        long orderId = orderService.createOrder(orderForm.getCustomerId());

        ActionForward forward = new ActionForward(mapping.findForward("success"));
        forward.setPath(forward.getPath() + "?id=" + orderId);
        forward.setRedirect(true);
        return forward;
    }
}
