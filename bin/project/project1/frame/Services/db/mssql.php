<?php
require_once "SqlBuilder.php";
require_once "db.php";
require_once 'StringEncode.php';

class MSDB extends DB
{

    /*
    $con = sqlsrv_connect("Driver={SQL Server};Server=数据库ip地址;Database=数据库名称;", "登录名","登录密码" ) or die("连接失败");
     */

    public function __construct()
    {
        $xml = simplexml_load_file('../config/ServiceCommunication.xml');
        // $connectionString = $xml->db->attributes()->dsn;

        $serverName = (string) $xml->db->attributes()->serverName;
        $Database = (string) $xml->db->attributes()->dbName;
        $user = (string) $xml->db->attributes()->userName;
        $pwd = (string) $xml->db->attributes()->password;

        $connectionInfo = array("Database" => $Database, "UID" => $user, "PWD" => $pwd);
        parent::__construct($serverName, $connectionInfo);
    }

    protected function initConnection($serverName, $connectionInfo)
    {
        $this->close();
        return sqlsrv_connect($serverName, $connectionInfo);
    }

    public function closeDatasetResource()
    {
        if (!empty($this->dataset)) {
            sqlsrv_free_stmt($this->dataset);
            $this->dataset = null;
        }
    }

    public function closeConnection()
    {
        if (!empty($this->connection)) {
            sqlsrv_close($this->connection);
            $this->connection = null;
        }
    }

    public function &getFieldNames()
    {
        if (empty($this->columns)) {
            $this->columns = array();
            $columns = sqlsrv_field_metadata($this->dataset);
            $index = 0;
            foreach ($columns as $fieldMetadata) {
                $fieldname = convertDBStringCode($fieldMetadata["Name"]);
                $this->columns[$index++] = $fieldMetadata;
                $this->columns[$fieldname] = $fieldMetadata;
            }
        }

        return $this->columns;
    }

    public function nextResult()
    {
        $this->clear();
        return sqlsrv_next_result($this->Dataset);
    }

    public function &query($sql, $params = array())
    {
        $this->lastError = null;
        $this->closeDataset();

        $sql = convertStringCode($sql, "GBK");
        $this->dataset = sqlsrv_query($this->connection, $sql, $params);
        if ($this->dataset === false) {
            $this->dataset = null;
        }

        if (!empty($this->dataset)) {
            $this->getRows();
        } else {
            $msg = sqlsrv_errors()[0][2];
            $msg = convertDBStringCode($msg);
            $this->lastError = $msg;
        }
        return $this->dataset;
    }

    public $lastError = null;
    public function exec($sql, $params = array())
    {
        $this->lastError = null;
        $this->closeDataset();

        $sql = convertStringCode($sql, "GBK");

        $this->dataset = sqlsrv_prepare($this->connection, $sql, $params);
        if ($this->dataset === false) {
            $this->dataset = null;
            return -1;
        }

        $b = sqlsrv_execute($this->dataset);
        if ($b) {
            $this->getRows();
        } else {
            $msg = sqlsrv_errors()[0][2];
            $msg = convertDBStringCode($msg);
            $this->lastError = $msg;
        }

        $ret = $b ? sqlsrv_rows_affected($this->dataset) : -1;
        return $ret;
    }

    protected function getRow()
    {
        if (empty($this->curRow)) {
            return null;
        }

        return $this->curRow;
    }

    public function toBinary($dataset, $columnIndex)
    {
        $row = &$this->getRow();
        if (empty($row)) {
            return null;
        }

        return $row[$columnIndex];
    }

    public function beginTran()
    {
        sqlsrv_begin_transaction($this->connection);
    }

    public function commitTran()
    {
        sqlsrv_commit($this->connection);
    }

    public function rollbackTran()
    {
        sqlsrv_rollback($this->connection);
    }

    protected function next()
    {
        $this->curRow = sqlsrv_fetch_array($this->dataset);
        return !empty($this->curRow);
    }

    public function getDataType($index)
    {
        $columns = $this->getFieldNames();
        $name = $this->getFieldName($index);
        return $columns[$name]["Type"];
    }

    public function getFieldCount()
    {
        return sqlsrv_num_fields($this->dataset);
    }

    public function getDataSize($index)
    {
        $columns = $this->getFieldNames();
        $name = $this->getFieldName($index);
        return $columns[$name]["Size"];
    }

    public function getFieldName($index)
    {
        $this->getFieldNames();
        return $this->columns[$index]["Name"];
    }

    public function getRecordCount()
    {
        return count($this->getRows());
    }

    public function &getPrimkeys()
    {
        return $this->primkeys;
    }

    protected function convertString($col, $data)
    {
        if (empty($data)) {
            return $data;
        }

        if (!is_string($data)) {
            return $data;
        }

        $column = $this->columns[$col];
        switch ($column["Type"]) {
            case 1:
            case -8:
            case -10:
            case -9:
            case -1:
            case 12:
                try {
                    return convertDBStringCode($data);
                } catch (Exception $e) {
                    return $data;
                }
        }

        return $data;
    }

    public function valueOf($col)
    {
        $this->getRow();
        return $this->curRow[$col];
    }

    public function toValue($col, $row)
    {
        $this->curRow = $this->getRows()[$row];
        return isset($this->curRow[$col]) ? $this->curRow[$col] : null;
    }

    public function getPageQueryTotalCount()
    {
        $where = $this->sqlBuilder->getwhere();
        $group = $this->sqlBuilder->getgroup();

        $sql = "SELECT count(*) as tc from (select " . $this->sqlBuilder->getFieldsString() . " FROM " . $this->sqlBuilder->gettablename();

        if (!empty($where)) {
            $sql .= " where " . $where;
        }

        $sql .= ") as dtable ";

        if (!empty($group)) {
            $sql .= " group by "+$group;
        }

        $db = new MSDB();
        $ds = $db->query($sql, array());
        if ($ds === false) {
            return -1;
        } else {
            $count = $db->valueOf(0);
            return $count;
        }

    }

    public function post()
    {
        $rows = &$this->rows;
        if (empty($rows)) {
            return true;
        }

        $primkeys = $this->primkeys;
        foreach ($rows as $row) {
            $this->primkeys = $primkeys;
            if (!$this->updateRow($row)) {
                throw new Exception(json_encode($row) . "update row【" . json_encode($row) . "】 is failed!");
            }

        }
    }

    protected function createSQLBuilder()
    {
        return new SQLBuilder(SQLBuilder::$dbMSSQL);
    }

    protected function getProcCommand($name, $params)
    {
        $command = "{call " . $name . "(";
        if (!empty($params)) {
            $tmp = "";
            for ($i = 0; $i < count($inParams); $i++) {
                if (empty($tmp)) {
                    $tmp = "?";
                } else {
                    $tmp .= ",?";
                }

            }

            $command .= $tmp;
        }

        return $command . ")}";
    }

}
