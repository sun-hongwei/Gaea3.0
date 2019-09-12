function TabControl() {
    this.Event = {
        onActiveChanged: undefined
    }
    this.id = undefined;

    this.tabControl = undefined;

    this.getValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.addTab = function (tabInfo) {
        var tabs = mini.get(this.id);
        var tab = tabs.getTab(tabInfo.name);
        exists = true;
        if (Tools.isNull(tab)) {
            exists = false;
            tabInfo._nodeid = tabInfo.name;
            tabs.addTab(tabInfo);
            mini.parse();
            tab = tabs.getTab(tabInfo.name);
            tab.showCloseButton = tabInfo.showCloseButton;
        }
        tabs.activeTab(tab);
        return {
            "tab": tab,
            "exists": exists
        }; //splitter.getTabBodyEl(tabInfo);
    }

    this.setScrollBar = function (tab, showHorizontalScrollBar, showVerticalScrollBar) {
        var el = this.getBodyForTab(tab);
        if (Tools.isNull(showHorizontalScrollBar))
            $(el).css("overflow-x", showHorizontalScrollBar ? "auto" : "hidden");
        if (Tools.isNull(showVerticalScrollBar))
            $(el).css("overflow-y", showVerticalScrollBar ? "auto" : "hidden");
    }

    this.getBodyForTab = function (tab) {
        var tabs = mini.get(this.id);
        return tabs.getTabBodyEl(tab);
    }

    this.getBodyForCurrent = function (tabInfo, nullAndCreate) {
        var tabs = mini.get(this.id);
        var tab = tabs.getActiveTab();
        if (Tools.isNull(tab)) {
            if (Tools.isNull(nullAndCreate) || nullAndCreate)
                return this.addTab(tabInfo);
            else
                return undefined;
        } else {
            if (!Tools.isNull(tabInfo.title))
                tabs.updateTab(tab, {
                    title: tabInfo.title
                })
            return tabs.getTabBodyEl(tab);
        }
    }

    this.getHtml = function (data) {
        var headerString = '<div style="border:0px;width:100%;height:100%;" plain="true" ' +
            this.setField('id', data.id) +
            this.setField('class', Tools.insertString(" ", data.styleClass, "mini-tabs")) +
            '>' +
            '</div>';
        return headerString;
    }

    this.init = function (id) {
        mini.parse();

        this.id = id;
        this.tabControl = mini.get(id);

        if (Tools.isNull(this.tabControl))
            return;

        this.tabControl.on("activechanged", this.onActiveChanged);
        this.tabControl.options = {};
        this.tabControl.options["TabControl"] = this;
        this.tabControl.options.frameobj = this;

    }

    this.write = function (dw, data) {
        var headerString = this.getHtml(data);
        dw.write(data, headerString);
        this.init(data.id)
    }

    this.onActiveChanged = function (e) {
        var tabs = e.sender;
        var tab = tabs.getActiveTab();
        if (tab && tab.name) {
            var tabcontrol = tabs.options["TabControl"];
            if (!Tools.isNull(tabcontrol.Event.onActiveChanged)) {
                tabcontrol.Event.onActiveChanged(tab.name);
            }
        }
    }


}