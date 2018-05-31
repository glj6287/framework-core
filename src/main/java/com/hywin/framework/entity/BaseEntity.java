package com.hywin.framework.entity;

/**
 * Created by wuyouyang on 2017/4/24.
 */
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

public class BaseEntity implements Serializable
{
    public static final String CREATE_DATE_PROPERTY_NAME = "createTime";
    public static final String MODIFY_DATE_PROPERTY_NAME = "updateTime";

    @Id
    protected String id;
    protected Date createTime;
    protected String createUser;
    protected Date updateTime;
    protected String updateUser;

    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity)obj;
        if ((this.id == null) || (other.getId() == null)) {
            return false;
        }
        return this.id.equals(other.getId());
    }

    public int hashCode()
    {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.id == null ? 0 : this.id.hashCode());
        return result;
    }

    public String getId()
    {
        return this.id;
    }
    public Date getCreateTime() { return this.createTime; }
    public String getCreateUser() { return this.createUser; }
    public Date getUpdateTime() { return this.updateTime; }
    public String getUpdateUser() {
        return this.updateUser;
    }

    public void setId(String id)
    {
        this.id = id;
    }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser;}

    public String toString()
    {
        return "BaseEntity(id=" + getId() + ", createTime=" + getCreateTime() + ", createUser=" + getCreateUser() + ", updateTime=" + getUpdateTime() + ", updateUser=" + getUpdateUser() + ")";
    }
}
