package com.orderco.legacy.action;

import com.orderco.legacy.dao.ProductDao;
import com.orderco.legacy.model.PurchaseOrder;
import com.orderco.legacy.service.OrderService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewOrderAction extends Action {

    private final OrderService orderService = new OrderService();
    private final ProductDao productDao = new ProductDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        long orderId = Long.parseLong(request.getParameter("id"));
        PurchaseOrder order = orderService.getOrder(orderId);

        if (order == null) {
            request.setAttribute("errorMessage", "Order " + orderId + " was not found.");
            return mapping.findForward("notfound");
        }

        request.setAttribute("order", order);
        if (PurchaseOrder.STATUS_DRAFT.equals(order.getStatus())) {
            request.setAttribute("products", productDao.findAll());
        }
        return mapping.findForward("success");
    }
}
