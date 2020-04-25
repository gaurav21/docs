package com.sismics.docs.core.dao.dto;

import com.google.common.base.MoreObjects;

/**
 * User DTO.
 *
 * @author jtremeaux 
 */
public class UserDto {
    /**
     * User ID.
     */
    private String id;
    
    /**
     * Username.
     */
    private String username;
    
    /**
     * Email address.
     */
    private String email;
    
    /**
     * Creation date of this user.
     */
    private Long createTimestamp;

    /**
     * Disable date of this user.
     */
    private Long disableTimestamp;

    /**
     * Storage quota.
     */
    private Long storageQuota;
    
    /**
     * Storage current usage.
     */
    private Long storageCurrent;

    /**
     * TOTP key.
     */
    private String totpKey;
    
    
    private Boolean inboxEnabled;

    private String inboxUserName;

	private String inboxPassword;
    
    private String inboxPort;
    
    private String inboxHostName;
    
    private String tag;
    
    private Integer companyId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }
    
    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getDisableTimestamp() {
        return disableTimestamp;
    }

    public UserDto setDisableTimestamp(Long disableTimestamp) {
        this.disableTimestamp = disableTimestamp;
        return this;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
    }

    public Long getStorageCurrent() {
        return storageCurrent;
    }

    public void setStorageCurrent(Long storageCurrent) {
        this.storageCurrent = storageCurrent;
    }

    public String getTotpKey() {
        return totpKey;
    }

    public void setTotpKey(String totpKey) {
        this.totpKey = totpKey;
    }
    
    public Boolean getInboxEnabled() {
		return inboxEnabled;
	}

	public void setInboxEnabled(Boolean inboxEnabled) {
		this.inboxEnabled = inboxEnabled;
	}

	public String getInboxUserName() {
		return inboxUserName;
	}

	public void setInboxUserName(String inboxUserName) {
		this.inboxUserName = inboxUserName;
	}

	public String getInboxPassword() {
		return inboxPassword;
	}

	public void setInboxPassword(String inboxPassword) {
		this.inboxPassword = inboxPassword;
	}

	public String getInboxPort() {
		return inboxPort;
	}

	public void setInboxPort(String inboxPort) {
		this.inboxPort = inboxPort;
	}

	public String getInboxHostName() {
		return inboxHostName;
	}

	public void setInboxHostName(String inboxHostName) {
		this.inboxHostName = inboxHostName;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .toString();
    }
}
