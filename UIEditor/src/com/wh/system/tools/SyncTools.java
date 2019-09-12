package com.wh.system.tools;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

/**
 * Created by Administrator on 2016/1/19.
 */
public class SyncTools {

    public interface ISyncValue {
        public interface IGetValue {
            public Object getValue();
        }

        public interface IGetValueAndWait{
            public void getValue(ISyncValue value);
        }
        ExecutorService pool = Executors.newFixedThreadPool(10);

        public void setValue(Object value);
        public Object getValue();
    }

    public static class SyncValue implements ISyncValue{
        Object value = null;
        IGetValue iGetValue = null;

        public SyncValue(IGetValue iGetValue) {
            this.iGetValue = iGetValue;
        }

        public SyncValue(){

        }

        public void setIGetValue(IGetValue iGetValue){
            this.iGetValue = iGetValue;
        }

        @Override
        public void setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public Object getValue() {
            if (EventQueue.isDispatchThread()) {
                return iGetValue.getValue();
            }

            value = null;
            final Object wait = new Object();
            synchronized (wait) {
                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (wait) {
                                try {
                                    value = iGetValue.getValue();
                                }catch(Throwable e){}
                                wait.notifyAll();
                            }
                        }
                    });
                    wait.wait();
                    return value;
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }
    }

    public static class SyncThreadValue implements ISyncValue{
        List<Object> values = new ArrayList<Object>();
        public SyncThreadValue() {
        }

        public void setValue(Object value){
            synchronized (values){
                values.add(value);
                values.notifyAll();
            }
        }

        public Object getValue() {
            synchronized (values){
                if (values.size() == 0){
                    try {
                        values.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }
                }

                return values.size() == 0?null:values.remove(0);
            }
        }
    }

    public static class ASyncThreadValue implements ISyncValue{
        Object value = null;
        Object syncObject = new Object();

        public ASyncThreadValue(){}
        public ASyncThreadValue(Object value){
            setValue(value);
        }

        public void setValue(Object value){
            synchronized (syncObject){
               this.value = value;
            }
        }

        public Object getValue() {
            synchronized (syncObject){
                return this.value;
            }
        }

        public boolean toBoolean(){
            Object obj = getValue();
            if (obj == null)
                return false;

            if (obj instanceof Boolean){
                return (Boolean)obj;
            }else
                return false;
        }

        public int toInteger(){
            Object obj = getValue();
            if (obj == null)
                return 0;

            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }else if (obj instanceof String){
                int value = Integer.parseInt((String) obj);
                return value;
            }else
                throw new ClassCastException("object no cast int!");
        }

        public float toFloat(){
            Object obj = getValue();
            if (obj == null)
                return 0F;

            if (obj instanceof Number) {
                return ((Number) obj).floatValue();
            }else if (obj instanceof String){
                float value = Float.parseFloat((String) obj);
                return value;
            }else
                throw new ClassCastException("object no cast float!");
        }

        public double toDouble(){
            Object obj = getValue();
            if (obj == null)
                return 0F;

            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }else if (obj instanceof String){
                double value = Double.parseDouble((String) obj);
                return value;
            }else
                throw new ClassCastException("object no cast double!");
        }

        public String toString(){
            Object obj = getValue();
            if (obj == null)
                return null;

            if (obj instanceof Number) {
                return obj.toString();
            }else if (obj instanceof String){
                return (String)value;
            }else
                throw new ClassCastException("object no cast string!");
        }

    }

    public static class SyncCallbackValue implements ISyncValue {

        public IGetValueAndWait iGetValue = null;

        public SyncCallbackValue(IGetValueAndWait iGetValue) {
            this.iGetValue = iGetValue;
        }

        @Override
        public void setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public Object getValue() {
            try {
                ISyncValue value = new SyncThreadValue();
                iGetValue.getValue(value);
                return value.getValue();
            } catch (Throwable e) {
                return null;
            }
        }
    }


}
