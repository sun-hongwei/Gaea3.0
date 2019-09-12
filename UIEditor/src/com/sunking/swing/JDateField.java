package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JDateField ���������</p>
 *                 ȷ��������ȷ������
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.sunking.swing.refer.JDateDocument;

public class JDateField
    extends JTextField
    implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * �����ո�ʽ
     */
    public static final SimpleDateFormat dateFormat
        = new SimpleDateFormat("yyyy-MM-dd");

    private JDateDocument doc = new JDateDocument(this,dateFormat);
    public JDateField() {
        setPreferredSize(new Dimension(100, 25));
        setDocument(doc);
        super.setHorizontalAlignment(JTextField.CENTER);
    }

    /**
     * ȡ������
     * @return Date
     */
    public Date getDate() {
        try {
            return dateFormat.parse(this.getText());
        }
        catch (Exception ex) {
            return new Date();
        }
    }

    /**
     * ��������
     * @return Date
     */
    public void setDate(Date date) {
        try {
            dateFormat.format(date);
        }
        catch (Exception ex) {
            dateFormat.format(new Date());
        }
    }

    /**
     * ����
     * @param args String[]
     */
    public static void main(String[] args) {
        JFrame frame = OpenSwingUtil.createDemoFrame("JDateField Demo");
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JDateField(), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
