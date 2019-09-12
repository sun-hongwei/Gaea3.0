<?php

require_once 'StringEncode.php';

abstract class DB
{
    public static $VERSION = 12;

    public static $ROW_STATE = "_state";
    public static $ROW_STATE_NONE = "none";
    public static $ROW_STATE_ADD = "added";
    public static $ROW_STATE_REMOVE = "removed";
    public static $ROW_STATE_EDIT = "modified";

    public static $OK = 0;
    public static $NotFoundPrimKey = -1;
    public static $JSONEmpty = -2;
    public static $NotFoundWhere = -3;
    public static $NotOpenDataset = -4;
    public static $NotFoundKey = -5;
    public static $LocateNotFound = -6;
    public static $UnKnownException = -7;

    protected $connection = NULL;

    protected $dataset = NULL;
    protected $columns = array();
    protected $rows = array();
    protected $indexs = array();

    protected $primkeys = array();

    protected $sqlBuilder = NULL;

    protected $curRow = NULL;

    abstract protected function convertString($col, $data);
    abstract protected function createSQLBuilder();
    abstract protected function initConnection($serverName, $connectionInfo);
    abstract protected function getProcCommand($name, $params);

    abstract public function valueOf($col);
    abstract public function post();
    abstract public function &getPrimkeys();
    abstract public function getDataType($index);
    abstract public function getFieldCount();
    abstract public function getDataSize($index);
    abstract public function getFieldName($index);
    abstract public function getRecordCount();
    abstract protected function next();
    abstract public function toValue($column, $row);

    abstract public function closeDatasetResource();
    abstract public function closeConnection();
    abstract public function query($sql, $params = array());
    abstract public function exec($sql, $params = array());
    abstract public function toBinary($dataset, $index);
    abstract public function beginTran();
    abstract public function commitTran();
    abstract public function rollbackTran();
    abstract public function nextResult();

    public function closeDataset()
    {
        $this->clear();
        $this->closeDatasetResource();
    }

    public static function Column_TypeToString($type)
    {
        switch ($type) {
            case SQL_BIGINT:
                return "ctBigInteger";
            case SQL_DATE:
            case SQL_TYPE_DATE:
                return "ctDate";
            case SQL_TIME:
            case SQL_TYPE_TIME:
                return "ctDateTime";
            case SQL_BIT:
                return "ctBool";
            case SQL_SMALLINT:
                return "ctShort";
            case SQL_TINYINT:
            case SQL_INTEGER:
                return "ctInteger";
            case SQL_DECIMAL:
            case SQL_NUMERIC:
            case SQL_REAL:
            case SQL_FLOAT:
            case SQL_DOUBLE:
                return "ctFloat";
            // case ctImage:
            //     return "ctImage";
            case SQL_BINARY:
            case SQL_VARBINARY:
            case SQL_LONGVARBINARY:
                return "ctBinary";
            case SQL_LONGVARCHAR:
                return "ctMemo";
            default:
                return "ctString";
        }
    }

    abstract public function &getFieldNames();

    public function hasField($fieldname)
    {
        $fields = &$this->getFieldNames();
        return isset($fields[strtoupper($fieldname)]);
    }

    public function isPrimKey($fieldname)
    {
        $fieldname = strtoupper($fieldname);
        $this->getPrimkeys();
        return isset($this->primkeys[$fieldname]);
    }

    abstract public function getPageQueryTotalCount();

    protected function getSimplejsonstring($value)
    {
        return "\"" . $value . "\"";
    }

    protected function getjsonstring($key, $value)
    {
        return $this->getSimplejsonstring($key) . ":" . $this->getSimplejsonstring($value);
    }

