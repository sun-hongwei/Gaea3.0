<?php

require_once 'Common.php';
require_once 'LocalStorage.php';
require_once 'throws.php';

class Scheduler
{
    public static $SPLIT = ":";
    //起始状态值
    public static $START_KEY = "start";
    //状态节点的根
    public static $STATE_ROOT_KEY = "state";
    //处理节点的根
    public static $PROCESS_ROOT_KEY = "process";
    //action关联的ui与actionid的关联项的根
    public static $UI_ROOT_KEY = "ui";
    //action节点名称
    public static $ACTION_NODE_KEY = "action";
    //处理节点的adapter节点根
    public static $PROCESS_ADAPTER_ROOT_KEY = "adapter";

    //状态节点的操作类型键名
    public static $STATE_OPER_KEY = "oper";
    //状态节点的xor操作类型,表示仅可以有一条通路选择
    public static $STATE_OPER_XOR = "otXor";
    //节点的and操作类型,表示某一状态节点必须所有prev状态全部达到才可以进入此节点，
    //或者必须同时开始此action节点的所有next状态
    public static $STATE_OPER_AND = "otAnd";
    //状态节点关联的actionid的键名
    public static $STATE_ACTION_ROOT_KEY = "action";
    //状态节点关联的adapterid的键名
    public static $STATE_ADAPTER_ROOT_KEY = "adapter";
    //状态节点的下一个状态名称的键名
    public static $STATE_NEXT_KEY = "next";
    //状态节点的上一个状态名称的键名
    public static $STATE_PREV_KEY = "prev";

    //动作节点下关联的执行情况信息
    public static $ACTION_EXECUTE_KEY = "execute";
    //动作节点下关联的workflow信息
    public static $ACTION_WORKFLOW_KEY = "workflow";
    //动作节点下关联的初始信息
    public static $ACTION_INITDATA_KEY = "initdata";
    //动作节点下关联的权限信息
    public static $ACTION_ROLE_KEY = "role";

    public static $USER_END_FLOW_TIME_KEY = "end";
    public static $USER_STATE_KEY = "state";
    public static $USER_TASK_KEY = "taskid";
    public static $USER_TIME_KEY = "time";

    public static $TASK_USER_KEY = "userid";
    public static $ACTION_ID_KEY = "id";
    public static $ACTION_STATE_KEY = "state";
    public static $ACTION_NEXT_KEY = "next";
    public static $ACTION_COUNTER_KEY = "counter";
    public static $ACTION_TIME_KEY = "time";
    public static $ACTION_USER_KEY = "userid";


    //adapter或action节点下的下一节点
    public static $PROCESS_NEXT_KEY = "next";

    public static $GlobalFlowData = array();

    private $flowname;
    private $taskid;
    private $userid;
    private $actionid;
    private $data;

    private $localStorage;

    public function getRunFlowFileName()
    {
        return "../run/" . $this->flowname . ".whr";
    }

    /**
     * 工作流数据的根节点
     */
    protected function getRoot()
    {
        return "php" . self::$SPLIT . "runflow" . self::$SPLIT . $this->flowname;
    }

    /**
     * 正在进行的工作流、状态、动作等的节点根
     */
    protected function getProcessingTaskRoot()
    {
        return $this->getRoot() . self::$SPLIT . "ing";
    }

    /**
     * 已经完成的工作流节点根
     */
    protected function getCompleteRoot()
    {
        return $this->getRoot() . self::$SPLIT . "end";
    }

    /**
     * 存放一个任务中一个动作的一个任务的数据
     * 数据的格式为：
     *{
     *  actionid:"执行的动作id",
     *  state:"状态id",
     *  taskid:"任务id",
     *  userid:"执行动作的用户id",
     *  memo:"动作执行说明",
     *  time:"执行动作的时间",
     *}
     * @param {string} actionid 动作id
     */
    protected function getTaskProcessingActionRoot($actionid)
    {
        return $this->getTaskProcessingActionKey($actionid) . self::$SPLIT . $this->taskid;
    }

    /**
     * 存放一个流程中已经完成的动作数据,用来追溯
     * 数据格式：参见getTaskProcessingActionRoot
     * @param {string} actionid 动作id
     */
    protected function getTaskTraceRoot()
    {
        return $this->getTaskTraceKey();
    }

