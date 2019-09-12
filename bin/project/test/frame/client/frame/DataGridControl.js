var GridOptionsKey = "datagridcontrol";

function GLOBAL_DATAGRID_CELLCLICK(gridid, linkString, field, rowIndex, value) {
    var grid = mini.get(gridid);
    var dgc = grid.options[GridOptionsKey];

    var link = JSON.parse(linkString.replace(/'/g, "\""));
    var dotype = "click";
    if (link.hasOwnProperty("uiid")) {
        dotype = "uiid";
    }

    switch (dotype) {
        case "click":
            dgc.doCellClick(link, field, rowIndex, value);
            break;
        case "uiid":
            dgc.doCellJump(link, field, rowIndex, value);
            break;
    }
}

function DataGridControl() {
    this.CHECKBOX_TYPE = 0;
    this.INDEX_TYPE = 1;
    this.DATETIME_TYPE = 2;
    this.TEXTBOX_TYPE = 3;
    this.COMBOBOX_TYPE = 4;
    this.SUB_TYPE = 5;
    this.CHECK_TYPE = 6;
    this.INT_TYPE = 7;
    this.CURRENCY_TYPE = 8;
    this.PROGRESS_TYPE = 9;

    this.header;
    this.uiInfo;
    this.grid = undefined;
    this.dataGrid = undefined;

    this.clickLinks = {};
    this.uiLinks = {};
    this.urlLinks = {};

    this.Event = {
        OnCellClick: undefined, //如果单元格设置为click类型，则会触发此事件function(link, field, rowIndex, value)
        OnSumCell: undefined, //合计单元格的绘制事件 function OnSumCell(dgc, grid, result, e.field, e.cellHtml, e.value)，返回值为需要绘制到单元格的html
        onCellEdited: undefined, //单元格编辑结束事件 function onCellEdited(grid, dgc, field, value)，无返回值
        onComboboxLoadData: undefined, //初始下拉字段列的编辑框的下拉列表事件 function onComboboxLoadData(grid, dgc, field, value)，返回jsonarray
        onRowSelected: undefined, //行选中事件function onRowSelected(grid, record)，无返回值
        onRowDeselected: undefined, //行取消选中事件 onRowDeselected(grid, record)，无返回值
        OnChangeCellStyle: undefined, //改变一个单元格的style事件 function OnChangeCellStyle(grid, dgc, e.cellStyle, field, value)，返回值为新style
        onDrawCellValue: undefined, //修改一个单元格的绘制值事件 function onDrawCellValue(grid, dgc, field, value)，返回值：新的cell值
        onDrawCellHtml: undefined, //修改一个单元格的绘制html事件，区别于onDrawCellValue，这个事件的返回值将直接被绘制到单元格 function onDrawCellHtml(grid, dgc, field, value)，返回值：html 
    };

    this.exportToExcel = function (name, sheetname, booleanConvert) {
        var rows = this.grid.getData();
        var cols = [];
        var columns = this.grid.getColumns();
        for (let index = 0; index < columns.length; index++) {
            var column = columns[index];
            if (Tools.isNull(column.field))
                continue;

            cols.push({
                name: column.header,
                field: column.field
            });
        }
        Tools.exportToExcel(rows, cols, name, sheetname, booleanConvert);
    }

    this.doCellJump = function (link, field, rowIndex, value) {
        var name = link.uiname;
        jumpUIForName(name, link.uiid);
    }

    this.doCellClick = function (link, field, rowIndex, value) {
        if (Tools.isNull(this.Event.OnCellClick)) {
            return;
        }

        this.Event.OnCellClick(link, field, rowIndex, value);
    }

    this.setLink = function (e, gid, link, name) {
        if (link.hasOwnProperty("name"))
            name = link.name;

        e.cellStyle = "text-align:center";
        var target = '';
        if (link.hasOwnProperty("target")) {
            target = 'target="' + link.target + '"';
        }

        var download = "";
        if (link.hasOwnProperty("download")) {
            download = 'download="' + link.download + '"';
        }

        if (link.hasOwnProperty("url")) {
            if (Tools.isNull(target))
                target = 'target="view_window"';
            e.cellHtml = '<a href="' + link.url + '" ' + download + ' ' + target + '>' + name + '</a>';
        } else {
            e.cellHtml = '<a href="javascript:GLOBAL_DATAGRID_CELLCLICK(\'' + gid + '\'' +
                ', \'' + JSON.stringify(link).replace(/\"/g, "\\'") + '\'' +
                ', \'' + e.field + '\'' +
                ', \'' + e.rowIndex + '\'' +
                ', \'' + e.value + '\'' +
                ')" ' +
                ' ' + download + ' ' + target + '>' + name + '</a>';
        }

    }

    this.drawUIAndUrlLink = function (dgc, e, defineLinks, id) {
        var value = e.value;
        if (defineLinks.hasOwnProperty("all")) {
            var link = defineLinks.all.all;
            dgc.setLink(e, id, link, value);
        }

        if (defineLinks.hasOwnProperty("row")) {
            var rowDefine = defineLinks["row"];
            if (rowDefine.hasOwnProperty(e.rowIndex)) {
                var link = rowDefine[e.rowIndex];
                dgc.setLink(e, id, link, value);
            }
        }

        if (!defineLinks.hasOwnProperty(e.column.field))
            return;

        if (defineLinks[e.field].hasOwnProperty("col")) {
            var link = defineLinks[e.field]["col"];
            dgc.setLink(e, id, link, value);
        }

        var links = defineLinks[e.field];
        if (links.hasOwnProperty(value)) {
            var link = links[value];
            dgc.setLink(e, id, link, value);
        }
    }

    this.drawLink = function (e) {
        var grid = e.sender;
        var dgc = grid.options[GridOptionsKey];

        this.drawUIAndUrlLink(dgc, e, dgc.clickLinks, grid.id);
        this.drawUIAndUrlLink(dgc, e, dgc.uiLinks, grid.id);
        this.drawUIAndUrlLink(dgc, e, dgc.urlLinks, grid.id);

        // //将性别文本替换成图片
        // if (column.field == "gender") {
        //     if (e.value == 1) {
        //         e.cellHtml = "<span class='icon-female'></span>"
        //     } else {
        //         e.cellHtml = "<span class='icon-boy'></span>"
        //     }
        // }

        // //设置行样式
        // if (record.gender == 1) {
        //     e.rowCls = "myrow";
        // }

    }

    this.processDefineLink = function (definLinks, link) {
        var field = link.field;
        var links = {};

        if (!link.hasOwnProperty("field") && !link.hasOwnProperty("row")) {
            field = "all";
        } else if (link.hasOwnProperty("field") && link.hasOwnProperty("row")) {

        } else if (link.hasOwnProperty("row")) {
            field = "row";
        }

        if (definLinks.hasOwnProperty(field)) {
            links = definLinks[field];
        } else {
            definLinks[field] = links;
        }

        if (field == "row") {
            links[link.row] = link;
        } else if (field == "all") {
            links["all"] = link;
        } else {
            if (link.hasOwnProperty("value"))
                links[link.value] = link;
            else
                links["col"] = link;
        }
    }

    this.initLinks = function (data) {
        this.rowLinks = {};
        this.fieldLinks = {};
        if (Tools.isNull(data) || Tools.isNull(data.links))
            return;

        for (var j = 0; j < data.links.length; j++) {
            var link = data.links[j];
            if (link.hasOwnProperty("url")) {
                this.processDefineLink(this.urlLinks, link);
            } else if (link.hasOwnProperty("click")) {
                this.processDefineLink(this.clickLinks, link);
            } else if (link.hasOwnProperty("uiid")) {
                this.processDefineLink(this.uiLinks, link);
            }
        }

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

    this.setFilterFilter = function (gid, header) {
        if (!Tools.isNull(header.filter))
            return '<input property="filter" style="width:100%;" class="mini-filteredit"' +
                this.setField('gid', gid) +
                this.setField('id', header.filter.name) +
                this.setField('onvaluechanged', header.filter.onvaluechanged) +
                '/>';
        else
            return '';
    }

    this.createColumn = function (gid, header) {
        var str = '';
        switch (header.type) {
            case this.CHECKBOX_TYPE:
                str = '<div type="checkboxcolumn" trueValue="1" falseValue="0" ' +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "30") +
                    this.setField('dataType', "boolean") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('readOnly', header.readOnly) +
                    '>' +
                    this.getValue(header.title, header.field) +
                    this.setFilterFilter(gid, header) +
                    '</div>';
                break;
            case this.CHECK_TYPE:
                str = '<div type="checkcolumn"' +
                    this.setField('headerAlign', header.headerAlign) +
                    this.setField('header', header.title) +
                    '>' +
                    '</div>';
                break;
            case this.INDEX_TYPE:
                str = '<div type="indexcolumn"' +
                    this.setField('headerAlign', header.headerAlign) +
                    this.setField('header', header.title) +
                    '>' +
                    '</div>';
                break;
            case this.DATETIME_TYPE:
                str = '<div ' +
                    this.setField('dataType', "date") +
                    this.setField('type', "date") +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "100") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    this.setField('dateFormat', header.dateFormat, "yyyy-MM-dd HH:mm:ss") +
                    this.setField('allowSort', header.allowSort, "true") +
                    this.setField('readOnly', header.readOnly) +
                    this.setField('summaryType', header.summaryType) +
                    '>' +
                    this.getValue(header.title, header.field) +
                    '<input property="editor" class="mini-datepicker" style="width:100%;"' +
                    '/>' +
                    this.setFilterFilter(gid, header) +
                    '</div>';
                break;
            case this.COMBOBOX_TYPE:
                str = '<div type="comboboxcolumn"' +
                    this.setField('dataType', header.dataType) +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "100") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    this.setField('allowSort', header.allowSort, "true") +
                    this.setField('readOnly', header.readOnly) +
                    this.setField('summaryType', header.summaryType) +
                    '>' +
                    this.getValue(header.title, header.field) +
                    '<input property="editor" class="mini-combobox" style="width:100%;"' +
                    this.setField('valueFromSelect', header.valueFromSelect, "false") +
                    this.setField('allowInput', header.allowInput, "true") +
                    this.setField('textName', header.textName) +
                    this.setField('valueField', header.valueField) +
                    this.setField('textField', header.textField) +
                    this.setField('data', header.onclick) +
                    '/>' +
                    this.setFilterFilter(gid, header) +
                    '</div>';
                break;
            case this.SUB_TYPE:
                str = '<div ' +
                    this.setField('header', header.title) +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    '>';
                break;
            case this.CURRENCY_TYPE:
                str = '<div ' +
                    this.setField('dataType', "currency") +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "100") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    this.setField('allowSort', header.allowSort, "true") +
                    this.setField('currencyUnit', header.currencyUnit) +
                    this.setField('readOnly', header.readOnly) +
                    this.setField('summaryType', header.summaryType) +
                    this.setField('decimalPlaces', header.decimalPlaces) +

                    '>' +
                    this.getValue(header.title, header.field) +
                    ' <input property="editor" class="mini-spinner" ' +
                    this.setField('minValue', header.minValue, "0") +
                    this.setField('maxValue', header.maxValue, "100") +
                    this.setField('value', header.value, "0") +
                    'style="width:100%;"/>' +
                    this.setFilterFilter(gid, header) +
                    '</div>';
                break;
            case this.INT_TYPE:
                str = '<div ' +
                    this.setField('dataType', "int") +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "100") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    this.setField('allowSort', header.allowSort, "true") +
                    this.setField('numberFormat', header.numberFormat) +
                    this.setField('readOnly', header.readOnly) +
                    this.setField('summaryType', header.summaryType) +
                    this.setField('decimalPlaces', header.decimalPlaces) +
                    '>' +
                    this.getValue(header.title, header.field) +
                    ' <input property="editor" class="mini-spinner" ' +
                    this.setField('minValue', header.minValue, "0") +
                    this.setField('maxValue', header.maxValue, "100") +
                    this.setField('value', header.value, "0") +
                    'style="width:100%;"/>' +
                    this.setFilterFilter(gid, header) +
                    '</div>';
                break;
            case this.PROGRESS_TYPE:
                str = '<div type="progresscolumn"' +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "100") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    this.setField('allowSort', header.allowSort, "true") +
                    this.setField('readOnly', header.readOnly) +
                    this.setField('cls', header.cls) +
                    '>' +
                    this.getValue(header.title, header.field) +
                    '</div>';
                break;
            case this.TEXTBOX_TYPE:
            default:
                str = '<div ' +
                    this.setField('name', header.name) +
                    this.setField('field', header.field) +
                    this.setField('width', header.width, "100") +
                    this.setField('headerAlign', header.headerAlign, "center") +
                    this.setField('align', header.align, "center") +
                    this.setField('allowSort', header.allowSort, "true") +
                    this.setField('readOnly', header.readOnly) +
                    this.setField('summaryType', header.summaryType) +
                    this.setField('vtype', header.vtype) +
                    '>' +
                    this.getValue(header.title, header.field) +
                    '<input property="editor" class="mini-textbox" style="width:100%;"' +
                    this.setField('value', header.value, "") +
                    '/>' +
                    this.setFilterFilter(gid, header) +
                    '</div>';
                break;
        }

        return str;
    }

    this.getHtml = function (data) {
        var headerString = '<div' +
            this.setField('id', data.id) +
            this.setField('name', data.name) +
            this.setField('class', Tools.insertString(" ", data.styleClass, "mini-datagrid")) +
            this.setField('idField', data.idField) +
            this.setField('sortMode', data.sortMode, "client") +
            this.setField('showReloadButton', data.showReloadButton) +
            this.setField('allowResize', data.allowResize, "false") +
            this.setField('allowcelledit', data.allowcelledit, "true") +
            this.setField('allowcellselect', data.allowcelledit == "true" ? "true" : data.allowcellselect, "true") +
            this.setField('multiselect', data.multiselect, "true") +
            this.setField('showSummaryRow', data.showSummaryRow, "false") +
            this.setField('showFilterRow', data.showFilterRow) +
            this.setField('showPager', data.showPager) +
            this.setField('style', Tools.addString("width:100%;height:100%;", data.style)) +
            this.setField('allowCellWrap', data.allowCellWrap) +
            this.setField('allowHeaderWrap', data.allowHeaderWrap) +
            this.setField('allowRowSelect', data.allowRowSelect) +
            this.setField('onlyCheckSelection', data.onlyCheckSelection) +
            this.setField('editNextOnEnterKey', data.editNextOnEnterKey) +
            // this.setField('cellEditAction', data.cellEditAction) +
            this.setField('allowCellValid', data.allowCellValid) +
            this.setField('allowUnselect', data.allowUnselect) +
            this.setField('allowAlternating', data.allowAlternating) +
            this.setField('collapseGroupOnLoad', data.collapseGroupOnLoad) +
            this.setField('autoHideRowDetail', data.autoHideRowDetail) +
            this.setField('showModified', data.showModified) +
            this.setField('enableGroupOrder', data.enableGroupOrder) +
            this.setField('skipReadOnlyCell', data.skipReadOnlyCell) +
            this.setField('showPageIndex', false) +
            this.setField('showPageInfo', false) +
            this.setField('navEditMode', data.navEditMode) +
            this.setField('showEmptyText', data.showEmptyText) +
            this.setField('frozenStartColumn', data.frozenStartColumn) +
            this.setField('frozenEndColumn', data.frozenEndColumn) +
            this.setField('pageSize', data.pageSize) +
            this.setField('emptyText', data.emptyText) +
            //this.setField('showLoading', header.showLoading, "true") +
            '>';

        if (!Tools.isNull(data.header)) {
            headerString += '<div property="columns" >';
            for (var i = 0; i < data.header.length; i++) {
                var header = data.header[i];
                headerString += this.createColumn(data.id, header);
                if (!Tools.isNull(header.subs)) {
                    headerString += '<div property="columns">';
                    for (var j = 0; j < header.subs.length; j++) {
                        headerString += this.createColumn(data.id, header.subs[j]);
                    }
                    headerString += '</div>';
                    headerString += '</div>';
                }
            }
            headerString += '</div>';
        }
        headerString += '</div>';

        this.header = data;
        return headerString;
    }

    this.load = function (data) {

        mini.parse();

        this.header = data;

        if (!data.border) {
            $(data.tabParent).find(".mini-grid-border").css("border", "0px");
        }

        this.initOptions();

        this.grid = mini.getByName(data.name, this.header.tabParent);

        if (this.grid == undefined)
            return false;

        this.dataGrid = new TomcatDataGrid(data.name, this.uiInfo);

        this.grid.on("select", this.Event.onRowSelected);
        this.grid.on("deselect", this.Event.onRowDeselected);
        this.grid.on("cellendedit", this.Event.onCellEdited);
        this.grid.on("drawcell", this.OnDrawCell);
        this.grid.on("drawsummarycell", this.onDrawSumCell);
        this.grid.on("cellbeginedit", this.onBeginEditCell);

        this.grid.options[GridOptionsKey] = this;

        if (!Tools.isNull(data.value)) {
            this.grid.loading();
            try {
                var initData = JSON.parse(data.value);
                this.grid.setData(initData.data);
            } catch (error) {

            }
            this.grid.unmask();
        }
        this.initLinks(this.header);

        this.onLogin();
    }

    this.onBeginEditCell = function (e) {
        var editor = e.column.editor;
        if (!Tools.isNull(editor) && editor.type == "combobox") {
            var grid = e.sender;
            var dgc = grid.options[GridOptionsKey];
            editor.on("valuechanged", function (e1) {
                if (Tools.isNull(dgc.Event.onCellEdited))
                    return;
                dgc.Event.onCellEdited(grid, dgc, e.column.field, e1.value);
            });

            var data = e.column.downdata;
            if (Tools.isNull(data))
                data = dgc.initComboboxData(e.column, e.value);
            if (!Tools.isNull(data)) {
                var comData = [];
                for (var key in data) {
                    var item = {};
                    item["id"] = data[key]["value"];
                    item["text"] = data[key]["text"];
                    comData.push(item);
                }
                editor.setData(comData);
            }
        }
    }

    this.initOptions = function () {
        this.grid = mini.getByName(this.header.name, this.header.tabParent);
        this.grid.options = {};
        this.grid.options.frameobj = this;
    }

    this.setDownData = function (downdatas) {
        var columns = this.grid.getColumns();
        for (var index = 0; index < columns.length; index++) {
            var column = columns[index];
            if (Tools.isNull(downdatas[column.field]))
                continue;

            var downdata = downdatas[column.field];
            if (Tools.isNull(downdata))
                continue;

            column.downdata = downdata;
        }
    }

    this.write = function (dw, uiInfo, data) {
        try {
            var jsonTmp = JSON.parse(data.header).header;
            data.header = jsonTmp;
        } catch (error) {}
        var headerString = this.getHtml(data);
        dw.write(data, headerString);
        this.header = data;
        this.uiInfo = uiInfo;
        mini.parse();

        this.grid = mini.getByName(data.name, this.header.tabParent);

        if (!Tools.isNull(data.header)) {
            var downdatas = {};
            var cls = {};
            for (var index = 0; index < data.header.length; index++) {
                const header = data.header[index];
                downdatas[header.field] = header.downdata;
                cls[header.field] = header.cls;
            }

            this.grid = mini.getByName(data.name, this.header.tabParent);
            this.setDownData(downdatas);
            this.setProgressCls(cls);
        }
        this.initOptions();
        this.load(data);

        if (this.header.autoInitDataSource) {
            this.initDataSource();
        }
    }

    this.setProgressCls = function (cls) {
        var columns = this.grid.getColumns();
        for (var index = 0; index < columns.length; index++) {
            var column = columns[index];
            if (Tools.isNull(cls[column.field]))
                continue;

            var cls = cls[column.field];
            if (Tools.isNull(cls))
                continue;

            column.cls = cls;
        }
    }

    this.onLogin = function (name, isOk, delayCallbacks) {
        var userid = GlobalSessionObject.getUserId();
        if (Tools.isNull(userid))
            return;

        var dataGrid = this.grid;
        if (Tools.isNull(this.uiInfo.workflow.runTaskName))
            return;

        var flowName = this.uiInfo.workflow.runTaskName.key;
        if (!Tools.isNull(flowName)) {
            if (this.header.autoRunFlowTasks) {
                var scheduler = new Scheduler();
                var uiName = this.uiInfo.workflow.name;
                var role = GlobalSessionObject.getGroupRole();
                var start = dataGrid.getPageIndex();
                var size = dataGrid.getPageSize();
                var info = scheduler.getTasks(userid, uiName, role, start, size, flowName);
                if (!info.ret) {
                    alert("获取任务列表失败！");
                    return;
                }
                dataGrid.setData(info.data);
            } else if (this.header.autoRunFlowHistoryTasks) {
                var scheduler = new Scheduler();
                var start = dataGrid.getPageIndex();
                var size = dataGrid.getPageSize();
                var result = scheduler.getHistoryTasks(userid, "", start, size, flowName);
                if (!result.ret) {
                    alert("获取历史任务列表失败！");
                    return;
                }
                dataGrid.setData(result.data);
            }
        }
    }

    this.getDataSourceId = function () {
        if (Tools.isNull(header.datasource))
            return [];
        else
            return [header.datasource];
    }

    this.initDataSource = function (params,callback) {
        var header = this.header;

        var dataSourceId = header.dataSource;

        if (Tools.isNull(dataSourceId))
            return;

        var dg = this.dataGrid;
        var ggrid = this.grid;
        if (Tools.isNull(header.field)) {
            if (Tools.isNull(params)) {
                var datasource = Tools.getDataSource(dataSourceId);
                params = Tools.isNull(datasource.params) ? {} : datasource.params;
                params[dataSourceId] = {};
                params[dataSourceId]["start"] = this.grid.getPageIndex();
                params[dataSourceId]["size"] = this.grid.getPageSize();
            } else {
                if (Tools.isNull(params[dataSourceId]["start"]))
                    params[dataSourceId]["start"] = this.grid.getPageIndex();
                if (Tools.isNull(params[dataSourceId]["size"]))
                    params[dataSourceId]["size"] = this.grid.getPageSize();
            }
            GlobalDataSources.get(dataSourceId, true,
                function (ds, columns, dataset, url, queryParams, params) {
                    initGridOptions(ggrid, url, queryParams, params["start"], params["size"]);
                    dg.directLoadData(dataset, params["start"], params["size"], 100);
                    if (!Tools.isNull(callback)) {
                        callback(ggrid);
                    }
                }, params);
        }
    }

    this.init = function (hint, command, conditions) {
        var obj = {
            keys: conditions,
            mark: command
        }

        this.dataGrid.LoadData(obj, hint);
        return true;
    }

    this.onDrawSumCell = function (e) {
        var result = e.result;
        var grid = e.sender;
        var dgc = grid.options[GridOptionsKey];
        if (Tools.isNull(dgc.Event.OnSumCell))
            return;

        e.cellHtml = dgc.Event.OnSumCell(dgc, grid, result, e.field, e.cellHtml, e.value);
    }

    this.initComboboxData = function (column, value) {
        var typename = column.type;
        if (typename == "comboboxcolumn") {
            var data = column.downdata;
            if (Tools.isNull(data) || data.length == 0) {
                if (!Tools.isNull(this.Event.onComboboxLoadData)) {
                    var data = this.Event.onComboboxLoadData(this.grid, this, column.field, value);
                    if (!Tools.isNull(data) && data.length > 0) {
                        column.downdata = {};
                        for (var index = 0; index < data.length; index++) {
                            var element = data[index];
                            column.downdata[element["id"]] = element;
                        }
                    }
                }

            }
            return column.downdata;
        }
        return undefined;
    }

    /*
    e:{
    sender: Object,
    rowIndex: Number,
    columnIndex: Number,
    record: Object,    
    column: Object,
    field: String,
    value: String,
    cellHtml: "",
    rowCls: "",
    cellCls: "",
    rowStyle: "",
    cellStyle: ""
    }
    */
    this.OnDrawCell = function (e) {
        var field = e.field;
        var value = e.value;
        var column = e.column;
        var grid = e.sender;
        var dgc = grid.options[GridOptionsKey];

        if (!Tools.isNull(value)) {
            if (Tools.isNull(e.cellHtml))
                e.cellHtml = value;
        } else
            return;

        if (!Tools.isNull(dgc.Event.OnChangeCellStyle)) {
            var style = dgc.Event.OnChangeCellStyle(grid, dgc, e.cellStyle, field, value);
            if (!Tools.isNull(style))
                e.cellStyle = style;
            //给帐号列，增加背景色
            //        e.cellStyle = "background:#fceee2";
        }

        if (!Tools.isNull(dgc.Event.onDrawCellValue)) {
            var newvalue = dgc.Event.onDrawCellValue(grid, dgc, field, value);
            if (!Tools.isNull(newvalue)) {
                e.value = newvalue;
            }
        }

        if (!Tools.isNull(dgc.Event.onDrawCellHtml)) {
            var newvalue = dgc.Event.onDrawCellHtml(grid, dgc, field, value);
            if (!Tools.isNull(newvalue)) {
                e.cellHtml = newvalue;
            }
        }

        var typename = e.column.type;
        if (typename == "comboboxcolumn") {
            if (Tools.isNull(column.downdata))
                column.downdata = dgc.initComboboxData(e.column, value);
        }

        if (typename == "date") {
            e.cellHtml =Tools.dateFormat(value,column.dateFormat); 
        }

        if (typename == "progresscolumn") {
            var num = parseFloat(value) > 100 ? 100 : value;
            if (!Tools.isNull(column.cls)) {
                e.cellHtml = '<div class="' + column.cls + '">' +
                    '<div style="width:' + num + '%;">' + value + '%</div></div>';
            } else {
                e.cellHtml = '<div class="processcontainer">' +
                    '<div class="processbar" style="width:' + num + '%;">' + value + '%</div></div>';
            }
        }

        if (!Tools.isNull(column.downdata)) {
            if (!Tools.isNull(column.downdata[value]))
                e.cellHtml = column.downdata[value]["text"];
        }

        dgc.drawLink(e);
    }

    this.onsave = function (command, hint, needReload, checkfunfun, checkrow, getOtherSaveDatas, paramObj) {
        if (!checkfun(paramObj))
            return false;

        var row = this.grid.getSelecteds();
        if (row == null || row == undefined || row == "") {
            return false;
        }

        for (var i = 0; i < row.length; i++) {
            if (!this.checkrow(row[i], paramObj)) {
                return false;
            }
        }

        postDatas = [];
        var json = this.dataGrid.getsaveData();
        if (json == null || json == undefined || json == "")
            return false;
        obj_table = DBOperation.GetSaveData(json);
        postDatas.push(obj_table);

        json = this.getOtherSaveDatas(rows, paramObj);
        if (json != null && json != undefined && json != "") {
            if (json instanceof Array) {
                for (var obj in json) {
                    obj_table = DBOperation.GetSaveSimpleData(obj);
                    post_datas.push(obj_table);
                }
            } else {
                obj_table = DBOperation.GetSaveSimpleData(json);
                post_datas.push(obj_table);
            }
        }

        var isok = DBOperation.SaveSimpleDatas(post_datas, command, hint);
        if (isok && needReload)
            this.grid.reload();

        return isok;
    }
}