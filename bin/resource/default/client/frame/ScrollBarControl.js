function ScrollBarControl() {
    this.div = undefined;
    this.id = undefined;
    this.postData;
    this.Event = {
        onError: undefined,
        onGetData: undefined,
        onGetPageHtml: undefined,
        onBeforeLoad: undefined,
        onBeforeSend: undefined,
        onAfterLoad: undefined,
    };

    this.postData = undefined;
    this.colCount = 4;

    this.loadingHtml = '<div class="loading"><img src="image/loading.gif" align="absmiddle" /></div>';
    this.nothingHtml = '<div class="nothingResult">已经加载全部数据了!</div>';
    this.loadPage = "./Services/SampleScrollGetService.php";
    this.heightOffset = 0;
    this.loadingClass = "loading";
    this.nothingClass = "nothingResult";
    this.scrollTarget = window;
    this.tag = undefined;

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

    this.getField = function (value, defaultValue) {
        return (Tools.isNull(value) ? defaultValue : value);
    }

    this.defaultOnBeforeLoad = function (opt) {
        divObj = opt.control.div; //opt.sender;
        sc = opt.control; //divObj.options["ScrollControl"];
        // 加载区域显示
        $('.' + sc.loadingClass).remove();
        $(divObj).append(sc.loadingHtml);
        data = sc.Event.onGetData(sc, false);
        sc.postData = sc.createSendObj(data);
        opt.contentData = sc.postData;
    }

    this.defaultOnGetPageHtml = function (data, cellspacing, pagewidth) {
        var ChapterObj = data.ChapterObj;

        var html = "        <table table-layout=\"fixed\" cellspacing=\"" + cellspacing + "\" border=\"0\" >";
        html += "        <caption style=\"text-align:center\"><a href=\"" + data.url + "\" title=\"" + data.title + "\" target=\"_blank\">" + data.title + "</a></caption>";
        html += "            <tr>";
        html += "                <td colspan=\"3\"><a href=\"" + data.url + "\" title=\"" + data.title + "\" target=\"_blank\">";
        html += "                    <img src=\"image/" + data.image + "\" width=\"" + pagewidth + "\" alt=\"图像下载中...\"/>";
        html += "                </a></td>";
        html += "            </tr>";
        html += "            <tr>";
        html += "                <td>作者：" + data.author + "(" + data.hits + "点击)" + "</td>";
        if (ChapterObj == undefined) {
            html += "                <td colspan=\"2\">更新：暂无</td>";
        } else {
            html += "                <td colspan=\"2\">更新：" + ChapterObj.addTime + "&nbsp;/&nbsp;" + ChapterObj.title + "</td>";
        }
        html += "            </tr>";
        html += "            <tr>";
        html += "                <td colspan=\"3\">" + data.memo + "</td>";
        html += "            </tr>";
        html += "        </table>";

        return html;
    }

    this.addPage = function (columns, cellspacing, pagewidth) {
        html = "<tr>"; //style=\"border-spacing:10px 10px;\"
        for (var j = 0; j < columns.length; j++) {
            data = columns[j];
            html += "<td width=\"" + (100 / this.colCount) + "%\">";
            html += this.Event.onGetPageHtml(data, cellspacing, pagewidth);
            html += "</td>";
        }
        html += "<tr>";
        return html;
    };

    this.onLoad = function (jsonObj) {
        // 隐藏加载区域
        id = jsonObj.optid;
        datas = jsonObj.data;
        divObj = document.getElementById(id);
        sc = divObj.options["ScrollControl"];
        $('.' + sc.loadingClass).remove();
        if (datas.length <= 0) {
            $(divObj).stopScrollPagination();
            $(divObj.id + '.' + sc.nothingClass).remove();
            $(divObj).append(sc.nothingHtml);
        } else {
            columns = [];

            var cellspacing = 10;

            var pagewidth = ($(divObj).width() - cellspacing * 4) / sc.colCount;
            var html = "<table table-layout=\"fixed\" cellpadding=\"" + cellspacing + "\" border=\"0\" width=\"100%\">";
            for (var i = 0; i < datas.length; i++) {
                columns.push(datas[i]);

                if (columns.length == sc.colCount) {
                    html += sc.addPage(columns, cellspacing, pagewidth);
                    columns = [];
                }
            }
            if (columns.length > 0)
                html += sc.addPage(columns, cellspacing, pagewidth);
            html += "</table>";
            $(divObj).append(html);
        }
    };

    this.createSendObj = function (data) {
        var json = Tools.createAjaxPackage(data.command, data.postdata);
        json["Command_Running_Hint"] = data.hint;
        json["optid"] = this.id;
        Tools.updateAjaxPackageSign(json);
        return json;
    }

    this.fireOnBeforeLoad = function (e) {
        this.defaultOnBeforeLoad(e);

        if (Tools.isNull(this.Event.onBeforeLoad))
            return;

        this.Event.onBeforeLoad(e);
    }

    this.fireOnBeforeSend = function (e) {
        if (Tools.isNull(this.Event.onBeforeSend))
            return;

        this.Event.onBeforeSend(e);
    }

    this.fireOnAfterLoad = function (e) {
        if (Tools.isNull(this.Event.onAfterLoad))
            return;

        this.Event.onAfterLoad(e);
    }

    this.createSendPackageObj = function () {
        return {
            'control': this,
            // 你要搜索结果的页面
            'contentPage': this.loadPage,
            // 你可以通过 children().size() 知道哪里是分页[JSON格式]
            'contentData': this.postData,
            // 谁该怎么滚动？在这个例子中，完整的窗口
            'scrollTarget': this.scrollTarget,
            // 在页面到达结束之前，从多少像素开始加载
            'heightOffset': this.heightOffset,
            // 加载前，可以显示一个加载DIV
            'beforeLoad': this.fireOnBeforeLoad,
            'beforeSend': this.fireOnBeforeSend,
            'afterLoad': this.fireOnAfterLoad,
            'dataType': 'JSON',
            'loader': this.onLoad,
            'error': function (opt) {
                // 加载区域显示
                $('.' + opt.control.loadingClass).remove();
                if (!Tools.isNull(opt.control.Event.onError))
                    opt.control.Event.onError(opt.control);
            }
        }
    }

    this.getHtml = function (data) {
        var headerString = '<div style="overflow:hidden;"' +
            this.setField('class', data.styleClass) +
            this.setField('id', data.id) +
            this.setField('name', data.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            '>';
        headerString += '</div>';
        return headerString;
    }

    this.init = function (data) {
        this.div = document.getElementById(data.id);
        if (this.div == undefined)
            return false;

        this.loadingClass = this.getField(data.loadingClass, this.loadingClass);
        this.nothingClass = this.getField(data.nothingClass, this.nothingClass);
        this.loadPage = this.getField(data.loadPage, this.loadPage);
        this.scrollTarget = this.getField(data.scrollTarget, this.scrollTarget);
        this.heightOffset = this.getField(data.heightOffset, this.heightOffset);
        this.tag = this.getField(data.tag, this.tag);
        this.id = data.id;
        this.div.options = {};
        this.div.options["ScrollControl"] = this;
        this.div.options.frameobj = this;
        this.Event.onGetPageHtml = this.getField(data.onGetPageHtml, this.defaultOnGetPageHtml);

    }

    this.write = function (dw, data) {
        var headerString = this.getHtml(data);

        dw.write(data, headerString);

        this.init(data);
    }

    this.load = function () {
        $(this.div).scrollPagination(this.createSendPackageObj());
    }
}