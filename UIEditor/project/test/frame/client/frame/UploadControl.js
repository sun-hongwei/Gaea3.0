function uploadButtonClick(e) {
    var uploadecontrol = e.sender.options["upload"];
    var fileid = uploadecontrol.id + 'file';
    var jqObj = $(uploadecontrol.div).find("#" + fileid);
    jqObj.val("");
    var domObj = jqObj[0];
    domObj.outerHTML = domObj.outerHTML;
    var newJqObj = jqObj.clone();
    jqObj.before(newJqObj);
    jqObj.remove();
    domObj = newJqObj[0];
    domObj.id = fileid;
    domObj.onchange = function () {
        var control = this.options["control"];

        for (var i = 0; i < domObj.files.length; i++) {
            control.add(domObj.files[i]);
        };

    }
    domObj.options = {
        "control": uploadecontrol
    };
    domObj.click();
}

function UploadControl() {
    this.Event = {
        onGetUserData: undefined, //function(fileobj),fileobj为浏览器的file对象，其fileobj.name为待上传文件名称
        onUploadEnd: undefined, //function(UploadItem, filename)
    }
    this.files = {};
    this.dw;
    this.header;
    this.columns = 3;
    this.columnWidth;
    this.id;
    this.div;

    this.remove = function (uploaditem) {
        delete this.files[uploaditem.filename];
        uploaditem.removeSelf();
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.add = function (fileobj) {
        if (!Tools.isNull(this.files[fileobj.name]))
            return;
        var userdata = {
            command: "baseupload",
            data: {}
        };
        if (!Tools.isNull(this.Event.onGetUserData))
            userdata = this.Event.onGetUserData(fileobj);
        if (!userdata) return false;
        var div = $(this.div).find("#" + this.id)[0];
        var item = new UploadItem();
        var uploader = new UploadFile();
        item.createItem(new DocumentWriter(), div, fileobj.name, {
            width: this.columnWidth,
            height: 44
        }, fileobj.name, this);
        setTimeout(function (item, fileobj, command, data) {
            uploader.upload(item, fileobj, command, data);
        }, 1000, item, fileobj, userdata.command, userdata.data);
        // uploader.upload(item, fileobj, userdata.command, userdata.data);
        item.uploader = uploader;
        this.files[fileobj.name] = item;
    }

    this.getHtml = function (header) {
        var headerString = '<div style="border:0px;width:100%;height:100%;' +
            (Tools.isNull(header.style) ? "" : header.style) + '"' +
            this.setField('id', header.id + "download_parent") +
            this.setField('name', header.name + "download_parent") +
            this.setField('class', header.styleClass) +
            ">";
        if (header.showButton) {
            headerString += '<a class="mini-button"' +
                this.setField('id', header.id + "button") +
                this.setField('name', header.name + "button") +
                this.setField('img', (Tools.isNull(header.buttonImage) ? undefined : "image/" + header.buttonImage)) +
                this.setField('plain', "true") +
                this.setField('onclick', "uploadButtonClick") +
                '>上传</a>';

        }
        headerString += '<div style="border:1px solid #d3d3d3;width:100%;overflow:auto"' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            '>';
        headerString += '<input type="file" id="' + header.id + 'file" multiple="multiple" style="display:none" mce_style="display:none">'
        headerString += '</div></div>';

        return headerString;
    }

    this.init = function (header, div) {
        this.header = header;
        this.columns = this.header.columns;
        this.id = this.header.id;
        this.div = div;

        var control = $(div).find("#" + this.header.id)[0];
        control.options = {};
        control.options["upload"] = this;
        control.options.frameobj = this;
        this.columnWidth = Math.floor($(control).width() / this.columns - 20);
        // if (this.columnWidth < 241)
        //     this.columnWidth = 241;

        if (this.header.showButton) {
            var width = $(div).width();
            var height = $(div).height();
            mini.parse();
            var button = mini.getByName(this.header.name + "button", div);
            button.options = {};
            button.options["upload"] = this;
            button.options.frameobj = this;
            var selector = $(button.getEl());
            var divTop = selector.height() + 5;
            $(control).height(height - divTop - 5);
        }
    }

    this.write = function (dw, header) {
        var headerString = this.getHtml(header);
        dw.write(header, headerString);
        this.init(header, header.tabParent);
    }
    this.clear = function(){
        this.files = {};
        var filesbtn = $("#"+this.id+"file");
        $("#"+this.id).empty();
        $("#"+this.id).append(filesbtn);
    }
}