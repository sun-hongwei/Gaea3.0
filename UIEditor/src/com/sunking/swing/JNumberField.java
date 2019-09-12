package com.sunking.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JNumberField ���ָ�ʽ�����</p>
 * ����:<BR>
 * 2005/04/17   1.����󳤶�,С��λ���������ɷ������ݿⶨ��Ĺ淶,��:NUMBER(10,2)<BR>
 *                ��:��󳤶�10,С��λ����2,�������������ܳ���ֻ��Ϊ8;<BR>
 * 2005/04/21   �����˲������븺����BUG<BR>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class JNumberField
    extends JTextField
    implements ActionListener, FocusListener, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JNumberField() {
        this(true);
    }

    public JNumberField(boolean addAction) {
        this(16, 0, addAction);
    }

    public JNumberField(int intPartLen) {
        this(intPartLen, true);
    }

    public JNumberField(int intPartLen, boolean addAction) {
        this(intPartLen, 0, addAction);
    }

    public JNumberField(int maxLen, int decLen) {
        this(maxLen, decLen, true);
    }

    public JNumberField(int maxLen, int decLen, boolean addAction) {
        setPreferredSize(new Dimension(150, 25));
        setDocument(new NumberDocument(maxLen, decLen));
        super.setHorizontalAlignment(JTextField.RIGHT);
        if (addAction) addActionListener(this);
        addFocusListener(this);
    }

    public JNumberField(int maxLen,
                        int decLen,
                        double minRange,
                        double maxRange,
                        boolean addAction) {
        setPreferredSize(new Dimension(150, 25));
        setDocument(new NumberDocument(maxLen, decLen, minRange, maxRange));
        super.setHorizontalAlignment(JTextField.RIGHT);
        if (addAction) addActionListener(this);
        addFocusListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        transferFocus();
    }

    public void focusGained(FocusEvent e) {
        selectAll();
    }

    public void focusLost(FocusEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = OpenSwingUtil.createDemoFrame("JNumberField Demo");
        frame.getContentPane().setLayout(new GridLayout(10,2));
        frame.getContentPane().add(new JLabel("New JNumberField()"));
        frame.getContentPane().add(new JNumberField());
        frame.getContentPane().add(new JLabel("New JNumberField(2)"));
        frame.getContentPane().add(new JNumberField(2));
        frame.getContentPane().add(new JLabel("New JNumberField(8,2)"));
        frame.getContentPane().add(new JNumberField(8,2));
        frame.getContentPane().add(new JLabel("New JNumberField(5,2,-10,100)"));
        frame.getContentPane().add(new JNumberField(5, 2, -10, 100, false));
        frame.setVisible(true);
    }
}

class NumberDocument
    extends PlainDocument {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int maxLength = 16;
    int decLength = 0;
    double minRange = -Double.MAX_VALUE;
    double maxRange = Double.MAX_VALUE;
    public NumberDocument(int maxLen, int decLen) {
        maxLength = maxLen;
        decLength = decLen;
    }

    /**
     * @param decLen int  С��λ����
     * @param maxLen int  ��󳤶�(��С��λ)
     * @param minRange double  ��Сֵ
     * @param maxRange double  ���ֵ
     */
    public NumberDocument(int maxLen,
                          int decLen,
                          double minRange,
                          double maxRange){
        this(maxLen, decLen);
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    public NumberDocument(int decLen) {
        decLength = decLen;
    }

    public NumberDocument() {}

    public void insertString(int offset, String s, AttributeSet a) throws
        BadLocationException {
        String str = getText(0, getLength());

        if (
            //����Ϊf,F,d,D
            s.equals("F") || s.equals("f") || s.equals("D") || s.equals("d")
            //��һλ��0ʱ,�ڶ�λֻ��ΪС����
            || (str.trim().equals("0") && !s.substring(0, 1).equals(".") && offset != 0)
            //����ģʽ��������С����
            || (s.equals(".") && decLength == 0)
        ) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        String strIntPart = "";
        String strDecPart = "";
        String strNew = str.substring(0, offset) + s + str.substring(offset, getLength());
        strNew = strNew.replaceFirst("-",""); //���������븺��
        int decPos = strNew.indexOf(".");
        if(decPos > -1){
            strIntPart = strNew.substring(0,decPos);
            strDecPart = strNew.substring(decPos + 1);
        }else{
            strIntPart = strNew;
        }
        if(strIntPart.length() > (maxLength - decLength)
           || strDecPart.length() > decLength
           || (strNew.length() > 1
               && strNew.substring(0, 1).equals("0")
               && !strNew.substring(1, 2).equals("."))){
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        try {
            if ( !strNew.equals("") && !strNew.equals("-")  ) {//���������븺��
                double d = Double.parseDouble(strNew);
                if (d < minRange || d > maxRange){
                    throw new Exception();
                }
            }
        }
        catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(offset, s, a);
    }

}
