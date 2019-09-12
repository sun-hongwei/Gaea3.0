function loadGridDataForData(ggrid, data, columnCallback) {
    var needAlert = false;
    var errMsg = "";
    if (Tools.isNull(data) || data.ret != 0) {
        ggrid.setData("{}");
        needAlert = true;
        if (!Tools.isNull(data.err))
            errMsg = data.err;
    } else {
        if (Tools.isNull(data.data)) {
            ggrid.clearRows();
            return;
        }
        ggrid.options.data = data.data.params;
        var griddata = data.data.data;

        // if (!Tools.isNull(data.data.params.total)) {
        //     ggrid.setTotalCount(data.data.params.total);
        // }


        ggrid.setPageSize(data.data.params.pagesize);
        ggrid.setPageIndex(data.data.params.pageindex);

        var total = ggrid.getPageIndex() * ggrid.getPageSize() + griddata.length;
        if (!Tools.isNull(griddata) && griddata.length > 0) {
            if (ggrid.getTotalCount() <= total)
                ggrid.setTotalCount(total + 1);
        } else {
            ggrid.setTotalCount(total);
        }

        if (ggrid.oncustombeforeload != undefined) {
            ggrid.oncustombeforeload(griddata);
        }

        if (!Tools.isNull(data.data.columns)) {
            ggrid.setColumns(data.data.columns);
        } else if (!Tools.isNull(columnCallback)) {
            columnCallback(griddata);
        } else if (ggrid.getColumns() == null || ggrid.getColumns().length == 0) {
            var row = griddata[0];
            var columns = [{
                header: "索引",
                type: "indexcolumn",
                name: "index",
                width: 30,
                visible: true
            }];
            for (var field in row) {
                columns.push({
                    header: field,
                    field: field,
                    name: field,
                    editor: {
                        cls: "mini-textbox",
                        type: "textbox",
                        style: "width:100%"
                    }
                });
            }
            ggrid.setColumns(columns);
        }

        ggrid.setData(griddata);
        if (ggrid.onuserload != undefined) {
            ggrid.onuserload(griddata);
        }
    }
    ggrid.unmask();

    if (needAlert) {
        alert("请求数据失败：" + errMsg + "!")
    }
}

function initGridOptions(ggrid, queryUri, queryKeys, pageindex, pagesize) {
    if (Tools.isNull(ggrid.options))
        ggrid.options = {};
    ggrid.options.queryUri = queryUri;
    ggrid.options.queryKeys = queryKeys;
    ggrid.options.grid = ggrid;
    ggrid.options.pagesize = pagesize;
    ggrid.options.pageindex = pageindex;
}

function loadGridData(ggrid, queryUri, queryKeys, pageindex, pagesize, columnCallback) {
    ggrid.loading();

    initGridOptions(ggrid, queryUri, queryKeys, pageindex, pagesize);

    if (Tools.isJson(queryKeys)) {
        queryKeys = JSON.stringify(queryKeys);
    }

    Tools.simpleTomcatSubmit(queryUri, {
        command: queryKeys,
        start: pageindex,
        size: pagesize,
        querykeys: queryKeys,
        pageindex: pageindex,
        pagesize: pagesize
    }, function (data) {
        data = {
            ret: data.ret,
            data: {
                data: data.data.DATA == undefined ? data.data.data : data.data.DATA,
                params: {
                    pageindex: pageindex,
                    pagesize: pagesize
                }
            }
        };
        loadGridDataForData(ggrid, data, columnCallback);
    });

}

function dataGrid_GetGridObject(eid, uiInfo) {
    if (!Tools.isNull(uiInfo))
        return getFrameControlByName(uiInfo, eid);
    else {
        return mini.get(eid);
    }
}

