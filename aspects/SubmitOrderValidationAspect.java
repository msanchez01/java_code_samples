package com.spfsolutions.ioms.aspects;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import com.j256.ormlite.dao.Dao;
import com.spfsolutions.ioms.annotations.PrepareSubmitOrder;
import com.spfsolutions.ioms.data.GeofenceAreaEntity;
import com.spfsolutions.ioms.data.MenuEntity;
import com.spfsolutions.ioms.data.OrderEntity;
import com.spfsolutions.ioms.data.PaymentTypeEntity;
import com.spfsolutions.ioms.data.ServerSettingEntity;
import com.spfsolutions.ioms.data.TransactionOrderEntity;
import com.spfsolutions.ioms.exceptions.MaximumOrderDateExceededException;
import com.spfsolutions.ioms.exceptions.NoMenuAvailableException;
import com.spfsolutions.ioms.exceptions.ResourceNotFoundException;
import com.spfsolutions.ioms.exceptions.RestaurantClosedException;
import com.spfsolutions.ioms.facades.OrderFacade;
import com.spfsolutions.ioms.models.OrderModel;
import com.spfsolutions.ioms.objects.TaxRate;
import com.spfsolutions.ioms.services.MenuService;
import com.spfsolutions.ioms.services.RestaurantService;

@Aspect
public class SubmitOrderValidationAspect {
    
    @Autowired
    Dao<OrderEntity, Integer> orderDao;
    
    @Autowired
    Dao<MenuEntity, Integer> menuDao;
    
    @Autowired
    Dao<TransactionOrderEntity, Integer> transactionOrderDao;
    
    @Autowired
    Dao<ServerSettingEntity, Integer> serverSettingDao;
    
    @Autowired
    Dao<PaymentTypeEntity, Integer> paymentTypeDao;
    
    @Autowired
    Dao<GeofenceAreaEntity, Integer> geofenceAreaDao;
    
    @Autowired
    MenuService menuService;
    
    @Autowired
    RestaurantService restaurantService;
    
    private static final Logger log = Logger
            .getLogger(OrderFacade.class);
    
    public SubmitOrderValidationAspect(){}
    
    @Before("@annotation(com.spfsolutions.ioms.annotations.ValidateTransactionId)")
    public void validateTransactionId(final JoinPoint joinPoint) throws Throwable
    {
        if(joinPoint.getArgs()[0] instanceof OrderModel){
            OrderModel model = (OrderModel) joinPoint.getArgs()[0];
            if(model.getTransactionId() == null){
                throw new ResourceNotFoundException(String.format("transactionId cannot be null"));
            }
            TransactionOrderEntity transactionOrderEntity = transactionOrderDao.queryForFirst(transactionOrderDao.queryBuilder().where().eq("TransactionId", model.getTransactionId()).prepare());
            if(transactionOrderEntity == null){
                throw new ResourceNotFoundException(String.format("Invalid transactionId"));
            }
            OrderEntity preExistingOrderEntity = orderDao.queryForFirst(orderDao.queryBuilder().where().eq("TransactionOrderId", transactionOrderEntity).prepare());
            if(preExistingOrderEntity != null){ //An order has been already placed using this transaction id
                throw new IllegalArgumentException(String.format("Transaction %s has been already used to place order %d", model.getTransactionId(), preExistingOrderEntity.getId()));
            }
        }
        
    }
    
    @Before("@annotation(com.spfsolutions.ioms.annotations.RejectIfSuspended)")
    public void rejectOrderIfSuspended(final JoinPoint joinPoint) throws SQLException{
        
        ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
        if(restaurantSettings.getCurrentStatus().equalsIgnoreCase("SUSPENDED")){
            throw new RestaurantClosedException(String.format("%s is not currently accepting electronic orders.", restaurantSettings.getName()));
        }    
    }
    
    @Before("@annotation(com.spfsolutions.ioms.annotations.PrepareSubmitOrder)")
    public void prepareSubmitOrder(final JoinPoint joinPoint) throws SQLException{
        
        if(!(joinPoint.getArgs()[0] instanceof OrderModel)){
            return;
        }
        ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
        
        OrderModel model = (OrderModel) joinPoint.getArgs()[0];
        int utcOffSet = restaurantService.getCurrentUtcOffSet();      
        
        if(model.getRequestedDateTime() != null)
        {     
            
            DateTime requestedDateTime = model.getRequestedDateTime().withZone(DateTimeZone.UTC);
            DateTime maximumAllowedOrderDate = new DateTime(DateTimeZone.UTC).plus(restaurantSettings.getMaximumOrderTime());
            DateTime nextAvailableRequestDateTime = new DateTime(DateTimeZone.UTC).plus(restaurantSettings.getSmallOrderMinimumLeadTime());
            DateTimeFormatter formatter = DateTimeFormat.forPattern("h:mm aa MM/dd/yyyy");
            if(requestedDateTime.isAfter(maximumAllowedOrderDate)){                  
                throw new MaximumOrderDateExceededException("This restaurant does not accept orders after ." + maximumAllowedOrderDate.plusMillis(utcOffSet).toString(formatter));
            }else if(requestedDateTime.isBefore(nextAvailableRequestDateTime)){
                String dateTimeChangeComment = String.format("Automatically changed Requested Date Time %s because it is prior to next available Order Date Time %s", requestedDateTime.toString(formatter), nextAvailableRequestDateTime.toString(formatter));
                log.info(dateTimeChangeComment);
                model.setComments(dateTimeChangeComment);
                model.setRequestedDateTime(nextAvailableRequestDateTime);
            }
            
        }
        
        PaymentTypeEntity paymentTypeEntity = paymentTypeDao.queryForId(model.getPaymentTypeId());        
        if(paymentTypeEntity == null){
            throw new ResourceNotFoundException("Invalid Payment Type");
        }
        
        model.setOrderZipcode(model.getDeliveryZipcode() !=null ? model.getDeliveryZipcode() : restaurantSettings.getZipCode());
        
        List<TaxRate> taxRates = menuService.getTaxRatesByZipcode(model.getOrderZipcode());   
        
        int geofenceAreaId = taxRates.isEmpty() ? 0 : taxRates.get(0).getGeofenceAreaId();
        GeofenceAreaEntity geofenceAreaEntity = geofenceAreaDao.queryForId(geofenceAreaId);
        if(geofenceAreaEntity != null){
            model.setGeofenceAreaName(geofenceAreaEntity.getName());
        }
        model.setTaxRates(taxRates);
    }
}