    /**
     * 存放所有完结任务信息
     * 数据格式：参见getTaskProcessingActionRoot
     */
    protected function getTaskCompleteRoot()
    {
        return $this->getTaskCompleteKey($this->userid);
    }

    /**
     * 存放任务建立信息
     * 数据格式：参见getTaskProcessingActionRoot
     */
    protected function getTaskRoot()
    {
        return $this->getTaskKey($this->taskid);
    }

    /**
     * 设置会签信息存储根节点，此节点未map节点，每个key为会签的状态，值为bool，false为未通过，true通过
     */
    protected function getCountersignRoot($state)
    {
        return $this->getProcessingTaskRoot()  . self::$SPLIT . $this->taskid . self::$SPLIT . $state;
    }

    //key------------------------------------------------------------------------------------

    /**
     * 获取所有完结任务信息key
     * 数据格式：参见getTaskProcessingActionRoot
     */
    protected function getTaskCompleteKey($userid)
    {
        return $this->getCompleteRoot() . self::$SPLIT . "tasks" . self::$SPLIT . $userid;
    }

    /**
     * 获取任务的追踪Key
     */
    protected function getTaskTraceKey()
    {
        return $this->getRoot() . self::$SPLIT . "traces" . $this->taskid;
    }

    /**
     * 正在进行的任务的动作键名称
     * @param {string} actionid 动作id
     */
    protected function getTaskProcessingActionKey()
    {
        return $this->getProcessingTaskRoot() . self::$SPLIT . $this->cur_state;
    }

    /**
     * 获取任务的创建信息key
     */
    protected function getTaskKey($taskid)
    {
        return $this->getRoot() . self::$SPLIT . $taskid;
    }

    public function load()
    {
        $filename = $this->getRunFlowFileName();
        if (!empty(self::$GlobalFlowData[$filename])) {
            $this->data = self::$GlobalFlowData[$filename];
            return;
        }
        $this->data = null;
        if (file_exists($filename)) {
            $str = file_get_contents($filename);
            $this->data = json_decode($str, true);
            $this->data[self::$UI_ROOT_KEY] = array();
            foreach ($this->data[self::$PROCESS_ROOT_KEY] as $actionid => &$action) {
                $values = array();
                foreach ($action[self::$STATE_NEXT_KEY] as $state) {
                    $values[$state] = $state;
                }
                $action[self::$STATE_NEXT_KEY] = $values;

                $roles = array();
                foreach ($action[self::$ACTION_ROLE_KEY] as $role) {
                    $roles[$role["id"]] = $role["name"];
                }
                $action[self::$ACTION_ROLE_KEY] = $roles;

                //对于and情况，此map会存在多个action，因此不能唯一确定action信息
                $uiid = $action["workflow"]["value"];
                if (!empty($this->data[self::$UI_ROOT_KEY][$uiid])) {
                    $data = $this->data[self::$UI_ROOT_KEY][$uiid];
                    $actions = array();
                    if (empty($data[0]))
                        $actions[] = $data;
                    else {
                        $actions = $data;
                    }
                    $actions[] = $action;
                    $this->data[self::$UI_ROOT_KEY][$uiid] = $actions;
                } else
                    $this->data[self::$UI_ROOT_KEY][$uiid] = $action;
            }
            self::$GlobalFlowData[$filename] = $this->data;
        }
        return $this;
    }

    protected static function now()
    {
        return date_format(date_create(), 'Y-m-d H:i:s:u');
    }

    /**
     * 开始一个任务，如果任务不存在则创建新任务
     * @param {string} taskmemo 任务说明
     */
    public function newTask($taskmemo)
    {
        $this->cur_state = $this->data[Scheduler::$START_KEY];

        $db = $this->localStorage;

        //设置任务状态
        $stateInfo = $this->setProcessingInfo($this->actionid, $taskmemo);

        //保存任务建立信息
        $db->setRoot($this->getTaskRoot());
        $db->sets($stateInfo);

        $this->localStorage->save();
        return $stateInfo;
    }

