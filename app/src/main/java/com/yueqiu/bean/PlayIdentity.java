package com.yueqiu.bean;

/**
 * Created by wangyun on 15/1/23.
 */
public class PlayIdentity {
    public String table_id;
    public String type;

    @Override
    public boolean equals(Object o) {
        PlayIdentity identity = (PlayIdentity) o;
        if(this.table_id.equals(identity.table_id) && this.type.equals(identity.type)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result  = 37 * result + table_id.hashCode();
        result  = 37 * result + type.hashCode();
        return result;
    }
}
