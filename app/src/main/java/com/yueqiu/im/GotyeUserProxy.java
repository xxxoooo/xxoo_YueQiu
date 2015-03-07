package com.yueqiu.im;

import com.gotye.api.GotyeUser;

/**
 * Created by doushuqi on 15/2/7.
 */
public class GotyeUserProxy {
    public GotyeUser gotyeUser;
    public String firstChar;

    public GotyeUserProxy(GotyeUser gotyeUser) {
        this.gotyeUser = gotyeUser;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof GotyeUser) {
            GotyeUser user = (GotyeUser) o;
            return user.getName().equals(gotyeUser.getName());
        } else {
            return false;
        }

    }
}