    protected function createProcessingInfo($actionid, $memo, $docode = 0, $state = null)
    {
        $stateInfo = array(
            "actionid" => $actionid,
            "state" => (empty($state) ? $this->cur_state : $state),
            "taskid" => $this->taskid,
            "execute" => $docode,
            "userid" => $this->userid,
            "memo" => empty($memo) ? "" : $memo,
            "time" => Scheduler::now(),
        );

        return $stateInfo;
    }

    protected function addTraceInfo($stateInfo)
    {
        /**
         * 保存当前的状态执行信息到追踪列表
         */
        $db = $this->localStorage;
        $db->setRoot($this->getTaskTraceRoot());
        $db->addToList($stateInfo);
    }

    //设置任务的运行信息
    protected function setProcessingInfo($actionid, $memo)
    {
        $db = $this->localStorage;

        //设置任务运行状态
        $stateInfo = $this->createProcessingInfo($actionid, $memo);

        $db->setRoot($this->getTaskProcessingActionRoot($actionid));
        $db->sets($stateInfo);

        $this->addTraceInfo($stateInfo);

        return $stateInfo;
    }

    //获取任务的运行信息
    protected function getProcessingInfo($actionid)
    {
        $db = $this->localStorage;
        $root = $this->getTaskProcessingActionRoot($actionid);
        $db->setRoot($root);
        return $db->all();
    }

    protected function deleteProcessingInfos()
    {
        $db = $this->localStorage;
        $oldstate = $this->cur_state;
        $deleteKeys = array();
        foreach ($this->data[self::$STATE_ROOT_KEY] as $state => $stateinfo) {
            if (empty($stateinfo[self::$STATE_ACTION_ROOT_KEY]))
                continue;

            $this->cur_state = $state;
            foreach ($stateinfo[self::$STATE_ACTION_ROOT_KEY] as $actionid) {
                $deleteKeys[] = $db->getKey($this->getTaskProcessingActionRoot($actionid));
            }
        }
        $this->cur_state = $oldstate;
        $db->deletes($deleteKeys);
    }

    /**
     * 添加trace信息，如果用户指定指定状态与真实状态不同写入realState，如果writeNewState为true还要写入newState
     * @param $actionid 本次的动作id
     * @param $memo 本次执行说明
     * @param $newState 本次跳转的真实状态
     * @param $jumpType 动作流转判定的返回码
     * @param $realState 本次动作执行,用户指定的分支状态
     * @param $writeNewState 是否需要写入$newState
     */
    protected function addSubmitTraceInfo($actionid, $memo, $newState, $jumpType, $realState, $writeNewState)
    {
        if ($realState != $newState) {
            $stateInfo = $this->createProcessingInfo($actionid, $memo, $jumpType, $realState);
            $this->addTraceInfo($stateInfo);
        }

        if ($writeNewState){
            $stateInfo = $this->createProcessingInfo($actionid, $memo, $jumpType, $newState);
            $this->addTraceInfo($stateInfo);
        }
    }