    public function toJSON($withHeader)
    {
        $json = "";
        $jsonstring = "{";

        $tablename = $this->sqlBuilder->gettablename();
        $where = $this->sqlBuilder->getwhere();
        $group = $this->sqlBuilder->getgroup();
        if ($withHeader) {
            $jsonstring .= $this->getjsonstring("TABLENAME", $tablename) . "," . $this->getjsonstring("FILTER", $where);
            $jsonstring .= "," . $this->getSimplejsonstring("columns") . ":[";
            $$bstart = true;
            for ($i = 0; $i < $this->getFieldCount(); $i++) {
                if ($bstart) {
                    $bstart = false;
                    $jsonstring .= "{";
                } else {
                    $jsonstring .= ",{";
                }

                $jsonstring .= $this->getjsonstring("name", $this->GetFieldName($i));
                $jsonstring .= "," . $this->getjsonstring("header", $this->getFieldName($i));
                $jsonstring .= "," . $this->getjsonstring("datatype", $this->Column_TypeToString($this->getDataType($i)));
                $jsonstring .= "," . $this->getjsonstring("isprimary", ($this->isPrimKey($i) ? "true" : "false"));
                $jsonstring .= "," . $this->getjsonstring("iscode", "false");
                $jsonstring .= "," . $this->getjsonstring("width", $this->getDataSize($i));
                $jsonstring .= "}";

            }
            $jsonstring .= "]";
        }
        if ($jsonstring == "{") {
            $jsonstring .= $this->getSimplejsonstring("total") . ":" . $this->getSimplejsonstring($this->getPageQueryTotalCount());
        } else {
            $jsonstring .= "," . $this->getSimplejsonstring("total") . ":" . $this->getSimplejsonstring($this->getPageQueryTotalCount());
        }

        $jsonstring .= "," . $this->getSimplejsonstring("data") . ":";
        $jsonstring .= "[";
        for ($j = 0; $j < $this->getRecordCount(); $j++) {
            if ($j == 0) {
                $jsonstring .= "{";
            } else {
                $jsonstring .= ",{";
            }

            $bstart = true;
            for ($k = 0; $k < $this->getFieldCount(); $k++) {
                $name = $this->GetFieldName($k);
                $ct = $this->getDataType($k);
                $value = $this->toValue($k, $j);
                if (is_object($value)){
                    $value = $value->format("Y-m-d H:i:s");
                }

                $strValue = NULL;
                if (substr($value, 0, 1) == "{" && substr($value, strlen($value) - 1, 1) == "}")
                    $strValue = $this->getSimplejsonstring($name) . ":" . $value;
                else
                    $strValue = $this->getjsonstring($name, $value);
                
                if ($bstart) {
                    $bstart = false;
                    $jsonstring .= $strValue;
                } else {
                    $jsonstring .= "," . $strValue;
                }

            }
            $jsonstring .= "}";

        }
        $jsonstring .= "]";

        $jsonstring .= "}";
        $json = $jsonstring;
        return $json;
    }

    public function locate($keyvalues)
    {
        if (count($keyvalues) == 0) {
            return -1;
        }

        if ($this->getRecordCount() == 0) {
            return -1;
        }

        $r = NULL;
        $b = false;
        foreach ($this->indexs as $index) {
            $b = false;
            foreach ($keyvalues as $key => $value) {
                $b = isset($index["fields"][strtoupper($key)]);
                if (!$b) {
                    break;
                }

            }
            if ($b) {
                $r = &$index;
                break;
            }
        }
        if (!$b) {
            $index = array();
            $index["fields"] = array();
            foreach (array_keys($keyvalues) as $key) {
                $index["fields"][strtoupper($key)] = $key;
            }

            for ($i = 0; $i < $this->getRecordCount(); $i++) {
                $key = "";
                $keys = array_values($index["fields"]);
                for ($j = 0; $j < count($keys); $j++) {
                    $v = $this->toValue($keys[$j], $i);
                    if (empty($v)) {
                        return -1;
                    }

                    $key .= $v;
                }
                $md5Key = md5($key);
                if (!isset($index["rowkeys"])) {
                    $index["rowkeys"] = array();
                }

                $rows = &$index["rowkeys"];
                if (!isset($rows[$md5Key])) {
                    $rows[$md5Key] = array();
                }
                $row = &$rows[$md5Key];
                $row[] = $i;

                if (!isset($index["rowindexs"])) {
                    $index["rowindexs"] = array();
                }

                unset($rows);
                unset($row);

                $rows = &$index["rowindexs"];
                if (!isset($rows[$i])) {
                    $rows[$i] = array();
                }
                $row = &$rows[$i];
                $row[] = $md5Key;
            }

            $this->indexs[] = &$index;

            $r = &$index;
        }

        $key = "";
        $keys = array_values($r["fields"]);
        for ($j = 0; $j < count($keys); $j++) {
            $v = $keyvalues[$keys[$j]];
            $key .= $v;
        }

        $md5Key = md5($key);
        $rows = $r["rowkeys"];
        if (isset($rows[$md5Key])) {
            $rowindex = $rows[$md5Key][0];
            return $rowindex;
        } else {
            return -1;
        }

    }

