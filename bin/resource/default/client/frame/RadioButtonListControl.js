function RadioButtonListControl() {
    this.data;
    this.header;
    this.div;
    this.textFontStyle;

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.getValue = function () {
        return this.header.value;
    }

    this.setValue = function (value) {
        this.header.value = value;
        this.load();
    }

    this.load = function (data) {
        if (Tools.isNull(data))
            data = this.data;

        var control = this.div;
        if (Tools.isNull(control))
            return;
        var items = $.parseJSON(data.data);
        var miniRadio = mini.getByName(data.name, this.div);
        miniRadio.loadData(items);
        $(control).find(".mini-radiobuttonlist-table").attr("cellspacing", 10);
        var selector = $(control).find("label");
        for (let index = 0; index < selector.length; index++) {
            var table = selector[index];
            var style = table.style;
            table.style = style + ";" + data.textFontStyle + ";line-height:" + data.fontSize + "px";
        }

        var table = $(control).children("table");
        table.css({
            width: "100%",
            height: "100%"
        });
        table.find("tr:eq(0) td:eq(0)").attr("align", "center");
    }

    this.getHtml = function(header){
        return '<input ' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('style', Tools.addString(header.style, ";width:100%;height:100%")) +
            this.setField('class', Tools.insertString(" ", header.styleClass, "mini-radiobuttonlist")) +
            // this.setField('width', Tools.convetPX(divWidth, header.width)) +
            // this.setField('height', header.height) +
            this.setField('repeatItems', header.repeatItems, "100") +
            this.setField('repeatDirection', header.repeatDirection, "vertical") +
            this.setField('repeatLayout', header.repeatLayout, "table") +
            this.setField('valueField', header.valueField) +
            this.setField('textField', header.textField) +
            '/>';
    }

    this.init = function(header, textFontStyle){
        this.data = header;
        this.id = header.id;
        this.textFontStyle = textFontStyle;
        mini.parse();

        this.div = header.tabParent;
        var div = this.div;
        if (Tools.isNull(div.options))
            div.options = {};
        div.options.frameobj = this;

        this.load();
    }

    this.write = function (dw, header, textFontStyle) {
        dw.write(header, this.getHtml(header))
        this.init(header, textFontStyle);
    }
}