    /**
     * 设置提交信息，当用户提交状态时调用，以完成状态的切换
     * @param {string} actionid 用户本次执行提交的action的id
     * @param {string} memo 用户本次提交的说明
     * @param {string} newState 用户提交时指定将要流转的新状态码
     * @param {int} jumpType newState指定的状态的跳转形式， 参见internalCheckState的状态代码返回说明
     * @param {string} realState 用户输入的真实状态 
     */
    protected function setSubmitStates($actionid, $memo, $newState, $jumpType, $realState)
    {
        $taskInfo = null;

        switch ($jumpType) {
            case 3: //最后节点并且为会签节点
            case 2: //任务执行完毕
                //将当前任务加入用户已经完结任务列表
                $taskInfo = $this->getTask();
                $taskInfo[self::$USER_END_FLOW_TIME_KEY] = self::now();
                break;
        }

        $this->localStorage->calls(
            array(
                $this->getTaskProcessingActionRoot($actionid),
                $this->getTaskTraceRoot()
            ),
            function ($db, $scheduler, $jumpType, $memo, $newState, $actionid, $taskInfo, $realState) {
                if (empty($newState)) {
                    return;
                }

                if (empty($scheduler->cur_state) || $scheduler->cur_state != $newState) {

                    /**
                     * 删除当前的执行信息
                     */
                    switch ($jumpType) {
                        case 2: //终止节点，删除所有进行节点信息
                        case 4: //and未完成，跳转到其他非and节点，此时需要删除所有已经发布但未完成的任务
                            $scheduler->deleteProcessingInfos();
                            break;
                        default:
                            $db->setRoot($scheduler->getTaskProcessingActionRoot($actionid));
                            $db->delete();
                            break;
                    }

                    /**
                     * 更新当前状态到新状态，并设置新的执行信息
                     */
                    switch ($jumpType) {
                        case 1:
                        case 5:
                            break;
                        default:
                            //更新当前状态
                            $scheduler->cur_state = $newState;
                            $stateInfo = $scheduler->data[self::$STATE_ROOT_KEY][$scheduler->cur_state];
                            switch ($jumpType) {
                                case 3: //任务执行完毕，并且为会签节点
                                case 2: //任务执行完毕
                                    //将当前任务加入用户已经完结任务列表
                                    $db->setRoot($scheduler->getTaskCompleteKey($taskInfo[self::$TASK_USER_KEY]));
                                    $db->addToList($taskInfo);
                                    $scheduler->addSubmitTraceInfo($actionid, $memo, $newState, $jumpType, $realState, true);
                                    break;
                                default:
                                    if (empty($stateInfo[self::$STATE_OPER_KEY])) {
                                        $scheduler->addSubmitTraceInfo($actionid, $memo, $newState, $jumpType, $realState, false);
                                        $scheduler->setProcessingInfo($actionid, $memo, $jumpType);
                                    } else {
                                        if ($stateInfo[self::$STATE_OPER_KEY] == self::$STATE_OPER_AND){
                                            $scheduler->addSubmitTraceInfo($actionid, $memo, $newState, $jumpType, $realState, false);
                                            foreach ($stateInfo[self::$STATE_ACTION_ROOT_KEY] as $action_id) {
                                                $scheduler->setProcessingInfo($action_id, $memo, $jumpType);
                                            }
                                        }
                                    }
                                    break;
                            }
                            break;
                    }
                }
            },
            $this->localStorage,
            $this,
            $jumpType,
            $memo,
            $newState,
            $actionid,
            $taskInfo,
            $realState
        );

        $this->localStorage->save();
    }

    protected function checkState($decide)
    {
        return $this->internalCheckState(null, $decide);
    }

    protected function getCountersignMap($state)
    {
        $db = $this->localStorage;

        $info = $this->data[self::$STATE_ROOT_KEY][$state];
        if (empty($info[self::$STATE_PREV_KEY]))
            throwException("此节点不是会签节点！");

        $root = $this->getCountersignRoot($state);
        $db->setRoot($root);
        return $db->all();
    }

    /**
     * 检查是否未会签状态，并且会签状态是否完成
     * @param state 要检查的会签进入状态
     * @return 
     * 0是会签状态,并且全部完成 
     * 1是会签状态，但跳转状态不是会签状态之一 
     * -1是会签状态,但有其他状态未完成 
     * -2不是会签节点
     */
    protected function checkCountersignState($decideState, $counterState)
    {
        $stateMap = $this->createCountersignMap($counterState, $decideState);

        $hasState = false;
        $completed = true;
        $info = $this->data[self::$STATE_ROOT_KEY][$counterState];
        $prevStates = $info[self::$STATE_PREV_KEY];
        foreach ($prevStates as $state) {
            $hasState = $hasState ? true : $decideState == $state;
            if ($completed)
                $completed = !empty($stateMap[$state]) && $stateMap[$state];
        }

        if ($hasState) {
            //输入状态是其中的一个状态
            return $completed ? 0 : -1;
        } else {
            //输入状态并不是其中的一个状态
            return 1;
        }
    }

