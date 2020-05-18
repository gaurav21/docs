/**
 * 
 */
package com.sismics.docs.core.service;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.sismics.docs.core.util.TransactionUtil;

/**
 * @author gaurav
 *
 */

//TODO Telegram Notification Service
public class TelegramNotificationService extends AbstractScheduledService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    /**
     * Phantom references queue.
     */
    private final ReferenceQueue<Path> referenceQueue = new ReferenceQueue<>();
    private final Set<TemporaryPathReference> referenceSet = new HashSet<>();
    
    public TelegramNotificationService() {
		// TODO Auto-generated constructor stub
	}
    
    @Override
    protected void startUp() {
    	log.info("Telegram notification service starting");
    }
    
    @Override
    protected void shutDown(){
    	log.info("Telegram Notification Service is Ending");
    }
    
    /**
     * Delete unreferenced temporary files.
     */
    private void deleteTemporaryFiles() throws Exception {
        TemporaryPathReference ref;
        while ((ref = (TemporaryPathReference) referenceQueue.poll()) != null) {
            Files.delete(Paths.get(ref.path));
            referenceSet.remove(ref);
        }
    }

    /**
     * Create a temporary file.
     *
     * @return New temporary file
     */
    public Path createTemporaryFile() throws IOException {
        Path path = Files.createTempFile("sismics_docs", null);
        referenceSet.add(new TemporaryPathReference(path, referenceQueue));
        return path;
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Phantom reference to a temporary file.
     *
     * @author bgamard
     */
    static class TemporaryPathReference extends PhantomReference<Path> {
        String path;
        TemporaryPathReference(Path referent, ReferenceQueue<? super Path> q) {
            super(referent, q);
            path = referent.toAbsolutePath().toString();
        }
    }
    
	@Override
	protected void runOneIteration() throws Exception {
		try {
			startBotService();
		} catch (Throwable e) {
            log.error("Exception during inbox synching", e);
        }
		
		
		
	}
	
	private void startBotService() {
		TransactionUtil.handle(() -> {
			log.info("Searching for telegram notifications");
	    	// Create your bot passing the token received from @BotFather
	    	TelegramBot bot = new TelegramBot("");
	    	bot.setUpdatesListener(updates -> {
	    	    // ... process updates
    	        if (updates != null) {
    		    	for (Update u : updates) {
    		    		if (u.message().document() != null) {
    		    			Document d = u.message().document();
    		    			log.info(u.message().document().toString());
    		    		} else {
    		    			
	    		    		log.info(u.message().from().username() + "~~~~~ " + u.message().text());
    		    		}
    		    		
    		    		
    		    	}
    	        }
	    	    // return id of last processed update or confirm them all
	    	    return UpdatesListener.CONFIRMED_UPDATES_ALL;
	    	});
	    	
	    	
	    	// sync
	    	GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
	    	GetUpdatesResponse updatesResponse = null; 
	    	//= bot.execute(getUpdates);
//	    	List<Update> updates = updatesResponse.updates();
	    	
	    	
	    	
	    	
	    	
	    	// async
//	    	bot.execute(getUpdates, new Callback<GetUpdates, GetUpdatesResponse>() {
//	    	    @Override
//	    	    public void onResponse(GetUpdates request, GetUpdatesResponse response) {
//	    	        List<Update> updates = response.updates();
//	    	        if (updates != null) {
//	    		    	for (Update u : updates) {
//	    		    		if (u.message().document() != null) {
//	    		    			Document d = u.message().document();
//	    		    			log.info(u.message().document().toString());
//	    		    		} else {
//	    		    			
//		    		    		log.info(u.message().toString());
//	    		    		}
//
//	    		    		
//	    		    	}
//	    	    	}
//	    	    }
//	    	    
//	    	    @Override
//	    	    public void onFailure(GetUpdates request, IOException e) {
//	    	    
//	    	    }
//	    	});

		});
    	
	}

}
