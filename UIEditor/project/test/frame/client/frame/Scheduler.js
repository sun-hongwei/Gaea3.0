function Scheduler(task_id) {
    this.taskid = task_id;
    this.SUCCESS = 0;
    this.FAIL = -1;

    this.ajaxService = function (command, jsonData) {
        var result = undefined;
        var ajaxurl = "./Services/SchedulerService.php";
        var json = {};
        json.command = command;
        json.data = jsonData;
        Tools.ajaxSubmit({
            url: ajaxurl,
            async: false,
            data: json,
            success: function (serviceData) {
                result = mini.decode(serviceData);
                if (!result.ret) {
                    alert(result.data);
                }
            }
        });

        if (Tools.isNull(result)) {
            return undefined;
        }

        return result;
    }

    this.createTask = function (userid, uiid, taskid, flowname, taskmemo) {
        return this.ajaxService("createtask", {
            userid: userid,
            taskid: taskid,
            flowname: flowname,
            uiid: uiid,
            taskmemo: taskmemo
        });
    }

    this.trace = function (taskid, flowname) {
        return this.ajaxService("trace", {
            taskid: taskid,
            flowname: flowname,
        });
    }

    this.getHistoryTasks = function (userid, key, start, size, flowname) {
        return this.ajaxService("gethistorytasks", {
            userid: userid,
            flowname: flowname,
            key: key,
            start: start,
            size: size
        });
    }

    this.getTasks = function (userid, uiid, role, start, size, flowname) {
        return this.ajaxService("gettasks", {
            userid: userid,
            flowname: flowname,
            uiid: uiid,
            role: role,
            start: start,
            size: size
        });
    }

    this.getTask = function (userid, uiid, taskid, flowname, role) {
        return this.ajaxService("gettask", {
            userid: userid,
            taskid: taskid,
            flowname: flowname,
            role: role,
            uiid: uiid,
        });
    }

    this.action = function (userid, uiid, taskid, flowname, $decide, memo, role) {
        return this.ajaxService("action", {
            userid: userid,
            taskid: taskid,
            role: role,
            decide: $decide,
            flowname:flowname,
            uiid: uiid,
            memo: memo,
        });
    }

}