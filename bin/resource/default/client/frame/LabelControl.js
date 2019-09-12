function LabelControl() {
    this.id;
    this.fontsize;
    this.fontcolor;
    this.fontname;
    this.fontweight;
    this.fontstyle;
    this.header;
    this.isLabel;
    this.div;

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

    this.getHtml = function (header, width, height, textFontStyle, fontstyle, fontsize, fontcolor, fontname, fontweight) {
        this.fontsize = fontsize;
        this.fontstyle = fontstyle;
        this.fontcolor = fontcolor;
        this.fontname = fontname;
        this.fontweight = fontweight;
        this.header = header;
        this.id = header.id;

        var enabledVar = header.enabled;

        this.isLabel = Tools.isNull(header.autosize) || header.autosize;

        var str = "";
        if (this.isLabel) {
            str = "<label" +
                this.setField("id", header.id) +
                this.setField('name', header.name) +
                this.setField('typecode', header.type) +
                this.setField('typename', header.typename) +
                this.setField('enabled', enabledVar) +
                this.setField('style', Tools.addString(header.style, ";" + textFontStyle)) +
                this.setField('class', header.styleClass) +
                "><nobr>" +
                header.value + "</nobr></label>";
        } else {
            str = "<canvas" +
                this.setField("id", header.id) +
                this.setField('name', header.name) +
                this.setField('width', width) +
                this.setField('height', height) +
                this.setField('typecode', header.type) +
                this.setField('typename', header.typename) +
                this.setField('enabled', enabledVar) +
                this.setField('style', header.style) +
                this.setField('class', header.styleClass) +
                ">" +
                "Your browser does not support the HTML5 canvas tag." +
                "</canvas>";
        }
        if (!Tools.isNull(header.isHref) && header.isHref) {

            if (!Tools.isNull(header.href)) {
                str = "<a" +
                    this.setField("id", header.id + "a") +
                    this.setField('href', header.href) +
                    this.setField('download', header.download) +
                    this.setField('target', header.target) +
                    '>' +
                    str +
                    '</a>';
            } else {
                str = "<a" +
                    this.setField("id", header.id + "a") +
                    this.setField('href', 'Javascript: void(0)') +
                    '>' +
                    str +
                    '</a>';

            }
        }

        return str;
    }

    this.load = function (div) {
        if (Tools.isNull(div))
            div = this.div;
        else
            this.div = div;
        var c = $(div).find("#" + this.id)[0];
        if (Tools.isNull(c.options))
            c.options = {};

        c.options["label"] = this;
        c.options.frameobj = this;
        if (this.isLabel)
            return;

        // $(c).width($(div).width);
        // $(c).height($(div).height);

        var header = this.header;
        var ctx = c.getContext("2d");
        ctx.clearRect(0, 0, c.width, c.height);
        ctx.font = this.fontstyle + " " + header.fontVariant + " " + this.fontweight + " " + this.fontsize + " " + this.fontname;
        // ctx.font = this.fontsize + " " + this.fontname;
        ctx.textBaseline = "top";
        ctx.fillStyle = this.fontcolor;
        var width = c.width;
        // Create gradient
        if (!Tools.isNull(header.gradient)) {
            var gradientInfo = JSON.parse(header.gradient);
            var gradient = ctx.createLinearGradient(0, 0, width, 0);
            for (var index = 0; index < gradientInfo.length; index++) {
                var element = gradientInfo[index];
                gradient.addColorStop(element.index, element.color);
            }
            // Fill with gradient
            ctx.fillStyle = gradient;
        }
        ctx.fillText(header.value, 0, 0, c.width);
    }

    this.uiInfo;
    this.header;
    this.div;
    this.write = function (dw, header, uiInfo, styleWidth, styleHeight, textFontStyle, fontstyle,
        fontsize, textcolor, fontweight) {

        var headerString = this.getHtml(header, styleWidth, styleHeight, textFontStyle, fontstyle,
            fontsize, textcolor, header.fontName, fontweight);
        dw.write(header, headerString);

        this.header = header;
        this.uiInfo = uiInfo;

        this.load(header.tabParent);
        if (header.isHref) {
            var c = $(this.div).find("#" + this.id)[0];
            $(c).css("cursor", "pointer");
            if (!Tools.isNull(header.showLinkLine) && !header.showLinkLine) {
                var a = $(this.div).find("#" + this.id + "a")[0];
                $(a).css("text-decoration", "none");
            }
            if (!header.useHrefColor)
                $(c).css("color", Tools.convetColor(header.textColor));
            else
                $(c).css("color", "#0000FF");
        }
    }
}