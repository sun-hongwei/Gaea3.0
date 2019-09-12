package com.sunking.swing.refer;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * <p>Title:OpenSwing </p>
 * <p>Description: JDateDocument ʵ�����ڵ������ʽ����<BR>
 * ����:<BR>
 * 2004/03/26   ��������caiyj�Ľ���������recoonд�Ĺ���JDateDocument��У�鷽��<BR>
 * 2004/04/02   ��������caiyj�ύ��BUG,��������ΪTableCellEditorʱ����ѡ����嵯��������<BR>
 * 2006/02/13   �Թ���ʽ�����˲�������<BR>
 * </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com"'>Sunking</a>
 * @version 1.0
 */
public class JDateDocument extends PlainDocument{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextComponent textComponent; //���������ı���
    private int newOffset; //�µĲ���λ��

    private SimpleDateFormat dateFormat;

    /******************************************************************
     ** �������ƣ�JDateDocument
     ** �������������ô��ı�����ʾ��Ĭ��ֵ����ʽ���ƺ͵�ǰ����
     ** ��ڲ�����
     ** tc : JTextComponent����,��ǰ�������ı���
     ** dateFormat : SimpleDateFormat����,��ǰ�����ı���ĸ�ʽ����
     ** initDateTime : String����,��ǰ����
     ** ����ֵ����
     ** �����ߣ���JDateDocument
     *******************************************************************/
    public JDateDocument(JTextComponent tc, SimpleDateFormat dateFormat) throws
        UnsupportedOperationException{
        //��ǰ���ڹ���
        this(tc, dateFormat, getCurrentDate(dateFormat));
    }

    public JDateDocument(JTextComponent tc,
                         SimpleDateFormat dateFormat,
                         String initDateTime) throws
        UnsupportedOperationException{
        //���õ�ǰ���ڸ�ʽ
        setDateFormat(dateFormat);
        //����������ı���
        textComponent = tc;
        //������ʾΪ��ǰ����,ͬʱ�����ʾ�ĸ�ʽ��
        try{
            insertString(0, initDateTime, null);
        } catch(BadLocationException ex){
            throw new UnsupportedOperationException(ex.getMessage());
        }
    }

    /**
     * ���õ�ǰ���ڸ�ʽ
     * @param dateFormat SimpleDateFormat
     */
    public void setDateFormat(SimpleDateFormat dateFormat){
        this.dateFormat = dateFormat;
    }

    /**
     * ȡ�õ�ǰ���ڸ�ʽ
     * @return SimpleDateFormat
     */
    public SimpleDateFormat getDateFormat(){
        return dateFormat;
    }

    /**
     * ȡ�õ�ǰϵͳ��ʱ
     * @param dateFormat SimpleDateFormat
     * @return String
     */
    public final static String getCurrentDate(SimpleDateFormat smFormat){
        return smFormat.format(new Date());
    }

