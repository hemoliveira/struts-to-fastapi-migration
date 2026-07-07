package com.orderco.legacy.action;

import com.orderco.legacy.service.BusinessRuleException;
import com.orderco.legacy.service.OrderService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SubmitOrderAction extends Action {

    private final OrderService orderService = new OrderService();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        long orderId = Long.parseLong(request.getParameter("id"));

        try {
            orderService.submitOrder(orderId);
        } catch (BusinessRuleException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("order", orderService.getOrder(orderId));
            return mapping.findForward("error");
        }

        ActionForward forward = new ActionForward(mapping.findForward("success"));
        forward.setPath(forward.getPath() + "?id=" + orderId);
        forward.setRedirect(true);
        return forward;
    }
}
