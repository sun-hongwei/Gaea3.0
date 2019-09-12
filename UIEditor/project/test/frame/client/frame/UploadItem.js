function UploadItemGlobalOnHrefClick(e) {
    var uploadControl = e.options["uploadcontrol"];
    var uploadItem = e.options["object"];
    uploadControl.remove(uploadItem);
}

function GlobalUploadOnMouseOver(e) {
    var uploadControl = e.options["uploadcontrol"];
    var uploadItem = e.options["object"];
    Tools.showHintForDiv($("#" + uploadItem.id + "_label")[0], uploadItem.filename);
}

function GlobalUploadItemOnMouseOut(e) {
    var uploadControl = e.options["uploadcontrol"];
    var uploadItem = e.options["object"];
    Tools.hideHint();
}

function UploadItem() {
    this.div;
    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.getImage = function (filename) {
        var ext = Tools.getFileExt(filename);
        ext = ext.toLowerCase();
        switch (ext) {
            case "png":
            case "jpg":
            case "jpeg":
            case "bmp":
            case "gif":
                return "frameimage/fxi/image.png";
            case "pdf":
                return "frameimage/fxi/pdf.png";
            case "zip":
            case "rar":
                return "frameimage/fxi/zip.png";
            case "doc":
            case "docx":
                return "frameimage/fxi/word.png";
            case "ppt":
            case "pptx":
                return "frameimage/fxi/ppt.png";
            case "xls":
            case "xlsx":
            case "csv":
                return "frameimage/fxi/excel.png";
            case "mpg":
            case "mpeg":
            case "avi":
            case "mp4":
            case "rm":
            case "rmvb":
            case "wmv":
                return "frameimage/fxi/video.png";
            case "mp3":
            case "wav":
            case "wma":
                return "frameimage/fxi/audio.png";
            default:
                return "frameimage/fxi/other.png";
        }
    }

    this.getFont = function (fontName, fontSize, fontstyle, textColor) {
        var fontstyle = "";
        switch (fontStyle) {
            case 2:
                fontstyle = "italic";
                break;
            case 3:
                fontstyle = "oblique";
                break;
            default:
            case 0:
                fontstyle = "normal";
                break;

        }

        var fontweight = fontStyle == "1" || fontStyle == "3" ? "bold" : "normal";
        var textFontStyleNoHeight = ";font-family:'" + fontName + "';font-style:" +
            fontStyle + ";font-weight:" + fontweight + ";font-size:" + Tools.convetFontSize(fontSize);
        if (!Tools.isNull(textColor))
            textFontStyleNoHeight += ";color:" + Tools.convetColor(textColor);
        return textFontStyleNoHeight;
    }

    this.getFontLineHeight = function (height) {
        return "line-height:" + height;
    }

    this.setPosition = function (position, title, hint) {
        $(this.div).find("#" + this.id + "_title").text(title);
        $(this.div).find("#" + this.id + "_hint").text(hint);
        $(this.div).find("#" + this.id + "_position").css("width", position + "%");
    }

    this.setUploadEnd = function (sizeTitle) {
        $(this.div).find("#" + this.id + "_uploding").hide();
        var endItem = $("#" + this.id + "_uploadEnd");
        endItem.html("<p>" + sizeTitle + " 上传完成！</p>");
        endItem.show();

        if (!Tools.isNull(this.uploadControl.Event.onUploadEnd))
            this.uploadControl.Event.onUploadEnd(this, this.filename);
    }

    this.removeSelf = function () {
        $(this.div).find("#" + this.id).remove();
        setTimeout(function (uploader) {
            uploader.remove();
        }, 100, this.uploader);
    }

    this.id = "";
    this.filename = "";
    this.uploader;
    this.uploadControl;

    this.createItem = function (dw, div, id, rect, filename, uploadcontrol) {
        this.div = div;
        this.filename = filename;
        this.uploadControl = uploadcontrol;
        id = hex_md5(id);
        this.id = id;

        var imageFileName = this.getImage(filename);

        var style = ";font-family:'Microsoft Yahei',verdana;line-height: 1.666;width:" + rect.width + "px;height:" + rect.height +
            "px;float:left;position:relative;margin:0 8px 8px 0;border-radius:3px;overflow:visible;font-size:12px;";

        var divStr = "<div" +
            this.setField("id", id) +
            this.setField("style", style) +
            ">";

        divStr += "<b id='" + id + "_icon' style='background-image: url(\"" + imageFileName +
            "\");background-repeat: no-repeat;margin: 7px 0 0 4px;width: 32px;height: 32px;display: inline-block;'>" +
            "</b>";

        divStr += "<div id='" + id + "_label' onmouseover='GlobalUploadOnMouseOver(this)' onmouseout='GlobalUploadItemOnMouseOut(this)' style='width: 200px;height: 16px;" +
            "white-space: nowrap;overflow: hidden;text-overflow: ellipsis;color: #666;position: absolute;width:10px;left: 41px;top: 4px;font-size: 12px;'>" +
            Tools.getFileName(filename) +
            "</div>";

        divStr += "<div style='position: absolute;right: 6px;top: 5px;font-size: 12px;'>";
        divStr += "<a" +
            this.setField("id", id + "_delete") +
            this.setField("style", "outline: 0;cursor: pointer;padding: 2px 4px 4px;+padding:3px 4px: ;text-decoration: none;border-radius: 3px;color: #004986 !important;") +
            this.setField('href', "javascript:void(0)") +
            this.setField('onclick', "UploadItemGlobalOnHrefClick(this)") +
            '>' + "删除" + "</a>";
        divStr += "</div>";

        divStr += "<div id='" + id + "_uploding' style='height: 20px;overflow: hidden;zoom: 1;display: inline-block;white-space: nowrap;" +
            "text-overflow: ellipsis;color: #b0b0b0;position: absolute;left: 41px;top: 21px;line-height: 160%;font-size: 12px;'>";
        divStr += "<span style='float: left;margin: 5px 0 0;-margin-right: -3px;position: relative;width: 50px;height: 8px;" +
            "border: 1px solid #ABABAB;background: #fff;font-size: 0px;line-height: 0;overflow: hidden;white-space: nowrap;color: #b0b0b0;'>";
        divStr += "<span id='" + id + "_position' style='position: absolute;left: 1px;top: 1px;height: 6px;-margin-right: -3px;font-size: 0px;line-height: 0;white-space: nowrap;" +
            "color: #b0b0b0;width: 0%;background: #79CC5A url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAAAGCAMAAAACAMs3AAAAA3NCSVQICAjb4U/gAAAASFBMVEWE12WE12SF12WE1mSC0mGB0mGB0mCB0WB5zFp5zFt4zFp4y1p0xlVzxlV0xlZzxVVxv1Fxv1Bwv1BwvlBquk5puU1puk3////H7KbBAAAAGHRSTlP//////////////////////////////wDNEy7qAAAACXBIWXMAAArwAAAK8AFCrDSYAAAAFnRFWHRDcmVhdGlvbiBUaW1lADA1LzE1LzEysU5BKAAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNAay06AAAACtSURBVCiRXYzbEoMgDEQjBUoVEGvx/z+1mWyTiT0PmWUvUAgPIYRAROEOHKTehL8si68F/coKjx8U41NIKUUh58w3KVELf6k56GTBPrG5TKmUl1BK2baNhd0iUVF8Z11Xe6JpEbZcgCMJ9V5r3fe9tcaXde/dP5kmVAdHXYao2RN0wW1pjLcyBNPHcZzniSeLoUDD9FtcmLi6oM+N67pMzDmhp2Da17zphY/m/AK+bxo7Cuh9lgAAAABJRU5ErkJggg==) repeat-x;'>";
        divStr += "</span>";
        divStr += "</span>";
        divStr += "<span id='" + id + "_title' style='height: 16px;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;color: #b0b0b0;" +
            "line-height: 160%;overflow: visible;font-size: 12px;margin-left: 8px;'>";
        divStr += "</span>";
        divStr += "</div>";

        divStr += "<div id='" + id + "_uploadEnd' style='height: 16px;white-space: nowrap;text-overflow: ellipsis;color: #b0b0b0;" +
            "position: absolute;left: 41px;top: 21px;line-height: 160%;overflow: visible;font-size: 12px;;display: none;'>";
        divStr += "</div>";

        divStr += "</div>";

        var header = {
            tabParent: div
        };
        dw.write(header, divStr);

        var control = $(div).find("#" + id)[0];
        this.uploader = new UploadFile(filename);
        control.options = {};
        control.options["uploadcontrol"] = uploadcontrol;
        control.options["object"] = this;
        control.options.frameobj = this;

        var control = $(div).find("#" + id + "_delete")[0];
        control.options = {};
        control.options["uploadcontrol"] = uploadcontrol;
        control.options["object"] = this;
        control.options.frameobj = this;

        control = $(div).find("#" + id + "_label")[0];
        control.options = {};
        control.options["uploadcontrol"] = uploadcontrol;
        control.options["object"] = this;
        control.options.frameobj = this;

        $(div).find("#" + id + "_label").width(rect.width - 41 - $("#" + id + "_delete").width() - 5);

    }
}