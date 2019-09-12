var GlobalUserLogin = {
    USER_DATA_ROLE_KEY: "USER_DATA_ROLE_KEY",
    USER_FUNCTION_ROLE_KEY: "USER_FUNCTION_ROLE_KEY",

    USE_PHP: "php",
    USE_TOMCAT: "tom",

    CUR_ENV: "tom",

    notifies: [],

    /**
     * 设置登录通知，当用户登录成功时，会自动调用用户注册的回调事件
     * @param {string} id 用户自定义id，当回调时作为参数
     * @param {callback} callback 用户回调过程，格式：function(id, isOk, localStacks)
     * isOk：是否登录成功，总为true，仅成功调用；localStacks：本地延迟调用栈，为CallbackStack对象，如果当前必须等待其他对象
     * 初始完毕，可以在此对象中注册一个回调，格式与callback同
     */
    registerNotify : function (id, callback) {
        var callbackInfo = {
            id: id,
            callback: callback
        };
        GlobalUserLogin.notifies.push(callbackInfo);
    },

    /**
     * 取消回调函数注册
     * @param {callback} callback 使用registerNotify函数注册的callback对象
     */
    unRegisteNotify : function (callback) {
        for (var index = 0; index < GlobalUserLogin.notifies.length; index++) {
            var element = GlobalUserLogin.notifies[index];
            if (callback == element.callback) {
                GlobalUserLogin.notifies.splice(index, 1);
                return;
            }
        }
    },

    fireNotify : function (isok) {
        var stacks = new CallbackStack();
        for (var index = 0; index < GlobalUserLogin.notifies.length; index++) {
            var element = GlobalUserLogin.notifies[index];
            element.callback(element.id, isok, stacks);
        }

        stacks.while(isok, function(callbackInfo, isOk, localStacks){
            callbackInfo.callback(callbackInfo.id, isOk, localStacks);
        });
    },

    initGlobalInfo: function (user, sessionid, sessioninfo, roleInfo) {
        var metaInfo = Tools.getMetaFile();
        GlobalSessionObject.setMetaInfo(metaInfo);
        GlobalSessionObject.setSessionID(sessionid);
        GlobalSessionObject.setUserId(user.userid);
        GlobalSessionObject.setUserName(user.username);
        GlobalSessionObject.setSessionInfo(sessioninfo);
        GlobalSessionObject.setUserRole(roleInfo);
        if (!Tools.isNull(user.supermenu)) {
            GlobalSessionObject.setSuperMenu(user.supermenu);
        } else {
            GlobalSessionObject.setSuperMenu(false);
        }
        if (!Tools.isNull(user.superbutton)) {
            GlobalSessionObject.setSuperButton(user.superbutton);
        } else {
            GlobalSessionObject.setSuperButton(false);
        }
        if (!Tools.isNull(user.superview)) {
            GlobalSessionObject.setSuperView(user.superview);
        } else {
            GlobalSessionObject.setSuperView(false);
        }

        GlobalUserLogin.fireNotify(true);
    },

    doTomcatOk: function (info) {
        GlobalUserLogin.initGlobalInfo(info.data.roleInfo.US,
            info.data.sessionid, info.data.sessioninfo, info.data.roleInfo);
    },

    doPhpOk: function (data) {
        GlobalUserLogin.initGlobalInfo(info.data,
            info.data["sid"], info.data["sinfo"], {});
    },

    sendTomcatRefreshRoles: function (callback) {
        Tools.simpleTomcatSubmit("/ex/user/refreshroles", {},
            function (data) {
                var isok = data.ret == 0;
                if (isok)
                    GlobalUserLogin.doTomcatOk(data);
                callback(isok);
            });

    },

    sendTomcatLogin: function (userid, pwd, dynamic_code, client_token, callback) {
        Tools.simpleTomcatSubmit("/ex/user/login", {
                userid: userid,
                pwd: pwd,
                dynamic_code: dynamic_code,
                client_token: client_token,
                host: ""
            },
            function (data) {
                var isok = data.ret == 0;
                if (isok)
                    GlobalUserLogin.doTomcatOk(data);
                callback(isok);
            });

    },

    sendPhpLogin: function (userid, password, dynamic_token_code, clientToken, callback) {
        Tools.ajaxSubmitDispatch("", "dynamic_token_code", "mainlogin", {
                "userid": userid,
                "password": password,
                "dynamic_token_code": dynamic_token_code,
                "client_token": clientToken,
                "ip": ""
            },
            "login",
            function (data) {
                var info = mini.decode(data);
                result = info["ret"];
                if (result)
                    GlobalUserLogin.doPhpOk(info);
                callback(result);
            }
        );

    },

    login: function (userid, password, dynamic_code, client_Token, callback) {
        GlobalSessionObject.clear();
        if (userid == "") {
            mini.alert("请输入用户名!");
            return false;
        }

        if (password == "") {
            mini.alert("请输入用户密码!");
            return false;
        }

        password = hex_md5(password);
        var result = false;

        if (GlobalUserLogin.CUR_ENV == GlobalUserLogin.USE_PHP) {
            GlobalUserLogin.sendPhpLogin(userid, password, dynamic_code, client_Token, callback);
        } else if (GlobalUserLogin.CUR_ENV == GlobalUserLogin.USE_TOMCAT) {
            GlobalUserLogin.sendTomcatLogin(userid, password, dynamic_code, client_Token, callback);
        }
    },

    refreshRoles: function (callback) {
        if (GlobalUserLogin.CUR_ENV == GlobalUserLogin.USE_PHP) {} else if (GlobalUserLogin.CUR_ENV == GlobalUserLogin.USE_TOMCAT) {
            GlobalUserLogin.sendTomcatRefreshRoles(callback);
        }
    },

    RelocationMainPage: function () {
        window.location.href = 'index.html';
    }
}