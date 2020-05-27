package com.sismics.docs.core.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.DocumentUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.EmailUtil;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Inbox scanning service.
 *
 * @author bgamard
 */
public class InboxService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(InboxService.class);

    /**
     * Last synchronization data.
     */
    private Date lastSyncDate;
    private int lastSyncMessageCount = 0;
    private String lastSyncError;

    public InboxService() {
    }

    @Override
    protected void startUp() {
        log.info("Inbox service starting up");
    }

    @Override
    protected void shutDown() {
        log.info("Inbox service shutting down");
    }
    
    @Override
    protected void runOneIteration() {
        try {

        	syncInbox();
            
        } catch (Throwable e) {
            log.error("Exception during inbox synching", e);
        }
    }  

    /**
     * Synchronize the inbox.
     */
    public void syncInbox() {
        TransactionUtil.handle(() -> {
        	UserDao users = new UserDao();
        	UserCriteria criteria = new UserCriteria();
        	criteria.setInboxEnabled(true);
        	SortCriteria sort = new SortCriteria(0, true);
        	List<UserDto> userDtos =  users.findByCriteria(criteria, sort);
        	log.info("Total mails to be synced " + userDtos.size());
        	for(Iterator<UserDto> userList = userDtos.iterator(); userList.hasNext();) {
        		UserDto user = userList.next();
        		if (user != null && !user.getInboxEnabled()) {
        			return;
        		} 
//        		Boolean enabled = ConfigUtil.getConfigBooleanValue(ConfigType.INBOX_ENABLED);
//                if (!enabled) {
//                    return;
//                }

                log.info("Synchronizing IMAP inbox..." + user.getInboxUserName() + " ----- loggedin user " + user.getUsername() + " tag " + user.getTag());
                Folder inbox = null;
                lastSyncError = null;
                lastSyncDate = new Date();
                lastSyncMessageCount = 0;
                try {
                    inbox = openInbox(user);
                    // we need to check on whether we need to only find the messages who check our criteria
                    if (user.getTag() != null) {
                    	String[] tags = user.getTag().split("~~");
                    	for (String tag : tags) {
                    		String[] subject  = tag.split("~");
                    		log.info("search for " + subject[0]);
                    		FlagTerm term = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
    						SearchTerm search = new AndTerm(new SubjectTerm(subject[0]), term);
    						Message[] messages = inbox.search(search);
		                    log.info(messages.length + " messages found tag - " + subject[0] + '~' + subject[1]);
		                    for (Message message : messages) {
		                    	if (StringUtils.lowerCase(message.getSubject()).contains(StringUtils.lowerCase(subject[0]))) {
			                        importMessage(message, subject[1], user);
			                        lastSyncMessageCount++;
		                    	}
		                    }
                    	}              	
                    } else {
                    	 Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                         log.info(messages.length + " messages found");
                         for (Message message : messages) {
                             importMessage(message, null, user);
                             lastSyncMessageCount++;
                         }
                    }
                    
                    
                    
                   
                } catch (FolderClosedException e) {
                    // Ignore this, we will just continue importing on the next cycle
                } catch (Exception e) {
                    log.error("Error synching the inbox", e);
                    log.error("Erro syncing inbox for " + user.getInboxUserName() + " ~~~~ logged in user " + user.getUsername());
                    lastSyncError = e.getMessage();
                } finally {
                    try {
                        if (inbox != null) {
                            inbox.close(false);
                            inbox.getStore().close();
                        }
                    } catch (Exception e) {
                        // NOP
                    }
                }
        	}
        	
            
        });
    }

    /**
     * Test the inbox configuration.
     *
     * @return Number of messages currently in the remote inbox
     */
    public int testInbox(UserDto user) {
        Boolean enabled = ConfigUtil.getConfigBooleanValue(ConfigType.INBOX_ENABLED);
        if (!enabled) {
            return -1;
        }

        Folder inbox = null;
        try {
            inbox = openInbox(user);
            return inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)).length;
        } catch (Exception e) {
            log.error("Error testing inbox", e);
            return -1;
        } finally {
            try {
                if (inbox != null) {
                    inbox.close(false);
                    inbox.getStore().close();
                }
            } catch (Exception e) {
                // NOP
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
    }

    /**
     * Open the remote inbox.
     *
     * @return Opened inbox folder
     * @throws Exception e
     */
    private Folder openInbox(UserDto user) throws Exception {
        Properties properties = new Properties();
//        String port = ConfigUtil.getConfigStringValue(ConfigType.INBOX_PORT);
//        properties.put("mail.imap.host", ConfigUtil.getConfigStringValue(ConfigType.INBOX_HOSTNAME));
//        properties.put("mail.imap.port", port);
        properties.put("mail.imap.host", user.getInboxHostName());
        boolean isSsl = "993".equals(user.getInboxPort());
        properties.put("mail.imap.ssl.enable", String.valueOf(isSsl));
        properties.setProperty("mail.imap.socketFactory.class",
                isSsl ? "javax.net.ssl.SSLSocketFactory" : "javax.net.DefaultSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "true");
        properties.setProperty("mail.imap.socketFactory.port", user.getInboxPort());
        properties.setProperty("mail.imaps.auth", "true");
     //   properties.setProperty("mail.debug", "true");
        if (isSsl) {
            properties.put("mail.imaps.connectiontimeout", 30000);
            properties.put("mail.imaps.timeout", 30000);
            properties.put("mail.imaps.writetimeout", 30000);
        } else {
            properties.put("mail.imap.connectiontimeout", 30000);
            properties.put("mail.imap.timeout", 30000);
            properties.put("mail.imap.writetimeout", 30000);
        }

        Session session = Session.getInstance(properties);

        Store store = session.getStore("imap");
//        store.connect(ConfigUtil.getConfigStringValue(ConfigType.INBOX_USERNAME),
//                ConfigUtil.getConfigStringValue(ConfigType.INBOX_PASSWORD));
        
        store.connect(user.getInboxUserName(), user.getInboxPassword());

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        return inbox;
    }

    /**
     * Import an email.
     *
     * @param message Message
     * @throws Exception e
     */
    private void importMessage(Message message, String tagName, UserDto user) throws Exception {
        log.info("Importing message: " + message.getSubject());

        // Parse the mail
        EmailUtil.MailContent mailContent = new EmailUtil.MailContent();
        mailContent.setSubject(message.getSubject());
        mailContent.setDate(message.getSentDate());
        EmailUtil.parseMailContent(message, mailContent);
        
        //TODO
        /*
         * This is where we will add the functionality to only import mails with particular subject line 
         * 
         */
        
        
        // Create the document
        Document document = new Document();
        
        //fetch the user with the email linked to this account
//        UserDao userDao = new UserDao();
//        log.debug(message.getFrom().toString() + " ~~~~~ ");
         
       // User user = userDao.getActiveByEmail(ConfigUtil.getConfigStringValue(ConfigType.INBOX_USERNAME));
        if (user != null) {
        	document.setUserId(user.getId());
        } else {
        	document.setUserId("admin");
        }
        
        
        
        if (mailContent.getSubject() == null) {
            document.setTitle("Imported email from EML file");
        } else {
            document.setTitle(StringUtils.abbreviate(mailContent.getSubject(), 100));
        }
        document.setDescription(StringUtils.abbreviate(mailContent.getMessage(), 4000));
        document.setSubject(StringUtils.abbreviate(mailContent.getSubject(), 500));
        document.setFormat("EML");
        document.setSource("Inbox");
        document.setLanguage(ConfigUtil.getConfigStringValue(ConfigType.DEFAULT_LANGUAGE));
        if (mailContent.getDate() == null) {
            document.setCreateDate(new Date());
        } else {
            document.setCreateDate(mailContent.getDate());
        }

        
        //TODO need to know which account is this email importer running on - use the email to find which user is this linkked with ? 
        // Save the document, create the base ACLs
        if (user != null) {
        	DocumentUtil.createDocument(document, user.getId());
        } else {
        	DocumentUtil.createDocument(document, "admin");
        }
        

        // Add the tag
       // String tagId = ConfigUtil.getConfigStringValue(ConfigType.INBOX_TAG);
        
        if (tagName != null) {
            TagDao tagDao = new TagDao();
            TagCriteria criteria = new TagCriteria();
            criteria.setTagName(tagName);
            List<TagDto> tags = tagDao.findByCriteria(criteria, new SortCriteria(0, true));
            if (!tags.isEmpty()) {
            	//TODO only checking the first element in the list 
            	TagDto tag = tags.get(0);
            	if (tag != null) {
                    tagDao.updateTagList(document.getId(), Sets.newHashSet(tag.getId()));
                }
            }
            
            
            
        }

        // Raise a document created event
        DocumentCreatedAsyncEvent documentCreatedAsyncEvent = new DocumentCreatedAsyncEvent();
        documentCreatedAsyncEvent.setUserId("admin");
        documentCreatedAsyncEvent.setDocument(document);
        ThreadLocalContext.get().addAsyncEvent(documentCreatedAsyncEvent);

        // Add files to the document
        for (EmailUtil.FileContent fileContent : mailContent.getFileContentList()) {
            FileUtil.createFile(fileContent.getName(), null, fileContent.getFile(), fileContent.getSize(),
                    document.getLanguage(), "admin", document.getId());
        }
    }

    public Date getLastSyncDate() {
        return lastSyncDate;
    }

    public int getLastSyncMessageCount() {
        return lastSyncMessageCount;
    }

    public String getLastSyncError() {
        return lastSyncError;
    }
}
