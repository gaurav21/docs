/**
 * 
 */
package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * @author gaurav
 *
 */
@Entity
@Table(name = "T_COMPANY")
public class Company implements Loggable {
	
	/**
     * User ID.
     */
    @Id
    @Column(name = "COM_ID_C", length = 36)
    private String id;
    
    
    /**
     * User's username.
     */
    @Column(name = "COM_NAME_C", nullable = false, length = 50)
    private String name;
    
    /**
     * Creation date.
     */
    @Column(name = "COM_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "COM_DELETEDATE_D")
    private Date deleteDate;

	@Override
	public String toMessage() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	@Override
	public Date getDeleteDate() {
		return deleteDate;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
	}

}
