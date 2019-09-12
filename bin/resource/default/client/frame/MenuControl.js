function MenuControl() {
    this.menu = undefined;

    this.Event = {
        OnItemClick: undefined,
    }

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

    this.setAlpha = function (el) {
        el.style.background = "rgba(0,0,0,0)";
        $(el).find("div.mini-menu-border").css("background", "rgba(0,0,0,0)");
        $(el).find("div.mini-menu-inner").css("background", "rgba(0,0,0,0)");
    }

    this.write = function (dw, data) {
        var border = "";
        if (!Tools.isNull(data.border) && data.border == "true")
            border = "border:0px";

        var headerString = '<div style=";width:100%;height:100%;padding:5px;' + border + ';"' +
            this.setField('class', Tools.insertString(" ", data.class, "mini-menubar")) +
            this.setField('id', data.id) +
            this.setField('name', data.name) +
            this.setField('textField', data.textField) +
            this.setField('parentField', data.parentField) +
            this.setField('idField', data.idField) +
            '>';
        headerString += '</div>';

        dw.write(data, headerString);

        mini.parse();

        this.menu = mini.get(data.id);
        if (this.menu == undefined)
            return false;

        this.menu.on("itemclick", this.OnNodeClick);

        this.id = data.id;
        if (this.menu.type == "menubar")
            this.type = this.MENUBAR;

        this.menu.options = {};
        this.menu.options["menucontrol"] = this;
        this.menu.options.frameobj = this;
        this.menuData = data.menuitems;
        if (Tools.isNull(this.menuData)) {
            this.menuData = Tools.getMainMenuInfo();
        }
        this.menu.loadList(this.menuData, this.menu.idField, this.menu.parentField);

        var hashData = {};
        for (var i = 0; i < data.menuitems.length; i++) {
            var jsonData = data.menuitems[i];
            hashData[jsonData[this.menu.idField]] = jsonData;
        }
        this.menu.options["menuitemdata"] = hashData;
        this.setAlpha(this.menu.getEl());
        // for (var i = 0; i < data.menuitems.length; i++){
        //     var jsonData = data.menuitems[i];
        //     var obj = mini.get(jsonData[this.menu.idField]);
        //     obj.options = {};
        //     obj.options["json"] = jsonData;
        // }
    }

    this.MENUBAR = 0;
    this.menuData = undefined;

    this.id = undefined;
    this.type = undefined;

    this.OnNodeClick = function (e) {
        this.selectid = undefined;
        var menu = e.sender;
        var mc = menu.options["menucontrol"];
        if (e.isLeaf) {
            if (!Tools.isNull(mc.Event.OnItemClick))
                mc.Event.OnItemClick(mc, menu, e.item, e.isLeaf);
        }

    }

}