function TomcatDataGrid(eid, uiInfo, columnCallback) {

    this.columnCallback = columnCallback;
    this.grid = dataGrid_GetGridObject(eid, uiInfo);

    this.beforeload = undefined;

    /**
      [
           {header:"索引", type:"indexcolumn", name:"index", width:30},
           {header:"编号", field:"id", name:"id", width:50, dataType:"int"},
           {header:"名称", field:"name", name:"name", width:150, dataType:"string"},
           {header:"说明", field:"memo", name:"memo", width:150, dataType:"string"},
      ]
     * */

    this.internalbeforeload = function (e) {
        e.cancel = true;
        var params = e.source.options;
        var queryUri = params.queryUri;
        var queryKeys = params.queryKeys;
        if (typeof (queryKeys) == 'string') {
            queryKeys = JSON.parse(queryKeys);
        }

        if (!Tools.isNull(queryKeys.param)) {
            queryKeys.param.start = e.params.pageIndex;
            queryKeys.param.size = e.params.pageSize;
        }

        var pagesize = e.params.pageSize;
        var pageindex = e.params.pageIndex;
        var ggrid = e.source;
        loadGridData(ggrid, queryUri, queryKeys, pageindex, pagesize, this.columnCallback);
    }

    this.GetValue = function (propertyName) {
        var result = null;
        if (this.grid != null) {
            try {
                var miniStr = "result = this.grid.get" + propertyName.slice(0, 1).toUpperCase() + propertyName.slice(1, propertyName.length) + "()";
                eval(miniStr);
            } catch (e) {
                result = $(this.grid).attr(propertyName);
            }
        }
        return result;
    }

    this.SetValue = function (value, propertyName) {
        if (this.grid != null) {
            try {
                var miniStr = "this.grid.set" + propertyName.slice(0, 1).toUpperCase() + propertyName.slice(1, propertyName.length) + "(value);";
                eval(miniStr);
            } catch (e) {
                $(this.grid).attr(propertyName, value);
            }
        }
    }

    /*
           给元素设置一个事件
    eventname: string, scriptfilename: string, functionname: string
    */
    this.SetEvent = function (eventname, scriptfilename, functionname) {
        if (this.id != null) {
            if (scriptfilename != ".") {
                $.ajax({
                    url: scriptfilename,
                    dataType: "script",
                    cache: false,
                    async: false
                });
            }
            if (this.grid != null) {
                try {
                    this.grid.on(eventname, function (eventArgs) {
                        var userEvent = "var obj = " + functionname + "(eventname, this.id, eventArgs)";
                        eval(userEvent);
                    });
                } catch (e) {
                    $(this.grid).bind(eventname, null, function (eventArgs) {
                        var userEvent = "var obj = " + functionname + "(eventname, this.id, eventArgs)";
                        eval(userEvent);
                    });
                }
            }
        }
    }

    //获取DataGridControl对象
    this.getControl = function () {
        return this.grid.options[GridOptionsKey];
    }

    this.setValue = function (columnName, rowIndex, value) {
        var row = this.grid.getRow(rowIndex);
        if (Tools.isNull(columnName)) {
            // var keys = Object.keys(value);
            for (var field in value) {
                this.editValue(field, value[field], row);
            }
        } else {
            this.editValue(columnName, value, row);
        }
    }

    this.getColumnValues = function (columnName) {
        var data = this.grid.getData();
        var result = [];
        for (var index = 0; index < data.length; index++) {
            var row = data[index];
            result.push(row[columnName]);
        }
    }

    this.setColumnValues = function (columnName, value) {
        var result = [];
        var isArray = Tools.isArray(value);
        for (var index = 0; index < this.grid.data.length; index++) {
            var row = this.grid.getRow(index);
            this.editValue(columnName, (isArray ? value[index] : value), row);
        }
    }

    this.getValue = function (columnName, rowIndex) {
        var row = this.grid.getRow(rowIndex);
        if (Tools.isNull(columnName))
            return row;
        return row[columnName];
    }

    //添加行
    this.addRow = function (fieldname) {
        var newRow = {};
        var rows = this.grid.getData();
        var rowIndex = rows.length;
        this.grid.addRow(newRow, rowIndex)
        if (!Tools.isNull(fieldname)) {
            var colIndex = -1;
            for (var index = 0; index < this.grid.columns.length; index++) {
                var element = this.grid.columns[index];
                if (element.field == fieldname) {
                    colIndex = index;
                    break;
                }
            }
            if (colIndex == -1)
                return;

            var cell = [rowIndex, colIndex];
            this.grid.cancelEdit();
            this.grid.setCurrentCell(cell);
            this.grid.beginEditCell();
        }
    }

    this.editRow = function () {
        var row = this.grid.getSelected();
        if (row) {
            if (this.isEditing())
                this.grid.cancelEdit();
            this.grid.beginEditRow(row);
        }
    }

    this.isEditing = function () {
        return this.grid.isEditing();
    }

    this.commitEdit = function () {
        if (this.grid.isEditing()) {
            this.grid.commitEdit();
        }
    }

    this.getRow = function () {
        return this.grid.getSelected();
    }

    //删除行
    this.removeRow = function () {
        this.grid.cancelEdit();
        var rows = this.grid.getSelecteds();
        if (rows.length > 0) {
            this.grid.removeRows(rows, true);
        }
    }

    this.setEvent = function (eventName, event) {
        this.getControl().Event[eventName] = event;
    }

    this.editValue = function (fieldname, value, selectRow) {
        var row = Tools.isNull(selectRow) ? this.grid.getSelected() : selectRow;
        var data = {};
        data[fieldname] = value;
        this.grid.updateRow(row, data);
    }

    this.editRowValue = function (data, selectRow) {
        var row = Tools.isNull(selectRow) ? this.grid.getSelected() : selectRow;
        this.grid.updateRow(row, data);
    }

    this.clearRow = function () {
        this.grid.clearRows();
    }

    this.directLoadData = function (dataset, pageindex, pagesize, total) {
        this.grid.un("rowclick",this.initrowclick);
        this.grid.on("rowclick",this.initrowclick);
        this.grid.un("cellclick", this.initcellclick);
        this.grid.on("cellclick", this.initcellclick);
        this.grid.un("beforeload", this.internalbeforeload);
        this.grid.on("beforeload", this.internalbeforeload);
        var data = {
            ret: 0,
            data: {
                data: dataset,
                params: {
                    total: total,
                    pagesize: pagesize,
                    pageindex: pageindex
                }
            }
        };

        loadGridDataForData(this.grid, data);
    }

    this.LoadData = function (ajaxurl, querykeys, pagesize, pageindex) {
        var ggrid = this.grid;
        ggrid.un("rowclick",this.initrowclick);
        ggrid.on("rowclick",this.initrowclick);
        ggrid.un("cellclick", this.initcellclick);
        ggrid.on("cellclick", this.initcellclick);
        ggrid.un("beforeload", this.internalbeforeload);
        ggrid.on("beforeload", this.internalbeforeload);

        loadGridData(ggrid, ajaxurl, querykeys, pageindex, ggrid.pageSize, this.columnCallback);

    }


    //将行集中的重复数据去掉，返回每行都唯一的行集
    this.getsimpleData = function (rows, keys) {
        var vkeys = {};
        var vdatas = [];
        for (var i = 0; i < rows.length; i++) {
            var key = "";
            for (var k = 0; k < keys.length; k++) {
                key += rows[i][keys[k]];
            }
            if (vkeys[key] == undefined || vkeys[key] == null) {
                vkeys[key] = rows[i];
                vdatas.push(rows[i]);
            }
        }
        return vdatas;
    }

    //获取与目标行集中重复的行,如果isdouble=true那么返回所有重复的行，否则返回所有不重复的行。
    this.getdoubleData = function (rows, sourcekeys, destkeys, isdouble) {
        var vdatas = [];
        var vdests = {};
        for (var i = 0; i < this.grid.data.length; i++) {
            var key = "";
            for (var k = 0; k < destkeys.length; k++) {
                key += this.grid.data[i][destkeys[k]];
            }
            vdests[key] = this.grid.data[i];
        }

        for (var i = 0; i < rows.length; i++) {
            var key = "";
            for (var k = 0; k < sourcekeys.length; k++) {
                key += rows[i][sourcekeys[k]];
            }
            if (!isdouble) {
                if (vdests[key] == undefined || vdests[key] == null) {
                    vdatas.push(rows[i]);
                }
            } else {
                if (vdests[key] != undefined && vdests[key] != null) {
                    vdatas.push(rows[i]);
                }
            }
        }
        return vdatas;
    }

    this.apply = function () {
        this.grid.accept();
    }

    //将数据更改保存到服务端
    this.saveData = function (callback) {
        var dataJson = this.getsaveData();
        var ggrid = this.grid;
        ggrid.loading("保存中，请稍后......");
        var queryUri = ggrid.options.queryUri;
        var queryKeys = ggrid.options.queryKeys;
        var pagesize = ggrid.options.pagesize;
        var pageindex = ggrid.options.pageindex;
        var isdo = false;

        var index = queryUri.lastIndexOf("/");
        queryUri = queryUri.substring(0, index + 1) + "savegrid";
        Tools.simpleTomcatSubmit(queryUri, {
            query: queryKeys,
            pageindex: pageindex,
            pagesize: pagesize,
            data: JSON.stringify(dataJson),
        }, function (data) {
            isdo = data.ret == 0;
            if (isdo)
                ggrid.accept();
            ggrid.unmask();
            if (!Tools.isNull(callback))
                callback(isdo)
        });

    }

    //获取要保存到服务端的数据
    this.getSaveData = function () {
        var data = this.grid.getChanges();
        var json = [];
        for (var i = 0; i < data.length; i++) {
            // var dataStr = mini.encode(data[i]);
            // dataStr = dataStr.replace(/T00:00:00/g, '');
            // var sdata = mini.decode(dataStr);
            json.push(data[i]);
        }

        return json;
    }

    this.getData = function (tree) {
        var result = [];
        if (this.grid) {
            if (tree) {
                var data = this.grid.getList();
            } else {
                var data = this.grid.getData();
            }
            for (var i = 0; i < data.length; i++) {
                var newJson = {};
                for (var k in data[i]) {
                    if (k != "_uid" && k != undefined && k != "_level" && k != "children") {
                        if (!tree) {
                            if (k == "_id") {
                                return false;
                            }
                        }
                        newJson[k] = data[i][k];
                    }
                }
                result.push(newJson);
            }
        }
        return result;
    }

    this.initcellclick = function (e) {

    };
    this.initrowclick = function (e) {

    }
}