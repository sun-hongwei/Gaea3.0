<?php

require_once 'Configure.php';
require_once 'throws.php';

class LocalStorage
{
    protected static $redis;
    protected static $BASE_KEY = "LocalStorage_PHP_GAEA_";
    protected static $WATCH_KEY = "LocalStorage_PHP_GAEA_WATCH_";
    public static function getKey($simpleKey)
    {
        return self::$BASE_KEY . $simpleKey;
    }

    protected static function REDIS()
    {
        if (empty(self::$redis)) {
            self::$redis = new Redis();
            $connectionInfo = Configure::get("LocalStorage", "host", "port", "password");
            self::$redis->pconnect($connectionInfo["host"], $connectionInfo["port"]);
            if (!self::$redis->auth($connectionInfo["password"]))
                throwException("not auth!");
        }

        return self::$redis;
    }

    public static function calls($watchKey, $callback, ...$params)
    {
        self::REDIS()->watch($watchKey);
        self::REDIS()->multi();
        try {
            $callback(...$params);
            if (self::REDIS()->exec() === false) {
                throwException("calls exec failed!");
            }
        } catch (Exception $e) {
            self::REDIS()->discard();
            throwException($e);
        }
    }

    protected $realRoot;
    protected $root;

    public function getType()
    {
        if (self::REDIS()->exists($this->realRoot))
            return self::REDIS()->type($this->realRoot);
        else
            return Redis::REDIS_NOT_FOUND;
    }

    public function setRoot($root)
    {
        $checkKey = self::getKey($root);
        $this->root = $root;
        $this->realRoot = $checkKey;
    }

    public function setRealRoot($root)
    {
        $this->root = $root;
        $this->realRoot = $root;
    }

    public function getRoot()
    {
        return $this->root;
    }

    public function exists($key)
    {
        if (!self::REDIS()->exists($this->realRoot))
            return false;
        return self::REDIS()->hExists($this->realRoot, $key);
    }

    public function set($key, $value, $nx = false)
    {
        if ($nx) {
            if (self::REDIS()->hSetNx($this->realRoot, $key, self::encodeValue($value)) === false) {
                throwException("redis set " . $key . " failed!");
            }
        } else {
            if (self::REDIS()->hSet($this->realRoot, $key, self::encodeValue($value)) === false) {
                throwException("redis set " . $key . " failed!");
            }
        }
    }

    public function save()
    {
        self::REDIS()->bgSave();
    }

    public function sets($keyValues, $nx = false)
    {
        if ($nx) { 
            $this->calls($this->realRoot, function($keyValues, $db){
                foreach($keyValues as $key => $value){
                    $db->set($key, $value, true);
                }    
            }, $keyValues, $this);
        } else {
            $value = self::encodeValues($keyValues);
            if (self::REDIS()->hMSet($this->realRoot, $value) === false) {
                throwException("redis sets " . $keyValues . " failed!");
            }
        }
    }

    public function get($key)
    {
        return self::decodeValue(self::REDIS()->hGet($this->realRoot, $key));
    }

    public function getAll()
    {
        return self::decodeValues(self::REDIS()->hGetAll($this->realRoot));
    }

    protected static function decodeValues($values)
    {
        if (empty($values))
            return $values;

        $result = array();
        foreach ($values as $key => $value) {
            $result[$key] = self::decodeValue($value);
        }

        return $result;
    }

    protected static function decodeValue($value)
    {
        if (empty($value))
            return $value;

        try {
            $data = json_decode($value, true);
            if (empty($data))
                return $value;
            else
                return $data;
        } catch (JsonException $e) {
            return $value;
        }
    }

    protected static function encodeValues($values)
    {
        $result = array();
        foreach ($values as $key => $value) {
            try {
                $result[$key] = self::encodeValue($value);
            } catch (Exception $e) {
                $result[$key] = $value;
            }
        }

        return $result;
    }

    protected static function encodeValue($value)
    {
        if (empty($value))
            return $value;

        if (!is_array($value)) {
            return $value;
        }

        return json_encode($value, true);
    }

    public function gets(array $keys)
    {
        return self::decodeValues(self::REDIS()->hMGet($this->realRoot, $keys));
    }

    public function keys()
    {
        return self::REDIS()->hKeys($this->realRoot);
    }

