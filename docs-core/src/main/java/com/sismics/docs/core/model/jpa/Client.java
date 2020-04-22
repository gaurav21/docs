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
@Table(name = "T_Client")
public class Client implements Loggable {

	/**
     * User ID.
     */
    @Id
    @Column(name = "USE_ID_C", length = 36)
    private String id;
    
    /**
     * Role ID.
     */
    @Column(name = "USE_IDROLE_C", nullable = false, length = 36)
    private String roleId;
    
    /**
     * User's username.
     */
    @Column(name = "USE_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password.
     */
    @Column(name = "USE_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * User's private key.
     */
    @Column(name = "USE_PRIVATEKEY_C", nullable = false, length = 100)
    private String privateKey;

    /**
     * False when the user passed the onboarding.
     */
    @Column(name = "USE_ONBOARDING_B", nullable = false)
    private boolean onboarding;

    /**
     * TOTP secret key.
     */
    @Column(name = "USE_TOTPKEY_C", length = 100)
    private String totpKey;
    
    /**
     * Email address.
     */
    @Column(name = "USE_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    
    /**
     * Creation date.
     */
    @Column(name = "USE_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "USE_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Disable date.
     */
    @Column(name = "USE_DISABLEDATE_D")
    private Date disableDate;
    
    
    @Column(name = "USE_MOB_NUMBER_C")
  //  @Pattern(regexp="(^$|[0-9]{10})")
    private String mobileNumber;
   
    @Column(name = "USE_ADDRESS_C")
    private String Address;
    
    @Column(name = "USE_PINCODE_C")
    private Integer pincode;

	@Override
	public String toMessage() {
		return username;
	}

	@Override
	public Date getDeleteDate() {
		return deleteDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public boolean isOnboarding() {
		return onboarding;
	}

	public void setOnboarding(boolean onboarding) {
		this.onboarding = onboarding;
	}

	public String getTotpKey() {
		return totpKey;
	}

	public void setTotpKey(String totpKey) {
		this.totpKey = totpKey;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getDisableDate() {
		return disableDate;
	}

	public void setDisableDate(Date disableDate) {
		this.disableDate = disableDate;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}

	public Integer getPincode() {
		return pincode;
	}

	public void setPincode(Integer pincode) {
		this.pincode = pincode;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
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