    /******************************************************************
     ** �������ƣ�public void insertString(int offset, String s,
     **             AttributeSet attributeSet) throws BadLocationException
     ** ��������������ԭ����,�����û������ʽΪ���ڸ�ʽ
     ** ��ڲ�����offset: int��,����λ��
     **            s: String��,�����ַ���
     **            attributeSet: AttributeSet��,���Լ�
     ** ����ֵ����
     ** �����ߣ���JDateDocument
     *******************************************************************/
    public void insertString(int offset, String s,
                             AttributeSet attributeSet) throws
        BadLocationException{
        String toTest; //���ڲ�������Ϸ��Ե��ַ���
        //�жϲ����ַ�������
        if(s.length() == 1){
            //����Ϊ1
            try{
                //��������Ϊ����
                Integer.parseInt(s);
            } catch(Exception ex){
                //��������ʾ������
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            //ȡ��ԭʼ����λ��
            newOffset = offset;
            //�������λ��Ϊ"/"," ","-"���ŵ�ǰ��,���ƶ�����������(�ı�newOffset��ֵ)
            if(offset == 4 || offset == 7 ||
               offset == 10 || offset == 13 ||
               offset == 16){
                newOffset++;
                textComponent.setCaretPosition(newOffset);
            }
            //�������λ��Ϊ���,�򲻲���
            if(offset == dateFormat.toPattern().length()){
                return;
            }
            //ȡ����ʾ��ʱ��,�����õ�Ҫ��ʾ���ַ���
            toTest = textComponent.getText();
            toTest = toTest.substring(0, newOffset) + s +
                toTest.substring(newOffset + 1);
            //���Ҫ��ʾ���ַ����Ϸ�,����ʾ,���������ʾ���˳�
            boolean isValid = isValidDate(toTest);
            if(!isValid){
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            //�����ַ���
            super.remove(newOffset, 1);
            super.insertString(newOffset, s, attributeSet);
        }
        //������볤��10
        else if(s.length() == 10 || s.length() == 19){
            //�Ϸ�����ʾ,���������ʾ�˳�
            if(!isValidDate(s)){
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            //�����ַ���
            super.remove(0, getLength());
            super.insertString(0, s, attributeSet);
        }
    }

    /**********************************************************************************
     ** �������ƣ�public void remove(int offset, int length) throws BadLocationException
     ** ��������������ԭ����,ɾ������λ�õ��ַ���
     ** ��ڲ�����offset: int��,����λ��
     **            length: int��,ɾ������
     ** ����ֵ����
     ** �����ߣ�insertString(int, String,AttributeSet)
     ***********************************************************************************/
    public void remove(int offset, int length) throws BadLocationException{
        //�������λ����"-"ǰ,�����һ�����λ��
        //yyyy-MM-dd HH:mm:ss
        if(offset == 4 || offset == 7 ||
           offset == 10 || offset == 13 ||
           offset == 16)
            textComponent.setCaretPosition(offset - 1);
        else
            textComponent.setCaretPosition(offset);
    }

    /**********************************************************************************
     ** �������ƣ�public boolean isLegalDate(String strDate)
     ** �����������жϲ���ĳ���Ϊ10���ַ����Ƿ�Ϸ�
     ** ��ڲ�����intY: int��,���ֵ
     **            intM: int��,�µ�ֵ
     **            intD: int��,�յ�ֵ
     ** ����ֵ��boolean��,��,��ʾ�ǺϷ���,��,��ʾ���Ϸ�
     ** �����ߣ�insertString(int, String,AttributeSet)
     ***********************************************************************************/
    private boolean isValidDate(String strDate){
        int intY, intM, intD; //��,��,��,ʱ,��,���ֵ
        int intH = 0, intMi = 0, intS = 0;
        int iCaretPosition; //���λ��
        int iPatternLen = getDateFormat().toPattern().length();
        //��ȡ�ַ���
        if(strDate == null){
            return false;
        }
        strDate = strDate.trim();
        //���Ϊ��,���Ȳ���,��Ϊ�Ƿ�,����false
        if(strDate.length() != iPatternLen){
            return false;
        }
        //�����ȫ���ַ�,�򷵻�false
        for(int i = 0; i < 10; i++){
            if(((int)strDate.charAt(i)) > 255){
                return false;
            }
        }
        //ȡ��,��,�յ�ֵ
        try{
            intY = Integer.parseInt(strDate.substring(0, 4));
            intM = Integer.parseInt(strDate.substring(5, 7));
            intD = Integer.parseInt(strDate.substring(8, 10));
        } catch(Exception e){
            //ʧ���򷵻�false
            return false;
        }
//        System.err.println("int:intY="+intY+",intM="+intM+",intD="+intD);
        iCaretPosition = textComponent.getCaretPosition();
        boolean isValid = true;

        //��Խ��
        if(intM > 12 || intM < 1){
            intM = Math.min(12, Math.max(1, intM));
            isValid = false;
        }
        //�����·�,�ж���������,��Խ��,���޸�
        if(intD < 1){
            intD = 1;
            isValid = false;
        }
        switch(intM){
            case 4:
            case 6:
            case 9:
            case 11: //�������Ϊ30��

                //����������30,���޸�Ϊ30
                if(intD > 30){
                    intD = 30;
                    isValid = false;
                }
                break;
            case 2: //2�·�

                //��������
                if((intY % 4 == 0 && intY % 100 != 0) || intY % 400 == 0){
                    //����������29,���޸�Ϊ29
                    if(intD > 29){
                        intD = 29;
                        isValid = false;
                    }
                } else{
                    //����������28,���޸�Ϊ28
                    if(intD > 28){
                        intD = 28;
                        isValid = false;
                    }
                }
                break;
            default: //�������Ϊ31��

                //����������31,���޸�Ϊ31
                if(intD > 31){
                    intD = 31;
                    isValid = false;
                }
        }
//        System.err.println("out:intY="+intY+",intM="+intM+",intD="+intD);

        //yyyy-MM-dd HH:mm:ss
        if(iPatternLen > 10){
            try{
                intH = Integer.parseInt(strDate.substring(11, 13));
                intMi = Integer.parseInt(strDate.substring(14, 16));
                intS = Integer.parseInt(strDate.substring(17));
            } catch(Exception e){
                return false;
            }
            //ʱԽ��
            if(intH > 23 || intH < 0){
                intH = Math.min(23, Math.max(0, intH));
                isValid = false;
            }
            //��Խ��
            if(intMi > 59 || intMi < 0){
                intMi = Math.min(59, Math.max(0, intMi));
                isValid = false;
            }
            //��Խ��
            if(intS > 59 || intS < 0){
                intS = Math.min(59, Math.max(0, intS));
                isValid = false;
            }
        }
        if(!isValid){
            textComponent.setText(toDateString(intY, intM, intD, intH, intMi,
                                               intS));
            textComponent.setCaretPosition(iCaretPosition + 1);
        }
        return isValid;
    }

    private String toDateString(int y, int m, int d, int h, int mi, int s){
        m = Math.max(1, Math.min(12, m));
        //�������Ϊ31��
        d = Math.max(1, Math.min(31, d));
        switch(m){
            case 4:
            case 6:
            case 9:
            case 11:
                d = Math.min(30, d); //�������Ϊ30��
                break;
            case 2:

                //����
                if((y % 4 == 0 && y % 100 != 0) || y % 400 == 0){
                    d = Math.min(29, d); //�������Ϊ29��
                } else{
                    d = Math.min(28, d); //�������Ϊ28��
                }
                break;
        }
        h = Math.max(1, Math.min(24, h));
        mi = Math.max(1, Math.min(59, mi));
        s = Math.max(1, Math.min(59, s));

        String strPattern = getDateFormat().toPattern();
        String strY = rPad0(4, "" + y);
        String strM = rPad0(2, "" + m);
        String strD = rPad0(2, "" + d);

        String strDate = "";
        strDate = strY + strPattern.substring(4, 5)
            + strM + strPattern.substring(7, 8) + strD;
        if(strPattern.length() == 19){
            strDate += strPattern.substring(10, 11)
                + rPad0(2, "" + h) + strPattern.substring(13, 14)
                + rPad0(2, "" + mi) + strPattern.substring(16, 17)
                + rPad0(2, "" + s);
        }
        return strDate;
    }

    private String rPad0(int maxLen, String str){
        if(str.length() < maxLen){
            str = "0" + str;
        }
        return str;
    }
}