    protected function createCountersignMap($state, $decideState)
    {
        $db = $this->localStorage;

        $info = $this->data[self::$STATE_ROOT_KEY][$state];
        if (empty($info[self::$STATE_PREV_KEY]))
            throwException("此节点不是会签节点！");

        $root = $this->getCountersignRoot($state);
        $db->setRoot($root);
        if (!$db->exists($decideState)) {
            $taskInfo = $this->getCurTask($this->actionid);
            $taskInfo[self::$ACTION_STATE_KEY] = $decideState;
            $taskInfo[self::$ACTION_TIME_KEY] = self::now();
            $taskInfo[self::$ACTION_USER_KEY] = $this->userid;
            $db->setRoot($root);
            $db->set($decideState, $taskInfo, true);
        }

        return $db->all();
    }

    protected function isCountersignState($state)
    {
        $info = $this->data[self::$STATE_ROOT_KEY][$state];
        return !empty($info[self::$STATE_PREV_KEY]);
    }
    /**
     * 检查指定状态完结是否可以跳转新状态
     * @param {string} $preState 要检测的原始状态，第一次输入时候为null时使用当前状态
     * @param {string} $state 要检查的状态，即客户端传送的decide
     * @return {json{ret:0, state:"状态码"}}
     * 
     * ret含义如下：
     * 0当前节点可跳转，state为目标状态码
     * 2当前节点已经为最后一个可执行状态节点，state为终止状态码
     * 5当前节点不可以跳转，输入的state不是当前任务的合法下级节点，
     * 7当前节点未会签节点，需要进行会签节点检查才可以确定真实状态，
     * 
     * state：目标状态码，一般为输入state，但如果有多对一情况，则为下一级action的前置状态码
     */
    protected function internalCheckState($preState, $state)
    {
        if (empty($preState)) {
            $preState = $this->cur_state;
        }

        //获取状态信息
        $info = $this->data[self::$STATE_ROOT_KEY][$preState];
        //获取下级状态
        $nextState = empty($info[self::$STATE_NEXT_KEY]) ? null : $info[self::$STATE_NEXT_KEY];
        if (!empty($nextState)) {
            //如果下级状态不为空则用下级状态递归调用，这一般是下级状态需要多个状态全部完成的情况，即and分支
            return $this->internalCheckState($nextState, $state);
        } else if (!empty($info[self::$STATE_OPER_KEY])) {
            //判断是否为会签任务
            if (empty($info[self::$STATE_PREV_KEY])) {
                foreach ($info[self::$STATE_ACTION_ROOT_KEY] as $actionid) {
                    $action = $this->getActionInfo($actionid);
                    foreach ($action[self::$ACTION_NEXT_KEY] as $actionNextState) {
                        if ($actionNextState == $state) {
                            return array("ret" => 7, "state" => $state);
                        }
                    }
                }

                throwException("无效的目标状态");
            }
        } else if (!empty($info[self::$ACTION_NODE_KEY])) {
            //没有下级节点、操作符，但有action，说明是简单状态节点，直接返回
            $actionid = $info[self::$ACTION_NODE_KEY];
            $action = $this->getActionInfo($actionid[0]);
            $nextstates = $action[self::$PROCESS_NEXT_KEY];
            foreach ($nextstates as $nextState) {
                if ($nextState == $state) {
                    return array("ret" => 0, "state" => $state);
                }
            }
            return array("ret" => 5, "state" => $state);
        } else {
            //没有下级节点、操作符、action，说明是终止状态，直接返回
            return array("ret" => 2, "state" => $state);
        }
    }

    /**
     * 获取真实的跳转节点
     * @param {string} state 要检查的状态
     * @return state节点的真实跳转节点
     */
    protected function getJumpState($state)
    {
        //获取状态信息
        $info = $this->data[self::$STATE_ROOT_KEY][$state];

        //获取下级状态
        $nextState = empty($info[self::$STATE_NEXT_KEY]) ? null : $info[self::$STATE_NEXT_KEY];
        if (!empty($nextState)) {
            //如果下级状态不为空则用下级状态递归调用
            return $this->getJumpState($nextState);
        } else {
            //没有下级节点直接返回
            return $state;
        }
    }

    /**
     * 获取真实的会签跳转节点
     * @param {string} state 要检查的状态
     * @return state节点的真实跳转节点
     */
    protected function getCountersignState($state)
    {
        //获取状态信息
        $info = $this->data[self::$STATE_ROOT_KEY][$state];

        //获取下级状态
        $nextState = empty($info[self::$STATE_PREV_KEY]) ? null : $info[self::$STATE_PREV_KEY];
        if (!empty($nextState)) {
            return $nextState;
        } else {
            return null;
        }
    }

