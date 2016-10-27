package com.spfsolutions.ioms.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import com.spfsolutions.ioms.async.Hook;
import com.spfsolutions.ioms.async.ShutdownService;
import com.spfsolutions.ioms.models.OrderModel;

@Service
public class AsyncOrderReviewService implements Runnable {

    private static final Logger logger = Logger.getLogger(AsyncOrderReviewService.class);
    
    private final BlockingQueue<DeferredResult<OrderModel>> resultReviewQueue = new LinkedBlockingQueue<DeferredResult<OrderModel>>();

    private Thread thread;

    private volatile boolean start = true;

    @Autowired
    private ShutdownService shutdownService;

    private Hook hook;
    
    @Autowired
    @Qualifier("pendingReviewQueue")
    private LinkedBlockingQueue<OrderModel> pendingReviewQueue;

    public void subscribe() {
        startThread();
    }

    private void startThread() {

        if (start) {
            synchronized (this) {
                if (start) {
                    start = false;
                    thread = new Thread(this, "Order Review Listerner running");
                    hook = shutdownService.createHook(thread);
                    thread.start();
                }
            }
        }
    }

    @Override
    public void run() {

        logger.info("AsyncOrderReviewService - Thread running");
        while (hook.keepRunning()) {
            try {
                Thread.sleep(200L);
                if(!resultReviewQueue.isEmpty()){
                    OrderModel model = pendingReviewQueue.take();
                    while(resultReviewQueue.size() != 0){
                        DeferredResult<OrderModel> reviewResult = resultReviewQueue.take();
                        reviewResult.setResult(model);
                    }
                }
                

            } catch (InterruptedException e) {
                System.out.println("Interrupted when waiting for latest update. "
                        + e.getMessage());
            }
        }
        logger.info("AsyncOrderReviewService - Thread ending");
    }
    
    public void getReviewUpdate(DeferredResult<OrderModel> reviewResult) {
        resultReviewQueue.add(reviewResult);
    }
    
    public void clearWaitingRequests(){
    	resultReviewQueue.clear();
    }

}
