function DivControl() {
    this.div;
    this.parent;
    this.uiInfo;
    this.header;
    this.expanded = false;

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
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' +
            fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.getHtml = function (header, style, divWidth, divHeight, needLabel) {
        if (!Tools.isNull(divWidth) && !Tools.isNull(divHeight)) {
            style += ";width:" + divWidth + ";height:" + divHeight;
        }

        needLabel = Tools.isNull(needLabel) ? true : needLabel;
        var html = '<div ' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            this.setField('required', header.required) +
            this.setField('enabled', header.enabled) +
            this.setField('class', header.styleClass) +
            this.setField('style', Tools.addString(header.style, style + ";" +
                (header.border ? "border:1px solid #d3d3d3;" : "border:0px;"))) +
            '>' +
            "<div" +
            this.setField('id', header.id + "title") +
            this.setField('name', header.name + "title") +
            this.setField("style", "display:none") +
            ">";
        if (needLabel) {
            html += "<label" +
                this.setField('id', header.id + "label1") +
                this.setField('name', header.name + "label1") +
                this.setField("class", header.titleClass) +
                ">" +
                header.title +
                "</label>" +
                "<label" +
                this.setField('id', header.id + "label2") +
                this.setField('name', header.name + "label2") +
                this.setField("class", header.titleClass) +
                "/>" +
                "<label" +
                this.setField('id', header.id + "label3") +
                this.setField('name', header.name + "label3") +
                this.setField("class", header.titleClass) +
                "/>" +
                "<label" +
                this.setField('id', header.id + "label4") +
                this.setField('name', header.name + "label4") +
                this.setField("class", header.titleClass) +
                "/>";
        }
        html +=
            "</div>" +
            '<img style="width:100%' +
            // Tools.convetPX(divWidth, header.width) + 
            ';height:100%' +
            // Tools.convetPX(divHeight, header.height) + ';display:none"' +
            this.setField('id', header.id + "image") +
            this.setField('name', header.name + "image") +
            '/>' +
            "</div>";

        return html;
    }

    this.initReport = function (header, uiInfo, div) {
        this.div = $(div).find("#" + header.id)[0];
        if (header.border)
            div.style.border = "1px solid " + Tools.convetColor(header.lineColor);
        div.style.background = Tools.convetColor(header.backgroundColor);
        this.uiInfo = uiInfo;
        header.data.expand = false;
        if (Tools.isNull(div.optioins)) {
            div.options = {
                userdata: {
                    data: header.data
                },
                div: this.div,
            };
        }
        this.init(div);
    }

    this.init = function (mcontrol) {
        var header = mcontrol.options["userdata"].data;
        this.header = header;
        this.div = mcontrol.options["div"];
        this.div.style.overflow = header.showScrollbar ? "auto" : "hidden";

        this.div.style["overflow-x"] = header.showHorizontalScrollBar ? "auto" : "hidden";
        this.div.style["overflow-y"] = header.showVerticalScrollBar ? "auto" : "hidden";

        if (Tools.isNull(this.div.options))
            this.div.options = {};
        this.div.options.control = this;
        this.div.options.frameobj = this;

        if (header.expand) {
            $(this.div).mouseup(function (e) {
                if (e.button != 0)
                    return;
                if (Tools.isNull(e.target) || e.target.tagName.toUpperCase() != "DIV")
                    return;

                var control = e.currentTarget.options.control;

                control.expanded ? control.collapsing() : control.expand();
            });

            if (!Tools.isNull(header.collapsing) && header.collapsing) {
                this.collapsing();
            }
        }

        if (header.divType == "dtBarCode" && !Tools.isNull(header.barCode)) {
            this.setBarCode(header.barCode);
        }
    }

    this.setBarCode = function (code) {
        code = encodeURI(code);
        var width = $(this.div).width();
        var height = $(this.div).height();
        $(this.div).qrcode({
            render: "canvas", //table方式 canvas方式
            width: width, //宽度 
            height: height, //高度 
            text: code, //任意内容 
        });
    }

    this.setChildVisiable = function (b) {
        var selector = $(this.div).children();
        b ? selector.show() : selector.hide();
    }

    this.getTitleDiv = function () {
        var magicdiv = this.div.options.magicdiv;
        return $(magicdiv.parent).find("#" + this.header.id + "title");
    }

    this.expand = function () {
        var magicdiv = this.div.options.magicdiv;
        var height = this.div.options.height;
        $(this.div).height(height);
        this.div.style.overflow = header.showScrollbar ? "auto" : "hidden";
        this.div.style["overflow-x"] = header.showHorizontalScrollBar ? "auto" : "hidden";
        this.div.style["overflow-y"] = header.showVerticalScrollBar ? "auto" : "hidden";

        this.setChildVisiable(true);
        $(this.getTitleDiv()).hide();
        magicdiv.relocation(false);
        this.expanded = true;
    }

    this.collapsing = function () {
        this.setChildVisiable(false);
        var magicdiv = this.div.options.magicdiv;
        var height = this.header.expandTitleHeight;
        $(this.getTitleDiv()).show();
        $(this.div).height(height);
        this.div.style.overflow = "hidden";
        this.div.style["overflow-x"] = "hidden";
        this.div.style["overflow-y"] = "hidden";
        magicdiv.relocation(false);
        this.expanded = false;
    }

    this.switchToImage = function () {
        var imageselector = $(this.div).find("img");
        var canvas = $(this.div).find("canvas");
        if (Tools.isNull(canvas[0]))
            return;

        imageselector[0].src = canvas[0].toDataURL();
        canvas.hide();
        imageselector.show();
        return imageselector[0];
    }

    this.switchToDiv = function () {
        var imageselector = $(this.div).find("img");
        var canvas = $(this.div).find("canvas");
        if (Tools.isNull(canvas[0]))
            return;
            
        imageselector.hide();
        canvas.show();
    }

    this.print = function(){
        this.switchToImage();
        Tools.printDiv([$(this.div).parent()], false, Tools.getDpiScaleFromUIInfo(this.uiInfo));
        this.switchToDiv();

    }
}