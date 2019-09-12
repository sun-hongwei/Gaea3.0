function SplitterControl() {
    this.Event = {
        onActiveChanged: undefined
    };

    this.hasTab = false;
    this.splitter = undefined;
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

    this.write = function (dw, data) {
        var headerString = '<div style="width:100%;height:100%;" borderStyle="border:0;"' +
            this.setField('class', Tools.insertString(" ", data.styleClass, "mini-splitter")) +
            this.setField('id', data.id) +
            this.setField('name', data.name) +
            '>' +
            '<div style="border-width:1px;"' +
            this.setField('id', data.leftid) +
            this.setField('size', data.leftsize, "180") +
            this.setField('maxSize', data.leftmaxSize, "260") +
            this.setField('minSize', data.leftminSize, "100") +
            this.setField('showCollapseButton', data.showCollapseButton, "true") +
            '>' +
            '</div>' +
            '<div showCollapseButton="false" style="border:0px;" >';
        this.hasTab = !Tools.isNull(data.rightTab);
        if (this.hasTab) {
            this.tabControl = new TabControl();
            tabStr = this.tabControl.getHtml({
                id: data.rightTab.tabid
            });
            headerString += tabStr;
        }
        headerString += '</div>';
        headerString += '</div>';

        dw.write(data, headerString);

        mini.parse();

        this.id = data.id;
        this.splitter = mini.getByName(data.name, data.tabParent);

        if (Tools.isNull(this.splitter))
            return;

        this.splitter.options = {};
        this.splitter.options.frameobj = this;

        if (this.hasTab) {
            this.tabControl.Event.onActiveChanged = this.Event.onActiveChanged;
            this.tabControl.init(data.rightTab.tabid);
        }

    }

    this.onActiveChanged = function (e) {
        var tabs = e.sender;
        var tab = tabs.getActiveTab();
        if (tab && tab.name) {
            splitter = tabs.options["frameobj"];
            if (!Tools.isNull(splitter.Event.onActiveChanged)) {
                splitter.Event.onActiveChanged(tab.name);
            }
        }
    }


}