    public function toCodeTableJSON($pcode)
    {
        $vstr = "[";
        $start = true;
        for ($j = 0; $j < $this->getRecordCount(); $j++) {
            $id = $this->toValue("Code", $j);
            $text = $this->toValue("CodeMean", $j);
            $pvalue = empty($pcode) ? "" : $this->toValue("PCode", $j);

            if ($pvalue != $pcode) {
                continue;
            }

            $keys = array();
            $keys["PCode"] = $id;
            $isLeaf = $this->locate($keys) == -1;

            $pvalue = $this->getSimplejsonstring($pvalue);

            $id = $this->getSimplejsonstring($id);
            $text = $this->getSimplejsonstring($text);

            if ($start) {
                $start = false;
                $vstr = $vstr . "{\"CodeMean\":" . $text . ",\"Code\":" . $id . ",\"PCode\":" . $pvalue;
            } else {
                $vstr = $vstr . "," . "{\"CodeMean\":" . $text . ",\"Code\":" . $id . ",\"PCode\":" . $pvalue;
            }

            if (!$isLeaf) {
                $vstr .= ",\"isLeaf\":false,\"expanded\":false";
            } else {
                $vstr .= ",\"isLeaf\":true,\"expanded\":false";
            }

            $vstr .= "}";
        }
        $vstr = $vstr . "]";
        return $vstr;
    }

    public function &getRows()
    {
        $this->curRow = NULL;
        if (empty($this->dataset)) {
            return array();
        }

        if (empty($this->rows)) {
            $this->rows = array();
            while ($this->next()) {
                $row = array();
                for ($i = 0; $i < $this->getFieldCount(); $i++) {
                    $fieldname = convertDBStringCode($this->getFieldName($i));
                    $value = $this->convertString($i, $this->valueOf($i));
                    $row[$fieldname] = $value;
                    $row[$i] = &$row[$fieldname];
                }
                $row[self::$ROW_STATE] = "none";

                $this->rows[] = $row;
            }
            if (count($this->rows) > 0)
                $this->curRow = $this->rows[0];
                
        }

        return $this->rows;
    }

    public function isOpenDataset()
    {
        return !empty($this->dataset) && $this->dataset !== false;
    }

    public function isConnection()
    {
        return !empty($this->connection) && $this->connection !== false;
    }

    public function simpleLoadFromJSON($json)
    {
        $tbname = "";
        $where = "";
        $orderKeys = "";
        $fields = "";
        $order = "";
        $group = "";
        $pagenum = 0;
        $pageno = 0;
        return $this->loadFromJSON($json, $tbname, $where, $orderKeys, $fields, $order, $group, $pagenum, $pageno);
    }