    public function all($start = null, $size = null)
    {
        switch ($this->getType()) {
            case Redis::REDIS_LIST:
                return $this->getList($start, $size);
            case Redis::REDIS_HASH:
                return $this->getAll();
            case Redis::REDIS_NOT_FOUND:
            case Redis::REDIS_STRING:
            case Redis::REDIS_SET:
            case Redis::REDIS_ZSET:
                return null;
        }
    }

    public function findKeys($expr)
    {
        $result = self::REDIS()->keys(self::getKey($expr));
        if ($result === false) {
            return array();
        } else {
            return $result;
        }
    }

    public function values()
    {
        return self::decodeValues(self::REDIS()->hValues($this->realRoot));
    }

    public function deleteHash($key)
    {
        if (self::REDIS()->hDel($this->realRoot, $key) === false) {
            throwException("redis delete " . $key . " failed!");
        }
    }

    public function delete()
    {
        if (self::REDIS()->delete($this->realRoot) === false) {
            throwException("redis delete " . $this->root . " failed!");
        }
    }

    public function deletes($keys)
    {
        if (self::REDIS()->delete($keys) === false) {
            throwException("redis delete " . $keys . " failed!");
        }
    }

    public function inc($key, $step = 1)
    {
        $step = abs($step);
        return self::REDIS()->hIncrBy($this->realRoot, $key, $step);
    }

    public function dec($key, $step = -1)
    {
        $step = -abs($step);
        return self::REDIS()->hIncrBy($this->realRoot, $key, $step);
    }

    public function size()
    {
        return self::REDIS()->hLen($this->realRoot);
    }

    public function addToList($value)
    {
        self::REDIS()->rPush($this->realRoot, self::encodeValue($value));
    }

    public function getList($start = null, $size = null)
    {
        $start = empty($start) ? 0 : $start;
        $end = empty($size) ? -1 : ($start + $size);
        return self::decodeValues(self::REDIS()->lRange($this->realRoot, $start, $end));
    }

    public function removeList($value = null, $count = null)
    {
        if (empty($value) && empty($count)) {
            if (self::REDIS()->delete($this->realRoot) === false) {
                throwException("delete list[" . $this->root . "] failed!");
            }
        } else if (!empty($value)) {
            if (self::REDIS()->lRem($this->realRoot, $value, empty($count) ? 1 : $count) === false) {
                throwException("delete list[" . $this->root . "." . $value . "] failed!");
            }
        }
    }

    public function copy($sourceRoot, $destRoot)
    {
        $this->setRoot($sourceRoot);
        $values = $this->all();
        if (empty($values))
            return;

        $this->setRoot($destRoot);
        $this->sets($values);
    }

    public function move($sourceRoot, $destRoot)
    {
        $this->setRoot($sourceRoot);
        $values = $this->all();
        if (empty($values))
            return;

        $this->calls(
            array($sourceRoot, $destRoot),
            function ($db, $values, $destRoot) {
                // $sourceRoot = self::getKey($sourceRoot);
                // $destRoot = self::getKey($destRoot);

                $db->delete();
                $db->setRoot($destRoot);
                $db->sets($values);
            },
            $this,
            $values,
            $destRoot
        );
    }

    public function moveToList($sourceRoot, $destRoot)
    {
        $this->setRoot($sourceRoot);
        $values = $this->all();
        if (empty($values))
            return;

        $this->calls(
            array($sourceRoot, $destRoot),
            function ($db, $values, $destRoot) {
                // $sourceRoot = self::getKey($sourceRoot);
                // $destRoot = self::getKey($destRoot);

                $db->delete();
                $db->setRoot($destRoot);
                $db->addToList($values);
            },
            $this,
            $values,
            $destRoot
        );
    }

    public function find($key, $start, $count)
    {
        $start = empty($start) ? 0 : $start;
        $count = empty($count) ? 20 : $count;
        self::REDIS()->setOption(Redis::OPT_SCAN, Redis::SCAN_RETRY);
        $result = array();
        while ($arr_keys = self::REDIS()->hScan($this->realRoot, $start, $key, $count)) {
            foreach ($arr_keys as $str_field => $str_value) {
                $result[$str_field] = self::decodeValue($str_value);
            }
        }
        return $result;
    }

    public function keyExists($key){
        return self::REDIS()->exists($key);
    }
}
