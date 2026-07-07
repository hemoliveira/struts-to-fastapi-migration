package com.orderco.legacy.action;

import com.orderco.legacy.dao.ProductDao;
import com.orderco.legacy.form.LineItemForm;
import com.orderco.legacy.service.BusinessRuleException;
import com.orderco.legacy.service.OrderService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddLineItemAction extends Action {

    private final OrderService orderService = new OrderService();
    private final ProductDao productDao = new ProductDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LineItemForm lineItemForm = (LineItemForm) form;

        try {
            orderService.addLineItem(lineItemForm.getOrderId(), lineItemForm.getProductId(),
                    lineItemForm.getQuantity());
        } catch (BusinessRuleException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("order", orderService.getOrder(lineItemForm.getOrderId()));
            request.setAttribute("products", productDao.findAll());
            return mapping.findForward("error");
        }

        ActionForward forward = new ActionForward(mapping.findForward("success"));
        forward.setPath(forward.getPath() + "?id=" + lineItemForm.getOrderId());
        forward.setRedirect(true);
        return forward;
    }
}
