package com.yueqiu.bean;

/**
 * Created by wangyun on 15/1/22.
 */
public class Identity{
    public int user_id;
    public String table_id;
    public int type;

    @Override
    public boolean equals(Object o) {
        Identity identity = (Identity) o;
        if(this.table_id.equals(identity.table_id) && this.type == identity.type
                && this.user_id == identity.user_id){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result  = 37 * result + table_id.hashCode();
        result  = 37 * result + type;
        result  = 37 * result + user_id;
        return result;
    }
}
