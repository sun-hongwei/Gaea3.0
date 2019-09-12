<?php
require_once 'Common.php';
require_once 'Scheduler.php';
require_once 'throws.php';

error_reporting(0);

$require = $_POST["data"];
$command = $_POST["command"];

function echoError($msg)
{
    $result = array("ret" => false, "data" => $msg);
    echo json_encode($result);
    die();
}

if ($command != null) {
    try {
        switch ($command) {
            case "trace":
                $taskid = $require["taskid"];
                $flowname = $require["flowname"];
                if (empty($taskid)) {
                    echoError("任务id不能为空！");
                }
                if (empty($flowname)) {
                    echoError("工作流名称不能为空！");
                }

                $scheduler = new Scheduler(null, $taskid, $flowname);
                $data = $scheduler->traceTask();
                $result = array("ret" => true, "data" => $data);

                echo json_encode($result);
                break;
            case "gethistorytasks":
                $userid = $require["userid"];
                $flowname = $require["flowname"];
                $key = $require["key"];
                $start = $require["start"];
                $size = $require["size"];

                if (empty($userid)) {
                    echoError("用户id不能为空！");
                }
                if (empty($flowname)) {
                    echoError("流程名称不能为空！");
                }
                $scheduler = new Scheduler($userid, null, $flowname);
                $data = $scheduler->getHistoryTasks($key, $start, $size);
                $result = array("ret" => true, "data" => $data);
                echo json_encode($result);
                break;
            case "gettasks":
                $role = $require["role"];
                $userid = $require["userid"];
                $flowname = $require["flowname"];
                $uiid = $require["uiid"];
                $start = $require["start"];
                $size = $require["size"];

                if (empty($role)) {
                    echoError("用户角色不能为空！");
                }
                if (empty($flowname)) {
                    echoError("流程名称不能为空！");
                }
                if (empty($userid)) {
                    echoError("用户id不能为空！");
                }
                if (empty($uiid)) {
                    echoError("执行动作id不能为空！");
                }
                $scheduler = new Scheduler($userid, null, $flowname, $uiid, $role);
                $data = $scheduler->getActionTasks($role, null, $start, $size);
                $result = array("ret" => true, "data" => $data);
                echo json_encode($result);
                break;
            case "createtask":
                $userid = $require["userid"];
                $taskid = $require["taskid"];
                $flowname = $require["flowname"];
                $taskmemo = $require["taskmemo"];
                $uiid = $require["uiid"];

                if (empty($taskid)) {
                    echoError("任务id不能为空！");
                }
                if (empty($flowname)) {
                    echoError("流程名称不能为空！");
                }
                if (empty($userid)) {
                    echoError("用户id不能为空！");
                }
                if (empty($uiid)) {
                    echoError("执行动作id不能为空！");
                }

                $scheduler = new Scheduler($userid, $taskid, $flowname, $uiid);
                $stateInfo = $scheduler->newTask($taskmemo);
                if (empty($stateInfo)) {
                    $result = array("ret" => false, "data" => "create task failed！");
                } else {
                    $result = array("ret" => true, "data" => $stateInfo);
                }

                echo json_encode($result);
                break;
            case "gettask":
                $role = $require["role"];
                $userid = $require["userid"];
                $taskid = $require["taskid"];
                $flowname = $require["flowname"];
                $uiid = $require["uiid"];

                if (empty($taskid)) {
                    echoError("任务id不能为空！");
                }
                if (empty($flowname)) {
                    echoError("流程名称不能为空！");
                }
                if (empty($userid)) {
                    echoError("用户id不能为空！");
                }
                if (empty($uiid)) {
                    echoError("执行动作id不能为空！");
                }

                $scheduler = new Scheduler($userid, $taskid, $flowname, $uiid, $role);
                $data = $scheduler->getCurTask(null);
                $result = array("ret" => true, "data" => $data);
                echo json_encode($result);
                break;
            case "action":
                $role = $require["role"];
                $userid = $require["userid"];
                $taskid = $require["taskid"];
                $decide = $require["decide"];
                $uiid = $require["uiid"];
                $flowname = $require["flowname"];
                $memo = $require["memo"];

                if (empty($taskid)) {
                    echoError("任务id不能为空！");
                }
                if (empty($flowname)) {
                    echoError("流程名称不能为空！");
                }
                if (empty($userid)) {
                    echoError("用户id不能为空！");
                }
                if (empty($uiid)) {
                    echoError("执行动作id不能为空！");
                }

                $scheduler = new Scheduler($userid, $taskid, $flowname, $uiid, $role);
                $data = $scheduler->action($decide, $memo);
                $result = array("ret" => true, "data" => $data);
                echo json_encode($result);
                break;
        }
    } catch (Exception $e) {
        $pen = fopen('PHP_errlog.log', 'a');
        fwrite($pen, 'SchedulerService ==> [' . date("Y-m-d h:i:s") . '] ==> ' . $e);
        fclose($pen);
        echo '{"ret":false,"data":"' . $e->getMessage() . '"}';
    }
}