    public function loadFromJSON($json, &$tbname, &$where,
        &$orderKeys, &$fields, &$order, &$group,
        &$pagenum, &$pageno) {
        try {

            if (empty($json)) {
                return self::$JSONEmpty;
            }

            $this->closeDataset();

            $fields = "";
            $tmp = $json["KEYS"];
            $primKeys = array();
            $this->primkeys = array();
            if (!empty($tmp)) {
                $primKeys = explode(",", $tmp);
            }

            $order = "";
            $orderKeys = "";
            $group = "";
            $needquery = true;
            $pagenum = 1;
            $pageno = 0;

            $tbname = $json["TABLENAMES"];
            $where = $json["FILTER"];
            if (isset($json["FIELDS"])) {
                $fields = $json["FIELDS"];
            } else if (isset($json["FieldNames"])){
                $fields = $json["FieldNames"];
            }

            if (isset($json["KEYORDER"])) {
                $orderKeys = $json["KEYORDER"];
            }

            if (isset($json["ORDER"])) {
                $order = $json["ORDER"];
            }

            if (isset($json["GROUP"])) {
                $group = $json["GROUP"];
            }

            if (isset($json["NOTNEEDQUERY"])) {
                $needquery = strtolower($json["NOTNEEDQUERY"]) != "true";
            }

            if (isset($json["PAGECOUNT"])) {
                $pagenum = $json["PAGECOUNT"];
            }

            if (isset($json["PAGENO"])) {
                $pageno = $json["PAGENO"];
            }

            $jsonrows = $json["value"];

            if (empty($jsonrows) || count($jsonrows) == 0) {
                return self::$OK;
            }

            $rows = array();
            for ($i = 0; $i < count($jsonrows); $i++) {
                $jsonrow = $jsonrows[$i];
                $row = $jsonrow["DATA"];
                $rows[] = $row;
            }

            if ($needquery) {
                if (empty($where)) {
                    return self::$NotFoundWhere;
                }
                if (count($primKeys) == 0) {
                    return self::$NotFoundPrimKey;
                }

                $updatetables = array();
                $updatetables = explode(",", $tbname);

                $this->dataset = $this->simpleSelect($tbname, $fields, $where, $group, $order, $orderKeys, $pagenum, $pageno, array());
                if ($this->dataset === false) {
                    return self::$NotOpenDataset;
                }

                $this->getRows();
            } else {
                $this->rows = array();
            }

            foreach($primKeys as $key){
                $this->primkeys[strtoupper($key)] = $key;
            }

            foreach ($rows as $row) {
                $state = $row["_state"];

                $keys = array();

                foreach ($primKeys as $primKey) {
                    if ($state == "modified") {
                        $oldkey = $primKey . ".old";
                        if (isset($row[$oldkey])) {
                            $keys[$primKey] = $row[$oldkey];
                            continue;
                        }
                    }

                    if (!isset($row[$primKey])) {
                        return self::$NotFoundKey;
                    }

                    $keys[$primKey] = $row[$primKey];
                }

                $curRow = NULL;
                $index = -1;
                if ($state == "added") {
                    $curRow = array();
                    $this->rows[] = &$curRow;
                } else {
                    $index = $this->Locate($keys);
                    if ($index != -1) {
                        $curRow = &$this->rows[$index];
                    }

                }

                if (!isset($curRow)) {
                    return self::$LocateNotFound;
                }

                $curRow[self::$ROW_STATE] = $state;

                if ($state == "removed") {
                    ;
                } else {
                    foreach (array_keys($row) as $key) {
                        if ($key == self::$ROW_STATE) {
                            continue;
                        }

                        if (substr($key, -4) == ".old") {
                            continue;
                        }

                        $value = $row[$key];
                        $curRow[$key] = is_array($value) ? json_encode($value) : $value;
                    }
                }
                unset($curRow);
            }

            return self::$OK;
        } catch (Exception $e) {
            return self::$UnKnownException;
        }
    }

    protected function updateRow(&$row)
    {
        if (empty($row)) {
            return true;
        }
        $fields = array();
        $values = array();

        foreach ($row as $field => $value) {
            if (is_int($field))
                continue;

            if (substr($field, 0, 1) == "_")
                continue;

            $fields[] = $field;
            $values[] = $value;
        }

        if (!isset($row[self::$ROW_STATE])) {
            return;
        }

        switch ($row[self::$ROW_STATE]) {
            case self::$ROW_STATE_NONE:
                continue;
            case self::$ROW_STATE_ADD:
                return $this->insert($this->sqlBuilder, $fields, $values) != -1;
            case self::$ROW_STATE_EDIT:
                return $this->update($this->sqlBuilder, $fields, $values) != -1;
            case self::$ROW_STATE_REMOVE:
                return $this->delete($this->sqlBuilder, $fields, $values) != -1;
        }

        return true;
    }

    public function __construct($serverName, $connectionInfo)
    {
        $this->close();
        $this->connection = $this->initConnection($serverName, $connectionInfo);
    }

    public function __destruct()
    {
        $this->close();
    }

    public function clear()
    {
        $this->columns = array();
        $this->rows = array();
        $this->indexs = array();
        $this->primkeys = array();
    }

    public function close()
    {
        $this->closeDataset();
        $this->closeConnection();
    }

