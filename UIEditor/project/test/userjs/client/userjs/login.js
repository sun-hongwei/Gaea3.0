function login_start(uiInfo, initdata) {
    this.getCommand = function () {
        control = getFrameControlByName(uiInfo, "command");
        var command = getFrameControlValue(control);
        return command;
    }
    this.getCommandUri = function () {
        return this.getUri(this.getCommand());
    }

    this.getUri = function (uri) {
        var control = getFrameControlByName(uiInfo, "pageview");
        var baseUrl = getFrameControlValue(control);
        return baseUrl + uri;
    }

    var globalLogin = this;
    GlobalEncrypt.initService(function (isok) {
        // var data = GlobalEncrypt.encrypt("RSA is ok!", GlobalEncrypt.RSA);
        // var value = GlobalEncrypt.decrypt(data, GlobalEncrypt.RSA);
        // alert(value);
        // data = GlobalEncrypt.encrypt("AES is ok！", GlobalEncrypt.AES);
        // value = GlobalEncrypt.decrypt(data, GlobalEncrypt.AES);
        // alert(value);

        // Tools.simpleTomcatSubmit(
        //     "http://localhost:8080/JSPAdapterServer/ex/sample/gets", { id: "abc你好这是个测试aaa！" },
        //     function(data) {
        //         if (data.ret != 0)
        //             alert(JSON.stringify(data.data));
        //     }, true);

        alert("加密信道初始完毕");
    });

    ControlHelp.setEvent(uiInfo, "printListview", "onClick", function (e, uiInfo) {
        var listview = getFrameControlObjectByName(uiInfo, "f5b775cc-3875-4597-bfc5-c2dd58d02bfb");
        listview.print(true);
    });

    ControlHelp.setEvent(uiInfo, "printreport", "onClick", function (e, uiInfo) {
        var report = getFrameControlObjectByName(uiInfo, "reportMain");
        report.print();
    });

    ControlHelp.setEvent(uiInfo, "printbacode", "onClick", function (e, uiInfo) {
        var report = getFrameControlObjectByName(uiInfo, "reportMain");
        var divControl = report.getCellFrameControl("barcodeDiv");
        divControl.print();
    });

    ControlHelp.setEvent(uiInfo, "reportMain", "onReportClick", function (report, element, id, name) {
        var value = getFrameControlValue(element);
        alert("您单击的是[" + value + "]！");
    });

    var control = getFrameControlByName(uiInfo, "reportMain");
    control = getFrameControl(control);
    var reportListView = control.getControlByName("5a991761-84f8-4425-9215-c61aac66be2a");
    ControlHelp.setEventForControl(uiInfo, reportListView, "onListViewClick", function(listView, row, header, e, report){
        var value = getFrameControlValue(e);
        alert(value);
    });
    ControlHelp.setEvent(uiInfo, "linkGrid", "OnCellClick", function (link, field, rowIndex, value) {
        alert("您单击的是[" + rowIndex + "," + field + "]单元格，值：[" + value + "]！");
    });

    ControlHelp.setEvent(uiInfo, "refresh", "onClick", function (file) {
        var control = getFrameControlByName(uiInfo, "f5b775cc-3875-4597-bfc5-c2dd58d02bfb");
        resetDataSourceControl(control);
    });

    ControlHelp.setEvent(uiInfo, "f5b775cc-3875-4597-bfc5-c2dd58d02bfb",
        "onListViewClick",
        function (listview, row, header, control, report) {
            var value;
            if (report != null) {
                var value = getFrameControlValue(control, control.id);
            } else {
                var value = getFrameControlValue(listview, header.id, row);
            }
            alert("值：[" + value + "]！");
        });

    ControlHelp.setEvent(uiInfo, "getReport", "onClick", function (e, uiInfo) {
        var report = getListViewReportControl(uiInfo,
            "f5b775cc-3875-4597-bfc5-c2dd58d02bfb", 0, "a2");
        var reportSubControlId = "b8891134-cb31-4395-983e-10ff7b7ce9d3"; //报表中的控件name
        var value = getFrameControlValue(report, reportSubControlId);
        alert("第1行，第二个报表的listbox值：[" + value + "]！");
    });

    ControlHelp.setEvent(uiInfo, "setReport", "onClick", function (e, uiInfo) {
        var report = getListViewReportControl(uiInfo,
            "f5b775cc-3875-4597-bfc5-c2dd58d02bfb", 0, "a2");
        var reportSubControlId = "b8891134-cb31-4395-983e-10ff7b7ce9d3"; //报表中的控件name
        setFrameControlValue(report, "[{\"text\":\"这是测试\"}]", reportSubControlId);
    });

    ControlHelp.setEvent(uiInfo, "testdatasource", "onClick", function (e, uiInfo) {
        var control = getFrameControlByName(uiInfo, "datasourcecommand");
        var commandValue = getFrameControlValue(control);
        var postData = {
            command: JSON.stringify({
                command: commandValue
            })
        };
        Tools.simpleTomcatSubmit("/report/sample/a1", postData, function (data) {
            if (data.ret == 0) {
                var control = getFrameControlByName(uiInfo, "resultview");
                var text = Tools.tomcatResultConvert(data.data);
                setFrameControlValue(control, text);

            } else {
                alert("获取数据源失败！");
            }
        });
        return {
            command: "sample",
            data: {
                page: "login"
            }
        };
    });

    ControlHelp.setEvent(uiInfo, "uploader", "onGetUserData", function (file) {
        return {
            command: "sample",
            data: {
                page: "login"
            }
        };
    });

    ControlHelp.setEvent(uiInfo, "uploader", "onUploadEnd", function (uploadItem, filename) {
        alert(filename + "上传完毕！");
    });

    ControlHelp.setEvent(uiInfo, "resetButton", "onClick", function (file) {
        Tools.simpleTomcatSubmit(globalLogin.getUri("reset"), {}, function (data) {
            if (data.ret == 0)
                alert("数据重置成功！");
            else {
                alert("数据重置失败！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "refreshRole", "onClick", function (file) {
        Tools.simpleTomcatSubmit(globalLogin.getUri("reset/role"), {}, function (data) {
            if (data.ret == 0)
                alert("权限重置成功！");
            else {
                alert("权限重置失败！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "loginButton", "onClick", function (file) {
        var control = getFrameControlByName(uiInfo, "userView");
        var userid = getFrameControlValue(control);
        GlobalUserLogin.login(userid, "123", "", "", function (isok) {
            if (!isok)
                alert("登录失败！");
            else {
                alert("登录成功！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "roleTest", "onClick", function (file) {
        var control = getFrameControlByName(uiInfo, "userView");
        var userid = getFrameControlValue(control);
        GlobalUserLogin.login(userid, "123", "", "", function (isok) {
            if (isok)
                jumpUIForName("测试权限", "rolemodel");
            else {
                alert("登录失败！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "logoffButton", "onClick", function (file) {
        GlobalSessionObject.clear();
        Tools.simpleTomcatSubmit("/ex/user/logoff", {}, function (data) {
            if (data.ret != 0)
                alert("未成功登出！");
            else {
                alert("已经推出登录！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "openfile", "onClick", function (e, uiInfo, controlname) {
        Tools.openFileSelect(function (files) {
            for (let index = 0; index < files.length; index++) {
                var element = files[index];
                ExcelTools.importExcel(element, function (jsons) {
                    for (let index = 0; index < jsons.length; index++) {
                        var element = jsons[index];
                        Tools.saveFileSelect("测试的excel导出.xls", ExcelTools.exportExcel(element));
                        //alert(JSON.stringify(element));
                    }
                });
                //alert(element.name);
            }
        })
    });

    ControlHelp.setEvent(uiInfo, "crypteButton", "valuechanged", function (e, uiInfo, controlname) {
        var crypteButton = getFrameControlByName(uiInfo, "crypteButton");
        var isCrypt = getFrameControlValue(crypteButton) == 1;
        Tools.requestCrypted = isCrypt;
        Tools.responseCrypted = isCrypt;
    });

    ControlHelp.setEvent(uiInfo, "submitButton", "onClick", function (e, uiInfo, controlname) {
        control = getFrameControlByName(uiInfo, "query");
        var queryKey = getFrameControlValue(control);

        if (globalLogin.getCommand() == "edit/update") {
            control = getFrameControlByName(uiInfo, "idview");
            var id = getFrameControlValue(control);
            control = getFrameControlByName(uiInfo, "nameview");
            var name = getFrameControlValue(control);
            control = getFrameControlByName(uiInfo, "descview");
            var memo = getFrameControlValue(control);
            queryKey = {
                id: id,
                name: name,
                memo: memo
            };
        } else {
            if (!Tools.isNull(queryKey)) {
                queryKey = {
                    "id": queryKey
                };
            } else {
                queryKey = {};
            }
        }

        var ajaxUrl = globalLogin.getCommandUri();

        Tools.simpleTomcatSubmit(ajaxUrl, queryKey, function (data) {
            if (data.ret != 0)
                alert(JSON.stringify(data.data));
            else {
                var jsonObj = data.data;
                var control = getFrameControlByName(uiInfo, "resultview");
                var text = Tools.tomcatResultConvert(jsonObj);
                setFrameControlValue(control, text);
            }
        });
    })

    ControlHelp.setEvent(uiInfo, "queryGrid", "onClick", function (e, uiInfo, controlname) {
        control = getFrameControlByName(uiInfo, "query");
        var queryKey = getFrameControlValue(control);
        if (!Tools.isNull(queryKey))
            queryKey = {
                id: queryKey
            };

        var datagrid = new TomcatDataGrid("grid");
        datagrid.LoadData(globalLogin.getUri("querygrid"), queryKey,
            10, 0);
    });

    ControlHelp.setEvent(uiInfo, "saveGrid", "onClick", function (e, uiInfo, controlname) {
        var datagrid = new TomcatDataGrid("grid");
        /**
         * 更新表格
        datagrid.saveData(function(isok) {
            if (isok) {
                alert("数据库更新成功！");
            }
        })
         */

        //新建一个服务端数据保存代理，tag为服务端自定义标识
        var postData = new RemoteDataset("tag2");
        postData.add({
            id: 0,
            idkey: 0,
            value: "测试值a1"
        }); //新增一行，必须包括主键字段值
        postData.add({
            id: 1,
            idkey: 0,
            value: "测试值a2"
        }); //新增一行
        postData.join({
            id: 2,
            idkey: 0,
            value: "测试值a3"
        }); //新增一行，但不填写保存标志
        //将刚join的行添加【更新】的保存标志，可用标志为：Insert、Update、Delete
        postData.setState(postData.Insert);

        var griddata = datagrid.getSaveData(); //获取 数据表格中的用户编辑数据
        //将datagrid的编辑数据加入到待更新数据集合中
        postData.pushDataset(griddata, "tag1");

        /**
         * 提交数据修改到服务端，isdo返回true代表成功，其他失败
         * @param postUri 服务端controller路径
         * @param postKeys 提交到服务端的自定义参数，格式为json对象
         * @
         */
        postData.postAll(globalLogin.getUri("savedatas"), {}, function (isdo) {
            if (isdo) {
                datagrid.apply();
                alert("更新成功！");
            } else {
                alert("更新失败！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "configButton", "onClick", function (e, uiInfo, controlname) {

        //新建一个服务端数据保存代理，tag为服务端自定义标识
        var postData = new RemoteDataset("tag2");
        postData.add({
            id: 0,
            memo: 0,
            value: "测试值sample_a1"
        }); //新增一行，必须包括主键字段值
        postData.add({
            id: 1,
            memo: 0,
            value: "测试值sample_a2"
        }); //新增一行
        postData.add({
            id: 2,
            memo: 0,
            value: "测试值sample_a3"
        }); //新增一行，但不填写保存标志
        postData.postAll(globalLogin.getUri("saveconfig"), {}, function (isdo) {
            if (isdo) {
                alert("更新成功！");
            } else {
                alert("更新失败！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "editButton", "onClick", function (e, uiInfo, controlname) {
        var datagrid = new TomcatDataGrid("grid");
        if (datagrid.isEditing())
            datagrid.commitEdit();
        else
            datagrid.editRow();
    });

    ControlHelp.setEvent(uiInfo, "insertButton", "onClick", function (e, uiInfo, controlname) {
        var datagrid = new TomcatDataGrid("grid");
        datagrid.addRow();
    });

    ControlHelp.setEvent(uiInfo, "deleteButton", "onClick", function (e, uiInfo, controlname) {
        var datagrid = new TomcatDataGrid("grid");
        datagrid.removeRow();
    });

}

var info = getGlobalScriptInfo("login")
login_start(info.uiInfo, info.initdata);