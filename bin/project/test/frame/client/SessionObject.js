function SessionStorageObject() {
    this.setItem = function (key, value) {
        sessionStorage.setItem(key, value);
    }

    this.getItem = function (key) {
        return sessionStorage.getItem(key);
    }

    this.removeItem = function (key) {
        sessionStorage.removeItem(key);
    }

    this.clearItem = function () {
        sessionStorage.clear();
    }
}

var GlobalSessionObject = {
    TREE_ROLE: "TR",
    UI_ROLE: "UI",
    BUTTON_ROLE: "BU",
    MENU_ROLE: "ME",
    GROUP_ROLE: "GP",
    SUPER_VIEW: "SV",
    SUPER_MENU: "SM",
    SUPER_BUTTON: "SB",

    SuperButton: "SuperButton",
    SuperMenu: "SuperMenu",
    SuperView: "SuperView",
    MetaInfo: "MetaInfo",
    UserRole: "UserRole",
    UserId: "UserId",
    UserName: "UserName",
    SessionId: "SessionId",
    SessionInfo: "SessionInfo",
    ClientUniqueId: "ClientUniqueId",

    localStorage: new SessionStorageObject(),

    setValue: function (key, value) {
        // GlobalCookies.setCookie(key, value);
        if (Tools.isObject(value))
            value = JSON.stringify(value);
        GlobalSessionObject.localStorage.setItem(key, value);
    },

    getValue: function (key) {
        // return GlobalCookies.getCookie(key);
        return GlobalSessionObject.localStorage.getItem(key);
    },

    removeValue: function (key) {
        // GlobalCookies.removeCookie(key);
        GlobalSessionObject.localStorage.removeItem(key);
    },

    clear: function () {
        GlobalSessionObject.localStorage.clearItem();
    },

    getSuperButton: function () {
        return GlobalSessionObject.getValue(GlobalSessionObject.SuperButton) == "true";
    },

    setSuperButton: function (isSuper) {
        GlobalSessionObject.setValue(GlobalSessionObject.SuperButton, isSuper ? "true" : "false");
    },

    getSuperMenu: function () {
        return GlobalSessionObject.getValue(GlobalSessionObject.SuperMenu) == "true";
    },

    setSuperMenu: function (isSuper) {
        GlobalSessionObject.setValue(GlobalSessionObject.SuperMenu, isSuper ? "true" : "false");
    },

    getSuperView: function () {
        return GlobalSessionObject.getValue(GlobalSessionObject.SuperView) == "true";
    },

    setSuperView: function (isSuper) {
        GlobalSessionObject.setValue(GlobalSessionObject.SuperView, isSuper ? "true" : "false");
    },

    setMetaInfo: function (metaInfo) {
        GlobalSessionObject.setValue(GlobalSessionObject.MetaInfo, metaInfo);
    },

    getMetaInfo: function () {
        return GlobalSessionObject.getValue(GlobalSessionObject.MetaInfo);
    },

    getNavRoles: function () {
        return getUserRole(GlobalSessionObject.TREE_ROLE);
    },

    getMenuRoles: function () {
        return getUserRole(GlobalSessionObject.MENU_ROLE);
    },

    getButtonRoles: function () {
        return getUserRole(GlobalSessionObject.BUTTON_ROLE);
    },

    getViewRoles: function () {
        return getUserRole(GlobalSessionObject.UI_ROLE);
    },

    checkControlRoles: function (uiInfo, controls, roles, callback, isButton) {
        if (GlobalSessionObject.getSuperButton())
            return;

        var allPass = Tools.isNull(roles) || $.isEmptyObject(roles);
        if (allPass)
            return;

        var names = Object.keys(controls);
        for (var index = 0; index < names.length; index++) {
            var name = names[index];
            var element = controls[name];
            if (Tools.isNull(element) || Tools.isNull(element.element))
                continue;
            var control = getFrameControlByName(uiInfo, name);
            if (Tools.isNull(control))
                continue;

            if (!roles.hasOwnProperty(uiInfo.workflow.id + "." + name))
                callback(control, element, isButton);
        }
    },

    checkButtonRoles: function (uiInfo, callback) {
        if (GlobalSessionObject.getSuperButton())
            return;

        var roles = GlobalSessionObject.getUserRole(GlobalSessionObject.BUTTON_ROLE);
        GlobalSessionObject.checkControlRoles(uiInfo, uiInfo.buttons, roles, callback, true);
        GlobalSessionObject.checkControlRoles(uiInfo, uiInfo.labels, roles, callback, false);
    },

    checkUIRole: function (uiInfo, callback) {
        if (GlobalSessionObject.getSuperView())
            return;

        var roles = GlobalSessionObject.getUserRole(GlobalSessionObject.UI_ROLE);
        var allDeny = Tools.isNull(roles);
        var isok = roles.hasOwnProperty(uiInfo.id);
        callback(allDeny | isok, uiInfo);
    },

    /**
     * 删除deleteId指定的节点及其所有子节点
     * @param {string} deleteId 要删除的节点id
     * @param {object} maps 所有节点组成的树列表
     * @param {object} alls 所有节点列表map，key为element.id
     */
    deleteChilds: function (deleteId, maps, alls) {
        if (maps.hasOwnProperty(deleteId)) {
            var list = maps[deleteId];
            for (var key in list) {
                if (list.hasOwnProperty(key)) {
                    var element = list[key];
                    if (!Tools.isNull(element.id))
                        GlobalSessionObject.deleteChilds(element.id, maps, alls);
                    delete alls[element.id];
                }
            }
            delete maps[deleteId];
        }
        delete alls[deleteId];
    },

    /**
     * 获取一个节点的所有父亲节点列表，包括自己
     * @param {*} element 需要获取的节点对象
     * @param {*} alls 包含所有节点的map，key为element.id
     * @param {*} result 包含所有父亲节点的返回结果map，key为element.id
     */
    getElementParents: function (element, alls, result) {
        var id;
        var pid;
        if (element.hasOwnProperty("roleid")) {
            id = element.roleid;
            pid = element.rolepid;
        } else {
            id = element.id;
            pid = element.pid;
        }
        result[id] = alls[id];
        if (!Tools.isNull(pid) && alls.hasOwnProperty(pid)) {
            GlobalSessionObject.getElementParents(alls[pid], alls, result);
        }
    },

    /**
     * 获取真正的权限节点列表，由于一个权限节点可能是某个节点的子节点，那么此节点的父节点也
     * 不可以删除，因此需要通过这个函数获取真正不能删除的节点列表
     * @param {*} roles 权限列表
     * @param {*} alls 树中所有节点的map，以节点id为索引
     * @return {*} 返回真正的权限节点列表
     */
    getRealTreeRoles: function (roles, alls) {
        var result = {};
        for (var key in roles) {
            if (roles.hasOwnProperty(key)) {
                var element = roles[key];
                GlobalSessionObject.getElementParents(element, alls, result);
            }
        }

        return result;
    },

    /**
     * 删除树中不符合权限的节点
     * @param {*} data 导航树控件的list对象
     * @param {*} roles 导航权限对象
     * @param {*} allDeny 是否拒绝所有权限设置
     * @return {*} 返回已经删除所有不符合权限设置的对象列表。
     */
    deleteTreeDatas: function (data, roles, allDeny) {
        if (allDeny)
            return {};

        var deletes = [];
        var alls = {};
        var maps = {};
        for (var index = data.length - 1; index >= 0; index--) {
            var element = data[index];
            alls[element.id] = element;
        }

        roles = GlobalSessionObject.getRealTreeRoles(roles, alls);
        for (var index = data.length - 1; index >= 0; index--) {
            var element = data[index];
            if (!roles.hasOwnProperty(element.id)) {
                deletes.push(element);
            }
            var list;
            var id = element.pid;
            var isRoot = Tools.isNull(id);
            if (isRoot)
                id = element.id;

            if (maps.hasOwnProperty(id)) {
                list = maps[id];
            } else {
                list = {};
                maps[id] = list;
            }
            if (!isRoot)
                list[element.id] = element;
        }

        for (var index = 0; index < deletes.length; index++) {
            var element = deletes[index];
            GlobalSessionObject.deleteChilds(element.id, maps, alls);
        }

        var result = [];
        var names = Object.keys(alls);
        for (var index = 0; index < names.length; index++) {
            var name = names[index];
            result.push(alls[name]);
        }

        return result;
    },

    checkMainMenuRole: function (uiInfo) {
        if (GlobalSessionObject.getSuperMenu())
            return;

        var roles = GlobalSessionObject.getUserRole(GlobalSessionObject.MENU_ROLE);
        var allDeny = Tools.isNull(roles);

        var menuIteminfo = Tools.getMainMenuInfo();
        if (Tools.isNull(menuIteminfo))
            return;

        if (Tools.isNull(uiInfo.menus))
            return;

        var names = Object.keys(uiInfo.menus);
        for (var index = 0; index < names.length; index++) {
            var menuName = names[index];
            var control = getFrameControlByName(uiInfo, menuName);
            if (Tools.isNull(control))
                continue;

            var result = GlobalSessionObject.deleteTreeDatas(menuIteminfo, roles, allDeny);
            control.loadList(result, control.idField, control.parentField);
        }
    },

    checkMainNavRole: function (uiInfo, callback) {
        if (GlobalSessionObject.getSuperMenu())
            return;

        var roles = GlobalSessionObject.getUserRole(GlobalSessionObject.TREE_ROLE);
        var allDeny = Tools.isNull(roles);

        var metaInfo = GlobalSessionObject.getMetaInfo();
        if (Tools.isNull(metaInfo))
            return;

        metaInfo = JSON.parse(metaInfo);
        var uiid = metaInfo.maintree.uiid;
        var controlName = metaInfo.maintree.cname;
        if (!uiInfo.trees.hasOwnProperty(controlName)) {
            if (!uiInfo.mainTrees.hasOwnProperty(controlName))
                return;
        }

        var control = getFrameControlByName(uiInfo, controlName);
        if (Tools.isNull(control))
            return;

        var dataList = control.getList();
        for (var index = 0; index < control.data.length; index++) {
            var element = control.data[index];
            dataList.push(element);
        }
        var data = GlobalSessionObject.deleteTreeDatas(dataList, roles, allDeny);
        control.loadList(data, control.idField, control.parentField);
    },

    getGroupRole: function () {
        var groups = GlobalSessionObject.getUserRole(GlobalSessionObject.GROUP_ROLE);
        if (Tools.isNull(groups))
            return {};
        else {
            var result = {};
            for (var gid in groups) {
                result[gid] = gid;
            }
            return result;
        }
    },

    getUserRole: function (roleName) {
        var userrole = GlobalSessionObject.getValue(GlobalSessionObject.UserRole);
        if (Tools.isNull(userrole))
            userrole = {};
        else
            userrole = JSON.parse(userrole);

        var role = userrole[roleName];
        if (Tools.isNull(role))
            return {};
        else
            return role;
    },

    setUserRole: function (userrole) {
        GlobalSessionObject.setValue(GlobalSessionObject.UserRole, userrole);
    },

    getSessionID: function () {
        var id = GlobalSessionObject.getValue(GlobalSessionObject.SessionId);
        if (Tools.isNull(id))
            id = "";
        return id;
    },

    setSessionID: function (sid) {
        GlobalSessionObject.setValue(GlobalSessionObject.SessionId, sid);
    },

    getSessionInfo: function () {
        return unescape(GlobalSessionObject.getValue(GlobalSessionObject.SessionInfo));
    },

    setSessionInfo: function (siinfo) {
        siinfo = escape(siinfo);
        GlobalSessionObject.setValue(GlobalSessionObject.SessionInfo, siinfo);
    },

    setUserId: function (userid) {
        userid = escape(userid);
        GlobalSessionObject.setValue(GlobalSessionObject.UserId, userid);
        //return this.GetSessionInfo().split(",")[6];
    },
    getUserId: function () {
        return unescape(GlobalSessionObject.getValue(GlobalSessionObject.UserId));
    },

    getUserName: function () {
        return unescape(GlobalSessionObject.getValue(GlobalSessionObject.UserName));
    },

    setUserName: function (username) {
        username = escape(username);
        GlobalSessionObject.setValue(GlobalSessionObject.UserName, username);
    },

    getUnique: function () {
        var uni = GlobalSessionObject.getValue(GlobalSessionObject.ClientUniqueId);
        if (Tools.isNull(uni)) {
            uni = new UUID().createUUID();
            GlobalSessionObject.setValue(GlobalSessionObject.ClientUniqueId, uni);
        }
        return uni;
    },

    clear: function () {
        GlobalSessionObject.localStorage.clearItem();
    }
}