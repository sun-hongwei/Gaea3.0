function ListViewGlobalOnHrefClick(e) {
    var cellDiv = global_GetListViewCellElement($(e));
    var listview = cellDiv.options["listview"];
    var row = cellDiv.options["row"];
    var header = cellDiv.options["header"];
    if (Tools.isNull(listview.Event.onListViewClick))
        return;

    var report = undefined;
    if (header.type == "report") {
        report = listview.getReportControl(row, header.id);
    }

    listview.Event.onListViewClick(listview, row, header, e, report);
}

function ListViewControl() {
    this.Event = {
        onListViewClick: undefined, //function(listview, control, report)
    }
    this.controlCommon = new ControlCommon();
    this.div;
    this.id;
    this.header;
    this.uiInfo;

    this.hrefs = [];
    this.headerkeys = {};
    this.headers = [];
    this.rowdatas = [];
    this.controls = {};
    this.divWidth = 5;

    this.reports = [];

    this.addReport = function (div, name) {
        var control = new ReportControlEx();
        var header = globalGetReportInfo(name);
        if (Tools.isNull(header))
            return;

        header.datasource = undefined;
        header.tabParent = div;

        var colWidths = undefined;
        var rowHeights = undefined;

        var sizes = undefined;

        control.write(new DocumentWriter(), header, this.uiInfo, $(header.tabParent).width(), $(header.tabParent).height(), colWidths, rowHeights);
        if (Tools.isNull(sizes)) {
            sizes = {
                col: control.cols,
                row: control.rows
            };
        }


        var report = $(div).find("#" + header.id)[0];
        if (Tools.isNull(report.options))
            report.options = {};

        report.options["listview"] = this;
        report.options["control"] = control;
        this.reports.push(report);

        report.report.Event.onReportClick = function (report, element, id, name) {
            var control = element;
            if (!Tools.isNull(name)) {
                control = report.getCellDivByName(name);
            }
            ListViewGlobalOnHrefClick(control);
        };

    }

    this.getFieldValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.loadHref = function (data) {
        var control = $("#" + data.id)[0];
        control.options = {};
        control.options["real"] = data.header;
        control.options["text"] = data.text;
        control.options["listview"] = this;
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.createImageCell = function (header, id) {
        var imagestyle = "width:100%;height:100%";
        switch (header.scaleMode) {
            case "smHeight":
                imagestyle = "height:100%;";
                break;
            case "smWidth":
                imagestyle = "width:100%;";
                break;
            case "smCenterInRectangle":
                imagestyle = "max-width: 100%;max-height: 100%;";
                break;
        }

        var spanStr = '<span style="height:100%;display:inline-block;vertical-align:middle"></span>';
        str = '<img' +
            this.setField('id', id) +
            this.setField('name', id) +
            this.setField('style', "border:0;" + imagestyle) +
            this.setField('src', "image/" + header.image) +
            this.setField('value', header.value) +
            this.setField('alt', header.alt, "下载中。。。") +
            '/>';
        if (!Tools.isNull(header.href)) {
            str = "<a id='" + header.id + "_a' " + this.getHrefHeader(header) + ">" + str + '</a>';
        }
        return spanStr + str;
    }

    this.createHeader = function (header, data, left, top, height, row) {
        return this.createCell(header, data, left, top, data.width, height, row, "text");
    }

    this.getHrefHeader = function (data) {
        var href = "href='javascript:void(0)' onclick='ListViewGlobalOnHrefClick(this)'";
        if (!Tools.isNull(data.href)) {
            var style = "";
            if (!data.useHrefColor) {
                style = ";color:" + Tools.convetColor(data.textColor);
            }
            href = this.setField('href', data.href) +
                this.setField('download', data.download) +
                this.setField('target', data.target) +
                this.setField("style", style);
        }
        return href;
    }

    this.getReportControl = function (row, id) {
        var reportDiv = this.getCellDiv(row, id);
        var report = reportDiv.children[0].options.control;
        return report;
    }

    this.getReportElement = function (row, id) {
        var reportDiv = this.getCellDiv(row, id);
        return reportDiv.children[0];
    }

    this.getReportChildControl = function (row, id, controlName) {
        var reportDiv = this.getCellDiv(row, id);
        var control = $(reportDiv).find("[name='" + controlName + "']")[0];
        if (Tools.isNull(control))
            return undefined;
        return control;
    }

    this.setReportValue = function (row, id, controlName, value) {
        var control = this.getReportChildControl(row, id, controlName);
        if (Tools.isNull(control))
            return undefined;

        var report = this.getReportControl(row, id);
        return report.setCellValue(control, value);
    }

    this.getReportValue = function (row, id, controlName) {
        var control = this.getReportChildControl(row, id, controlName);
        if (Tools.isNull(control))
            return undefined;

        var report = this.getReportControl(row, id);
        return report.getCellValue(control);
    }

    /***
     * 返回格式：
     * [{"field":"value"}]
     */
    this.getValue = function () {
        var result = [];
        for (var index = 0; index < this.getRowCount(); index++) {
            var row = {};
            for (var index1 = 0; index1 < this.headers.length; index1++) {
                var header = this.headers[index1];
                row[header.id] = this.getCellValue(index, header.id);
            }
            result.push(row);
        }
        return result;
    }

    this.getCellValue = function (rowIndex, id, isText) {
        var typename = this.headerkeys[id].type;
        var control;
        switch (typename) {
            case "image":
            case "text":
                control = this.getCell(rowIndex, id);
                break;
            case "report":
                control = this.getReportControl(rowIndex, id);
                break;
        }

        if (Tools.isNull(control))
            return undefined;

        if (!Tools.isNull(isText) && isText) {
            switch (typename) {
                case "image":
                    return control.src;
                case "text":
                    return $(control).text();
                case "report":
                    return JSON.stringify(control.getValue());
            }
        } else {
            switch (typename) {
                case "image":
                case "text":
                    return $(control).attr("value");
                case "report":
                    return this.getReportElement(rowIndex, id);;
            }

        }
    }

    /***
     * value格式：
     * 同getValue函数返回格式
     */
    this.setValue = function (value) {
        for (var index = 0; index < value.length; index++) {
            var row = value[index];
            if (index == this.getRowCount())
                return;
            else {
                for (var field in row) {
                    if (this.headerkeys.hasOwnProperty(field)) {
                        this.setCellValue(index, field, row[field]);
                    }
                }
            }
        }
    }

    this.setCellValue = function (rowIndex, id, value, isText, isDataSource) {
        var typename = this.headerkeys[id].type;
        var control;
        switch (typename) {
            case "image":
            case "text":
                control = this.getCell(rowIndex, id);
                break;
            case "report":
                control = this.getReportControl(rowIndex, id);
                break;
        }

        if (Tools.isNull(control))
            return undefined;

        if (!Tools.isNull(isText) && isText) {
            switch (typename) {
                case "image":
                    control.src = value;
                    break;
                case "text":
                    $(control).text(value);
                    break;
                case "report":
                    control.setValue(JSON.parse(value));
                    break;
            }
        } else {
            switch (typename) {
                case "image":
                case "text":
                    $(control).attr("value", value);
                    break;
                case "report":
                    if (!Tools.isNull(isDataSource) && isDataSource)
                        control.setDataSourceRow(value);
                    else
                        control.setValue(value);
                    break;
            }

        }
    }


    this.setData = function (value) {
        for (var row = 0; row < value.length; row++) {
            var rowData = value[row];
            this.setRowData(rowData, row, false);
        }
        this.reset();
    }

    this.setRowData = function (rowData, row, refresh) {
        if (Tools.isNull(this.rowdatas[row]))
            return;

        for (var column in rowData) {
            if (rowData.hasOwnProperty(column)) {
                var cellData = rowData[column];
                this.setCellValue(row, column, cellData, false);
            }
        }

        if (refresh)
            this.reset();
    }

    /**
     * 获取单元格div，不是包含数据的element
     * @param {int} row 行号
     * @param {string} id 列id
     */
    this.getCellDiv = function (row, id) {
        var div = this.getRowDiv(row);
        if (Tools.isNull(div))
            return undefined;

        var control = $(div).find("#" + id + "_div")[0];
        return control;
    }

    this.getFieldId = function (index) {
        return this.headers[index].id;
    }

    /**
     * 获取单元格控件，这个方法获取的是显示数据的真实DOM元素
     * @param {int} row 行号
     * @param {string} id 列id
     */
    this.getCell = function (row, id) {
        var div = this.getRowDiv(row);
        if (Tools.isNull(div))
            return undefined;

        var control = $(div).find("#" + id)[0];
        return control;
    }

    this.createCell = function (header, data, left, top, width, height, row, typename) {

        var lineColor = "#000";
        if (!Tools.isNull(header.lineColor))
            lineColor = Tools.convetColor(header.lineColor);

        var backgroundColor = "";
        if (!Tools.isNull(header.backgroundColor))
            backgroundColor = "background:" + Tools.convetColor(header.backgroundColor);

        var border = (header.showLine ? "border:1px solid " + lineColor : "border:0px");
        var style = border + ";" + backgroundColor + ";position:absolute;left:" + left + "px;top:" + top + "px;width:" + width + "px;height:" + height + "px";

        var fontstyle = header.fontStyle;
        if (!Tools.isNull(data.fontstyle)) {
            switch (data.fontstyle) {
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

        }
        var fontsize = header.fontSize;
        if (!Tools.isNull(data.fontsize))
            fontsize = data.fontsize;

        var fontname = header.fontName;
        if (!Tools.isNull(data.fontname))
            fontname = data.fontname;

        var textcolor = header.textColor;
        if (!Tools.isNull(data.textcolor))
            textcolor = data.textcolor;

        var fontweight = data.fontstyle == "1" || data.fontstyle == "3" ? "bold" : "normal";

        var textFontStyle = "word-break: break-all;font-family:'" + fontname + "';font-style:" + fontstyle + ";font-weight:" +
            fontweight + ";font-size:" + Tools.convetFontSize(fontsize) + ";color:" + Tools.convetColor(textcolor) + ";text-align:" +
            (Tools.isNull(data.align) ? "center" : data.align);

        var allStyles = style;
        if (Tools.isNull(typename))
            typename = this.headerkeys[data.id].type;
        switch (typename) {
            case "image":
                allStyles = style + ";text-align:center";
                break;
            case "text":
                allStyles = style + ";margin:0px;auto;display:table;" + textFontStyle;
                break;
        }

        var id = data.id;

        var cellString = "<div" +
            this.setField("style", allStyles) +
            this.setField('id', id + "_div") +
            this.setField('name', id + "_div") +
            ">";
        switch (typename) {
            case "text":
                var text = data.title;
                if (!Tools.isNull(data.text))
                    text = data.text;

                cellString += '<div id="' + id + '_layout" style="display:table-cell;border:0px; vertical-align:middle"><table id="' + id + '_table"  align="' +
                    (Tools.isNull(data.align) ? "center" : data.align) + '" border="0" cellpadding="0" cellspacing="0">';
                if (!Tools.isNull(data.href)) {
                    cellString += "<tr><td><a id='" + id + "_a' " + this.getHrefHeader(data) + "><span value='" + header.value + "' id='" + id +
                        "' style='" + (Tools.isNull(data.style) ? "" : data.style) + "'>" + text + "</span></td></tr>";
                    this.hrefs.push({
                        "id": id + "_a",
                        "text": text,
                        "header": data
                    });
                } else {
                    cellString += "<tr><td><span value='" + header.value + "' id='" + id + "' style='" + (Tools.isNull(data.style) ? "" : data.style) + "'>" +
                        (Tools.isNull(text) ? "" : text) + "</span></td></tr>";
                }
                cellString += "</table></div>";
                break;
            case "image":
                cellString += this.createImageCell(data, id, "width:" + width + "px;height:" + height + "px");
                if (!Tools.isNull(data.href))
                    this.hrefs.push({
                        "id": id,
                        "text": "image/" + data.image,
                        "header": data
                    });
                break;
            case "report":
                break;
        }

        cellString += "</div>";

        return cellString;
    }

    this.getHeaderHeight = function () {
        if (this.header.showHeader && this.headers.length > 0) {
            return this.header.headerHeight;
        }
        return 0;
    }

    this.getRowHeight = function (index) {
        this.getRowDiv(index);
    }

    this.getRowHeights = function () {
        var top = this.getHeaderHeight() + this.header.rowDiv;
        for (var index = 0; index < this.rowdatas.length; index++) {
            top += this.header.rowDiv + this.header.lineHeight;
        }
        return top;
    }

    this.getRowCount = function () {
        return this.rowdatas.length;
    }

    this.newRowId = function () {
        return this.getRowID(this.getRowCount());
    }

    this.getRowID = function (index) {
        return this.id + "_" + index;
    }

    this.getRowDiv = function (index) {
        return $("#" + this.getRowID(index))[0];
    }

    /**
     * 获取指定的行有多少列存在
     */
    this.getRowColumnCount = function (rowIndex) {
        var count = 0;
        for (var index = 0; index < this.headers.length; index++) {
            var header = this.headers[index];
            if (Tools.isNull(this.getCellDiv(rowIndex, header.id)))
                break;
            count++;
        }

        return count;
    }

    this.getColumnCount = function () {
        return this.headers.length;
    }

    /**
     * 刷新uiInfo的listview信息
     */
    this.refreshUIInfo = function () {
        var uiInfo = this.uiInfo;
        var datas = [];
        var columns = Object.keys(this.rowdatas);
        for (var index = 0; index < columns.length; index++) {
            var column_id = columns[index];
            var row = this.rowdatas[column_id]; //row数据格式：{"id","行id", "列id":{"列定义key":"值"}}
            var newrow = [];
            var rowColumns = Object.keys(row);
            for (var index1 = 0; index1 < rowColumns.length; index1++) {
                var field = rowColumns[index1];
                if (field != "id") {
                    newrow.push(row[field]);
                }
            };
            datas.push(newrow);
        };

        refreshFrameListView(uiInfo, this.header, datas);
    }

    this.getCellLeft = function (cellid) {
        var size = this.header.colDiv;
        for (var index = 0; index < this.headers.length; index++) {
            if (this.headers[index].id == cellid) {
                break;
            }
            size += this.header.colDiv + this.headers[index].width;
        }

        return size;
    }

    this.getColumnIndex = function (colId) {
        var result = -1;
        for (var index = 0; index < this.headers.length; index++) {
            var header = this.headers[index];
            result = index;
            if (header.id == colId)
                return result;
        }

        return -1;
    }

    /**
     * 添加一个单元格到指定行
     * @param {int} rowIndex 行索引
     * @param {object} cellData 要添加的数据
     * @param {string} id 要添加的列id
     */
    this.addToLine = function (rowIndex, cellData, colIndex) {
        var header = this.headers[colIndex];
        if (Tools.isNull(header))
            return;

        var left = this.getCellLeft(header.id);
        var headerString = "";

        var report = undefined;
        var headerString = this.createCell(this.header, cellData, left, 0,
            header.width - this.header.colDiv, this.header.lineHeight, rowIndex);
        if (header.type == "report") {
            report = {
                row: rowIndex,
                id: header.id,
                name: cellData.name
            };
        }

        var dw = new DocumentWriter();
        dw.write({
            tabParent: this.getRowDiv(rowIndex),
        }, headerString);

        if (!Tools.isNull(report)) {
            var cellDiv = this.getCellDiv(report.row, report.id)
            this.addReport(cellDiv, report.name);
        }

        var div = this.getCellDiv(rowIndex, header.id);
        if (Tools.isNull(div))
            return;

        if (Tools.isNull(div.options))
            div.options = {};
        div.options["row"] = rowIndex;
        div.options["header"] = header;
        div.options["listview"] = this;
    }

    /**
     * 添加一行数据到listview
     * @param {object} row 行数据集合，格式如下：
     * {"a1": { id: "a1", name: "listviewitem" }}
     * @param {bool} noRefresh 是否立即刷新listivew显示
     */
    this.add = function (row, noRefresh) {
        var top = this.getRowHeights();
        var key = this.newRowId();
        var rowIndex = this.getRowCount();

        var headerString = "<div style='position:absolute;left:0px;top:" + top + "px;width:100%;height:" + this.header.lineHeight + "px'" +
            this.setField('id', key) +
            ">";
        headerString += "</div>";

        var dw = new DocumentWriter();
        dw.write({
            tabParent: this.div
        }, headerString);

        var control = $("#" + key)[0];
        control.options = {};
        control.options["row"] = key;
        control.options["listview"] = this;

        this.controls[key] = {
            control: control
        };
        var newRow = JSON.parse(JSON.stringify(row));
        newRow.id = key;
        this.rowdatas.push(newRow);

        for (var j = 0; j < this.headers.length; j++) {
            var cell = row[this.headers[j].id];
            if (Tools.isNull(cell))
                continue;
            this.addToLine(rowIndex, cell, j);
        }

        if (Tools.isNull(noRefresh) || !noRefresh) {
            this.refreshUIInfo();
        }

        $(control).height($(control).height() + 10);
        return this.controls[key];

    }

    this.resetHeader = function (headers) {
        this.clear();
        this.initHeaderData(headers);

        var dw = new DocumentWriter();
        var left = this.divWidth;
        var top = "0";

        var headerString = "<div  style='position:absolute;left:0px;top:" + top + "px;width:100%;height:" + this.header.headerHeight + "px'" +
            this.setField('id', this.header.id + "_header") +
            this.setField('name', this.header.name + "_header") +
            ">";

        for (var i = 0; i < headers.length; i++) {
            headerString += this.createHeader(this.header, headers[i], left, top, this.header.headerHeight, -1);
            left += headers[i].width;
        }
        headerString += "</div>";
        dw.write({
            tabParent: this.div
        }, headerString);
    }

    /**
     * 删除指定行
     * @param {int} index 要删除的行号，从0开始
     */
    this.remove = function (index) {
        this.rowdatas.remove(index);
        this.reset();
    }

    /**
     * 清除所有数据及标题
     */
    this.clear = function () {
        if (!Tools.isNull(this.div))
            $(this.div).empty();
        this.rowdatas = [];
        this.controls = {};
        this.hrefs = [];
        this.reports = [];

    }

    /**
     * 删除所有行
     */
    this.removeRows = function () {
        this.rowdatas = [];
        this.reset();
    }

    this.setTemplateData = function (datasources, template) {
        var dataset = datasources[template.datasource];
        if (Tools.isNull(dataset))
            return;

        var cols = template.cols;
        var rowIndex = 0;
        var colCount = Math.max(this.headers.length, cols.length);

        while (rowIndex < dataset.length) {
            var rowData = {};

            var curRowIndex = this.getRowCount() - 1;
            var rowColCount = this.getRowColumnCount(curRowIndex);
            //为true表示将新单元格追加到当前行的后面
            var addToLine = rowColCount > 0 && rowColCount != colCount && (Tools.isNull(template.newline) || !template.newline);
            var size = 0;
            //每行数据用于listview的一个单元格
            if (addToLine) {
                size = Math.min(colCount - rowColCount, dataset.length - rowIndex);
            } else
                size = Math.min(colCount, dataset.length - rowIndex);

            if (size > 0) {

                //修改listview的一行数据中报表格式的数据，因为其他数据与listview的单元格数据一致，报表不同
                for (var index = (addToLine ? rowColCount : 0); index < (addToLine ? rowColCount + size : size); index++) {
                    var col = cols[index];
                    switch (col.type) {
                        case "report":
                            rowData[col.id] = {
                                id: col.id,
                                name: col.template
                            };
                            break;
                    }
                }

                if (addToLine) {
                    for (var id in rowData) {
                        this.addToLine(curRowIndex, rowData[id], this.getColumnIndex(id));
                        var row = dataset[rowIndex++];
                        this.setCellValue(curRowIndex, id, row, false, true);
                    }
                } else {
                    //加入一行
                    this.add(rowData);
                    //使用数据源数据更新每个单元格
                    for (var index = 0; index < size; index++) {
                        var row = dataset[rowIndex++];
                        var col = cols[index];
                        this.setCellValue(this.getRowCount() - 1, col.id, row, false, true);
                    }
                }

            }
        }

    }

    this.getDataSourceId = function () {
        var templates = undefined;
        if (!Tools.isNull(this.header.template))
            templates = this.header.template.template;
        if (!Tools.isNull(templates)) {
            var dsids = [];
            for (var index = 0; index < templates.length; index++) {
                dsids.push(templates[index].datasource);
            }
            return dsids;
        } else
            return Tools.isNull(header.datasource) ? [] : [header.datasource];
    }

    this.initDataSource = function (params) {
        if (Tools.isNull(this.uiInfo) || Tools.isNull(this.header))
            return;

        if (Tools.isNull(this.header.template))
            return;

        if (Tools.isNull(this.header.template.template))
            return;

        var templates = this.header.template.template;
        var headers = this.header.template.header;
        if (Tools.isNull(headers))
            headers = this.headers;
        else
            this.headers = headers;

        for (var index = 0; index < headers.length; index++) {
            var header = headers[index];
            header.type = "report";
        }

        this.initView(undefined, headers);
        var rowTemplate = {};
        var refTemplates = {};
        var idTemplates = {};
        for (var index = 0; index < templates.length; index++) {
            var template = templates[index];
            if (template.hasOwnProperty("startRow")) {
                rowTemplate[template.startRow] = template;
            } else if (template.hasOwnProperty("ref")) {
                refTemplates[template.ref] = template;
            }

            idTemplates[template.id] = template;
        }

        var sortTemplates = Tools.sortObject(rowTemplate);
        var datasources = {}

        for (var id in idTemplates) {
            if (idTemplates.hasOwnProperty(id)) {
                var template = idTemplates[id];
                var dsId = template.datasource;
                var param = undefined;
                if (!Tools.isNull(params))
                    param = params[dsId];
                GlobalDataSources.get(dsId, true,
                    function (datasource, columns, dataset) {
                        datasources[datasource.id] = dataset;
                    }, param);
            }
        }

        for (var row in sortTemplates) {
            if (sortTemplates.hasOwnProperty(row)) {
                var template = sortTemplates[row];
                this.initTemplate(template, datasources, refTemplates);
            }
        }
    }

    this.initTemplate = function (template, datasources, refTemplates) {
        this.setTemplateData(datasources, template);
        if (refTemplates.hasOwnProperty(template.id)) {
            var oldId = template.id;
            template = refTemplates[template.id];
            delete refTemplates[oldId];
            this.initTemplate(template, datasources, refTemplates);
        }
    }

    /**
     * 初始化listview,如果rowdatas和headerDefines都不设置，则使用现有数据刷新listivew
     * @param {object} rowdatas 行数据集合，可为空，为空使用this.rowdatas，格式如下：
     * [ 
     *     [  { "fontstyle": "1",
     *        "textcolor": "0xFF33CCFF",
     *        "fontsize": "10",
     *        "id": "a",
     *        "text": "等待审核",
     *        "fontname": "微软雅黑",
     *        "value": "值1"
     *        }
     *    ],
     *    [
     *        {
     *            "id": "a",
     *            "text": "竞标中",
     *            "fontname": "微软雅黑",
     *            "value": "值2"
     *        }
     *    ]
     * ]
     * @param {object} headerDefines 列定义集合，可为空，为空使用this.header.header，格式如下：
     * [
     *        {
     *            "width": 140,
     *            "id": "a",
     *            "title": "标题",
     *            "type": "text",
     *            "align": "left"
     *        },
     *        {
     *            "width": 300,
     *            "textcolor": "0xFFFF6633",
     *            "id": "b",
     *            "title": "名称",
     *            "type": "text"
     *        }
     * ]
     */
    this.reset = function (rowdatas, headerDefines) {
        this.initView(rowdatas, headerDefines);
        if (this.header.autoInitDataSource)
            this.initDataSource();
    }

    this.initView = function (rowdatas, headerDefines) {
        if (Tools.isNull(headerDefines))
            headerDefines = JSON.parse(this.header.header);
        else
            this.header.header = JSON.stringify(headerDefines);

        this.headers = headerDefines;
        this.resetHeader(headerDefines);

        if (Tools.isNull(rowdatas))
            rowdatas = this.rowdatas;

        this.rowdatas = [];
        for (var i = 0; i < rowdatas.length; i++) {
            var row = rowdatas[i];
            this.add(row);
        }

        this.hrefs.forEach(function (element) {
            this.loadHref(element);
        }, this);
    }

    this.initHeaderData = function (headers) {
        this.headerkeys = {};
        this.headers = headers;

        if (Tools.isNull(headers) || Tools.isNull(this.header) || Tools.isNull(this.div))
            return;

        var spaceWidth = 0;
        var maxCount = Math.min(this.header.visiableColumnCount, this.headers.length);
        if (this.header.autoColumnWidth) {
            var vscrollbarWidth = Tools.getScrollbarWidth();

            var divWidth = $(this.div).width() - vscrollbarWidth - this.header.colDiv * (headers.length - 1);
            spaceWidth = divWidth / maxCount - (divWidth % maxCount != 0 ? 1 : 0) - 3;
        }

        for (var i = 0; i < this.headers.length; i++) {
            if (spaceWidth != 0) {
                this.headers[i].width = spaceWidth;
            }
            this.headerkeys[this.headers[i].id] = this.headers[i];
        }

    }

    this.initEnv = function (header, uiInfo) {
        this.id = header.id;
        this.uiInfo = uiInfo;
        this.header = header;
    }

    this.getHtml = function (header) {
        var headerString = '<div style=";position:absolute;border:0px;width:100%;height:100%"' +
            this.setField('id', header.id) +
            this.setField('class', header.styleClass) +
            this.setField('name', header.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            '>';

        headerString += '</div>';
        return headerString;
    }

    this.init = function (header, uiInfo, div) {
        this.initEnv(header, uiInfo);
        if (header.border)
            div.style.border = "1px solid " + Tools.convetColor(header.lineColor);

        div.style.background = Tools.convetColor(header.backgroundColor);

        this.div = $(div).find("#" + header.id)[0];

        this.div.style.overflow = !header.showScrollbar ? "hidden" : "auto";

        this.div.style["overflow-x"] = header.showHorizontalScrollBar ? "auto" : "hidden";
        this.div.style["overflow-y"] = header.showVerticalScrollBar ? "auto" : "hidden";

        if (header.autoColumnWidth && header.showScrollbar) {
            if (header.showHorizontalScrollBar)
                this.div.style["overflow-x"] = "hidden";
        }

        this.div.options = {};
        this.div.options.listview = this;
        this.div.options.frameobj = this;

        if (Tools.isNull(header.header))
            return;

        var rowdatas = [];
        if (!Tools.isNull(header.data)) {
            var rows = Tools.isObject(header.data) ? header.data : JSON.parse(header.data);
            for (var index = 0; index < rows.length; index++) {
                var rowdata = {};
                var columns = rows[index];
                for (var index1 = 0; index1 < columns.length; index1++) {
                    var column = columns[index1];
                    rowdata[column.id] = column;
                }
                rowdatas.push(rowdata);
            }
        }

        this.reset(rowdatas);
    }

    this.write = function (dw, uiInfo, header) {
        var headerString = this.getHtml(header);

        dw.write(header, headerString);

        this.init(header, uiInfo, header.tabParent);
    }

    /**
     * 打印此listview，仅当listview中的每一个cell都是报表
     */
    this.print = function (paging) {
        for (var index = 0; index < this.reports.length; index++) {
            var report = getFrameControl(this.reports[index]);
            report.switchToImage();
        }

        if (!Tools.isNull(paging) && paging) {
            var divs = [];
            for (var i = 0; i < this.getRowCount(); i++) {
                for (var j = 0; j < this.getColumnCount(); j++) {
                    var div = this.getCellDiv(i, this.getFieldId(j));
                    if (Tools.isNull(div) || $(div).children().length == 0)
                        continue;
                    divs.push(div);
                }
            }

            Tools.printDiv(divs, true, Tools.getDpiScaleFromUIInfo(this.uiInfo));
        } else
            Tools.printDiv(this.div, false, Tools.getDpiScaleFromUIInfo(this.uiInfo));

        for (var index = 0; index < this.reports.length; index++) {
            var report = getFrameControl(this.reports[index]);
            report.switchImageToControl();
        }
    }
}