    /**
     * 检查输入状态是否为终止状态
     * @param {string} state 要检查的状态
     * @return {bool} true终止状态，其他不是
     */
    protected function isEndState($state)
    {
        //获取状态信息
        $info = $this->data[self::$STATE_ROOT_KEY][$state];

        return empty($info);
    }

    /**
     * 获取指定taskid的任务创建信息
     * @param {string} taskid 要获取的任务id，如果为null，则使用当前任务id
     * @return {json} 参见getTaskProcessingActionRoot的说明
     */
    public function getTask($taskid = null)
    {
        if (empty($taskid))
            $taskid = $this->taskid;
        $db = $this->localStorage;
        $db->setRoot($this->getTaskKey($taskid));
        return $db->all();
    }

    /**
     * 执行当前状态的动作以便跳转到下一个状态
     * @param {string} uiid 本次执行的modelnode的id，不能为null
     * @param {string} decide 本次执行的动作要跳转的目的状态码，不能为null
     * @param {string} memo 本次动作执行的说明
     * @return {json{ret:0, state:"状态码"}}
     * 
     * ret含义如下：
     * 0当前节点可跳转，state为目标状态码
     * 1当前节点不可跳转，等待其他节点状态完成，state为目标状态码
     * 2当前节点已经为最后一个可执行状态节点，state为终止状态码
     * 3当前节点已经为最后一个可执行状态节点，且通过会签的非会签节点跳转到此节点，state为终止状态码
     * 4当前节点可以跳转，并且当前为and节点[会签]，但输入的状态并不是and状态集合中的一个，说明客户做了and外的跳转，
     * 5当前节点不可以跳转，输入的state不是当前任务的合法下级节点，
     * 6当前节点可以跳转，多个and节点全部完成，
     * 
     * 一般是会签情况下，某一个参与者选择了驳回，state为目标状态码
     * state：目标状态码，一般为输入state，但如果有多对一情况，则为下一级action的前置状态码
     */
    public function action($decide, $memo = null)
    {
        $info = $this->checkState($decide);
        if ($info["ret"] == 5) {
            return 5;
        }

        $isAnd = $info["ret"] == 7;
        $needSave = true;
        $countersignState = null;
        if ($isAnd) {
            $countersignState = $this->getCountersignState($info["state"]);
            $isAnd = !empty($countersignState);
        }

        if ($isAnd) {
            $result = $this->checkCountersignState($info["state"], $countersignState);
            switch ($result) {
                case 0: //是会签节点，全部完成，可以跳转
                    $memo = "会签自动跳转";
                    $info["ret"] = 6;
                    $this->addTraceInfo($this->createProcessingInfo($this->actionid, $memo, $info["ret"], $info["state"]));
                    break;
                case -1: //是会签节点，但未全部完成，不可以跳转
                    $info["ret"] = 1;
                    $needSave = false;
                    //存储用户的跳转状态
                    $this->addTraceInfo($this->createProcessingInfo($this->actionid, $memo, $info["ret"], $info["state"]));
                    break;
                case 1: //不是会签节点
                    $info["ret"] = 4;
                    break;
            }
        }


        if ($needSave) {
            $state = $this->getJumpState($info["state"]);
            if ($this->isEndState($state)) {
                $info["ret"] = ($isAnd && $info["ret"] == 6) ? 3 : 2;
            }

            $this->setSubmitStates($this->actionid, $memo, $state, $info["ret"], $info["state"]);
            $info["inputstate"] = $info["state"];
            $info["jumpedstate"] = $state;
        }
        return $info;
    }

    /**
     * 获取uiid对应的action信息
     * @param {string} uiid Gaea中的模块节点name
     * @return 如果此uiid对应的action为多个，返回action信息的array，否则返回action信息
     */
    public function getActionInfoForUIID($uiid)
    {
        if (empty($this->data[self::$UI_ROOT_KEY][$uiid])) {
            throwException("not found uiid[" . $uiid . "]");
        }

        return $this->data[self::$UI_ROOT_KEY][$uiid];
    }