    protected function createDDLSqlBuilder(SQLBuilder $source, $ddlType, $fields = array(), $values = array()){

        $sqlBuilder = $this->createSQLBuilder();
        $sqlBuilder->setRawFields($source->getFields());
        $sqlBuilder->addTable($source->gettablename());
        $sqlBuilder->setSqlType($ddlType);
        if ($ddlType != SQLBuilder::$stInsert){
            $sqlBuilder->setRawwhere($source->getwhere());
            $sqlBuilder->addLogicalOperation(SQLBuilder::$otAnd);
            $sqlBuilder->addLogicalOperation(SQLBuilder::$otLeftPair);
    
            $first = true;
            for ($i = 0; $i < count($fields); $i++) {
                $field = $fields[$i];
                if (!$this->isPrimKey($field))
                    continue;
                    
                if (!$first) {
                    $sqlBuilder->addLogicalOperation(SQLBuilder::$otAnd);
                }
                if ($first)
                    $first = false;
    
                $sqlBuilder->addwhere($fields[$i], SQLBuilder::$otEqual, $values[$i]);
            }
            $sqlBuilder->addLogicalOperation(SQLBuilder::$otRightPair);
        }        
        return $sqlBuilder;
    }

    public function insert(SQLBuilder $sqlBuilder, $fields, $values)
    {
        $insertFields = array();
        $insertValues = array();
        for ($i=0; $i < count($values); $i++) { 
            $value = $values[$i];
            if (empty($value) || strtoupper($value) == "NULL" || strtolower($value) == "undefined")
                continue;
            $insertFields[] = $fields[$i];
            $insertValues[] = $values[$i];
        }
        $insertSqlBuilder = $this->createDDLSqlBuilder($sqlBuilder, SQLBuilder::$stInsert);
        $insertSqlBuilder->setRawFields($insertFields);
        $insertSqlBuilder->setValue($insertValues);
        $sql = $insertSqlBuilder->getSql();

        return $this->exec($sql, $values);
    }

    public function delete(SQLBuilder $sqlBuilder, $fields, $values)
    {
        if (count($this->primkeys) == 0)
            throw new Exception("no found primkeys");

        $delSqlBuilder = $this->createDDLSqlBuilder($sqlBuilder, SQLBuilder::$stDelete, $fields, $values);
        $sql = $delSqlBuilder->getSql();
        return $this->exec($sql, array());
    }

    public function update(SQLBuilder $sqlBuilder, $fields, $values)
    {
        if (count($this->primkeys) == 0)
            throw new Exception("no found primkeys");

        $updateSqlBuilder = $this->createDDLSqlBuilder($sqlBuilder, SQLBuilder::$stUpdate, $fields, $values);
        for ($i = 0; $i < count($fields); $i++) {
            if ($this->isPrimKey($fields[$i]))
                continue;

            $updateSqlBuilder->addset($fields[$i], $values[$i]);
        }
        $sql = $updateSqlBuilder->getSql();

        return $this->exec($sql, array());
    }

    public function simpleSelect($tablename, $fields = "*", $where = NULL, $group = NULL, $order = NULL, $orderkey = NULL, $pagenum = 20, $pageno = 0, $params = array())
    {
        $this->sqlBuilder = new SQLBuilder(SQLBuilder::$dbMSSQL);
        $this->sqlBuilder->addTable($tablename);
        if (empty($fields)) {
            $this->sqlBuilder->addField("*");
        } else {
            $this->sqlBuilder->addField($fields);
        }

        if (!empty($where)) {
            $this->sqlBuilder->setRawwhere($where);
        }

        if (!empty($group)) {
            $this->sqlBuilder->addgroup($group);
        }

        if (!empty($order)) {
            $this->sqlBuilder->addSort($order);
        }

        if (!empty($orderkey)) {
            $this->sqlBuilder->addPageorderField($orderkey);
        }

        if (!empty($pagenum)) {
            $this->sqlBuilder->setpageSize($pagenum);
        }

        if (!empty($pageno)) {
            $this->sqlBuilder->setpagePos($pageno);
        }

        $sql = $this->sqlBuilder->getSql();
        return $this->query($sql, $params);
    }

    public function select(SQLBuilder $sqlbuilder, $params = array())
    {
        $sql = $sqlbuilder->getSql();
        $this->sqlBuilder = clone $sqlbuilder;
        return $this->query($sql, $params);
    }

    public function callProc($name, $params)
    {
        $command = $this->getCommand($name, $params);
        return $this->exec($command, $params);
    }

}
