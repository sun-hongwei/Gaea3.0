function ToolBarControl() {
    this.id = undefined;
    this.bar = undefined;
    this.div = undefined;
    this.controlCommon = new ControlCommon();
    this.data = undefined;

    this.items = {};
    this.divs = [];

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

    this.addItem = function (header, uiData) {
        var top = 5;
        var height = $(this.div).height() - 10;
        var left = 0;
        for (var index = 0; index < this.divs.length; index++) {
            var element = this.divs[index];
            var width = $(element).position().left + $(element).width();
            if (left < width)
                left = width;
        }

        var magicDiv = new MagicDiv();
        var divSelector = $(this.div);
        header.data.left = left + header.data.space;
        header.data.top = top;
        header.data.height = height;
        var control = magicDiv.getControl(new DocumentWriter(), divSelector.width(), divSelector.height(), header, uiData, this.div, this.data, null);
        this.items[control.name] = control;
        this.divs.push($("#" + header.data.id + "div")[0]);
    }

    this.createToolbar = function (dw, data) {
        var borderString;
        if (!Tools.isNull(data.border) && data.border == "true") {
            borderString = 'border:1px solid #FF0000';
        } else
            borderString = "border:0px";

        var headerString = '<div style="width:100%;height:100%;' + borderString + ';overflow:hidden"' +
            this.setField('class', Tools.insertString(" ", data.class, "mini-toolbar")) +
            this.setField('id', data.id) +
            this.setField('name', data.name) +
            '>';
        headerString += '</div>';

        dw.write(data, headerString);

        mini.parse();

        this.bar = mini.get(data.id);
        if (this.bar == undefined)
            return false;

        this.id = data.id;
        this.div = this.bar.getEl();

        this.div.options = {
            "toolbarcontrol": this
        };
        this.bar.options = {};
        this.bar.options["toolbarcontrol"] = this;
        this.bar.options.frameobj = this;

    }

    this.load = function (dw, data, uiData) {
        if (Tools.isNull(this.div))
            this.createToolbar(dw, data);
        else
            $(this.div).empty();

        this.data = data;
        this.div = this.bar.getEl();
        this.items = [];
        this.divs = [];

        if (Tools.isNull(data.data))
            return;
        var items = JSON.parse(data.data);
        for (i = 0; i < items.length; i++) {
            var item = items[i];
            this.addItem(item, uiData);
        }

    }

    this.getControl = function (name) {
        return this.items[name];
    }
}