    /**
     * 获取action信息
     * @param {string} 要获取的actionid，不可以为null
     * @return {Action} 返回action的信息
     */
    public function getActionInfo($actionid)
    {
        $info = $this->data[self::$PROCESS_ROOT_KEY][$actionid];
        if (empty($info)) {
            throwException("cur state not found!");
        }

        return $info;
    }

    /**
     * 获取指定actionid的权限信息
     * @param {string} 要获取权限的动作id
     * @return 权限信息列表，格式：{"权限id":"权限名称"}
     */
    public function roles($actionid)
    {
        $action = $this->getActionInfo($actionid);
        if (empty($action)) {
            return array();
        }

        return $action[self::$ACTION_ROLE_KEY];
    }

    protected function findInfo($baseKey, $key = null, $start = null, $size = null)
    {
        if (empty($key)) {
            $key = "*";
        }

        $db = $this->localStorage;
        $keys = $db->findKeys($baseKey . (empty($key) ? "" : $key));
        $result = array();
        foreach ($keys as $key) {
            $db->setRealRoot($key);
            $info = $db->all($start, $size);
            if (!empty($info)) {
                $result[] = $info;
            }
        }
        return $result;
    }

    /**
     * 获取已经流转到指定动作的任务列表，如果roles指定的用户权限和动作的role权限不符,则返回array()
     * @param {jsonobject} roles 用户的权限信息，格式{"组id":"1"}
     * @param {string} actionid 获取任务列表的动作id
     * @param {int} start 查询的起始索引，从0开始，默认为0
     * @param {int} size 返回的最大结果数量，默认为20
     * @return {array} 返回所有符合要求的任务信息
     * 任务数据格式参见getTaskProcessingActionRoot
     */
    public function getActionTasks($roles, $actionid, $start = null, $size = null)
    {
        if (empty($actionid)) {
            $actionid = $this->actionid;
        }
        $deny = true;
        foreach ($this->roles($actionid) as $role) {
            if (!empty($roles[$role])) {
                $deny = false;
                break;
            }
        }

        if ($deny) {
            return array();
        }

        return $this->findInfo($this->getTaskProcessingActionKey($actionid), null, $start, $size);
    }

    /**
     * 或者当前用户的当前任务在指定的动作处的状态信息
     * @param actionid 要查询的动作id
     * @return 如果有用户已经完成这个动作，返回array()，否则返回状态信息
     */
    public function getCurTask($actionid)
    {
        if (empty($actionid)) {
            $actionid = $this->actionid;
        }

        $db = $this->localStorage;
        $db->setRoot($this->getTaskProcessingActionRoot($actionid));
        return $db->all();
    }

    /**
     * 返回当前用户的历史任务数据
     */
    public function getHistoryTasks($key = null, $start = null, $size = null)
    {
        if (empty($key)) {
            $key = "*";
        }

        $result = $this->findInfo($this->getTaskCompleteRoot(), $key, $start, $size);
        if (count($result) == 0)
            return array();
        else
            return $result[0];
    }

    /**
     * 查询当前任务的执行痕迹
     */
    public function traceTask()
    {
        $info = $this->findInfo($this->getTaskTraceKey(), null);
        if (count($info) == 0) {
            return array();
        } else {
            return $info[0];
        }
    }

    public function __construct($userid, $taskid, $flowname = null, $uiid = null, $roles = null)
    {
        $this->userid = $userid;
        $this->flowname = $flowname;
        $this->taskid = $taskid;
        $this->localStorage = new LocalStorage();
        if (!empty($flowname)) {
            $this->load();
            if (!empty($uiid)) {
                $action = $this->getActionInfoForUIID($uiid);
                if (!empty($action[0])) {
                    foreach ($action as $a) {
                        foreach ($this->roles($a[self::$ACTION_ID_KEY]) as $role) {
                            if (!empty($roles[$role])) {
                                $this->actionid = $a["id"];
                                $this->cur_state = $a["state"];
                                return;
                            }
                        }
                    }
                } else {
                    $this->actionid = $action["id"];
                    $this->cur_state = $action["state"];
                }
            }
        }
    }
}
