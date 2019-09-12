var ControlHelp = {
    setCurrentEvent: function(controlname, eventName, callback) {
        ControlHelp.setEvent(Global_Current_UIInfo, controlname, eventName, callback);
    },

    /**
     * 设置MiniUI组件的事件
     * @param {*} control 要设置的控件，有getFrameControlBy...系列函数返回
     * @param {*} eventName 要设置的事件名称
     * @param {*} callback 要设置的事件回调函数
     */
    setMiniEvent: function(control, eventName, callback) {
        ControlHelp.setEventForControl(undefined, control, eventName, callback);
    },

    /**
     * 设置MiniUI组件的事件
     * @param {*} uiInfo 要设置控件所在的界面描述信息
     * @param {*} control 要设置的控件，有getFrameControlBy...系列函数返回
     * @param {*} eventName 要设置的事件名称
     * @param {*} callback 要设置的事件回调函数
     */
    setEventForControl: function(uiInfo, control, eventName, callback) {
        if (Tools.isNull(control))
            return;

        if (Tools.isNull(uiInfo) ||
            Tools.isNull(control.options) ||
            Tools.isNull(control.options.magicdiv) ||
            !control.options.magicdiv.Event.hasOwnProperty(eventName)) {
            if (!Tools.isNull(control.options)) {
                for (var key in control.options) {
                    if (!Tools.isNull(control.options[key].Event)) {
                        if (control.options[key].Event.hasOwnProperty(eventName)) {
                            control.options[key].Event[eventName] = callback;
                            return;
                        }
                    }
                }

            }
            var miniControl = mini.get(control.id);
            if (Tools.isNull(miniControl)) {
                alert("未找到id为[" + control.id + "]的控件！");
                return;
            }

            miniControl.un(eventName);
            miniControl.on(eventName, function(e) {
                callback(e, uiInfo, control.name);
            });
        } else {
            control.options.magicdiv.Event[eventName] = callback;
        }
    },

    /**
     * 设置MiniUI组件的事件
     * @param {*} uiInfo 要设置控件所在的界面描述信息
     * @param {*} controlname 要设置的控件name
     * @param {*} eventName 要设置的事件名称
     * @param {*} callback 要设置的事件回调函数
     */
    setEvent: function(uiInfo, controlname, eventName, callback) {
        controlname = controlname.toLowerCase();
        for (var index = 0; index < uiInfo.data.length; index++) {
            var element = uiInfo.data[index];
            if (element.data.name.toLowerCase() == controlname) {
                var control = getFrameControlByName(uiInfo, element.data.name);
                this.setEventForControl(uiInfo, control, eventName, callback);
                return;
            }
        }
    },

    /**
     * 设置主菜单单击事件
     */
    setMenuEvent: function(callback) {
        GlobalFrameManger.Event.onMenuClick = callback;
    },

    /**
     * 设置frame的Tab组件的页面切换事件
     * @param {*} callback 切换的回调事件，函数格式为：function(tabName)
     */
    setOnMainActiveChanged: function(callback) {
        GlobalFrameManger.Event.onMainActiveChanged = callback;
    },

    setOnInitUI: function(callback) {
        GlobalFrameManger.Event.onInitUI = callback;
    },

    setUploadEvent: function(uiInfo, controlname, eventName, callback) {
        controlname = controlname.toLowerCase();
        for (var index = 0; index < uiInfo.data.length; index++) {
            var element = uiInfo.data[index];
            if (element.data.name.toLowerCase() == controlname) {
                var control = getFrameControlByName(uiInfo, element.data.name);
                control.options.upload.Event[eventName] = callback;
                return;
            }
        }
    },

}