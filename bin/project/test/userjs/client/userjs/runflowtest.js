function runflowtest_start(uiInfo, initdata) {

    this.getFlowName = function () {
        return getFrameRunFlowName(uiInfo);
    }

    this.getRowBound = function () {
        var control = getFrameControlByName(uiInfo, "start");
        var start = getFrameControlValue(control);
        control = getFrameControlByName(uiInfo, "end");
        var end = getFrameControlValue(control);
        return {
            start: start,
            end: end
        };
    }

    var testObj = this;
    ControlHelp.setEvent(uiInfo, "login", "onClick", function () {
        var control = getFrameControlByName(uiInfo, "user");
        var userid = getFrameControlValue(control);
        control = getFrameControlByName(uiInfo, "pwd");
        var pwd = getFrameControlValue(control);
        GlobalUserLogin.login(userid, pwd, "", "", function (isok) {
            if (!isok)
                alert("登录失败！");
            else {
                GlobalSessionObject.checkButtonRoles(uiInfo, function (control, elementInfo) {
                    removeFrameControl(control);
                })
                
                alert("登录成功！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "logoff", "onClick", function () {
        GlobalSessionObject.clear();
        Tools.simpleTomcatSubmit("/ex/user/logoff", {}, function (data) {
            if (data.ret != 0)
                alert("未成功退出登录！");
            else {
                alert("已经成功退出登录！");
            }
        });
    });

    ControlHelp.setEvent(uiInfo, "getTasks", "onClick", function () {
        var scheduler = new Scheduler(Tools.guid());
        var userid = GlobalSessionObject.getUserId();
        var uiName = getFrameUIName(uiInfo);
        var role = GlobalSessionObject.getGroupRole();
        var rowbound = testObj.getRowBound();
        var info = scheduler.getTasks(userid, uiName, role, rowbound.start, rowbound.end, testObj.getFlowName());
        if (!info.ret){
            alert("获取任务列表失败！");
            return;
        }
        var dataGrid = getFrameControlByName(uiInfo, "tasks");
        setFrameControlValue(dataGrid, info.data);
    });

    ControlHelp.setEvent(uiInfo, "createtask", "onClick", function () {
        control = getFrameControlByName(uiInfo, "memo");
        var taskmemo = getFrameControlValue(control);

        var taskid = Tools.guid();
        var scheduler = new Scheduler(taskid);
        var userid = GlobalSessionObject.getUserId();
        var uiName = getFrameUIName(uiInfo);
        var info = scheduler.createTask(userid, uiName, taskid, testObj.getFlowName(), taskmemo);
        if (info.ret) {
            control = getFrameControlByName(uiInfo, "task");
            setFrameControlValue(control, info.data);
            alert("新建任务成功！")
        } else
            alert("任务建立失败：" + info.data);
    });

    ControlHelp.setEvent(uiInfo, "loadTask", "onClick", function () {
        var dg = new TomcatDataGrid("tasks", uiInfo);
        var row = dg.getRow();
        if (Tools.isNull(row)) {
            alert("请先选择一个任务！");
            return;
        }

        var taskid = row["taskid"];
        var scheduler = new Scheduler(taskid);
        var userid = GlobalSessionObject.getUserId();
        var uiName = getFrameUIName(uiInfo);
        var info = scheduler.getTask(userid, uiName, taskid, testObj.getFlowName(), 
            GlobalSessionObject.getGroupRole());
        if (info.ret) {
            control = getFrameControlByName(uiInfo, "task");
            setFrameControlValue(control, [info.data]);
        } else
            alert("装载任务失败：" + info.data);
    });

    ControlHelp.setEvent(uiInfo, "action", "onClick", function () {
        var dg = new TomcatDataGrid("tasks", uiInfo);
        var row = dg.getRow();
        if (Tools.isNull(row)) {
            alert("请先选择一个任务！");
            return;
        }

        var control = getFrameControlByName(uiInfo, "state");
        control = getFrameControlByName(uiInfo, "memo");
        var memo = getFrameControlValue(control);

        var scheduler = new Scheduler();
        var taskid = row["taskid"];
        var userid = GlobalSessionObject.getUserId();
        var uiName = getFrameUIName(uiInfo);

        var info = scheduler.action(userid, uiName, taskid, memo, testObj.getFlowName());
        if (info.ret) {
            control = getFrameControlByName(uiInfo, "task");
            setFrameControlValue(control, info.data);
        } else
            alert("装载任务失败：" + info.data);
    });

    ControlHelp.setEvent(uiInfo, "trace", "onClick", function () {
        var dg = new TomcatDataGrid("tasks", uiInfo);
        var row = dg.getRow();
        if (Tools.isNull(row)) {
            alert("请先选择一个任务！");
            return;
        }

        var scheduler = new Scheduler();
        var taskid = row["taskid"];

        var info = scheduler.trace(taskid, testObj.getFlowName());
        if (info.ret) {
            control = getFrameControlByName(uiInfo, "traceTree");
            setFrameControlValue(control, info.data);
        } else
            alert("装载任务失败：" + info.data);
    });

    ControlHelp.setEvent(uiInfo, "gethistory", "onClick", function () {
        var scheduler = new Scheduler();
        var userid = GlobalSessionObject.getUserId();
        var rowbound = testObj.getRowBound();
        var result = scheduler.getHistoryTasks(userid, "", rowbound.start, rowbound.end, testObj.getFlowName());
        if (!result.ret)
            return;

        var dataGrid = getFrameControlByName(uiInfo, "tasks");
        setFrameControlValue(dataGrid, result.data);
    });

    ControlHelp.setEvent(uiInfo, "refreshrole", "onClick", function () {
        GlobalUserLogin.refreshRoles();
    });
    
}

function runflowtest_getRunFlowTaskInfo(uiInfo, controlInfo, state) {
    var dg = new TomcatDataGrid("tasks", uiInfo);
    var row = dg.getRow();
    if (Tools.isNull(row)) {
        alert("请先选择一个任务！");
        return {};
    }

    return row;
}

