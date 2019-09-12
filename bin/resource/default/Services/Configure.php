<?php
class Configure{
    public static function get($root, ...$keys){
        $results = array();
        $result = NULL;
        foreach($keys as $key){
            $xml = simplexml_load_file( '../config/ServiceCommunication.xml' );
            $result = (string)$xml->$root->attributes()->$key;
            $results[$key] = $result;
        }

        if (count($results) == 1)
            return $result;
        else
            return $results;
    }
}
?>