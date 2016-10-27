package com.spfsolutions.ioms.controllers;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spfsolutions.ioms.annotations.LogRequest;
import com.spfsolutions.ioms.models.OrderModel;
import com.spfsolutions.ioms.models.OrderValidationModel;
import com.spfsolutions.ioms.objects.OrderPayment;
import com.spfsolutions.ioms.objects.OrderQueueType;
import com.spfsolutions.ioms.objects.OrderRefund;
import com.spfsolutions.ioms.objects.OrderReview;
import com.spfsolutions.ioms.services.AsyncOrderReviewService;
import com.spfsolutions.ioms.services.MealService;
import com.spfsolutions.ioms.services.ScheduleService;

@Configuration
@EnableAsync
@ComponentScan
@Controller
public class MealController {
    
    ObjectMapper objMapper = new ObjectMapper();
    
    @Autowired
    private AsyncOrderReviewService asyncOrderReview;
    
    @Autowired
    MealService mealService;
    
    @Autowired
    ScheduleService scheduleService;
	
	@RequestMapping("/order/review/")
    @ResponseBody
    public DeferredResult<OrderModel> getOrdersToReview() {
        
        final DeferredResult<OrderModel> result = new DeferredResult<OrderModel>(15000L, Collections.emptyList());
       
        asyncOrderReview.getReviewUpdate(result);
        return result;
    }
    
    @RequestMapping(value = "/order/review/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @LogRequest    
    public OrderReview reviewOrder(@Valid @RequestBody final OrderReview model) throws SQLException {
        
        return mealService.reviewOrder(model);
    }
        
    @RequestMapping(value = "/order/queuetypes/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    public @ResponseBody List<OrderQueueType> getOrderQueueTypes()
    {
        return mealService.getOrderQueueTypes();    
    }
    
    @RequestMapping(value = "/api/order/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED) 
    @LogRequest
    public @ResponseBody OrderModel submitOrder(@Valid @RequestBody final OrderModel model) throws Throwable
    {        
        OrderModel returnedModel = null;
        
        returnedModel= mealService.submitOrder(model);   
        return returnedModel;
    }
    
    @RequestMapping(value = "/api/order/review/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED) 
    @LogRequest
    public @ResponseBody OrderModel getOrderReviewStatus(@RequestParam(value="transactionId", required=false) String transactionId) throws SQLException
    {
        OrderModel returnedModel = mealService.getOrderByTransactionId(transactionId);
        
        return returnedModel; 
    }
    
    @RequestMapping(value = "/api/order/validate/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    @LogRequest
    public @ResponseBody OrderValidationModel validateOrder(@Valid @RequestBody OrderValidationModel model) throws SQLException
    {                
        return mealService.validateOrder(model);
    }
    
    @RequestMapping(value = "/api/order/payment/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED) 
    @LogRequest
    public @ResponseBody OrderModel submitPayment(@Valid @RequestBody OrderPayment model)
    {
        return mealService.submitPayment(model);
    }
    
    @RequestMapping(value = "/api/order/{id}/cancel/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    public @ResponseBody OrderModel editOrder(@PathVariable int id)
    {
        OrderModel model = new OrderModel(id, 500);
        return mealService.editOrder(model, false);    
    }
    
    @RequestMapping(value = "/order/", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED) 
    public @ResponseBody OrderModel editOrder(@RequestBody OrderModel model)
    {
        return mealService.editOrder(model, true);    
    }
    
    @RequestMapping(value = "/order/cleanup/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    @Async
    public @ResponseBody void cleanUpOrders()
    {
        scheduleService.cleanUpOldOrders();    
    }
    
    @RequestMapping(value = "/order/refund/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody OrderRefund refundOrder(@RequestBody OrderRefund model)
    {
        return mealService.submitRefund(model);    
    }
    
    
    @RequestMapping(value = "/order/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    public @ResponseBody List<OrderModel> getOrders(@RequestParam(value="referenceId", required=false) String referenceId, @RequestParam(value="currentStatus", required=false) String status, @RequestParam(value="startDate", required=false) Date startDate, @RequestParam(value="endDate", required=false) Date endDate) throws SQLException
    {
        return mealService.getOrders(referenceId, status, startDate, endDate);    
    }
    
    @RequestMapping(value = "/order/review/first/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    public @ResponseBody void prepareOrdersToReview() throws SQLException
    {
        mealService.getCurrentOrdersToReview();    
    }
    
    @RequestMapping(value = "/order/feed/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    public @ResponseBody List<OrderModel> getOrders()
    {
        return mealService.getCurrentOrders();    
    }
    
    @RequestMapping(value = "/order/{id}/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    public @ResponseBody OrderModel getOrder(@PathVariable int id)
    {
        return mealService.getOrder(id, null, 0);    
    }
    
    @RequestMapping(value = "/order/{id}/print/", method = RequestMethod.GET, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK) 
    @Async
    public void printCustomerReceiptOrder(@PathVariable int id)
    {
        mealService.printCustomerReceipt(id);    
    }
    
        
    @RequestMapping(value = "/meals", method = RequestMethod.GET)
    public String Index()
    {
        return "meals";
    }
    
    @RequestMapping(value = "/orderHistory", method = RequestMethod.GET)
    public String OrderHistory()
    {
        return "orderHistory";
    }
    
    @RequestMapping(value = "/orderfeed", method = RequestMethod.GET)
    public String OrderFeed()
    {
        return "orderfeed";
    }
}
