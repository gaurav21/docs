package com.sismics.docs.core.dao.criteria;

/**
 * User criteria.
 *
 * @author bgamard 
 */
public class UserCriteria {
    /**
     * Search query.
     */
    private String search;
    
    /**
     * Group ID.
     */
    private String groupId;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Username.
     */
    private String userName;
    
    private Integer companyId;

    private Boolean inboxEnabled;

    public UserCriteria setSearch(String search) {
        this.search = search;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public UserCriteria setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserCriteria setUserName(String userName) {
        this.userName = userName;
        return this;
    }
    
    public Integer getCompanyId() {
		return companyId;
	}

	public String getSearch() {
        return search;
    }

	public Boolean getInboxEnabled() {
		return inboxEnabled;
	}

	public void setInboxEnabled(Boolean inboxEnabled) {
		this.inboxEnabled = inboxEnabled;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}
}
