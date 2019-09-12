package com.sunking.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description:JIpAddressField   IP��ַ��ʽ�������<BR>
 * ����:<BR>
 * 2004/04/27 �������� it-canoe �Ľ����������ҷ������"."���л�����λ�õĹ���<BR>
 * </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */

public class JIpAddressField
    extends JTextField
    implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JNumberField ip1 = new JNumberField(3, 0, 0, 255, true);
    JNumberField ip2 = new JNumberField(3, 0, 0, 255, true);
    JNumberField ip3 = new JNumberField(3, 0, 0, 255, true);
    JNumberField ip4 = new JNumberField(3, 0, 0, 255, true);

    class LeftRightKeyListener
        extends KeyAdapter {
        public static final String LEFT_TRAN = "LEFT_TRAN";
        public static final String RIGHT_TRAN = "RIGHT_TRAN";
        public void keyPressed(KeyEvent e) {
            JTextComponent txt = (JTextComponent) e.getComponent();
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                if (txt.getCaretPosition() == 0) {
                    txt.firePropertyChange(LEFT_TRAN, 0, 1);
                }
            }
            else if (e.getKeyCode() == KeyEvent.VK_RIGHT
                     || e.getKeyChar() == '.') {
                if (txt.getCaretPosition() == txt.getText().length()) {
                    txt.firePropertyChange(RIGHT_TRAN, 0, 1);
                }
            }
        }
    };
    class LRTransListener
        implements PropertyChangeListener {
        private Component leftComponent, rightComponent;
        public LRTransListener(Component leftComponent,
                               Component rightComponent) {
            this.leftComponent = leftComponent;
            this.rightComponent = rightComponent;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == LeftRightKeyListener.LEFT_TRAN) {
                if (leftComponent != null) {
                    leftComponent.requestFocus();
                }
            }
            else if (evt.getPropertyName() == LeftRightKeyListener.RIGHT_TRAN) {
                if (rightComponent != null) {
                    rightComponent.requestFocus();
                }
            }
        }
    }

    public JIpAddressField() {
        this("0.0.0.0");
    }

    public JIpAddressField(String strIpAddress) {
        setPreferredSize(new Dimension(250, 25));
        this.setLayout(new GridLayout(1, 9, 0, 0));
        ip1.setBorder(null);
        ip2.setBorder(null);
        ip3.setBorder(null);
        ip4.setBorder(null);
        this.add(createDot(""));
        this.add(ip1);
        this.add(createDot("."));
        this.add(ip2);
        this.add(createDot("."));
        this.add(ip3);
        this.add(createDot("."));
        this.add(ip4);
        this.add(createDot(""));
        this.setFocusable(false);
        setIpAddress(strIpAddress);
        LeftRightKeyListener lrKeyl = new LeftRightKeyListener();
        ip1.addKeyListener(lrKeyl);
        ip2.addKeyListener(lrKeyl);
        ip3.addKeyListener(lrKeyl);
        ip4.addKeyListener(lrKeyl);
        ip1.addPropertyChangeListener(new LRTransListener(null, ip2));
        ip2.addPropertyChangeListener(new LRTransListener(ip1, ip3));
        ip3.addPropertyChangeListener(new LRTransListener(ip2, ip4));
        ip4.addPropertyChangeListener(new LRTransListener(ip3, null));
    }

    private JLabel createDot(String dot) {
        JLabel lb = new JLabel(dot);
        lb.setOpaque(false);
        return lb;
    }

    /**
     * ����IP��ַ
     * @param strIpAddress String
     */
    public void setIpAddress(String strIpAddress) {
        if (strIpAddress != null && strIpAddress.equals("")) {
            ip1.setText("");
            ip2.setText("");
            ip3.setText("");
            ip4.setText("");
        }
        else {

            if (strIpAddress == null || (strIpAddress.indexOf(".") == -1)
                || (strIpAddress.split("\\.").length != 4)) {
                throw new UnsupportedOperationException("Invalid IP Address:" +
                    strIpAddress);
            }
            String strIp[] = strIpAddress.split("\\.");
            ip1.setText(strIp[0]);
            ip2.setText(strIp[1]);
            ip3.setText(strIp[2]);
            ip4.setText(strIp[3]);
        }
    }

    /**
     * ȡ��IP��ַ
     * @return String
     */
    public String getIpAddress() {
        String strIp1 = ip1.getText().trim();
        String strIp2 = ip2.getText().trim();
        String strIp3 = ip3.getText().trim();
        String strIp4 = ip4.getText().trim();
        return (strIp1.equals("") ? "0" : strIp1) + "." +
            (strIp2.equals("") ? "0" : strIp2) + "." +
            (strIp3.equals("") ? "0" : strIp3) + "." +
            (strIp4.equals("") ? "0" : strIp4);
    }

    public String getText() {
        return getIpAddress();
    }

    public static void main(String[] args) {
        JFrame frame = OpenSwingUtil.createDemoFrame("JIPAddressField Demo");
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JIpAddressField());
        frame.getContentPane().add(new JIpAddressField("255.255.255.255"));
        JIpAddressField ip = new JIpAddressField();
        ip.setIpAddress("192.168.0.122");
        frame.getContentPane().add(ip);
        frame.setVisible(true);
    }
}
