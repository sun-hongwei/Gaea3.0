function internalbeforeload(e) {
    e.cancel = true;
    e.source.loading();
    var json = Tools.createAjaxPackage(e.source.options["mark"], e.source.options["keys"]);
    json["data"]["value"][0]["params"]["PAGECOUNT"] = e.params.pageSize;
    json["data"]["value"][0]["params"]["PAGEINDEX"] = e.params.pageIndex;
    json["Command_Running_Hint"] = e.source.options["memo"];

    Tools.updateAjaxPackageSign(json);

    var pagecount = e.params.pageSize;
    var pageindex = e.params.pageIndex + 1;
    Tools.ajaxSubmit({
        url: e.source.options["ajaxurl"],
        data: json,
        success: function (griddatatxt) {
            var ggrid = e.source.options["grid"];
            if (Tools.isNull(griddatatxt)) {
                ggrid.setData("{}");
                return;
            }
            var data = mini.decode(griddatatxt);
            ggrid.options["data"] = data;
            //ggrid.setTotalCount(data["data"]["total"]);
            ggrid.setPageSize(data["params"]["PAGECOUNT"]);
            ggrid.setPageIndex(data["params"]["PAGEINDEX"]);

            var griddata = data["data"]["data"];
            if (ggrid.oncustombeforeload != undefined) {
                ggrid.oncustombeforeload(griddata);
            }
            ggrid.setData(griddata);
            if (ggrid.onuserload != undefined) {
                ggrid.onuserload(griddata);
            }
            //return data["data"]["data"];
        }
    });
    e.source.unmask();
}

function dataGrid_GetGridObject(eid, uiInfo){
    if (!Tools.isNull(uiInfo))
        return getFrameControlByName(uiInfo, eid);
    else{
        return mini.get(eid);
    }
}

function DataGrid(eid, uiInfo) {

    this.primaryKeyValues = {};
    this.grid = dataGrid_GetGridObject(eid, uiInfo);

    this.beforeload = undefined;

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
        var row = this.getRow();
        if (row) {
            this.grid.cancelEdit();
            this.grid.beginEditRow(row);
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

    this.editRow = function (data, selectRow) {
        var row = Tools.isNull(selectRow) ? this.grid.getSelected() : selectRow;
        this.grid.updateRow(row, data);
    }

    this.clearRow = function () {
        this.grid.clearRows();
    }

    /*
    requires：打开表格需要的参数，格式 ：
     {
     keys:{key2:value2,key1:value1},\\键值数据
     mark:"zhyo1",\\数据打开标志 
	 PAGECOUNT:"100",\\每页记录数量
     pageno:"0",\\起始页号,
     }
    */

    //请求格式：{"sid":"session id","siinfo":"session info","username":"username", "command":"指令","data":{"value":[{"fire_operation_id":"操作id",params:{"key1":"value1","key2":"value2"}}],"sign":"数据签名"}}
    //请求格式：{"sid":"session id","siinfo":"session info","username":"username", "command":"指令","data":{"value":[{"fire_operation_id":"操作id",params:{"key1":"value1","key2":"value2"}}],"sign":"数据签名"}}
    this.LoadData = function (requires, pagecount, pageindex, memo) {
        var ajaxurl = "./Services/DataGridServices.php";
        var ggrid = this.grid;
        ggrid.loading();
        var require = requires.keys;
        require["PAGEINDEX"] = pageindex;
        require["PAGECOUNT"] = pagecount;
        var json = Tools.createAjaxPackage(requires.mark, requires.keys);
        json["Command_Running_Hint"] = memo;
        this.grid.un("beforeload", internalbeforeload);
        this.grid.on("beforeload", internalbeforeload);


        var onbeforeload = ggrid.oncustombeforeload;
        var onuserload = ggrid.onuserload;
        Tools.updateAjaxPackageSign(json);




        Tools.ajaxSubmit({
            url: ajaxurl,
            async: false,
            data: json,
            success: function (griddatatxt) {
                var data = mini.decode(griddatatxt);

                if (ggrid.options == undefined || ggrid.options == null)
                    ggrid.options = {};
                ggrid.options["data"] = data;
                ggrid.options["mark"] = requires.mark;
                ggrid.options["keys"] = requires.keys;
                ggrid.options["memo"] = memo;
                ggrid.options["ajaxurl"] = ajaxurl;
                ggrid.options["grid"] = ggrid;
                if (!Tools.isNull(data["data"])) {

                    //不填写不能计算索引列
                    if (undefined != data["data"]["total"]) {
                        ggrid.setTotalCount(data["data"]["total"]);
                    }

                    if (undefined != data["params"]["PAGECOUNT"]) {
                        ggrid.setPageSize(data["params"]["PAGECOUNT"]);
                    }

                    if (undefined != data["params"]["PAGEINDEX"]) {
                        ggrid.setPageIndex(data["params"]["PAGEINDEX"]);
                    }


                    //////////////////////////////////////////////////////

                    var griddata = data["data"]["data"];

                    if (onbeforeload != undefined) {
                        onbeforeload(griddata);
                    }

                    //此处griddata的格式为：{data:[{},{}],id:1},其中data必须和miniui的格式一致，id等则随意，为附加数据,比如用于统计列的数据。

                    ggrid.setData(griddata);

                    if (onuserload != undefined) {
                        onuserload(griddata);
                    }
                } else {
                    alert("数据表格数据初始失败，可能您已登录超时！");
                    //window.history.go(-1);
                }

                //return data["data"]["data"];
            }

        });
        ggrid.unmask();
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

    //将数据更改保存到服务端
    this.saveData = function (memo) {
        var dataJson = this.getsaveData(memo);
        var json = Tools.createAjaxPackage("save", dataJson);
        Tools.updateAjaxPackageSign(json);

        this.grid.loading("保存中，请稍后......");

        var isdo = false;
        Tools.ajaxSubmit({
            url: this.grid.options["ajaxurl"],
            data: json,
            success: function (griddatatxt) {
                var data = mini.decode(griddatatxt);
                isdo = data['ret'];
            },
            async: false
        });

        this.grid.unmask();

        return isdo;
    }

    //获取要保存到服务端的数据
    this.getsaveData = function (memo) {
        var dataJson = [];
        var params = this.grid.options["data"];
        var data = this.grid.getChanges();

        var json = {};
        json["value"] = {};
        for (var i = 0; i < data.length; i++) {
            var dataStr = mini.encode(data[i]);
            dataStr = dataStr.replace(/T00:00:00/g, '');
            var sdata = mini.decode(dataStr);
            json["value"][i] = {};
            json["value"][i]["DATA"] = sdata;
        }
        json["TABLENAMES"] = params["params"]["TABLENAMES"];
        json["FILTER"] = params["params"]["FILTER"];
        json["FieldNames"] = params["params"]["FIELDS"];
        json["ORDER"] = params["params"]["ORDER"];
        json["GROUP"] = params["params"]["GROUP"];
        json["PAGECOUNT"] = params["params"]["PAGECOUNT"];
        json["PAGENO"] = params["params"]["PAGEINDEX"];
        json["KEYS"] = params["params"]["KEYS"];

        var rjson = {
            GRIDDATA: json,
            opertype: "gridsave"
        }
        rjson["Command_Running_Hint"] = memo;

        return rjson;
    }
}